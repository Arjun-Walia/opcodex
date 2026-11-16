/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.debug;

import java.math.BigInteger;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.app.emulator.EmulatorHelper;
import ghidra.pcode.emulate.BreakCallBack;
import ghidra.pcode.pcoderaw.PcodeOpRaw;
import ghidra.program.model.pcode.Varnode;
import ghidra.pcode.memstate.MemoryFaultHandler;
import ghidra.program.model.address.Address;
import ghidra.program.model.address.AddressSpace;
import ghidra.program.model.lang.Language;
import ghidra.program.model.lang.Register;
import ghidra.program.model.listing.*;
import ghidra.program.model.mem.MemoryBlock;
import ghidra.program.model.symbol.Reference;
import ghidra.util.task.TaskMonitor;

/**
 * One emulated debugging session over a program. Code is emulated instruction by instruction from
 * the program's bytes; nothing runs natively and the program itself is never changed. Calls into
 * imported functions are stepped over with a zero result, since their code isn't in the program.
 */
public final class DebugSession implements AutoCloseable {

	public static final int MAX_RUN_INSTRUCTIONS = 2_000_000;
	private static final int STACK_SLOTS = 16;

	private final Program program;
	private final EmulatorHelper emu;
	private final Address sentinel;
	private final Set<Address> breakpoints = new TreeSet<>();
	private final List<Register> shown;
	private Map<String, BigInteger> previous = new HashMap<>();
	private final List<String> events = new ArrayList<>();
	private String reason = "started";
	private boolean running = true;
	private long steps;

	public DebugSession(Program program, Address start, Map<String, BigInteger> registers) {
		this.program = program;
		this.emu = new EmulatorHelper(program);
		emu.setMemoryFaultHandler(new QuietFaults());
		Language language = program.getLanguage();
		// Processor-specific operations without an emulator model (cpuid, rdtsc, xgetbv...) give 0.
		emu.registerDefaultCallOtherCallback(new BreakCallBack() {
			@Override
			public boolean pcodeCallback(PcodeOpRaw op) {
				Varnode out = op.getOutput();
				if (out != null) {
					emu.getEmulator().getMemState().setValue(out, BigInteger.ZERO);
				}
				String name = language.getUserDefinedOpName((int) op.getInput(0).getOffset());
				String note = "Skipped " + (name == null ? "a processor-specific operation" : name) +
					" (not modelled by the emulator; its result is 0).";
				if (!events.contains(note)) {
					events.add(note);
				}
				return true;
			}
		});
		AddressSpace space = start.getAddressSpace();
		int pointer = language.getDefaultSpace().getPointerSize();
		long top = pointer >= 8 ? 0x7ff0_0000_0000L : 0x7ff0_0000L;
		this.sentinel = space.getAddress(pointer >= 8 ? 0x7fee_dead_0000L : 0x7fed_0000L);
		this.shown = RegisterSets.shown(language);
		Register sp = emu.getStackPointerRegister();
		long spValue = top - 0x1000;
		emu.writeRegister(sp, spValue);
		// Return to a sentinel address so we know when the function under test returns.
		Register link = linkRegister(language);
		if (link != null) {
			emu.writeRegister(link, sentinel.getOffset());
		}
		else {
			spValue -= pointer;
			emu.writeRegister(sp, spValue);
			emu.writeMemoryValue(space.getAddress(spValue), pointer, sentinel.getOffset());
		}
		registers.forEach((name, value) -> {
			Register reg = language.getRegister(name);
			if (reg == null) {
				throw new IllegalArgumentException("No register named " + name);
			}
			emu.writeRegister(reg, value);
		});
		emu.writeRegister(emu.getPCRegister(), start.getAddressableWordOffset());
		previous = snapshot();
	}

	private static Register linkRegister(Language language) {
		if (language.getProcessor().toString().toLowerCase(Locale.ROOT).contains("x86")) {
			return null;
		}
		for (String name : List.of("lr", "LR", "ra", "RA", "x30")) {
			Register r = language.getRegister(name);
			if (r != null) {
				return r;
			}
		}
		return null;
	}

	public synchronized JsonObject step(int count) {
		events.clear();
		for (int i = 0; i < count && running; i++) {
			stepOne();
		}
		if (running) {
			reason = "step";
		}
		return state();
	}

	/** Runs a call as one step; anything else steps once. */
	public synchronized JsonObject stepOver() {
		events.clear();
		Instruction instruction = instructionAtPc();
		if (instruction == null || !instruction.getFlowType().isCall() || isExternalCall(instruction)) {
			return step(1);
		}
		Address after = instruction.getFallThrough();
		JsonObject state = runUntil(after);
		if ("reached".equals(reason)) {
			reason = "step"; // stepping over a call is still a step
			state.addProperty("reason", reason);
		}
		return state;
	}

	public synchronized JsonObject run() {
		events.clear();
		return runUntil(null);
	}

	public synchronized JsonObject runTo(Address target) {
		events.clear();
		return runUntil(target);
	}

	private JsonObject runUntil(Address target) {
		long limit = MAX_RUN_INSTRUCTIONS;
		boolean first = true;
		while (running && limit-- > 0) {
			Address pc = emu.getExecutionAddress();
			if (!first) {
				if (target != null && pc.equals(target)) {
					reason = "reached";
					return state();
				}
				if (breakpoints.contains(pc)) {
					reason = "breakpoint";
					return state();
				}
			}
			first = false;
			stepOne();
		}
		if (running) {
			reason = "limit";
			events.add("Stopped after " + MAX_RUN_INSTRUCTIONS + " instructions.");
		}
		return state();
	}

	private void stepOne() {
		Instruction instruction = instructionAtPc();
		if (instruction != null && isExternalCall(instruction)) {
			skipExternalCall(instruction);
			return;
		}
		try {
			boolean ok = emu.step(TaskMonitor.DUMMY);
			steps++;
			if (!ok || emu.getLastError() != null) {
				fail(Objects.requireNonNullElse(emu.getLastError(), "Emulation stopped."));
				return;
			}
		}
		catch (Exception e) {
			fail(e.getMessage() == null ? e.toString() : e.getMessage());
			return;
		}
		Address pc = emu.getExecutionAddress();
		if (pc.equals(sentinel)) {
			running = false;
			reason = "returned";
		}
	}

	private void fail(String message) {
		running = false;
		reason = "error";
		events.add(message);
	}

	private Instruction instructionAtPc() {
		return program.getListing().getInstructionAt(emu.getExecutionAddress());
	}

	private boolean isExternalCall(Instruction instruction) {
		if (!instruction.getFlowType().isCall()) {
			return false;
		}
		return externalTarget(instruction) != null;
	}

	/** An imported function a call goes to. */
	private record Import(String name, boolean noReturn) {
		static Import of(Function f, Address fallback) {
			return f != null ? new Import(f.getName(), f.hasNoReturn()) : new Import(fallback.toString(), false);
		}
	}

	/** The imported function a call goes to, or null for a call into the program. */
	private Import externalTarget(Instruction instruction) {
		FunctionManager functions = program.getFunctionManager();
		for (Reference ref : instruction.getReferencesFrom()) {
			Address to = ref.getToAddress();
			if (to.isExternalAddress()) {
				return Import.of(functions.getFunctionAt(to), to);
			}
			Function f = functions.getFunctionAt(to);
			if (f != null && f.isThunk()) {
				Function thunked = f.getThunkedFunction(true);
				if (thunked != null && thunked.isExternal()) {
					return new Import(thunked.getName(), thunked.hasNoReturn() || f.hasNoReturn());
				}
			}
			if (ref.getReferenceType().isIndirect() || ref.getReferenceType().isData()) {
				// call [IAT slot]: the slot holds a pointer to an import
				for (Reference slot : program.getReferenceManager().getReferencesFrom(to)) {
					if (slot.getToAddress().isExternalAddress()) {
						return Import.of(functions.getFunctionAt(slot.getToAddress()), slot.getToAddress());
					}
				}
			}
		}
		return null;
	}

	private void skipExternalCall(Instruction instruction) {
		Import target = externalTarget(instruction);
		steps++;
		if (target.noReturn()) {
			running = false;
			reason = "exited";
			events.add("Called " + target.name() + ", which doesn't return: the program would end here.");
			return;
		}
		Register result = RegisterSets.returnRegister(program.getLanguage());
		if (result != null) {
			emu.writeRegister(result, 0);
		}
		emu.writeRegister(emu.getPCRegister(), instruction.getFallThrough().getAddressableWordOffset());
		events.add("Stepped over the call to " + target.name() + " (imported; returns 0 in the emulator).");
	}

	public synchronized void setBreakpoint(Address address) {
		breakpoints.add(address);
	}

	public synchronized void clearBreakpoint(Address address) {
		breakpoints.remove(address);
	}

	public synchronized void writeRegister(String name, BigInteger value) {
		Register reg = program.getLanguage().getRegister(name);
		if (reg == null) {
			throw new IllegalArgumentException("No register named " + name);
		}
		emu.writeRegister(reg, value);
	}

	public synchronized byte[] read(Address address, int length) {
		return emu.readMemory(address, length);
	}

	public synchronized JsonObject state() {
		JsonObject out = new JsonObject();
		Address pc = emu.getExecutionAddress();
		out.addProperty("pc", pc.toString());
		out.addProperty("running", running);
		out.addProperty("reason", reason);
		out.addProperty("steps", steps);
		Instruction instruction = program.getListing().getInstructionAt(pc);
		out.addProperty("instruction", instruction == null ? "" : instruction.toString());
		Function function = program.getFunctionManager().getFunctionContaining(pc);
		out.addProperty("function", function == null ? "" : function.getName(true));
		Map<String, BigInteger> now = snapshot();
		JsonArray registers = new JsonArray();
		JsonArray changed = new JsonArray();
		for (Register reg : shown) {
			BigInteger value = now.get(reg.getName());
			JsonObject r = new JsonObject();
			r.addProperty("name", reg.getName());
			r.addProperty("value", "0x" + value.toString(16));
			r.addProperty("bits", reg.getBitLength());
			registers.add(r);
			if (!value.equals(previous.get(reg.getName()))) {
				changed.add(reg.getName());
			}
		}
		previous = now;
		out.add("registers", registers);
		out.add("changed", changed);
		out.add("stack", stack());
		JsonArray points = new JsonArray();
		breakpoints.forEach(b -> points.add(b.toString()));
		out.add("breakpoints", points);
		JsonArray notes = new JsonArray();
		events.forEach(notes::add);
		out.add("events", notes);
		return out;
	}

	private JsonArray stack() {
		JsonArray out = new JsonArray();
		int pointer = program.getLanguage().getDefaultSpace().getPointerSize();
		BigInteger sp = emu.readRegister(emu.getStackPointerRegister());
		AddressSpace space = program.getAddressFactory().getDefaultAddressSpace();
		boolean little = !program.getLanguage().isBigEndian();
		for (int i = 0; i < STACK_SLOTS; i++) {
			Address at = space.getAddress(sp.longValue() + (long) i * pointer);
			byte[] bytes = emu.readMemory(at, pointer);
			byte[] ordered = bytes.clone();
			if (little) {
				for (int a = 0, b = ordered.length - 1; a < b; a++, b--) {
					byte t = ordered[a];
					ordered[a] = ordered[b];
					ordered[b] = t;
				}
			}
			JsonObject slot = new JsonObject();
			slot.addProperty("address", at.toString());
			slot.addProperty("value", "0x" + new BigInteger(1, ordered).toString(16));
			slot.addProperty("offset", i * pointer);
			out.add(slot);
		}
		return out;
	}

	private Map<String, BigInteger> snapshot() {
		Map<String, BigInteger> out = new HashMap<>();
		for (Register reg : shown) {
			out.put(reg.getName(), emu.readRegister(reg));
		}
		return out;
	}

	public boolean belongsTo(Program other) {
		return program == other;
	}

	public boolean inProgramMemory(Address address) {
		MemoryBlock block = program.getMemory().getBlock(address);
		return block != null;
	}

	@Override
	public synchronized void close() {
		running = false;
		emu.dispose();
	}

	/** Reads of memory the program doesn't define (stack, heap) are zero, without log noise. */
	private final class QuietFaults implements MemoryFaultHandler {
		@Override
		public boolean uninitializedRead(Address address, int size, byte[] buf, int bufOffset) {
			return emu.getEmulateExecutionState() != ghidra.pcode.emulate.EmulateExecutionState.INSTRUCTION_DECODE;
		}

		@Override
		public boolean unknownAddress(Address address, boolean write) {
			return false;
		}
	}
}

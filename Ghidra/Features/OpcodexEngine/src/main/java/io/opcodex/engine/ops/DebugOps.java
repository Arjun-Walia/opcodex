/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.debug.DebugSession;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/**
 * The debugger: an emulated session per program. Emulation reads the program's bytes and changes
 * nothing in the program or on this computer, so these are read operations.
 */
final class DebugOps {

	private static final Map<String, DebugSession> SESSIONS = new ConcurrentHashMap<>();

	private DebugOps() {
	}

	private interface Body {
		JsonObject run(Program program, DebugSession session, JsonObject params) throws RpcException;
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject start = FunctionOps.programParam();
		Schemas.property(start, "address", Schemas.string("Where to start, usually a function's entry."), true);
		Schemas.property(start, "registers",
			Schemas.any("Starting register values, for example {\"RDI\": \"5\", \"RSI\": \"0x10\"}."), false);
		JsonObject plain = FunctionOps.programParam();
		JsonObject step = FunctionOps.programParam();
		Schemas.property(step, "count", Schemas.integer("Instructions to step (1 to 10000).", 1, 10000), false);
		JsonObject at = FunctionOps.programParam();
		Schemas.property(at, "address", Schemas.string("An address in the program."), true);
		JsonObject reg = FunctionOps.programParam();
		Schemas.property(reg, "name", Schemas.string("Register name, for example RAX."), true);
		Schemas.property(reg, "value", Schemas.string("New value, decimal or 0x hex."), true);
		JsonObject mem = FunctionOps.programParam();
		Schemas.property(mem, "address", Schemas.string("First address to read."), true);
		Schemas.property(mem, "length", Schemas.integer("Bytes to read (1 to 4096).", 1, 4096), true);
		JsonObject state = Schemas.any("Debugger state: pc, instruction, registers, changed, stack, breakpoints.");

		return List.of(
			new Operation("debug.start",
				"Starts debugging at an address by emulating the program's code. Nothing runs natively, and " +
					"calls to imported functions are stepped over.",
				OperationClass.READ, start, state, (ctx, p) -> start(services, p)),
			new Operation("debug.state", "Shows the debugger's current state.", OperationClass.READ, plain, state,
				with(services, (program, s, p) -> s.state())),
			new Operation("debug.step", "Steps one instruction, or count instructions, stepping into calls.",
				OperationClass.READ, step, state,
				with(services, (program, s, p) -> s.step(Params.integer(p, "count", 1)))),
			new Operation("debug.stepOver", "Steps one instruction, running a whole call as one step.",
				OperationClass.READ, plain, state, with(services, (program, s, p) -> s.stepOver())),
			new Operation("debug.run",
				"Runs until a breakpoint, the function returns, an error, or " + DebugSession.MAX_RUN_INSTRUCTIONS +
					" instructions.",
				OperationClass.READ, plain, state, with(services, (program, s, p) -> s.run())),
			new Operation("debug.runTo", "Runs until an address is reached (or a breakpoint, return or error).",
				OperationClass.READ, at, state,
				with(services, (program, s, p) -> s.runTo(address(program, p, "address")))),
			new Operation("debug.breakpoint.set", "Sets a breakpoint at an address.", OperationClass.READ, at, state,
				with(services, (program, s, p) -> {
					s.setBreakpoint(address(program, p, "address"));
					return s.state();
				})),
			new Operation("debug.breakpoint.clear", "Removes the breakpoint at an address.", OperationClass.READ, at,
				state, with(services, (program, s, p) -> {
					s.clearBreakpoint(address(program, p, "address"));
					return s.state();
				})),
			new Operation("debug.register.set", "Changes a register in the debugger.", OperationClass.READ, reg,
				state, with(services, (program, s, p) -> {
					try {
						s.writeRegister(Params.requireString(p, "name"), number(Params.requireString(p, "value")));
					}
					catch (IllegalArgumentException e) {
						throw new RpcException(ErrorCode.INVALID_PARAMS, e.getMessage());
					}
					return s.state();
				})),
			new Operation("debug.memory.read", "Reads memory as the debugged code sees it, including the stack.",
				OperationClass.READ, mem, Schemas.any("Address and hex bytes."),
				with(services, (program, s, p) -> {
					Address from = address(program, p, "address");
					byte[] bytes = s.read(from, Params.integer(p, "length", 16));
					JsonObject out = new JsonObject();
					out.addProperty("address", from.toString());
					out.addProperty("hex", HexFormat.of().formatHex(bytes));
					return out;
				})),
			new Operation("debug.stop", "Ends the debugging session.", OperationClass.READ, plain,
				Schemas.any("{stopped: true}"), (ctx, p) -> {
					DebugSession old = SESSIONS.remove(Params.requireString(p, "programId"));
					if (old != null) {
						old.close();
					}
					JsonObject out = new JsonObject();
					out.addProperty("stopped", true);
					return out;
				}));
	}

	private static JsonObject start(EngineServices services, JsonObject p) throws RpcException {
		String id = Params.requireString(p, "programId");
		Program program = FunctionOps.program(services, p);
		Address address = address(program, p, "address");
		if (program.getListing().getInstructionAt(address) == null) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "There's no instruction at " + address +
				". Start at a function's entry or another disassembled address.");
		}
		Map<String, BigInteger> registers = new LinkedHashMap<>();
		JsonElement given = p.get("registers");
		if (given != null && !given.isJsonNull()) {
			if (!given.isJsonObject()) {
				throw new RpcException(ErrorCode.INVALID_PARAMS, "registers must be an object of name to value.");
			}
			for (Map.Entry<String, JsonElement> e : given.getAsJsonObject().entrySet()) {
				registers.put(e.getKey(), number(e.getValue().getAsString()));
			}
		}
		DebugSession session;
		try {
			session = new DebugSession(program, address, registers);
		}
		catch (IllegalArgumentException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, e.getMessage());
		}
		DebugSession old = SESSIONS.put(id, session);
		if (old != null) {
			old.close();
		}
		return session.state();
	}

	private static OperationHandler with(EngineServices services, Body body) {
		return (ctx, p) -> {
			String id = Params.requireString(p, "programId");
			Program program = FunctionOps.program(services, p);
			DebugSession session = SESSIONS.get(id);
			if (session != null && !session.belongsTo(program)) {
				SESSIONS.remove(id, session);
				session.close();
				session = null;
			}
			if (session == null) {
				throw new RpcException(ErrorCode.NOT_FOUND, "No debugging session. Start one with debug.start.");
			}
			return body.run(program, session, p);
		};
	}

	private static Address address(Program program, JsonObject p, String key) throws RpcException {
		return Params.address(program, key, Params.requireString(p, key));
	}

	private static BigInteger number(String text) throws RpcException {
		String t = text.trim().toLowerCase(Locale.ROOT);
		try {
			boolean negative = t.startsWith("-");
			String digits = negative ? t.substring(1) : t;
			BigInteger value = digits.startsWith("0x") ? new BigInteger(digits.substring(2), 16) : new BigInteger(digits);
			return negative ? value.negate() : value;
		}
		catch (NumberFormatException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "Not a number: " + text);
		}
	}
}

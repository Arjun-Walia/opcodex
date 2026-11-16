/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.debug;

import java.util.*;

import ghidra.program.model.lang.Language;
import ghidra.program.model.lang.Register;

/** Which registers a debugger shows for a processor, and where a function's result goes. */
final class RegisterSets {

	private static final List<String> X64 = List.of("RAX", "RBX", "RCX", "RDX", "RSI", "RDI", "RBP", "RSP",
		"R8", "R9", "R10", "R11", "R12", "R13", "R14", "R15", "RIP", "ZF", "CF", "SF", "OF");
	private static final List<String> X86 = List.of("EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "EBP", "ESP",
		"EIP", "ZF", "CF", "SF", "OF");
	private static final List<String> AARCH64 = List.of("x0", "x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8",
		"x19", "x20", "x29", "x30", "sp", "pc", "NG", "ZR", "CY", "OV");
	private static final List<String> ARM = List.of("r0", "r1", "r2", "r3", "r4", "r5", "r6", "r7", "r8",
		"r9", "r10", "r11", "r12", "sp", "lr", "pc", "NG", "ZR", "CY", "OV");
	private static final int GENERIC_LIMIT = 24;

	private RegisterSets() {
	}

	static List<Register> shown(Language language) {
		for (List<String> names : List.of(X64, X86, AARCH64, ARM)) {
			List<Register> found = resolve(language, names);
			if (found.size() >= names.size() - 4) {
				return found;
			}
		}
		// Anything else: the program counter, stack pointer and pointer-sized general registers.
		int bits = language.getDefaultSpace().getPointerSize() * 8;
		LinkedHashSet<Register> out = new LinkedHashSet<>();
		out.add(language.getProgramCounter());
		for (Register reg : language.getRegisters()) {
			if (out.size() >= GENERIC_LIMIT) {
				break;
			}
			if (reg.isBaseRegister() && !reg.isProcessorContext() && !reg.isHidden() && reg.getBitLength() == bits) {
				out.add(reg);
			}
		}
		return List.copyOf(out);
	}

	private static List<Register> resolve(Language language, List<String> names) {
		List<Register> out = new ArrayList<>();
		for (String name : names) {
			Register reg = language.getRegister(name);
			if (reg != null) {
				out.add(reg);
			}
		}
		return out;
	}

	/** The register that holds an integer result under the usual calling convention. */
	static Register returnRegister(Language language) {
		for (String name : List.of("RAX", "EAX", "x0", "r0", "v0", "a0")) {
			Register reg = language.getRegister(name);
			if (reg != null) {
				return reg;
			}
		}
		return null;
	}
}

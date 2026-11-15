/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.app.decompiler.*;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.*;
import ghidra.program.model.mem.MemoryAccessException;
import ghidra.program.model.pcode.HighVariable;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.ProgramHandle;

/** Decompiled C and disassembly. */
final class CodeOps {

	private CodeOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject decompileParams = FunctionOps.programParam();
		Schemas.property(decompileParams, "address",
			Schemas.string("Any address inside the function."), true);
		Schemas.property(decompileParams, "timeoutSec",
			Schemas.integer("Give up after this many seconds (default 30).", 1, 600), false);

		JsonObject disassembleParams = FunctionOps.programParam();
		Schemas.property(disassembleParams, "address",
			Schemas.string("Address of the first instruction."), true);
		Schemas.property(disassembleParams, "count",
			Schemas.integer("Number of instructions (default 64).", 1, 2000), false);

		return List.of(
			new Operation("code.decompile",
				"Decompiles the function containing an address to C. Tokens link text to " +
					"addresses and variables.",
				OperationClass.READ, decompileParams, Schemas.any("Decompiled function."),
				(ctx, params) -> decompile(services, params)),
			new Operation("code.disassemble",
				"Lists instructions starting at an address, with bytes, mnemonic and operands.",
				OperationClass.READ, disassembleParams, Schemas.any("Instructions."),
				(ctx, params) -> disassemble(services, params)));
	}

	private static JsonObject decompile(EngineServices services, JsonObject params)
			throws RpcException {
		ProgramHandle handle =
			services.workspace().get(Params.requireString(params, "programId"));
		Program program = handle.program();
		Function function = FunctionOps.containing(program, params);
		DecompilerPool pool =
			handle.attachment(DecompilerPool.class, () -> new DecompilerPool(program));
		DecompileResults results = pool.decompile(function, Params.integer(params, "timeoutSec", 30));

		JsonObject out = new JsonObject();
		out.addProperty("entry", function.getEntryPoint().toString());
		out.addProperty("name", function.getName(true));
		out.addProperty("c", results.getDecompiledFunction().getC());
		out.add("tokens", tokens(results.getCCodeMarkup()));
		return out;
	}

	private static JsonArray tokens(ClangTokenGroup markup) {
		JsonArray out = new JsonArray();
		if (markup == null) {
			return out;
		}
		List<ClangNode> nodes = new ArrayList<>();
		markup.flatten(nodes);
		for (ClangNode node : nodes) {
			if (!(node instanceof ClangToken token)) {
				continue;
			}
			JsonObject t = new JsonObject();
			if (token instanceof ClangBreak br) {
				t.addProperty("text", "\n" + " ".repeat(Math.max(0, br.getIndent())));
				t.addProperty("kind", "break");
				out.add(t);
				continue;
			}
			t.addProperty("text", token.getText());
			t.addProperty("kind", kind(token));
			Address address = token.getMinAddress();
			if (address != null) {
				t.addProperty("address", address.toString());
			}
			HighVariable variable = token.getHighVariable();
			if (variable != null && variable.getName() != null) {
				t.addProperty("varName", variable.getName());
			}
			out.add(t);
		}
		return out;
	}

	private static String kind(ClangToken token) {
		String simple = token.getClass().getSimpleName();
		String trimmed = simple.replaceFirst("^Clang", "").replaceFirst("Token$", "");
		if (trimmed.isEmpty()) {
			return "text";
		}
		return Character.toLowerCase(trimmed.charAt(0)) + trimmed.substring(1);
	}

	private static JsonObject disassemble(EngineServices services, JsonObject params)
			throws RpcException {
		Program program = FunctionOps.program(services, params);
		Address start = Params.address(program, "address", Params.requireString(params, "address"));
		int count = Params.integer(params, "count", 64);
		InstructionIterator instructions = program.getListing().getInstructions(start, true);
		JsonArray items = new JsonArray();
		HexFormat hex = HexFormat.of();
		while (instructions.hasNext() && items.size() < count) {
			Instruction instruction = instructions.next();
			JsonObject item = new JsonObject();
			item.addProperty("address", instruction.getAddress().toString());
			try {
				item.addProperty("bytes", hex.formatHex(instruction.getBytes()));
			}
			catch (MemoryAccessException e) {
				item.addProperty("bytes", "");
			}
			item.addProperty("mnemonic", instruction.getMnemonicString());
			List<String> operands = new ArrayList<>();
			for (int i = 0; i < instruction.getNumOperands(); i++) {
				operands.add(instruction.getDefaultOperandRepresentation(i));
			}
			item.addProperty("operands", String.join(",", operands));
			items.add(item);
		}
		JsonObject out = new JsonObject();
		out.add("items", items);
		return out;
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.program.model.address.Address;
import ghidra.program.model.block.*;
import ghidra.program.model.listing.*;
import ghidra.program.model.symbol.FlowType;
import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitor;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** Control-flow graphs of functions and call graphs around them. */
final class GraphOps {

	static final int MAX_BLOCKS = 2000;
	static final int MAX_INSTRUCTIONS_PER_BLOCK = 300;

	private GraphOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject graphParams = FunctionOps.programParam();
		Schemas.property(graphParams, "address", Schemas.string("Any address inside the function."), true);
		JsonObject callParams = FunctionOps.programParam();
		Schemas.property(callParams, "address", Schemas.string("Any address inside the function."), true);
		Schemas.property(callParams, "depth",
			Schemas.integer("How many calls away to follow, in each direction (1 to 3).", 1, 3), false);
		Schemas.property(callParams, "limit",
			Schemas.integer("Most functions to include (1 to 300).", 1, 300), false);
		return List.of(
			new Operation("function.graph",
				"Shows a function's basic blocks with their instructions, and the branches between them " +
					"(taken, not taken, jump or fall-through).",
				OperationClass.READ, graphParams, Schemas.any("Blocks and edges."),
				(ctx, params) -> {
					Program program = FunctionOps.program(services, params);
					return graph(program, FunctionOps.containing(program, params));
				}),
			new Operation("function.callgraph",
				"Shows the functions that call a function and the functions it calls, a few calls deep.",
				OperationClass.READ, callParams, Schemas.any("Functions and calls."),
				(ctx, params) -> {
					Program program = FunctionOps.program(services, params);
					int depth = Params.integer(params, "depth", 1);
					int limit = Params.integer(params, "limit", 100);
					return callGraph(FunctionOps.containing(program, params), depth, limit);
				}));
	}

	static JsonObject graph(Program program, Function function) throws RpcException {
		try {
			return blocks(program, function);
		}
		catch (CancelledException e) {
			throw new RpcException(ErrorCode.CANCELLED, "Cancelled.");
		}
	}

	private static JsonObject blocks(Program program, Function function) throws CancelledException {
		TaskMonitor monitor = TaskMonitor.DUMMY;
		BasicBlockModel model = new BasicBlockModel(program);
		List<CodeBlock> blocks = new ArrayList<>();
		CodeBlockIterator it = model.getCodeBlocksContaining(function.getBody(), monitor);
		boolean truncated = false;
		while (it.hasNext()) {
			if (blocks.size() == MAX_BLOCKS) {
				truncated = true;
				break;
			}
			blocks.add(it.next());
		}
		blocks.sort(Comparator.comparing(CodeBlock::getFirstStartAddress));
		Set<Address> starts = new HashSet<>();
		blocks.forEach(b -> starts.add(b.getFirstStartAddress()));

		Listing listing = program.getListing();
		JsonArray blockJson = new JsonArray();
		JsonArray edges = new JsonArray();
		for (CodeBlock block : blocks) {
			JsonObject b = new JsonObject();
			b.addProperty("start", block.getFirstStartAddress().toString());
			b.addProperty("end", block.getMaxAddress().toString());
			JsonArray instructions = new JsonArray();
			Instruction last = null;
			for (Instruction instruction : listing.getInstructions(block, true)) {
				last = instruction;
				if (instructions.size() < MAX_INSTRUCTIONS_PER_BLOCK) {
					JsonObject i = new JsonObject();
					i.addProperty("address", instruction.getAddress().toString());
					i.addProperty("text", instruction.toString());
					String comment = CodeOps.comments(instruction);
					if (comment != null) {
						i.addProperty("comment", comment);
					}
					instructions.add(i);
				}
				else {
					truncated = true;
				}
			}
			b.add("instructions", instructions);
			blockJson.add(b);
			boolean conditional = last != null && last.getFlowType().isConditional() && last.getFlowType().isJump();
			CodeBlockReferenceIterator dests = block.getDestinations(monitor);
			while (dests.hasNext()) {
				CodeBlockReference ref = dests.next();
				Address to = ref.getDestinationBlock().getFirstStartAddress();
				if (!starts.contains(to)) {
					continue; // calls and branches out of the function aren't part of its graph
				}
				FlowType flow = ref.getFlowType();
				String kind;
				if (flow.isFallthrough()) {
					kind = conditional ? "false" : "fall";
				}
				else if (flow.isJump()) {
					kind = flow.isConditional() ? "true" : "jump";
				}
				else {
					continue;
				}
				JsonObject e = new JsonObject();
				e.addProperty("from", block.getFirstStartAddress().toString());
				e.addProperty("to", to.toString());
				e.addProperty("kind", kind);
				edges.add(e);
			}
		}
		JsonObject out = new JsonObject();
		out.addProperty("entry", function.getEntryPoint().toString());
		out.addProperty("name", function.getName(true));
		out.add("blocks", blockJson);
		out.add("edges", edges);
		out.addProperty("truncated", truncated);
		return out;
	}

	static JsonObject callGraph(Function root, int depth, int limit) {
		Map<Function, Integer> seen = new LinkedHashMap<>();
		Set<String> edgeKeys = new LinkedHashSet<>();
		JsonArray edges = new JsonArray();
		seen.put(root, 0);
		boolean truncated = false;
		for (boolean callers : new boolean[] { true, false }) {
			Deque<Function> queue = new ArrayDeque<>(List.of(root));
			Map<Function, Integer> distance = new HashMap<>(Map.of(root, 0));
			while (!queue.isEmpty()) {
				Function current = queue.poll();
				int d = distance.get(current);
				if (d == depth) {
					continue;
				}
				Set<Function> next = callers ? current.getCallingFunctions(TaskMonitor.DUMMY)
						: current.getCalledFunctions(TaskMonitor.DUMMY);
				for (Function other : sorted(next)) {
					if (!seen.containsKey(other)) {
						if (seen.size() == limit) {
							truncated = true;
							continue;
						}
						seen.put(other, callers ? -(d + 1) : d + 1);
					}
					Function from = callers ? other : current;
					Function to = callers ? current : other;
					String key = from.getEntryPoint() + ">" + to.getEntryPoint();
					if (edgeKeys.add(key)) {
						JsonObject e = new JsonObject();
						e.addProperty("from", from.getEntryPoint().toString());
						e.addProperty("to", to.getEntryPoint().toString());
						edges.add(e);
					}
					if (!distance.containsKey(other)) {
						distance.put(other, d + 1);
						queue.add(other);
					}
				}
			}
		}
		JsonArray nodes = new JsonArray();
		seen.forEach((f, level) -> {
			JsonObject n = new JsonObject();
			n.addProperty("entry", f.getEntryPoint().toString());
			n.addProperty("name", f.getName(true));
			n.addProperty("level", level);
			n.addProperty("external", f.isExternal() || f.isThunk() && f.getThunkedFunction(true) != null &&
				f.getThunkedFunction(true).isExternal());
			nodes.add(n);
		});
		JsonObject out = new JsonObject();
		out.addProperty("root", root.getEntryPoint().toString());
		out.add("nodes", nodes);
		out.add("edges", edges);
		out.addProperty("truncated", truncated);
		return out;
	}

	private static List<Function> sorted(Set<Function> functions) {
		List<Function> out = new ArrayList<>(functions);
		out.sort(Comparator.comparing(Function::getEntryPoint));
		return out;
	}
}

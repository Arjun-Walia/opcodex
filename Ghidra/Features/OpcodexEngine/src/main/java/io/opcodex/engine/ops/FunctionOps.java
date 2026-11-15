/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.List;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.program.model.address.*;
import ghidra.program.model.listing.*;
import ghidra.util.task.TaskMonitor;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** Listing and describing functions. */
final class FunctionOps {

	private FunctionOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject listParams = Paging.addTo(programParam());
		Schemas.property(listParams, "query",
			Schemas.string("Only functions whose name contains this text (case-insensitive)."),
			false);
		JsonObject getParams = programParam();
		Schemas.property(getParams, "address",
			Schemas.string("Any address inside the function."), true);

		return List.of(
			new Operation("program.functions.list",
				"Lists functions in address order, a page at a time, with size and call counts.",
				OperationClass.READ, listParams, Schemas.any("A page of functions."),
				(ctx, params) -> list(services, params)),
			new Operation("function.get",
				"Describes the function containing an address: signature, body, callers and " +
					"callees.",
				OperationClass.READ, getParams, Schemas.any("Function details."),
				(ctx, params) -> {
					Program program = program(services, params);
					return describe(containing(program, params));
				}));
	}

	static JsonObject programParam() {
		JsonObject schema = Schemas.object();
		Schemas.property(schema, "programId", Schemas.string("Program id from program.list."),
			true);
		return schema;
	}

	static Program program(EngineServices services, JsonObject params) throws RpcException {
		return services.workspace().get(Params.requireString(params, "programId")).program();
	}

	static Function containing(Program program, JsonObject params) throws RpcException {
		Address address = Params.address(program, "address", Params.requireString(params, "address"));
		Function function = program.getFunctionManager().getFunctionContaining(address);
		if (function == null) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No function contains " + address);
		}
		return function;
	}

	private static JsonObject list(EngineServices services, JsonObject params)
			throws RpcException {
		Program program = program(services, params);
		int limit = Paging.limit(params);
		String query = Params.string(params, "query");
		String needle = query == null ? null : query.toLowerCase(Locale.ROOT);
		String cursor = Params.string(params, "cursor");
		FunctionIterator functions;
		if (cursor == null) {
			functions = program.getFunctionManager().getFunctions(true);
		}
		else {
			Address after = Params.address(program, "cursor", cursor).next();
			if (after == null) {
				return Paging.page(new JsonArray(), null);
			}
			functions = program.getFunctionManager().getFunctions(after, true);
		}
		JsonArray items = new JsonArray();
		Function last = null;
		while (functions.hasNext()) {
			Function function = functions.next();
			if (needle != null && !function.getName().toLowerCase(Locale.ROOT).contains(needle)) {
				continue;
			}
			if (items.size() == limit) {
				return Paging.page(items, last.getEntryPoint().toString());
			}
			items.add(summary(function));
			last = function;
		}
		return Paging.page(items, null);
	}

	static JsonObject summary(Function function) {
		JsonObject item = new JsonObject();
		item.addProperty("entry", function.getEntryPoint().toString());
		item.addProperty("name", function.getName(true));
		item.addProperty("size", function.getBody().getNumAddresses());
		item.addProperty("callers", function.getCallingFunctions(TaskMonitor.DUMMY).size());
		item.addProperty("callees", function.getCalledFunctions(TaskMonitor.DUMMY).size());
		return item;
	}

	private static JsonObject describe(Function function) {
		JsonObject out = new JsonObject();
		out.addProperty("entry", function.getEntryPoint().toString());
		out.addProperty("name", function.getName(true));
		out.addProperty("signature", function.getPrototypeString(false, false));
		JsonArray body = new JsonArray();
		for (AddressRange range : function.getBody()) {
			JsonObject r = new JsonObject();
			r.addProperty("start", range.getMinAddress().toString());
			r.addProperty("end", range.getMaxAddress().toString());
			body.add(r);
		}
		out.add("body", body);
		out.add("callers", refs(function.getCallingFunctions(TaskMonitor.DUMMY)));
		out.add("callees", refs(function.getCalledFunctions(TaskMonitor.DUMMY)));
		return out;
	}

	private static JsonArray refs(java.util.Set<Function> functions) {
		JsonArray out = new JsonArray();
		functions.stream()
				.sorted((a, b) -> a.getEntryPoint().compareTo(b.getEntryPoint()))
				.forEach(f -> {
					JsonObject ref = new JsonObject();
					ref.addProperty("entry", f.getEntryPoint().toString());
					ref.addProperty("name", f.getName(true));
					out.add(ref);
				});
		return out;
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.program.model.address.*;
import ghidra.program.model.data.StringDataInstance;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Program;
import ghidra.program.util.DefinedStringIterator;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** Defined strings in a program. */
final class StringOps {

	private StringOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject params = Paging.addTo(FunctionOps.programParam());
		Schemas.property(params, "minLength",
			Schemas.integer("Shortest string to include (default 4).", 1, 4096), false);
		return List.of(new Operation("program.strings.list",
			"Lists defined strings in address order, with their encoding.", OperationClass.READ,
			params, Schemas.any("A page of strings."), (ctx, p) -> page(services, p)));
	}

	private static JsonObject page(EngineServices services, JsonObject params)
			throws RpcException {
		Program program = FunctionOps.program(services, params);
		int minLength = Params.integer(params, "minLength", 4);
		int limit = Paging.limit(params);
		AddressSetView scope = program.getMemory();
		String cursor = Params.string(params, "cursor");
		if (cursor != null) {
			Address after = Params.address(program, "cursor", cursor).next();
			if (after == null) {
				return Paging.page(new JsonArray(), null);
			}
			scope = scope.intersect(new AddressSet(after, program.getMaxAddress()));
		}
		JsonArray items = new JsonArray();
		Address last = null;
		for (Data data : DefinedStringIterator.forProgram(program, scope)) {
			StringDataInstance string = StringDataInstance.getStringDataInstance(data);
			String value = string.getStringValue();
			if (value == null || value.length() < minLength) {
				continue;
			}
			if (items.size() == limit) {
				return Paging.page(items, last.toString());
			}
			JsonObject item = new JsonObject();
			item.addProperty("address", data.getAddress().toString());
			item.addProperty("value", value);
			item.addProperty("encoding", string.getCharsetName());
			items.add(item);
			last = data.getAddress();
		}
		return Paging.page(items, null);
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.model.symbol.*;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** Cross-references to and from an address. */
final class XrefOps {

	private XrefOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject params = Paging.addTo(FunctionOps.programParam());
		Schemas.property(params, "address", Schemas.string("Address."), true);
		return List.of(
			new Operation("xrefs.to", "Lists references that point to an address.",
				OperationClass.READ, params, Schemas.any("A page of references."),
				(ctx, p) -> page(services, p, true)),
			new Operation("xrefs.from", "Lists references made from an address.",
				OperationClass.READ, params.deepCopy(), Schemas.any("A page of references."),
				(ctx, p) -> page(services, p, false)));
	}

	private static JsonObject page(EngineServices services, JsonObject params, boolean to)
			throws RpcException {
		Program program = FunctionOps.program(services, params);
		Address address = Params.address(program, "address", Params.requireString(params, "address"));
		ReferenceManager refs = program.getReferenceManager();
		List<Reference> all = new ArrayList<>();
		if (to) {
			refs.getReferencesTo(address).forEach(all::add);
		}
		else {
			all.addAll(Arrays.asList(refs.getReferencesFrom(address)));
		}
		Comparator<Reference> order = Comparator
				.comparing((Reference r) -> to ? r.getFromAddress() : r.getToAddress())
				.thenComparing(r -> r.getReferenceType().getName());
		all.sort(order);

		int start = 0;
		String cursor = Params.string(params, "cursor");
		if (cursor != null) {
			try {
				start = Integer.parseUnsignedInt(cursor);
			}
			catch (NumberFormatException e) {
				throw new RpcException(io.opcodex.engine.rpc.ErrorCode.INVALID_PARAMS,
					"cursor must be the nextCursor of a previous page.");
			}
		}
		int limit = Paging.limit(params);
		JsonArray items = new JsonArray();
		for (int i = start; i < all.size() && items.size() < limit; i++) {
			Reference ref = all.get(i);
			JsonObject item = new JsonObject();
			item.addProperty("from", ref.getFromAddress().toString());
			item.addProperty("to", ref.getToAddress().toString());
			item.addProperty("type", ref.getReferenceType().getName());
			items.add(item);
		}
		int next = start + items.size();
		return Paging.page(items, next < all.size() ? Integer.toString(next) : null);
	}
}

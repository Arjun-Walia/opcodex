/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.io.IOException;
import java.util.List;

import com.google.gson.*;

import io.opcodex.engine.catalog.*;
import io.opcodex.engine.journal.JournalEntry;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;

/** Browsing and selectively reverting recorded changes. */
final class HistoryOps {

	private HistoryOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject list = Paging.addTo(FunctionOps.programParam());
		Schemas.property(list, "actor",
			Schemas.string("Only changes by this actor: desktop, cli or mcp:<client>."), false);

		JsonObject revert = FunctionOps.programParam();
		Schemas.property(revert, "entryId", Schemas.string("History entry to revert."), true);
		EditOps.revisionParam(revert);

		return List.of(
			new Operation("history.list",
				"Lists recorded changes to a program, newest first, with who made them and the " +
					"values before and after.",
				OperationClass.READ, list, Schemas.any("A page of history entries."),
				(ctx, params) -> list(services, params)),
			new Operation("history.revert",
				"Reverts one recorded change, if the value hasn't changed again since. The revert " +
					"is recorded too.",
				OperationClass.WRITE, revert, Schemas.any("New revision and history entry."),
				(ctx, params) -> {
					String programId = Params.requireString(params, "programId");
					JournalEntry entry = find(services, Params.requireString(params, "entryId"));
					if (!entry.programId().equals(programId) ||
						!EditOps.REVERTIBLE.contains(entry.op())) {
						throw new RpcException(ErrorCode.NOT_FOUND,
							"No revertible change with that id in this program.");
					}
					return EditOps.change(services, ctx, entry.op(), params, entry.target(),
						orNull(entry.before()), orNull(entry.after()), entry.id());
				}));
	}

	private static JsonElement orNull(JsonElement value) {
		return value == null ? JsonNull.INSTANCE : value;
	}

	private static JournalEntry find(EngineServices services, String id) throws RpcException {
		try {
			return services.journal().find(id).orElseThrow(
				() -> new RpcException(ErrorCode.NOT_FOUND, "No history entry " + id));
		}
		catch (IOException e) {
			throw new RpcException(ErrorCode.ENGINE_ERROR, "Couldn't read the history.");
		}
	}

	private static JsonObject list(EngineServices services, JsonObject params)
			throws RpcException {
		String programId = Params.requireString(params, "programId");
		String actor = Params.string(params, "actor");
		List<JournalEntry> entries;
		try {
			entries = services.journal().newestFirst(e -> e.programId().equals(programId) &&
				(actor == null || actor.equals(e.actor())));
		}
		catch (IOException e) {
			throw new RpcException(ErrorCode.ENGINE_ERROR, "Couldn't read the history.");
		}
		int start = 0;
		String cursor = Params.string(params, "cursor");
		if (cursor != null) {
			start = -1;
			for (int i = 0; i < entries.size(); i++) {
				if (entries.get(i).id().equals(cursor)) {
					start = i + 1;
					break;
				}
			}
			if (start < 0) {
				throw new RpcException(ErrorCode.INVALID_PARAMS,
					"cursor must be the nextCursor of a previous page.");
			}
		}
		int limit = Paging.limit(params);
		JsonArray items = new JsonArray();
		int i = start;
		for (; i < entries.size() && items.size() < limit; i++) {
			items.add(entries.get(i).toJson());
		}
		String next = i < entries.size() ? entries.get(i - 1).id() : null;
		return Paging.page(items, next);
	}
}

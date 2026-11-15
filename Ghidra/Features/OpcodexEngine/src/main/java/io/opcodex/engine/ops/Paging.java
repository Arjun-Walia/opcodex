/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.opcodex.engine.catalog.Schemas;

/** Shared paging parameters and result shape: {@code {items, nextCursor?}}. */
final class Paging {

	static final int DEFAULT_LIMIT = 200;
	static final int MAX_LIMIT = 1000;

	private Paging() {
	}

	/** Adds {@code limit} and {@code cursor} to an object schema. */
	static JsonObject addTo(JsonObject schema) {
		Schemas.property(schema, "limit",
			Schemas.integer("Items per page (default 200).", 1, MAX_LIMIT), false);
		Schemas.property(schema, "cursor",
			Schemas.string("nextCursor from the previous page."), false);
		return schema;
	}

	static int limit(JsonObject params) {
		return Params.integer(params, "limit", DEFAULT_LIMIT);
	}

	static JsonObject page(JsonArray items, String nextCursor) {
		JsonObject out = new JsonObject();
		out.add("items", items);
		if (nextCursor != null) {
			out.addProperty("nextCursor", nextCursor);
		}
		return out;
	}
}

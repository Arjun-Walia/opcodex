/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/** Small builders for the JSON Schema objects used by operation declarations. */
public final class Schemas {

	private Schemas() {
	}

	public static JsonObject object() {
		JsonObject schema = new JsonObject();
		schema.addProperty("type", "object");
		schema.add("properties", new JsonObject());
		schema.add("required", new JsonArray());
		schema.addProperty("additionalProperties", false);
		return schema;
	}

	public static JsonObject string(String description) {
		return typed("string", description);
	}

	public static JsonObject integer(String description, long min, long max) {
		JsonObject schema = typed("integer", description);
		schema.addProperty("minimum", min);
		schema.addProperty("maximum", max);
		return schema;
	}

	public static JsonObject bool(String description) {
		return typed("boolean", description);
	}

	public static JsonObject enumOf(String description, String... values) {
		JsonObject schema = typed("string", description);
		JsonArray allowed = new JsonArray();
		for (String value : values) {
			allowed.add(value);
		}
		schema.add("enum", allowed);
		return schema;
	}

	public static JsonObject any(String description) {
		JsonObject schema = new JsonObject();
		schema.addProperty("description", description);
		return schema;
	}

	/** Adds a property to an object schema and returns the same schema for chaining. */
	public static JsonObject property(JsonObject objectSchema, String name, JsonObject schema,
			boolean required) {
		objectSchema.getAsJsonObject("properties").add(name, schema);
		if (required) {
			objectSchema.getAsJsonArray("required").add(name);
		}
		return objectSchema;
	}

	private static JsonObject typed(String type, String description) {
		JsonObject schema = new JsonObject();
		schema.addProperty("type", type);
		schema.addProperty("description", description);
		return schema;
	}
}

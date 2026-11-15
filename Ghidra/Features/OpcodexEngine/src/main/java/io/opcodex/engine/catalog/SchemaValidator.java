/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import java.util.Map;

import com.google.gson.*;

import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/**
 * Checks request parameters against the subset of JSON Schema that {@link Schemas} produces:
 * required and unknown properties, primitive types, integer bounds and enums.
 */
public final class SchemaValidator {

	private SchemaValidator() {
	}

	public static void validate(JsonObject schema, JsonObject params) throws RpcException {
		JsonObject properties = schema.has("properties") ? schema.getAsJsonObject("properties")
				: new JsonObject();
		boolean closed = schema.has("additionalProperties") &&
			!schema.get("additionalProperties").getAsBoolean();
		if (closed) {
			for (String key : params.keySet()) {
				if (!properties.has(key)) {
					throw invalid("Unknown parameter: " + key);
				}
			}
		}
		if (schema.has("required")) {
			for (JsonElement required : schema.getAsJsonArray("required")) {
				String key = required.getAsString();
				if (!params.has(key) || params.get(key).isJsonNull()) {
					throw invalid("Missing required parameter: " + key);
				}
			}
		}
		for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
			JsonElement propertySchema = properties.get(entry.getKey());
			if (propertySchema != null && propertySchema.isJsonObject() &&
				!entry.getValue().isJsonNull()) {
				checkValue(entry.getKey(), propertySchema.getAsJsonObject(), entry.getValue());
			}
		}
	}

	private static void checkValue(String name, JsonObject schema, JsonElement value)
			throws RpcException {
		String type = schema.has("type") ? schema.get("type").getAsString() : null;
		if (type == null) {
			return;
		}
		switch (type) {
			case "string" -> {
				if (!isPrimitive(value) || !value.getAsJsonPrimitive().isString()) {
					throw invalid(name + " must be a string.");
				}
				if (schema.has("enum") && !schema.getAsJsonArray("enum").contains(value)) {
					throw invalid(name + " must be one of " + schema.getAsJsonArray("enum") + ".");
				}
			}
			case "integer" -> {
				if (!isPrimitive(value) || !value.getAsJsonPrimitive().isNumber()) {
					throw invalid(name + " must be an integer.");
				}
				double number = value.getAsDouble();
				if (number != Math.rint(number) || Double.isInfinite(number)) {
					throw invalid(name + " must be an integer.");
				}
				if (schema.has("minimum") && number < schema.get("minimum").getAsDouble() ||
					schema.has("maximum") && number > schema.get("maximum").getAsDouble()) {
					throw invalid(name + " must be between " + schema.get("minimum") + " and " +
						schema.get("maximum") + ".");
				}
			}
			case "boolean" -> {
				if (!isPrimitive(value) || !value.getAsJsonPrimitive().isBoolean()) {
					throw invalid(name + " must be true or false.");
				}
			}
			case "object" -> {
				if (!value.isJsonObject()) {
					throw invalid(name + " must be an object.");
				}
			}
			case "array" -> {
				if (!value.isJsonArray()) {
					throw invalid(name + " must be an array.");
				}
			}
			default -> {
				// Unknown types are not produced by Schemas; accept.
			}
		}
	}

	private static boolean isPrimitive(JsonElement value) {
		return value.isJsonPrimitive();
	}

	private static RpcException invalid(String message) {
		return new RpcException(ErrorCode.INVALID_PARAMS, message);
	}
}

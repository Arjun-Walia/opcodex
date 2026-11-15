/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import java.util.Objects;

import com.google.gson.JsonObject;

/** One operation in the catalog: the single declaration shared by the UI, CLI and MCP. */
public record Operation(String name, String description, OperationClass cls,
		JsonObject paramsSchema, JsonObject resultSchema, OperationHandler handler) {

	public Operation {
		Objects.requireNonNull(name, "name");
		Objects.requireNonNull(description, "description");
		Objects.requireNonNull(cls, "cls");
		Objects.requireNonNull(paramsSchema, "paramsSchema");
		Objects.requireNonNull(resultSchema, "resultSchema");
		Objects.requireNonNull(handler, "handler");
	}
}

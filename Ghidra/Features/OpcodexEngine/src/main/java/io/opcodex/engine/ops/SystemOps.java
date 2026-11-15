/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.framework.Application;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.spi.EngineServices;

/** Engine identity and the self-describing catalog. */
final class SystemOps {

	static final String API_VERSION = "1.0";

	private SystemOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject info = Schemas.object();
		Schemas.property(info, "product", Schemas.string("Product name."), true);
		Schemas.property(info, "edition", Schemas.string("community, pro or enterprise."), true);
		Schemas.property(info, "engineVersion", Schemas.string("Analysis engine version."), true);
		Schemas.property(info, "apiVersion", Schemas.string("Engine API version."), true);

		return List.of(
			new Operation("engine.info",
				"Shows the product, edition, analysis engine version and API version.",
				OperationClass.READ, Schemas.object(), info, (ctx, params) -> {
					JsonObject out = new JsonObject();
					out.addProperty("product", "Opcodex");
					out.addProperty("edition", services.entitlements().features().isEmpty()
							? "community"
							: "pro");
					out.addProperty("engineVersion", Application.getApplicationVersion());
					out.addProperty("apiVersion", API_VERSION);
					JsonArray features = new JsonArray();
					services.entitlements().features().stream().sorted().forEach(features::add);
					out.add("features", features);
					return out;
				}),
			new Operation("catalog.list",
				"Lists every operation with its description, class and parameter schema.",
				OperationClass.READ, Schemas.object(), Schemas.any("Operation descriptions."),
				(ctx, params) -> services.catalog().describe()));
	}
}

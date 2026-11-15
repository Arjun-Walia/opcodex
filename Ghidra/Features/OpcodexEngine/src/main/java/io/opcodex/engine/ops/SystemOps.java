/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.framework.Application;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.policy.ApprovalPolicy;
import io.opcodex.engine.spi.EngineServices;

/** Engine identity and the self-describing catalog. */
final class SystemOps {

	static final String API_VERSION = "1.0";

	private SystemOps() {
	}

	static JsonObject policySchema() {
		JsonObject schema = Schemas.object();
		Schemas.property(schema, "mode",
			Schemas.enumOf("Approval mode.", "ask", "allow-writes", "allow-all"), true);
		return schema;
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
			new Operation("policy.get",
				"Shows what agents may change in the open project without approval: ask, " +
					"allow-writes or allow-all.",
				OperationClass.READ, Schemas.object(), Schemas.any("Policy."), (ctx, params) -> {
					JsonObject out = new JsonObject();
					out.addProperty("mode", services.policy().wireName());
					return out;
				}),
			new Operation("policy.set",
				"Changes what agents may change in the open project without approval. Desktop " +
					"only.",
				OperationClass.SYSTEM, policySchema(), Schemas.any("Policy."), (ctx, params) -> {
					ApprovalPolicy mode = ApprovalPolicy.fromWire(params.get("mode").getAsString())
							.orElseThrow();
					services.setPolicy(mode);
					JsonObject out = new JsonObject();
					out.addProperty("mode", mode.wireName());
					return out;
				}),
			new Operation("catalog.list",
				"Lists every operation with its description, class and parameter schema.",
				OperationClass.READ, Schemas.object(), Schemas.any("Operation descriptions."),
				(ctx, params) -> services.catalog().describe()));
	}
}

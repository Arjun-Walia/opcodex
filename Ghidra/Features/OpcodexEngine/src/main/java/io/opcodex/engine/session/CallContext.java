/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import java.util.function.Consumer;

import com.google.gson.JsonObject;

import io.opcodex.engine.spi.EngineServices;

/** Who is calling and with which engine services. */
public record CallContext(Credential.Kind kind, String actor, EngineServices services,
		Consumer<JsonObject> events) {
	public CallContext(Credential.Kind kind, String actor, EngineServices services) {
		this(kind, actor, services, event -> {});
	}
}

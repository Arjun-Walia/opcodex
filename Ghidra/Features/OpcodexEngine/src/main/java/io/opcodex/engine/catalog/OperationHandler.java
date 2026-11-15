/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.session.CallContext;

/** Executes one catalog operation. */
@FunctionalInterface
public interface OperationHandler {
	JsonElement handle(CallContext ctx, JsonObject params) throws RpcException;
}

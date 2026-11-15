/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import com.google.gson.JsonObject;

/** A domain error returned to the client with a stable code and a safe message. */
public class RpcException extends Exception {

	private final ErrorCode code;
	private final transient JsonObject detail;

	public RpcException(ErrorCode code, String message) {
		this(code, message, null);
	}

	public RpcException(ErrorCode code, String message, JsonObject detail) {
		super(message);
		this.code = code;
		this.detail = detail;
	}

	public ErrorCode code() {
		return code;
	}

	/** Extra structured data for the client, such as an approval preview; may be null. */
	public JsonObject detail() {
		return detail;
	}
}

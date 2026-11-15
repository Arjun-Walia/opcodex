/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/** Typed access to parameters that the schema validator has already checked. */
final class Params {

	private Params() {
	}

	static String string(JsonObject params, String key) {
		JsonElement value = params.get(key);
		return value == null || value.isJsonNull() ? null : value.getAsString();
	}

	static String requireString(JsonObject params, String key) throws RpcException {
		String value = string(params, key);
		if (value == null || value.isBlank()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, key + " is required.");
		}
		return value;
	}

	static int integer(JsonObject params, String key, int defaultValue) {
		JsonElement value = params.get(key);
		return value == null || value.isJsonNull() ? defaultValue : value.getAsInt();
	}

	static long requireLong(JsonObject params, String key) throws RpcException {
		JsonElement value = params.get(key);
		if (value == null || value.isJsonNull()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, key + " is required.");
		}
		return value.getAsLong();
	}

	/**
	 * Refuses network and device paths from CLI and MCP clients. Touching a UNC path on Windows
	 * sends the user's credentials to that server, and analysed binaries can prompt agents to.
	 */
	static void requireLocalPath(io.opcodex.engine.session.CallContext ctx, String key,
			String path) throws RpcException {
		if (ctx.kind() == io.opcodex.engine.session.Credential.Kind.DESKTOP) {
			return;
		}
		String trimmed = path.strip();
		if (trimmed.startsWith("\\\\") || trimmed.startsWith("//") || trimmed.startsWith("\\/") ||
			trimmed.startsWith("/\\")) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				key + " must be a local path. Network and device paths are refused for agents " +
					"and the CLI; open them from the Opcodex desktop.");
		}
	}

	static Address address(Program program, String key, String text) throws RpcException {
		Address address = program.getAddressFactory().getAddress(text);
		if (address == null) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				key + " is not an address in this program: " + text);
		}
		return address;
	}
}

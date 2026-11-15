/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

import com.google.gson.*;

import ghidra.util.Msg;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.session.CallContext;
import io.opcodex.engine.session.Credential;
import io.opcodex.engine.spi.EngineServices;

/**
 * Turns one JSON-RPC request line into one response line: authenticate, check the operation
 * class against the credential, run the handler and map failures to stable error codes.
 */
public final class Dispatcher {

	public static final int PARSE_ERROR = -32700;
	public static final int INVALID_REQUEST = -32600;
	public static final int METHOD_NOT_FOUND = -32601;
	public static final int INVALID_PARAMS = -32602;
	public static final int DOMAIN_ERROR = -32000;

	private static final Pattern CLIENT_ACTOR = Pattern.compile("^(cli|mcp:[a-z0-9._-]{1,64})$");

	/** Optional check run after authentication and before the handler (approval policy). */
	@FunctionalInterface
	public interface Gate {
		void check(CallContext ctx, Operation op, JsonObject params) throws RpcException;
	}

	private final Catalog catalog;
	private final List<Credential> credentials;
	private final EngineServices services;
	private volatile Gate gate = (ctx, op, params) -> {
	};

	public Dispatcher(Catalog catalog, Map<String, Credential> tokens, EngineServices services) {
		this.catalog = Objects.requireNonNull(catalog, "catalog");
		this.credentials = List.copyOf(tokens.values());
		this.services = services;
	}

	public void setGate(Gate gate) {
		this.gate = Objects.requireNonNull(gate, "gate");
	}

	/** Handles one request line; returns the response line, or null for a notification. */
	public String handleLine(String line) {
		JsonObject request;
		try {
			JsonElement parsed = JsonParser.parseString(line);
			if (!parsed.isJsonObject()) {
				return error(JsonNull.INSTANCE, INVALID_REQUEST, "Request must be a JSON object.",
					null, null).toString();
			}
			request = parsed.getAsJsonObject();
		}
		catch (JsonParseException e) {
			return error(JsonNull.INSTANCE, PARSE_ERROR, "Invalid JSON.", null, null).toString();
		}
		JsonElement id = request.has("id") ? request.get("id") : null;
		JsonObject response = dispatch(request, id == null ? JsonNull.INSTANCE : id);
		return id == null ? null : response.toString();
	}

	private JsonObject dispatch(JsonObject request, JsonElement id) {
		if (!"2.0".equals(stringOrNull(request, "jsonrpc"))) {
			return error(id, INVALID_REQUEST, "jsonrpc must be \"2.0\".", null, null);
		}
		String method = stringOrNull(request, "method");
		if (method == null) {
			return error(id, INVALID_REQUEST, "method is required.", null, null);
		}
		Credential credential = authenticate(stringOrNull(request, "auth"));
		if (credential == null) {
			return domainError(id, new RpcException(ErrorCode.UNAUTHORIZED, "Not authorized."));
		}
		Optional<Operation> found = catalog.find(method);
		if (found.isEmpty()) {
			return error(id, METHOD_NOT_FOUND, "Unknown operation: " + method, null, null);
		}
		Operation op = found.get();
		try {
			String actor = actorFor(credential, request);
			if (op.cls() == OperationClass.SYSTEM && credential.kind() != Credential.Kind.DESKTOP) {
				throw new RpcException(ErrorCode.FORBIDDEN,
					op.name() + " can only be used from the Opcodex desktop.");
			}
			JsonElement params = request.has("params") ? request.get("params") : new JsonObject();
			if (!params.isJsonObject()) {
				throw new RpcException(ErrorCode.INVALID_PARAMS, "params must be an object.");
			}
			CallContext ctx = new CallContext(credential.kind(), actor, services);
			JsonObject paramObject = params.getAsJsonObject();
			gate.check(ctx, op, paramObject);
			JsonElement result = op.handler().handle(ctx, paramObject);
			JsonObject response = base(id);
			response.add("result", result == null ? JsonNull.INSTANCE : result);
			return response;
		}
		catch (RpcException e) {
			return domainError(id, e);
		}
		catch (RuntimeException e) {
			Msg.error(this, "Operation failed: " + op.name(), e);
			return domainError(id,
				new RpcException(ErrorCode.ENGINE_ERROR, "The engine could not complete " +
					op.name() + ". See the engine log for details."));
		}
	}

	private Credential authenticate(String token) {
		if (token == null) {
			return null;
		}
		byte[] presented = token.getBytes(StandardCharsets.UTF_8);
		Credential match = null;
		for (Credential candidate : credentials) {
			if (MessageDigest.isEqual(presented,
				candidate.token().getBytes(StandardCharsets.UTF_8))) {
				match = candidate;
			}
		}
		return match;
	}

	private static String actorFor(Credential credential, JsonObject request)
			throws RpcException {
		if (credential.kind() == Credential.Kind.DESKTOP) {
			return "desktop";
		}
		String requested = stringOrNull(request, "actor");
		if (requested == null) {
			return "cli";
		}
		if (!CLIENT_ACTOR.matcher(requested).matches()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				"actor must be \"cli\" or \"mcp:<client>\".");
		}
		return requested;
	}

	private static String stringOrNull(JsonObject object, String key) {
		JsonElement value = object.get(key);
		return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()
				? value.getAsString()
				: null;
	}

	private static JsonObject base(JsonElement id) {
		JsonObject response = new JsonObject();
		response.addProperty("jsonrpc", "2.0");
		response.add("id", id);
		return response;
	}

	private static JsonObject domainError(JsonElement id, RpcException e) {
		int numeric = e.code() == ErrorCode.INVALID_PARAMS ? INVALID_PARAMS : DOMAIN_ERROR;
		return error(id, numeric, e.getMessage(), e.code(), e.detail());
	}

	private static JsonObject error(JsonElement id, int numeric, String message, ErrorCode code,
			JsonObject detail) {
		JsonObject error = new JsonObject();
		error.addProperty("code", numeric);
		error.addProperty("message", message);
		if (code != null) {
			JsonObject data = detail == null ? new JsonObject() : detail.deepCopy();
			data.addProperty("code", code.name());
			error.add("data", data);
		}
		JsonObject response = base(id);
		response.add("error", error);
		return response;
	}
}

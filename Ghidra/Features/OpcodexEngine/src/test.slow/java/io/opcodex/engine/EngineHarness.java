/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

import com.google.gson.*;

import io.opcodex.engine.session.Credential;

/** Drives a real engine core through its dispatcher, as the desktop or as a CLI client. */
public final class EngineHarness implements AutoCloseable {

	public static final String DESKTOP = "harness-desktop-token-000000000000";
	public static final String CLIENT = "harness-client-token-0000000000000";

	/** push rbp; mov rbp,rsp; mov eax,42; pop rbp; ret */
	public static final byte[] RETURN_42 = { 0x55, 0x48, (byte) 0x89, (byte) 0xE5, (byte) 0xB8,
		0x2A, 0x00, 0x00, 0x00, 0x5D, (byte) 0xC3 };

	private final EngineCore core;
	private final Path projectsDir;
	private int nextId = 1;

	public EngineHarness() throws Exception {
		projectsDir = Files.createTempDirectory("opcodex-engine-test");
		core = EngineCore.start(Map.of(DESKTOP, new Credential(Credential.Kind.DESKTOP, DESKTOP),
			CLIENT, new Credential(Credential.Kind.CLIENT, CLIENT)));
	}

	public EngineCore core() {
		return core;
	}

	public Path projectsDir() {
		return projectsDir;
	}

	/** Sends one request and returns the whole JSON-RPC response. */
	public JsonObject raw(String token, String actor, String method, JsonObject params) {
		JsonObject req = new JsonObject();
		req.addProperty("jsonrpc", "2.0");
		req.addProperty("id", nextId++);
		req.addProperty("method", method);
		req.add("params", params == null ? new JsonObject() : params);
		req.addProperty("auth", token);
		if (actor != null) {
			req.addProperty("actor", actor);
		}
		return JsonParser.parseString(core.dispatcher().handleLine(req.toString()))
				.getAsJsonObject();
	}

	/** Calls as the desktop and returns the result, failing the test on any error. */
	public JsonElement desktop(String method, JsonObject params) {
		return resultOf(raw(DESKTOP, null, method, params));
	}

	/** Calls as a CLI client and returns the result, failing the test on any error. */
	public JsonElement client(String method, JsonObject params) {
		return resultOf(raw(CLIENT, null, method, params));
	}

	/** Returns the domain error code of a response, or null if it succeeded. */
	public static String errorCode(JsonObject response) {
		if (!response.has("error")) {
			return null;
		}
		JsonObject error = response.getAsJsonObject("error");
		return error.has("data") ? error.getAsJsonObject("data").get("code").getAsString()
				: String.valueOf(error.get("code").getAsInt());
	}

	private static JsonElement resultOf(JsonObject response) {
		if (response.has("error")) {
			throw new AssertionError("Engine call failed: " + response);
		}
		return response.get("result");
	}

	public static JsonObject params(Object... keyValues) {
		JsonObject out = new JsonObject();
		for (int i = 0; i < keyValues.length; i += 2) {
			String key = (String) keyValues[i];
			Object value = keyValues[i + 1];
			if (value instanceof Number n) {
				out.addProperty(key, n);
			}
			else if (value instanceof Boolean b) {
				out.addProperty(key, b);
			}
			else if (value instanceof JsonElement e) {
				out.add(key, e);
			}
			else {
				out.addProperty(key, String.valueOf(value));
			}
		}
		return out;
	}

	/** Creates a project and imports {@link #RETURN_42} as raw x86-64; returns the program id. */
	public String createProjectWithReturn42() {
		desktop("project.create", params("dir", projectsDir.toString(), "name", "demo"));
		JsonObject imported = desktop("program.import",
			params("name", "ret42.bin", "bytesBase64", Base64.getEncoder().encodeToString(RETURN_42),
				"language", "x86:LE:64:default", "compiler", "gcc")).getAsJsonObject();
		return imported.get("programId").getAsString();
	}

	@Override
	public void close() {
		core.close();
	}
}

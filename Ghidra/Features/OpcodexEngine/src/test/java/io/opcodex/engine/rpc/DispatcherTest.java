/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.*;

import io.opcodex.engine.catalog.*;
import io.opcodex.engine.session.Credential;

public class DispatcherTest {

	private static final String DESKTOP_TOKEN = "desktop-token-0123456789abcdef";
	private static final String CLIENT_TOKEN = "client-token-0123456789abcdef";

	private Catalog catalog;
	private Dispatcher dispatcher;
	private final AtomicInteger calls = new AtomicInteger();
	private volatile String lastActor;

	@Before
	public void setUp() {
		catalog = new Catalog();
		OperationHandler counting = (ctx, params) -> {
			calls.incrementAndGet();
			lastActor = ctx.actor();
			JsonObject out = new JsonObject();
			out.addProperty("ok", true);
			return out;
		};
		catalog.register(new Operation("demo.read", "Reads.", OperationClass.READ,
			Schemas.object(), Schemas.object(), counting));
		catalog.register(new Operation("policy.set", "Changes policy.", OperationClass.SYSTEM,
			Schemas.object(), Schemas.object(), counting));
		dispatcher = new Dispatcher(catalog,
			Map.of(DESKTOP_TOKEN, new Credential(Credential.Kind.DESKTOP, DESKTOP_TOKEN),
				CLIENT_TOKEN, new Credential(Credential.Kind.CLIENT, CLIENT_TOKEN)),
			null);
	}

	private JsonObject call(String method, String token, String actor) {
		JsonObject req = new JsonObject();
		req.addProperty("jsonrpc", "2.0");
		req.addProperty("id", 7);
		req.addProperty("method", method);
		req.add("params", new JsonObject());
		if (token != null) {
			req.addProperty("auth", token);
		}
		if (actor != null) {
			req.addProperty("actor", actor);
		}
		return JsonParser.parseString(dispatcher.handleLine(req.toString())).getAsJsonObject();
	}

	private static String domainCode(JsonObject response) {
		return response.getAsJsonObject("error").getAsJsonObject("data").get("code").getAsString();
	}

	@Test
	public void wrongTokenIsRejectedBeforeDispatch() {
		JsonObject res = call("demo.read", "not-a-token", null);
		assertEquals("UNAUTHORIZED", domainCode(res));
		assertEquals(0, calls.get());
		JsonObject missing = call("demo.read", null, null);
		assertEquals("UNAUTHORIZED", domainCode(missing));
		assertEquals(0, calls.get());
	}

	@Test
	public void clientCredentialCannotCallSystemOperation() {
		JsonObject res = call("policy.set", CLIENT_TOKEN, null);
		assertEquals("FORBIDDEN", domainCode(res));
		assertEquals(0, calls.get());
	}

	@Test
	public void desktopCredentialCanCallSystemOperation() {
		JsonObject res = call("policy.set", DESKTOP_TOKEN, null);
		assertTrue(res.getAsJsonObject("result").get("ok").getAsBoolean());
		assertEquals(7, res.get("id").getAsInt());
		assertEquals("desktop", lastActor);
	}

	@Test
	public void unknownMethodReturnsMinus32601() {
		JsonObject res = call("no.such.op", CLIENT_TOKEN, null);
		assertEquals(-32601, res.getAsJsonObject("error").get("code").getAsInt());
	}

	@Test
	public void invalidActorIsRejected() {
		JsonObject res = call("demo.read", CLIENT_TOKEN, "desktop");
		assertEquals("INVALID_PARAMS", domainCode(res));
		assertEquals(0, calls.get());
	}

	@Test
	public void clientActorDefaultsToCliAndAcceptsMcpName() {
		call("demo.read", CLIENT_TOKEN, null);
		assertEquals("cli", lastActor);
		call("demo.read", CLIENT_TOKEN, "mcp:claude-code");
		assertEquals("mcp:claude-code", lastActor);
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

public class SchemaValidatorTest {

	private static JsonObject schema() {
		JsonObject s = Schemas.object();
		Schemas.property(s, "programId", Schemas.string("Program."), true);
		Schemas.property(s, "limit", Schemas.integer("Page size.", 1, 1000), false);
		Schemas.property(s, "kind", Schemas.enumOf("Kind.", "eol", "pre"), false);
		Schemas.property(s, "force", Schemas.bool("Force."), false);
		return s;
	}

	private static String reject(String json) {
		try {
			SchemaValidator.validate(schema(), JsonParser.parseString(json).getAsJsonObject());
		}
		catch (RpcException e) {
			assertEquals(ErrorCode.INVALID_PARAMS, e.code());
			return e.getMessage();
		}
		fail("Expected INVALID_PARAMS for " + json);
		return null;
	}

	@Test
	public void acceptsValidParams() throws Exception {
		SchemaValidator.validate(schema(), JsonParser
				.parseString("{\"programId\":\"p\",\"limit\":10,\"kind\":\"eol\",\"force\":true}")
				.getAsJsonObject());
	}

	@Test
	public void rejectsUnknownMissingAndMistypedParams() {
		assertTrue(reject("{\"programId\":\"p\",\"extra\":1}").contains("extra"));
		assertTrue(reject("{}").contains("programId"));
		assertTrue(reject("{\"programId\":5}").contains("programId"));
		assertTrue(reject("{\"programId\":\"p\",\"limit\":1001}").contains("limit"));
		assertTrue(reject("{\"programId\":\"p\",\"limit\":1.5}").contains("limit"));
		assertTrue(reject("{\"programId\":\"p\",\"kind\":\"post\"}").contains("kind"));
		assertTrue(reject("{\"programId\":\"p\",\"force\":\"yes\"}").contains("force"));
	}
}

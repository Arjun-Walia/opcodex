/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gson.*;

public class CatalogTest {

	private static Operation op(String name, OperationClass cls) {
		JsonObject params = Schemas.object();
		Schemas.property(params, "programId", Schemas.string("Program id."), true);
		return new Operation(name, "Does " + name + ".", cls, params, Schemas.object(),
			(ctx, p) -> JsonNull.INSTANCE);
	}

	@Test
	public void catalogDescribeListsNameClassAndSchemas() {
		Catalog catalog = new Catalog();
		catalog.register(op("program.list", OperationClass.READ));
		catalog.register(op("symbol.rename", OperationClass.WRITE));

		JsonArray described = catalog.describe();
		assertEquals(2, described.size());
		JsonObject first = described.get(0).getAsJsonObject();
		assertEquals("program.list", first.get("name").getAsString());
		assertEquals("read", first.get("class").getAsString());
		assertEquals("Does program.list.", first.get("description").getAsString());
		JsonObject params = first.getAsJsonObject("params");
		assertEquals("object", params.get("type").getAsString());
		assertTrue(params.getAsJsonObject("properties").has("programId"));
		assertEquals("programId", params.getAsJsonArray("required").get(0).getAsString());
		assertTrue(first.has("result"));
		assertEquals("write", described.get(1).getAsJsonObject().get("class").getAsString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void duplicateOperationNameRejected() {
		Catalog catalog = new Catalog();
		catalog.register(op("program.list", OperationClass.READ));
		catalog.register(op("program.list", OperationClass.READ));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidOperationNameRejected() {
		new Catalog().register(op("Program List", OperationClass.READ));
	}

	@Test
	public void findReturnsRegisteredOperation() {
		Catalog catalog = new Catalog();
		catalog.register(op("code.decompile", OperationClass.READ));
		assertTrue(catalog.find("code.decompile").isPresent());
		assertFalse(catalog.find("code.missing").isPresent());
	}
}

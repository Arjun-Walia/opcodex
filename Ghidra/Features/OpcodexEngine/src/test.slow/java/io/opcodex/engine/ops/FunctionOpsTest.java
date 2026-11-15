/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.program.model.data.VoidDataType;
import ghidra.program.model.listing.Program;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class FunctionOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private static final int FUNCTIONS = 1500;

	private EngineHarness engine;
	private ProgramBuilder builder;
	private Program program;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "functions"));
		builder = new ProgramBuilder("many", ProgramBuilder._X64, this);
		builder.createMemory(".text", "0x1000", 0x1000);
		for (int i = 0; i < FUNCTIONS; i++) {
			builder.createEmptyFunction("f" + i, "0x" + Integer.toHexString(0x1000 + i * 2), 1,
				VoidDataType.dataType);
		}
		builder.createMemoryCallReference("0x1000", "0x1002");
		builder.createString("0x1f00", "hello opcodex", java.nio.charset.StandardCharsets.US_ASCII,
			true, ghidra.program.model.data.StringDataType.dataType);
		program = builder.getProgram();
		programId = engine.adopt(program);
	}

	@After
	public void tearDown() {
		engine.close();
		builder.dispose();
	}

	private String addr(String text) {
		return program.getAddressFactory().getAddress(text).toString();
	}

	@Test
	public void listFunctionsPagesWithCursor() {
		JsonObject first = engine.client("program.functions.list",
			params("programId", programId, "limit", 1000)).getAsJsonObject();
		assertEquals(1000, first.getAsJsonArray("items").size());
		assertTrue(first.has("nextCursor"));

		JsonObject second = engine.client("program.functions.list",
			params("programId", programId, "limit", 1000, "cursor",
				first.get("nextCursor").getAsString())).getAsJsonObject();
		assertEquals(FUNCTIONS - 1000, second.getAsJsonArray("items").size());
		assertFalse(second.has("nextCursor"));

		Set<String> seen = new HashSet<>();
		for (JsonObject page : new JsonObject[] { first, second }) {
			for (JsonElement item : page.getAsJsonArray("items")) {
				assertTrue("duplicate across pages",
					seen.add(item.getAsJsonObject().get("entry").getAsString()));
			}
		}
		assertEquals(FUNCTIONS, seen.size());
	}

	@Test
	public void limitAbove1000IsInvalidParams() {
		JsonObject response = engine.raw(EngineHarness.CLIENT, null, "program.functions.list",
			params("programId", programId, "limit", 1001));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(response));
	}

	@Test
	public void queryFiltersByName() {
		JsonObject page = engine.client("program.functions.list",
			params("programId", programId, "query", "f149")).getAsJsonObject();
		// f149, f1490..f1499
		assertEquals(11, page.getAsJsonArray("items").size());
	}

	@Test
	public void xrefsToCalledFunction() {
		JsonObject page = engine.client("xrefs.to", params("programId", programId, "address",
			"0x1002")).getAsJsonObject();
		JsonArray items = page.getAsJsonArray("items");
		assertEquals(1, items.size());
		JsonObject ref = items.get(0).getAsJsonObject();
		assertEquals(addr("0x1000"), ref.get("from").getAsString());
		assertEquals(addr("0x1002"), ref.get("to").getAsString());
		assertEquals("UNCONDITIONAL_CALL", ref.get("type").getAsString());

		JsonArray from = engine.client("xrefs.from",
			params("programId", programId, "address", "0x1000")).getAsJsonObject()
				.getAsJsonArray("items");
		assertEquals(1, from.size());
	}

	@Test
	public void functionGetReportsCallersAndCallees() {
		JsonObject callee = engine.client("function.get",
			params("programId", programId, "address", "0x1002")).getAsJsonObject();
		assertEquals("f1", callee.get("name").getAsString());
		assertEquals(addr("0x1000"),
			callee.getAsJsonArray("callers").get(0).getAsJsonObject().get("entry").getAsString());
	}

	@Test
	public void unknownAddressIsInvalidParams() {
		JsonObject response = engine.raw(EngineHarness.CLIENT, null, "function.get",
			params("programId", programId, "address", "not-an-address"));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(response));
		JsonObject none = engine.raw(EngineHarness.CLIENT, null, "function.get",
			params("programId", programId, "address", "0x1f00"));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(none));
	}

	@Test
	public void stringsListFindsDefinedString() {
		JsonArray items = engine.client("program.strings.list",
			params("programId", programId)).getAsJsonObject().getAsJsonArray("items");
		assertEquals(1, items.size());
		assertEquals("hello opcodex", items.get(0).getAsJsonObject().get("value").getAsString());
		JsonArray none = engine.client("program.strings.list",
			params("programId", programId, "minLength", 20)).getAsJsonObject()
				.getAsJsonArray("items");
		assertEquals(0, none.size());
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.program.model.data.VoidDataType;
import ghidra.program.model.listing.Program;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class GraphOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	/**
	 * main(edi): if (edi == 0) eax = 2; else eax = 1; return eax.
	 * caller: call main; ret.
	 */
	static ProgramBuilder branchyProgram(Object test) throws Exception {
		ProgramBuilder builder = new ProgramBuilder("branchy", ProgramBuilder._X64, test);
		builder.createMemory(".text", "0x1000", 0x100);
		builder.setBytes("0x1000",
			"55 48 89 e5 83 ff 00 74 07 b8 01 00 00 00 eb 05 b8 02 00 00 00 5d c3");
		builder.setBytes("0x1020", "e8 db ff ff ff c3");
		builder.createEmptyFunction("main", "0x1000", 0x17, VoidDataType.dataType);
		builder.createEmptyFunction("caller", "0x1020", 6, VoidDataType.dataType);
		builder.disassemble("0x1000", 0x17);
		builder.disassemble("0x1020", 6);
		// probe: xor eax,eax; cpuid; ret (cpuid has no emulator implementation)
		builder.setBytes("0x1040", "31 c0 0f a2 c3");
		builder.createEmptyFunction("probe", "0x1040", 5, VoidDataType.dataType);
		builder.disassemble("0x1040", 5);
		return builder;
	}

	private EngineHarness engine;
	private ProgramBuilder builder;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		engine.desktop("project.create", params("dir", engine.projectsDir().toString(), "name", "graphs"));
		builder = branchyProgram(this);
		Program program = builder.getProgram();
		programId = engine.adopt(program);
	}

	@After
	public void tearDown() {
		engine.close();
		builder.dispose();
	}

	@Test
	public void blocksAndBranchesOfAFunction() {
		JsonObject graph = engine.client("function.graph", params("programId", programId, "address", "0x1009"))
				.getAsJsonObject();
		assertEquals("00001000", graph.get("entry").getAsString());
		JsonArray blocks = graph.getAsJsonArray("blocks");
		assertEquals(4, blocks.size());
		JsonObject first = blocks.get(0).getAsJsonObject();
		assertEquals("00001000", first.get("start").getAsString());
		assertEquals(4, first.getAsJsonArray("instructions").size());

		Map<String, String> edges = new HashMap<>();
		for (JsonElement e : graph.getAsJsonArray("edges")) {
			JsonObject edge = e.getAsJsonObject();
			edges.put(edge.get("from").getAsString() + ">" + edge.get("to").getAsString(),
				edge.get("kind").getAsString());
		}
		assertEquals("true", edges.get("00001000>00001010"));
		assertEquals("false", edges.get("00001000>00001009"));
		assertEquals("jump", edges.get("00001009>00001015"));
		assertEquals("fall", edges.get("00001010>00001015"));
		assertEquals(4, edges.size());
		assertFalse(graph.get("truncated").getAsBoolean());
	}

	@Test
	public void callersAndCallees() {
		JsonObject calls = engine.client("function.callgraph", params("programId", programId, "address", "0x1000"))
				.getAsJsonObject();
		assertEquals("00001000", calls.get("root").getAsString());
		JsonArray edges = calls.getAsJsonArray("edges");
		assertEquals(1, edges.size());
		assertEquals("00001020", edges.get(0).getAsJsonObject().get("from").getAsString());
		assertEquals("00001000", edges.get(0).getAsJsonObject().get("to").getAsString());
		assertEquals(2, calls.getAsJsonArray("nodes").size());
	}

	@Test
	public void addressesOutsideFunctionsAreNotFound() {
		JsonObject response = engine.raw(EngineHarness.CLIENT, null, "function.graph",
			params("programId", programId, "address", "0x1080"));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(response));
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.program.model.data.StringDataType;
import ghidra.program.model.data.VoidDataType;
import ghidra.program.model.listing.Program;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class SummaryOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private ProgramBuilder builder;
	private Program program;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "summary"));
		builder = new ProgramBuilder("triage", ProgramBuilder._X64, this);
		builder.createMemory(".text", "0x1000", 0x200);
		builder.setBytes("0x1000", "55 48 89 e5 b8 2a 00 00 00 5d c3");
		builder.createEmptyFunction("main", "0x1000", 11, VoidDataType.dataType);
		builder.createEntryPoint("0x1000", "entry");
		builder.createString("0x1100", "https://opcodex.example/beacon",
			StandardCharsets.US_ASCII, true, StringDataType.dataType);
		builder.createExternalLibraries("KERNEL32.DLL");
		builder.createExternalFunction("0x0", "KERNEL32.DLL", "VirtualAllocEx");
		builder.createExternalFunction("0x0", "KERNEL32.DLL", "GetTickCount");
		program = builder.getProgram();
		programId = engine.adopt(program);
	}

	@After
	public void tearDown() {
		engine.close();
		builder.dispose();
	}

	@Test
	public void summaryDescribesTheProgramAtAGlance() {
		JsonObject s = engine.client("program.summary", params("programId", programId))
				.getAsJsonObject();
		assertEquals("x86:LE:64:default", s.get("language").getAsString());
		assertEquals(1, s.get("functionCount").getAsInt());
		assertTrue(s.has("format"));
		assertEquals(64, s.get("addressSize").getAsInt());

		JsonArray blocks = s.getAsJsonArray("blocks");
		JsonObject text = blocks.get(0).getAsJsonObject();
		assertEquals(".text", text.get("name").getAsString());
		assertEquals(0x200, text.get("size").getAsLong());
		double entropy = text.get("entropy").getAsDouble();
		assertTrue("entropy in bits per byte: " + entropy, entropy >= 0 && entropy <= 8);

		JsonArray entries = s.getAsJsonArray("entryPoints");
		assertEquals(1, entries.size());

		JsonArray imports = s.getAsJsonArray("imports");
		assertEquals("KERNEL32.DLL", imports.get(0).getAsJsonObject().get("library").getAsString());
		assertEquals(2, imports.get(0).getAsJsonObject().getAsJsonArray("functions").size());

		JsonArray risky = s.getAsJsonArray("riskyApis");
		assertEquals(1, risky.size());
		assertEquals("VirtualAllocEx", risky.get(0).getAsJsonObject().get("name").getAsString());
		assertEquals("process-injection",
			risky.get(0).getAsJsonObject().get("category").getAsString());

		JsonArray notable = s.getAsJsonArray("notableStrings");
		assertEquals("https://opcodex.example/beacon",
			notable.get(0).getAsJsonObject().get("value").getAsString());
		assertEquals("url", notable.get(0).getAsJsonObject().get("kind").getAsString());
	}

	@Test
	public void memoryReadReturnsBytesAndRefusesHugeRanges() {
		JsonObject read = engine.client("memory.read",
			params("programId", programId, "address", "0x1000", "length", 11)).getAsJsonObject();
		assertEquals("554889e5b82a0000005dc3", read.get("hex").getAsString());
		assertEquals(11, read.get("length").getAsInt());

		JsonObject tooBig = engine.raw(EngineHarness.CLIENT, null, "memory.read",
			params("programId", programId, "address", "0x1000", "length", 65537));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(tooBig));
	}

	@Test
	public void memoryReadStopsAtTheEndOfInitializedMemory() {
		JsonObject read = engine.client("memory.read",
			params("programId", programId, "address", "0x11f0", "length", 64)).getAsJsonObject();
		assertEquals(16, read.get("length").getAsInt());
	}
}

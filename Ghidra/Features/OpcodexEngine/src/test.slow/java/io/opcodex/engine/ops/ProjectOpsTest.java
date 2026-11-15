/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.*;

import com.google.gson.*;

import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class ProjectOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
	}

	@After
	public void tearDown() {
		engine.close();
	}

	@Test
	public void createImportAnalyzeAndListRawX86() {
		String programId = engine.createProjectWithReturn42();

		JsonObject analyzed =
			engine.desktop("program.analyze", params("programId", programId)).getAsJsonObject();
		assertEquals(programId, analyzed.get("programId").getAsString());

		JsonArray programs = engine.client("program.list", new JsonObject()).getAsJsonArray();
		assertEquals(1, programs.size());
		JsonObject program = programs.get(0).getAsJsonObject();
		assertEquals(programId, program.get("programId").getAsString());
		assertEquals("ret42.bin", program.get("name").getAsString());
		assertEquals("x86:LE:64:default", program.get("language").getAsString());
		assertTrue(program.get("revision").getAsLong() > 0);
	}

	@Test
	public void engineInfoReportsEngineVersion() {
		JsonObject info = engine.client("engine.info", new JsonObject()).getAsJsonObject();
		assertEquals("Opcodex", info.get("product").getAsString());
		assertEquals("12.1.4", info.get("engineVersion").getAsString());
		assertEquals("1.0", info.get("apiVersion").getAsString());
	}

	@Test
	public void openUnknownProgramIdReturnsNotFound() {
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "empty"));
		JsonObject response = engine.raw(EngineHarness.DESKTOP, null, "program.analyze",
			params("programId", "00000000-0000-0000-0000-000000000000"));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(response));
	}

	@Test
	public void createRefusesExistingProject() throws Exception {
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "keep"));
		engine.desktop("project.close", new JsonObject());
		assertTrue(Files.exists(engine.projectsDir().resolve("keep.gpr")));

		JsonObject again = engine.raw(EngineHarness.DESKTOP, null, "project.create",
			params("dir", engine.projectsDir().toString(), "name", "keep"));
		assertEquals("CONFLICT", EngineHarness.errorCode(again));
		assertTrue(Files.exists(engine.projectsDir().resolve("keep.gpr")));
	}

	@Test
	public void importRequiresExactlyOneSource() {
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "src"));
		JsonObject neither = engine.raw(EngineHarness.DESKTOP, null, "program.import",
			params("name", "x.bin", "language", "x86:LE:64:default"));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(neither));
	}

	@Test
	public void openingAProjectReloadsItsPrograms() {
		String programId = engine.createProjectWithReturn42();
		engine.desktop("project.close", new JsonObject());
		engine.client("project.open", params("dir", engine.projectsDir().toString(), "name", "demo"));
		JsonArray programs = engine.client("program.list", new JsonObject()).getAsJsonArray();
		assertEquals(1, programs.size());
		assertEquals(programId, programs.get(0).getAsJsonObject().get("programId").getAsString());
	}
}

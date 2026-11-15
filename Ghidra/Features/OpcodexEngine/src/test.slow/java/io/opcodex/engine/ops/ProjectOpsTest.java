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
	public void clientCannotSwitchAwayFromOpenProject() {
		engine.createProjectWithReturn42();
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "other"));
		engine.desktop("project.open", params("dir", engine.projectsDir().toString(), "name", "demo"));
		JsonObject response = engine.raw(EngineHarness.CLIENT, null, "project.open",
			params("dir", engine.projectsDir().toString(), "name", "other"));
		assertEquals("CONFLICT", EngineHarness.errorCode(response));
		JsonObject create = engine.raw(EngineHarness.CLIENT, null, "project.create",
			params("dir", engine.projectsDir().toString(), "name", "third"));
		assertEquals("CONFLICT", EngineHarness.errorCode(create));
	}

	@Test
	public void reopeningTheOpenProjectKeepsItsPrograms() {
		String programId = engine.createProjectWithReturn42();
		long revision = engine.revision(programId);
		engine.client("project.open", params("dir", engine.projectsDir().toString(), "name", "demo"));
		assertTrue(engine.core().services().workspace().openHandle(programId).isPresent());
		assertEquals(revision, engine.revision(programId));
	}

	@Test
	public void clientNetworkPathsAreRefused() {
		for (String dir : new String[] { "\\\\opcodex-test-host.invalid\\share",
			"//opcodex-test-host.invalid/share", "\\\\?\\UNC\\host\\share",
			"\\\\.\\pipe\\x" }) {
			JsonObject response = engine.raw(EngineHarness.CLIENT, null, "project.open",
				params("dir", dir, "name", "a"));
			assertEquals(dir, "INVALID_PARAMS", EngineHarness.errorCode(response));
		}
	}

	@Test
	public void importAndAnalyzeAreJournaled() {
		String programId = engine.createProjectWithReturn42();
		engine.desktop("program.analyze", params("programId", programId));
		JsonArray items = engine.desktop("history.list", params("programId", programId))
				.getAsJsonObject().getAsJsonArray("items");
		assertEquals(2, items.size());
		assertEquals("program.analyze", items.get(0).getAsJsonObject().get("op").getAsString());
		assertEquals("program.import", items.get(1).getAsJsonObject().get("op").getAsString());
		assertEquals("desktop", items.get(0).getAsJsonObject().get("actor").getAsString());
	}

	@Test
	public void closeWaitsForInFlightWrite() throws Exception {
		String programId = engine.createProjectWithReturn42();
		var workspace = engine.core().services().workspace();
		java.util.concurrent.CountDownLatch started = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.atomic.AtomicBoolean committed =
			new java.util.concurrent.atomic.AtomicBoolean();
		Thread writer = new Thread(() -> {
			try {
				workspace.write(programId, "Slow write", p -> {
					started.countDown();
					Thread.sleep(500);
					p.getListing().setComment(p.getMemory().getMinAddress(),
						ghidra.program.model.listing.CommentType.EOL, "kept");
					return null;
				});
				committed.set(true);
			}
			catch (Exception e) {
				committed.set(false);
			}
		});
		writer.start();
		started.await();
		engine.desktop("project.close", new JsonObject());
		writer.join(10_000);
		assertTrue("the in-flight write must finish before the project closes", committed.get());
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

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class HistoryOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private String programId;
	private String entry;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		programId = engine.createProjectWithReturn42();
		engine.core().services().workspace().write(programId, "Create function", p -> {
			var start = p.getMemory().getMinAddress();
			new ghidra.app.cmd.disassemble.DisassembleCommand(start, null, true).applyTo(p);
			new ghidra.app.cmd.function.CreateFunctionCmd(start).applyTo(p);
			return null;
		});
		entry = engine.client("program.functions.list", params("programId", programId))
				.getAsJsonObject()
				.getAsJsonArray("items")
				.get(0)
				.getAsJsonObject()
				.get("entry")
				.getAsString();
	}

	@After
	public void tearDown() {
		engine.close();
	}

	private String nameAtEntry() {
		return engine.client("function.get", params("programId", programId, "address", entry))
				.getAsJsonObject()
				.get("name")
				.getAsString();
	}

	private String rename(String newName) {
		return engine.desktop("symbol.rename", params("programId", programId, "address", entry,
			"newName", newName, "expectedRevision", engine.revision(programId)))
				.getAsJsonObject()
				.get("entryId")
				.getAsString();
	}

	private JsonObject revert(String entryId) {
		return engine.raw(EngineHarness.DESKTOP, null, "history.revert", params("programId",
			programId, "entryId", entryId, "expectedRevision", engine.revision(programId)));
	}

	@Test
	public void revertRestoresPreviousName() {
		String original = nameAtEntry();
		String id = rename("answer");
		JsonObject reverted = revert(id);
		assertNull(reverted.toString(), EngineHarness.errorCode(reverted));
		assertEquals(original, nameAtEntry());

		JsonObject newest = engine.desktop("history.list", params("programId", programId))
				.getAsJsonObject()
				.getAsJsonArray("items")
				.get(0)
				.getAsJsonObject();
		assertEquals(id, newest.get("revertOf").getAsString());
	}

	@Test
	public void revertConflictsWhenValueChangedSince() {
		String first = rename("answer");
		rename("answer_v2");
		JsonObject response = revert(first);
		assertEquals("CONFLICT", EngineHarness.errorCode(response));
		assertEquals("answer_v2", nameAtEntry());
	}

	@Test
	public void historyFiltersByActorAndPages() {
		rename("a1");
		rename("a2");
		rename("a3");
		JsonObject page = engine.desktop("history.list",
			params("programId", programId, "limit", 2)).getAsJsonObject();
		assertEquals(2, page.getAsJsonArray("items").size());
		JsonObject rest = engine.desktop("history.list", params("programId", programId,
			"limit", 2, "cursor", page.get("nextCursor").getAsString())).getAsJsonObject();
		assertEquals(1, rest.getAsJsonArray("items").size());
		JsonArray agentOnly = engine.desktop("history.list",
			params("programId", programId, "actor", "mcp:someone")).getAsJsonObject()
				.getAsJsonArray("items");
		assertEquals(0, agentOnly.size());
	}

	@Test
	public void revertSurvivesRestart() throws Exception {
		String original = nameAtEntry();
		String id = rename("answer");
		engine.close();

		engine = new EngineHarness(engine.projectsDir());
		engine.desktop("project.open",
			params("dir", engine.projectsDir().toString(), "name", "demo"));
		assertEquals("answer", nameAtEntry());
		JsonObject reverted = revert(id);
		assertNull(reverted.toString(), EngineHarness.errorCode(reverted));
		assertEquals(original, nameAtEntry());
	}

	@Test
	public void revertOfUnknownEntryIsNotFound() {
		JsonObject response = revert("missing-entry");
		assertEquals("NOT_FOUND", EngineHarness.errorCode(response));
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.program.model.data.VoidDataType;
import ghidra.program.model.listing.CommentType;
import ghidra.program.model.listing.Program;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class EditOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private ProgramBuilder builder;
	private Program program;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		engine.desktop("project.create",
			params("dir", engine.projectsDir().toString(), "name", "edits"));
		builder = new ProgramBuilder("edits", ProgramBuilder._X64, this);
		builder.createMemory(".text", "0x1000", 0x100);
		builder.createEmptyFunction("FUN_00001000", "0x1000", 4, VoidDataType.dataType);
		program = builder.getProgram();
		programId = engine.adopt(program);
	}

	@After
	public void tearDown() {
		engine.close();
		builder.dispose();
	}

	private JsonObject rename(String token, String newName, long expected) {
		return engine.raw(token, null, "symbol.rename", params("programId", programId,
			"address", "0x1000", "newName", newName, "expectedRevision", expected));
	}

	private int journalSize() {
		return engine.desktop("history.list", params("programId", programId)).getAsJsonObject()
				.getAsJsonArray("items")
				.size();
	}

	@Test
	public void renameRecordsBeforeAndAfterInJournal() {
		long revision = engine.revision(programId);
		JsonObject response = rename(EngineHarness.DESKTOP, "parse_header", revision);
		JsonObject result = response.getAsJsonObject("result");
		assertTrue(result.get("revision").getAsLong() > revision);
		assertEquals("parse_header",
			program.getFunctionManager().getFunctionAt(builder.addr("0x1000")).getName());

		JsonArray items = engine.desktop("history.list", params("programId", programId))
				.getAsJsonObject()
				.getAsJsonArray("items");
		assertEquals(1, items.size());
		JsonObject entry = items.get(0).getAsJsonObject();
		assertEquals(result.get("entryId").getAsString(), entry.get("id").getAsString());
		assertEquals("desktop", entry.get("actor").getAsString());
		assertEquals("symbol.rename", entry.get("op").getAsString());
		// FUN_00001000 is a default name, recorded as null so a revert restores the default.
		assertTrue(entry.get("before").isJsonNull());
		assertEquals("parse_header", entry.get("after").getAsString());
		assertEquals("rename FUN_00001000 → parse_header", entry.get("summary").getAsString());
	}

	@Test
	public void renameWithStaleRevisionChangesNothing() {
		long revision = engine.revision(programId);
		JsonObject response = rename(EngineHarness.DESKTOP, "parse_header", revision - 1);
		assertEquals("STALE_REVISION", EngineHarness.errorCode(response));
		assertEquals("FUN_00001000",
			program.getFunctionManager().getFunctionAt(builder.addr("0x1000")).getName());
		assertEquals(0, journalSize());
	}

	@Test
	public void invalidNameIsInvalidParamsAndNotJournaled() {
		JsonObject response =
			rename(EngineHarness.DESKTOP, "has space", engine.revision(programId));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(response));
		assertEquals(0, journalSize());
	}

	@Test
	public void commentSetAndClear() {
		engine.desktop("comment.set", params("programId", programId, "address", "0x1000",
			"kind", "eol", "text", "checks the magic", "expectedRevision",
			engine.revision(programId)));
		assertEquals("checks the magic",
			program.getListing().getComment(CommentType.EOL, builder.addr("0x1000")));
		engine.desktop("comment.set", params("programId", programId, "address", "0x1000",
			"kind", "eol", "text", "", "expectedRevision", engine.revision(programId)));
		assertNull(program.getListing().getComment(CommentType.EOL, builder.addr("0x1000")));
		assertEquals(2, journalSize());
	}

	@Test
	public void commentOver64KiBIsInvalidParams() {
		JsonObject response = engine.raw(EngineHarness.DESKTOP, null, "comment.set",
			params("programId", programId, "address", "0x1000", "kind", "eol", "text",
				"x".repeat(64 * 1024 + 1), "expectedRevision", engine.revision(programId)));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(response));
	}
}

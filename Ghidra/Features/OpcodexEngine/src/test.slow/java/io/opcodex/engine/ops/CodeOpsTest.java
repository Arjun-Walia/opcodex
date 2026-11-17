/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class CodeOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private String programId;
	private String entry;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		programId = engine.createProjectWithReturn42();
		engine.desktop("program.analyze", params("programId", programId));
		JsonArray functions = engine.client("program.functions.list",
			params("programId", programId)).getAsJsonObject().getAsJsonArray("items");
		if (functions.isEmpty()) {
			// Raw binaries have no entry point; define the function at the start of the image.
			engine.core().services().workspace().write(programId, "Create function", p -> {
				var start = p.getMemory().getMinAddress();
				new ghidra.app.cmd.disassemble.DisassembleCommand(start, null, true).applyTo(p);
				new ghidra.app.cmd.function.CreateFunctionCmd(start).applyTo(p);
				return null;
			});
			functions = engine.client("program.functions.list",
				params("programId", programId)).getAsJsonObject().getAsJsonArray("items");
		}
		assertEquals(1, functions.size());
		entry = functions.get(0).getAsJsonObject().get("entry").getAsString();
	}

	@After
	public void tearDown() {
		engine.close();
	}

	@Test
	public void decompileRawFunctionReturnsReturn42() {
		JsonObject result = engine.client("code.decompile",
			params("programId", programId, "address", entry)).getAsJsonObject();
		assertTrue(result.get("c").getAsString(), result.get("c").getAsString().contains("0x2a"));
		boolean linked = false;
		for (JsonElement token : result.getAsJsonArray("tokens")) {
			JsonObject t = token.getAsJsonObject();
			if (t.has("address")) {
				linked = true;
			}
		}
		assertTrue("Expected tokens linked to addresses", linked);
	}

	@Test
	public void disassembleCountBounds() {
		JsonArray items = engine.client("code.disassemble",
			params("programId", programId, "address", entry, "count", 3)).getAsJsonObject()
				.getAsJsonArray("items");
		assertEquals(3, items.size());
		assertEquals("PUSH", items.get(0).getAsJsonObject().get("mnemonic").getAsString());
		assertEquals("55", items.get(0).getAsJsonObject().get("bytes").getAsString());

		JsonObject tooMany = engine.raw(EngineHarness.CLIENT, null, "code.disassemble",
			params("programId", programId, "address", entry, "count", 2001));
		assertEquals("INVALID_PARAMS", EngineHarness.errorCode(tooMany));
	}

	@Test
	public void commentsShowInAssemblyAndCode() {
		engine.desktop("comment.set", params("programId", programId, "address", entry, "kind", "eol",
			"text", "returns the answer", "expectedRevision", engine.revision(programId)));
		JsonObject asm = engine.client("code.disassemble",
			params("programId", programId, "address", entry, "count", 3)).getAsJsonObject();
		assertEquals("returns the answer",
			asm.getAsJsonArray("items").get(0).getAsJsonObject().get("comment").getAsString());
		JsonObject c = engine.client("code.decompile", params("programId", programId, "address", entry))
				.getAsJsonObject();
		assertTrue(c.toString(), c.toString().contains("returns the answer"));
	}
}

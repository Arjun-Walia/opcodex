/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.policy;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;
import io.opcodex.engine.catalog.*;

public class ApprovalPolicyTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		programId = engine.createProjectWithReturn42();
		engine.core().services().catalog().register(new Operation("test.run", "Runs code.",
			OperationClass.RUN, Schemas.object(), Schemas.object(), (ctx, p) -> new JsonObject()));
	}

	@After
	public void tearDown() {
		engine.close();
	}

	private JsonObject clientRename(String newName) {
		String entry = engine.core().services().workspace().openHandle(programId).orElseThrow()
				.program().getMemory().getMinAddress().toString();
		return engine.raw(EngineHarness.CLIENT, "mcp:test-client", "symbol.rename",
			params("programId", programId, "address", entry, "newName", newName,
				"expectedRevision", engine.revision(programId)));
	}

	@Test
	public void defaultPolicyIsAsk() {
		assertEquals("ask", engine.client("policy.get", new JsonObject()).getAsJsonObject()
				.get("mode")
				.getAsString());
	}

	@Test
	public void clientWriteUnderAskReturnsApprovalRequiredWithPreview() {
		JsonObject response = clientRename("parse_header");
		assertEquals("APPROVAL_REQUIRED", EngineHarness.errorCode(response));
		String preview = response.getAsJsonObject("error").getAsJsonObject("data")
				.get("preview").getAsString();
		assertTrue(preview, preview.contains("parse_header"));
	}

	@Test
	public void allowWritesPermitsClientRenameButNotRun() {
		engine.desktop("policy.set", params("mode", "allow-writes"));
		JsonObject renamed = clientRename("parse_header");
		assertNull(renamed.toString(), EngineHarness.errorCode(renamed));
		JsonObject history = engine.desktop("history.list", params("programId", programId))
				.getAsJsonObject().getAsJsonArray("items").get(0).getAsJsonObject();
		assertEquals("mcp:test-client", history.get("actor").getAsString());

		JsonObject run = engine.raw(EngineHarness.CLIENT, null, "test.run", new JsonObject());
		assertEquals("APPROVAL_REQUIRED", EngineHarness.errorCode(run));

		engine.desktop("policy.set", params("mode", "allow-all"));
		assertNull(EngineHarness.errorCode(
			engine.raw(EngineHarness.CLIENT, null, "test.run", new JsonObject())));
	}

	@Test
	public void clientCannotChangePolicy() {
		JsonObject response =
			engine.raw(EngineHarness.CLIENT, null, "policy.set", params("mode", "allow-all"));
		assertEquals("FORBIDDEN", EngineHarness.errorCode(response));
	}

	@Test
	public void policyPersistsWithTheProject() throws Exception {
		engine.desktop("policy.set", params("mode", "allow-writes"));
		engine.close();
		engine = new EngineHarness(engine.projectsDir());
		engine.desktop("project.open",
			params("dir", engine.projectsDir().toString(), "name", "demo"));
		assertEquals("allow-writes", engine.client("policy.get", new JsonObject())
				.getAsJsonObject().get("mode").getAsString());
	}
}

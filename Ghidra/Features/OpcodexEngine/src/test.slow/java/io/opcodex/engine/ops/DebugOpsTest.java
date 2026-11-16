/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class DebugOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private EngineHarness engine;
	private ProgramBuilder builder;
	private String programId;

	@Before
	public void setUp() throws Exception {
		engine = new EngineHarness();
		engine.desktop("project.create", params("dir", engine.projectsDir().toString(), "name", "debug"));
		builder = GraphOpsTest.branchyProgram(this);
		programId = engine.adopt(builder.getProgram());
	}

	@After
	public void tearDown() {
		engine.client("debug.stop", params("programId", programId));
		engine.close();
		builder.dispose();
	}

	private JsonObject call(String method, Object... keyValues) {
		Object[] all = new Object[keyValues.length + 2];
		all[0] = "programId";
		all[1] = programId;
		System.arraycopy(keyValues, 0, all, 2, keyValues.length);
		return engine.client(method, params(all)).getAsJsonObject();
	}

	private static String reg(JsonObject state, String name) {
		for (JsonElement r : state.getAsJsonArray("registers")) {
			if (r.getAsJsonObject().get("name").getAsString().equals(name)) {
				return r.getAsJsonObject().get("value").getAsString();
			}
		}
		throw new AssertionError("no register " + name);
	}

	@Test
	public void stepsThroughTheTakenBranchAndReturns() {
		JsonObject state = call("debug.start", "address", "0x1000");
		assertEquals("00001000", state.get("pc").getAsString());
		assertTrue(state.get("instruction").getAsString().startsWith("PUSH"));
		state = call("debug.step", "count", 4);
		assertEquals("edi is 0, so the branch is taken", "00001010", state.get("pc").getAsString());
		state = call("debug.run");
		assertEquals("returned", state.get("reason").getAsString());
		assertEquals("0x2", reg(state, "RAX"));
		assertFalse(state.get("running").getAsBoolean());
	}

	@Test
	public void startingRegistersChooseTheOtherBranch() {
		JsonObject regs = new JsonObject();
		regs.addProperty("RDI", "5");
		JsonObject start = params("programId", programId, "address", "0x1000");
		start.add("registers", regs);
		engine.client("debug.start", start);
		JsonObject state = call("debug.run");
		assertEquals("0x1", reg(state, "RAX"));
	}

	@Test
	public void stopsAtBreakpointsAndReportsChangedRegisters() {
		call("debug.start", "address", "0x1000");
		call("debug.breakpoint.set", "address", "0x1010");
		JsonObject state = call("debug.run");
		assertEquals("breakpoint", state.get("reason").getAsString());
		assertEquals("00001010", state.get("pc").getAsString());
		JsonArray points = call("debug.state").getAsJsonArray("breakpoints");
		assertEquals(1, points.size());
		state = call("debug.step");
		assertTrue(state.getAsJsonArray("changed").toString().contains("RAX"));
		call("debug.breakpoint.clear", "address", "0x1010");
		assertEquals(0, call("debug.state").getAsJsonArray("breakpoints").size());
	}

	@Test
	public void stepOverRunsAWholeCall() {
		JsonObject state = call("debug.start", "address", "0x1020");
		assertTrue(state.get("instruction").getAsString().startsWith("CALL"));
		state = call("debug.stepOver");
		assertEquals("00001025", state.get("pc").getAsString());
		assertEquals("step", state.get("reason").getAsString());
		assertEquals("0x2", reg(state, "RAX"));
	}

	@Test
	public void showsTheStackAndMemory() {
		JsonObject state = call("debug.start", "address", "0x1000");
		JsonArray stack = state.getAsJsonArray("stack");
		assertTrue(stack.size() >= 4);
		JsonObject memory = call("debug.memory.read", "address", "0x1000", "length", 4);
		assertEquals("554889e5", memory.get("hex").getAsString());
	}

	@Test
	public void instructionsTheEmulatorCannotModelAreSkippedWithANote() {
		call("debug.start", "address", "0x1040");
		JsonObject state = call("debug.run");
		assertEquals("returned", state.get("reason").getAsString());
		assertTrue(state.getAsJsonArray("events").toString().contains("cpuid"));
	}

	@Test
	public void nothingToStepWithoutASession() {
		JsonObject response = engine.raw(EngineHarness.CLIENT, null, "debug.step", params("programId", programId));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(response));
	}
}

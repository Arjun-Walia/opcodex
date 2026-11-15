/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.util.concurrent.*;

import org.junit.Test;

import com.google.gson.JsonObject;

import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class JobsTest extends AbstractGhidraHeadlessIntegrationTest {
	@Test
	public void cancelledAnalysisLeavesTheProgramUsable() throws Exception {
		try (EngineHarness engine = new EngineHarness();
				ExecutorService executor = Executors.newSingleThreadExecutor()) {
			String programId = engine.createProjectWithReturn42();
			CountDownLatch writing = new CountDownLatch(1);
			CountDownLatch resume = new CountDownLatch(1);
			Future<?> blocker = executor.submit(() -> engine.core().services().workspace()
					.write(programId, "Test writer", program -> {
						writing.countDown();
						if (!resume.await(5, TimeUnit.SECONDS)) {
							throw new TimeoutException("Writing did not resume");
						}
						return null;
					}));
			String jobId;
			try {
				assertTrue(writing.await(5, TimeUnit.SECONDS));
				jobId = start(engine, programId);
				engine.desktop("job.cancel", params("jobId", jobId));
			}
			finally {
				resume.countDown();
			}
			blocker.get(5, TimeUnit.SECONDS);
			assertEquals("cancelled", waitForJob(engine, jobId).get("state").getAsString());
			assertEquals(programId, engine.client("program.info", params("programId", programId))
					.getAsJsonObject().get("programId").getAsString());
			JsonObject complete = waitForJob(engine, start(engine, programId));
			assertEquals("done", complete.get("state").getAsString());
			assertEquals(programId, complete.getAsJsonObject("result").get("programId").getAsString());

			engine.desktop("project.close", new JsonObject());
			engine.desktop("project.open", params("dir", engine.projectsDir().toString(), "name", "demo"));
			assertEquals("NOT_FOUND", EngineHarness.errorCode(engine.raw(EngineHarness.CLIENT, null,
				"job.get", params("jobId", jobId))));
		}
	}

	private static String start(EngineHarness engine, String programId) {
		JsonObject params = params("programId", programId);
		params.addProperty("background", true);
		return engine.desktop("program.analyze", params).getAsJsonObject().get("jobId").getAsString();
	}

	private static JsonObject waitForJob(EngineHarness engine, String id) throws InterruptedException {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
		do {
			JsonObject job = engine.client("job.get", params("jobId", id)).getAsJsonObject();
			if (!"running".equals(job.get("state").getAsString())) {
				return job;
			}
			Thread.sleep(20);
		} while (System.nanoTime() < deadline);
		throw new AssertionError("Job did not finish: " + id);
	}
}

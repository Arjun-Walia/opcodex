/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.jobs;

import static org.junit.Assert.*;

import java.util.concurrent.*;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JobManagerTest {

	@Test
	public void reportsProgressAndCompletion() throws Exception {
		try (JobManager jobs = new JobManager()) {
			Object project = new Object();
			BlockingQueue<JsonObject> events = new LinkedBlockingQueue<>();
			Job job = jobs.start(project, "analysis", "demo", events::add, monitor -> {
				monitor.initialize(10);
				monitor.setProgress(10);
				return new JsonPrimitive(42);
			});
			JsonObject completed;
			do {
				completed = events.poll(5, TimeUnit.SECONDS);
				assertNotNull("Expected a job event", completed);
			} while (!"done".equals(completed.getAsJsonObject("params").get("state").getAsString()));
			assertEquals(10, completed.getAsJsonObject("params").get("progress").getAsInt());
			assertEquals(42, jobs.get(project, job.id()).toJson().get("result").getAsInt());
		}
	}

	@Test
	public void cancellationIsScopedAndCooperative() throws Exception {
		try (JobManager jobs = new JobManager()) {
			Object project = new Object();
			CountDownLatch started = new CountDownLatch(1);
			CountDownLatch resume = new CountDownLatch(1);
			CountDownLatch cancelled = new CountDownLatch(1);
			Job job = jobs.start(project, "analysis", "demo", event -> {
				if ("cancelled".equals(event.getAsJsonObject("params").get("state").getAsString())) {
					cancelled.countDown();
				}
			}, monitor -> {
				started.countDown();
				assertTrue(resume.await(5, TimeUnit.SECONDS));
				monitor.checkCancelled();
				return new JsonPrimitive(42);
			});
			assertTrue(started.await(5, TimeUnit.SECONDS));
			assertThrows(io.opcodex.engine.rpc.RpcException.class,
				() -> jobs.get(new Object(), job.id()));
			job.cancel();
			resume.countDown();
			assertTrue(cancelled.await(5, TimeUnit.SECONDS));
			assertEquals("cancelled", job.toJson().get("state").getAsString());
		}
	}
}

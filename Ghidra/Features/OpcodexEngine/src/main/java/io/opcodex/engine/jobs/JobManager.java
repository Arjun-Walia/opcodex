/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.jobs;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/** Bounds queued and retained jobs. Slow clients can poll when progress delivery falls behind. */
public final class JobManager implements AutoCloseable {
	private static final int MAX_ACTIVE = 16;
	private static final int MAX_RETAINED = 64;

	@FunctionalInterface
	public interface JobBody {
		JsonElement run(JobMonitor monitor) throws Exception;
	}

	private record Entry(Object project, Job job) {
	}

	private final Map<String, Entry> jobs = new LinkedHashMap<>();
	private final ExecutorService worker = Executors.newSingleThreadExecutor(
		Thread.ofPlatform().daemon().name("opcodex-job-").factory());
	private final ExecutorService events = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
		new ArrayBlockingQueue<>(64), Thread.ofPlatform().daemon().name("opcodex-job-events-").factory(),
		new ThreadPoolExecutor.DiscardPolicy());

	public synchronized Job start(Object project, String kind, String programId,
			Consumer<JsonObject> sink, JobBody body) throws RpcException {
		if (worker.isShutdown() || jobs.values().stream().filter(e -> !e.job().finished())
				.count() >= MAX_ACTIVE) {
			throw new RpcException(ErrorCode.CONFLICT, "No slot for a background job. Try again later.");
		}
		if (jobs.size() >= MAX_RETAINED) {
			Iterator<Entry> iterator = jobs.values().iterator();
			while (iterator.hasNext()) {
				if (iterator.next().job().finished()) {
					iterator.remove();
					break;
				}
			}
		}
		Job job = new Job(kind, programId, sink, events::execute);
		jobs.put(job.id(), new Entry(project, job));
		worker.execute(() -> {
			events.execute(job::emit);
			job.run(body);
			events.execute(job::emit);
		});
		return job;
	}

	public synchronized Job get(Object project, String id) throws RpcException {
		Entry entry = jobs.get(id);
		if (entry == null || entry.project() != project) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No job with id " + id);
		}
		return entry.job();
	}

	public synchronized List<Job> list(Object project) {
		return jobs.values().stream().filter(e -> e.project() == project).map(Entry::job).toList();
	}

	public synchronized void cancelProject(Object project) {
		list(project).forEach(Job::cancel);
	}

	@Override
	public synchronized void close() {
		jobs.values().forEach(entry -> entry.job().cancel());
		worker.shutdown();
		events.shutdownNow();
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.jobs;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ghidra.util.Msg;
import ghidra.util.exception.CancelledException;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/** One background operation and its observable state. */
public final class Job {
	private final String id = UUID.randomUUID().toString();
	private final String kind;
	private final String programId;
	private final Consumer<JsonObject> events;
	private final JobMonitor monitor;
	private long lastProgress = System.nanoTime();
	private volatile Outcome outcome;

	private record Outcome(String state, JsonElement result, String error) {
	}

	Job(String kind, String programId, Consumer<JsonObject> events, Consumer<Runnable> delivery) {
		this.kind = kind;
		this.programId = programId;
		this.events = events;
		monitor = new JobMonitor(() -> {
			long now = System.nanoTime();
			if (!finished() && now - lastProgress >= TimeUnit.MILLISECONDS.toNanos(200)) {
				lastProgress = now;
				delivery.accept(this::emit);
			}
		});
	}

	public String id() {
		return id;
	}

	public void cancel() {
		if (!finished()) {
			monitor.requestCancel();
		}
	}

	boolean finished() {
		return outcome != null;
	}

	void run(JobManager.JobBody body) {
		try {
			monitor.checkCancelled();
			JsonElement result = body.run(monitor);
			monitor.beginCommit();
			outcome = new Outcome("done", result, null);
		}
		catch (Exception e) {
			if (e instanceof CancelledException ||
				(e instanceof RpcException rpc && rpc.code() == ErrorCode.CANCELLED)) {
				outcome = new Outcome("cancelled", null, null);
			}
			else {
				Msg.error(this, "Background job " + id + " failed", e);
				outcome = new Outcome("failed", null, "The job failed. See the engine log.");
			}
		}
	}

	public JsonObject toJson() {
		Outcome completed = outcome;
		JsonObject out = new JsonObject();
		out.addProperty("jobId", id);
		out.addProperty("kind", kind);
		out.addProperty("programId", programId);
		out.addProperty("state", completed == null ? "running" : completed.state());
		monitor.addProgress(out);
		if (completed != null) {
			if (completed.result() != null) {
				out.add("result", completed.result().deepCopy());
			}
			if (completed.error() != null) {
				out.addProperty("error", completed.error());
			}
		}
		return out;
	}

	void emit() {
		JsonObject progress = toJson();
		for (String field : new String[] { "kind", "programId", "result", "error" }) {
			progress.remove(field);
		}
		JsonObject notification = new JsonObject();
		notification.addProperty("jsonrpc", "2.0");
		notification.addProperty("method", "job.progress");
		notification.add("params", progress);
		events.accept(notification);
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.jobs;

import com.google.gson.JsonObject;

import ghidra.util.exception.CancelledException;
import ghidra.util.task.TaskMonitorAdapter;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/** Captures analysis progress and keeps a cancellation request across analyzer stages. */
public final class JobMonitor extends TaskMonitorAdapter {
	private final Runnable changed;
	private String message = "";
	private long progress;
	private long maximum;
	private boolean committed;
	private volatile boolean requested;

	JobMonitor(Runnable changed) {
		super(true);
		this.changed = changed;
	}

	@Override
	public synchronized void setMessage(String message) {
		this.message = message == null ? "" : message;
		changed.run();
	}

	@Override
	public synchronized String getMessage() {
		return message;
	}

	@Override
	public synchronized void setProgress(long progress) {
		this.progress = progress;
		changed.run();
	}

	@Override
	public synchronized void incrementProgress(long amount) {
		setProgress(progress + amount);
	}

	@Override
	public synchronized long getProgress() {
		return progress;
	}

	@Override
	public synchronized void initialize(long maximum) {
		progress = 0;
		setMaximum(maximum);
	}

	@Override
	public synchronized void setMaximum(long maximum) {
		this.maximum = maximum;
		changed.run();
	}

	@Override
	public synchronized long getMaximum() {
		return maximum;
	}

	@Override
	public boolean isCancelled() {
		return requested || super.isCancelled();
	}

	@Override
	public void checkCancelled() throws CancelledException {
		if (isCancelled()) {
			throw new CancelledException();
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void checkCanceled() throws CancelledException {
		checkCancelled();
	}

	synchronized void requestCancel() {
		if (!committed) {
			requested = true;
		}
	}

	/** Establishes the point after which the transaction can commit without a late cancel. */
	public synchronized void beginCommit() throws RpcException {
		if (isCancelled()) {
			throw new RpcException(ErrorCode.CANCELLED, "Analysis was cancelled.");
		}
		committed = true;
	}

	synchronized void addProgress(JsonObject out) {
		out.addProperty("message", message);
		out.addProperty("progress", progress);
		out.addProperty("maximum", maximum);
	}
}

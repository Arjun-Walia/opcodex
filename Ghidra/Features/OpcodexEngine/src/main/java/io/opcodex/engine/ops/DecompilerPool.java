/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import ghidra.app.decompiler.*;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.Program;
import ghidra.util.task.TaskMonitor;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/** A small pool of decompiler processes for one program; disposed when the program closes. */
final class DecompilerPool implements AutoCloseable {

	static final int SIZE = 2;

	private final Program program;
	private final BlockingQueue<DecompInterface> idle = new ArrayBlockingQueue<>(SIZE);
	private final List<DecompInterface> all = new ArrayList<>();
	private boolean closed;

	DecompilerPool(Program program) {
		this.program = program;
	}

	DecompileResults decompile(Function function, int timeoutSec) throws RpcException {
		DecompInterface decompiler = borrow();
		try {
			DecompileResults results =
				decompiler.decompileFunction(function, timeoutSec, TaskMonitor.DUMMY);
			if (results == null || !results.decompileCompleted()) {
				String reason = results == null ? "no result" : results.getErrorMessage();
				if (results != null && results.isTimedOut()) {
					throw new RpcException(ErrorCode.CANCELLED,
						"Decompiling " + function.getName() + " took longer than " + timeoutSec +
							" s.");
				}
				throw new RpcException(ErrorCode.ENGINE_ERROR,
					"The decompiler couldn't finish " + function.getName() + ": " + reason);
			}
			return results;
		}
		finally {
			idle.offer(decompiler);
		}
	}

	private DecompInterface borrow() throws RpcException {
		synchronized (this) {
			if (closed) {
				throw new RpcException(ErrorCode.NOT_FOUND, "The program was closed.");
			}
			DecompInterface ready = idle.poll();
			if (ready != null) {
				return ready;
			}
			if (all.size() < SIZE) {
				DecompInterface created = new DecompInterface();
				created.toggleCCode(true);
				created.toggleSyntaxTree(true);
				created.setSimplificationStyle("decompile");
				if (!created.openProgram(program)) {
					String message = created.getLastMessage();
					created.dispose();
					throw new RpcException(ErrorCode.ENGINE_ERROR,
						"The decompiler couldn't start: " + message);
				}
				all.add(created);
				return created;
			}
		}
		try {
			DecompInterface next = idle.poll(5, TimeUnit.MINUTES);
			if (next == null) {
				throw new RpcException(ErrorCode.CANCELLED, "Timed out waiting for a decompiler.");
			}
			return next;
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RpcException(ErrorCode.CANCELLED, "Decompile was cancelled.");
		}
	}

	@Override
	public synchronized void close() {
		closed = true;
		all.forEach(DecompInterface::dispose);
		all.clear();
		idle.clear();
	}
}

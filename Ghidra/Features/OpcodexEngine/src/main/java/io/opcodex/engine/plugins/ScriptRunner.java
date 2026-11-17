/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.plugins;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import generic.jar.ResourceFile;
import ghidra.app.plugin.core.osgi.BundleHost;
import ghidra.app.script.*;
import ghidra.framework.model.Project;
import ghidra.program.model.address.Address;
import ghidra.program.model.listing.Program;
import ghidra.program.util.ProgramLocation;
import ghidra.util.task.TaskMonitor;
import ghidra.util.task.TaskMonitorAdapter;

/** Runs Java GhidraScripts headlessly against a program, capturing what they print. */
public final class ScriptRunner {

	public static final int MAX_OUTPUT_CHARS = 200_000;

	public record Result(String output, String error, boolean timedOut) {
		Result(String output, String error) {
			this(output, error, false);
		}
	}

	private static BundleHost host;
	private static final Set<Path> added = new HashSet<>();

	private ScriptRunner() {
	}

	private static synchronized BundleHost host(Path scriptDir) {
		if (host == null) {
			host = GhidraScriptUtil.acquireBundleHostReference();
		}
		Path dir = scriptDir.toAbsolutePath().normalize();
		if (added.add(dir)) {
			host.add(List.of(new ResourceFile(dir.toFile())), true, false);
		}
		return host;
	}

	/** Loads the script; throws with a readable message if it doesn't compile. */
	public static GhidraScript load(Path script) throws Exception {
		host(script.getParent());
		ResourceFile source = new ResourceFile(script.toFile());
		GhidraScriptProvider provider = GhidraScriptUtil.getProvider(source);
		if (provider == null) {
			throw new IllegalArgumentException("Opcodex runs Java scripts (.java); this isn't one.");
		}
		StringWriter problems = new StringWriter();
		try {
			return provider.getScriptInstance(source, new PrintWriter(problems));
		}
		catch (GhidraScriptLoadException e) {
			String detail = problems.toString().trim();
			throw new IllegalArgumentException("The script didn't compile" +
				(detail.isEmpty() ? ": " + e.getMessage() : ":\n" + truncate(detail)));
		}
	}

	/**
	 * Runs inside the caller's transaction, on a thread of its own with a hard time limit. Printing
	 * is captured; failures are returned, not thrown. A script that ignores cancellation is
	 * abandoned at the limit: the caller rolls the transaction back, so its later writes fail.
	 */
	public static Result run(GhidraScript script, Project project, Program program, Address at, String[] args,
			long timeLimitMillis) {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out, true);
		script.setScriptArgs(args);
		GhidraState state = new GhidraState(null, project, program,
			at == null ? null : new ProgramLocation(program, at), null, null);
		TaskMonitorAdapter monitor = new TaskMonitorAdapter(true);
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				script.execute(state, new ScriptControls(writer, writer, false, monitor));
			}
			catch (Throwable t) {
				failure.set(t);
			}
		}, "opcodex-script-" + script.getClass().getSimpleName());
		thread.setDaemon(true);
		thread.start();
		try {
			thread.join(timeLimitMillis);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (thread.isAlive()) {
			monitor.cancel();
			thread.interrupt();
			try {
				// End the script's own nested transaction for it, so the caller's rollback can
				// complete and the program stays usable even if the script never returns.
				script.end(false);
			}
			catch (RuntimeException alreadyEnded) {
				// It finished at the last moment.
			}
			return new Result(truncate(out.toString()), "The script ran longer than " +
				Math.max(1, timeLimitMillis / 60000) + " minutes and was stopped; nothing it changed was kept. " +
				"If it ignores cancellation it may keep using the processor until the engine restarts.", true);
		}
		Throwable t = failure.get();
		if (t != null) {
			String message = t.getMessage() == null ? t.toString() : t.getMessage();
			return new Result(truncate(out.toString()), message);
		}
		return new Result(truncate(out.toString()), null);
	}

	static String truncate(String text) {
		return text.length() <= MAX_OUTPUT_CHARS ? text
				: text.substring(0, MAX_OUTPUT_CHARS) + "\n… output cut at " + MAX_OUTPUT_CHARS + " characters.";
	}
}

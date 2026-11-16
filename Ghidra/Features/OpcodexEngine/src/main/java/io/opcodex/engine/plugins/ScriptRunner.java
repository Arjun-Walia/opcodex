/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.plugins;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.*;

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

	public record Result(String output, String error) {
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

	/** Runs inside the caller's transaction. Printing is captured; errors are returned, not thrown. */
	public static Result run(GhidraScript script, Project project, Program program, Address at, String[] args,
			TaskMonitor monitor) {
		StringWriter out = new StringWriter();
		PrintWriter writer = new PrintWriter(out, true);
		script.setScriptArgs(args);
		GhidraState state = new GhidraState(null, project, program,
			at == null ? null : new ProgramLocation(program, at), null, null);
		try {
			script.execute(state, new ScriptControls(writer, writer, false, monitor));
			return new Result(truncate(out.toString()), null);
		}
		catch (Exception e) {
			String message = e.getMessage() == null ? e.toString() : e.getMessage();
			return new Result(truncate(out.toString()), message);
		}
	}

	/** A monitor that cancels after a time limit, so a runaway script can't hang the engine. */
	public static TaskMonitor timeLimit(long millis) {
		long deadline = System.currentTimeMillis() + millis;
		return new TaskMonitorAdapter(true) {
			@Override
			public boolean isCancelled() {
				return super.isCancelled() || System.currentTimeMillis() > deadline;
			}
		};
	}

	static String truncate(String text) {
		return text.length() <= MAX_OUTPUT_CHARS ? text
				: text.substring(0, MAX_OUTPUT_CHARS) + "\n… output cut at " + MAX_OUTPUT_CHARS + " characters.";
	}
}

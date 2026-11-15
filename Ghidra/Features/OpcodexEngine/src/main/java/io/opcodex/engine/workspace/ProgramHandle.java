/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.workspace;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

import ghidra.framework.model.DomainFile;
import ghidra.program.model.listing.Program;
import ghidra.util.Msg;

/** An open program with its own single writer thread, so edits never interleave. */
public final class ProgramHandle {

	private final String id;
	private final Program program;
	private final ExecutorService writer;
	private final Map<Class<?>, AutoCloseable> attachments = new ConcurrentHashMap<>();

	ProgramHandle(String id, Program program) {
		this.id = id;
		this.program = program;
		this.writer = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "opcodex-writer-" + id);
			t.setDaemon(true);
			return t;
		});
	}

	/** Stable program id derived from the project file's id. */
	public static String idFor(DomainFile file) {
		return UUID.nameUUIDFromBytes(file.getFileID().getBytes(StandardCharsets.UTF_8))
				.toString();
	}

	public String id() {
		return id;
	}

	public Program program() {
		return program;
	}

	/** The program's modification number; every committed change increases it. */
	public long revision() {
		return program.getModificationNumber();
	}

	/** Returns the per-program helper of this type, creating it once. Closed with the program. */
	public <T extends AutoCloseable> T attachment(Class<T> type, Supplier<T> factory) {
		return type.cast(attachments.computeIfAbsent(type, k -> factory.get()));
	}

	ExecutorService writer() {
		return writer;
	}

	void shutdown() {
		writer.shutdownNow();
		for (AutoCloseable attachment : attachments.values()) {
			try {
				attachment.close();
			}
			catch (Exception e) {
				Msg.warn(this, "Could not close " + attachment.getClass().getSimpleName(), e);
			}
		}
		attachments.clear();
	}
}

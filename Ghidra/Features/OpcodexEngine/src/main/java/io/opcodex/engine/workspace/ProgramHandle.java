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

/**
 * An open program with its own single writer thread, so edits never interleave.
 * <p>
 * Its revision starts above every revision issued before for this program, including those from
 * earlier engine runs, so a client's old {@code expectedRevision} can never match again.
 */
public final class ProgramHandle {

	private static final long DRAIN_SECONDS = 120;

	private final String id;
	private final Program program;
	private final ExecutorService writer;
	private final Map<Class<?>, AutoCloseable> attachments = new ConcurrentHashMap<>();
	private final long base;
	private final long modificationAtOpen;

	ProgramHandle(String id, Program program, long base) {
		this.id = id;
		this.program = program;
		this.base = base;
		this.modificationAtOpen = program.getModificationNumber();
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

	/** The program's revision; every committed change increases it and it never repeats. */
	public long revision() {
		return base + (program.getModificationNumber() - modificationAtOpen);
	}

	/** Returns the per-program helper of this type, creating it once. Closed with the program. */
	public <T extends AutoCloseable> T attachment(Class<T> type, Supplier<T> factory) {
		return type.cast(attachments.computeIfAbsent(type, k -> factory.get()));
	}

	ExecutorService writer() {
		return writer;
	}

	/** Lets queued and running writes finish, then closes the per-program helpers. */
	void shutdown() {
		writer.shutdown();
		try {
			if (!writer.awaitTermination(DRAIN_SECONDS, TimeUnit.SECONDS)) {
				Msg.warn(this, "Writes to " + id + " didn't finish in time; stopping them.");
				writer.shutdownNow();
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			writer.shutdownNow();
		}
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

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.journal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import ghidra.util.Msg;

/** Append-only record of every change, stored as one JSON object per line. */
public final class Journal {

	public static final String FILE_NAME = "journal.jsonl";

	private final Path file;

	public Journal(Path dataDir) {
		this.file = dataDir.resolve(FILE_NAME);
	}

	/** Appends one entry and forces it to disk before returning. */
	public synchronized void append(JournalEntry entry) throws IOException {
		Files.createDirectories(file.getParent());
		byte[] line = (entry.toJson().toString() + "\n").getBytes(StandardCharsets.UTF_8);
		try (FileChannel channel = FileChannel.open(file, StandardOpenOption.CREATE,
			StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
			ByteBuffer buffer = ByteBuffer.wrap(line);
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			channel.force(true);
		}
	}

	/** Entries matching the filter, newest first. */
	public synchronized List<JournalEntry> newestFirst(Predicate<JournalEntry> filter)
			throws IOException {
		List<JournalEntry> out = new ArrayList<>();
		if (!Files.exists(file)) {
			return out;
		}
		for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
			if (line.isBlank()) {
				continue;
			}
			try {
				JournalEntry entry = JournalEntry.fromJson(JsonParser.parseString(line).getAsJsonObject());
				if (filter.test(entry)) {
					out.add(entry);
				}
			}
			catch (JsonParseException | IllegalStateException | NullPointerException e) {
				// A torn final line after a crash; keep every complete entry.
				Msg.warn(this, "Skipping an unreadable journal line in " + file);
			}
		}
		Collections.reverse(out);
		return out;
	}

	/** The highest revision journaled for a program, or 0 if none. */
	public long maxRevision(String programId) throws IOException {
		return newestFirst(e -> e.programId().equals(programId)).stream()
				.mapToLong(e -> Math.max(e.revisionBefore(), e.revisionAfter()))
				.max()
				.orElse(0);
	}

	public Optional<JournalEntry> find(String id) throws IOException {
		return newestFirst(e -> e.id().equals(id)).stream().findFirst();
	}
}

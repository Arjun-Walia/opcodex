/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.policy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.opcodex.engine.catalog.OperationClass;

/** What CLI and MCP clients may change in a project without a person approving it. */
public enum ApprovalPolicy {
	/** Every write or run needs approval. */
	ASK("ask"),
	/** Writes are allowed; running code still needs approval. */
	ALLOW_WRITES("allow-writes"),
	/** Writes and running code are allowed. */
	ALLOW_ALL("allow-all");

	public static final String FILE_NAME = "policy.json";

	private final String wireName;

	ApprovalPolicy(String wireName) {
		this.wireName = wireName;
	}

	public String wireName() {
		return wireName;
	}

	public static Optional<ApprovalPolicy> fromWire(String name) {
		return Arrays.stream(values()).filter(p -> p.wireName.equals(name)).findFirst();
	}

	/** Whether a client may run an operation of this class without approval. */
	public boolean permits(OperationClass cls) {
		return switch (cls) {
			case READ -> true;
			case WRITE -> this != ASK;
			case RUN -> this == ALLOW_ALL;
			case SYSTEM -> false;
		};
	}

	public static ApprovalPolicy load(Path dataDir) throws IOException {
		Path file = dataDir.resolve(FILE_NAME);
		if (!Files.exists(file)) {
			return ASK;
		}
		JsonObject json =
			JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
		return fromWire(json.get("mode").getAsString()).orElse(ASK);
	}

	public void save(Path dataDir) throws IOException {
		Files.createDirectories(dataDir);
		JsonObject json = new JsonObject();
		json.addProperty("mode", wireName);
		Path temp = Files.createTempFile(dataDir, ".policy", ".tmp");
		try {
			Files.writeString(temp, json + "\n", StandardCharsets.UTF_8);
			Files.move(temp, dataDir.resolve(FILE_NAME), StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
		}
		finally {
			Files.deleteIfExists(temp);
		}
	}
}

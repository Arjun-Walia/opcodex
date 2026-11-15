/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.journal;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

import com.google.gson.*;

/**
 * One recorded change: who made it, what it changed and the value before and after, so the
 * change can be reverted selectively later, even after a restart.
 */
public record JournalEntry(String id, String time, String actor, String op, String programId,
		long revisionBefore, long revisionAfter, JsonObject target, JsonElement before,
		JsonElement after, String revertOf, String summary) {

	private static final SecureRandom RANDOM = new SecureRandom();

	/** A new time-ordered id: milliseconds then random bits. */
	public static String newId() {
		byte[] random = new byte[6];
		RANDOM.nextBytes(random);
		return String.format("%013x", System.currentTimeMillis()) + HexFormat.of().formatHex(random);
	}

	public static String now() {
		return Instant.now().toString();
	}

	public JsonObject toJson() {
		JsonObject out = new JsonObject();
		out.addProperty("id", id);
		out.addProperty("time", time);
		out.addProperty("actor", actor);
		out.addProperty("op", op);
		out.addProperty("programId", programId);
		out.addProperty("revisionBefore", revisionBefore);
		out.addProperty("revisionAfter", revisionAfter);
		out.add("target", target == null ? JsonNull.INSTANCE : target.deepCopy());
		out.add("before", before == null ? JsonNull.INSTANCE : before.deepCopy());
		out.add("after", after == null ? JsonNull.INSTANCE : after.deepCopy());
		if (revertOf != null) {
			out.addProperty("revertOf", revertOf);
		}
		if (summary != null) {
			out.addProperty("summary", summary);
		}
		return out;
	}

	public static JournalEntry fromJson(JsonObject json) {
		return new JournalEntry(json.get("id").getAsString(), json.get("time").getAsString(),
			json.get("actor").getAsString(), json.get("op").getAsString(),
			json.get("programId").getAsString(), json.get("revisionBefore").getAsLong(),
			json.get("revisionAfter").getAsLong(),
			json.get("target").isJsonObject() ? json.getAsJsonObject("target") : null,
			json.get("before"), json.get("after"),
			json.has("revertOf") ? json.get("revertOf").getAsString() : null,
			json.has("summary") ? json.get("summary").getAsString() : null);
	}
}

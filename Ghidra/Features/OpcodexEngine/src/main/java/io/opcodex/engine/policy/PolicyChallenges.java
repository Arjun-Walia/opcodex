/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.policy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.*;
import java.util.Optional;

/**
 * One-time codes for changing the approval policy from a terminal. The CLI shows the code and
 * a person types it back. Each code allows one attempt and expires after 60 seconds.
 */
public final class PolicyChallenges {

	public static final Duration LIFETIME = Duration.ofSeconds(60);

	private record Pending(String code, ApprovalPolicy mode, Instant expires) {
	}

	private final SecureRandom random = new SecureRandom();
	private final Clock clock;
	private Pending pending;

	public PolicyChallenges(Clock clock) {
		this.clock = clock;
	}

	/** Starts a change; replaces any earlier pending one. Returns the six-digit code. */
	public synchronized String request(ApprovalPolicy mode) {
		String code = String.format("%06d", random.nextInt(1_000_000));
		pending = new Pending(code, mode, clock.instant().plus(LIFETIME));
		return code;
	}

	/** Consumes the pending change; returns its mode only if the typed code matches in time. */
	public synchronized Optional<ApprovalPolicy> confirm(String challenge, String typed) {
		Pending current = pending;
		pending = null;
		if (current == null || challenge == null || typed == null) {
			return Optional.empty();
		}
		boolean sameChallenge = equal(current.code(), challenge);
		boolean typedCorrectly = equal(current.code(), typed.strip());
		boolean inTime = !clock.instant().isAfter(current.expires());
		return sameChallenge && typedCorrectly && inTime ? Optional.of(current.mode())
				: Optional.empty();
	}

	private static boolean equal(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
			b.getBytes(StandardCharsets.UTF_8));
	}
}

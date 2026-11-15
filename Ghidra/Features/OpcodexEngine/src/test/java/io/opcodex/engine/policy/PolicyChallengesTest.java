/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.policy;

import static org.junit.Assert.*;

import java.time.*;
import java.util.Optional;

import org.junit.Test;

public class PolicyChallengesTest {

	/** A clock the test moves by hand. */
	private static final class ManualClock extends Clock {
		private Instant now = Instant.parse("2026-11-15T10:00:00Z");

		void advance(Duration d) {
			now = now.plus(d);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return now;
		}
	}

	@Test
	public void correctCodeReturnsRequestedMode() {
		PolicyChallenges challenges = new PolicyChallenges(new ManualClock());
		String code = challenges.request(ApprovalPolicy.ALLOW_WRITES);
		assertTrue(code.matches("\\d{6}"));
		assertEquals(Optional.of(ApprovalPolicy.ALLOW_WRITES), challenges.confirm(code, code));
	}

	@Test
	public void confirmWithWrongCodeFailsAndInvalidatesChallenge() {
		PolicyChallenges challenges = new PolicyChallenges(new ManualClock());
		String code = challenges.request(ApprovalPolicy.ALLOW_ALL);
		String wrong = code.equals("000000") ? "111111" : "000000";
		assertEquals(Optional.empty(), challenges.confirm(code, wrong));
		assertEquals("one attempt only", Optional.empty(), challenges.confirm(code, code));
	}

	@Test
	public void challengeExpiresAfter60s() {
		ManualClock clock = new ManualClock();
		PolicyChallenges challenges = new PolicyChallenges(clock);
		String code = challenges.request(ApprovalPolicy.ALLOW_WRITES);
		clock.advance(Duration.ofSeconds(61));
		assertEquals(Optional.empty(), challenges.confirm(code, code));
	}

	@Test
	public void aNewRequestReplacesTheOldOne() {
		PolicyChallenges challenges = new PolicyChallenges(new ManualClock());
		challenges.request(ApprovalPolicy.ALLOW_ALL);
		String second = challenges.request(ApprovalPolicy.ALLOW_WRITES);
		assertEquals(Optional.of(ApprovalPolicy.ALLOW_WRITES), challenges.confirm(second, second));
	}

	@Test
	public void anyFailedAttemptUsesUpThePendingCode() {
		PolicyChallenges challenges = new PolicyChallenges(new ManualClock());
		String first = challenges.request(ApprovalPolicy.ALLOW_ALL);
		String second = challenges.request(ApprovalPolicy.ALLOW_WRITES);
		if (first.equals(second)) {
			return; // One-in-a-million collision; nothing to distinguish.
		}
		assertEquals(Optional.empty(), challenges.confirm(first, first));
		assertEquals(Optional.empty(), challenges.confirm(second, second));
	}
}

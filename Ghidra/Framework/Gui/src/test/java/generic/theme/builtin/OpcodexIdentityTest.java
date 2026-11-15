/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import static org.junit.Assert.*;
import org.junit.Test;
import ghidra.framework.ApplicationProperties;

public class OpcodexIdentityTest {
	@Test public void namedProductCheckoutReportsItsRevision() {
		var properties = new ApplicationProperties("Opcodex");
		properties.setProperty("application.revision.opcodex", "c".repeat(40));
		assertEquals("c".repeat(40), properties.getSourceRevision());
	}
	@Test public void numberedSourceCheckoutsReportTheActualRevision() {
		var properties = new ApplicationProperties("Opcodex");
		properties.setProperty("application.revision.src2", "a".repeat(40));
		assertEquals("a".repeat(40), properties.getSourceRevision());
	}
	@Test public void productRevisionWinsOverSecondaryRepositoryMetadata() {
		var properties = new ApplicationProperties("Opcodex");
		properties.setProperty("application.revision.src", "b".repeat(40));
		properties.setProperty("opcodex.source.revision", "a".repeat(40));
		assertEquals("a".repeat(40), properties.getSourceRevision());
	}
	@Test public void missingRevisionIsExplicitRatherThanAQuestionMark() {
		var properties = new ApplicationProperties("Opcodex");
		properties.setProperty("application.revision.src2", "unknown");
		assertEquals("Unavailable (development checkout)", properties.getSourceRevision());
	}
}

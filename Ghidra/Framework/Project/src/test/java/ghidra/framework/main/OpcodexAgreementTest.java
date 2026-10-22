/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package ghidra.framework.main;

import static org.junit.Assert.*;
import org.junit.Test;
import ghidra.util.HTMLUtilities;

public class OpcodexAgreementTest {
    @Test public void provenanceCommentsDoNotCreateVisibleBlankParagraphs() {
        String source = "<!-- OPCODEX: provenance -->\n<HTML>\n<HEAD><TITLE>Agreement</TITLE></HEAD>\n" +
            "<BODY>First paragraph\ncontinues here.<BR><BR>Second paragraph.</BODY></HTML>";
        String html = UserAgreementDialog.prepareAgreementText(source);
        assertTrue(HTMLUtilities.isHTML(html));
        assertFalse(html.contains("provenance"));
        assertEquals(2, html.split("<BR>", -1).length - 1);
        assertTrue(html.contains("First paragraph continues here."));
        assertTrue(html.contains("Second paragraph."));
    }
    @Test public void plainTextFallbackRemainsAnHtmlDocument() {
        assertTrue(HTMLUtilities.isHTML(UserAgreementDialog.prepareAgreementText("First line\nSecond line")));
    }
}

/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package ghidra.util.filechooser;

import static org.junit.Assert.*;
import java.io.File;
import java.util.List;
import org.junit.Test;

public class OpcodexNativePatternsTest {
	@Test public void extensionPatternsPreserveCompoundSuffixesAndDoNotParseLabels() {
		var filter = ExtensionFileFilter.forExtensions("Images (translated label)", "PNG", "tar.gz");
		assertEquals(List.of("*.png", "*.tar.gz"), filter.getNativePatterns());
	}
	@Test public void emptyExtensionFilterAndAllFilesUseTheShellWildcard() {
		assertEquals(List.of("*.*"), ExtensionFileFilter.forExtensions("Any").getNativePatterns());
		assertEquals(List.of("*.*"), GhidraFileFilter.ALL.getNativePatterns());
	}
	@Test public void customPredicatesAndSubclassesKeepTheApplicationPicker() {
		GhidraFileFilter custom = new GhidraFileFilter() {
			@Override public String getDescription() { return "Only specific projects"; }
			@Override public boolean accept(File file, GhidraFileChooserModel model) { return false; }
		};
		assertTrue(custom.getNativePatterns().isEmpty());
		assertTrue(new ExtensionFileFilter("png", "Custom") {}.getNativePatterns().isEmpty());
	}
}

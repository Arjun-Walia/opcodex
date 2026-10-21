/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme;

import static org.junit.Assert.*;
import org.junit.Test;
import generic.theme.builtin.OpcodexUi;

public class OpcodexUiBootstrapTest {
	@Test public void chromeIsInactiveBeforeDesktopThemeInitialization() {
		ThemeManager previous = ThemeManager.INSTANCE;
		try {
			ThemeManager.INSTANCE = null;
			assertFalse(OpcodexUi.active());
			ThemeManager.INSTANCE = new StubThemeManager();
			assertFalse(OpcodexUi.active());
		}
		finally { ThemeManager.INSTANCE = previous; }
	}
}

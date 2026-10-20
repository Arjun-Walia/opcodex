/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import static org.junit.Assert.*;
import java.awt.Color;
import java.awt.image.MultiResolutionImage;
import javax.swing.ImageIcon;
import org.junit.Test;
import generic.theme.*;

/** Contracts for the real theme objects rather than a duplicate palette fixture. */
public class OpcodexThemeTest {
	@Test
	public void productThemesHaveMatchedLookAndFeelsAndNames() {
		GTheme dark = new OpcodexDarkTheme();
		GTheme light = new OpcodexLightTheme();
		assertEquals("Opcodex Dark", dark.getName());
		assertEquals("Opcodex Light", light.getName());
		assertTrue(dark.useDarkDefaults());
		assertFalse(light.useDarkDefaults());
		assertEquals(LafType.FLAT_DARK, dark.getLookAndFeelType());
		assertEquals(LafType.FLAT_LIGHT, light.getLookAndFeelType());
	}

	@Test
	public void textAndSelectionRemainReadableInBothThemes() {
		for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
			checkContrast(theme, "system.color.fg.view", "system.color.bg.view", 4.5);
			checkContrast(theme, "system.color.fg.control", "system.color.bg.control", 4.5);
			checkContrast(theme, "system.color.fg.selected.view", "system.color.bg.selected.view", 4.5);
			checkContrast(theme, "color.fg.messages.hint", "system.color.bg.view", 4.5);
			checkContrast(theme, "color.border.button.focused", "system.color.bg.control", 3.0);
			checkContrast(theme, "color.fg.decompiler.keyword", "system.color.bg.view", 4.5);
			checkContrast(theme, "color.fg.decompiler.comment", "system.color.bg.view", 4.5);
		}
	}

	@Test
	public void iconSerializationKeepsAReusableResourcePath() throws Exception {
		for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
			IconValue original = theme.getIcon("icon.folder.open");
			String serialized = original.getSerializationString();
			assertFalse(serialized.contains(GTheme.JAVA_ICON));
			String path = serialized.split("=", 2)[1].trim();
			assertTrue(path.startsWith("images/opcodex/ui/"));
			IconValue restored = IconValue.parse("icon.folder.open", path);
			assertEquals(16, restored.getRawValue().getIconWidth());
			assertEquals(16, restored.getRawValue().getIconHeight());
		}
	}

	@Test
	public void iconsCarryRealResolutionVariantsAndStableLogicalDimensions() {
		for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
			ImageIcon icon = (ImageIcon) theme.getIcon("icon.folder.open").getRawValue();
			assertEquals(16, icon.getIconWidth());
			assertEquals(16, icon.getIconHeight());
			MultiResolutionImage image = (MultiResolutionImage) icon.getImage();
			assertEquals(6, image.getResolutionVariants().size());
			assertEquals(32, image.getResolutionVariant(32, 32).getWidth(null));
			assertEquals(64, image.getResolutionVariant(64, 64).getWidth(null));
		}
	}

	private void checkContrast(GTheme theme, String fg, String bg, double minimum) {
		double a = luminance(theme.getColor(fg).getRawValue());
		double b = luminance(theme.getColor(bg).getRawValue());
		double ratio = (Math.max(a, b) + .05) / (Math.min(a, b) + .05);
		assertTrue(theme.getName() + " " + fg + " contrast=" + ratio, ratio >= minimum);
	}

	private double luminance(Color color) {
		return .2126 * linear(color.getRed()) + .7152 * linear(color.getGreen()) +
			.0722 * linear(color.getBlue());
	}

	private double linear(int channel) {
		double value = channel / 255.0;
		return value <= .04045 ? value / 12.92 : Math.pow((value + .055) / 1.055, 2.4);
	}
}

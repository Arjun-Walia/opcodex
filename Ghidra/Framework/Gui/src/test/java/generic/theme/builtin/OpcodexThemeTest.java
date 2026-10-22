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
import javax.swing.UIDefaults;
import javax.swing.LookAndFeel;
import org.junit.Test;
import generic.theme.*;
import generic.theme.laf.*;

/** Contracts for the real theme objects rather than a duplicate palette fixture. */
public class OpcodexThemeTest {
	@Test
	public void transparentIconsRetainTheirLiveSourceAndOpacity() {
		javax.swing.Icon source = new OpcodexImageIcon("error", true);
		javax.swing.Icon transparent = generic.util.image.ImageUtils.makeTransparent(source, .6f);
		assertTrue(transparent instanceof resources.icons.DerivedImageIcon);
		assertSame(source, ((resources.icons.DerivedImageIcon) transparent).getSourceIcon());
		java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(32, 32,
			java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D canvas = image.createGraphics();
		try {
			canvas.scale(2, 2);
			transparent.paintIcon(null, canvas, 0, 0);
		} finally { canvas.dispose(); }
		int maximum = 0;
		for (int x = 0; x < 32; x++) for (int y = 0; y < 32; y++)
			maximum = Math.max(maximum, image.getRGB(x, y) >>> 24);
		assertEquals(153, maximum);
	}

	@Test
	public void darkCanvasIsBlackAndBothThemesDefineTheSameRoles() {
		GTheme dark = new OpcodexDarkTheme();
		GTheme light = new OpcodexLightTheme();
		assertEquals(Color.BLACK, dark.getColor("system.color.bg.view").getRawValue());
		assertEquals(dark.getColors().stream().map(ColorValue::getId).collect(java.util.stream.Collectors.toSet()),
			light.getColors().stream().map(ColorValue::getId).collect(java.util.stream.Collectors.toSet()));
		assertEquals(dark.getFonts().stream().map(FontValue::getId).collect(java.util.stream.Collectors.toSet()),
			light.getFonts().stream().map(FontValue::getId).collect(java.util.stream.Collectors.toSet()));
		assertEquals(dark.getIcons().stream().map(IconValue::getId).collect(java.util.stream.Collectors.toSet()),
			light.getIcons().stream().map(IconValue::getId).collect(java.util.stream.Collectors.toSet()));
	}
	@Test
	public void sizedAndDisabledIconsKeepLiveVectorPaintingAtDisplayScale() {
		final double[] observed = new double[2];
		javax.swing.Icon vector = new javax.swing.Icon() {
			public int getIconWidth() { return 16; }
			public int getIconHeight() { return 16; }
			public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
				java.awt.Graphics2D canvas = (java.awt.Graphics2D) g;
				observed[0] = canvas.getTransform().getScaleX();
				observed[1] = ((java.awt.AlphaComposite) canvas.getComposite()).getAlpha();
			}
		};
		for (double scale : new double[] {1, 1.25, 1.5, 2}) {
			java.awt.Graphics2D g = new java.awt.image.BufferedImage(64, 64,
				java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
			try {
				g.scale(scale, scale);
				OpcodexUi.disabled(OpcodexUi.sized(vector, 20)).paintIcon(null, g, 0, 0);
				assertEquals(scale * 20 / 16, observed[0], .00001);
				assertEquals(.38, observed[1], .00001);
			} finally { g.dispose(); }
		}
	}

	@Test
	public void everyOriginalGlyphPaintsDirectlyAtFractionalScale() {
		GTheme theme = new OpcodexDarkTheme();
		for (IconValue entry : theme.getIcons()) {
			if (!entry.getId().startsWith("icon.opcodex.glyph.")) continue;
			java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(40, 40,
				java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics2D g = image.createGraphics();
			try {
				g.scale(1.5, 1.5);
				entry.get(theme).paintIcon(null, g, 0, 0);
			} finally { g.dispose(); }
			boolean painted = false;
			for (int x = 0; x < 40; x++) for (int y = 0; y < 40; y++) {
				painted |= (image.getRGB(x, y) >>> 24) != 0;
			}
			assertTrue("Empty glyph: " + entry.getId(), painted);
		}
	}
	@Test
	public void composedStatusesKeepTheirSizeAndEveryBindingResolves() {
		for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
			for (IconValue entry : theme.getIcons()) {
				javax.swing.Icon icon = entry.get(theme);
				assertNotNull(entry.getId(), icon);
				assertNotSame("Unresolved icon: " + entry.getId(), IconValue.LAST_RESORT_DEFAULT, icon);
			}
			assertEquals(20, theme.getIcon("icon.empty.20").get(theme).getIconWidth());
			assertEquals(8, theme.getIcon("icon.widget.tabs.empty.small").get(theme).getIconWidth());
			assertEquals(16, theme.getIcon("icon.widget.tabs.empty.small").get(theme).getIconHeight());
			assertNotEquals(theme.getIcon("icon.debugger.breakpoint.enable").getSerializationString(),
				theme.getIcon("icon.debugger.breakpoint.disable").getSerializationString());
		}
	}
	@Test
	public void legacyImageIconScalingKeepsDeviceScaleAndVariants() {
		javax.swing.ImageIcon source = new OpcodexImageIcon("branch", true);
		javax.swing.ImageIcon scaled = resources.ResourceManager.getScaledIcon(source, 24, 24);
		assertTrue(OpcodexUi.isVector(scaled));
		assertEquals(24, scaled.getIconWidth());
		MultiResolutionImage image = (MultiResolutionImage) scaled.getImage();
		assertEquals(48, image.getResolutionVariant(48, 48).getWidth(null));
		assertEquals(96, image.getResolutionVariant(96, 96).getWidth(null));
	}
	@Test
	public void chromeUsesItsSurfaceTokenThroughRealFlatLafNormalization() throws Exception {
		for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
			String laf = theme.useDarkDefaults() ? "FlatDarkLaf" : "FlatLightLaf";
			LookAndFeel lookAndFeel = (LookAndFeel) Class.forName("com.formdev.flatlaf." + laf)
				.getConstructor().newInstance();
			UIDefaults defaults = lookAndFeel.getDefaults();
			GThemeValueMap values = (theme.useDarkDefaults() ? new DarkMapper(defaults) :
				new LightMapper(defaults)).getNormalizedJavaDefaults();
			values.load(theme);
			for (String key : new String[] {"Panel.background", "ToolBar.background",
					"MenuBar.background", "TitlePane.background", "TabbedPane.background"}) {
				assertNotNull("Real FlatLaf key: " + key, defaults.getColor(key));
				assertEquals(theme.getName() + " " + key,
					values.getResolvedColor("system.color.bg.control"),
					values.getResolvedColor("laf.color." + key));
			}
		}
	}

    @Test public void selectionsStayReadableThroughRealFlatLafNormalization() throws Exception {
        for (GTheme theme : new GTheme[] {new OpcodexDarkTheme(), new OpcodexLightTheme()}) {
            LookAndFeel laf = (LookAndFeel) Class.forName("com.formdev.flatlaf." +
                (theme.useDarkDefaults() ? "FlatDarkLaf" : "FlatLightLaf")).getConstructor().newInstance();
            UIDefaults defaults = laf.getDefaults();
            GThemeValueMap values = (theme.useDarkDefaults() ? new DarkMapper(defaults) : new LightMapper(defaults)).getNormalizedJavaDefaults();
            values.load(theme);
            for (String key : new String[] {"List", "Table", "Tree", "TextField", "TextArea", "TextPane",
                "EditorPane", "ComboBox", "Menu", "MenuItem", "CheckBoxMenuItem", "RadioButtonMenuItem", "MenuBar"}) {
                Color fg = values.getResolvedColor("laf.color." + key + ".selectionForeground");
                Color bg = values.getResolvedColor("laf.color." + key + ".selectionBackground");
                assertNotNull(key, fg); assertNotNull(key, bg);
                assertTrue(theme.getName() + " " + key + " contrast", contrast(fg, bg) >= 4.5);
            }
        }
    }
    private static double contrast(Color first, Color second) {
        double a = selectionLuminance(first), b = selectionLuminance(second);
        return (Math.max(a, b) + .05) / (Math.min(a, b) + .05);
    }
    private static double selectionLuminance(Color color) {
        double[] parts = {color.getRed()/255d, color.getGreen()/255d, color.getBlue()/255d};
        for (int i = 0; i < 3; i++) parts[i] = parts[i] <= .04045 ? parts[i]/12.92 : Math.pow((parts[i]+.055)/1.055, 2.4);
        return .2126*parts[0] + .7152*parts[1] + .0722*parts[2];
    }

	private static class DarkMapper extends FlatDarkUiDefaultsMapper {
		DarkMapper(UIDefaults defaults) { super(defaults); }
	}

	private static class LightMapper extends FlatUiDefaultsMapper {
		LightMapper(UIDefaults defaults) { super(defaults); }
	}

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
			IconValue original = theme.getIcon("icon.opcodex.glyph.folder-open");
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
			ImageIcon icon = (ImageIcon) theme.getIcon("icon.folder.open").get(theme);
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

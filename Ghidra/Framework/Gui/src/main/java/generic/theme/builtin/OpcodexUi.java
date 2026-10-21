/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import generic.theme.ThemeManager;
import generic.theme.ApplicationThemeManager;

/** Product chrome helpers; keep icon painting vector-based even for disabled controls. */
public final class OpcodexUi {
	/** Bundled resource callers keep their dimensions, animation/custom images and other themes. */
	public static ImageIcon resourceIcon(String path, ImageIcon legacy) {
		if (!(legacy instanceof resources.icons.UrlImageIcon url) || legacy instanceof OpcodexResourceIcon ||
			path == null || legacy instanceof resources.icons.UnresolvedIcon ||
			path.contains(":") || path.contains("..") || path.startsWith("/")) return legacy;
		String name = path;
		if (name.startsWith("images/")) name = name.substring(7);
		else if (name.startsWith("defaultTools/images/")) name = name.substring(20);
		// A supplied file or unrelated resource directory is not a built-in icon.
		if (name.equals("opcodex/tool-24.png")) return new OpcodexResourceIcon(url, "code");
		if (name.contains("/") || name.contains("\\")) return legacy;
		String glyph = OpcodexIconResources.GLYPHS.get(name);
		return glyph == null ? legacy : new OpcodexResourceIcon(url, glyph);
	}
	/** Recognize vector sources through the standard theme and derived-icon wrappers. */
	public static boolean isVector(Icon icon) {
		if (icon instanceof OpcodexImageIcon || icon instanceof OpcodexResourceIcon ||
			icon instanceof OpcodexPaintedImageIcon) return true;
		if (icon instanceof generic.theme.GIcon themed) return isVector(themed.getDelegate());
		if (icon instanceof resources.icons.DerivedImageIcon derived) return isVector(derived.getSourceIcon());
		if (icon instanceof resources.MultiIcon multi)
			return java.util.Arrays.stream(multi.getIcons()).anyMatch(OpcodexUi::isVector);
		if (icon instanceof resources.icons.TranslateIcon moved) return isVector(moved.getBaseIcon());
		if (icon instanceof resources.icons.RotateIcon rotated) return isVector(rotated.getSourceIcon());
		return false;
	}
	/** ImageIcon-compatible transform; draw geometry at the device scale, never enlarge a bitmap. */
	public static ImageIcon imageIcon(Icon icon, int width, int height, float alpha) {
		return new OpcodexPaintedImageIcon(icon, width, height, alpha);
	}
	private OpcodexUi() {}
	public static boolean active() {
		// Bootstrap/headless managers deliberately do not implement desktop theme access.
		return ThemeManager.getInstance() instanceof ApplicationThemeManager manager &&
			manager.getActiveTheme() instanceof OpcodexTheme;
	}
	public static Icon sized(Icon icon, int height) {
		return icon == null ? null : new PaintedIcon(icon, height, 1f);
	}
	public static Icon disabled(Icon icon) {
		return icon == null ? null : new PaintedIcon(icon, icon.getIconHeight(), .38f);
	}
	/** Remove raised frames in product themes while retaining other themes' original border. */
	public static Border quietBorder(Border legacy, Insets padding) {
		return new AbstractBorder() {
			@Override public Insets getBorderInsets(Component c, Insets target) {
				Insets source = active() ? padding : legacy.getBorderInsets(c);
				target.set(source.top, source.left, source.bottom, source.right);
				return target;
			}
			@Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (!active()) legacy.paintBorder(c, g, x, y, width, height);
			}
		};
	}
	/** Flat graph/card boundaries that follow theme changes without changing their contents. */
	public static Border flatBorder(Border legacy) {
		return new AbstractBorder() {
			@Override public Insets getBorderInsets(Component c, Insets target) {
				Insets source = active() ? new Insets(1, 1, 1, 1) : legacy.getBorderInsets(c);
				target.set(source.top, source.left, source.bottom, source.right);
				return target;
			}
			@Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if (!active()) { legacy.paintBorder(c, g, x, y, width, height); return; }
				g.setColor(new generic.theme.GColor("system.color.bg.border"));
				g.drawRect(x, y, Math.max(0, width - 1), Math.max(0, height - 1));
			}
		};
	}
	private record PaintedIcon(Icon source, int height, float alpha) implements Icon {
		@Override public int getIconHeight() { return height; }
		@Override public int getIconWidth() {
			return Math.round(source.getIconWidth() * height / (float) Math.max(1, source.getIconHeight()));
		}
		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D canvas = (Graphics2D) g.create();
			try {
				canvas.translate(x, y);
				double scale = height / (double) Math.max(1, source.getIconHeight());
				canvas.scale(scale, scale);
				float inherited = canvas.getComposite() instanceof AlphaComposite composite
					? composite.getAlpha() : 1f;
				canvas.setComposite(AlphaComposite.SrcOver.derive(alpha * inherited));
				source.paintIcon(c, canvas, 0, 0);
			} finally { canvas.dispose(); }
		}
	}
}

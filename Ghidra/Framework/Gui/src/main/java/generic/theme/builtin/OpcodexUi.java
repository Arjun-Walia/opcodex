/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.*;
import javax.swing.Icon;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import generic.theme.ThemeManager;

/** Product chrome helpers; keep icon painting vector-based even for disabled controls. */
public final class OpcodexUi {
	private OpcodexUi() {}
	public static boolean active() {
		return ThemeManager.getInstance().getActiveTheme() instanceof OpcodexTheme;
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

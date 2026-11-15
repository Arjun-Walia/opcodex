/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.*;
import javax.swing.*;
import generic.theme.ThemeManager;
import resources.icons.UrlImageIcon;

/** Lazy compatibility adapter for known built-in filenames, including saved tool icons. */
final class OpcodexResourceIcon extends UrlImageIcon {
	private final ImageIcon legacy;
	private final String glyph;
	private Object cachedTheme;
	private Image cachedImage;
    private Icon cachedPainter;
    private Object painterTheme;
	OpcodexResourceIcon(UrlImageIcon legacy, String glyph) {
		super(legacy.getOriginalPath(), legacy.getUrl());
		this.legacy = legacy; this.glyph = glyph;
	}
	@Override public int getIconWidth() { return legacy.getIconWidth(); }
	@Override public int getIconHeight() { return legacy.getIconHeight(); }
	@Override public String getDescription() { return legacy.getDescription(); }
	@Override public int getImageLoadStatus() { return legacy.getImageLoadStatus(); }
	@Override public synchronized void paintIcon(Component c, Graphics graphics, int x, int y) {
		if (!OpcodexUi.active()) { legacy.paintIcon(c, graphics, x, y); return; }
		Object theme = ThemeManager.getInstance().getActiveTheme();
        if (theme != painterTheme) {
            boolean dark = ThemeManager.getInstance().getActiveTheme().useDarkDefaults();
            cachedPainter = OpcodexUi.sized(new OpcodexImageIcon(glyph, dark), getIconHeight());
            painterTheme = theme;
        }
        Graphics2D canvas = (Graphics2D) graphics.create();
        try {
            canvas.translate(x, y);
            canvas.scale(getIconWidth() / (double) cachedPainter.getIconWidth(), 1);
            cachedPainter.paintIcon(c, canvas, 0, 0);
        } finally { canvas.dispose(); }
	}
	@Override public synchronized Image getImage() {
		if (!OpcodexUi.active()) return legacy.getImage();
		Object theme = ThemeManager.getInstance().getActiveTheme();
		if (theme != cachedTheme) { cachedImage = OpcodexPaintedImageIcon.variants(this); cachedTheme = theme; }
		return cachedImage;
	}
}

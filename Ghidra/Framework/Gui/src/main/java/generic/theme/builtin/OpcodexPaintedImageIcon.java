/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import resources.icons.DerivedImageIcon;

/** Live rendering for APIs which require ImageIcon; image consumers receive real HiDPI variants. */
final class OpcodexPaintedImageIcon extends DerivedImageIcon {
	private final int width, height;
	private final float alpha;
	OpcodexPaintedImageIcon(Icon source, int width, int height, float alpha) {
		super(source);
		this.width = width; this.height = height; this.alpha = alpha;
	}
	@Override public int getIconWidth() { return width; }
	@Override public int getIconHeight() { return height; }
	@Override public synchronized void paintIcon(Component c, Graphics graphics, int x, int y) {
		Graphics2D g = (Graphics2D) graphics.create();
		try {
			g.translate(x, y);
			Icon source = getSourceIcon();
			g.scale(width / (double) Math.max(1, source.getIconWidth()),
				height / (double) Math.max(1, source.getIconHeight()));
			float inherited = g.getComposite() instanceof AlphaComposite a ? a.getAlpha() : 1;
			g.setComposite(AlphaComposite.SrcOver.derive(inherited * alpha));
			source.paintIcon(c, g, 0, 0);
		} finally { g.dispose(); }
	}
	@Override protected Image createImage() {
		return variants(this);
	}
	static Image variants(Icon icon) {
		double[] scales = {1, 1.25, 1.5, 2, 3, 4};
		Image[] images = new Image[scales.length];
		for (int i = 0; i < scales.length; i++) {
			double scale = scales[i];
			BufferedImage image = new BufferedImage(Math.max(1, (int) Math.ceil(icon.getIconWidth() * scale)),
				Math.max(1, (int) Math.ceil(icon.getIconHeight() * scale)), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			try { g.scale(scale, scale); icon.paintIcon(null, g, 0, 0); }
			finally { g.dispose(); }
			images[i] = image;
		}
		return new BaseMultiResolutionImage(images);
	}
}

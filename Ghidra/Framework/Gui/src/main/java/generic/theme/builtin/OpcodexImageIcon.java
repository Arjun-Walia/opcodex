/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.*;
import java.awt.image.BaseMultiResolutionImage;
import resources.ResourceManager;
import resources.icons.UrlImageIcon;

/** Lazy product icon that retains a resource path for user-theme export. */
final class OpcodexImageIcon extends UrlImageIcon {
	private final String glyph;
	private final boolean dark;

	OpcodexImageIcon(String glyph, boolean dark) {
		super(path(glyph, dark, 16), ResourceManager.getResource(path(glyph, dark, 16)));
		this.glyph = glyph;
		this.dark = dark;
	}

	@Override
	public synchronized void paintIcon(Component component, Graphics graphics, int x, int y) {
		Graphics2D canvas = (Graphics2D) graphics.create();
		try {
			canvas.translate(x, y);
			canvas.scale(16d / 24d, 16d / 24d);
			canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			canvas.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			String ink = dark ? "#c8cdd3" : "#4b5563";
			if (glyph.equals("warning")) ink = dark ? "#edbd6c" : "#8a5a0c";
			if (glyph.equals("error")) ink = dark ? "#f18b91" : "#b64248";
			if (glyph.equals("check")) ink = dark ? "#89d7a6" : "#216a43";
			canvas.setColor(Color.decode(ink));
			for (OpcodexVectorPaths.Part part : OpcodexVectorPaths.get(glyph)) {
				canvas.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
					10, part.dash(), 0));
				if (part.fill()) canvas.fill(part.shape());
				if (part.stroke()) canvas.draw(part.shape());
			}
		} finally { canvas.dispose(); }
	}

	@Override
	protected Image createImage() {
		int[] sizes = { 16, 20, 24, 32, 48, 64 };
		Image[] variants = new Image[sizes.length];
		for (int i = 0; i < sizes.length; i++) {
			variants[i] = ResourceManager.loadImage(path(glyph, dark, sizes[i])).getImage();
		}
		return new BaseMultiResolutionImage(variants);
	}

	private static String path(String glyph, boolean dark, int size) {
		return "images/opcodex/ui/" + (dark ? "dark/" : "light/") +
			glyph + "-" + size + ".png";
	}
}

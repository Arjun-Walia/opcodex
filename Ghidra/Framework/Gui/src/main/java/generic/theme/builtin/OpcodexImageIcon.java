/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.awt.Image;
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

/*
 * SPDX-License-Identifier: MIT
 * Filename rules adapted from Better File Icons 0.6.1.
 * Copyright (c) 2026 Opcodex contributors
 *
 * Copyright (c) 2012-2024 Luke Davis and others
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ghidra.framework.data;

import java.awt.Component;
import java.awt.Graphics;
import java.util.Locale;

import javax.swing.Icon;

import generic.theme.Gui;
import generic.theme.builtin.OpcodexUi;

/** Matches project filenames to Opcodex glyphs while keeping the content handler fallback. */
final class OpcodexProjectFileIcons {
	private OpcodexProjectFileIcons() {
	}

	static Icon decorate(String name, Icon contentIcon) {
		String glyph = glyphForName(name);
		if (glyph == null) {
			return contentIcon;
		}
		return new ProjectFileIcon("icon.opcodex.glyph." + glyph, contentIcon);
	}

	static String glyphForName(String name) {
		String lower = name.toLowerCase(Locale.ROOT);
		String exact = switch (lower) {
			case "makefile", "cmakelists.txt" -> "settings";
			case "package.json" -> "braces";
			case "license" -> "book";
			case ".gitattributes" -> "branch";
			default -> null;
		};
		if (exact != null) {
			return exact;
		}

		int dot = lower.lastIndexOf('.');
		if (dot <= 0) {
			return null;
		}
		return switch (lower.substring(dot + 1)) {
			case "c", "cc", "cpp", "cxx", "h", "hpp", "py", "go", "rs", "rb", "php", "swift", "kt", "cs" -> "code";
			case "asm", "s", "nasm" -> "instruction";
			case "exe" -> "cpu";
			case "dll", "so", "dylib" -> "library";
			case "bin", "elf", "obj", "o", "wasm" -> "binary";
			case "pdb" -> "memory";
			case "class", "java", "js", "ts", "json", "xml" -> "braces";
			case "jar", "zip", "gz", "tgz", "xz", "7z", "rar", "tar" -> "archive";
			case "txt", "log" -> "text";
			case "md" -> "book";
			case "yaml", "yml", "toml", "ini", "cfg" -> "settings";
			case "sh", "ps1", "bat" -> "terminal";
			case "map" -> "graph";
			case "diff", "patch" -> "compare";
			// Opcodex project objects and common reverse-engineering inputs.
			case "sys" -> "cpu";
			case "lib", "a" -> "library";
			case "dex" -> "binary";
			case "hex" -> "hex";
			case "gdt" -> "book";
			case "gzf" -> "archive";
			default -> null;
		};
	}

	private record ProjectFileIcon(String iconId, Icon contentIcon) implements Icon {
		@Override
		public int getIconWidth() {
			return contentIcon.getIconWidth();
		}

		@Override
		public int getIconHeight() {
			return contentIcon.getIconHeight();
		}

		@Override
		public void paintIcon(Component component, Graphics graphics, int x, int y) {
			if (!OpcodexUi.active()) {
				contentIcon.paintIcon(component, graphics, x, y);
				return;
			}

			Icon glyph = Gui.getIcon(iconId);
			int left = x + Math.max(0, (getIconWidth() - glyph.getIconWidth()) / 2);
			int top = y + Math.max(0, (getIconHeight() - glyph.getIconHeight()) / 2);
			glyph.paintIcon(component, graphics, left, top);
		}
	}
}

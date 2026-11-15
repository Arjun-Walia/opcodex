/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.theme.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Map;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import generic.theme.GTheme;
import generic.theme.LafType;
import generic.theme.builtin.OpcodexUi;

/** Reads a standalone VS Code color palette and keeps the Opcodex workspace and icon theme. */
final class VsCodePalette {
	private static final int MAX_BYTES = 1024 * 1024;
	private static final Map<String, String[]> COLORS = Map.ofEntries(
		Map.entry("editor.background", new String[] { "system.color.bg.view" }),
		Map.entry("editor.foreground", new String[] { "system.color.fg.view" }),
		Map.entry("sideBar.background", new String[] { "system.color.bg.control" }),
		Map.entry("sideBar.foreground", new String[] { "system.color.fg.control" }),
		Map.entry("panel.border", new String[] { "system.color.bg.border", "color.bg.table.grid" }),
		Map.entry("editorWidget.background", new String[] { "system.color.bg.tooltip" }),
		Map.entry("editorWidget.foreground", new String[] { "system.color.fg.tooltip" }),
		Map.entry("list.activeSelectionBackground", new String[] { "system.color.bg.selected.view",
			"color.bg.selection", "color.bg.header.active", "color.bg.widget.tabs.selected.active" }),
		Map.entry("list.activeSelectionForeground", new String[] { "system.color.fg.selected.view",
			"color.fg.header.active", "color.fg.widget.tabs.selected.active" }),
		Map.entry("focusBorder", new String[] { "color.border.button.focused",
			"laf.color.Component.focusColor", "laf.color.Component.accentColor",
			"laf.color.Button.focusedBorderColor", "laf.color.TextComponent.focusedBorderColor" }),
		Map.entry("editorCursor.foreground", new String[] { "color.cursor.focused" }));

	private VsCodePalette() {
	}

	static boolean accepts(File file) {
		String name = file.getName().toLowerCase(Locale.ROOT);
		return name.endsWith(".json") || name.endsWith(".jsonc");
	}

	static GTheme read(File file) throws IOException {
		byte[] bytes;
		try (var stream = Files.newInputStream(file.toPath())) {
			bytes = stream.readNBytes(MAX_BYTES + 1);
		}
		if (bytes.length > MAX_BYTES) {
			throw new IOException("The VS Code theme file exceeds 1 MiB.");
		}

		JsonObject root = parse(bytes);
		JsonElement element = root.get("colors");
		if (element == null || !element.isJsonObject()) {
			throw new IOException("Choose a VS Code theme JSON file with a colors object.");
		}
		JsonObject colors = element.getAsJsonObject();
		String name = file.getName().replaceFirst("(?i)\\.jsonc?$", "");
		name = name.replaceAll("[^\\p{L}\\p{N} ._-]", " ").strip();
		if (name.length() > 80) {
			name = name.substring(0, 80).strip();
		}

		LafType laf = isDark(root, colors) ? LafType.FLAT_DARK : LafType.FLAT_LIGHT;
		GTheme result = new GTheme("VS Code " + (name.isEmpty() ? "Palette" : name), laf);
		COLORS.forEach((key, ids) -> {
			Color value = opaqueColor(colors.get(key));
			if (value != null) {
				for (String id : ids) {
					result.setColor(id, value);
				}
			}
		});
		if (result.getColors().isEmpty()) {
			throw new IOException("The VS Code theme contains no supported opaque colors.");
		}
		result.setColor(OpcodexUi.IMPORTED_PALETTE, Color.BLACK);
		return result;
	}

	private static JsonObject parse(byte[] bytes) throws IOException {
		var reader = new JsonReader(new StringReader(new String(bytes, StandardCharsets.UTF_8)));
		reader.setStrictness(Strictness.LENIENT);
		try {
			JsonElement element = JsonParser.parseReader(reader);
			if (element.isJsonObject() && reader.peek() == JsonToken.END_DOCUMENT) {
				return element.getAsJsonObject();
			}
		}
		catch (JsonParseException e) {
			throw new IOException("The VS Code theme file could not be parsed.", e);
		}
		throw new IOException("The VS Code theme must contain one JSON object.");
	}

	private static boolean isDark(JsonObject root, JsonObject colors) {
		JsonElement type = root.get("type");
		if (type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()) {
			switch (type.getAsString()) {
				case "light", "hcLight" -> { return false; }
				case "dark", "hcDark" -> { return true; }
			}
		}
		Color bg = opaqueColor(colors.get("editor.background"));
		return bg == null || 299 * bg.getRed() + 587 * bg.getGreen() + 114 * bg.getBlue() < 128000;
	}

	private static Color opaqueColor(JsonElement value) {
		if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
			return null;
		}
		String hex = value.getAsString();
		if (hex.matches("#[0-9a-fA-F]{3}")) {
			hex = "#" + hex.substring(1, 2).repeat(2) + hex.substring(2, 3).repeat(2) +
				hex.substring(3).repeat(2);
		}
		else if (hex.matches("#[0-9a-fA-F]{6}[fF]{2}")) {
			hex = hex.substring(0, 7);
		}
		return hex.matches("#[0-9a-fA-F]{6}") ? Color.decode(hex) : null;
	}
}

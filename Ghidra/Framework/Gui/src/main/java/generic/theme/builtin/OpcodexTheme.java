/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package generic.theme.builtin;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import javax.swing.plaf.InsetsUIResource;
import generic.theme.*;

/** Original Opcodex workbench palette; analysis and data formats are unchanged. */
public abstract class OpcodexTheme extends DiscoverableGTheme {
	protected OpcodexTheme(String name, LafType laf, boolean dark) {
		super(name, laf);
		String canvas = dark ? "#181a1d" : "#fbfcfd";
		String surface = dark ? "#202327" : "#f1f3f5";
		String text = dark ? "#e8eaed" : "#252930";
		String secondary = dark ? "#a7aeb8" : "#5d6672";
		String border = dark ? "#454c56" : "#a6afba";
		String accent = dark ? "#75d9de" : "#087582";
		String selection = dark ? "#273e43" : "#d7edf0";
		color("system.color.bg.control", surface);
		color("system.color.bg.view", canvas);
		color("system.color.bg.tooltip", surface);
		color("system.color.bg.selected.view", selection);
		color("system.color.bg.border", border);
		for (String id : new String[] {"system.color.fg.control", "system.color.fg.view",
				"system.color.fg.tooltip", "system.color.fg.selected.view"}) {
			color(id, text);
		}
		color("system.color.fg.disabled", secondary);
		color("color.fg.disabled", secondary);
		color("color.fg.messages.hint", secondary);
		// FlatLaf's normalized palette can associate a panel with the border group when
		// its original RGB matches. Bind chrome by role instead of inheriting that match.
		refs("system.color.bg.control", "Panel.background", "Label.background",
			"ToolBar.background", "ToolBar.floatingBackground", "SplitPane.background",
			"TabbedPane.background", "ScrollBar.background", "MenuBar.background",
			"Menu.background", "MenuItem.background", "PopupMenu.background",
			"TitlePane.background", "TitlePane.inactiveBackground", "TableHeader.background");
		refs("system.color.bg.border", "Component.borderColor", "Component.disabledBorderColor",
			"ToolBar.separatorColor", "MenuBar.borderColor", "PopupMenu.borderColor",
			"TableHeader.separatorColor", "TableHeader.bottomSeparatorColor",
			"TabbedPane.contentAreaColor", "Button.borderColor", "Button.disabledBorderColor");
		refs("system.color.bg.selected.view", "Button.hoverBackground",
			"Button.pressedBackground", "Button.selectedBackground", "Button.default.background",
			"Button.default.hoverBackground", "Button.default.pressedBackground",
			"Button.toolbar.hoverBackground", "Button.toolbar.pressedBackground",
			"Button.toolbar.selectedBackground", "MenuBar.hoverBackground",
			"Table.selectionInactiveBackground", "Tree.selectionInactiveBackground");
		color("color.bg.table.row.alt", dark ? "#1e2125" : "#f1f4f6");
		color("color.bg.table.grid", dark ? "#30363e" : "#dce1e6");
		color("color.bg.header.active", selection);
		color("color.bg.header.inactive", surface);
		color("color.fg.header.active", text);
		color("color.fg.header.inactive", secondary);
		color("color.bg.widget.tabs.selected.active", selection);
		color("color.bg.widget.tabs.selected.inactive", surface);
		color("color.fg.widget.tabs.selected.active", text);
		color("color.fg.widget.tabs.selected.inactive", text);
		color("color.fg.widget.tabs.unselected", secondary);
		color("color.border.button.focused", accent);
		color("laf.color.Component.focusColor", accent);
		color("laf.color.Component.accentColor", accent);
		color("laf.color.Button.focusedBorderColor", accent);
		color("laf.color.TextComponent.focusedBorderColor", accent);
		color("color.cursor.focused", accent);
		color("color.cursor.unfocused", secondary);
		color("color.fg.button", text);
		color("color.fg.error", dark ? "#f18b91" : "#b64248");
		color("color.fg.messages.warning", dark ? "#edbd6c" : "#8a5a0c");
		color("color.bg.filterfield", dark ? "#3b3527" : "#fff0d5");
		color("color.fg.filterfield", text);
		color("color.bg.find.highlight", dark ? "#574b28" : "#ffedb3");
		color("color.bg.find.highlight.active", dark ? "#75602a" : "#ffd775");
		color("color.bg.selection", selection);
		color("color.bg.fieldpanel.selection", selection);
		color("color.bg.currentline", dark ? "#202d31" : "#edf5f6");
		color("color.bg.listing.highlighter", dark ? "#303a40" : "#e7eff1");
		String keyword = dark ? "#8bbcec" : "#245b9c";
		String comment = dark ? "#a1a8b3" : "#606a76";
		String type = dark ? "#d2b8ee" : "#714796";
		String constant = dark ? "#eac286" : "#7a5313";
		String variable = dark ? "#9ccea5" : "#28633d";
		for (String id : new String[] {"color.fg.listing.mnemonic", "color.fg.listing.function.name",
				"color.fg.listing.label.primary", "color.fg.decompiler.keyword"}) {
			color(id, keyword);
		}
		for (String suffix : new String[] {"auto", "eol", "repeatable", "ref.repeatable",
				"plate", "post", "pre"}) {
			color("color.fg.listing.comment." + suffix, comment);
		}
		color("color.fg.decompiler.comment", comment);
		color("color.fg.decompiler.type", type);
		color("color.fg.decompiler.parameter", type);
		color("color.fg.decompiler.variable", variable);
		color("color.fg.decompiler.constant", constant);
		color("color.fg.decompiler.global", accent);
		color("color.fg.decompiler.external.function", type);
		color("color.fg.decompiler.special", keyword);
		color("color.fg.decompiler.error", dark ? "#f18b91" : "#b64248");
		color("color.bg.decompiler.current.variable", dark ? "#3b3527" : "#fff0d5");
		color("color.fg.listing.ext.entrypoint", keyword);
		for (String suffix : new String[] {"function.return.type", "function.variable",
				"function.variable.assigned", "function.tag"}) {
			color("color.fg.listing." + suffix, type);
		}
		for (String suffix : new String[] {"function.param", "function.param.dynamic",
				"register", "xref", "xref.read", "xref.write"}) {
			color("color.fg.listing." + suffix, variable);
		}
		color("color.fg.listing.function.param.auto", comment);
		color("color.fg.listing.constant", constant);
		color("color.fg.listing.bytes", secondary);
		Font ui = new Font(Font.DIALOG, Font.PLAIN, 13);
		for (String id : new String[] {"system.font.control", "system.font.view",
				"system.font.menu"}) {
			setFont(id, ui);
		}
		setFont("font.monospaced", new Font(Font.MONOSPACED, Font.PLAIN, 13));
		addProperty(new BooleanPropertyValue("laf.property.Table.showHorizontalLines", false));
		addProperty(new BooleanPropertyValue("laf.property.Table.showVerticalLines", false));
		OpcodexIcons.install(this, dark);
	}

	private void color(String id, String hex) {
		setColor(id, Color.decode(hex));
	}

	private void refs(String token, String... keys) {
		for (String key : keys) {
			setColorRef("laf.color." + key, token);
		}
	}

	/** Called after FlatLaf installation and before component UIs update. */
	public static void installComponentDefaults() {
		UIManager.put("Component.arc", 8);
		UIManager.put("Button.arc", 8);
		UIManager.put("TextComponent.arc", 8);
		UIManager.put("Component.focusWidth", 1);
		UIManager.put("Component.innerFocusWidth", 0);
		UIManager.put("Button.minimumHeight", 28);
		UIManager.put("Button.margin", new InsetsUIResource(5, 10, 5, 10));
		UIManager.put("Button.toolbar.spacingInsets", new InsetsUIResource(4, 4, 4, 4));
		UIManager.put("Button.toolbar.margin", new InsetsUIResource(4, 4, 4, 4));
		UIManager.put("Tree.rowHeight", 24);
		UIManager.put("Table.rowHeight", 24);
		UIManager.put("List.cellMargins", new InsetsUIResource(3, 6, 3, 6));
		UIManager.put("ScrollBar.width", 12);
		UIManager.put("ScrollBar.showButtons", false);
		UIManager.put("TabbedPane.tabHeight", 32);
		UIManager.put("MenuItem.minimumWidth", 190);
		UIManager.put("MenuItem.margin", new InsetsUIResource(5, 8, 5, 8));
	}
}

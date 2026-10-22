/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package generic.theme.builtin;

import java.util.*;
import javax.swing.*;
import generic.theme.GTheme;
import generic.theme.IconValue;

/** Generated from ui-assets/icons/catalog.json; 16px logical size, 1x through 4x variants. */
final class OpcodexIcons {
	private static final String[][] ENTRIES = {
		{"icon.add", "plus"},
		{"icon.base.application.home", "home"},
		{"icon.base.edit.bytes", "edit"},
		{"icon.base.hover.off", "info"},
		{"icon.base.hover.on", "info"},
		{"icon.base.lock", "lock"},
		{"icon.base.pinned", "pin"},
		{"icon.base.search.marker", "search"},
		{"icon.base.unlock", "unlock"},
		{"icon.check", "check"},
		{"icon.checkmark.green", "check"},
		{"icon.clear", "close"},
		{"icon.collapse.all", "collapse"},
		{"icon.configure.filter", "settings"},
		{"icon.console", "terminal"},
		{"icon.copy", "copy"},
		{"icon.cut", "cut"},
		{"icon.decompiler.action.export", "export"},
		{"icon.decompiler.action.provider", "decompile"},
		{"icon.decompiler.action.provider.readonly", "lock"},
		{"icon.decompiler.action.provider.unreachable", "filter-off"},
		{"icon.delete", "trash"},
		{"icon.docking.application.home", "home"},
		{"icon.domain.file.uknown", "file"},
		{"icon.down", "down"},
		{"icon.drive", "drive"},
		{"icon.error", "error"},
		{"icon.expand.all", "expand"},
		{"icon.extension.configure", "settings"},
		{"icon.filter.options.contains", "filter"},
		{"icon.flag", "flag"},
		{"icon.folder.closed", "folder"},
		{"icon.folder.new", "folder-add"},
		{"icon.folder.open", "folder-open"},
		{"icon.help", "help"},
		{"icon.home", "home"},
		{"icon.information", "info"},
		{"icon.left", "left"},
		{"icon.left.alt", "left"},
		{"icon.lock", "lock"},
		{"icon.logviewer.toolbar.scroll.lock", "lock"},
		{"icon.make.selection", "selection"},
		{"icon.navigate.in", "import"},
		{"icon.navigate.out", "export"},
		{"icon.not.allowed", "error"},
		{"icon.paste", "paste"},
		{"icon.plugin.bookmark.type.analysis", "wand"},
		{"icon.plugin.bookmark.type.note", "bookmark"},
		{"icon.plugin.calltree.function", "function"},
		{"icon.plugin.calltree.node.external", "export"},
		{"icon.plugin.codebrowser.cursor.location", "right"},
		{"icon.plugin.codebrowser.cursor.marker", "search"},
		{"icon.plugin.codebrowser.format.collapse", "up"},
		{"icon.plugin.codebrowser.format.expand", "down"},
		{"icon.plugin.codebrowser.provider", "listing"},
		{"icon.plugin.console.clear", "clear-console"},
		{"icon.plugin.console.provider", "terminal"},
		{"icon.plugin.console.scroll.lock", "lock"},
		{"icon.plugin.datatypes.archive.built.in.closed", "book"},
		{"icon.plugin.datatypes.archive.built.in.open", "book"},
		{"icon.plugin.datatypes.archive.file.closed", "book"},
		{"icon.plugin.datatypes.archive.file.open", "book"},
		{"icon.plugin.datatypes.archive.program.closed", "book"},
		{"icon.plugin.datatypes.archive.program.open", "book"},
		{"icon.plugin.datatypes.archive.project.closed", "book"},
		{"icon.plugin.datatypes.archive.project.open", "book"},
		{"icon.plugin.datatypes.built.in", "cube"},
		{"icon.plugin.datatypes.enum", "table"},
		{"icon.plugin.datatypes.function", "function"},
		{"icon.plugin.datatypes.pointer", "right"},
		{"icon.plugin.datatypes.provider", "layers"},
		{"icon.plugin.datatypes.structure", "grid"},
		{"icon.plugin.datatypes.typedef", "tag"},
		{"icon.plugin.datatypes.union", "layers"},
		{"icon.plugin.datatypes.util.open.root", "library"},
		{"icon.plugin.datatypes.util.root", "library"},
		{"icon.plugin.fsbrowser.import", "import"},
		{"icon.plugin.fsbrowser.library", "library"},
		{"icon.plugin.functiongraph.action.provider", "graph"},
		{"icon.plugin.functiongraph.action.provider.satellite", "grid"},
		{"icon.plugin.functiongraph.action.vertex.choose.color", "palette"},
		{"icon.plugin.functiongraph.action.vertex.choose.color.palette", "grid"},
		{"icon.plugin.functiongraph.action.vertex.edit.format", "table"},
		{"icon.plugin.functiongraph.action.vertex.edit.label", "tag"},
		{"icon.plugin.functiongraph.action.vertex.full.screen", "expand"},
		{"icon.plugin.functiongraph.action.vertex.group", "layers"},
		{"icon.plugin.functiongraph.action.vertex.group.add", "plus"},
		{"icon.plugin.functiongraph.action.vertex.maximize", "expand"},
		{"icon.plugin.functiongraph.action.vertex.minimize", "collapse"},
		{"icon.plugin.functiongraph.action.vertex.regroup", "layers"},
		{"icon.plugin.functiongraph.action.vertex.ungroup", "expand"},
		{"icon.plugin.functiongraph.action.vertex.xrefs", "link"},
		{"icon.plugin.functiongraph.action.viewer.clone", "graph-snapshot"},
		{"icon.plugin.functiongraph.action.viewer.home", "home"},
		{"icon.plugin.functiongraph.action.viewer.layout", "settings"},
		{"icon.plugin.functiongraph.action.viewer.reset", "refresh"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.cycles", "refresh"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.cycles.all", "graph-cycle"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.off", "filter-off"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.paths.all", "graph"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.paths.from.to.vertex", "link"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.paths.from.vertex", "right"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.paths.to.vertex", "left"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.scoped.flow.forward", "right"},
		{"icon.plugin.functiongraph.action.viewer.vertex.hover.scoped.flow.reverse", "left"},
		{"icon.plugin.functiongraph.layout.experimental", "graph"},
		{"icon.plugin.functiongraph.layout.flowchart", "graph"},
		{"icon.plugin.functiongraph.layout.flowchart.left", "graph"},
		{"icon.plugin.functionwindow.provider", "function"},
		{"icon.plugin.manager.default", "grid"},
		{"icon.plugin.navigation.bookmark", "next-bookmark"},
		{"icon.plugin.navigation.bookmark.analysis", "wand"},
		{"icon.plugin.navigation.bookmark.note", "bookmark"},
		{"icon.plugin.navigation.bytes", "grid"},
		{"icon.plugin.navigation.data", "next-data"},
		{"icon.plugin.navigation.function", "function"},
		{"icon.plugin.navigation.highlight.range.next", "highlight-next"},
		{"icon.plugin.navigation.highlight.range.previous", "highlight-previous"},
		{"icon.plugin.navigation.instruction", "instruction"},
		{"icon.plugin.navigation.label", "tag"},
		{"icon.plugin.navigation.selection.range.next", "selection-next"},
		{"icon.plugin.navigation.selection.range.previous", "selection-previous"},
		{"icon.plugin.navigation.undefined", "help"},
		{"icon.plugin.programtree.closed.folder", "folder"},
		{"icon.plugin.programtree.docs", "book"},
		{"icon.plugin.programtree.fragment", "code"},
		{"icon.plugin.programtree.fragment.closed.folder", "folder"},
		{"icon.plugin.programtree.fragment.empty", "file"},
		{"icon.plugin.programtree.fragment.open.folder", "folder-open"},
		{"icon.plugin.programtree.fragment.viewed", "code"},
		{"icon.plugin.programtree.fragment.viewed.closed.folder.with.description", "folder"},
		{"icon.plugin.programtree.fragment.viewed.empty", "file"},
		{"icon.plugin.programtree.new.tree", "folder-add"},
		{"icon.plugin.programtree.open.folder", "folder-open"},
		{"icon.plugin.scriptmanager.provider", "terminal"},
		{"icon.plugin.symboltree.node.category.classes.closed", "folder"},
		{"icon.plugin.symboltree.node.category.classes.open", "folder-open"},
		{"icon.plugin.symboltree.node.category.exports.closed", "folder"},
		{"icon.plugin.symboltree.node.category.exports.open", "folder-open"},
		{"icon.plugin.symboltree.node.category.function.closed", "folder"},
		{"icon.plugin.symboltree.node.category.function.open", "folder-open"},
		{"icon.plugin.symboltree.node.category.imports.closed", "folder"},
		{"icon.plugin.symboltree.node.category.imports.open", "folder-open"},
		{"icon.plugin.symboltree.node.category.label.closed", "folder"},
		{"icon.plugin.symboltree.node.category.label.open", "folder-open"},
		{"icon.plugin.symboltree.node.category.namespace.closed", "folder"},
		{"icon.plugin.symboltree.node.category.namespace.open", "folder-open"},
		{"icon.plugin.symboltree.node.class", "cube"},
		{"icon.plugin.symboltree.node.code", "tag"},
		{"icon.plugin.symboltree.node.code.external", "export"},
		{"icon.plugin.symboltree.node.function", "function"},
		{"icon.plugin.symboltree.node.function.external", "export"},
		{"icon.plugin.symboltree.node.function.thunk", "link"},
		{"icon.plugin.symboltree.node.group.folder.closed", "folder"},
		{"icon.plugin.symboltree.node.group.folder.open", "folder-open"},
		{"icon.plugin.symboltree.node.library", "library"},
		{"icon.plugin.symboltree.node.local.variable", "variable"},
		{"icon.plugin.symboltree.node.namespace", "braces"},
		{"icon.plugin.symboltree.node.organization.closed", "folder"},
		{"icon.plugin.symboltree.node.organization.open", "folder-open"},
		{"icon.plugin.symboltree.node.parameter", "parameter"},
		{"icon.plugin.symboltree.node.root", "graph"},
		{"icon.plugin.symboltree.provider", "tree"},
		{"icon.projectdata.delete", "trash"},
		{"icon.projectdata.rename", "edit"},
		{"icon.properties", "settings"},
		{"icon.provider.clone", "camera"},
		{"icon.redo", "redo"},
		{"icon.refresh", "refresh"},
		{"icon.rename", "edit"},
		{"icon.right", "right"},
		{"icon.right.alt", "right"},
		{"icon.run", "play"},
		{"icon.save", "save"},
		{"icon.save.as", "save"},
		{"icon.search", "search"},
		{"icon.sort.ascending", "sort-up"},
		{"icon.sort.descending", "sort-down"},
		{"icon.stop", "stop"},
		{"icon.subtract", "minus"},
		{"icon.table", "table"},
		{"icon.toggle.collapse", "collapse"},
		{"icon.toggle.expand", "expand"},
		{"icon.undo", "undo"},
		{"icon.up", "up"},
		{"icon.warning", "warning"},
		{"icon.warning.strong", "warning"},
		{"icon.widget.filterpanel.filter.off", "filter-off"},
		{"icon.widget.filterpanel.filter.on", "filter"},
		{"icon.widget.tabs.close", "close"},
		{"icon.widget.tabs.close.highlight", "close"},
		{"icon.widget.tabs.list", "down"},
		{"icon.zoom.in", "zoom-in"},
		{"icon.zoom.out", "zoom-out"}
	};
	static void install(GTheme theme, boolean dark) {
		Map<String, Icon> icons = new HashMap<>();
		theme.setIcon("icon.opcodex.glyph.file", load("file", dark));
		theme.setIcon("icon.opcodex.glyph.folder", load("folder", dark));
		theme.setIcon("icon.opcodex.glyph.folder-open", load("folder-open", dark));
		theme.setIcon("icon.opcodex.glyph.folder-add", load("folder-add", dark));
		theme.setIcon("icon.opcodex.glyph.save", load("save", dark));
		theme.setIcon("icon.opcodex.glyph.copy", load("copy", dark));
		theme.setIcon("icon.opcodex.glyph.cut", load("cut", dark));
		theme.setIcon("icon.opcodex.glyph.paste", load("paste", dark));
		theme.setIcon("icon.opcodex.glyph.plus", load("plus", dark));
		theme.setIcon("icon.opcodex.glyph.minus", load("minus", dark));
		theme.setIcon("icon.opcodex.glyph.close", load("close", dark));
		theme.setIcon("icon.opcodex.glyph.trash", load("trash", dark));
		theme.setIcon("icon.opcodex.glyph.search", load("search", dark));
		theme.setIcon("icon.opcodex.glyph.filter", load("filter", dark));
		theme.setIcon("icon.opcodex.glyph.filter-off", load("filter-off", dark));
		theme.setIcon("icon.opcodex.glyph.settings", load("settings", dark));
		theme.setIcon("icon.opcodex.glyph.left", load("left", dark));
		theme.setIcon("icon.opcodex.glyph.right", load("right", dark));
		theme.setIcon("icon.opcodex.glyph.up", load("up", dark));
		theme.setIcon("icon.opcodex.glyph.down", load("down", dark));
		theme.setIcon("icon.opcodex.glyph.undo", load("undo", dark));
		theme.setIcon("icon.opcodex.glyph.redo", load("redo", dark));
		theme.setIcon("icon.opcodex.glyph.refresh", load("refresh", dark));
		theme.setIcon("icon.opcodex.glyph.home", load("home", dark));
		theme.setIcon("icon.opcodex.glyph.lock", load("lock", dark));
		theme.setIcon("icon.opcodex.glyph.unlock", load("unlock", dark));
		theme.setIcon("icon.opcodex.glyph.check", load("check", dark));
		theme.setIcon("icon.opcodex.glyph.warning", load("warning", dark));
		theme.setIcon("icon.opcodex.glyph.error", load("error", dark));
		theme.setIcon("icon.opcodex.glyph.info", load("info", dark));
		theme.setIcon("icon.opcodex.glyph.help", load("help", dark));
		theme.setIcon("icon.opcodex.glyph.stop", load("stop", dark));
		theme.setIcon("icon.opcodex.glyph.play", load("play", dark));
		theme.setIcon("icon.opcodex.glyph.expand", load("expand", dark));
		theme.setIcon("icon.opcodex.glyph.collapse", load("collapse", dark));
		theme.setIcon("icon.opcodex.glyph.table", load("table", dark));
		theme.setIcon("icon.opcodex.glyph.terminal", load("terminal", dark));
		theme.setIcon("icon.opcodex.glyph.function", load("function", dark));
		theme.setIcon("icon.opcodex.glyph.code", load("code", dark));
		theme.setIcon("icon.opcodex.glyph.braces", load("braces", dark));
		theme.setIcon("icon.opcodex.glyph.cube", load("cube", dark));
		theme.setIcon("icon.opcodex.glyph.variable", load("variable", dark));
		theme.setIcon("icon.opcodex.glyph.parameter", load("parameter", dark));
		theme.setIcon("icon.opcodex.glyph.library", load("library", dark));
		theme.setIcon("icon.opcodex.glyph.tag", load("tag", dark));
		theme.setIcon("icon.opcodex.glyph.layers", load("layers", dark));
		theme.setIcon("icon.opcodex.glyph.bookmark", load("bookmark", dark));
		theme.setIcon("icon.opcodex.glyph.pin", load("pin", dark));
		theme.setIcon("icon.opcodex.glyph.link", load("link", dark));
		theme.setIcon("icon.opcodex.glyph.graph", load("graph", dark));
		theme.setIcon("icon.opcodex.glyph.wand", load("wand", dark));
		theme.setIcon("icon.opcodex.glyph.edit", load("edit", dark));
		theme.setIcon("icon.opcodex.glyph.selection", load("selection", dark));
		theme.setIcon("icon.opcodex.glyph.history", load("history", dark));
		theme.setIcon("icon.opcodex.glyph.camera", load("camera", dark));
		theme.setIcon("icon.opcodex.glyph.sort-up", load("sort-up", dark));
		theme.setIcon("icon.opcodex.glyph.sort-down", load("sort-down", dark));
		theme.setIcon("icon.opcodex.glyph.import", load("import", dark));
		theme.setIcon("icon.opcodex.glyph.export", load("export", dark));
		theme.setIcon("icon.opcodex.glyph.bug", load("bug", dark));
		theme.setIcon("icon.opcodex.glyph.drive", load("drive", dark));
		theme.setIcon("icon.opcodex.glyph.zoom-in", load("zoom-in", dark));
		theme.setIcon("icon.opcodex.glyph.zoom-out", load("zoom-out", dark));
		theme.setIcon("icon.opcodex.glyph.flag", load("flag", dark));
		theme.setIcon("icon.opcodex.glyph.grid", load("grid", dark));
		theme.setIcon("icon.opcodex.glyph.book", load("book", dark));
		theme.setIcon("icon.opcodex.glyph.array", load("array", dark));
		theme.setIcon("icon.opcodex.glyph.cpu", load("cpu", dark));
		theme.setIcon("icon.opcodex.glyph.memory", load("memory", dark));
		theme.setIcon("icon.opcodex.glyph.pause", load("pause", dark));
		theme.setIcon("icon.opcodex.glyph.step-into", load("step-into", dark));
		theme.setIcon("icon.opcodex.glyph.step-over", load("step-over", dark));
		theme.setIcon("icon.opcodex.glyph.step-out", load("step-out", dark));
		theme.setIcon("icon.opcodex.glyph.step-back", load("step-back", dark));
		theme.setIcon("icon.opcodex.glyph.rewind", load("rewind", dark));
		theme.setIcon("icon.opcodex.glyph.fast-forward", load("fast-forward", dark));
		theme.setIcon("icon.opcodex.glyph.record", load("record", dark));
		theme.setIcon("icon.opcodex.glyph.breakpoint", load("breakpoint", dark));
		theme.setIcon("icon.opcodex.glyph.breakpoint-disabled", load("breakpoint-disabled", dark));
		theme.setIcon("icon.opcodex.glyph.breakpoint-mixed", load("breakpoint-mixed", dark));
		theme.setIcon("icon.opcodex.glyph.breakpoint-pending", load("breakpoint-pending", dark));
		theme.setIcon("icon.opcodex.glyph.connect", load("connect", dark));
		theme.setIcon("icon.opcodex.glyph.disconnect", load("disconnect", dark));
		theme.setIcon("icon.opcodex.glyph.split", load("split", dark));
		theme.setIcon("icon.opcodex.glyph.merge", load("merge", dark));
		theme.setIcon("icon.opcodex.glyph.compare", load("compare", dark));
		theme.setIcon("icon.opcodex.glyph.branch", load("branch", dark));
		theme.setIcon("icon.opcodex.glyph.clock", load("clock", dark));
		theme.setIcon("icon.opcodex.glyph.image", load("image", dark));
		theme.setIcon("icon.opcodex.glyph.monitor", load("monitor", dark));
		theme.setIcon("icon.opcodex.glyph.phone", load("phone", dark));
		theme.setIcon("icon.opcodex.glyph.volume", load("volume", dark));
		theme.setIcon("icon.opcodex.glyph.archive", load("archive", dark));
		theme.setIcon("icon.opcodex.glyph.binary", load("binary", dark));
		theme.setIcon("icon.opcodex.glyph.hex", load("hex", dark));
		theme.setIcon("icon.opcodex.glyph.text", load("text", dark));
		theme.setIcon("icon.opcodex.glyph.regex", load("regex", dark));
		theme.setIcon("icon.opcodex.glyph.key", load("key", dark));
		theme.setIcon("icon.opcodex.glyph.star", load("star", dark));
		theme.setIcon("icon.opcodex.glyph.user", load("user", dark));
		theme.setIcon("icon.opcodex.glyph.users", load("users", dark));
		theme.setIcon("icon.opcodex.glyph.target", load("target", dark));
		theme.setIcon("icon.opcodex.glyph.eye", load("eye", dark));
		theme.setIcon("icon.opcodex.glyph.move", load("move", dark));
		theme.setIcon("icon.opcodex.glyph.pointer", load("pointer", dark));
		theme.setIcon("icon.opcodex.glyph.list", load("list", dark));
		theme.setIcon("icon.opcodex.glyph.lightbulb", load("lightbulb", dark));
		theme.setIcon("icon.opcodex.glyph.wave", load("wave", dark));
		theme.setIcon("icon.opcodex.glyph.asterisk", load("asterisk", dark));
		theme.setIcon("icon.opcodex.glyph.circle", load("circle", dark));
		theme.setIcon("icon.opcodex.glyph.circle-check", load("circle-check", dark));
		theme.setIcon("icon.opcodex.glyph.circle-minus", load("circle-minus", dark));
		theme.setIcon("icon.opcodex.glyph.tree", load("tree", dark));
		theme.setIcon("icon.opcodex.glyph.swap", load("swap", dark));
		theme.setIcon("icon.opcodex.glyph.rss", load("rss", dark));
		theme.setIcon("icon.opcodex.glyph.usb", load("usb", dark));
		theme.setIcon("icon.opcodex.glyph.fit", load("fit", dark));
		theme.setIcon("icon.opcodex.glyph.chevron-down", load("chevron-down", dark));
		theme.setIcon("icon.opcodex.glyph.arrow-up-left", load("arrow-up-left", dark));
		theme.setIcon("icon.opcodex.glyph.arrow-down-right", load("arrow-down-right", dark));
		theme.setIcon("icon.opcodex.glyph.palette", load("palette", dark));
		theme.setIcon("icon.opcodex.glyph.clear-console", load("clear-console", dark));
		theme.setIcon("icon.opcodex.glyph.graph-snapshot", load("graph-snapshot", dark));
		theme.setIcon("icon.opcodex.glyph.listing", load("listing", dark));
		theme.setIcon("icon.opcodex.glyph.decompile", load("decompile", dark));
		theme.setIcon("icon.opcodex.glyph.instruction", load("instruction", dark));
		theme.setIcon("icon.opcodex.glyph.selection-next", load("selection-next", dark));
		theme.setIcon("icon.opcodex.glyph.selection-previous", load("selection-previous", dark));
		theme.setIcon("icon.opcodex.glyph.highlight-next", load("highlight-next", dark));
		theme.setIcon("icon.opcodex.glyph.highlight-previous", load("highlight-previous", dark));
		theme.setIcon("icon.opcodex.glyph.register-bank", load("register-bank", dark));
		theme.setIcon("icon.opcodex.glyph.next-data", load("next-data", dark));
		theme.setIcon("icon.opcodex.glyph.api-reference", load("api-reference", dark));
		theme.setIcon("icon.opcodex.glyph.editor-ide", load("editor-ide", dark));
		theme.setIcon("icon.opcodex.glyph.editor-external", load("editor-external", dark));
		theme.setIcon("icon.opcodex.glyph.rename", load("rename", dark));
		theme.setIcon("icon.opcodex.glyph.run-again", load("run-again", dark));
		theme.setIcon("icon.opcodex.glyph.filter-clear", load("filter-clear", dark));
		theme.setIcon("icon.opcodex.glyph.filter-tree-contains", load("filter-tree-contains", dark));
		theme.setIcon("icon.opcodex.glyph.filter-tree-exact", load("filter-tree-exact", dark));
		theme.setIcon("icon.opcodex.glyph.filter-tree-starts", load("filter-tree-starts", dark));
		theme.setIcon("icon.opcodex.glyph.filter-tree-regex", load("filter-tree-regex", dark));
		theme.setIcon("icon.opcodex.glyph.next-bookmark", load("next-bookmark", dark));
		theme.setIcon("icon.opcodex.glyph.graph-cycle", load("graph-cycle", dark));
		for (String[] entry : ENTRIES) {
			Icon icon = icons.computeIfAbsent(entry[1], glyph -> load(glyph, dark));
			theme.setIcon(entry[0], icon);
		}
		try {
			theme.addIcon(IconValue.parse("icon.add", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.arrow.down.right", "icon.opcodex.glyph.arrow-down-right"));
			theme.addIcon(IconValue.parse("icon.arrow.up.left", "icon.opcodex.glyph.arrow-up-left"));
			theme.addIcon(IconValue.parse("icon.base.application.128", "images/opcodex/brand/app-neutral-128.png"));
			theme.addIcon(IconValue.parse("icon.base.application.16", "images/opcodex/brand/app-neutral-16.png"));
			theme.addIcon(IconValue.parse("icon.base.application.24", "images/opcodex/brand/app-neutral-24.png"));
			theme.addIcon(IconValue.parse("icon.base.application.256", "images/opcodex/brand/app-neutral-256.png"));
			theme.addIcon(IconValue.parse("icon.base.application.32", "images/opcodex/brand/app-neutral-32.png"));
			theme.addIcon(IconValue.parse("icon.base.application.40", "images/opcodex/brand/app-neutral-40.png"));
			theme.addIcon(IconValue.parse("icon.base.application.48", "images/opcodex/brand/app-neutral-48.png"));
			theme.addIcon(IconValue.parse("icon.base.application.64", "images/opcodex/brand/app-neutral-64.png"));
			theme.addIcon(IconValue.parse("icon.base.application.home", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.base.delete", "icon.delete"));
			theme.addIcon(IconValue.parse("icon.base.edit.bytes", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.base.hover.off", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.base.hover.on", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.base.lock", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.base.mem.search.panel.options", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.base.mem.search.panel.scan", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.base.mem.search.panel.search", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.base.pinned", "icon.opcodex.glyph.pin"));
			theme.addIcon(IconValue.parse("icon.base.plugin.quickfix.done", "icon.checkmark.green"));
			theme.addIcon(IconValue.parse("icon.base.plus", "icon.add"));
			theme.addIcon(IconValue.parse("icon.base.search.marker", "icon.opcodex.glyph.search"));
			theme.addIcon(IconValue.parse("icon.base.source.c", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.base.unlock", "icon.opcodex.glyph.unlock"));
			theme.addIcon(IconValue.parse("icon.base.util.datatree.version.control.archive.dt.checkout.undo", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.base.util.fixed.bit.size.field", "icon.pulldown"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.area.markers.all", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.area.markers.diff", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.area.markers.unmatched", "icon.navigate.in"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.cursor", "icon.opcodex.glyph.pointer"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.diff.next", "icon.opcodex.glyph.sort-up"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.hover.off", "icon.base.hover.off"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.hover.on", "icon.base.hover.on"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.previous.next", "icon.opcodex.glyph.sort-down"));
			theme.addIcon(IconValue.parse("icon.base.util.listingcompare.provider", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.base.util.listingdiff.diffs.byte", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.base.util.listingdiff.diffs.constants", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.base.util.listingdiff.diffs.not", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.base.util.listingdiff.diffs.registers", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.base.util.viewer.fieldfactory.label", "icon.base.pinned [move(0,4)]"));
			theme.addIcon(IconValue.parse("icon.base.util.viewer.fieldfactory.openclose.closed", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.base.util.viewer.fieldfactory.openclose.open", "icon.opcodex.glyph.minus"));
			theme.addIcon(IconValue.parse("icon.base.util.viewer.fieldfactory.variable", "icon.warning"));
			theme.addIcon(IconValue.parse("icon.base.util.xml.functions.bookmark", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.bsim.change.password", "icon.opcodex.glyph.key"));
			theme.addIcon(IconValue.parse("icon.bsim.connected", "icon.opcodex.glyph.connect"));
			theme.addIcon(IconValue.parse("icon.bsim.disconnected", "icon.opcodex.glyph.disconnect"));
			theme.addIcon(IconValue.parse("icon.bsim.functions.table", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.bsim.query.dialog.provider", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.bsim.results.status.ignored", "icon.opcodex.glyph.circle-check"));
			theme.addIcon(IconValue.parse("icon.bsim.results.status.matches", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.bsim.results.status.name.applied", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.bsim.results.status.signature.applied", "EMPTY_ICON {icon.opcodex.glyph.check[move(-2,-1)]} {icon.opcodex.glyph.check [move(4,0)]}"));
			theme.addIcon(IconValue.parse("icon.bsim.table.split", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.byte.sequence.analyzer.clipboard.merged", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.byte.sequence.analyzer.merge", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.function.bit.patterns.disabled", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.function.bit.patterns.enabled", "icon.opcodex.glyph.circle-check"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.pattern.mining.analyzer", "icon.opcodex.glyph.zoom-in"));
			theme.addIcon(IconValue.parse("icon.bytepatterns.send.to.clipboard", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.check", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.checkmark.green", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.clear", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.collapse.all", "icon.opcodex.glyph.collapse"));
			theme.addIcon(IconValue.parse("icon.configure.filter", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.console", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.content.handler.archive.dt", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.content.handler.link", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.content.handler.link.overlay", "EMPTY_ICON[size(16,16)]{icon.content.handler.link[move(0,8)]}"));
			theme.addIcon(IconValue.parse("icon.content.handler.linked.folder.closed", "icon.datatree.node.domain.folder.closed{icon.content.handler.link.overlay}"));
			theme.addIcon(IconValue.parse("icon.content.handler.linked.folder.open", "icon.datatree.node.domain.folder.open{icon.content.handler.link.overlay}"));
			theme.addIcon(IconValue.parse("icon.content.handler.program", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.content.handler.trace", "icon.opcodex.glyph.history"));
			theme.addIcon(IconValue.parse("icon.copy", "icon.opcodex.glyph.copy"));
			theme.addIcon(IconValue.parse("icon.cut", "icon.opcodex.glyph.cut"));
			theme.addIcon(IconValue.parse("icon.data.type.audio.player", "icon.opcodex.glyph.volume"));
			theme.addIcon(IconValue.parse("icon.datatree.node.domain.file", "icon.domain.file.uknown"));
			theme.addIcon(IconValue.parse("icon.datatree.node.domain.folder.closed", "icon.folder.closed"));
			theme.addIcon(IconValue.parse("icon.datatree.node.domain.folder.open", "icon.folder.open"));
			theme.addIcon(IconValue.parse("icon.debugger", "icon.opcodex.glyph.bug"));
			theme.addIcon(IconValue.parse("icon.debugger.add", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.debugger.attach", "icon.opcodex.glyph.connect"));
			theme.addIcon(IconValue.parse("icon.debugger.autoread", "icon.opcodex.glyph.eye"));
			theme.addIcon(IconValue.parse("icon.debugger.blank", "EMPTY_ICON"));
			theme.addIcon(IconValue.parse("icon.debugger.block", "icon.debugger.map.sections"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.clear", "icon.opcodex.glyph.breakpoint{icon.opcodex.glyph.close[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.clear.all", "icon.opcodex.glyph.breakpoint-disabled{icon.opcodex.glyph.layers[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.disable", "icon.opcodex.glyph.breakpoint-disabled"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.disable.all", "icon.opcodex.glyph.breakpoint-disabled{icon.opcodex.glyph.layers[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.enable", "icon.opcodex.glyph.breakpoint"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.enable.all", "icon.opcodex.glyph.breakpoint{icon.opcodex.glyph.layers[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.make.effective", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.disabled", "icon.debugger.breakpoint.disable"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.enabled", "icon.debugger.breakpoint.enable"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.ineffective.disabled", "icon.opcodex.glyph.breakpoint-pending"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.ineffective.enabled", "icon.opcodex.glyph.breakpoint-pending"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.ineffective.mixed", "icon.opcodex.glyph.breakpoint-pending"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.marker.mixed", "icon.opcodex.glyph.breakpoint-mixed"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.overlay.inconsistent", "icon.opcodex.glyph.breakpoint-pending"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.set", "icon.opcodex.glyph.breakpoint"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.close_all_zoom_windows", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.grid", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.no_outline", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.outline", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.single_column", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.zoom_in", "icon.opcodex.glyph.zoom-in"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.zoom_out", "icon.opcodex.glyph.zoom-out"));
			theme.addIcon(IconValue.parse("icon.debugger.breakpoint.timeline.zoom_out_max", "icon.opcodex.glyph.fit"));
			theme.addIcon(IconValue.parse("icon.debugger.capture.symbols", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.debugger.clear", "icon.opcodex.glyph.cut"));
			theme.addIcon(IconValue.parse("icon.debugger.close", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.debugger.config", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.debugger.connect", "icon.opcodex.glyph.connect"));
			theme.addIcon(IconValue.parse("icon.debugger.connect.accept", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.debugger.connect.outbound", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.debugger.control.mode.ro.target", "icon.opcodex.glyph.record"));
			theme.addIcon(IconValue.parse("icon.debugger.control.mode.ro.trace", "icon.opcodex.glyph.history"));
			theme.addIcon(IconValue.parse("icon.debugger.control.mode.rw.emulator", "icon.opcodex.glyph.cpu{icon.opcodex.glyph.edit[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.control.mode.rw.target", "icon.opcodex.glyph.monitor{icon.opcodex.glyph.edit[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.control.mode.rw.trace", "icon.opcodex.glyph.history{icon.opcodex.glyph.edit[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.debugger.data.types", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.debugger.delete", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.debugger.detach", "icon.opcodex.glyph.disconnect"));
			theme.addIcon(IconValue.parse("icon.debugger.diff", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.debugger.diff.next", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.debugger.diff.previous", "icon.opcodex.glyph.up"));
			theme.addIcon(IconValue.parse("icon.debugger.disassemble", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.debugger.disconnect", "icon.opcodex.glyph.disconnect"));
			theme.addIcon(IconValue.parse("icon.debugger.display.export.facts", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.debugger.display.export.xml", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.debugger.display.graph", "icon.opcodex.glyph.breakpoint"));
			theme.addIcon(IconValue.parse("icon.debugger.display.import.facts", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.debugger.display.import.xml", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.debugger.display.xml", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.debugger.display.xml.filtered", "icon.debugger.display.xml"));
			theme.addIcon(IconValue.parse("icon.debugger.emulate", "icon.debugger.process"));
			theme.addIcon(IconValue.parse("icon.debugger.enable.edits", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.debugger.filter", "icon.opcodex.glyph.filter-off"));
			theme.addIcon(IconValue.parse("icon.debugger.import", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.debugger.interrupt", "icon.opcodex.glyph.pause"));
			theme.addIcon(IconValue.parse("icon.debugger.kill", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.debugger.launch", "icon.opcodex.glyph.play"));
			theme.addIcon(IconValue.parse("icon.debugger.log.error", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.debugger.log.fatal", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.debugger.log.warn", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.debugger.map.auto", "icon.debugger.config"));
			theme.addIcon(IconValue.parse("icon.debugger.map.identically", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.debugger.map.manual", "icon.debugger.provider.mappings"));
			theme.addIcon(IconValue.parse("icon.debugger.map.modules", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.debugger.map.regions", "icon.debugger.map.modules"));
			theme.addIcon(IconValue.parse("icon.debugger.map.sections", "icon.debugger.map.modules"));
			theme.addIcon(IconValue.parse("icon.debugger.marker.event", "icon.debugger.marker.register"));
			theme.addIcon(IconValue.parse("icon.debugger.marker.register", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.debugger.model.table.attributes", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.debugger.model.table.elements", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.debugger.model.tree.objects", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.debugger.modules.table.sections", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.debugger.node.object.empty", "icon.debugger.object.unpopulated"));
			theme.addIcon(IconValue.parse("icon.debugger.node.object.event", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.debugger.node.object.populated", "icon.debugger.object.populated"));
			theme.addIcon(IconValue.parse("icon.debugger.node.object.running", "icon.opcodex.glyph.play"));
			theme.addIcon(IconValue.parse("icon.debugger.node.object.terminated", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.debugger.object.populated", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.debugger.object.unpopulated", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.debugger.open.windbg.trace", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.debugger.overlay.tx", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.debugger.package", "icon.opcodex.glyph.bug"));
			theme.addIcon(IconValue.parse("icon.debugger.pin", "icon.opcodex.glyph.pin"));
			theme.addIcon(IconValue.parse("icon.debugger.process", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.debugger.processor", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.breakpoints", "icon.opcodex.glyph.breakpoint-mixed"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.console", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.listing", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.mappings", "icon.content.handler.program"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.memory.bytes", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.model", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.modules", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.objects", "icon.debugger.object.populated"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.pcode", "icon.opcodex.glyph.step-into"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.regions", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.registers", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.stack", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.debugger.provider.time", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.debugger.record", "icon.opcodex.glyph.record"));
			theme.addIcon(IconValue.parse("icon.debugger.refresh", "icon.opcodex.glyph.refresh"));
			theme.addIcon(IconValue.parse("icon.debugger.refresh.memory", "icon.debugger.refresh"));
			theme.addIcon(IconValue.parse("icon.debugger.rename.snapshot", "icon.debugger.provider.time"));
			theme.addIcon(IconValue.parse("icon.debugger.resume", "icon.opcodex.glyph.play"));
			theme.addIcon(IconValue.parse("icon.debugger.resume.back", "icon.opcodex.glyph.rewind"));
			theme.addIcon(IconValue.parse("icon.debugger.save", "icon.save"));
			theme.addIcon(IconValue.parse("icon.debugger.seek.present", "icon.opcodex.glyph.target"));
			theme.addIcon(IconValue.parse("icon.debugger.select.addresses", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.debugger.select.registers", "icon.opcodex.glyph.target"));
			theme.addIcon(IconValue.parse("icon.debugger.select.rows", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.debugger.skip.over", "icon.opcodex.glyph.step-over"));
			theme.addIcon(IconValue.parse("icon.debugger.snap.backward", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.debugger.snap.forward", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.debugger.step.back", "icon.opcodex.glyph.step-back"));
			theme.addIcon(IconValue.parse("icon.debugger.step.back.into", "icon.opcodex.glyph.step-back"));
			theme.addIcon(IconValue.parse("icon.debugger.step.back.over", "icon.opcodex.glyph.step-back"));
			theme.addIcon(IconValue.parse("icon.debugger.step.finish", "icon.opcodex.glyph.step-out"));
			theme.addIcon(IconValue.parse("icon.debugger.step.into", "icon.opcodex.glyph.step-into"));
			theme.addIcon(IconValue.parse("icon.debugger.step.last", "icon.opcodex.glyph.step-out"));
			theme.addIcon(IconValue.parse("icon.debugger.step.over", "icon.opcodex.glyph.step-over"));
			theme.addIcon(IconValue.parse("icon.debugger.sync", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.debugger.table.object", "icon.table"));
			theme.addIcon(IconValue.parse("icon.debugger.thread", "icon.opcodex.glyph.wave"));
			theme.addIcon(IconValue.parse("icon.debugger.toggle", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.debugger.tree.object", "icon.debugger.object.unpopulated"));
			theme.addIcon(IconValue.parse("icon.debugger.unique.ref.read", "icon.opcodex.glyph.pointer [rotate(180)]"));
			theme.addIcon(IconValue.parse("icon.debugger.unique.ref.write", "icon.opcodex.glyph.pointer"));
			theme.addIcon(IconValue.parse("icon.debugger.visibility", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.export", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.provider", "icon.opcodex.glyph.decompile"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.provider.clone", "icon.provider.clone"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.provider.readonly", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.provider.unreachable", "icon.opcodex.glyph.filter-off"));
			theme.addIcon(IconValue.parse("icon.decompiler.action.slash", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.delete", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.dialog.error.expandable.cause", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.dialog.error.expandable.exception", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.dialog.error.expandable.frame", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.dialog.error.expandable.report", "icon.spreadsheet"));
			theme.addIcon(IconValue.parse("icon.dialog.error.expandable.stack", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.docking.application.128", "images/opcodex/brand/app-neutral-128.png"));
			theme.addIcon(IconValue.parse("icon.docking.application.16", "images/opcodex/brand/app-neutral-16.png"));
			theme.addIcon(IconValue.parse("icon.docking.application.home", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.domain.file.uknown", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.down", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.dragon.256", "images/opcodex/brand/app-neutral-256.png"));
			theme.addIcon(IconValue.parse("icon.drive", "icon.opcodex.glyph.drive"));
			theme.addIcon(IconValue.parse("icon.empty", "EMPTY_ICON"));
			theme.addIcon(IconValue.parse("icon.empty.20", "EMPTY_ICON[size(20,20)]"));
			theme.addIcon(IconValue.parse("icon.error", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.expand.all", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.extension.configure", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.filechooser.default.directory", "icon.folder.closed"));
			theme.addIcon(IconValue.parse("icon.filechooser.default.file", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.filechooser.places.desktop", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.filechooser.places.downloads", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.filechooser.places.home", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.filechooser.places.my.computer", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.filechooser.places.recent", "icon.opcodex.glyph.folder {icon.opcodex.glyph.undo [move(6,10)]}"));
			theme.addIcon(IconValue.parse("icon.filter.options.contains", "icon.opcodex.glyph.filter"));
			theme.addIcon(IconValue.parse("icon.filter.options.exact", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.filter.options.not", "icon.opcodex.glyph.circle-minus"));
			theme.addIcon(IconValue.parse("icon.filter.options.regex", "icon.opcodex.glyph.regex"));
			theme.addIcon(IconValue.parse("icon.filter.options.starts.with", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.flag", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.folder.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.folder.new", "icon.opcodex.glyph.folder-add"));
			theme.addIcon(IconValue.parse("icon.folder.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.font", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.frontend.project.connected", "icon.opcodex.glyph.connect"));
			theme.addIcon(IconValue.parse("icon.frontend.project.disconnected", "icon.opcodex.glyph.disconnect"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.apk", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.bat", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.bz", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.bz2", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.c", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.class", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.cpio", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.cpp", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.cs", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.css", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.data", "icon.opcodex.glyph.drive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.default", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.dfu", "icon.opcodex.glyph.circle-minus"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.dll", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.dmg", "icon.opcodex.glyph.circle-check"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.dylib", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.exe", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.f", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.gif", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.gz", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.h", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.html", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.img", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.img3", "icon.opcodex.glyph.asterisk"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.index", "icon.opcodex.glyph.bookmark"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.ipsw", "icon.opcodex.glyph.phone"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.iso", "icon.opcodex.glyph.drive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.jar", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.java", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.jpeg", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.jpg", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.js", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.kext", "icon.opcodex.glyph.circle"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.lib", "icon.opcodex.glyph.drive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.o", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.obj", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.p", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.pdf", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.plist", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.png", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.py", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.rss", "icon.opcodex.glyph.rss"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.sh", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.so", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.strings", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.tar", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.tgz", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.txt", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.usb", "icon.opcodex.glyph.usb"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.xhtml", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.xml", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.xz", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.extension.zip", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.overlay.filesystem", "EMPTY_ICON{icon.opcodex.glyph.circle-check[size(8,8)][move(0,8)]}"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.overlay.imported", "EMPTY_ICON{icon.opcodex.glyph.check[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.overlay.link", "EMPTY_ICON{icon.content.handler.link[move(0,8)]}"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.overlay.missing.password", "EMPTY_ICON{icon.opcodex.glyph.lock[size(8,8)][move(8,0)]}"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.substring.release.", "icon.opcodex.glyph.circle-minus"));
			theme.addIcon(IconValue.parse("icon.fsbrowser.file.substring.so.", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.fit.to.window", "icon.opcodex.glyph.fit"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.lasso", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.layout.algorithm", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.program.graph", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.satellite.view", "icon.opcodex.glyph.tree"));
			theme.addIcon(IconValue.parse("icon.graph.default.display.view.magnifier", "icon.opcodex.glyph.zoom-in"));
			theme.addIcon(IconValue.parse("icon.graph.layout.default", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.graph.satellite", "icon.opcodex.glyph.tree"));
			theme.addIcon(IconValue.parse("icon.graph.satellite.large", "icon.opcodex.glyph.tree"));
			theme.addIcon(IconValue.parse("icon.help", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.help.navigation.aid.disabled.overlay", "icon.not.allowed"));
			theme.addIcon(IconValue.parse("icon.help.navigation.aid.enabled", "icon.opcodex.glyph.lightbulb"));
			theme.addIcon(IconValue.parse("icon.home", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.information", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.left", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.left.alt", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.lock", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.logviewer.toolbar.file.open", "icon.folder.open"));
			theme.addIcon(IconValue.parse("icon.logviewer.toolbar.scroll.lock", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.make.selection", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.menu.file.new.project", "icon.folder.new"));
			theme.addIcon(IconValue.parse("icon.navigate.in", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.navigate.out", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.not.allowed", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.opcodex.splash", dark ? "images/opcodex/brand/splash-dark.png" : "images/opcodex/brand/splash-light.png"));
			theme.addIcon(IconValue.parse("icon.paste", "icon.opcodex.glyph.paste"));
			theme.addIcon(IconValue.parse("icon.pending", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.plugin.assembler.question", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.add", "icon.check"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.delete", "icon.base.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.select", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.analysis", "icon.opcodex.glyph.wand"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.default", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.error", "icon.base.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.info", "icon.information"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.note", "icon.opcodex.glyph.bookmark"));
			theme.addIcon(IconValue.parse("icon.plugin.bookmark.type.warning", "icon.warning"));
			theme.addIcon(IconValue.parse("icon.plugin.bundlemanager.disable", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.plugin.bundlemanager.enable", "icon.opcodex.glyph.play"));
			theme.addIcon(IconValue.parse("icon.plugin.byteviewer.options", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.plugin.byteviewer.provider", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.filter.non.calls", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.filter.select.source", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.function", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.node.dead.end", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.node.external", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.recursive", "icon.opcodex.glyph.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.reference", "icon.opcodex.glyph.link [size(12,12)][move(4,4)]"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.refresh", "icon.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.refresh.not.needed", "icon.plugin.calltree.refresh [disabled]"));
			theme.addIcon(IconValue.parse("icon.plugin.calltree.unify.functions", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.carry", "icon.opcodex.glyph.up"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.compute", "icon.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.ones.complement", "icon.opcodex.glyph.minus"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.select", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.show.hex", "icon.opcodex.glyph.hex"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.twos.complement", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.checksum.xor", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.plugin.clipboard.copy", "icon.copy"));
			theme.addIcon(IconValue.parse("icon.plugin.clipboard.paste", "icon.paste"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.cursor.location", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.cursor.marker", "icon.opcodex.glyph.search"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.format.collapse", "icon.opcodex.glyph.up"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.format.expand", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.hover.off", "icon.base.hover.off"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.hover.on", "icon.base.hover.on"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.mark.and.select.armed", "icon.opcodex.glyph.selection {icon.opcodex.glyph.play[size(12,12)][move(4,4)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.mark.and.select.unarmed", "icon.opcodex.glyph.selection {icon.opcodex.glyph.stop[size(12,12)][move(4,4)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.codebrowser.provider", "icon.opcodex.glyph.listing"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.apply", "icon.opcodex.glyph.save"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.array", "icon.opcodex.glyph.array"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.bit.field.dialog.add", "icon.base.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.bit.field.dialog.delete", "icon.base.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.bit.field.dialog.edit", "icon.opcodex.glyph.move"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.bit.field.editor.decrement", "icon.opcodex.glyph.minus"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.bit.field.editor.increment", "icon.base.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.clear", "icon.clear"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.create", "icon.plugin.datatypes.structure"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.delete", "icon.base.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.duplicate", "icon.opcodex.glyph.copy"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.duplicate.multiple", "icon.opcodex.glyph.copy"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.insert.undefined", "icon.base.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.move.down", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.move.up", "icon.opcodex.glyph.up"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.provider", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.provider.structure", "icon.plugin.datatypes.structure"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.provider.union", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.search.next", "icon.opcodex.glyph.down [size(16,16)]"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.search.previous", "icon.opcodex.glyph.up [size(16,16)]"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.show.type", "icon.home"));
			theme.addIcon(IconValue.parse("icon.plugin.composite.editor.unpackage", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.plugin.console.clear", "icon.opcodex.glyph.clear-console"));
			theme.addIcon(IconValue.parse("icon.plugin.console.provider", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.plugin.console.scroll.lock", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.datagraph.action.viewer.reset", "icon.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.datagraph.action.viewer.vertex.format", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.built.in.closed", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.built.in.open", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.file.closed", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.file.open", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.invalid", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.program.closed", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.program.open", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.project.closed", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.archive.project.open", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.associate.single.type", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.built.in", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.built.in.disabled", "icon.opcodex.glyph.cube [disabled]"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.commit.single.type", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.conflict.mode.rename.and.add", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.conflict.mode.replace.existing", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.conflict.mode.replace.or.rename", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.conflict.mode.use.existing", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.default", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.default.disabled", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.enum", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.filter.arrays.off", "icon.opcodex.glyph.array"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.filter.arrays.on", "icon.opcodex.glyph.array"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.filter.pointers.off", "icon.opcodex.glyph.pointer"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.filter.pointers.on", "icon.opcodex.glyph.pointer"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.function", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.pointer", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.provider", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.structure", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.change.local", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.change.source", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.conflict", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.missing", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.node.archive.file.checked.out", "icon.check"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.node.archive.file.checked.out.exclusive", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.node.archive.file.checked.out.not.latest", "icon.opcodex.glyph.history"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.node.archive.file.hijacked", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.tree.node.archive.file.read.only", "icon.opcodex.glyph.user [size(10,10)]"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.typedef", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.union", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.closed.archive", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.closed.folder", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.closed.folder.disabled", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.closed.folder.locked", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.favorite", "icon.opcodex.glyph.star"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.favorite.disabled", "icon.opcodex.glyph.star [disabled]"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.open.archive", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.open.folder", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.open.folder.disabled", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.open.folder.locked", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.open.root", "icon.opcodex.glyph.library"));
			theme.addIcon(IconValue.parse("icon.plugin.datatypes.util.root", "icon.opcodex.glyph.library"));
			theme.addIcon(IconValue.parse("icon.plugin.datawindow.provider", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.debug.dbviewer.provider", "icon.opcodex.glyph.fit"));
			theme.addIcon(IconValue.parse("icon.plugin.debug.domaineventviewer.provider", "icon.console"));
			theme.addIcon(IconValue.parse("icon.plugin.debug.propertymanager.marker", "icon.opcodex.glyph.search"));
			theme.addIcon(IconValue.parse("icon.plugin.debug.propertymanager.provider", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.plugin.decompiler.taint.default.query", "icon.opcodex.glyph.search"));
			theme.addIcon(IconValue.parse("icon.plugin.decompiler.taint.gate.set", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.plugin.decompiler.text.finder.select.functions", "icon.make.selection {icon.opcodex.glyph.function[size(12,12)][move(6,6)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.enum.editor.add", "icon.base.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.enum.editor.apply", "icon.save"));
			theme.addIcon(IconValue.parse("icon.plugin.enum.editor.delete", "icon.base.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.enum.editor.home", "icon.home"));
			theme.addIcon(IconValue.parse("icon.plugin.enum.editor.provider", "icon.plugin.datatypes.enum"));
			theme.addIcon(IconValue.parse("icon.plugin.fcg.layout.bow.tie", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.close", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.collapse.all", "icon.opcodex.glyph.collapse"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.compress", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.copy", "icon.copy"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.create.firmware", "icon.opcodex.glyph.cpu"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.cut", "icon.cut"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.delete", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.eclipse", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.expand.all", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.extract", "icon.opcodex.glyph.archive"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.font", "icon.font"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.import", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.info", "icon.information"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.ios", "icon.opcodex.glyph.phone"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.jar", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.library", "icon.opcodex.glyph.library"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.list.mounted", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.locked", "icon.base.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.new", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.open.all", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.open.as.binary", "icon.opcodex.glyph.cpu"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.open.file.system", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.open.in.listing", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.paste", "icon.paste"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.photo", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.redo", "icon.redo"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.refresh", "icon.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.rename", "icon.rename"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.save", "icon.save"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.save.as", "icon.save.as"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.undo", "icon.undo"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.unlocked", "icon.base.unlock"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.view.as.image", "icon.opcodex.glyph.image"));
			theme.addIcon(IconValue.parse("icon.plugin.fsbrowser.view.as.text", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.function.next", "icon.opcodex.glyph.down [move(3,1)] {icon.opcodex.glyph.function [move(-5,-2)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.function.previous", "icon.opcodex.glyph.up [move(3,1)] {icon.opcodex.glyph.function [move(-5,-2)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.function.remove", "icon.opcodex.glyph.trash [move(3,3)] {icon.opcodex.glyph.function [move(-5,-2)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.new", "icon.opcodex.glyph.code {icon.opcodex.glyph.star [size(16,16)][move(4,-4)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.open.function.table", "icon.opcodex.glyph.code {icon.add [size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.scroll.lock", "icon.base.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.functioncompare.scroll.unlock", "icon.base.unlock"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.provider", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.provider.satellite", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.choose.color", "icon.opcodex.glyph.palette"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.choose.color.palette", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.edit.format", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.edit.label", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.full.screen", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.group", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.group.add", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.maximize", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.minimize", "icon.opcodex.glyph.collapse"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.regroup", "icon.opcodex.glyph.layers"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.ungroup", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.vertex.xrefs", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.clone", "icon.opcodex.glyph.graph-snapshot"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.home", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.layout", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.reset", "icon.opcodex.glyph.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.cycles", "icon.opcodex.glyph.refresh"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.cycles.all", "icon.opcodex.glyph.graph-cycle"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.off", "icon.opcodex.glyph.filter-off"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.paths.all", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.paths.from.to.vertex", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.paths.from.vertex", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.paths.to.vertex", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.scoped.flow.forward", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.action.viewer.vertex.hover.scoped.flow.reverse", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.layout.experimental", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.layout.flowchart", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.layout.flowchart.left", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiongraph.layout.nested.code", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.functiontags.add", "icon.opcodex.glyph.right [size(16,16)]"));
			theme.addIcon(IconValue.parse("icon.plugin.functiontags.remove", "icon.opcodex.glyph.left [size(16,16)]"));
			theme.addIcon(IconValue.parse("icon.plugin.functionwindow.provider", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.address", "icon.opcodex.glyph.target"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.binary", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.hex", "icon.opcodex.glyph.hex"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.manual.entry", "icon.base.edit.bytes"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.operand", "icon.opcodex.glyph.parameter"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.scalar", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.instructiontable.undefined", "icon.opcodex.glyph.binary"));
			theme.addIcon(IconValue.parse("icon.plugin.interpreter.provider", "icon.console"));
			theme.addIcon(IconValue.parse("icon.plugin.jython", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.plugin.locationreferences.highlight", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.plugin.manager.default", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.add", "icon.add"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.delete", "icon.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.expand.down", "icon.toggle.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.expand.up", "icon.toggle.collapse"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.image.base", "icon.opcodex.glyph.home"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.merge", "icon.opcodex.glyph.merge"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.move", "icon.opcodex.glyph.move"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.provider", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.plugin.memorymap.split", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.plugin.merge", "icon.opcodex.glyph.merge"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.changed", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.conflict.collapse", "icon.toggle.collapse"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.conflict.expand", "icon.toggle.expand"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.conflict.lock", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.conflict.unlock", "icon.opcodex.glyph.unlock"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.status.complete", "icon.checkmark.green"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.status.in.progress", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.plugin.merge.status.pending", "icon.opcodex.glyph.circle-check"));
			theme.addIcon(IconValue.parse("icon.plugin.myprogramchanges.checkin", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.plugin.myprogramchanges.merge", "icon.opcodex.glyph.merge"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark", "icon.opcodex.glyph.next-bookmark"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.analysis", "icon.opcodex.glyph.wand"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.error", "icon.error"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.info", "icon.information"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.note", "icon.opcodex.glyph.bookmark"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.unknown", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bookmark.warning", "icon.warning"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.bytes", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.data", "icon.opcodex.glyph.next-data"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.function", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.highlight.range.next", "icon.opcodex.glyph.highlight-next"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.highlight.range.previous", "icon.opcodex.glyph.highlight-previous"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.instruction", "icon.opcodex.glyph.instruction"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.label", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.location.next", "icon.right"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.location.previous", "icon.left"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.selection.range.next", "icon.opcodex.glyph.selection-next"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.selection.range.previous", "icon.opcodex.glyph.selection-previous"));
			theme.addIcon(IconValue.parse("icon.plugin.navigation.undefined", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.plugin.overview.provider", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.apply", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.apply.next", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.cursor.location", "icon.opcodex.glyph.pointer"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.get.diffs", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.ignore", "icon.opcodex.glyph.cut"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.open.close.program", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.select.by.program", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.plugin.programdiff.settings", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.busy", "icon.base.edit.bytes"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.close", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.close.highlight", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.empty.small", "EMPTY_ICON[size(8,16)]"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.list", "icon.opcodex.glyph.fast-forward"));
			theme.addIcon(IconValue.parse("icon.plugin.programmanager.transient", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.closed.folder", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.docs", "icon.opcodex.glyph.book"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.drag.copy", "icon.opcodex.glyph.move"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.drag.move", "icon.opcodex.glyph.copy"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.closed.folder", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.empty", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.open.folder", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.viewed", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.viewed.closed.folder.with.description", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.fragment.viewed.empty", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.new.tree", "icon.opcodex.glyph.folder-add"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.open.folder", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.programtree.open.tree", "icon.folder.open"));
			theme.addIcon(IconValue.parse("icon.plugin.python", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.plugin.reachability.provider", "icon.opcodex.glyph.graph [rotate(90)]"));
			theme.addIcon(IconValue.parse("icon.plugin.register", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.plugin.register.group", "icon.opcodex.glyph.memory"));
			theme.addIcon(IconValue.parse("icon.plugin.register.provider", "icon.opcodex.glyph.register-bank"));
			theme.addIcon(IconValue.parse("icon.plugin.scalartable.provider", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.api", "icon.opcodex.glyph.api-reference"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.delete", "icon.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.edit", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.edit.eclipse", "icon.opcodex.glyph.editor-ide"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.edit.vscode", "icon.opcodex.glyph.editor-external"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.keybinding", "icon.opcodex.glyph.key"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.manage", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.new", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.provider", "icon.opcodex.glyph.terminal"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.rename", "icon.opcodex.glyph.rename"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.run", "icon.run"));
			theme.addIcon(IconValue.parse("icon.plugin.scriptmanager.run.again", "icon.opcodex.glyph.run-again"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.conflicts", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.defined", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.defined.partial", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.provider", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.undefined", "icon.search"));
			theme.addIcon(IconValue.parse("icon.plugin.stringtable.words", "icon.opcodex.glyph.filter"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltable.data.from", "icon.opcodex.glyph.variable"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltable.instructions.from", "icon.opcodex.glyph.text"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltable.provider", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltable.references.to", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltable.referencetable.provider", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.goto", "icon.base.search.marker"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.classes.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.classes.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.exports.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.exports.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.function.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.function.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.imports.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.imports.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.label.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.label.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.namespace.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.category.namespace.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.class", "icon.opcodex.glyph.cube"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.code", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.code.external", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.code.pinned", "icon.base.pinned"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.function", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.function.external", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.function.thunk", "icon.opcodex.glyph.link"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.group.folder.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.group.folder.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.library", "icon.opcodex.glyph.library"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.local.variable", "icon.opcodex.glyph.variable"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.namespace", "icon.opcodex.glyph.braces"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.organization.closed", "icon.opcodex.glyph.folder"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.organization.open", "icon.opcodex.glyph.folder-open"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.parameter", "icon.opcodex.glyph.parameter"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.node.root", "icon.opcodex.glyph.graph"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.provider", "icon.opcodex.glyph.tree"));
			theme.addIcon(IconValue.parse("icon.plugin.symboltree.set.external", "icon.base.edit.bytes"));
			theme.addIcon(IconValue.parse("icon.plugin.table.service", "icon.search"));
			theme.addIcon(IconValue.parse("icon.plugin.table.service.marker", "icon.base.search.marker"));
			theme.addIcon(IconValue.parse("icon.plugin.terminal.terminate", "icon.delete"));
			theme.addIcon(IconValue.parse("icon.plugin.totd.provider", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.plugin.viewstrings.provider", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.checked.out", "icon.check"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.checked.out.exclusive", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.hijacked", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.not.latest", "icon.opcodex.glyph.history"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.read.only", "icon.opcodex.glyph.user [size(8,8)]"));
			theme.addIcon(IconValue.parse("icon.project.data.file.ghidra.unsupported", "icon.opcodex.glyph.file"));
			theme.addIcon(IconValue.parse("icon.project.info.convert", "icon.opcodex.glyph.wand"));
			theme.addIcon(IconValue.parse("icon.project.root.repo.connected", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.project.root.repo.connected.overlay", "EMPTY_ICON[size(18,16)]{icon.project.root.repo.connected[move(12,8)]}"));
			theme.addIcon(IconValue.parse("icon.project.root.repo.disconnected", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.project.root.repo.disconnected.overlay", "EMPTY_ICON[size(18,16)]{icon.project.root.repo.disconnected[move(12,8)]}"));
			theme.addIcon(IconValue.parse("icon.project.users.in.project", "icon.opcodex.glyph.user"));
			theme.addIcon(IconValue.parse("icon.projectdata.copy", "icon.copy"));
			theme.addIcon(IconValue.parse("icon.projectdata.cut", "icon.cut"));
			theme.addIcon(IconValue.parse("icon.projectdata.delete", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.projectdata.find.checkouts.search", "icon.search {icon.check}"));
			theme.addIcon(IconValue.parse("icon.projectdata.new.folder", "icon.folder.new"));
			theme.addIcon(IconValue.parse("icon.projectdata.paste", "icon.paste"));
			theme.addIcon(IconValue.parse("icon.projectdata.rename", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.projectdata.version.control.update", "icon.opcodex.glyph.merge"));
			theme.addIcon(IconValue.parse("icon.properties", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.provider.clone", "icon.opcodex.glyph.camera"));
			theme.addIcon(IconValue.parse("icon.pulldown", "icon.opcodex.glyph.chevron-down"));
			theme.addIcon(IconValue.parse("icon.redo", "icon.opcodex.glyph.redo"));
			theme.addIcon(IconValue.parse("icon.refresh", "icon.opcodex.glyph.refresh"));
			theme.addIcon(IconValue.parse("icon.rename", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.right", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.right.alt", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.run", "icon.opcodex.glyph.play"));
			theme.addIcon(IconValue.parse("icon.sample.action.hello.world", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.sample.action.show.graph", "icon.opcodex.glyph.code"));
			theme.addIcon(IconValue.parse("icon.sample.graph.dependency.layout", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.sample.hello.world.action.clear", "icon.opcodex.glyph.cut"));
			theme.addIcon(IconValue.parse("icon.sample.kitchen.sink.action.hello.program", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.sample.kitchen.sink.action.hello.world", "icon.opcodex.glyph.left"));
			theme.addIcon(IconValue.parse("icon.sample.plugin.action.save", "icon.opcodex.glyph.save"));
			theme.addIcon(IconValue.parse("icon.sample.plugin.action.show.options", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.sample.provider", "icon.opcodex.glyph.cut"));
			theme.addIcon(IconValue.parse("icon.sample.provider.graph", "icon.opcodex.glyph.grid"));
			theme.addIcon(IconValue.parse("icon.sample.provider.hello.world", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.save", "icon.opcodex.glyph.save"));
			theme.addIcon(IconValue.parse("icon.save.as", "icon.opcodex.glyph.save"));
			theme.addIcon(IconValue.parse("icon.search", "icon.opcodex.glyph.search"));
			theme.addIcon(IconValue.parse("icon.sort.ascending", "icon.opcodex.glyph.sort-up"));
			theme.addIcon(IconValue.parse("icon.sort.descending", "icon.opcodex.glyph.sort-down"));
			theme.addIcon(IconValue.parse("icon.spreadsheet", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.stop", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.subtract", "icon.opcodex.glyph.minus"));
			theme.addIcon(IconValue.parse("icon.table", "icon.opcodex.glyph.table"));
			theme.addIcon(IconValue.parse("icon.task.progress.1", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.2", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.3", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.4", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.5", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.6", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.7", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.1", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.10", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.11", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.2", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.3", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.4", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.5", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.6", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.7", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.8", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.task.progress.hourglass.9", "icon.opcodex.glyph.clock"));
			theme.addIcon(IconValue.parse("icon.text.field.clear", "icon.opcodex.glyph.filter-clear[size(12,12)]"));
			theme.addIcon(IconValue.parse("icon.theme.export", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.theme.font.decrement", "icon.opcodex.glyph.text{icon.opcodex.glyph.minus[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.theme.font.increment", "icon.opcodex.glyph.text{icon.opcodex.glyph.plus[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.theme.import", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.toggle.collapse", "icon.opcodex.glyph.collapse"));
			theme.addIcon(IconValue.parse("icon.toggle.expand", "icon.opcodex.glyph.expand"));
			theme.addIcon(IconValue.parse("icon.undo", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.up", "icon.opcodex.glyph.up"));
			theme.addIcon(IconValue.parse("icon.version.control.add", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.version.control.check.in", "icon.opcodex.glyph.export"));
			theme.addIcon(IconValue.parse("icon.version.control.check.out", "icon.opcodex.glyph.import"));
			theme.addIcon(IconValue.parse("icon.version.control.check.out.undo", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.version.control.dialog.add", "icon.version.control.add"));
			theme.addIcon(IconValue.parse("icon.version.control.dialog.check.in", "icon.version.control.check.in"));
			theme.addIcon(IconValue.parse("icon.version.control.hijack.undo", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.version.tracking.accept.match", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.accept.match", "icon.version.tracking.accept.match"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.add.to.session", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.choose.tag", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.clear.match", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.accept.and.apply.manual.match", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.and.accept.manual.match", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.implied.match", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.manual.match", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.selection", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.create.session", "icon.opcodex.glyph.branch"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.edit.all.tags", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.edit.markup.address", "icon.opcodex.glyph.edit"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.function.filter.not.accepted", "icon.flag{icon.opcodex.glyph.error[size(10,10)][move(6,6)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.markup.reset", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.apply.blocked", "icon.lock{icon.checkmark.green[size(12,12)][move(4,0)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.one.to.many", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.reject", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.remove", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.select.existing", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.match.tag.remove", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.save.session", "icon.opcodex.glyph.save"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.show.listings", "icon.opcodex.glyph.split"));
			theme.addIcon(IconValue.parse("icon.version.tracking.action.show.settings", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.version.tracking.add", "icon.opcodex.glyph.plus"));
			theme.addIcon(IconValue.parse("icon.version.tracking.auto", "icon.opcodex.glyph.wand"));
			theme.addIcon(IconValue.parse("icon.version.tracking.correlator.status.already.run", "icon.opcodex.glyph.flag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.empty", "EMPTY_ICON"));
			theme.addIcon(IconValue.parse("icon.version.tracking.filter.status.applied", "icon.opcodex.glyph.circle-check"));
			theme.addIcon(IconValue.parse("icon.version.tracking.filter.status.invalid", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.version.tracking.filtered", "icon.opcodex.glyph.lightbulb"));
			theme.addIcon(IconValue.parse("icon.version.tracking.function.filter.all", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.version.tracking.function.filter.unmatched", "icon.opcodex.glyph.filter"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.applied", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.conflict", "icon.opcodex.glyph.compare"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.dont.care", "icon.opcodex.glyph.asterisk"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.dont.know", "icon.opcodex.glyph.help"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.failed", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.version.tracking.markup.status.rejected", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.applied", "icon.opcodex.glyph.circle-check[size(8,8)][move(9,4)]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.disabled", "icon.opcodex.glyph.circle-check[size(8,8)][disabled]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.error", "icon.opcodex.glyph.error[size(8,8)][move(36,4)]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.ignored", "icon.opcodex.glyph.circle-minus[size(8,8)][move(27,4)]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.not.applied", "icon.opcodex.glyph.circle[size(8,8)][move(0,4)]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.markup.status.rejected", "icon.opcodex.glyph.error[size(8,8)][move(18,4)]"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.selection.track.match", "icon.opcodex.glyph.selection"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.selection.track.none", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.selection.track.row", "icon.opcodex.glyph.settings"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.accepted.error", "icon.opcodex.glyph.flag {icon.opcodex.glyph.trash[size(8,8)][move(10,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.accepted.fully.applied", "icon.opcodex.glyph.flag {icon.opcodex.glyph.check[size(8,8)][move(10,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.accepted.fully.considered", "icon.opcodex.glyph.flag {icon.opcodex.glyph.circle-check[size(8,8)][move(10,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.accepted.some.unexamined", "icon.opcodex.glyph.flag {icon.opcodex.glyph.warning[move(10,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.blocked", "icon.opcodex.glyph.lock"));
			theme.addIcon(IconValue.parse("icon.version.tracking.match.table.status.rejected", "icon.opcodex.glyph.error"));
			theme.addIcon(IconValue.parse("icon.version.tracking.new.session.info", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.version.tracking.new.session.swap", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.version.tracking.package", "icon.opcodex.glyph.branch"));
			theme.addIcon(IconValue.parse("icon.version.tracking.provider.function", "icon.opcodex.glyph.function"));
			theme.addIcon(IconValue.parse("icon.version.tracking.provider.implied.match", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.provider.markup", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.provider.one.to.many", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.provider.related.matches", "icon.opcodex.glyph.user"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.accepted", "icon.opcodex.glyph.check"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.available", "icon.opcodex.glyph.stop"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.callee", "icon.opcodex.glyph.right"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.caller", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.locked.out", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.target", "icon.opcodex.glyph.user"));
			theme.addIcon(IconValue.parse("icon.version.tracking.related.match.unrelated", "icon.opcodex.glyph.user"));
			theme.addIcon(IconValue.parse("icon.version.tracking.replace", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.version.tracking.replaced", "icon.opcodex.glyph.swap"));
			theme.addIcon(IconValue.parse("icon.version.tracking.session.content.type", "icon.opcodex.glyph.branch"));
			theme.addIcon(IconValue.parse("icon.version.tracking.subtract", "icon.opcodex.glyph.minus"));
			theme.addIcon(IconValue.parse("icon.version.tracking.table.renderer.multiple.symbols", "icon.opcodex.glyph.list"));
			theme.addIcon(IconValue.parse("icon.version.tracking.tag.button.undo", "icon.opcodex.glyph.undo"));
			theme.addIcon(IconValue.parse("icon.version.tracking.tag.status.deleted", "icon.opcodex.glyph.tag{icon.opcodex.glyph.close[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.tag.status.existing", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.version.tracking.tag.status.new", "icon.opcodex.glyph.tag{icon.opcodex.glyph.plus[size(8,8)][move(8,8)]}"));
			theme.addIcon(IconValue.parse("icon.version.tracking.unfiltered", "icon.opcodex.glyph.lightbulb"));
			theme.addIcon(IconValue.parse("icon.warning", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.warning.strong", "icon.opcodex.glyph.warning"));
			theme.addIcon(IconValue.parse("icon.widget.filterpanel.filter.off", "icon.opcodex.glyph.filter-off"));
			theme.addIcon(IconValue.parse("icon.widget.filterpanel.filter.on", "icon.opcodex.glyph.filter"));
			theme.addIcon(IconValue.parse("icon.widget.imagepanel.reset", "icon.opcodex.glyph.tag"));
			theme.addIcon(IconValue.parse("icon.widget.imagepanel.zoom.in", "icon.zoom.in"));
			theme.addIcon(IconValue.parse("icon.widget.imagepanel.zoom.out", "icon.zoom.out"));
			theme.addIcon(IconValue.parse("icon.widget.pathmanager.reset", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.widget.table.delete.row", "icon.opcodex.glyph.trash"));
			theme.addIcon(IconValue.parse("icon.widget.table.header.help", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.widget.table.header.help.hovered", "icon.opcodex.glyph.info"));
			theme.addIcon(IconValue.parse("icon.widget.table.header.pending", "icon.pending"));
			theme.addIcon(IconValue.parse("icon.widget.tabs.close", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.widget.tabs.close.highlight", "icon.opcodex.glyph.close"));
			theme.addIcon(IconValue.parse("icon.widget.tabs.empty.small", "EMPTY_ICON[size(8,16)]"));
			theme.addIcon(IconValue.parse("icon.widget.tabs.list", "icon.opcodex.glyph.down"));
			theme.addIcon(IconValue.parse("icon.window", "icon.opcodex.glyph.monitor"));
			theme.addIcon(IconValue.parse("icon.zoom.in", "icon.opcodex.glyph.zoom-in"));
			theme.addIcon(IconValue.parse("icon.zoom.out", "icon.opcodex.glyph.zoom-out"));
			theme.addIcon(IconValue.parse("icon.filter.tree.contains", "icon.opcodex.glyph.filter-tree-contains"));
			theme.addIcon(IconValue.parse("icon.filter.tree.exact", "icon.opcodex.glyph.filter-tree-exact"));
			theme.addIcon(IconValue.parse("icon.filter.tree.starts", "icon.opcodex.glyph.filter-tree-starts"));
			theme.addIcon(IconValue.parse("icon.filter.tree.regex", "icon.opcodex.glyph.filter-tree-regex"));
		} catch (java.text.ParseException error) {
			throw new IllegalStateException("Invalid generated Opcodex icon binding", error);
		}
	}
	private static Icon load(String glyph, boolean dark) {
		return new OpcodexImageIcon(glyph, dark);
	}
	static String ink(String glyph, boolean dark) { return switch (glyph) {
		case "warning" -> dark ? "#edbd6c" : "#8a5a0c";
		case "error" -> dark ? "#f18b91" : "#b64248";
		case "check" -> dark ? "#89d7a6" : "#216a43";
		case "circle-check" -> dark ? "#89d7a6" : "#216a43";
		case "breakpoint" -> dark ? "#f18b91" : "#b64248";
		case "breakpoint-mixed" -> dark ? "#edbd6c" : "#8a5a0c";
		case "breakpoint-pending" -> dark ? "#edbd6c" : "#8a5a0c";
		case "record" -> dark ? "#f18b91" : "#b64248";
		case "asterisk" -> dark ? "#edbd6c" : "#8a5a0c";
		default -> dark ? "#c8cdd3" : "#4b5563";
	}; }
}

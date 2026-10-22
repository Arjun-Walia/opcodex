/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking;

import java.util.HashMap;
import java.util.Map;
import org.jdom2.Element;
import generic.theme.Gui;
import generic.theme.ThemeListener;
import generic.theme.ThemeManager;
import generic.theme.builtin.GhidraClassicTheme;
import ghidra.util.Msg;
import ghidra.util.Swing;
import ghidra.util.xml.XmlUtilities;
import resources.ResourceManager;

/** Keeps each layout independently so theme changes never discard the user's arrangement. */
final class OpcodexWorkspaceLayouts {
    private final DockingWindowManager manager;
    private final Map<String, Element> layouts = new HashMap<>();
    private final ThemeListener listener = event -> {
        if (event.isLookAndFeelChanged()) Swing.runLater(this::themeChanged);
    };
    private String mode = currentMode();
    private boolean loaded;
    private boolean disposed;

    OpcodexWorkspaceLayouts(DockingWindowManager manager) {
        this.manager = manager;
        Gui.addThemeListener(listener);
    }
    private static String currentMode() {
        return ThemeManager.getInstance() instanceof generic.theme.ApplicationThemeManager themes &&
            themes.getActiveTheme() instanceof GhidraClassicTheme
            ? "classic" : "modern";
    }
    boolean isLoaded() { return loaded; }
    void dispose() { disposed = true; Gui.removeThemeListener(listener); }
    void save(Element target) {
        layouts.put(mode, manager.saveWindowingDataToXml().clone());
        target.setAttribute("OPCODEX_LAYOUT_MODE", mode);
        for (var entry : layouts.entrySet()) {
            String name = "OPCODEX_" + entry.getKey().toUpperCase(java.util.Locale.ROOT) + "_LAYOUT";
            target.removeChild(name);
            target.addContent(new Element(name).addContent(entry.getValue().clone()));
        }
    }
    Element load(Element source) {
        loaded = true;
        String savedMode = source.getAttributeValue("OPCODEX_LAYOUT_MODE", "modern");
        Element root = source.getChild("ROOT_NODE");
        if (root != null) layouts.put(savedMode, root.clone());
        for (String candidate : new String[] {"classic", "modern"}) {
            Element saved = source.getChild("OPCODEX_" + candidate.toUpperCase(java.util.Locale.ROOT) + "_LAYOUT");
            if (saved != null && saved.getChild("ROOT_NODE") != null)
                layouts.put(candidate, saved.getChild("ROOT_NODE").clone());
        }
        Element wanted = layout(mode);
        if (wanted == null) return source;
        Element result = source.clone();
        result.removeChild("ROOT_NODE");
        result.addContent(wanted.clone());
        return result;
    }
    private Element layout(String wanted) {
        if (layouts.containsKey(wanted)) return layouts.get(wanted);
        // Only CodeBrowser's default geometry differs; other tools retain their stable defaults.
        if (!"CodeBrowser".equals(manager.getTool().getName())) return null;
        String path = wanted.equals("classic") ? "classicTools/CodeBrowser.xml" : "defaultTools/CodeBrowser.tool";
        try {
            var resource = ResourceManager.getResource(path);
            if (resource == null) return null;
            Element template = XmlUtilities.createSecureSAXBuilder(false, false).build(resource).getRootElement();
            if (!wanted.equals("classic")) template = template.getChild("TOOL");
            return template == null ? null : template.getChild("ROOT_NODE");
        } catch (Exception error) {
            Msg.error(this, "Unable to read " + wanted + " workspace layout", error);
            return null;
        }
    }
    private void themeChanged() {
        if (disposed || !loaded) return;
        String wanted = currentMode();
        if (wanted.equals(mode)) return; // Dark/Light preserves all positions without rebuilding.
        layouts.put(mode, manager.saveWindowingDataToXml().clone());
        mode = wanted;
        Element root = layout(wanted);
        if (root != null) manager.restoreWindowDataFromXml(new Element("TOOL").addContent(root.clone()));
    }
}

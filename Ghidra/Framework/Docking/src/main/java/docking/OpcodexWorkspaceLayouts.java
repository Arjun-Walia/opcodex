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
        layouts.put(mode, capture());
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
        // Fresh Debugger defaults use a readable modern layout; customized saved layouts stay intact.
        if (root != null && !(source.getAttribute("OPCODEX_LAYOUT_MODE") == null &&
                "Debugger".equals(manager.getTool().getName()) && matchesDebuggerDefault(root))) {
            layouts.put(savedMode, root.clone());
        }
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
        String tool = manager.getTool().getName();
        if (!java.util.List.of("CodeBrowser", "Debugger").contains(tool)) return null;
        String path = tool.equals("Debugger")
            ? (wanted.equals("classic") ? "defaultTools/Debugger.tool" : "opcodexTools/Debugger.xml")
            : (wanted.equals("classic") ? "classicTools/CodeBrowser.xml" : "defaultTools/CodeBrowser.tool");
        try {
            var resource = ResourceManager.getResource(path);
            if (resource == null) return null;
            Element template = XmlUtilities.createSecureSAXBuilder(false, false).build(resource).getRootElement();
            if (template.getName().equals("TOOL_CONFIG")) template = template.getChild("TOOL");
            return template == null ? null : template.getChild("ROOT_NODE");
        } catch (Exception error) {
            Msg.error(this, "Unable to read " + wanted + " workspace layout", error);
            return null;
        }
    }
    private boolean matchesDebuggerDefault(Element root) {
        try {
            var resource = ResourceManager.getResource("defaultTools/Debugger.tool");
            if (resource == null) return false;
            Element original = XmlUtilities.createSecureSAXBuilder(false, false).build(resource)
                .getRootElement().getChild("TOOL").getChild("ROOT_NODE");
            return sameStructure(root, original);
        } catch (Exception error) {
            Msg.error(this, "Unable to compare the default Debugger layout", error);
            return false;
        }
    }
    private static boolean sameStructure(Element a, Element b) {
        if (b == null || !a.getName().equals(b.getName()) || a.getAttributes().size() != b.getAttributes().size() ||
                a.getChildren().size() != b.getChildren().size()) return false;
        for (var attribute : a.getAttributes()) {
            if (!attribute.getValue().equals(b.getAttributeValue(attribute.getName()))) return false;
        }
        for (int i = 0; i < a.getChildren().size(); i++) {
            if (!sameStructure(a.getChildren().get(i), b.getChildren().get(i))) return false;
        }
        return true;
    }
    private void themeChanged() {
        if (disposed || !loaded) return;
        String wanted = currentMode();
        if (wanted.equals(mode)) return; // Dark/Light preserves all positions without rebuilding.
        layouts.put(mode, capture());
        mode = wanted;
        Element root = layout(wanted);
        if (root != null) manager.restoreWindowDataFromXml(new Element("TOOL").addContent(root.clone()));
    }

    private Element capture() {
        Element root = manager.saveWindowingDataToXml().clone();
        ComponentProvider provider = manager.getActiveComponentProvider();
        if (provider != null) {
            root.setAttribute("FOCUSED_OWNER", provider.getOwner());
            root.setAttribute("FOCUSED_NAME", provider.getName());
            root.setAttribute("FOCUSED_TITLE", provider.getTitle());
        }
        else {
            Element selected = selectedProvider(root);
            if (selected != null) {
                root.setAttribute("FOCUSED_OWNER", selected.getAttributeValue("OWNER"));
                root.setAttribute("FOCUSED_NAME", selected.getAttributeValue("NAME"));
                root.setAttribute("FOCUSED_TITLE", selected.getAttributeValue("TITLE"));
            }
        }
        return root;
    }

    private static Element selectedProvider(Element node) {
        if (node.getName().equals("COMPONENT_NODE")) {
            var entries = node.getChildren("COMPONENT_INFO");
            int top = Integer.parseInt(node.getAttributeValue("TOP_INFO", "0"));
            if (top >= 0 && top < entries.size() && "true".equals(entries.get(top).getAttributeValue("ACTIVE"))) return entries.get(top);
            return entries.stream().filter(entry -> "true".equals(entry.getAttributeValue("ACTIVE"))).findFirst().orElse(null);
        }
        for (Element child : node.getChildren()) {
            Element found = selectedProvider(child);
            if (found != null) return found;
        }
        return null;
    }
}

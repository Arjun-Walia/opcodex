/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import java.util.Map;
import java.util.WeakHashMap;
import org.jdom2.Element;
import docking.DockingWindowManager;
import ghidra.util.Msg;
import ghidra.util.xml.XmlUtilities;
import resources.ResourceManager;

/** Explicit, reversible layout reset for existing projects; never rewrites program data. */
final class OpcodexWorkspaces {
	private static final Map<DockingWindowManager, Element> PREVIOUS = new WeakHashMap<>();

	static boolean hasPrevious(DockingWindowManager manager) { return PREVIOUS.containsKey(manager); }

	static void focus(DockingWindowManager manager) {
		try {
			var resource = ResourceManager.getResource("defaultTools/CodeBrowser.tool");
			if (resource == null) throw new java.io.IOException("Focused workspace resource is missing");
			Element template = XmlUtilities.createSecureSAXBuilder(false, false).build(resource)
				.getRootElement().getChild("TOOL");
			if (template == null || template.getChild("ROOT_NODE") == null)
				throw new java.io.IOException("Focused workspace resource is incomplete");
			PREVIOUS.put(manager, snapshot(manager));
			manager.restoreWindowDataFromXml(template);
		} catch (Exception exception) {
			Msg.showError(OpcodexWorkspaces.class, manager.getRootFrame(), "Workspace unavailable",
				"Opcodex could not restore the focused workspace.", exception);
		}
	}

	static void previous(DockingWindowManager manager) {
		Element previous = PREVIOUS.get(manager);
		if (previous == null) return;
		PREVIOUS.put(manager, snapshot(manager));
		manager.restoreWindowDataFromXml(previous);
	}

	private static Element snapshot(DockingWindowManager manager) {
		return new Element("TOOL").addContent(manager.saveWindowingDataToXml());
	}
}

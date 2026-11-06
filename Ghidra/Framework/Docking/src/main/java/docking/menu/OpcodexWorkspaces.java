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
import docking.OpcodexWorkspaceLayouts;
import ghidra.util.Msg;

/** Explicit, reversible layout reset for existing projects; never rewrites program data. */
final class OpcodexWorkspaces {
	private static final Map<DockingWindowManager, Element> PREVIOUS = new WeakHashMap<>();

	static boolean hasPrevious(DockingWindowManager manager) { return PREVIOUS.containsKey(manager); }

	static void focus(DockingWindowManager manager) {
		try {
			String tool = manager.getTool().getName();
			Element root = OpcodexWorkspaceLayouts.readDefault(tool, false);
			if (root == null)
				throw new java.io.IOException("Focused workspace resource is incomplete");
			Element previous = snapshot(manager);
			for (String attribute : java.util.List.of("X_POS", "Y_POS", "WIDTH", "HEIGHT", "EX_STATE")) {
				String value = previous.getChild("ROOT_NODE").getAttributeValue(attribute);
				if (value != null) root.setAttribute(attribute, value);
			}
			PREVIOUS.put(manager, previous);
			manager.restoreWindowDataFromXml(new Element("TOOL").addContent(root.clone()));
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

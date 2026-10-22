/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import docking.*;
import docking.action.ToggleDockingActionIf;
import docking.action.MultiActionDockingActionIf;
import docking.action.DockingActionIf;
import docking.widgets.EmptyBorderButton;
import docking.widgets.VariableHeightPanel;
import generic.theme.GColor;
import generic.theme.GIcon;
import generic.theme.builtin.OpcodexUi;

/** A focused presentation of existing actions, with the complete command set still available. */
final class OpcodexToolbars {
	private static final Set<String> PRIMARY = Set.of("Save File", "Save Tool", "Save Project",
		"Previous Location in History", "Next Location in History", "Undo", "Redo",
		"Function Graph", "Script Manager", "New Project", "Open Project", "Import File");
	private static final Set<String> LOCAL = Set.of("Refresh", "Refresh Tree", "Copy");

	static VariableHeightPanel create(List<ToolBarItemManager> items,
			DockingWindowManager manager, boolean local) {
		VariableHeightPanel bar = new VariableHeightPanel(true, 2, 0);
		bar.setBorder(BorderFactory.createEmptyBorder(local ? 0 : 7, local ? 0 : 10,
			local ? 0 : 7, local ? 0 : 10));
		populate(bar, items, manager, local, true);
		return bar;
	}

	private static void populate(VariableHeightPanel bar, List<ToolBarItemManager> items,
			DockingWindowManager manager, boolean local, boolean expanded) {
		bar.removeAll();
		Set<String> visible = local ? LOCAL : PRIMARY;
		List<ToolBarItemManager> hidden = items.stream()
			.filter(item -> !expanded && !isPrimary(item, visible)).toList();
		for (ToolBarItemManager item : items) {
			if (expanded || isPrimary(item, visible)) bar.add(item.getButton());
		}
		if (!local && items.stream().anyMatch(item ->
			item.getAction().getName().equals("Previous Location in History")) &&
			manager != null && manager.getTool() != null &&
			manager.getTool().getAllActions().stream()
				.anyMatch(action -> action.getName().equals("Go To Address/Label"))) {
			bar.add(Box.createHorizontalStrut(16));
			JButton navigate = new JButton("Go to symbol or address…", new GIcon("icon.search"));
			navigate.setName("Opcodex Go To");
			navigate.setToolTipText("Go to a function, symbol or address (G)");
			navigate.setHorizontalAlignment(SwingConstants.LEFT);
			navigate.setPreferredSize(new Dimension(270, 32));
			navigate.addActionListener(event -> manager.getTool().getAllActions().stream()
				.filter(action -> action.getName().equals("Go To Address/Label"))
				.findFirst().ifPresent(action -> DockingActionPerformer.perform(action,
					new ActionEvent(navigate, ActionEvent.ACTION_PERFORMED, action.getName()), manager)));
			bar.add(navigate);
		}
		if (!hidden.isEmpty() || expanded) {
			EmptyBorderButton more = new EmptyBorderButton(OpcodexUi.sized(new MoreIcon(), 20));
			more.setName(local ? "More panel actions" : "More workspace actions");
			more.setToolTipText(more.getName() + " · includes complete toolbar option");
			more.getAccessibleContext().setAccessibleName(more.getName());
			more.addActionListener(event -> {
				JPopupMenu menu = new JPopupMenu();
				Map<String, JMenu> groups = new LinkedHashMap<>();
				for (ToolBarItemManager item : hidden) {
					JButton source = item.getButton();
					JMenuItem entry = effectiveAction(item.getAction()) instanceof ToggleDockingActionIf
						? new JCheckBoxMenuItem(item.getAction().getName(), source.isSelected())
						: new JMenuItem(item.getAction().getName());
					entry.setEnabled(source.isEnabled());
					entry.addActionListener(click -> source.doClick(0));
					var data = item.getAction().getMenuBarData();
					String group = data != null && data.getMenuPath().length > 1
						? data.getMenuPath()[0].replace("&", "") : "Other actions";
					JMenu section = groups.computeIfAbsent(group, name -> {
						JMenu created = new JMenu(name);
						menu.add(created);
						return created;
					});
					section.add(entry);
				}
				if (!hidden.isEmpty()) menu.addSeparator();
				JCheckBoxMenuItem complete = new JCheckBoxMenuItem("Show complete toolbar", expanded);
				complete.addActionListener(click -> {
					populate(bar, items, manager, local, !expanded);
					bar.revalidate();
					bar.repaint();
				});
				menu.add(complete);
				if (!local && manager != null && manager.getTool() != null &&
					manager.getTool().getName().equals("CodeBrowser")) {
					menu.addSeparator();
					JMenuItem focused = new JMenuItem("Restore focused workspace");
					focused.addActionListener(click -> OpcodexWorkspaces.focus(manager));
					menu.add(focused);
					JMenuItem previous = new JMenuItem("Restore previous workspace");
					previous.setEnabled(OpcodexWorkspaces.hasPrevious(manager));
					previous.addActionListener(click -> OpcodexWorkspaces.previous(manager));
					menu.add(previous);
				}
				menu.show(more, 0, more.getHeight());
			});
			bar.add(more);
		}
	}

	private static boolean isPrimary(ToolBarItemManager item, Set<String> visible) {
		// Split controls own additional commands and state choices. Keep the real button
		// visible so its complete original dropdown and context behavior remain available.
		return visible.contains(item.getAction().getName()) ||
			effectiveAction(item.getAction()) instanceof MultiActionDockingActionIf;
	}
	private static DockingActionIf effectiveAction(DockingActionIf action) {
		// Window proxies implement optional interfaces even when their delegate does not.
		while (action instanceof DockingActionProxy proxy) action = proxy.getProxyAction();
		return action;
	}

	private static final class MoreIcon implements Icon {
		@Override public int getIconWidth() { return 20; }
		@Override public int getIconHeight() { return 20; }
		@Override public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D canvas = (Graphics2D) g.create();
			try {
				canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				canvas.setColor(new GColor("system.color.fg.control"));
				for (int i = 0; i < 3; i++) canvas.fillOval(x + 3 + i * 6, y + 9, 2, 2);
			} finally { canvas.dispose(); }
		}
	}
}

/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package ghidra.framework.main;

import java.awt.*;

import javax.swing.*;

import generic.theme.builtin.OpcodexUi;

/** Arranges the existing project explorer and tool chest without replacing their actions. */
final class OpcodexProjectWorkspace extends JPanel {
	private final ProjectDataPanel explorer;
	private final ProjectToolBar tools;
	private final JPanel explorerCard = card("Project Explorer", null);
	private final JPanel launcherCard = card("Tool Launcher", "Open a tool for this project.");
	private final JScrollPane launcherScroll = new JScrollPane();
	private final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	private Boolean modern;
	private int dividerLocation = 260;

	OpcodexProjectWorkspace(ProjectDataPanel explorer, ProjectToolBar tools) {
		super(new BorderLayout());
		this.explorer = explorer;
		this.tools = tools;
		launcherScroll.setBorder(BorderFactory.createEmptyBorder());
		launcherScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		launcherCard.add(launcherScroll, BorderLayout.CENTER);
		explorerCard.setMinimumSize(new Dimension(170, 120));
		launcherCard.setMinimumSize(new Dimension(200, 120));
		split.setLeftComponent(explorerCard);
		split.setRightComponent(launcherCard);
		split.setBorder(BorderFactory.createEmptyBorder());
		split.setDividerSize(9);
		split.setContinuousLayout(true);
		arrange();
	}

	@Override
	public void updateUI() {
		super.updateUI();
		if (explorer != null) {
			SwingUtilities.invokeLater(this::arrange);
		}
	}

	private void arrange() {
		boolean useCards = OpcodexUi.active();
		if (modern != null && modern == useCards) {
			return;
		}
		if (Boolean.TRUE.equals(modern)) {
			dividerLocation = split.getDividerLocation();
		}
		modern = useCards;
		removeAll();
		if (useCards) {
			explorerCard.add(explorer, BorderLayout.CENTER);
			launcherScroll.setViewportView(tools);
			add(split, BorderLayout.CENTER);
			split.setDividerLocation(dividerLocation);
		}
		else {
			launcherScroll.setViewportView(null);
			add(tools, BorderLayout.NORTH);
			add(explorer, BorderLayout.CENTER);
		}
		revalidate();
		repaint();
	}

	private static JPanel card(String title, String description) {
		JPanel result = new JPanel(new BorderLayout(0, 5));
		result.setName(title);
		result.setBorder(OpcodexUi.surfaceBorder(12, 6));
		JPanel header = new JPanel(new BorderLayout(0, 4));
		header.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 8));
		JLabel heading = new JLabel(title);
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, heading.getFont().getSize2D() + 2));
		header.add(heading, BorderLayout.NORTH);
		if (description != null) {
			header.add(new JLabel(description), BorderLayout.SOUTH);
		}
		result.add(header, BorderLayout.NORTH);
		return result;
	}
}

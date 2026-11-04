/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import docking.widgets.EmptyBorderButton;
import generic.theme.GColor;
import generic.theme.builtin.OpcodexUi;

/** Direct navigation to the tool's existing providers; no duplicate plugin or workspace state. */
final class OpcodexWorkspaceRail extends JPanel {
	private final DockingWindowManager manager;
	private final PropertyChangeListener focusListener = event -> refreshFocus();
	private final JPanel shortcuts = new JPanel();
	private final java.util.Map<AbstractButton, String> targets = new java.util.LinkedHashMap<>();
	private String providerSignature = "";

	OpcodexWorkspaceRail(DockingWindowManager manager) {
		super(new BorderLayout());
		this.manager = manager;
		setName("Opcodex workspace navigation");
		getAccessibleContext().setAccessibleName("Workspace navigation");
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 0, 1, new GColor("system.color.bg.border")),
			BorderFactory.createEmptyBorder(8, 5, 8, 5)));
		shortcuts.setLayout(new BoxLayout(shortcuts, BoxLayout.Y_AXIS));
		add(shortcuts, BorderLayout.NORTH);
		EmptyBorderButton all = button("All panels", "grid", () -> showPanels(null));
		add(all, BorderLayout.SOUTH);
	}

	@Override public void addNotify() {
		super.addNotify();
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", focusListener);
	}
	@Override public void removeNotify() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner", focusListener);
		super.removeNotify();
	}
	@Override public void updateUI() {
		super.updateUI();
		if (manager != null) refresh();
	}

	void refresh() {
		boolean available = OpcodexUi.active() && manager.getTool() != null &&
			List.of("CodeBrowser", "Debugger", "Emulator").contains(manager.getTool().getName());
		setVisible(available);
		if (!available) return;
		String signature = providers().stream().map(provider -> provider.getOwner() + ":" + provider.getName())
			.sorted().collect(java.util.stream.Collectors.joining("|"));
		if (!signature.equals(providerSignature)) {
			providerSignature = signature;
			shortcuts.removeAll(); targets.clear();
			addShortcut("Listing", "listing", "Listing");
			addShortcut("Functions", "function", "Functions Window");
			addShortcut("Symbols", "tree", "Symbol Tree");
			addShortcut("Types", "cube", "DataTypes Provider");
			addShortcut("Graph", "graph", "Function Graph");
			addShortcut("Scripts", "code", "Script Manager");
			addShortcut("Console", "terminal", "Console");
			shortcuts.revalidate();
		}
		refreshFocus();
	}

	private void addShortcut(String label, String glyph, String name) {
		if (providers().stream().noneMatch(provider -> provider.getName().equals(name))) return;
		EmptyBorderButton button = button(label, glyph, () -> showPanels(name));
		targets.put(button, name);
		shortcuts.add(button);
		shortcuts.add(Box.createVerticalStrut(6));
	}
	private EmptyBorderButton button(String label, String glyph, Runnable action) {
		// Theme-aware GIcons resolve the correct geometry/palette after a theme switch.
		var button = new EmptyBorderButton(OpcodexUi.sized(new generic.theme.GIcon("icon.opcodex.glyph." + glyph), 20));
		button.setName("Workspace: " + label);
		button.setPreferredSize(new Dimension(40, 40));
		button.setMaximumSize(new Dimension(40, 40));
		button.setToolTipText("<html><b>" + label + "</b><br>" +
			(label.equals("All panels") ? "Open any panel in this workspace." : "Show and focus the " + label + " panel.") + "</html>");
		button.getAccessibleContext().setAccessibleName(label);
		button.addActionListener(event -> action.run());
		return button;
	}
	private List<ComponentProvider> providers() {
		return manager.getComponentProviders(ComponentProvider.class).stream()
			.sorted(Comparator.comparing(ComponentProvider::getName).thenComparing(ComponentProvider::getOwner))
			.toList();
	}
	private void showPanels(String name) {
		if (name == null) { showPanelPicker(); return; }
		List<ComponentProvider> choices = providers().stream()
			.filter(provider -> provider.getName().equals(name)).toList();
		if (name != null && choices.size() == 1) { open(choices.get(0)); return; }
		JPopupMenu menu = new JPopupMenu("Workspace panels");
		for (ComponentProvider provider : choices) {
			JMenuItem entry = new JMenuItem(provider.getTitle());
			entry.addActionListener(event -> open(provider));
			menu.add(entry);
		}
		menu.show(this, getWidth(), name == null ? Math.max(0, getHeight() - menu.getPreferredSize().height) : 0);
	}
	private void showPanelPicker() {
		List<ComponentProvider> choices = providers();
		JPopupMenu popup = new JPopupMenu();
		JPanel content = new JPanel(new BorderLayout(0, 8));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		JTextField search = new JTextField();
		search.setName("Find workspace panel");
		search.putClientProperty("JTextField.placeholderText", "Find a panel…");
		search.getAccessibleContext().setAccessibleName("Find a panel");
		DefaultListModel<ComponentProvider> model = new DefaultListModel<>();
		JList<ComponentProvider> list = new JList<>(model);
		list.setName("Workspace panel results");
		list.getAccessibleContext().setAccessibleName("Workspace panels");
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFixedCellHeight(30);
		list.setCellRenderer(new DefaultListCellRenderer() {
			@Override public Component getListCellRendererComponent(JList<?> source, Object value,
					int index, boolean selected, boolean focused) {
				ComponentProvider provider = (ComponentProvider) value;
				return super.getListCellRendererComponent(source,
					provider.getTitle(), index, selected, focused);
			}
		});
		Runnable filter = () -> {
			String query = search.getText().strip().toLowerCase(java.util.Locale.ROOT);
			model.clear();
			for (ComponentProvider provider : choices) {
				String text = (provider.getTitle() + " " + provider.getName() + " " + provider.getOwner())
					.toLowerCase(java.util.Locale.ROOT);
				if (text.contains(query)) model.addElement(provider);
			}
			if (!model.isEmpty()) list.setSelectedIndex(0);
		};
		search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
			public void insertUpdate(javax.swing.event.DocumentEvent e) { filter.run(); }
			public void removeUpdate(javax.swing.event.DocumentEvent e) { filter.run(); }
			public void changedUpdate(javax.swing.event.DocumentEvent e) { filter.run(); }
		});
		Runnable choose = () -> {
			ComponentProvider provider = list.getSelectedValue();
			if (provider != null) { popup.setVisible(false); open(provider); }
		};
		search.addActionListener(event -> choose.run());
		list.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "open-panel");
		list.getActionMap().put("open-panel", new AbstractAction() {
			@Override public void actionPerformed(java.awt.event.ActionEvent e) { choose.run(); }
		});
		search.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "panel-results");
		search.getActionMap().put("panel-results", new AbstractAction() {
			@Override public void actionPerformed(java.awt.event.ActionEvent e) { list.requestFocusInWindow(); }
		});
		list.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override public void mouseClicked(java.awt.event.MouseEvent event) {
				int index = list.locationToIndex(event.getPoint());
				if (SwingUtilities.isLeftMouseButton(event) && index >= 0 &&
					list.getCellBounds(index, index).contains(event.getPoint())) choose.run();
			}
		});
		content.add(search, BorderLayout.NORTH);
		content.add(new JScrollPane(list), BorderLayout.CENTER);
		content.add(new JLabel("Enter to open · Esc to close"), BorderLayout.SOUTH);
		Rectangle screen = getGraphicsConfiguration().getBounds();
		content.setPreferredSize(new Dimension(Math.min(390, screen.width - getWidth()), Math.min(360, Math.max(180, getHeight() - 24))));
		popup.add(content);
		filter.run();
		popup.show(this, getWidth(), Math.max(0, getHeight() - content.getPreferredSize().height - 12));
		SwingUtilities.invokeLater(search::requestFocusInWindow);
	}
	private void open(ComponentProvider provider) {
		manager.showComponent(provider, true);
		provider.requestFocus();
		refreshFocus();
	}
	private void refreshFocus() {
		Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
		List<ComponentProvider> available = providers();
		for (var entry : targets.entrySet()) {
			entry.getKey().setSelected(focus != null && available.stream().anyMatch(provider ->
				provider.getName().equals(entry.getValue()) && provider.getComponent() != null &&
				SwingUtilities.isDescendingFrom(focus, provider.getComponent())));
		}
	}
}

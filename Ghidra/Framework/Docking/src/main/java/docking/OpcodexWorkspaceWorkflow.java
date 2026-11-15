/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package docking;

import java.awt.*;
import javax.swing.*;
import docking.actions.dialog.ActionChooserDialog;
import generic.theme.GColor;
import generic.theme.builtin.OpcodexUi;

/** A short workflow guide backed by the existing commands and providers. */
final class OpcodexWorkspaceWorkflow extends JPanel {
    private final DockingWindowManager manager;
    private final JLabel heading = new JLabel();
    private final JPanel steps = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
    private String configuredTool;

    OpcodexWorkspaceWorkflow(DockingWindowManager manager) {
        super(new BorderLayout(12, 6));
        this.manager = manager;
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new GColor("system.color.bg.border")),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        add(heading, BorderLayout.WEST);
        add(steps, BorderLayout.CENTER);
    }

    private void configure(String tool) {
        configuredTool = tool;
        heading.setText(tool);
        setName("Opcodex " + tool.toLowerCase(java.util.Locale.ROOT) + " workflow");
        steps.removeAll();
        switch (tool) {
            case "Debugger" -> {
                steps.add(step("1  Launch or connect", "Find a launcher or connection command.", () -> search("launch")));
                steps.add(step("2  Choose a thread", "Inspect threads and choose where to step.", () -> open("Threads")));
                steps.add(step("3  Inspect state", "Open Registers and the related inspection tabs.", () -> open("Registers")));
            }
            case "Emulator" -> {
                steps.add(step("1  Start a trace", "Find the command to emulate the open program.", () -> search("Emulate Program in new Trace")));
                steps.add(step("2  Follow execution", "Inspect the emulated threads and their current addresses.", () -> open("Threads")));
                steps.add(step("3  Inspect state", "Open registers for the selected thread.", () -> open("Registers")));
            }
            case "Version Tracking" -> {
                steps.add(step("1  Set up a session", "Find the command to create a comparison session.", () -> search("Create New Session")));
                steps.add(step("2  Review matches", "Open the list of matches between programs.", () -> open("Version Tracking Matches")));
                steps.add(step("3  Review markup", "Inspect markup for the selected match.", () -> open("Version Tracking Markup Items")));
            }
        }
    }

    private void search(String query) {
        ComponentProvider provider = manager.getActiveComponentProvider();
        ActionContext context = provider == null ? new DefaultActionContext() : provider.getActionContext(null);
        manager.getTool().showDialog(ActionChooserDialog.forOpcodex(manager.getTool(), context, query));
    }

    private JButton step(String label, String detail, Runnable action) {
        JButton button = new JButton(label);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setName(configuredTool + " step: " + label);
        button.setToolTipText(detail);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.getAccessibleContext().setAccessibleDescription(detail);
        button.addActionListener(event -> action.run());
        return button;
    }

    private void open(String name) {
        ComponentProvider provider = manager.getComponentProvider(name);
        if (provider != null) {
            manager.showComponent(provider, true);
            provider.requestFocus();
        }
    }

    void refresh() {
        String tool = manager.getTool() == null ? "" : manager.getTool().getName();
        boolean visible = OpcodexUi.active() && (OpcodexWorkspaceLayouts.isTraceWorkspace(tool) || "Version Tracking".equals(tool));
        if (visible && !tool.equals(configuredTool)) {
            configure(tool);
        }
        setVisible(visible);
    }
}

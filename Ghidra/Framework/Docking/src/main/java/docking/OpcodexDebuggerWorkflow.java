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

/** Persistent orientation and direct entry points for the original debugger providers. */
final class OpcodexDebuggerWorkflow extends JPanel {
    private final DockingWindowManager manager;

    OpcodexDebuggerWorkflow(DockingWindowManager manager) {
        super(new BorderLayout(12, 6));
        this.manager = manager;
        setName("Opcodex debugger workflow");
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new GColor("system.color.bg.border")),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        JLabel heading = new JLabel("Debugger");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD));
        add(heading, BorderLayout.WEST);
        JPanel steps = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        steps.add(step("1  Launch or connect", "Find a launcher or connection command for your target.", () -> {
            ComponentProvider provider = manager.getActiveComponentProvider();
            ActionContext context = provider == null ? new DefaultActionContext() : provider.getActionContext(null);
            manager.getTool().showDialog(ActionChooserDialog.forOpcodex(manager.getTool(), context, "launch"));
        }));
        steps.add(step("2  Choose a thread", "Select a target thread. Pause or step using the Execution ribbon group.", () -> open("Threads")));
        steps.add(step("3  Inspect state", "Inspect registers. Memory, breakpoints and watches share the right-hand tabs.", () -> open("Registers")));
        add(steps, BorderLayout.CENTER);
    }

    private JButton step(String label, String detail, Runnable action) {
        JButton button = new JButton(label);
        button.putClientProperty("JButton.buttonType", "toolBarButton");
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        button.setName("Debugger step: " + label);
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
        setVisible(OpcodexUi.active() && manager.getTool() != null && "Debugger".equals(manager.getTool().getName()));
    }
}

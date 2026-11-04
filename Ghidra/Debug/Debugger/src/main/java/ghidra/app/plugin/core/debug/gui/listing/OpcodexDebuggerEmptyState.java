/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package ghidra.app.plugin.core.debug.gui.listing;

import java.awt.*;
import javax.swing.*;
import docking.*;
import docking.actions.dialog.ActionChooserDialog;
import generic.theme.*;
import generic.theme.builtin.OpcodexUi;

/** An actionable empty view, shown only when the dynamic Listing has no trace. */
final class OpcodexDebuggerEmptyState extends JPanel {
    OpcodexDebuggerEmptyState(Tool tool) {
        super(new GridBagLayout());
        setName("Opcodex debugger empty session");
        setBackground(new GColor("system.color.bg.view"));
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel symbol = new JLabel(OpcodexUi.sized(new GIcon("icon.opcodex.glyph.bug"), 36));
        JLabel title = new JLabel("Ready to debug");
        title.setFont(UIManager.getFont("Label.font").deriveFont(Font.BOLD, 20f));
        JLabel detail = new JLabel("Launch a target or connect to a running process.");
        detail.setFont(UIManager.getFont("Label.font").deriveFont(13f));
        JLabel hint = new JLabel("Live code appears here when a trace is active.");
        hint.setFont(UIManager.getFont("Label.font").deriveFont(12f));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actions.setOpaque(false);
        JButton launch = action("Launch target", () -> tool.showDialog(ActionChooserDialog.forOpcodex(tool, new DefaultActionContext(), "launch")));
        launch.setBackground(new GColor("system.color.bg.selected.view"));
        launch.setForeground(new GColor("system.color.fg.selected.view"));
        actions.add(launch);
        actions.add(action("Connections", () -> {
            ComponentProvider provider = tool.getWindowManager().getComponentProvider("Connections");
            if (provider != null) { tool.getWindowManager().showComponent(provider, true); provider.requestFocus(); }
        }));
        for (JComponent component : new JComponent[] {symbol, title, detail, actions, hint}) {
            component.setAlignmentX(CENTER_ALIGNMENT);
            content.add(component);
            content.add(Box.createVerticalStrut(component == symbol ? 18 : component == detail ? 22 : 10));
        }
        add(content);
        getAccessibleContext().setAccessibleDescription("No debugging trace is open. Launch a target or open Connections to begin.");
    }

    private JButton action(String name, Runnable work) {
        JButton button = new JButton(name);
        button.setName("Debugger empty state: " + name);
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(event -> work.run());
        return button;
    }
}

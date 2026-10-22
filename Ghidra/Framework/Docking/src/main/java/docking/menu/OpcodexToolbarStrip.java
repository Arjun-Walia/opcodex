/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** One row at every width; the actual action and split buttons remain in the viewport. */
final class OpcodexToolbarStrip extends JPanel {
    private static final FocusAdapter FOLLOW_FOCUS = new FocusAdapter() {
        @Override public void focusGained(FocusEvent event) {
            Component child = event.getComponent();
            if (child.getParent() instanceof JComponent parent) parent.scrollRectToVisible(child.getBounds());
        }
    };
    private final JPanel commands;
    private final JScrollPane scroll;
    private final JButton previous = arrow(false);
    private final JButton next = arrow(true);

    OpcodexToolbarStrip(JPanel commands, boolean local) {
        super(new BorderLayout(2, 0));
        this.commands = commands;
        setName(local ? "Opcodex panel toolbar" : "Opcodex workspace toolbar");
        setBorder(BorderFactory.createEmptyBorder(local ? 0 : 7, local ? 0 : 10,
            local ? 0 : 7, local ? 0 : 10));
        scroll = new JScrollPane(commands, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setMinimumSize(new Dimension(0, 0));
        scroll.getViewport().addChangeListener(event -> updateArrows());
        scroll.addMouseWheelListener(event -> {
            if (commands.getWidth() <= scroll.getViewport().getWidth()) return;
            move(event.getWheelRotation() * 96);
            event.consume();
        });
        previous.addActionListener(event -> move(-scroll.getViewport().getWidth() / 2));
        next.addActionListener(event -> move(scroll.getViewport().getWidth() / 2));
        add(previous, BorderLayout.WEST);
        add(scroll, BorderLayout.CENTER);
        add(next, BorderLayout.EAST);
        commands.addContainerListener(new ContainerAdapter() {
            @Override public void componentAdded(ContainerEvent event) { followFocus(event.getChild()); }
        });
        for (Component child : commands.getComponents()) followFocus(child);
    }

    private void followFocus(Component child) {
        child.removeFocusListener(FOLLOW_FOCUS);
        child.addFocusListener(FOLLOW_FOCUS);
    }

    private static JButton arrow(boolean forward) {
        JButton button = new JButton(new ScrollIcon(forward));
        button.setDisabledIcon(button.getIcon()); // The painter handles disabled color at device scale.
        String name = forward ? "Scroll toolbar right" : "Scroll toolbar left";
        button.setName(name);
        button.setToolTipText(name + " · mouse wheel also scrolls commands");
        button.getAccessibleContext().setAccessibleName(name);
        button.setPreferredSize(new Dimension(24, 32));
        button.addPropertyChangeListener("enabled", event -> button.setCursor(Cursor.getPredefinedCursor(
            button.isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR)));
        return button;
    }

    private record ScrollIcon(boolean forward) implements Icon {
            @Override public int getIconWidth() { return 12; }
            @Override public int getIconHeight() { return 16; }
            @Override public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D pen = (Graphics2D) g.create();
                try {
                    pen.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    pen.setColor(c.isEnabled() ? c.getForeground() : UIManager.getColor("Label.disabledForeground"));
                    pen.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int start = x + (forward ? 4 : 8), middle = x + (forward ? 8 : 4);
                    pen.drawPolyline(new int[] {start, middle, start}, new int[] {y+4, y+8, y+12}, 3);
                } finally { pen.dispose(); }
            }
    }

    private void move(int distance) {
        JViewport viewport = scroll.getViewport();
        int maximum = Math.max(0, commands.getWidth() - viewport.getWidth());
        int x = Math.max(0, Math.min(maximum, viewport.getViewPosition().x + distance));
        viewport.setViewPosition(new Point(x, 0));
    }

    private void updateArrows() {
        JViewport viewport = scroll.getViewport();
        int x = viewport.getViewPosition().x;
        previous.setEnabled(x > 0);
        next.setEnabled(x + viewport.getWidth() < commands.getWidth());
    }

    @Override public void doLayout() {
        Insets insets = getInsets();
        boolean overflow = commands.getPreferredSize().width > getWidth() - insets.left - insets.right;
        previous.setVisible(overflow);
        next.setVisible(overflow);
        super.doLayout();
        updateArrows();
    }

    @Override public Dimension getMinimumSize() {
        return new Dimension(0, getPreferredSize().height);
    }
}

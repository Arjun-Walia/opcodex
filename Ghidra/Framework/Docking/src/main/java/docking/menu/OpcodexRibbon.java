/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import java.awt.*;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import docking.*;
import docking.action.DockingActionIf;
import docking.actions.dialog.ActionChooserDialog;
import generic.theme.GColor;

/** Labelled command groups, using the original stateful action and split buttons. */
final class OpcodexRibbon {
    private record ButtonStyle(String text, int horizontal, int vertical, Font font, int gap, boolean visible) {}
    private static final String STYLE = "opcodex.ribbon.originalStyle";
    private static final Map<DockingWindowManager, String> SELECTED_TABS = new WeakHashMap<>();

    static JComponent wrap(JPanel commands, DockingWindowManager manager) {
        JPanel ribbon = new JPanel(new BorderLayout());
        ribbon.setName("Opcodex command ribbon");
        OpcodexToolbarStrip strip = new OpcodexToolbarStrip(commands, false);
        ribbon.add(strip, BorderLayout.CENTER);
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new GColor("system.color.bg.border")),
            BorderFactory.createEmptyBorder(4, 10, 4, 12)));
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        Map<String, JToggleButton> choices = new LinkedHashMap<>();
        ButtonGroup selection = new ButtonGroup();
        boolean debugger = manager != null && manager.getTool() != null && "Debugger".equals(manager.getTool().getName());
        String initial = manager == null ? "Home" : SELECTED_TABS.getOrDefault(manager, debugger ? "Debug" : "Home");
        Set<String> categories = java.util.Arrays.stream(commands.getComponents()).map(Component::getName)
            .filter(name -> name != null && name.startsWith("Ribbon group: "))
            .map(name -> name.substring("Ribbon group: ".length())).collect(Collectors.toSet());
        for (String name : List.of("Home", "Navigate", "Analyze", "Panels", "Debug", "All tools")) {
            if (name.equals("Debug") && !debugger) continue;
            if (name.equals("Navigate") && categories.stream().noneMatch(c -> List.of("Navigate", "Selection").contains(c))) continue;
            if (name.equals("Analyze") && categories.stream().noneMatch(c -> List.of("Analyze", "Search", "Select", "Graph").contains(c))) continue;
            if (name.equals("Panels") && categories.stream().noneMatch(c -> visibleOn("Panels", c, "") && !c.equals("Workspace"))) continue;
            JToggleButton tab = new JToggleButton(name);
            tab.setFont(UIManager.getFont("Button.font").deriveFont(13f));
            tab.setName("Ribbon tab: " + name);
            tab.setContentAreaFilled(false);
            tab.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            tab.getAccessibleContext().setAccessibleName(name + " commands");
            tab.addActionListener(event -> commands.putClientProperty("opcodex.ribbon.tab", name));
            selection.add(tab); choices.put(name, tab); tabs.add(tab);
        }
        if (!choices.containsKey(initial)) initial = "Home";
        header.add(tabs, BorderLayout.WEST);
        Runnable update = () -> {
            String selected = (String)commands.getClientProperty("opcodex.ribbon.tab");
            for (var entry : choices.entrySet()) {
                boolean active = entry.getKey().equals(selected);
                entry.getValue().setSelected(active);
                entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, active ? new GColor("color.border.button.focused") : entry.getValue().getBackground()),
                    BorderFactory.createEmptyBorder(5, 11, 5, 11)));
            }
            for (Component component : commands.getComponents()) {
                if (!(component instanceof JPanel section) || section.getName() == null || !section.getName().startsWith("Ribbon group: ")) continue;
                String category = section.getName().substring("Ribbon group: ".length());
                JPanel row = (JPanel)section.getComponent(0);
                boolean any = false;
                for (Component child : row.getComponents()) {
                    boolean visible = visibleOn(selected, category, child.getName());
                    child.setVisible(visible); any |= visible;
                }
                section.setVisible(any);
            }
            if (manager != null) SELECTED_TABS.put(manager, selected);
            commands.revalidate(); strip.revalidate(); strip.repaint();
        };
        commands.addPropertyChangeListener("opcodex.ribbon.tab", event -> update.run());
        commands.putClientProperty("opcodex.ribbon.tab", initial);
        if (manager != null && manager.getTool() != null) {
            JPanel finder = new JPanel(new BorderLayout());
            JTextField search = new JTextField(24);
            search.setName("Find a command");
            search.putClientProperty("JTextField.placeholderText", "Search tools and commands…");
            search.putClientProperty("JTextField.leadingIcon", generic.theme.builtin.OpcodexUi.sized(
                new generic.theme.GIcon("icon.opcodex.glyph.search"), 16));
            search.putClientProperty("JComponent.roundRect", true);
            search.getAccessibleContext().setAccessibleName("Search tools and commands");
            search.setToolTipText("Type a name or purpose, then press Enter. Search includes menu and panel commands.");
            search.addActionListener(event -> {
                ComponentProvider provider = manager.getActiveComponentProvider();
                ActionContext context = provider == null ? new DefaultActionContext() : provider.getActionContext(null);
                ActionChooserDialog dialog = ActionChooserDialog.forOpcodex(manager.getTool(), context, search.getText());
                manager.getTool().showDialog(dialog);
            });
            finder.add(search, BorderLayout.CENTER);
            header.add(finder, BorderLayout.EAST);
        }
        ribbon.add(header, BorderLayout.NORTH);
        ribbon.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        return ribbon;
    }

    static void showComplete(JPanel commands, boolean complete) {
        commands.putClientProperty("opcodex.ribbon.tab", complete ? "All tools" : "Home");
    }

    private static boolean visibleOn(String tab, String category, String command) {
        if (category.equals("Workspace") || tab.equals("All tools")) return true;
        return switch (tab) {
            case "Debug" -> category.equals("Debug session") || category.equals("Execution");
            case "Navigate" -> category.equals("Navigate") || category.equals("Selection");
            case "Analyze" -> List.of("Analyze", "Search", "Select", "Graph").contains(category);
            case "Panels" -> !List.of("Project", "Navigate", "Selection", "Edit", "Analyze", "Search", "Select", "Debug session", "Execution").contains(category);
            default -> category.equals("Project") || category.equals("Edit") || category.equals("Analyze") ||
                (category.equals("Navigate") && List.of("Previous Location in History", "Next Location in History", "Opcodex Go To").contains(command)) ||
                List.of("Function Graph", "Script Manager").contains(command);
        };
    }

    static JPanel group(String name) {
        JPanel group = new JPanel(new BorderLayout(0, 4));
        group.setName("Ribbon group: " + name);
        group.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new GColor("system.color.bg.border")),
            BorderFactory.createEmptyBorder(0, 5, 0, 9)));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        row.setName("Ribbon commands: " + name);
        group.add(row, BorderLayout.CENTER);
        JLabel label = new JLabel(name, SwingConstants.LEADING);
        label.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        label.setFont(label.getFont().deriveFont(Math.max(10f, label.getFont().getSize2D() - 2)));
        group.add(label, BorderLayout.SOUTH);
        return group;
    }

    static void add(JPanel group, JButton button, String caption) {
        if (button.getClientProperty(STYLE) == null) {
            button.putClientProperty(STYLE, new ButtonStyle(button.getText(),
                button.getHorizontalTextPosition(), button.getVerticalTextPosition(), button.getFont(),
                button.getIconTextGap(), button.isVisible()));
        }
        button.setText(caption);
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setIconTextGap(7);
        button.setFont(UIManager.getFont("Button.font").deriveFont(12f));
        ((JPanel) group.getComponent(0)).add(button);
    }

    static void restore(JButton button) {
        if (button.getClientProperty(STYLE) instanceof ButtonStyle original) {
            button.setText(original.text());
            button.setHorizontalTextPosition(original.horizontal());
            button.setVerticalTextPosition(original.vertical());
            button.setFont(original.font() instanceof javax.swing.plaf.UIResource ? UIManager.getFont("Button.font") : original.font());
            button.setIconTextGap(original.gap());
            button.setVisible(original.visible());
            button.putClientProperty(STYLE, null);
        }
    }

    static String category(DockingActionIf action) {
        String text = (action.getName() + " " + action.getOwner()).toLowerCase(Locale.ROOT);
        if (text.contains("selected range") || text.contains("highlight")) return "Selection";
        if (text.contains("step") || text.contains("resume") || text.contains("interrupt") ||
                text.contains("kill") || text.contains("disconnect")) return "Execution";
        if (text.contains("launch") || text.contains("connect") || text.contains("record") ||
                text.contains("debuggercontrol") || text.contains("tracermi")) return "Debug session";
        if (text.contains("undo") || text.contains("redo") || text.contains("clipboard")) return "Edit";
        if (text.contains("history") || text.contains("navigate") || text.contains("go to")) return "Navigate";
        if (text.contains("analy") || text.contains("disassembl")) return "Analyze";
        if (action.getMenuBarData() != null) {
            String top = action.getMenuBarData().getMenuPath()[0].replace("&", "");
            return switch (top) {
                case "File", "Project" -> "Project";
                case "Navigation" -> "Navigate";
                case "Analysis" -> "Analyze";
                case "Window" -> "Panels";
                case "Debugger" -> "Debug session";
                default -> top;
            };
        }
        if (text.contains("save") || text.contains("project") || text.contains("import")) return "Project";
        return "Panels & tools";
    }

    static String caption(String name) {
        return switch (name) {
            case "Previous Location in History" -> "Back";
            case "Next Location in History" -> "Forward";
            case "Save File" -> "Save";
            case "Save Tool" -> "Save tool";
            case "Go To Address/Label" -> "Go to";
            case "Next Selected Range" -> "Next range";
            case "Previous Selected Range" -> "Previous range";
            case "Step Into" -> "Step into";
            case "Step Over" -> "Step over";
            case "Step Out" -> "Step out";
            default -> name.length() <= 18 ? name : name.substring(0, 17).stripTrailing() + "…";
        };
    }
}

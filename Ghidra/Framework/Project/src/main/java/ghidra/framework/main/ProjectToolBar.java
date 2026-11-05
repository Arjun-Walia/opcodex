/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ghidra.framework.main;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import generic.theme.builtin.OpcodexUi;
import ghidra.framework.model.*;
import ghidra.util.HTMLUtilities;

/**
 * Toolbar that shows icons for the tools in the user's tool chest.
 */
class ProjectToolBar extends JToolBar implements ToolChestChangeListener, Scrollable {
	private final static int TYPICAL_NUM_TOOLS = 5;

	private Map<String, ToolButton> toolButtonMap;
	private FrontEndPlugin plugin;
	private FrontEndTool tool;
	private LayoutManager classicLayout;
	private final GridLayout cardLayout = new GridLayout(0, 2, 10, 10);

	ProjectToolBar(FrontEndPlugin plugin) {
		super();
		classicLayout = getLayout();
		if (OpcodexUi.active()) {
			setLayout(cardLayout);
		}
		this.plugin = plugin;
		tool = ((FrontEndTool) plugin.getTool());
		toolButtonMap = new HashMap<>(TYPICAL_NUM_TOOLS);

		// remove the default etched border
		// OPCODEX: a quiet inset instead of the inherited etched title frame.
		setBorder(BorderFactory.createEmptyBorder(8, 4, 12, 4));

		setActiveProject(plugin.getActiveProject());

		// let the default coloring shine through this toolbar; leaving this opaque causes
		// odd display coloring issues in some LookAndFeels, like Metal
		setOpaque(false);
		setFloatable(false); // it is odd to allow the user to undock the tool buttons
	}

	@Override
	public void toolTemplateAdded(ToolTemplate toolConfig) {
		// rebuild the tool bar so that the tools are shown in
		// alphabetical order
		populateToolBar();
	}

	/**
	 * ToolSet was added to the project toolchest
	 */
	@Override
	public void toolSetAdded(ToolSet toolset) {
		ToolChest toolChest = tool.getProject().getLocalToolChest();
		toolTemplateAdded(toolChest.getToolTemplate(toolset.getName()));
	}

	@Override
	public void toolRemoved(String toolName) {
		if (!toolButtonMap.containsKey(toolName)) {
			return;
		}
		ToolButton button = toolButtonMap.get(toolName);
		this.remove(button);
		toolButtonMap.remove(toolName);
		button.dispose();

		tool.getToolFrame().validate();

		repaint();
	}

	void setActiveProject(Project project) {
		// first clear state from previous project
		clear();

		if (project == null) {
			return;
		}
		populateToolBar();
	}

	private void clear() {
		this.removeAll();
		Iterator<ToolButton> it = toolButtonMap.values().iterator();
		while (it.hasNext()) {
			ToolButton tb = it.next();
			tb.dispose();
		}
		toolButtonMap.clear();

		if (tool.isVisible()) {
			tool.getToolFrame().validate();
		}
	}

	/**
	 * Redo the tool bar.
	 */
	private void populateToolBar() {
		clear();
		ToolChest tc = plugin.getActiveProject().getLocalToolChest();
		ToolTemplate[] templates = tc.getToolTemplates();
		for (ToolTemplate element : templates) {
			addConfig(element);
		}
		invalidate();
		tool.getToolFrame().validate();
		repaint();
	}

	/**
	 * Add a button for the tool template to the tool bar.
	 */
	private void addConfig(ToolTemplate toolConfig) {
		ToolButton button = new ToolButton(plugin, toolConfig);
		// OPCODEX: align every startup tool on the same icon/caption baseline and hit target.
        styleButton(button, toolConfig.getName());
        this.add(button);
        toolButtonMap.put(toolConfig.getName(), button);
    }

    @Override public void updateUI() {
        super.updateUI();
        if (toolButtonMap == null) return;
        setLayout(OpcodexUi.active() ? cardLayout : classicLayout);
        toolButtonMap.forEach((name, button) -> styleButton(button, name));
        revalidate();
    }

    private void styleButton(ToolButton button, String name) {
        boolean modern = OpcodexUi.active();
        button.putClientProperty("opcodex.launcherCard", modern);
        button.setHorizontalAlignment(modern ? SwingConstants.LEADING : SwingConstants.CENTER);
        button.setHorizontalTextPosition(modern ? SwingConstants.TRAILING : SwingConstants.CENTER);
        button.setVerticalTextPosition(modern ? SwingConstants.CENTER : SwingConstants.BOTTOM);
        if (!modern) {
            button.setText(null);
            button.setPreferredSize(null); button.setMinimumSize(null); button.setMaximumSize(null);
            button.setMargin(new java.awt.Insets(0, 0, 0, 0));
            return;
        }
        String label = switch (name) {
			case "CodeBrowser" -> "Code Browser";
			case "VersionTracking" -> "Version Tracking";
			default -> name;
		};
		String description = switch (name) {
			case "CodeBrowser" -> "Inspect code and symbols";
			case "Debugger" -> "Inspect a running target";
			case "Emulator" -> "Explore execution in isolation";
			case "VersionTracking", "Version Tracking" -> "Compare program versions";
			default -> "Open this workspace tool";
		};
		button.setText("<html><b>" + HTMLUtilities.escapeHTML(label) +
			"</b><br><small>" + description + "</small></html>");
		button.setIconTextGap(12);
		button.getAccessibleContext().setAccessibleName(label);
		button.getAccessibleContext().setAccessibleDescription(description);
		button.setMargin(new Insets(10, 14, 10, 14));
		button.setPreferredSize(new Dimension(208, 78));
	}

	@Override public void doLayout() {
		if (getLayout() == cardLayout) {
			int columns = getWidth() < 434 ? 1 : 2;
			if (cardLayout.getColumns() != columns) {
				cardLayout.setColumns(columns);
				revalidate();
			}
		}
		super.doLayout();
	}

	@Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
	@Override public int getScrollableUnitIncrement(Rectangle visible, int axis, int direction) { return 24; }
	@Override public int getScrollableBlockIncrement(Rectangle visible, int axis, int direction) { return Math.max(24, visible.height - 24); }
	@Override public boolean getScrollableTracksViewportWidth() { return true; }
	@Override public boolean getScrollableTracksViewportHeight() { return false; }

	public ToolButton getToolButtonForToolConfig(ToolTemplate toolTemplate) {
		return toolButtonMap.get(toolTemplate.getName());
	}
}

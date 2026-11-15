/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package ghidra.framework.main;

import java.awt.*;
import javax.swing.*;
import docking.widgets.button.GButton;
import generic.theme.*;

/** First-run project actions, using the existing project commands. */
class OpcodexWelcomePanel extends JPanel {
	OpcodexWelcomePanel(Runnable createProject, Runnable openProject) {
		super(new GridBagLayout());
		setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JLabel heading = new JLabel("Your next discovery starts here");
		heading.setFont(Gui.getFont("font.standard").deriveFont(Font.BOLD, 24f));
		heading.setAlignmentX(LEFT_ALIGNMENT);
		JLabel subtitle = new JLabel("Create a workspace or open an existing project.");
		subtitle.setForeground(new GColor("color.fg.messages.hint"));
		subtitle.setAlignmentX(LEFT_ALIGNMENT);
		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		actions.setAlignmentX(LEFT_ALIGNMENT);
		GButton create = new GButton("Create Project");
		create.setIcon(new GIcon("icon.folder.new"));
		create.setMnemonic('C');
		create.setToolTipText("Create a project (Ctrl+N)");
		create.addActionListener(event -> createProject.run());
		GButton open = new GButton("Open Project");
		open.setIcon(new GIcon("icon.folder.open"));
		open.setMnemonic('O');
		open.setToolTipText("Open an existing project (Ctrl+O)");
		open.addActionListener(event -> openProject.run());
		actions.add(create);
		actions.add(Box.createHorizontalStrut(12));
		actions.add(open);
		JLabel hint = new JLabel("Projects keep your programs, notes and analysis together.");
		hint.setForeground(new GColor("color.fg.messages.hint"));
		hint.setAlignmentX(LEFT_ALIGNMENT);
		content.add(heading);
		content.add(Box.createVerticalStrut(12));
		content.add(subtitle);
		content.add(Box.createVerticalStrut(28));
		content.add(actions);
		content.add(Box.createVerticalStrut(20));
		content.add(hint);
		add(content);
		getAccessibleContext().setAccessibleName("Opcodex project welcome");
	}
}

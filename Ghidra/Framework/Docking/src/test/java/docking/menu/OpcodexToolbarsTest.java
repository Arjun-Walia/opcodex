/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import static org.junit.Assert.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import org.junit.Test;
import docking.ActionContext;
import docking.DockingActionProxy;
import docking.action.*;
import docking.test.AbstractDockingTest;
import resources.ResourceManager;

public class OpcodexToolbarsTest extends AbstractDockingTest {
	@Test public void ordinaryWindowProxiesDoNotBecomeSplitControls() {
		runSwing(() -> {
			DockingAction action = new DockingAction("Secondary command", "Test") {
				@Override public void actionPerformed(ActionContext context) {}
			};
			action.setToolBarData(new ToolBarData(ResourceManager.getDefaultIcon()));
			ToolBarItemManager item = new ToolBarItemManager(new DockingActionProxy(action), null);
			try {
				JComponent toolbar = OpcodexToolbars.create(List.of(item), null, false);
				assertNull(item.getButton().getParent());
				assertEquals("More workspace actions", toolbar.getComponent(0).getName());
			}
			finally { item.dispose(); }
		});
	}
	@Test public void focusedToolbarsRetainSplitControlsAndEveryAlternative() {
		runSwing(() -> {
			AtomicInteger performed = new AtomicInteger();
			DockingAction option = new DockingAction("Alternative", "Test") {
				@Override public void actionPerformed(ActionContext context) { performed.incrementAndGet(); }
			};
			option.setMenuBarData(new MenuData(new String[] {"Alternative"}));
			MultiActionDockingAction split = new MultiActionDockingAction("Unlisted split control", "Test");
			split.setToolBarData(new ToolBarData(ResourceManager.getDefaultIcon()));
			split.setActions(List.of(option));
			for (boolean local : new boolean[] {false, true}) {
				ToolBarItemManager item = new ToolBarItemManager(new DockingActionProxy(split), null);
				try {
					JComponent toolbar = OpcodexToolbars.create(List.of(item), null, local);
					assertSame(toolbar, item.getButton().getParent());
					var button = (MultipleActionDockingToolbarButton) item.getButton();
					JPopupMenu options = button.doCreateMenu();
					assertEquals(1, options.getComponentCount());
					((JMenuItem) options.getComponent(0)).doClick(0);
				}
				finally { item.dispose(); }
			}
			assertEquals(2, performed.get());
		});
	}
}

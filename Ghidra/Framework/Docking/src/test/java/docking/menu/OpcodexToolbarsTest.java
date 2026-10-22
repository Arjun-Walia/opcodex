/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package docking.menu;

import static org.junit.Assert.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;
import org.junit.Test;
import docking.ActionContext;
import docking.DockingActionProxy;
import docking.action.*;
import docking.test.AbstractDockingTest;
import resources.ResourceManager;

public class OpcodexToolbarsTest extends AbstractDockingTest {
    @Test public void providerHeadersAcceptTheStableToolbarHost() {
        runSwing(() -> {
            docking.GenericHeader header = new docking.GenericHeader();
            try { header.setSize(320, 40); header.update(); header.doLayout(); }
            finally { header.dispose(); }
        });
    }
	@Test public void ordinaryWindowProxiesRemainVisibleByDefault() {
		runSwing(() -> {
			DockingAction action = new DockingAction("Secondary command", "Test") {
				@Override public void actionPerformed(ActionContext context) {}
			};
			action.setToolBarData(new ToolBarData(ResourceManager.getDefaultIcon()));
			ToolBarItemManager item = new ToolBarItemManager(new DockingActionProxy(action), null);
			try {
				JComponent toolbar = OpcodexToolbars.create(List.of(item), null, false);
				assertTrue(SwingUtilities.isDescendingFrom(item.getButton(), toolbar));
                assertNotNull(find(toolbar, "More workspace actions"));
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
					assertTrue(SwingUtilities.isDescendingFrom(item.getButton(), toolbar));
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

    private static Component find(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (name.equals(child.getName())) return child;
            if (child instanceof Container nested) {
                Component found = find(nested, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test public void narrowToolbarsKeepOneRowAndScrollToEveryOriginalButton() {
        runSwing(() -> {
            List<ToolBarItemManager> items = new ArrayList<>();
            JFrame frame = new JFrame();
            try {
                for (int i = 0; i < 45; i++) {
                    DockingAction action = new DockingAction("Command " + i, "Test") {
                        @Override public void actionPerformed(ActionContext context) {}
                    };
                    action.setToolBarData(new ToolBarData(ResourceManager.getDefaultIcon()));
                    items.add(new ToolBarItemManager(action, null));
                }
                JComponent strip = OpcodexToolbars.create(items, null, false);
                frame.add(strip); frame.setSize(360, 110); frame.setVisible(true); frame.validate();
                int center = items.get(0).getButton().getY() + items.get(0).getButton().getHeight() / 2;
                for (var item : items) {
                    JButton button = item.getButton();
                    assertEquals(center, button.getY() + button.getHeight() / 2);
                    assertTrue(SwingUtilities.isDescendingFrom(button, strip));
                }
                JButton next = (JButton) find(strip, "Scroll toolbar right");
                JButton previous = (JButton) find(strip, "Scroll toolbar left");
                assertTrue(next.isVisible());
                for (int i = 0; i < 50 && next.isEnabled(); i++) next.doClick(0);
                assertFalse(next.isEnabled());
                assertTrue(previous.isEnabled());
                JButton last = items.get(items.size() - 1).getButton();
                assertTrue(last.getVisibleRect().width > 0);
                for (int i = 0; i < 50 && previous.isEnabled(); i++) previous.doClick(0);
                assertTrue(items.get(0).getButton().getVisibleRect().width > 0);
            } finally {
                frame.dispose(); items.forEach(ToolBarItemManager::dispose);
            }
        });
    }
}

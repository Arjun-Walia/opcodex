/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package docking.actions.dialog;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Test;
import docking.DefaultActionContext;
import docking.action.*;
import resources.Icons;

public class OpcodexCommandSearchTest {
    @Test public void searchIndexIncludesPurposeAndMenuPathWithoutHtmlMarkup() {
        DockingAction command = new DockingAction("Example", "Opcodex test") {
            @Override public void actionPerformed(docking.ActionContext context) {}
        };
        command.setDescription("<html>Decode <b>attributes</b> &amp; values</html>");
        command.setMenuBarData(new MenuData(new String[] {"Tools", "Inspect"}));
        String indexed = ActionChooserDialog.commandIndexText(command);
        assertTrue(indexed.contains("decode"));
        assertTrue(indexed.contains("attributes"));
        assertTrue(indexed.contains("inspect"));
        assertFalse(indexed.contains("<html>"));
        assertFalse(indexed.contains("&amp;"));
    }
    @Test public void finderIndexesEveryActionOnceAcrossItsEntryPoints() {
        DockingAction command = new DockingAction("Example", "Opcodex test") {
            @Override public void actionPerformed(docking.ActionContext context) {}
        };
        command.setMenuBarData(new MenuData(new String[] {"Tools", "Example"}));
        command.setToolBarData(new ToolBarData(Icons.ADD_ICON));
        command.setPopupMenuData(new MenuData(new String[] {"Example"}));
        Set<DockingActionIf> local = new HashSet<>(Set.of(command));
        Set<DockingActionIf> global = new HashSet<>(Set.of(command));
        ActionsModel model = new ActionsModel(local, global, new DefaultActionContext(), true);
        model.setDisplayLevel(ActionDisplayLevel.ALL);
        assertEquals(1, model.getSize());
        model.dispose();
    }

    @Test public void unavailableAndNonToolbarCommandsRemainDiscoverable() {
        Set<DockingActionIf> commands = new HashSet<>();
        for (int i = 0; i < 3; i++) {
            DockingAction action = new DockingAction("Command " + i, "Opcodex test") {
                @Override public boolean isValidContext(docking.ActionContext context) { return false; }
                @Override public void actionPerformed(docking.ActionContext context) { fail("Disabled command ran"); }
            };
            action.setEnabled(false);
            commands.add(action);
        }
        ActionsModel model = new ActionsModel(new HashSet<>(), commands, new DefaultActionContext(), true);
        model.setDisplayLevel(ActionDisplayLevel.ALL);
        assertEquals(3, model.getSize());
        model.dispose();
    }
}

/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package ghidra.app.nav;

import static org.junit.Assert.*;
import javax.swing.*;
import org.junit.Test;
import docking.test.AbstractDockingTest;

public class OpcodexListingEmptyStateTest extends AbstractDockingTest {
    @Test public void emptyStateRetainsTheOriginalListingAndCanBeDismissed() {
        runSwing(() -> {
            JPanel listing = new JPanel();
            ListingPanelContainer container = new ListingPanelContainer(listing, true);
            JPanel empty = new JPanel();
            container.setEmptyState(empty);
            container.showEmptyState(true);
            assertTrue(empty.isVisible());
            assertTrue(SwingUtilities.isDescendingFrom(listing, container));
            container.showEmptyState(false);
            assertFalse(empty.isVisible());
            assertTrue(SwingUtilities.isDescendingFrom(listing, container));
        });
    }

    @Test public void comparisonAndClearKeepTheOriginalViewAndNorthControls() {
        runSwing(() -> {
            JPanel listing = new JPanel(), comparison = new JPanel(), north = new JPanel();
            ListingPanelContainer container = new ListingPanelContainer(listing, true);
            container.setNorthPanel(north);
            container.setOtherPanel(comparison, "Original", "Comparison");
            assertTrue(SwingUtilities.isDescendingFrom(listing, container));
            assertTrue(SwingUtilities.isDescendingFrom(comparison, container));
            container.clearOtherPanel();
            assertTrue(SwingUtilities.isDescendingFrom(listing, container));
            assertFalse(SwingUtilities.isDescendingFrom(comparison, container));
            assertSame(north, container.getNorthPanel());
            assertTrue(SwingUtilities.isDescendingFrom(north, container));
        });
    }
}

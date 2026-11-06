/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
 */
package docking.widgets.filter;

import static org.junit.Assert.*;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.junit.Test;
import resources.MultiIcon;
import resources.icons.TranslateIcon;
import generic.theme.GIcon;

public class OpcodexFilterIconTest extends docking.test.AbstractDockingTest {
    @Test public void contextualSymbolsPreserveEveryMatchModeAndInversionBadge() {
        Icon context = new ImageIcon(new java.awt.image.BufferedImage(16,16,
            java.awt.image.BufferedImage.TYPE_INT_ARGB));
        for (TextFilterStrategy strategy : TextFilterStrategy.values()) {
            FilterOptions ordinary = new FilterOptions(strategy,true,true,false);
            assertSame(context,ordinary.getFilterStateIcon(context));
            FilterOptions inverted = new FilterOptions(strategy,true,true,true);
            Icon decorated = inverted.getFilterStateIcon(context);
            assertTrue(decorated instanceof MultiIcon);
            Icon[] layers = ((MultiIcon)decorated).getIcons();
            assertSame(context,layers[0]);
            assertTrue(layers[1] instanceof TranslateIcon);
            GIcon not = (GIcon)((TranslateIcon)layers[1]).getBaseIcon();
            assertEquals("icon.filter.options.not",not.getId());
            assertEquals(strategy,inverted.getTextFilterStrategy());
            assertTrue(inverted.isInverted());
            assertTrue(inverted.isCaseSensitive());
        }
    }
}

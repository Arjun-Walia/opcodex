/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package ghidra.util.task;

import static org.junit.Assert.*;
import javax.swing.*;
import org.junit.Test;

public class OpcodexActivityTest {
	@Test
	public void taskActivityUsesAccessibleProgressWithoutMascotFrames() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			ChompingBitsAnimationPanel panel = new ChompingBitsAnimationPanel();
			assertEquals(1, panel.getComponentCount());
			assertTrue(panel.getComponent(0) instanceof JProgressBar);
			JProgressBar progress = (JProgressBar) panel.getComponent(0);
			assertTrue(progress.isIndeterminate());
			assertEquals("Task in progress", progress.getAccessibleContext().getAccessibleName());
			assertFalse(progress.isBorderPainted());
		});
	}
}

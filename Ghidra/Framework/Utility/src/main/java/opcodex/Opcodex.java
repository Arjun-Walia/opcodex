/* ###
 * IP: Apache License 2.0
 * Copyright 2026 Opcodex contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
package opcodex;

/** Product entry point: X11 derives WM_CLASS from the bottom-most main class. */
public final class Opcodex {
	private Opcodex() {}
	public static void main(String[] args) throws Exception {
		ghidra.Ghidra.main(args);
	}
}

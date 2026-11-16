/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import java.io.IOException;
import java.io.Reader;

/**
 * Ties a desktop-owned engine to the desktop's lifetime. The desktop keeps the engine's stdin
 * open while it runs; when it exits or crashes the operating system closes that pipe, and the
 * engine stops instead of running on with nobody attached.
 */
public final class ParentWatch {

	private ParentWatch() {
	}

	/** Reads {@code in} until end of input (or a read error), then runs {@code onClosed}. */
	public static Thread start(Reader in, Runnable onClosed) {
		Thread thread = new Thread(() -> {
			char[] discard = new char[256];
			try {
				while (in.read(discard) != -1) {
					// Nothing is expected after the credential; keep draining until closed.
				}
			}
			catch (IOException closed) {
				// A broken pipe means the desktop is gone, the same as end of input.
			}
			onClosed.run();
		}, "opcodex-desktop-watch");
		thread.setDaemon(true);
		thread.start();
		return thread;
	}
}

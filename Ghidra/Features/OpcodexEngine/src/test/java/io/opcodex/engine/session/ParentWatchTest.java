/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import static org.junit.Assert.*;

import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ParentWatchTest {

	@Test
	public void stopsWhenTheDesktopClosesItsEnd() throws Exception {
		PipedWriter desktop = new PipedWriter();
		PipedReader engine = new PipedReader(desktop);
		CountDownLatch stopped = new CountDownLatch(1);

		Thread watch = ParentWatch.start(engine, stopped::countDown);
		assertTrue(watch.isDaemon());
		assertFalse(stopped.await(200, TimeUnit.MILLISECONDS));

		desktop.close();

		assertTrue(stopped.await(5, TimeUnit.SECONDS));
	}

	@Test
	public void ignoresAnythingTheDesktopWritesBeforeClosing() throws Exception {
		PipedWriter desktop = new PipedWriter();
		PipedReader engine = new PipedReader(desktop);
		CountDownLatch stopped = new CountDownLatch(1);

		ParentWatch.start(engine, stopped::countDown);
		desktop.write("noise\n");
		desktop.flush();
		assertFalse(stopped.await(200, TimeUnit.MILLISECONDS));

		desktop.close();
		assertTrue(stopped.await(5, TimeUnit.SECONDS));
	}
}

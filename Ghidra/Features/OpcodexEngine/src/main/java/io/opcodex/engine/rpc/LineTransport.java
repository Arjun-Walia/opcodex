/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.JsonObject;

import ghidra.util.Msg;

/**
 * Newline-delimited JSON-RPC over a byte stream. Each request line is handed to the dispatcher;
 * responses and events are written as single lines, never interleaved.
 */
public final class LineTransport {

	/** Longest accepted request line, in bytes. */
	public static final int MAX_LINE_BYTES = 8 * 1024 * 1024;

	private static final String OVERSIZED_RESPONSE =
		"{\"jsonrpc\":\"2.0\",\"id\":null,\"error\":{\"code\":" + Dispatcher.INVALID_REQUEST +
			",\"message\":\"Request line exceeds " + MAX_LINE_BYTES + " bytes.\"}}";

	private final InputStream in;
	private final OutputStream out;
	private final Dispatcher dispatcher;
	private final Object writeLock = new Object();

	public LineTransport(InputStream in, OutputStream out, Dispatcher dispatcher) {
		this.in = new BufferedInputStream(Objects.requireNonNull(in, "in"));
		this.out = new BufferedOutputStream(Objects.requireNonNull(out, "out"));
		this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
	}

	/** Serves requests until the input ends or the output fails. */
	public void serve() throws IOException {
		ByteArrayOutputStream line = new ByteArrayOutputStream();
		boolean oversized = false;
		int b;
		while ((b = in.read()) != -1) {
			if (b != '\n') {
				if (!oversized) {
					if (line.size() >= MAX_LINE_BYTES) {
						oversized = true;
						line.reset();
					}
					else {
						line.write(b);
					}
				}
				continue;
			}
			if (oversized) {
				writeLine(OVERSIZED_RESPONSE);
				oversized = false;
				continue;
			}
			String text = line.toString(StandardCharsets.UTF_8).strip();
			line.reset();
			if (text.isEmpty()) {
				continue;
			}
			String response = dispatcher.handleLine(text, this::sendEvent);
			if (response != null) {
				writeLine(response);
			}
		}
	}

	/** Sends a notification (an event) to the client. Safe to call from any thread. */
	public void sendEvent(JsonObject notification) {
		try {
			writeLine(notification.toString());
		}
		catch (IOException e) {
			Msg.debug(this, "Event not delivered: client disconnected", e);
		}
	}

	private void writeLine(String text) throws IOException {
		byte[] bytes = (text + "\n").getBytes(StandardCharsets.UTF_8);
		synchronized (writeLock) {
			out.write(bytes);
			out.flush();
		}
	}
}

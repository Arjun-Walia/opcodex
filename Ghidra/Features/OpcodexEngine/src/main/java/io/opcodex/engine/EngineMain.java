/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ghidra.GhidraApplicationLayout;
import ghidra.GhidraLaunchable;
import io.opcodex.engine.rpc.LineTransport;
import io.opcodex.engine.rpc.TcpServer;
import io.opcodex.engine.session.Credential;
import io.opcodex.engine.session.SessionFiles;

/**
 * Engine host entry point.
 * <ul>
 * <li>{@code --stdio}: the first stdin line is {@code {"credential":"...","kind":"DESKTOP|CLIENT"}};
 * then JSON-RPC lines are served on stdin/stdout. All logging goes to stderr.</li>
 * <li>{@code --tcp}: serves 127.0.0.1 on a random port, writes the session files and prints
 * {@code {"ready":true,"port":N}}. With {@code --desktop-token-stdin}, the first stdin line holds
 * the desktop credential.</li>
 * </ul>
 */
public final class EngineMain implements GhidraLaunchable {

	private static final SecureRandom RANDOM = new SecureRandom();

	@Override
	public void launch(GhidraApplicationLayout layout, String[] args) throws Exception {
		main(args);
	}

	public static void main(String[] args) throws Exception {
		boolean stdio = has(args, "--stdio");
		boolean tcp = has(args, "--tcp");
		if (stdio == tcp) {
			System.err.println("Usage: EngineMain --stdio | --tcp [--desktop-token-stdin]");
			System.exit(64);
		}
		PrintStream protocolOut = System.out;
		// Keep the protocol stream clean: everything else that prints goes to stderr.
		System.setOut(System.err);
		BufferedReader stdin =
			new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
		if (stdio) {
			runStdio(stdin, protocolOut);
		}
		else {
			runTcp(stdin, protocolOut, has(args, "--desktop-token-stdin"));
		}
	}

	private static void runStdio(BufferedReader stdin, PrintStream out) throws Exception {
		Credential credential = readCredential(stdin);
		try (EngineCore core = EngineCore.start(Map.of(credential.token(), credential))) {
			InputStream rest = new ReaderInputStream(stdin);
			new LineTransport(rest, out, core.dispatcher()).serve();
		}
		System.exit(0);
	}

	private static void runTcp(BufferedReader stdin, PrintStream out, boolean desktopFromStdin)
			throws Exception {
		Map<String, Credential> tokens = new HashMap<>();
		String clientToken = newToken();
		tokens.put(clientToken, new Credential(Credential.Kind.CLIENT, clientToken));
		if (desktopFromStdin) {
			Credential desktop = readCredential(stdin);
			if (desktop.kind() != Credential.Kind.DESKTOP) {
				throw new IllegalArgumentException("Expected a DESKTOP credential on stdin.");
			}
			tokens.put(desktop.token(), desktop);
		}
		EngineCore core = EngineCore.start(tokens);
		TcpServer server = new TcpServer();
		int port = server.start(core.dispatcher());
		Path sessionDir = SessionFiles.defaultDir();
		SessionFiles.write(sessionDir, clientToken, port, "community", "1.0");
		CountDownLatch stopped = new CountDownLatch(1);
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			server.stop();
			core.close();
			SessionFiles.delete(sessionDir);
			stopped.countDown();
		}, "opcodex-engine-shutdown"));
		JsonObject ready = new JsonObject();
		ready.addProperty("ready", true);
		ready.addProperty("port", port);
		out.println(ready);
		out.flush();
		stopped.await();
	}

	private static Credential readCredential(BufferedReader stdin) throws IOException {
		String line = stdin.readLine();
		if (line == null) {
			throw new EOFException("Expected a credential line on stdin.");
		}
		JsonObject object = JsonParser.parseString(line).getAsJsonObject();
		String token = object.get("credential").getAsString();
		if (token.length() < 32) {
			throw new IllegalArgumentException("Credential is too short.");
		}
		Credential.Kind kind = Credential.Kind.valueOf(object.get("kind").getAsString());
		return new Credential(kind, token);
	}

	static String newToken() {
		byte[] bytes = new byte[32];
		RANDOM.nextBytes(bytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

	private static boolean has(String[] args, String flag) {
		for (String arg : args) {
			if (flag.equals(arg)) {
				return true;
			}
		}
		return false;
	}

	/** Exposes the rest of a reader as bytes, after the credential line was consumed. */
	private static final class ReaderInputStream extends InputStream {
		private final Reader reader;
		private byte[] pending = new byte[0];
		private int index;

		ReaderInputStream(Reader reader) {
			this.reader = reader;
		}

		@Override
		public int read(byte[] bytes, int offset, int length) throws IOException {
			java.util.Objects.checkFromIndexSize(offset, length, bytes.length);
			int count = 0;
			while (count < length) {
				int value = read();
				if (value == -1) {
					return count == 0 ? -1 : count;
				}
				bytes[offset + count++] = (byte) value;
				if (value == '\n') {
					break;
				}
			}
			return count;
		}

		@Override
		public int read() throws IOException {
			while (index >= pending.length) {
				int c = reader.read();
				if (c == -1) {
					return -1;
				}
				pending = String.valueOf((char) c).getBytes(StandardCharsets.UTF_8);
				if (Character.isHighSurrogate((char) c)) {
					int low = reader.read();
					if (low != -1) {
						pending = new String(new char[] { (char) c, (char) low })
								.getBytes(StandardCharsets.UTF_8);
					}
				}
				index = 0;
			}
			return pending[index++] & 0xff;
		}
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import static org.junit.Assert.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.*;

import io.opcodex.engine.catalog.*;
import io.opcodex.engine.session.Credential;

public class LineTransportTest {

	private static final String TOKEN = "client-token-abcdefabcdefabcdef";

	private Dispatcher dispatcher;

	@Before
	public void setUp() {
		Catalog catalog = new Catalog();
		catalog.register(new Operation("demo.echo", "Echoes.", OperationClass.READ,
			Schemas.object(), Schemas.object(), (ctx, params) -> new JsonPrimitive("pong")));
		dispatcher = new Dispatcher(catalog,
			Map.of(TOKEN, new Credential(Credential.Kind.CLIENT, TOKEN)), null);
	}

	private static String validCall(int id) {
		return "{\"jsonrpc\":\"2.0\",\"id\":" + id + ",\"method\":\"demo.echo\",\"auth\":\"" +
			TOKEN + "\"}";
	}

	private static List<JsonObject> lines(ByteArrayOutputStream out) {
		List<JsonObject> parsed = new ArrayList<>();
		for (String line : out.toString(StandardCharsets.UTF_8).split("\n")) {
			if (!line.isEmpty()) {
				parsed.add(JsonParser.parseString(line).getAsJsonObject());
			}
		}
		return parsed;
	}

	@Test
	public void malformedAndOversizedLinesReturnErrorsAndKeepServing() throws Exception {
		StringBuilder input = new StringBuilder("not json\n");
		input.append("a".repeat(LineTransport.MAX_LINE_BYTES + 1)).append('\n');
		input.append(validCall(3)).append('\n');
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		new LineTransport(new ByteArrayInputStream(input.toString().getBytes(StandardCharsets.UTF_8)),
			out, dispatcher).serve();

		List<JsonObject> responses = lines(out);
		assertEquals(3, responses.size());
		assertEquals(Dispatcher.PARSE_ERROR,
			responses.get(0).getAsJsonObject("error").get("code").getAsInt());
		assertEquals(Dispatcher.INVALID_REQUEST,
			responses.get(1).getAsJsonObject("error").get("code").getAsInt());
		assertEquals("pong", responses.get(2).get("result").getAsString());
	}

	@Test
	public void eventsInterleaveWithoutCorruptingLines() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		LineTransport transport =
			new LineTransport(new ByteArrayInputStream(new byte[0]), out, dispatcher);
		ExecutorService pool = Executors.newFixedThreadPool(20);
		List<Future<?>> futures = new ArrayList<>();
		for (int t = 0; t < 20; t++) {
			int thread = t;
			futures.add(pool.submit(() -> {
				for (int i = 0; i < 50; i++) {
					JsonObject event = new JsonObject();
					event.addProperty("jsonrpc", "2.0");
					event.addProperty("method", "event.test");
					event.addProperty("payload", "thread-" + thread + "-" + i + "-" + "x".repeat(200));
					transport.sendEvent(event);
				}
			}));
		}
		for (Future<?> f : futures) {
			f.get(30, TimeUnit.SECONDS);
		}
		pool.shutdown();
		assertEquals(1000, lines(out).size());
	}

	@Test
	public void tcpServerServesOnLoopbackOnly() throws Exception {
		TcpServer server = new TcpServer();
		int port = server.start(dispatcher);
		try {
			assertTrue(server.boundAddress().isLoopbackAddress());
			try (Socket socket = new Socket(java.net.InetAddress.getLoopbackAddress(), port)) {
				socket.setSoTimeout(10_000);
				OutputStream os = socket.getOutputStream();
				os.write((validCall(9) + "\n").getBytes(StandardCharsets.UTF_8));
				os.flush();
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				JsonObject response = JsonParser.parseString(reader.readLine()).getAsJsonObject();
				assertEquals("pong", response.get("result").getAsString());
			}
		}
		finally {
			server.stop();
		}
	}
}

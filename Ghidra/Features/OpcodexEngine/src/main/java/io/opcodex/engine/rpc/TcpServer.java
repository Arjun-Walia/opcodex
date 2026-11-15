/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

import java.io.IOException;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ghidra.util.Msg;

/** Accepts loopback connections and serves each one with its own {@link LineTransport}. */
public final class TcpServer {

	private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
	private volatile ServerSocket server;

	/** Binds 127.0.0.1 on a random free port and starts accepting; returns the port. */
	public synchronized int start(Dispatcher dispatcher) throws IOException {
		if (server != null) {
			throw new IllegalStateException("Already started");
		}
		ServerSocket socket = new ServerSocket(0, 16, InetAddress.getLoopbackAddress());
		server = socket;
		Thread acceptor = new Thread(() -> acceptLoop(socket, dispatcher), "opcodex-engine-accept");
		acceptor.setDaemon(true);
		acceptor.start();
		return socket.getLocalPort();
	}

	public InetAddress boundAddress() {
		return server.getInetAddress();
	}

	public synchronized void stop() {
		ServerSocket socket = server;
		server = null;
		closeQuietly(socket);
		clients.forEach(TcpServer::closeQuietly);
		clients.clear();
	}

	private void acceptLoop(ServerSocket socket, Dispatcher dispatcher) {
		while (!socket.isClosed()) {
			try {
				Socket client = socket.accept();
				if (!client.getInetAddress().isLoopbackAddress()) {
					closeQuietly(client);
					continue;
				}
				clients.add(client);
				Thread worker = new Thread(() -> serve(client, dispatcher), "opcodex-engine-client");
				worker.setDaemon(true);
				worker.start();
			}
			catch (IOException e) {
				if (!socket.isClosed()) {
					Msg.warn(this, "Engine accept failed: " + e.getMessage());
				}
			}
		}
	}

	private void serve(Socket client, Dispatcher dispatcher) {
		try {
			new LineTransport(client.getInputStream(), client.getOutputStream(), dispatcher).serve();
		}
		catch (IOException e) {
			Msg.debug(this, "Client connection ended: " + e.getMessage());
		}
		finally {
			clients.remove(client);
			closeQuietly(client);
		}
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		}
		catch (Exception e) {
			Msg.debug(TcpServer.class, "Close failed: " + e.getMessage());
		}
	}
}

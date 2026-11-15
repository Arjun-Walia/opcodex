/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine;

import java.io.IOException;
import java.util.Map;

import ghidra.GhidraApplicationLayout;
import ghidra.framework.Application;
import ghidra.framework.HeadlessGhidraApplicationConfiguration;
import io.opcodex.engine.catalog.Catalog;
import io.opcodex.engine.rpc.Dispatcher;
import io.opcodex.engine.session.Credential;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.spi.EntitlementProvider;
import io.opcodex.engine.workspace.Workspace;

/** Wires the catalog, workspace and dispatcher together; shared by the entry point and tests. */
public final class EngineCore implements AutoCloseable {

	private record Services(Catalog catalog, Workspace workspace,
			EntitlementProvider entitlements) implements EngineServices {
	}

	private final Services services;
	private final Dispatcher dispatcher;

	private EngineCore(Services services, Dispatcher dispatcher) {
		this.services = services;
		this.dispatcher = dispatcher;
	}

	/** Initialises the analysis engine if needed and builds an engine for these credentials. */
	public static EngineCore start(Map<String, Credential> tokens) throws IOException {
		ensureApplication();
		Services services =
			new Services(new Catalog(), new Workspace(), EntitlementProvider.COMMUNITY);
		services.catalog().loadProviders(services, EngineCore.class.getClassLoader());
		return new EngineCore(services, new Dispatcher(services.catalog(), tokens, services));
	}

	public Dispatcher dispatcher() {
		return dispatcher;
	}

	public EngineServices services() {
		return services;
	}

	@Override
	public void close() {
		services.workspace().close();
	}

	private static synchronized void ensureApplication() throws IOException {
		if (!Application.isInitialized()) {
			Application.initializeApplication(new GhidraApplicationLayout(),
				new HeadlessGhidraApplicationConfiguration());
		}
	}
}

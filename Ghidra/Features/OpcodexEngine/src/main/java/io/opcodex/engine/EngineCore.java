/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine;

import java.io.IOException;
import java.util.Map;

import com.google.gson.JsonObject;

import ghidra.GhidraApplicationLayout;
import ghidra.framework.Application;
import ghidra.framework.HeadlessGhidraApplicationConfiguration;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.journal.Journal;
import io.opcodex.engine.policy.ApprovalPolicy;
import io.opcodex.engine.rpc.*;
import io.opcodex.engine.session.CallContext;
import io.opcodex.engine.session.Credential;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.spi.EntitlementProvider;
import io.opcodex.engine.workspace.Workspace;

/** Wires the catalog, workspace and dispatcher together; shared by the entry point and tests. */
public final class EngineCore implements AutoCloseable {

	private static final int PREVIEW_CHARS = 300;

	private static final class Services implements EngineServices {
		private final Catalog catalog = new Catalog();
		private final Workspace workspace = new Workspace();
		private final EntitlementProvider entitlements;

		Services(EntitlementProvider entitlements) {
			this.entitlements = entitlements;
		}

		@Override
		public Catalog catalog() {
			return catalog;
		}

		@Override
		public Workspace workspace() {
			return workspace;
		}

		@Override
		public EntitlementProvider entitlements() {
			return entitlements;
		}

		@Override
		public Journal journal() throws RpcException {
			return workspace.journal();
		}

		@Override
		public ApprovalPolicy policy() throws RpcException {
			try {
				return ApprovalPolicy.load(workspace.opcodexDataDir());
			}
			catch (IOException e) {
				throw new RpcException(ErrorCode.ENGINE_ERROR, "Couldn't read the approval policy.");
			}
		}

		@Override
		public void setPolicy(ApprovalPolicy policy) throws RpcException {
			try {
				policy.save(workspace.opcodexDataDir());
			}
			catch (IOException e) {
				throw new RpcException(ErrorCode.ENGINE_ERROR, "Couldn't save the approval policy.");
			}
		}
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
		Services services = new Services(EntitlementProvider.load(EngineCore.class.getClassLoader()));
		services.catalog().loadProviders(services, EngineCore.class.getClassLoader());
		// Plugins add operations after Community's, so they can't replace built-in ones.
		io.opcodex.engine.ops.PluginOps.loadPlugins(services.catalog(), services, EngineCore.class.getClassLoader());
		Dispatcher dispatcher = new Dispatcher(services.catalog(), tokens, services);
		dispatcher.setGate((ctx, op, params) -> checkApproval(services, ctx, op, params));
		return new EngineCore(services, dispatcher);
	}

	/** Clients may write or run only as far as the open project's approval policy allows. */
	private static void checkApproval(Services services, CallContext ctx, Operation op,
			JsonObject params) throws RpcException {
		if (ctx.kind() == Credential.Kind.DESKTOP || op.cls() == OperationClass.READ) {
			return;
		}
		ApprovalPolicy policy;
		try {
			policy = services.policy();
		}
		catch (RpcException e) {
			if (e.code() == ErrorCode.NOT_FOUND) {
				return; // No project open: the operation itself reports that.
			}
			throw e;
		}
		if (policy.permits(op.cls())) {
			return;
		}
		JsonObject detail = new JsonObject();
		detail.addProperty("operation", op.name());
		detail.addProperty("policy", policy.wireName());
		detail.addProperty("preview", preview(op, params));
		throw new RpcException(ErrorCode.APPROVAL_REQUIRED,
			"This project's approval policy (" + policy.wireName() + ") doesn't let " +
				ctx.actor() + " " + (op.cls() == OperationClass.RUN ? "run code" : "make changes") +
				". Approve it in the Opcodex desktop, or run `opcodex policy set` in a terminal.",
			detail);
	}

	private static String preview(Operation op, JsonObject params) {
		JsonObject shown = params.deepCopy();
		shown.remove("expectedRevision");
		shown.remove("bytesBase64");
		String text = op.name() + " " + shown;
		return text.length() <= PREVIEW_CHARS ? text : text.substring(0, PREVIEW_CHARS) + "…";
	}

	public Dispatcher dispatcher() {
		return dispatcher;
	}

	public EngineServices services() {
		return services;
	}

	@Override
	public void close() {
		io.opcodex.engine.ops.PluginOps.unloadPlugins();
		services.workspace().jobs().close();
		services.workspace().close();
	}

	private static synchronized void ensureApplication() throws IOException {
		if (!Application.isInitialized()) {
			Application.initializeApplication(new GhidraApplicationLayout(),
				new HeadlessGhidraApplicationConfiguration());
		}
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import io.opcodex.engine.catalog.Catalog;
import io.opcodex.engine.journal.Journal;
import io.opcodex.engine.policy.ApprovalPolicy;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.workspace.Workspace;

/** Engine services available to operation handlers. */
public interface EngineServices {

	Catalog catalog();

	Workspace workspace();

	EntitlementProvider entitlements();

	/** The change journal of the open project. */
	Journal journal() throws RpcException;

	/** The approval policy of the open project. */
	ApprovalPolicy policy() throws RpcException;

	void setPolicy(ApprovalPolicy policy) throws RpcException;
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.ArrayList;
import java.util.List;

import io.opcodex.engine.catalog.Operation;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.spi.OperationProvider;

/** Every operation that ships with Opcodex Community. */
public final class CommunityOperations implements OperationProvider {

	@Override
	public List<Operation> operations(EngineServices services) {
		List<Operation> all = new ArrayList<>();
		all.addAll(SystemOps.operations(services));
		all.addAll(ProjectOps.operations(services));
		all.addAll(ImportOps.operations(services));
		all.addAll(JobOps.operations(services));
		all.addAll(FunctionOps.operations(services));
		all.addAll(CodeOps.operations(services));
		all.addAll(XrefOps.operations(services));
		all.addAll(StringOps.operations(services));
		all.addAll(SummaryOps.operations(services));
		all.addAll(EditOps.operations(services));
		all.addAll(HistoryOps.operations(services));
		all.addAll(GraphOps.operations(services));
		all.addAll(DebugOps.operations(services));
		all.addAll(PluginOps.operations(services));
		return all;
	}
}

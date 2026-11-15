/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.opcodex.engine.catalog.*;
import io.opcodex.engine.jobs.Job;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.session.CallContext;
import io.opcodex.engine.session.Credential;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.Workspace;

final class JobOps {
	private JobOps() {
	}

	static List<Operation> operations(EngineServices services) {
		Workspace workspace = services.workspace();
		JsonObject ref = Schemas.object();
		Schemas.property(ref, "jobId", Schemas.string("Background job id."), true);
		return List.of(
			new Operation("job.list", "Lists recent jobs for the open project.", OperationClass.READ,
				Schemas.object(), Schemas.any("Jobs."), (ctx, params) -> {
					JsonArray out = new JsonArray();
					workspace.jobs().list(workspace.journal()).forEach(job -> out.add(job.toJson()));
					return out;
				}),
			new Operation("job.get", "Shows a background job's progress and result.",
				OperationClass.READ, ref, Schemas.any("Job state."), (ctx, params) -> workspace.jobs()
					.get(workspace.journal(), Params.requireString(params, "jobId")).toJson()),
			new Operation("job.cancel", "Requests cancellation before a background job commits.",
				OperationClass.WRITE, ref, Schemas.any("Job state."), (ctx, params) -> {
					synchronized (workspace) {
						Job job = workspace.jobs().get(workspace.journal(),
							Params.requireString(params, "jobId"));
						requireWrite(ctx);
						job.cancel();
						return job.toJson();
					}
				}));
	}

	/** Rechecks the gate while the caller holds the lock for the selected project. */
	static void requireWrite(CallContext ctx) throws RpcException {
		if (ctx.kind() != Credential.Kind.DESKTOP &&
			!ctx.services().policy().permits(OperationClass.WRITE)) {
			throw new RpcException(ErrorCode.APPROVAL_REQUIRED,
				"The open project's policy requires approval for this job operation.");
		}
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.nio.file.Path;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ghidra.framework.model.DomainFile;
import ghidra.program.model.listing.Program;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.ProgramHandle;

/** Opening, creating and listing projects and their programs. */
final class ProjectOps {

	private ProjectOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject location = Schemas.object();
		Schemas.property(location, "dir", Schemas.string("Folder that holds the project."), true);
		Schemas.property(location, "name", Schemas.string("Project name."), true);
		JsonObject projectResult = Schemas.object();
		Schemas.property(projectResult, "projectId", Schemas.string("Project name."), true);

		return List.of(
			new Operation("project.create",
				"Creates a new, empty project. Refuses to replace an existing project.",
				OperationClass.READ, location, projectResult, (ctx, params) -> {
					Path dir = directory(params);
					String name = Params.requireString(params, "name");
					services.workspace().create(dir, name);
					return projectId(name);
				}),
			new Operation("project.open", "Opens an existing project.", OperationClass.READ,
				location, projectResult, (ctx, params) -> {
					String name = Params.requireString(params, "name");
					services.workspace().open(directory(params), name);
					return projectId(name);
				}),
			new Operation("project.close", "Closes the open project and its programs.",
				OperationClass.READ, Schemas.object(), Schemas.object(), (ctx, params) -> {
					services.workspace().close();
					return new JsonObject();
				}),
			new Operation("program.info",
				"Opens a program if needed and shows its name, language and current revision. " +
					"Writes need the revision.",
				OperationClass.READ, FunctionOps.programParam(), Schemas.any("Program."),
				(ctx, params) -> {
					ProgramHandle handle =
						services.workspace().get(Params.requireString(params, "programId"));
					Program program = handle.program();
					JsonObject out = new JsonObject();
					out.addProperty("programId", handle.id());
					out.addProperty("name", program.getDomainFile().getName());
					out.addProperty("path", program.getDomainFile().getPathname());
					out.addProperty("language", program.getLanguageID().getIdAsString());
					out.addProperty("compiler",
						program.getCompilerSpec().getCompilerSpecID().getIdAsString());
					out.addProperty("revision", handle.revision());
					out.addProperty("functionCount",
						program.getFunctionManager().getFunctionCount());
					return out;
				}),
			new Operation("program.list",
				"Lists the programs in the open project. Open programs include their language " +
					"and revision.",
				OperationClass.READ, Schemas.object(), Schemas.any("Programs."),
				(ctx, params) -> listPrograms(services)));
	}

	private static JsonArray listPrograms(EngineServices services) throws RpcException {
		JsonArray out = new JsonArray();
		for (DomainFile file : services.workspace().programFiles()) {
			String id = ProgramHandle.idFor(file);
			JsonObject entry = new JsonObject();
			entry.addProperty("programId", id);
			entry.addProperty("name", file.getName());
			entry.addProperty("path", file.getPathname());
			services.workspace().openHandle(id).ifPresentOrElse(handle -> {
				Program program = handle.program();
				entry.addProperty("open", true);
				entry.addProperty("language", program.getLanguageID().getIdAsString());
				entry.addProperty("revision", handle.revision());
			}, () -> entry.addProperty("open", false));
			out.add(entry);
		}
		return out;
	}

	private static Path directory(JsonObject params) throws RpcException {
		String dir = Params.requireString(params, "dir");
		Path path = Path.of(dir);
		if (!path.isAbsolute()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "dir must be an absolute path.");
		}
		return path.normalize();
	}

	private static JsonObject projectId(String name) {
		JsonObject out = new JsonObject();
		out.addProperty("projectId", name);
		return out;
	}
}

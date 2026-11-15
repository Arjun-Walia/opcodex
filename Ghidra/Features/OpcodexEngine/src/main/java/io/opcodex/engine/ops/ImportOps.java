/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;

import ghidra.app.plugin.core.analysis.AutoAnalysisManager;
import ghidra.app.util.importer.MessageLog;
import ghidra.app.util.importer.ProgramLoader;
import ghidra.app.util.opinion.LoadResults;
import ghidra.framework.model.DomainFile;
import ghidra.program.model.listing.Program;
import ghidra.util.Msg;
import ghidra.util.task.TaskMonitor;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.ProgramHandle;
import io.opcodex.engine.workspace.Workspace;

/** Importing binaries into the open project and running auto-analysis. */
final class ImportOps {

	/** Largest file accepted for import. */
	static final long MAX_IMPORT_BYTES = 2L * 1024 * 1024 * 1024;

	private ImportOps() {
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject importParams = Schemas.object();
		Schemas.property(importParams, "path",
			Schemas.string("Absolute path of the file to import. Use this or bytesBase64."), false);
		Schemas.property(importParams, "bytesBase64",
			Schemas.string("File contents, base64-encoded. Use this or path."), false);
		Schemas.property(importParams, "name", Schemas.string("Name in the project."), true);
		Schemas.property(importParams, "language",
			Schemas.string("Language id, for example x86:LE:64:default. Optional for known " +
				"formats."),
			false);
		Schemas.property(importParams, "compiler",
			Schemas.string("Compiler spec id, for example gcc or windows."), false);
		Schemas.property(importParams, "folder",
			Schemas.string("Project folder, for example /firmware. Defaults to /."), false);

		JsonObject programRef = Schemas.object();
		Schemas.property(programRef, "programId", Schemas.string("Program id."), true);

		return List.of(
			new Operation("program.import",
				"Imports a binary into the open project. Doesn't analyse it; call " +
					"program.analyze next.",
				OperationClass.WRITE, importParams, Schemas.any("Imported program."),
				(ctx, params) -> importProgram(services.workspace(), params)),
			new Operation("program.analyze",
				"Runs auto-analysis on a program: finds functions, references, strings and " +
					"types.",
				OperationClass.WRITE, programRef, Schemas.any("Analysis result."),
				(ctx, params) -> analyze(services.workspace(),
					Params.requireString(params, "programId"))));
	}

	private static JsonObject importProgram(Workspace workspace, JsonObject params)
			throws RpcException {
		String path = Params.string(params, "path");
		String base64 = Params.string(params, "bytesBase64");
		if ((path == null) == (base64 == null)) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				"Give exactly one of path or bytesBase64.");
		}
		String name = Params.requireString(params, "name");
		if (name.contains("/") || name.contains("\\")) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "name can't contain slashes.");
		}
		String folder = Params.string(params, "folder");
		folder = folder == null ? "/" : folder;
		if (!folder.startsWith("/") || folder.contains("..")) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				"folder must be a project path that starts with /.");
		}

		ProgramLoader.Builder builder = ProgramLoader.builder()
				.project(workspace.project())
				.projectFolderPath(folder)
				.name(name)
				.log(new MessageLog())
				.monitor(TaskMonitor.DUMMY);
		if (path != null) {
			builder.source(validatedFile(path).toFile());
		}
		else {
			try {
				builder.source(Base64.getDecoder().decode(base64));
			}
			catch (IllegalArgumentException e) {
				throw new RpcException(ErrorCode.INVALID_PARAMS, "bytesBase64 is not valid base64.");
			}
		}
		String language = Params.string(params, "language");
		if (language != null) {
			builder.language(language);
		}
		String compiler = Params.string(params, "compiler");
		if (compiler != null) {
			builder.compiler(compiler);
		}

		try (LoadResults<Program> results = builder.load()) {
			results.save(TaskMonitor.DUMMY);
			Program program = results.getPrimaryDomainObject(workspace);
			DomainFile file = results.getPrimary().getSavedDomainFile();
			ProgramHandle handle = workspace.register(program, file);
			JsonObject out = new JsonObject();
			out.addProperty("programId", handle.id());
			out.addProperty("name", file.getName());
			out.addProperty("path", file.getPathname());
			out.addProperty("language", program.getLanguageID().getIdAsString());
			out.addProperty("revision", handle.revision());
			return out;
		}
		catch (Exception e) {
			Msg.warn(ImportOps.class, "Import failed: " + e.getMessage(), e);
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				"Opcodex couldn't import " + name + ": " + safeMessage(e));
		}
	}

	private static JsonObject analyze(Workspace workspace, String programId) throws RpcException {
		return workspace.write(programId, "Auto-analysis", program -> {
			AutoAnalysisManager manager = AutoAnalysisManager.getAnalysisManager(program);
			manager.initializeOptions();
			manager.reAnalyzeAll(null);
			manager.startAnalysis(TaskMonitor.DUMMY);
			JsonObject out = new JsonObject();
			out.addProperty("programId", programId);
			out.addProperty("functionCount", program.getFunctionManager().getFunctionCount());
			return out;
		});
	}

	private static Path validatedFile(String path) throws RpcException {
		Path file = Path.of(path);
		if (!file.isAbsolute()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "path must be absolute.");
		}
		if (!Files.isRegularFile(file)) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No file at " + path);
		}
		try {
			if (Files.size(file) > MAX_IMPORT_BYTES) {
				throw new RpcException(ErrorCode.INVALID_PARAMS, "The file is larger than 2 GiB.");
			}
		}
		catch (java.io.IOException e) {
			throw new RpcException(ErrorCode.NOT_FOUND, "Can't read " + path);
		}
		return file;
	}

	private static String safeMessage(Exception e) {
		String message = e.getMessage();
		return message == null ? e.getClass().getSimpleName() : message;
	}
}

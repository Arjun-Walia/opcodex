/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

import com.google.gson.*;

import ghidra.app.script.GhidraScript;
import ghidra.program.model.address.Address;
import io.opcodex.engine.catalog.*;
import io.opcodex.engine.journal.JournalEntry;
import io.opcodex.engine.plugins.*;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.workspace.Workspace;

/** Plugins (engine operations, scripts and themes) and running scripts. */
public final class PluginOps {

	private static final Pattern SCRIPT_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]{0,99}\\.java");
	private static final long SCRIPT_TIME_LIMIT_MS = 10 * 60 * 1000;

	private static volatile PluginStore store = PluginStore.standard();
	private static volatile PluginLoader.Loaded loaded =
		new PluginLoader.Loaded(Map.of(), Map.of());
	private static volatile Set<String> enabledAtStart = Set.of();

	private PluginOps() {
	}

	/** Called once at engine start, after Community's operations are registered. */
	public static void loadPlugins(Catalog catalog, EngineServices services, ClassLoader parent) {
		store = PluginStore.standard();
		loaded = PluginLoader.load(store, catalog, services, parent);
		enabledAtStart = enabledWithOperations();
	}

	private static Set<String> enabledWithOperations() {
		Set<String> out = new TreeSet<>();
		store.list().stream().filter(p -> p.enabled() && !p.jars().isEmpty()).forEach(p -> out.add(p.id()));
		return out;
	}

	static List<Operation> operations(EngineServices services) {
		JsonObject none = Schemas.object();
		JsonObject install = Schemas.object();
		Schemas.property(install, "path", Schemas.string("A plugin folder, or a .zip file that holds one."), true);
		JsonObject byId = Schemas.object();
		Schemas.property(byId, "id", Schemas.string("Plugin id from plugin.list."), true);
		JsonObject enable = Schemas.object();
		Schemas.property(enable, "id", Schemas.string("Plugin id from plugin.list."), true);
		Schemas.property(enable, "enabled", Schemas.bool("Whether the plugin is on."), true);
		JsonObject importScript = Schemas.object();
		Schemas.property(importScript, "path", Schemas.string("A Java script file (.java) to copy into your scripts."),
			true);
		JsonObject run = FunctionOps.programParam();
		Schemas.property(run, "script", Schemas.string("Script id from script.list."), true);
		EditOps.revisionParam(run);
		Schemas.property(run, "args", Schemas.any("Arguments for the script, as a list of strings."), false);
		Schemas.property(run, "address", Schemas.string("The current address the script sees."), false);

		return List.of(
			new Operation("plugin.list",
				"Lists installed plugins with what each adds: operations, scripts and themes.",
				OperationClass.READ, none, Schemas.any("Plugins."), (ctx, p) -> list()),
			new Operation("plugin.install",
				"Installs a plugin from a folder or .zip. Plugins run with your permissions. Desktop only.",
				OperationClass.SYSTEM, install, Schemas.any("The installed plugin."),
				(ctx, p) -> guarded(() -> {
					PluginStore.Plugin plugin = store.install(Path.of(Params.requireString(p, "path")));
					JsonObject out = describe(plugin);
					out.addProperty("restartNeeded", !plugin.jars().isEmpty());
					return out;
				})),
			new Operation("plugin.setEnabled", "Turns a plugin on or off. Desktop only.", OperationClass.SYSTEM,
				enable, Schemas.any("Plugins."), (ctx, p) -> guarded(() -> {
					String id = Params.requireString(p, "id");
					store.find(id).orElseThrow(() -> new IllegalArgumentException("No plugin named " + id + "."));
					store.setEnabled(id, p.get("enabled").getAsBoolean());
					return list();
				})),
			new Operation("plugin.remove", "Removes a plugin. Desktop only.", OperationClass.SYSTEM, byId,
				Schemas.any("Plugins."), (ctx, p) -> guarded(() -> {
					store.remove(Params.requireString(p, "id"));
					return list();
				})),
			new Operation("plugin.themes", "Returns the theme files of enabled plugins.", OperationClass.READ, none,
				Schemas.any("Theme files."), (ctx, p) -> themes()),
			new Operation("script.list", "Lists the Java scripts you can run: your own and those of enabled plugins.",
				OperationClass.READ, none, Schemas.any("Scripts."), (ctx, p) -> scripts()),
			new Operation("script.import", "Copies a Java script into your scripts folder. Desktop only.",
				OperationClass.SYSTEM, importScript, Schemas.any("The script."), (ctx, p) -> guarded(() -> {
					Path from = Path.of(Params.requireString(p, "path"));
					String name = from.getFileName().toString();
					if (!SCRIPT_NAME.matcher(name).matches()) {
						throw new IllegalArgumentException("Choose a Java script whose file name is a class name, " +
							"for example MyScript.java.");
					}
					Files.createDirectories(store.scriptsDir());
					Path to = store.scriptsDir().resolve(name);
					if (Files.exists(to)) {
						throw new IllegalArgumentException(name + " is already in your scripts.");
					}
					Files.copy(from, to);
					JsonObject out = new JsonObject();
					out.addProperty("id", "user:" + name);
					return out;
				})),
			new Operation("script.run",
				"Runs a Java script against a program as one change. It can change anything, so agents need " +
					"the allow-all policy. Recorded in history; a failed script changes nothing.",
				OperationClass.RUN, run, Schemas.any("Output, and the new revision."),
				(ctx, p) -> runScript(services, ctx.actor(), p)));
	}

	private interface Body {
		JsonElement get() throws Exception;
	}

	private static JsonElement guarded(Body body) throws RpcException {
		try {
			return body.get();
		}
		catch (RpcException e) {
			throw e;
		}
		catch (IllegalArgumentException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, e.getMessage());
		}
		catch (NoSuchFileException e) {
			throw new RpcException(ErrorCode.NOT_FOUND, "Not found: " + e.getMessage());
		}
		catch (Exception e) {
			throw new RpcException(ErrorCode.ENGINE_ERROR, e.getMessage() == null ? e.toString() : e.getMessage());
		}
	}

	private static JsonObject list() {
		JsonObject out = new JsonObject();
		out.addProperty("folder", store.root().toString());
		out.addProperty("scriptsFolder", store.scriptsDir().toString());
		JsonArray plugins = new JsonArray();
		for (PluginStore.Plugin plugin : store.list()) {
			plugins.add(describe(plugin));
		}
		out.add("plugins", plugins);
		out.addProperty("restartNeeded", !enabledWithOperations().equals(enabledAtStart));
		return out;
	}

	private static JsonObject describe(PluginStore.Plugin plugin) {
		JsonObject o = new JsonObject();
		o.addProperty("id", plugin.id());
		o.addProperty("name", plugin.name());
		o.addProperty("version", plugin.version());
		o.addProperty("description", plugin.description());
		o.addProperty("author", plugin.author());
		o.addProperty("enabled", plugin.enabled());
		String error = plugin.error() != null ? plugin.error() : loaded.errors().get(plugin.id());
		if (error != null) {
			o.addProperty("error", error);
		}
		JsonArray ops = new JsonArray();
		loaded.operations().getOrDefault(plugin.id(), List.of()).forEach(ops::add);
		o.add("operations", ops);
		o.addProperty("hasOperations", !plugin.jars().isEmpty());
		o.add("scripts", names(plugin.scripts()));
		o.add("themes", names(plugin.themes()));
		return o;
	}

	private static JsonArray names(List<Path> files) {
		JsonArray out = new JsonArray();
		files.forEach(f -> out.add(f.getFileName().toString()));
		return out;
	}

	private static JsonArray themes() throws RpcException {
		JsonArray out = new JsonArray();
		for (PluginStore.Plugin plugin : store.list()) {
			if (!plugin.enabled()) {
				continue;
			}
			for (Path file : plugin.themes()) {
				try {
					JsonObject t = new JsonObject();
					t.addProperty("plugin", plugin.id());
					t.addProperty("file", file.getFileName().toString());
					t.addProperty("json", PluginStore.readTheme(file));
					out.add(t);
				}
				catch (Exception e) {
					// A broken theme file is skipped; the desktop validates the rest.
				}
			}
		}
		return out;
	}

	private static JsonArray scripts() {
		JsonArray out = new JsonArray();
		for (Path file : listJava(store.scriptsDir())) {
			out.add(script("user:" + file.getFileName(), file, null));
		}
		for (PluginStore.Plugin plugin : store.list()) {
			if (plugin.enabled()) {
				for (Path file : plugin.scripts()) {
					out.add(script("plugin:" + plugin.id() + ":" + file.getFileName(), file, plugin.id()));
				}
			}
		}
		return out;
	}

	private static List<Path> listJava(Path dir) {
		try (var s = Files.list(dir)) {
			return s.filter(f -> SCRIPT_NAME.matcher(f.getFileName().toString()).matches()).sorted().toList();
		}
		catch (Exception e) {
			return List.of();
		}
	}

	private static JsonObject script(String id, Path file, String plugin) {
		JsonObject o = new JsonObject();
		o.addProperty("id", id);
		o.addProperty("name", file.getFileName().toString().replaceFirst("\\.java$", ""));
		if (plugin != null) {
			o.addProperty("plugin", plugin);
		}
		o.addProperty("description", firstComment(file));
		return o;
	}

	/** The first line of the script's leading comment, if any, as its description. */
	private static String firstComment(Path file) {
		try {
			for (String line : Files.readAllLines(file)) {
				String t = line.trim();
				if (t.startsWith("//") && t.length() > 2) {
					return t.substring(2).replaceFirst("^\\s*@\\w+\\s*", "").trim();
				}
				if (!t.isEmpty() && !t.startsWith("/*") && !t.startsWith("*")) {
					break;
				}
			}
		}
		catch (Exception e) {
			// No description.
		}
		return "";
	}

	/** Resolves a script id to a file, only within the user's scripts or an enabled plugin. */
	static Path resolve(String id) throws RpcException {
		String[] parts = id.split(":");
		Path file = null;
		if (parts.length == 2 && parts[0].equals("user") && SCRIPT_NAME.matcher(parts[1]).matches()) {
			file = store.scriptsDir().resolve(parts[1]);
		}
		else if (parts.length == 3 && parts[0].equals("plugin") && SCRIPT_NAME.matcher(parts[2]).matches()) {
			PluginStore.Plugin plugin = store.find(parts[1]).filter(PluginStore.Plugin::enabled).orElse(null);
			if (plugin != null) {
				file = plugin.dir().resolve("scripts").resolve(parts[2]);
			}
		}
		if (file == null || !Files.isRegularFile(file)) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No script " + id + ". See script.list.");
		}
		return file;
	}

	/** Thrown inside the write so the transaction rolls back; RpcExceptions pass through unwrapped. */
	private static final class ScriptFailed extends RpcException {
		final transient ScriptRunner.Result result;

		ScriptFailed(ScriptRunner.Result result) {
			super(ErrorCode.ENGINE_ERROR, result.error());
			this.result = result;
		}
	}

	private static JsonElement runScript(EngineServices services, String actor, JsonObject p) throws RpcException {
		String programId = Params.requireString(p, "programId");
		String id = Params.requireString(p, "script");
		long expected = Params.requireLong(p, "expectedRevision");
		Path file = resolve(id);
		String[] args = args(p.get("args"));
		GhidraScript script;
		try {
			script = ScriptRunner.load(file);
		}
		catch (IllegalArgumentException e) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, e.getMessage());
		}
		catch (Exception e) {
			throw new RpcException(ErrorCode.ENGINE_ERROR, "Couldn't load the script: " + e.getMessage());
		}
		String addressText = Params.string(p, "address");
		var project = services.workspace().project();
		Workspace.Precheck precheck = (program, revision) -> {
			if (revision != expected) {
				throw new RpcException(ErrorCode.STALE_REVISION,
					"The program changed since revision " + expected + " (now " + revision + "). Read it again.");
			}
		};
		Workspace.Written<ScriptRunner.Result> written;
		try {
			written = services.workspace().write(programId, "Script " + file.getFileName(), precheck, program -> {
				Address at = addressText == null ? program.getMinAddress()
						: Params.address(program, "address", addressText);
				ScriptRunner.Result result = ScriptRunner.run(script, project, program, at, args,
					ScriptRunner.timeLimit(SCRIPT_TIME_LIMIT_MS));
				if (result.error() != null) {
					throw new ScriptFailed(result); // rolls the transaction back
				}
				return result;
			});
		}
		catch (ScriptFailed failed) {
			JsonObject out = new JsonObject();
			out.addProperty("ok", false);
			out.addProperty("output", failed.result.output());
			out.addProperty("error", failed.result.error());
			// Rolled back, but an aborted transaction still moves the revision: report the real one.
			out.addProperty("revision", services.workspace().get(programId).revision());
			return out;
		}
		JsonObject target = new JsonObject();
		target.addProperty("script", id);
		JsonPrimitive after = new JsonPrimitive(head(written.value().output(), 2000));
		JournalEntry entry = new JournalEntry(JournalEntry.newId(), JournalEntry.now(), actor, "script.run",
			programId, written.revisionBefore(), written.revisionAfter(), target, JsonNull.INSTANCE, after, null,
			"Ran script " + file.getFileName());
		EditOps.append(services, entry);
		JsonObject out = new JsonObject();
		out.addProperty("ok", true);
		out.addProperty("output", written.value().output());
		out.addProperty("revision", written.revisionAfter());
		out.addProperty("entryId", entry.id());
		return out;
	}

	private static String[] args(JsonElement given) throws RpcException {
		if (given == null || given.isJsonNull()) {
			return new String[0];
		}
		if (given.isJsonPrimitive()) {
			String text = given.getAsString().trim();
			return text.isEmpty() ? new String[0] : text.split("\\s+");
		}
		if (!given.isJsonArray()) {
			throw new RpcException(ErrorCode.INVALID_PARAMS, "args must be a list of strings.");
		}
		List<String> out = new ArrayList<>();
		given.getAsJsonArray().forEach(e -> out.add(e.getAsString()));
		return out.toArray(String[]::new);
	}

	private static String head(String text, int max) {
		return text.length() <= max ? text : text.substring(0, max) + "…";
	}
}

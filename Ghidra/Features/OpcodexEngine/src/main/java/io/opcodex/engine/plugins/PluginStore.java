/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.gson.*;

/**
 * Installed plugins: one folder each under the plugins folder, described by
 * {@code opcodex-plugin.json}. A plugin can carry engine operations ({@code lib/*.jar}), Java
 * scripts ({@code scripts/*.java}) and theme files ({@code themes/*.json}).
 */
public final class PluginStore {

	public static final String MANIFEST = "opcodex-plugin.json";
	private static final Pattern ID = Pattern.compile("[a-z0-9][a-z0-9-]{1,62}");
	private static final long MAX_INSTALL_BYTES = 256L * 1024 * 1024;
	private static final int MAX_ENTRIES = 5000;

	public record Plugin(String id, String name, String version, String description, String author, Path dir,
			boolean enabled, List<Path> jars, List<Path> scripts, List<Path> themes, String error) {
	}

	private final Path root;
	private final Path scriptsDir;

	public PluginStore(Path root, Path scriptsDir) {
		this.root = root;
		this.scriptsDir = scriptsDir;
	}

	/** %APPDATA%\Opcodex\plugins on Windows; OPCODEX_PLUGINS_DIR overrides. */
	public static PluginStore standard() {
		String override = System.getProperty("opcodex.plugins.dir", System.getenv("OPCODEX_PLUGINS_DIR"));
		Path base;
		if (override != null && !override.isBlank()) {
			Path plugins = Path.of(override);
			return new PluginStore(plugins, plugins.resolveSibling("scripts"));
		}
		String appData = System.getenv("APPDATA");
		base = appData != null ? Path.of(appData, "Opcodex")
				: Path.of(System.getProperty("user.home"), ".config", "opcodex");
		return new PluginStore(base.resolve("plugins"), base.resolve("scripts"));
	}

	public Path root() {
		return root;
	}

	/** Where the user's own scripts live (not part of any plugin). */
	public Path scriptsDir() {
		return scriptsDir;
	}

	public List<Plugin> list() {
		List<Plugin> out = new ArrayList<>();
		if (!Files.isDirectory(root)) {
			return out;
		}
		Set<String> disabled = disabled();
		try (Stream<Path> dirs = Files.list(root)) {
			dirs.filter(Files::isDirectory).sorted().forEach(dir -> out.add(read(dir, disabled)));
		}
		catch (IOException e) {
			// An unreadable plugins folder means no plugins.
		}
		return out;
	}

	public Optional<Plugin> find(String id) {
		return list().stream().filter(p -> p.id().equals(id)).findFirst();
	}

	private Plugin read(Path dir, Set<String> disabled) {
		String folder = dir.getFileName().toString();
		try {
			JsonObject m = JsonParser.parseString(Files.readString(dir.resolve(MANIFEST), StandardCharsets.UTF_8))
					.getAsJsonObject();
			String id = text(m, "id");
			if (!folder.equals(id)) {
				throw new IllegalArgumentException("The manifest id doesn't match its folder name.");
			}
			return new Plugin(id, text(m, "name"), text(m, "version"), optional(m, "description"),
				optional(m, "author"), dir, !disabled.contains(id), files(dir.resolve("lib"), ".jar"),
				files(dir.resolve("scripts"), ".java"), files(dir.resolve("themes"), ".json"), null);
		}
		catch (Exception e) {
			String why = e instanceof NoSuchFileException ? "It has no " + MANIFEST + "." : e.getMessage();
			return new Plugin(folder, folder, "", "", "", dir, false, List.of(), List.of(), List.of(), why);
		}
	}

	private static String text(JsonObject m, String key) {
		JsonElement e = m.get(key);
		if (e == null || !e.isJsonPrimitive() || e.getAsString().isBlank()) {
			throw new IllegalArgumentException("The manifest needs \"" + key + "\".");
		}
		return e.getAsString().trim();
	}

	private static String optional(JsonObject m, String key) {
		JsonElement e = m.get(key);
		return e != null && e.isJsonPrimitive() ? e.getAsString().trim() : "";
	}

	static List<Path> files(Path dir, String suffix) {
		if (!Files.isDirectory(dir)) {
			return List.of();
		}
		try (Stream<Path> s = Files.list(dir)) {
			return s.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().toLowerCase(Locale.ROOT)
					.endsWith(suffix)).sorted().toList();
		}
		catch (IOException e) {
			return List.of();
		}
	}

	/** Installs a plugin from a folder or a .zip that holds the manifest at its top level. */
	public Plugin install(Path source) throws IOException {
		Path staging = Files.createTempDirectory("opcodex-plugin-");
		try {
			if (Files.isDirectory(source)) {
				copyTree(source, staging);
			}
			else if (source.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".zip")) {
				unzip(source, staging);
			}
			else {
				throw new IllegalArgumentException("Choose a plugin folder or a .zip file.");
			}
			Path top = staging;
			if (!Files.exists(top.resolve(MANIFEST))) {
				// Allow a zip that wraps everything in one folder.
				try (Stream<Path> children = Files.list(staging)) {
					List<Path> dirs = children.filter(Files::isDirectory).toList();
					if (dirs.size() == 1 && Files.exists(dirs.get(0).resolve(MANIFEST))) {
						top = dirs.get(0);
					}
				}
			}
			JsonObject m = JsonParser.parseString(Files.readString(top.resolve(MANIFEST), StandardCharsets.UTF_8))
					.getAsJsonObject();
			String id = text(m, "id");
			text(m, "name");
			text(m, "version");
			if (!ID.matcher(id).matches()) {
				throw new IllegalArgumentException(
					"A plugin id uses lower-case letters, digits and dashes, 2 to 63 characters.");
			}
			Path target = root.resolve(id);
			if (Files.exists(target)) {
				throw new IllegalArgumentException("A plugin named " + id + " is already installed. Remove it first.");
			}
			Files.createDirectories(root);
			copyTree(top, target);
			return read(target, disabled());
		}
		catch (NoSuchFileException e) {
			throw new IllegalArgumentException("That isn't a plugin: there's no " + MANIFEST + ".");
		}
		catch (JsonParseException | IllegalStateException e) {
			throw new IllegalArgumentException("The plugin's " + MANIFEST + " isn't valid JSON.");
		}
		finally {
			deleteTree(staging);
		}
	}

	public void remove(String id) throws IOException {
		Plugin plugin = find(id).orElseThrow(() -> new IllegalArgumentException("No plugin named " + id + "."));
		deleteTree(plugin.dir());
		setEnabled(id, true); // forget its state
	}

	public void setEnabled(String id, boolean enabled) throws IOException {
		Set<String> disabled = new TreeSet<>(disabled());
		if (enabled) {
			disabled.remove(id);
		}
		else {
			disabled.add(id);
		}
		JsonObject state = new JsonObject();
		JsonArray list = new JsonArray();
		disabled.forEach(list::add);
		state.add("disabled", list);
		Files.createDirectories(root);
		Files.writeString(root.resolve(".state.json"), state.toString(), StandardCharsets.UTF_8);
	}

	private Set<String> disabled() {
		try {
			JsonObject state = JsonParser.parseString(
				Files.readString(root.resolve(".state.json"), StandardCharsets.UTF_8)).getAsJsonObject();
			Set<String> out = new HashSet<>();
			state.getAsJsonArray("disabled").forEach(e -> out.add(e.getAsString()));
			return out;
		}
		catch (Exception e) {
			return Set.of();
		}
	}

	private static void unzip(Path zip, Path into) throws IOException {
		Path base = into.toAbsolutePath().normalize();
		long total = 0;
		int count = 0;
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
			ZipEntry entry;
			byte[] buffer = new byte[64 * 1024];
			while ((entry = in.getNextEntry()) != null) {
				if (++count > MAX_ENTRIES) {
					throw new IllegalArgumentException("The plugin has too many files.");
				}
				Path out = base.resolve(entry.getName()).normalize();
				if (!out.startsWith(base) || entry.getName().contains("..")) {
					throw new IllegalArgumentException("The plugin zip has an unsafe path: " + entry.getName());
				}
				if (entry.isDirectory()) {
					Files.createDirectories(out);
					continue;
				}
				Files.createDirectories(out.getParent());
				try (var file = Files.newOutputStream(out)) {
					int n;
					while ((n = in.read(buffer)) > 0) {
						total += n;
						if (total > MAX_INSTALL_BYTES) {
							throw new IllegalArgumentException("The plugin is larger than 256 MB.");
						}
						file.write(buffer, 0, n);
					}
				}
			}
		}
	}

	private static void copyTree(Path from, Path to) throws IOException {
		try (Stream<Path> walk = Files.walk(from)) {
			for (Path p : (Iterable<Path>) walk::iterator) {
				if (Files.isSymbolicLink(p)) {
					continue; // never follow links out of the plugin
				}
				Path target = to.resolve(from.relativize(p).toString());
				if (Files.isDirectory(p)) {
					Files.createDirectories(target);
				}
				else {
					Files.createDirectories(target.getParent());
					Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	static void deleteTree(Path dir) throws IOException {
		if (!Files.exists(dir)) {
			return;
		}
		try (Stream<Path> walk = Files.walk(dir)) {
			for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
				Files.deleteIfExists(p);
			}
		}
	}

	/** Reads a theme file of a plugin, refusing anything larger than a theme could be. */
	public static String readTheme(Path file) throws IOException {
		if (Files.size(file) > 64 * 1024) {
			throw new IOException("Theme file too large: " + file.getFileName());
		}
		try (InputStream in = Files.newInputStream(file)) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}

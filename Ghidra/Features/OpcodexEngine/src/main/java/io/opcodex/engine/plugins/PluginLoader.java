/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import ghidra.util.Msg;
import io.opcodex.engine.catalog.Catalog;
import io.opcodex.engine.catalog.Operation;
import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.spi.OperationProvider;

/**
 * Loads the engine operations of enabled plugins at engine start. Each plugin gets its own class
 * loader, and a plugin that fails to load is reported without stopping the others.
 */
public final class PluginLoader {

	public record Loaded(Map<String, List<String>> operations, Map<String, String> errors,
			List<URLClassLoader> loaders) {

		public static Loaded none() {
			return new Loaded(Map.of(), Map.of(), List.of());
		}

		/** Releases the plugins' jars (Windows keeps open files locked). */
		public void close() {
			for (URLClassLoader loader : loaders) {
				try {
					loader.close();
				}
				catch (Exception e) {
					// Nothing more to do; the process ending releases it.
				}
			}
		}
	}

	private PluginLoader() {
	}

	public static Loaded load(PluginStore store, Catalog catalog, EngineServices services, ClassLoader parent) {
		Map<String, List<String>> operations = new TreeMap<>();
		Map<String, String> errors = new TreeMap<>();
		List<URLClassLoader> loaders = new ArrayList<>();
		for (PluginStore.Plugin plugin : store.list()) {
			if (!plugin.enabled() || plugin.jars().isEmpty()) {
				continue;
			}
			try {
				URL[] urls = new URL[plugin.jars().size()];
				for (int i = 0; i < urls.length; i++) {
					urls[i] = plugin.jars().get(i).toUri().toURL();
				}
				URLClassLoader loader = new URLClassLoader("plugin:" + plugin.id(), urls, parent);
				try {
					// Collect everything first: a plugin's operations register all together or not at all.
					List<Operation> ops = new ArrayList<>();
					Set<String> seen = new HashSet<>();
					for (OperationProvider provider : ServiceLoader.load(OperationProvider.class, loader)) {
						if (provider.getClass().getClassLoader() != loader) {
							continue; // Community's own providers, seen through the parent
						}
						for (Operation op : provider.operations(services)) {
							if (catalog.find(op.name()).isPresent() || !seen.add(op.name())) {
								throw new IllegalStateException("Operation " + op.name() + " already exists.");
							}
							ops.add(op);
						}
					}
					ops.forEach(catalog::register);
					loaders.add(loader);
					operations.put(plugin.id(), ops.stream().map(Operation::name).toList());
				}
				catch (Throwable t) {
					loader.close();
					throw t;
				}
			}
			catch (Throwable t) {
				String message = t.getMessage() == null ? t.toString() : t.getMessage();
				errors.put(plugin.id(), message);
				Msg.error(PluginLoader.class, "Plugin " + plugin.id() + " didn't load: " + message, t);
			}
		}
		return new Loaded(operations, errors, loaders);
	}
}

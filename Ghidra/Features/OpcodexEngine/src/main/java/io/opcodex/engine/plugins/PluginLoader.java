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

	public record Loaded(Map<String, List<String>> operations, Map<String, String> errors) {
	}

	private PluginLoader() {
	}

	public static Loaded load(PluginStore store, Catalog catalog, EngineServices services, ClassLoader parent) {
		Map<String, List<String>> operations = new TreeMap<>();
		Map<String, String> errors = new TreeMap<>();
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
				List<String> names = new ArrayList<>();
				for (OperationProvider provider : ServiceLoader.load(OperationProvider.class, loader)) {
					if (provider.getClass().getClassLoader() != loader) {
						continue; // Community's own providers, seen through the parent
					}
					for (Operation op : provider.operations(services)) {
						if (catalog.find(op.name()).isPresent()) {
							throw new IllegalStateException("Operation " + op.name() + " already exists.");
						}
						catalog.register(op);
						names.add(op.name());
					}
				}
				operations.put(plugin.id(), names);
			}
			catch (Throwable t) {
				String message = t.getMessage() == null ? t.toString() : t.getMessage();
				errors.put(plugin.id(), message);
				Msg.error(PluginLoader.class, "Plugin " + plugin.id() + " didn't load: " + message, t);
			}
		}
		return new Loaded(operations, errors);
	}
}

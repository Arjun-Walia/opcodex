/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

import java.util.*;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.opcodex.engine.spi.EngineServices;
import io.opcodex.engine.spi.OperationProvider;

/** Registry of every operation the engine serves. */
public final class Catalog {

	private static final Pattern NAME = Pattern.compile("^[a-z][a-z0-9]*(\\.[a-z][a-zA-Z0-9]*)+$");

	private final Map<String, Operation> operations = new LinkedHashMap<>();

	public synchronized void register(Operation operation) {
		if (!NAME.matcher(operation.name()).matches()) {
			throw new IllegalArgumentException("Invalid operation name: " + operation.name());
		}
		if (operations.putIfAbsent(operation.name(), operation) != null) {
			throw new IllegalArgumentException("Duplicate operation: " + operation.name());
		}
	}

	public synchronized Optional<Operation> find(String name) {
		return Optional.ofNullable(operations.get(name));
	}

	public synchronized List<Operation> all() {
		return List.copyOf(operations.values());
	}

	/** Registers every operation from the providers found by {@link ServiceLoader}. */
	public void loadProviders(EngineServices services, ClassLoader loader) {
		for (OperationProvider provider : ServiceLoader.load(OperationProvider.class, loader)) {
			provider.operations(services).forEach(this::register);
		}
	}

	public synchronized JsonArray describe() {
		JsonArray out = new JsonArray();
		for (Operation op : operations.values()) {
			JsonObject entry = new JsonObject();
			entry.addProperty("name", op.name());
			entry.addProperty("description", op.description());
			entry.addProperty("class", op.cls().wireName());
			entry.add("params", op.paramsSchema().deepCopy());
			entry.add("result", op.resultSchema().deepCopy());
			out.add(entry);
		}
		return out;
	}
}

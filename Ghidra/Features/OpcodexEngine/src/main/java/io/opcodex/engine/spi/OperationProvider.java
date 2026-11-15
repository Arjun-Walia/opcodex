/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import java.util.List;

import io.opcodex.engine.catalog.Operation;

/** Contributes catalog operations; discovered with {@link java.util.ServiceLoader}. */
public interface OperationProvider {
	List<Operation> operations(EngineServices services);
}

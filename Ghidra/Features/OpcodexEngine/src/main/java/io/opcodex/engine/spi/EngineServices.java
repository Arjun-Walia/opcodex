/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import io.opcodex.engine.catalog.Catalog;
import io.opcodex.engine.workspace.Workspace;

/** Engine services available to operation handlers. */
public interface EngineServices {

	Catalog catalog();

	Workspace workspace();

	EntitlementProvider entitlements();
}

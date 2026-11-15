/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import java.util.Set;

/** Reports which paid features are unlocked. Community ships an implementation with none. */
public interface EntitlementProvider {

	/** The Community implementation: no paid features. */
	EntitlementProvider COMMUNITY = Set::of;

	Set<String> features();
}

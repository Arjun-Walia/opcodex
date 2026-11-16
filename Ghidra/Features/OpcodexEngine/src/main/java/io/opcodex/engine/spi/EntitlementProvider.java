/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/**
 * Reports which paid features are unlocked. Community ships {@link #COMMUNITY}, with none; a paid
 * edition installs its own implementation through {@link ServiceLoader}.
 */
public interface EntitlementProvider {

	/** The Community implementation: no paid features. */
	EntitlementProvider COMMUNITY = Set::of;

	Set<String> features();

	/** Refuses a call that needs {@code feature} when it isn't unlocked. */
	default void require(String feature) throws RpcException {
		if (!features().contains(feature)) {
			throw new RpcException(ErrorCode.ENTITLEMENT_REQUIRED,
				"This needs Opcodex Pro. See https://opcodex.xyz/pricing/");
		}
	}

	/** The installed provider, or {@link #COMMUNITY} when none is installed. */
	static EntitlementProvider load(ClassLoader loader) {
		Iterator<EntitlementProvider> found = ServiceLoader.load(EntitlementProvider.class, loader).iterator();
		return found.hasNext() ? found.next() : COMMUNITY;
	}
}

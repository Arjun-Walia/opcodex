/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.spi;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.Test;

import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

public class EntitlementProviderTest {

	/** Stands in for a paid edition's provider. */
	public static final class Unlocked implements EntitlementProvider {
		@Override
		public Set<String> features() {
			return Set.of("recovery");
		}
	}

	@Test
	public void communityHasNoPaidFeatures() {
		RpcException refused = assertThrows(RpcException.class,
			() -> EntitlementProvider.COMMUNITY.require("recovery"));
		assertEquals(ErrorCode.ENTITLEMENT_REQUIRED, refused.code());
	}

	@Test
	public void withoutAnInstalledProviderTheEngineIsCommunity() {
		assertSame(EntitlementProvider.COMMUNITY, EntitlementProvider.load(getClass().getClassLoader()));
	}

	@Test
	public void anInstalledProviderIsUsed() throws Exception {
		Path dir = Files.createTempDirectory("opcodex-spi");
		Path services = dir.resolve("META-INF/services/" + EntitlementProvider.class.getName());
		Files.createDirectories(services.getParent());
		Files.writeString(services, Unlocked.class.getName() + "\n");
		try (URLClassLoader loader = new URLClassLoader(new URL[] { dir.toUri().toURL() }, getClass().getClassLoader())) {
			EntitlementProvider provider = EntitlementProvider.load(loader);
			assertEquals(Set.of("recovery"), provider.features());
			provider.require("recovery");
		}
	}
}

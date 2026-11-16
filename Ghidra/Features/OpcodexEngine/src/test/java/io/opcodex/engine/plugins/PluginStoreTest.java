/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.plugins;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Before;
import org.junit.Test;

public class PluginStoreTest {

	private Path home;
	private PluginStore store;

	@Before
	public void setUp() throws Exception {
		home = Files.createTempDirectory("opcodex-plugins-test");
		store = new PluginStore(home.resolve("plugins"), home.resolve("scripts"));
	}

	private Path pluginFolder(String id) throws Exception {
		Path dir = Files.createTempDirectory("src-" + id);
		Files.writeString(dir.resolve(PluginStore.MANIFEST),
			"{\"id\":\"" + id + "\",\"name\":\"Acme\",\"version\":\"1.0.0\",\"description\":\"Tools\"}");
		Files.createDirectories(dir.resolve("scripts"));
		Files.writeString(dir.resolve("scripts/Hello.java"), "// script");
		Files.createDirectories(dir.resolve("themes"));
		Files.writeString(dir.resolve("themes/night.json"), "{}");
		return dir;
	}

	@Test
	public void installsAFolderAndListsWhatItCarries() throws Exception {
		PluginStore.Plugin p = store.install(pluginFolder("acme-tools"));
		assertEquals("acme-tools", p.id());
		assertTrue(p.enabled());
		assertEquals(1, p.scripts().size());
		assertEquals(1, p.themes().size());
		assertNull(p.error());
		assertEquals(1, store.list().size());
	}

	@Test
	public void installsAZipWrappedInOneFolder() throws Exception {
		Path zip = home.resolve("p.zip");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
			out.putNextEntry(new ZipEntry("acme/" + PluginStore.MANIFEST));
			out.write("{\"id\":\"acme\",\"name\":\"Acme\",\"version\":\"2\"}".getBytes(StandardCharsets.UTF_8));
			out.closeEntry();
		}
		assertEquals("acme", store.install(zip).id());
	}

	@Test
	public void refusesZipEntriesThatEscapeThePluginFolder() throws Exception {
		Path zip = home.resolve("evil.zip");
		try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zip))) {
			out.putNextEntry(new ZipEntry("../../outside.txt"));
			out.write(1);
			out.closeEntry();
		}
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> store.install(zip));
		assertTrue(e.getMessage().contains("unsafe path"));
		assertFalse(Files.exists(home.resolve("outside.txt")));
	}

	@Test
	public void refusesBadIdsAndDuplicates() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> store.install(pluginFolder("Bad_ID")));
		store.install(pluginFolder("acme"));
		assertThrows(IllegalArgumentException.class, () -> store.install(pluginFolder("acme")));
	}

	@Test
	public void refusesFoldersWithoutAManifest() throws Exception {
		Path empty = Files.createTempDirectory("empty");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> store.install(empty));
		assertTrue(e.getMessage().contains("opcodex-plugin.json"));
	}

	@Test
	public void disablesEnablesAndRemoves() throws Exception {
		store.install(pluginFolder("acme"));
		store.setEnabled("acme", false);
		assertFalse(store.find("acme").orElseThrow().enabled());
		store.setEnabled("acme", true);
		assertTrue(store.find("acme").orElseThrow().enabled());
		store.remove("acme");
		assertTrue(store.list().isEmpty());
	}

	@Test
	public void aBrokenPluginIsListedWithItsError() throws Exception {
		Files.createDirectories(home.resolve("plugins/broken"));
		PluginStore.Plugin p = store.list().get(0);
		assertEquals("broken", p.id());
		assertFalse(p.enabled());
		assertNotNull(p.error());
	}
}

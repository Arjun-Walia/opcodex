/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class SessionFilesTest {

	@Test
	public void sessionFilesAreUserOnly() throws Exception {
		Path dir = Files.createTempDirectory("opcodex-session");
		SessionFiles.write(dir, "secret-token", 41234, "community", "1.0");

		Path token = dir.resolve(SessionFiles.TOKEN_FILE);
		Path engine = dir.resolve(SessionFiles.ENGINE_FILE);
		assertEquals("secret-token", Files.readString(token, StandardCharsets.UTF_8).trim());
		JsonObject info = JsonParser.parseString(Files.readString(engine)).getAsJsonObject();
		assertEquals(41234, info.get("port").getAsInt());
		assertEquals(ProcessHandle.current().pid(), info.get("pid").getAsLong());
		assertEquals("community", info.get("edition").getAsString());
		assertEquals("1.0", info.get("apiVersion").getAsString());

		for (Path file : List.of(token, engine)) {
			PosixFileAttributeView posix =
				Files.getFileAttributeView(file, PosixFileAttributeView.class);
			if (posix != null) {
				assertEquals(Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
					posix.readAttributes().permissions());
				continue;
			}
			AclFileAttributeView acl = Files.getFileAttributeView(file, AclFileAttributeView.class);
			assertNotNull("Expected POSIX or ACL file attributes", acl);
			UserPrincipal owner = file.getFileSystem()
					.getUserPrincipalLookupService()
					.lookupPrincipalByName(System.getProperty("user.name"));
			List<AclEntry> entries = acl.getAcl();
			assertFalse(entries.isEmpty());
			for (AclEntry entry : entries) {
				assertEquals(owner, entry.principal());
			}
		}
	}
}

/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.EnumSet;
import java.util.List;

import com.google.gson.JsonObject;

/**
 * Writes the files a CLI or MCP client uses to find and authenticate to a running engine host.
 * Both files are readable only by the current user.
 */
public final class SessionFiles {

	public static final String TOKEN_FILE = "client.token";
	public static final String ENGINE_FILE = "engine.json";

	private SessionFiles() {
	}

	public static void write(Path dir, String clientToken, int port, String edition,
			String apiVersion) throws IOException {
		Files.createDirectories(dir);
		restrictToUser(dir, true);
		writePrivate(dir.resolve(TOKEN_FILE), clientToken + "\n");
		JsonObject info = new JsonObject();
		info.addProperty("port", port);
		info.addProperty("pid", ProcessHandle.current().pid());
		info.addProperty("edition", edition);
		info.addProperty("apiVersion", apiVersion);
		writePrivate(dir.resolve(ENGINE_FILE), info.toString() + "\n");
	}

	/** Removes the session files, if this process wrote them. */
	public static void delete(Path dir) {
		for (String name : List.of(ENGINE_FILE, TOKEN_FILE)) {
			try {
				Files.deleteIfExists(dir.resolve(name));
			}
			catch (IOException e) {
				// Best effort at shutdown; a stale file is detected by its pid.
			}
		}
	}

	/** Default per-user session directory for this platform. */
	public static Path defaultDir() {
		String override = System.getenv("OPCODEX_SESSION_DIR");
		if (override != null && !override.isBlank()) {
			return Path.of(override);
		}
		String os = System.getProperty("os.name", "").toLowerCase();
		if (os.contains("win")) {
			String local = System.getenv("LOCALAPPDATA");
			Path base = local != null ? Path.of(local)
					: Path.of(System.getProperty("user.home"), "AppData", "Local");
			return base.resolve("Opcodex").resolve("engine");
		}
		String runtime = System.getenv("XDG_RUNTIME_DIR");
		if (runtime != null && !runtime.isBlank()) {
			return Path.of(runtime, "opcodex");
		}
		return Path.of(System.getProperty("user.home"), ".local", "state", "opcodex", "engine");
	}

	private static void writePrivate(Path file, String content) throws IOException {
		Path temp = Files.createTempFile(file.getParent(), ".opcodex", ".tmp");
		try {
			restrictToUser(temp, false);
			Files.writeString(temp, content, StandardCharsets.UTF_8);
			Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.ATOMIC_MOVE);
		}
		finally {
			Files.deleteIfExists(temp);
		}
		restrictToUser(file, false);
	}

	private static void restrictToUser(Path path, boolean directory) throws IOException {
		PosixFileAttributeView posix =
			Files.getFileAttributeView(path, PosixFileAttributeView.class);
		if (posix != null) {
			EnumSet<PosixFilePermission> perms =
				EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
			if (directory) {
				perms.add(PosixFilePermission.OWNER_EXECUTE);
			}
			posix.setPermissions(perms);
			return;
		}
		AclFileAttributeView acl = Files.getFileAttributeView(path, AclFileAttributeView.class);
		if (acl == null) {
			throw new IOException("Cannot restrict permissions on " + path);
		}
		UserPrincipal user = path.getFileSystem()
				.getUserPrincipalLookupService()
				.lookupPrincipalByName(System.getProperty("user.name"));
		AclEntry.Builder entry = AclEntry.newBuilder()
				.setType(AclEntryType.ALLOW)
				.setPrincipal(user)
				.setPermissions(EnumSet.allOf(AclEntryPermission.class));
		if (directory) {
			entry.setFlags(AclEntryFlag.FILE_INHERIT, AclEntryFlag.DIRECTORY_INHERIT);
		}
		acl.setAcl(List.of(entry.build()));
	}
}

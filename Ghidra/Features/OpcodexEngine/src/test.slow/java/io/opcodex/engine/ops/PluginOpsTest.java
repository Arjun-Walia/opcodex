/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.ops;

import static io.opcodex.engine.EngineHarness.params;
import static org.junit.Assert.*;

import java.nio.file.*;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.*;

import com.google.gson.*;

import ghidra.program.database.ProgramBuilder;
import ghidra.test.AbstractGhidraHeadlessIntegrationTest;
import io.opcodex.engine.EngineHarness;

public class PluginOpsTest extends AbstractGhidraHeadlessIntegrationTest {

	private Path home;
	private EngineHarness engine;
	private ProgramBuilder builder;
	private String programId;

	@Before
	public void setUp() throws Exception {
		home = Files.createTempDirectory("opcodex-plugin-ops");
		System.setProperty("opcodex.plugins.dir", home.resolve("plugins").toString());
		Files.createDirectories(home.resolve("scripts"));
		Files.writeString(home.resolve("scripts/TagEntry.java"), """
			// Tags the current address with a comment.
			import ghidra.app.script.GhidraScript;
			public class TagEntry extends GhidraScript {
				@Override
				protected void run() throws Exception {
					setEOLComment(currentAddress, "tagged by script");
					println("tagged " + currentAddress);
				}
			}
			""");
		Files.writeString(home.resolve("scripts/Breaks.java"), """
			import ghidra.app.script.GhidraScript;
			public class Breaks extends GhidraScript {
				@Override
				protected void run() throws Exception {
					setEOLComment(currentAddress, "should not stay");
					throw new IllegalStateException("deliberate failure");
				}
			}
			""");
		start();
	}

	private void start() throws Exception {
		if (engine != null) {
			engine.close();
		}
		engine = new EngineHarness(Files.createTempDirectory("projects"));
		engine.desktop("project.create", params("dir", engine.projectsDir().toString(), "name", "plugins"));
		if (builder == null) {
			builder = GraphOpsTest.branchyProgram(this);
		}
		programId = engine.adopt(builder.getProgram());
	}

	@After
	public void tearDown() {
		engine.close();
		builder.dispose();
		System.clearProperty("opcodex.plugins.dir");
	}

	@Test
	public void runsAScriptAsOneRecordedChange() {
		JsonArray scripts = engine.client("script.list", params()).getAsJsonArray();
		assertTrue(scripts.toString().contains("user:TagEntry.java"));
		assertTrue(scripts.toString().contains("Tags the current address"));
		long revision = engine.revision(programId);
		JsonObject run = engine.desktop("script.run",
			params("programId", programId, "script", "user:TagEntry.java", "expectedRevision", revision, "address", "0x1000"))
				.getAsJsonObject();
		assertTrue(run.toString(), run.get("ok").getAsBoolean());
		assertTrue(run.get("output").getAsString().contains("tagged 00001000"));
		assertNotEquals(revision, run.get("revision").getAsLong());
		assertEquals("tagged by script",
			builder.getProgram().getListing().getComment(ghidra.program.model.listing.CommentType.EOL,
				builder.getProgram().getAddressFactory().getAddress("0x1000")));
		JsonObject history = engine.client("history.list", params("programId", programId)).getAsJsonObject();
		assertTrue(history.toString().contains("Ran script TagEntry.java"));
		JsonObject revert = engine.raw(EngineHarness.DESKTOP, null, "history.revert", params("programId", programId,
			"entryId", run.get("entryId").getAsString(), "expectedRevision", run.get("revision").getAsLong()));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(revert));
	}

	@Test
	public void aFailingScriptChangesNothing() {
		long revision = engine.revision(programId);
		JsonObject run = engine.desktop("script.run",
			params("programId", programId, "script", "user:Breaks.java", "expectedRevision", revision, "address", "0x1000"))
				.getAsJsonObject();
		assertFalse(run.get("ok").getAsBoolean());
		assertTrue(run.get("error").getAsString().contains("deliberate failure"));
		assertEquals(engine.revision(programId), run.get("revision").getAsLong());
		assertNull(builder.getProgram().getListing().getComment(ghidra.program.model.listing.CommentType.EOL,
			builder.getProgram().getAddressFactory().getAddress("0x1000")));
	}

	@Test
	public void agentsNeedAllowAllToRunScriptsAndCannotInstallPlugins() {
		long revision = engine.revision(programId);
		JsonObject run = engine.raw(EngineHarness.CLIENT, "mcp:test", "script.run",
			params("programId", programId, "script", "user:TagEntry.java", "expectedRevision", revision));
		assertEquals("APPROVAL_REQUIRED", EngineHarness.errorCode(run));
		JsonObject install = engine.raw(EngineHarness.CLIENT, "mcp:test", "plugin.install",
			params("path", home.toString()));
		assertEquals("FORBIDDEN", EngineHarness.errorCode(install));
	}

	@Test
	public void scriptsOutsideTheScriptFoldersCannotBeRun() {
		JsonObject run = engine.raw(EngineHarness.DESKTOP, null, "script.run", params("programId", programId,
			"script", "user:..\\..\\Windows.java", "expectedRevision", engine.revision(programId)));
		assertEquals("NOT_FOUND", EngineHarness.errorCode(run));
	}

	@Test
	public void aPluginAddsOperationsAfterARestart() throws Exception {
		Path source = home.resolve("acme");
		Files.createDirectories(source.resolve("lib"));
		Files.writeString(source.resolve("opcodex-plugin.json"),
			"{\"id\":\"acme\",\"name\":\"Acme\",\"version\":\"1.0.0\"}");
		buildProviderJar(source.resolve("lib/acme.jar"));

		JsonObject installed = engine.desktop("plugin.install", params("path", source.toString())).getAsJsonObject();
		assertTrue(installed.get("restartNeeded").getAsBoolean());
		assertTrue(engine.client("plugin.list", params()).getAsJsonObject().get("restartNeeded").getAsBoolean());

		start(); // the engine loads plugin operations when it starts
		JsonObject listed = engine.client("plugin.list", params()).getAsJsonObject();
		assertTrue(listed.toString(), listed.toString().contains("acme.hello"));
		assertEquals("hello from acme", engine.client("acme.hello", params()).getAsJsonObject()
				.get("message").getAsString());

		engine.desktop("plugin.setEnabled", params("id", "acme", "enabled", false));
		start();
		JsonObject gone = engine.raw(EngineHarness.CLIENT, null, "acme.hello", params());
		assertEquals("-32601", EngineHarness.errorCode(gone)); // method not found
	}

	/** Compiles a tiny OperationProvider into a plugin jar, as a plugin author would. */
	private static void buildProviderJar(Path jar) throws Exception {
		Path src = Files.createTempDirectory("acme-src");
		Path pkg = src.resolve("acme");
		Files.createDirectories(pkg);
		Files.writeString(pkg.resolve("AcmeOps.java"), """
			package acme;
			import java.util.List;
			import com.google.gson.JsonObject;
			import io.opcodex.engine.catalog.*;
			import io.opcodex.engine.spi.*;
			public class AcmeOps implements OperationProvider {
				public List<Operation> operations(EngineServices services) {
					return List.of(new Operation("acme.hello", "Says hello.", OperationClass.READ,
						Schemas.object(), Schemas.any("A greeting."), (ctx, p) -> {
							JsonObject o = new JsonObject();
							o.addProperty("message", "hello from acme");
							return o;
						}));
				}
			}
			""");
		Path classes = Files.createTempDirectory("acme-classes");
		JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
		// Compile against the engine and Gson exactly as loaded, like a plugin built against a release.
		String cp = String.join(java.io.File.pathSeparator,
			Path.of(io.opcodex.engine.catalog.Operation.class.getProtectionDomain().getCodeSource().getLocation().toURI())
					.toString(),
			Path.of(JsonObject.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString());
		int status = javac.run(null, null, null, "-d", classes.toString(), "-cp", cp,
			pkg.resolve("AcmeOps.java").toString());
		assertEquals(0, status);
		try (JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
			out.putNextEntry(new JarEntry("acme/AcmeOps.class"));
			out.write(Files.readAllBytes(classes.resolve("acme/AcmeOps.class")));
			out.closeEntry();
			out.putNextEntry(new JarEntry("META-INF/services/io.opcodex.engine.spi.OperationProvider"));
			out.write("acme.AcmeOps\n".getBytes());
			out.closeEntry();
		}
	}
}

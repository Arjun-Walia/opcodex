/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.workspace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

import ghidra.base.project.GhidraProject;
import ghidra.framework.model.*;
import ghidra.framework.store.LockException;
import ghidra.program.model.listing.Program;
import ghidra.util.Msg;
import ghidra.util.task.TaskMonitor;
import io.opcodex.engine.journal.Journal;
import io.opcodex.engine.jobs.JobManager;
import io.opcodex.engine.rpc.ErrorCode;
import io.opcodex.engine.rpc.RpcException;

/**
 * The engine's open project and its programs. Reads may run on any thread; every change to a
 * program runs on that program's writer thread inside one transaction and is saved on commit.
 */
public final class Workspace implements AutoCloseable {

	/** A change applied to a program inside a transaction. */
	@FunctionalInterface
	public interface WriteBody<T> {
		T apply(Program program) throws Exception;
	}

	/**
	 * Runs on the writer thread before the transaction starts. Throwing here refuses the change
	 * without touching the program, so a refused write never moves the revision.
	 */
	@FunctionalInterface
	public interface Precheck {
		void check(Program program, long revision) throws RpcException;
	}

	/** A committed change with the revisions read on the writer thread around it. */
	public record Written<T>(T value, long revisionBefore, long revisionAfter) {
	}

	private final Map<String, ProgramHandle> open = new ConcurrentHashMap<>();
	private final JobManager jobs = new JobManager();
	private GhidraProject project;
	private Path projectDir;
	private String projectName;
	private Journal journal;

	/**
	 * Creates a new project; refuses to touch an existing one. A client may only create a
	 * project when none is open, so it can't close one that someone else is using.
	 */
	public synchronized void create(Path dir, String name, boolean mayReplaceOpen)
			throws RpcException {
		checkName(name);
		if (project != null && !mayReplaceOpen) {
			throw busy();
		}
		if (Files.exists(dir.resolve(name + ".gpr")) || Files.exists(dir.resolve(name + ".rep"))) {
			throw new RpcException(ErrorCode.CONFLICT,
				"A project named " + name + " already exists in " + dir + ".");
		}
		close();
		try {
			Files.createDirectories(dir);
			project = GhidraProject.createProject(dir.toString(), name, false);
		}
		catch (IOException e) {
			throw engineError("Could not create the project", e);
		}
		adopt(dir, name);
	}

	/** Opens a project. Opening the project that is already open changes nothing. */
	public synchronized void open(Path dir, String name, boolean mayReplaceOpen)
			throws RpcException {
		checkName(name);
		if (project != null && dir.equals(projectDir) && name.equals(projectName)) {
			return;
		}
		if (project != null && !mayReplaceOpen) {
			throw busy();
		}
		if (!Files.exists(dir.resolve(name + ".gpr"))) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No project named " + name + " in " + dir);
		}
		close();
		try {
			project = GhidraProject.openProject(dir.toString(), name, false);
		}
		catch (LockException e) {
			throw new RpcException(ErrorCode.CONFLICT,
				"The project is open in another program. Close it there first.");
		}
		catch (Exception e) {
			throw engineError("Could not open the project", e);
		}
		adopt(dir, name);
	}

	private RpcException busy() {
		return new RpcException(ErrorCode.CONFLICT, "Project " + projectName +
			" is open in Opcodex. Use it, or close it from the desktop or with " +
			"`opcodex project.close` first.");
	}

	private void adopt(Path dir, String name) {
		projectDir = dir;
		projectName = name;
		journal = new Journal(dir.resolve(name + ".rep").resolve("opcodex"));
	}

	/** Closes every program after its queued writes finish, then the project. */
	@Override
	public synchronized void close() {
		if (journal != null) {
			jobs.cancelProject(journal);
		}
		for (ProgramHandle handle : open.values()) {
			handle.shutdown();
			handle.program().release(this);
		}
		open.clear();
		if (project != null) {
			project.close();
			project = null;
		}
		projectDir = null;
		projectName = null;
		journal = null;
	}

	public synchronized Project project() throws RpcException {
		if (project == null) {
			throw new RpcException(ErrorCode.NOT_FOUND, "No project is open.");
		}
		return project.getProject();
	}

	/** Directory for Opcodex data that belongs to the open project. */
	public synchronized Path opcodexDataDir() throws RpcException {
		project();
		return projectDir.resolve(projectName + ".rep").resolve("opcodex");
	}

	/** The change journal of the open project. */
	public synchronized Journal journal() throws RpcException {
		project();
		return journal;
	}

	public JobManager jobs() {
		return jobs;
	}

	public synchronized String projectName() throws RpcException {
		project();
		return projectName;
	}

	/** Every program file in the open project, in folder order. */
	public List<DomainFile> programFiles() throws RpcException {
		List<DomainFile> files = new ArrayList<>();
		collect(project().getProjectData().getRootFolder(), files);
		return files;
	}

	public Optional<ProgramHandle> openHandle(String programId) {
		return Optional.ofNullable(open.get(programId));
	}

	/** Returns the open program with this id, opening it from the project if needed. */
	public synchronized ProgramHandle get(String programId) throws RpcException {
		ProgramHandle handle = open.get(programId);
		if (handle != null) {
			return handle;
		}
		for (DomainFile file : programFiles()) {
			if (ProgramHandle.idFor(file).equals(programId)) {
				try {
					DomainObject object = file.getDomainObject(this, true, false, TaskMonitor.DUMMY);
					return register((Program) object, file);
				}
				catch (Exception e) {
					throw engineError("Could not open " + file.getName(), e);
				}
			}
		}
		throw new RpcException(ErrorCode.NOT_FOUND, "No program with id " + programId);
	}

	/** Adopts a program this workspace already holds as a consumer. */
	public synchronized ProgramHandle register(Program program, DomainFile file) {
		String id = ProgramHandle.idFor(file);
		ProgramHandle existing = open.get(id);
		if (existing != null) {
			program.release(this);
			return existing;
		}
		ProgramHandle handle = new ProgramHandle(id, program, revisionBase(id));
		open.put(id, handle);
		return handle;
	}

	/**
	 * The first revision for a newly opened program: above every revision journaled for it and
	 * never below the current time in milliseconds, so revisions issued before a restart
	 * (journaled or not) are very unlikely to be reused.
	 */
	private long revisionBase(String programId) {
		long journaled = 0;
		if (journal != null) {
			try {
				journaled = journal.maxRevision(programId);
			}
			catch (IOException e) {
				Msg.warn(this, "Couldn't read the journal for revisions: " + e.getMessage());
			}
		}
		return Math.max(journaled + 1, System.currentTimeMillis());
	}

	/** Runs a change on the program's writer thread in one transaction, then saves it. */
	public <T> T write(String programId, String txName, WriteBody<T> body) throws RpcException {
		return write(programId, txName, (program, revision) -> {
		}, body).value();
	}

	/**
	 * Runs {@code precheck} and then {@code body} on the program's writer thread. The
	 * transaction starts only after the precheck passes.
	 */
	public <T> Written<T> write(String programId, String txName, Precheck precheck,
			WriteBody<T> body) throws RpcException {
		return write(get(programId), txName, precheck, body);
	}

	/** Uses the captured handle so a queued job cannot write to a reopened project. */
	public <T> Written<T> write(ProgramHandle handle, String txName, Precheck precheck,
			WriteBody<T> body) throws RpcException {
		Future<Written<T>> future;
		try {
			future = handle.writer().submit(() -> {
				Program program = handle.program();
				long before = handle.revision();
				precheck.check(program, before);
				int tx = program.startTransaction(txName);
				boolean committed = false;
				T result;
				try {
					result = body.apply(program);
					committed = true;
				}
				finally {
					try {
						program.endTransaction(tx, committed);
					}
					catch (IllegalStateException alreadyEnded) {
						// Force-ended and rolled back (a script past its time limit): nothing to end.
						if (committed) {
							throw alreadyEnded;
						}
					}
				}
				if (program.isChanged()) {
					program.save(txName, TaskMonitor.DUMMY);
				}
				return new Written<>(result, before, handle.revision());
			});
		}
		catch (RejectedExecutionException e) {
			throw new RpcException(ErrorCode.NOT_FOUND, "The program was closed.");
		}
		try {
			return future.get();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RpcException(ErrorCode.CANCELLED, txName + " was cancelled.");
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof RpcException rpc) {
				throw rpc;
			}
			throw engineError(txName + " failed", e.getCause());
		}
	}

	private static void collect(DomainFolder folder, List<DomainFile> out) {
		for (DomainFile file : folder.getFiles()) {
			if (Program.class.isAssignableFrom(file.getDomainObjectClass())) {
				out.add(file);
			}
		}
		for (DomainFolder child : folder.getFolders()) {
			collect(child, out);
		}
	}

	private static void checkName(String name) throws RpcException {
		if (name == null || !name.matches("[A-Za-z0-9][A-Za-z0-9 ._-]{0,63}")) {
			throw new RpcException(ErrorCode.INVALID_PARAMS,
				"Project names use letters, digits, spaces, dots, dashes and underscores.");
		}
	}

	private static RpcException engineError(String message, Throwable cause) {
		Msg.error(Workspace.class, message, cause);
		return new RpcException(ErrorCode.ENGINE_ERROR, message + ". See the engine log.");
	}
}

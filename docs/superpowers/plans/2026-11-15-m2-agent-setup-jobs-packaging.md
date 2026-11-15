# Milestone 2: Agent setup, long jobs and shipping the engine — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (or subagent-driven-development) to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:**
- Agents can connect to Opcodex with one command (`opcodex setup`), and an installation can be checked with `opcodex doctor`.
- Long operations run as cancellable jobs with progress.
- The engine host and the CLI ship inside the Opcodex distribution and the Windows installer, so an installed PC needs no Java or environment variables.

**Architecture:**
- The engine gains a `JobManager`. Long operations accept `background: true` and return `{jobId}`; progress goes out as `job.progress` notifications on the caller's connection.
- The CLI resolves an engine in order: `OPCODEX_ENGINE_CMD`, then the installed layout next to its own executable, then the portable layout.
- `opcodex setup` edits each agent app's MCP configuration and preserves everything else in the file:
  - JSON with key order kept;
  - for Codex TOML, a marked block.
- The Gradle `buildOpcodex` task builds the CLI with cargo and packages it as `support/opcodex(.exe)`. The desktop image build also copies it next to `Opcodex.exe`.

**Tech Stack:** As M1, plus serde_json `preserve_order` (indexmap 2.14.2, cached) and the `dirs` 7 crate (cached) for home folders.

**Spec:** `docs/superpowers/specs/2026-11-15-opcodex-new-front-end-design.md` §5.3, §8, §11 milestones 2 and 7.

## Global Constraints

- Everything from the M1 plan applies.
- `opcodex setup` never removes or rewrites anything it didn't add. Before changing a file it writes `<file>.opcodex-backup` (only once, if no backup exists). It prints the plan and needs `--yes` when stdin isn't a terminal.
- Setup targets and their files. Locations were verified on this PC on 15 Nov 2026 where the folder existed; others are marked VERIFY in docs.
  - Claude Code: `~/.claude.json`, `mcpServers.opcodex`
  - Claude Desktop: `%APPDATA%/Claude/claude_desktop_config.json` (macOS: `~/Library/Application Support/Claude/…`), `mcpServers.opcodex`
  - Cursor: `~/.cursor/mcp.json`, `mcpServers.opcodex`
  - VS Code: `%APPDATA%/Code/User/mcp.json` (Linux: `~/.config/Code/User/mcp.json`), `servers.opcodex` with `"type":"stdio"`
  - Windsurf: `~/.codeium/windsurf/mcp_config.json`, `mcpServers.opcodex`
  - Gemini CLI: `~/.gemini/settings.json`, `mcpServers.opcodex`
  - Codex CLI: `~/.codex/config.toml`, a `[mcp_servers.opcodex]` block between `# >>> opcodex setup` and `# <<< opcodex setup` markers
- Each server entry is `{"command": "<absolute path of this opcodex executable>", "args": ["mcp"]}`.
- An app counts as installed when its config file or its config folder exists. Missing apps are skipped and reported.
- Job progress notifications are `{"jsonrpc":"2.0","method":"job.progress","params":{"jobId","state","message","progress","maximum"}}`. Job states: `running`, `done`, `failed`, `cancelled`.

## Review Focus

1. **A user's existing agent config with other servers and custom keys** must keep every other key, in order, after setup and after uninstall. Test: `setup_preserves_other_servers_and_key_order`.
2. **A malformed JSON config** must be skipped with a clear message, never overwritten. Test: `setup_skips_malformed_json`.
3. **Cancelling a running analysis job** must stop it, leave the program usable and report `cancelled`. Test: `JobsTest.cancelStopsAnalysisAndReportsCancelled`.
4. **The installed CLI on a PC without Java on PATH** must still start the engine from the bundled runtime. Test: `installed_layout_resolves_bundled_runtime`, and the installed smoke check in Task 7.
5. **A second `opcodex setup` run** must change nothing and report "already set up". Test: `setup_is_idempotent`.

---

### Task 1: Certify and ship the engine module in the distribution

**Files:**
- Modify: `Ghidra/Features/OpcodexEngine/certification.manifest` (list `src/main/resources/META-INF/services/io.opcodex.engine.spi.OperationProvider`, IP `Apache License 2.0`)
- Test: `opcodex/tests/test_engine_module.py`

- [ ] Write the test `test_engine_module_files_are_certified`. It walks the module tree and asserts that every file except `.java` files, `build.gradle` and anything under `src/test*` appears in `certification.manifest`. It also asserts the IP column is `Opcodex` or `Apache License 2.0`.
- [ ] Run it and see it fail (the services file isn't listed). Fix the manifest, then see it pass.
- [ ] Commit `build(engine): certify engine module resources`.

### Task 2: Background jobs with progress and cancel

**Files:**
- Create: `jobs/JobManager.java`, `jobs/Job.java`, `jobs/JobMonitor.java` (a `TaskMonitorAdapter` that records message, progress and maximum, and checks cancel)
- Modify: `session/CallContext.java` (add `Consumer<JsonObject> events`), `rpc/Dispatcher.java` (an events sink per transport), `rpc/LineTransport.java` (passes `this::sendEvent`), `ops/ImportOps.java` (`program.analyze` accepts `background`), and a new `ops/JobOps.java` (`job.list`, `job.get`, `job.cancel`)
- Test: `src/test.slow/java/io/opcodex/engine/ops/JobsTest.java`, `src/test/java/io/opcodex/engine/jobs/JobManagerTest.java`

**Interfaces:**
- `JobManager.start(String kind, String programId, Consumer<JsonObject> events, JobBody body) : Job`. Here `JobBody` is `JsonElement run(JobMonitor m) throws Exception`.
- `Job.toJson()` returns `{jobId, kind, programId, state, message, progress, maximum, result?, error?}`.
- `job.get {jobId}` (read) → `Job.toJson()`.
- `job.cancel {jobId}` (write, so it follows the approval policy) → `{jobId, state}`.
- `program.analyze {programId, background?}` returns `{jobId}` when `background` is true.
- Events are sent to the connection that started the job.

- [ ] Tests:
  - `startedJobFinishesWithResultAndEmitsProgress` (unit, with a fake body);
  - `failedBodyReportsFailedWithSafeMessage`;
  - `cancelStopsAnalysisAndReportsCancelled` (integration: background-analyse a ProgramBuilder program with 2000 functions, cancel immediately, poll `job.get` until it's no longer running, expect `cancelled`, then confirm `program.info` still answers);
  - `jobGetUnknownIsNotFound`.
- [ ] See them fail, implement, see them pass, commit `feat(engine): run long operations as cancellable jobs with progress`.

### Task 3: CLI finds the installed or portable engine

**Files:**
- Modify: `opcodex/cli/src/client.rs` (`engine_command()` becomes `resolve_engine(exe: &Path, env) -> Result<Vec<String>>`)
- Test: unit tests in `opcodex/cli/src/client_tests.rs`

**Rules:**
1. Use `OPCODEX_ENGINE_CMD` if it's set.
2. **Installed layout:** `<root>/opcodex.exe` (or `<root>/app/support/opcodex.exe`) with `<root>/runtime/bin/java(.exe)` and `<root>/app/Ghidra/Framework/Utility/lib/Utility.jar`. The command is `[java, -Djava.system.class.loader=ghidra.GhidraClassLoader, -Dfile.encoding=UTF8, -Xshare:off, -cp, Utility.jar, ghidra.Ghidra, io.opcodex.engine.EngineMain]`.
3. **Portable layout:** `<dist>/support/opcodex(.exe)` with `<dist>/Ghidra/Framework/Utility/lib/Utility.jar`. Java comes from `JAVA_HOME/bin/java`, then `java` on PATH.
4. Otherwise, fail with the M1 message, extended to mention reinstalling.

- [ ] Tests:
  - `installed_layout_resolves_bundled_runtime` (temp tree of empty files);
  - `portable_layout_uses_java_home`;
  - `env_command_wins`;
  - `nothing_found_explains`.
- [ ] Fail, implement, pass, commit `feat(cli): start the engine from an installed or portable Opcodex`.

### Task 4: `opcodex doctor`

**Files:** Create `opcodex/cli/src/doctor.rs`; test `opcodex/cli/tests/doctor_cli.rs`.

`opcodex doctor [--format json|text]` reports these checks, each as `{name, ok, detail}`:
- `executable` (path, version);
- `engine` (the resolved command, or why none was found);
- `connect` (starts or attaches, calls `engine.info`, shows the engine version);
- `session` (session folder and whether a shared engine is running);
- `agents` (each setup target: installed? registered? pointing at this executable?).

Exit 0 if every required check passes (`executable`, `engine`, `connect`), otherwise 1.

- [ ] Tests: `doctor_reports_missing_engine` (no env, empty session dir: exit 1, and the JSON has `engine.ok == false`); `doctor_connects_to_engine` (end-to-end, skipped without `OPCODEX_ENGINE_CMD`).
- [ ] Fail, implement, pass, commit `feat(cli): add opcodex doctor`.

### Task 5: `opcodex setup` and `opcodex setup --uninstall`

**Files:** Create `opcodex/cli/src/setup.rs` (targets, the plan, JSON and TOML editors); test `opcodex/cli/tests/setup_cli.rs`. Enable `serde_json/preserve_order` and add `dirs = "7.0.0"`.

**Interfaces:**
- `opcodex setup [--only claude-code,codex,...] [--yes] [--dry-run]` and `opcodex setup --uninstall [--yes]`.
- The home folder and app-data folder can be overridden with `OPCODEX_SETUP_HOME` and `OPCODEX_SETUP_APPDATA`, for tests.

- [ ] Tests (all with temp homes):
  - `setup_preserves_other_servers_and_key_order`;
  - `setup_skips_malformed_json`;
  - `setup_is_idempotent`;
  - `codex_toml_block_added_and_removed` (existing `[model]` table kept byte-for-byte);
  - `codex_toml_existing_unmanaged_opcodex_table_is_reported_not_changed`;
  - `uninstall_removes_only_our_entry_and_keeps_backup`;
  - `dry_run_changes_nothing`;
  - `non_tty_without_yes_refuses`.
- [ ] Fail, implement, pass, commit `feat(cli): add opcodex setup for agent apps`.

### Task 6: Package the CLI with the distribution and desktop image

**Files:**
- Modify: `opcodex/build.gradle` (a `buildOpcodexCli` Exec task: `cargo build --release --locked --manifest-path opcodex/cli/Cargo.toml`, which `buildOpcodex` depends on); `opcodex/package_release.py` (add `--cli PATH`; copy it to `<root>/support/opcodex[.exe]` and record its SHA-256 in the manifest); `opcodex/build-desktop.ps1` (copy `app/support/opcodex.exe` to the image root next to `Opcodex.exe`)
- Test: `opcodex/tests/test_release.py` (a new case: the packaged ZIP contains `support/opcodex(.exe)` and its checksum in `opcodex-release.json`)

- [ ] Write the test, see it fail, implement, see it pass. Run the existing release tests (`python -m unittest discover -s opcodex/tests`); all must pass.
- [ ] Commit `build: ship the opcodex CLI in the distribution and Windows app`.

### Task 7: Build, install and verify on this PC

- [ ] Run `gradle buildOpcodexInstaller` in the background. Log to `build/native-next-front-end.log`. Don't edit tracked sources while it runs.
- [ ] Install the produced MSI (0.1.6 upgrades the installed 0.1.5): `msiexec /i <msi> /qn /l*v build/install-0.1.6.log`. Record the exit code.
- [ ] Installed smoke checks, with no `OPCODEX_ENGINE_CMD` and `JAVA_HOME` unset:
  - `"%LOCALAPPDATA%\Programs\Opcodex\Application\opcodex.exe" doctor` exits 0;
  - `opcodex.exe engine.info` returns 12.1.4;
  - an MCP handshake and `tools/list` succeed;
  - `Opcodex.exe` still starts its main window (the existing desktop smoke harness, or a process check).
- [ ] Record the evidence in `build/installed-proof-next-front-end.json` and in the vault.

### Task 8: Website docs for the CLI, MCP and agent setup

**Files (opcodex-web):** a new Starlight page `src/content/docs/agents/cli-and-mcp.md`, linked from the docs sidebar. It's labelled "Preview: in the next release" until a release ships it.

- [ ] Content: what the CLI and MCP do, `opcodex setup` / `doctor`, operation list (from `catalog.list`), approval policy and privacy, and examples for Claude Code and Codex. **No pricing.**
- [ ] Run the website's checks (`npm run build`, `npm test`), commit, push the docs branch, and deploy following the site's normal flow (see opcodex-web `docs/site/VERIFICATION.md`).

### Task 9: Docs and handover

- [ ] Update the engine and CLI READMEs, `MODIFICATIONS.md` if needed, and the vault: [[CODEX_HANDOFF]] status and log, [[NEXT_FRONT_END]].

# Milestone 1: Engine host, catalog, CLI and MCP — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A headless Opcodex engine host that serves one operation catalog over JSON-RPC (stdio and loopback TCP), plus a Rust `opcodex` CLI and MCP server. Together they can open a project, import and analyse a binary, list and decompile functions, rename and comment, and revert any edit selectively, with every write journaled.

**Architecture:** A new fork-owned Gradle module `Ghidra/Features/OpcodexEngine` (package `io.opcodex.engine`) starts the framework through `HeadlessGhidraApplicationConfiguration`. Operations are declared once in a `Catalog` and dispatched by a JSON-RPC layer that enforces credentials and operation classes. The Rust crate `opcodex/cli` connects to a running host (from `engine.json`) or spawns one over stdio. It exposes every catalog operation as a subcommand, and as MCP tools through `opcodex mcp`.

**Tech Stack:** Java 21 (compiled by JDK 25 with `--release 21`), Gradle 9.7.1 (cached at `C:\Users\pc\.gradle\wrapper\dists\gradle-9.7.1-bin\1w1c7tv4s851m17nbqdsro2tv\gradle-9.7.1\bin\gradle.bat`), Gson 2.13.2 (already an `api` dependency of `:Generic`), JUnit 4 through the upstream `javaTestProject.gradle`; Rust stable with `serde`, `serde_json`, `clap` 4, `anyhow`.

**Spec:** `docs/superpowers/specs/2026-11-15-opcodex-new-front-end-design.md` (§5.1–5.3, §5.5, §12).

## Global Constraints

- New files carry `/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */` (Rust/Java) as the first line, matching `opcodex/build-desktop.ps1`.
- No changes to upstream files, except registering the module. If any upstream file must change, add an `OPCODEX:` marker and a `MODIFICATIONS.md` entry.
- Engine version stays `12.1.4`. `application.java.compiler=21`.
- The host binds only to `127.0.0.1`. Tokens are 256-bit random values, base64url-encoded. The token file and `engine.json` are readable only by the current user.
- Operation names are dotted lowercase (`program.functions.list`). Classes are `read | write | run | system`.
- Error codes (JSON-RPC `error.data.code`): `UNAUTHORIZED`, `FORBIDDEN`, `NOT_FOUND`, `INVALID_PARAMS`, `STALE_REVISION`, `CONFLICT`, `APPROVAL_REQUIRED`, `CANCELLED`, `ENGINE_ERROR`. JSON-RPC numeric codes: -32602 for invalid params, -32601 for unknown method, -32000 for all domain errors.
- Engine stack traces go to the host log, never into responses.
- Every `write` operation takes `expectedRevision` (the program modification number) and journals the change before returning.
- Gradle commands run from `D:\opcodex` with `JAVA_HOME=C:\Program Files\Java\jdk-25.0.2` and `--offline`. In this plan, `$G` means the cached gradle.bat above.

## Review Focus

1. **A stale write after an external change.** `expectedRevision` that is lower than the current one must return `STALE_REVISION` and change nothing. Test: `EditOpsTest.renameWithStaleRevisionChangesNothing`.
2. **Revert after the value changed again.** Reverting a rename after someone renamed it again must return `CONFLICT` and change nothing. Test: `HistoryOpsTest.revertConflictsWhenValueChangedSince`.
3. **A client credential calling a `system` operation** (`policy.set`) must get `FORBIDDEN`, and a missing or wrong token must get `UNAUTHORIZED` before any dispatch. Test: `DispatcherTest.clientCredentialCannotCallSystemOperation`, `DispatcherTest.wrongTokenIsRejectedBeforeDispatch`.
4. **Malformed input**: non-JSON lines, oversized lines (> 8 MiB) and unknown fields must return a JSON-RPC error without killing the connection. Test: `LineTransportTest.malformedAndOversizedLinesReturnErrorsAndKeepServing`.
5. **Listing a large program** must page (`limit` ≤ 1000, `cursor`) rather than return everything. Test: `FunctionOpsTest.listFunctionsPagesWithCursor`.

---

## File structure

```
Ghidra/Features/OpcodexEngine/
  build.gradle, Module.manifest, certification.manifest, README.md
  src/main/java/io/opcodex/engine/
    EngineMain.java                     # entry: --stdio | --tcp; init application; wire everything
    catalog/OperationClass.java         # enum READ, WRITE, RUN, SYSTEM
    catalog/Operation.java              # record: name, description, cls, paramsSchema, resultSchema, handler
    catalog/OperationHandler.java       # functional: JsonObject handle(CallContext, JsonObject)
    catalog/Catalog.java                # registry + ServiceLoader load + describe()
    catalog/Schemas.java                # tiny builders for JSON Schema objects
    spi/OperationProvider.java          # ServiceLoader SPI: List<Operation> operations(EngineServices)
    spi/EntitlementProvider.java        # SPI; Community impl returns no paid features
    rpc/RpcException.java               # domain error with ErrorCode
    rpc/ErrorCode.java                  # enum of codes above
    rpc/Dispatcher.java                 # JSON-RPC parse → auth → class check → handler → response
    rpc/LineTransport.java              # NDJSON over InputStream/OutputStream, 8 MiB line cap
    rpc/TcpServer.java                  # 127.0.0.1 acceptor, one LineTransport per connection
    session/Credential.java             # record: kind (DESKTOP|CLIENT), token
    session/CallContext.java            # record: credential kind, actor string, EngineServices
    session/SessionFiles.java           # write token file + engine.json with user-only perms
    workspace/Workspace.java            # project open/create, program registry by id, per-program writer
    workspace/ProgramHandle.java        # id, Program, single-thread write executor, revision()
    journal/JournalEntry.java           # record: id, time, actor, op, programId, revision, target, before, after
    journal/Journal.java                # append-only JSONL in <project>.rep/opcodex/journal.jsonl
    policy/ApprovalPolicy.java          # enum ASK, ALLOW_WRITES, ALLOW_ALL; stored per project
    ops/SystemOps.java                  # engine.info, catalog.list, policy.get, policy.set
    ops/ProjectOps.java                 # project.create, project.open, project.close, program.list
    ops/ImportOps.java                  # program.import (raw bytes+language or file), program.analyze
    ops/FunctionOps.java                # program.functions.list, function.get
    ops/CodeOps.java                    # code.decompile, code.disassemble
    ops/XrefOps.java                    # xrefs.to, xrefs.from
    ops/StringOps.java                  # program.strings.list
    ops/EditOps.java                    # symbol.rename, comment.set
    ops/HistoryOps.java                 # history.list, history.revert
    ops/CommunityOperations.java        # OperationProvider listing all of the above
  src/main/resources/META-INF/services/io.opcodex.engine.spi.OperationProvider
  src/test/java/io/opcodex/engine/... (one test class per ops/rpc class)
opcodex/cli/
  Cargo.toml, src/main.rs, src/client.rs (connect/spawn/call), src/catalog.rs (dynamic subcommands),
  src/mcp.rs (stdio MCP server), src/format.rs (json|jsonl|md|text|csv), tests/cli_e2e.rs
```

---

### Task 1: Module skeleton, catalog and dispatcher

**Files:**
- Create: `Ghidra/Features/OpcodexEngine/{build.gradle,Module.manifest,certification.manifest,README.md}`, `catalog/*`, `spi/*`, `rpc/{RpcException,ErrorCode,Dispatcher}.java`, `session/{Credential,CallContext}.java`
- Test: `src/test/java/io/opcodex/engine/rpc/DispatcherTest.java`, `catalog/CatalogTest.java`

**Interfaces:**
- Produces:
  - `record Operation(String name, String description, OperationClass cls, JsonObject paramsSchema, JsonObject resultSchema, OperationHandler handler)`;
  - `interface OperationHandler { JsonElement handle(CallContext ctx, JsonObject params) throws RpcException; }`;
  - `Catalog.register(Operation)`, `Catalog.find(String) : Optional<Operation>`, `Catalog.describe() : JsonArray` (name, description, class, schemas);
  - `Dispatcher(Catalog, Map<String,Credential> tokens)` with `String handleLine(String line)` (returns one JSON-RPC response line, or null for notifications);
  - `record CallContext(Credential.Kind kind, String actor, EngineServices services)`, where `EngineServices` is an interface with `Workspace workspace()`, `Journal journal(String programId)`, `ApprovalPolicy policy()`;
  - `interface OperationProvider { List<Operation> operations(EngineServices s); }`.
- Wire format: request `{"jsonrpc":"2.0","id":1,"method":"<op>","params":{...},"auth":"<token>","actor":"mcp:claude-code"}`. `auth` is required. `actor` is honoured only for CLIENT credentials, and only as `mcp:<name>`/`cli`, matching `^(cli|mcp:[a-z0-9._-]{1,64})$`. DESKTOP credentials are always the actor `desktop`.

- [ ] **Step 1: Write failing tests**

```java
@Test public void wrongTokenIsRejectedBeforeDispatch() // handler counter stays 0; error.data.code == "UNAUTHORIZED"
@Test public void clientCredentialCannotCallSystemOperation() // SYSTEM op with CLIENT token → "FORBIDDEN"
@Test public void desktopCredentialCanCallSystemOperation()
@Test public void unknownMethodReturnsMinus32601()
@Test public void invalidActorIsRejected() // actor "desktop" from CLIENT → INVALID_PARAMS
@Test public void catalogDescribeListsNameClassAndSchemas()
@Test public void duplicateOperationNameRejected() // Catalog.register twice → IllegalArgumentException
```

- [ ] **Step 2: Run them and confirm they fail.** `$G :OpcodexEngine:test --offline --tests "io.opcodex.engine.rpc.*" --tests "io.opcodex.engine.catalog.*"`. Expected: compilation failure (classes missing).
- [ ] **Step 3: Implement.** `build.gradle` follows `Ghidra/Features/FunctionID/build.gradle`: distributableGhidraModule, javaProject, javaTestProject. Dependencies: `api project(':Base')`, `api project(':Decompiler')`. `eclipse.project.name = 'Features OpcodexEngine'`. `Module.manifest` is empty. `certification.manifest` is `##VERSION: 2.0`, plus one line per non-Java file. Token comparison uses `MessageDigest.isEqual`.
- [ ] **Step 4: Run the tests and confirm they pass.** Same command, expect PASS.
- [ ] **Step 5: Commit.** `git add Ghidra/Features/OpcodexEngine && git commit -m "feat(engine): add Opcodex engine host module with catalog and dispatcher"`

### Task 2: NDJSON transport, TCP server, session files

**Files:**
- Create: `rpc/LineTransport.java`, `rpc/TcpServer.java`, `session/SessionFiles.java`
- Test: `rpc/LineTransportTest.java`, `session/SessionFilesTest.java`

**Interfaces:**
- Consumes: `Dispatcher.handleLine`.
- Produces:
  - `LineTransport(InputStream in, OutputStream out, Dispatcher d)` with `void serve()` (blocks until EOF) and `void sendEvent(JsonObject notification)` (thread-safe writes);
  - `TcpServer.start(Dispatcher d) : int port` and `stop()`;
  - `SessionFiles.write(Path dir, String clientToken, int port, String edition, String apiVersion)`, which writes `client.token` and `engine.json` (`{"port","pid","edition","apiVersion"}`);
  - `SessionFiles.defaultDir()`: Windows `%LOCALAPPDATA%\Opcodex\engine`, Linux `$XDG_RUNTIME_DIR/opcodex` or `~/.local/state/opcodex/engine`.

- [ ] **Step 1: Write failing tests.** `malformedAndOversizedLinesReturnErrorsAndKeepServing`: feed `not json\n`, then a line of 8 MiB + 1 bytes, then a valid call. Expect two error responses (-32700 parse error; -32600 for oversized) and then a success. `eventsInterleaveWithoutCorruptingLines` (20 threads sending events, every output line parses). `sessionFilesAreUserOnly`: on POSIX, permissions `rw-------`; on Windows the ACL has only the current user. Verify with `AclFileAttributeView`.
- [ ] **Step 2: Run them and confirm they fail.**
- [ ] **Step 3: Implement.**
  - The line reader reads bytes up to `\n` with a hard 8 MiB cap. On overflow it discards up to the next newline.
  - Windows ACL: replace the ACL with a single entry for `UserPrincipalLookupService.lookupPrincipalByName(System.getProperty("user.name"))`.
  - TCP binds `InetAddress.getLoopbackAddress()` on port 0.
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat(engine): add NDJSON transport, loopback TCP server and session files`

### Task 3: Engine bootstrap, workspace, project and import operations

**Files:**
- Create: `EngineMain.java`, `workspace/{Workspace,ProgramHandle}.java`, `ops/{SystemOps,ProjectOps,ImportOps,CommunityOperations}.java`, `META-INF/services/...`
- Test: `ops/ProjectOpsTest.java` extends `ghidra.test.AbstractGhidraHeadlessIntegrationTest`

**Interfaces:**
- Produces operations:
  - `engine.info` (read) → `{product:"Opcodex", edition, engineVersion:"12.1.4", apiVersion:"1.0"}`
  - `catalog.list` (read) → `Catalog.describe()`
  - `project.create {dir, name}` / `project.open {dir, name}` (system) → `{projectId}`
  - `program.import {path? | bytesBase64?, name, language?, compiler?, folder?}` (write) → `{programId, name, language, revision}`
  - `program.analyze {programId}` (write) → `{programId, revision, functionCount}`
  - `program.list` (read) → `[{programId, name, path, language, revision}]`
- `Workspace`: `ProgramHandle open(String domainPath)`, `ProgramHandle get(String programId)` (throws `RpcException(NOT_FOUND)`), `<T> T write(String programId, String txName, Function<Program,T> body)`, which runs on the handle's single-thread executor inside one transaction and commits only if the body returns normally. `ProgramHandle.revision()` returns `program.getModificationNumber()`.
- `program.id` is a stable `UUID` derived from the domain file ID (`DomainFile.getFileID()`).
- Import uses `ProgramLoader.builder().source(...).project(project).projectFolderPath(folder).name(name).language(language).compiler(compiler).monitor(monitor).load()`, then saves the primary program into the project and releases the `LoadResults`. Analysis uses `AutoAnalysisManager.getAnalysisManager(program)`: `initializeOptions()`, `reAnalyzeAll(null)`, `startAnalysis(monitor)`, all inside the workspace writer.
- `EngineMain`:
  - `--stdio`: reads the desktop or client token from the first stdin line, `{"credential":"<token>","kind":"DESKTOP|CLIENT"}`, then serves NDJSON on stdin/stdout.
  - `--tcp`: generates a client token, writes the session files and prints `{"ready":true,"port":N}` to stdout.
  - Initialises through `Application.initializeApplication(new GhidraApplicationLayout(), new HeadlessGhidraApplicationConfiguration())` when it isn't already initialised.

- [ ] **Step 1: Write failing tests.** `createImportAnalyzeAndListRawX86` imports `bytesBase64` of `55 48 89 E5 B8 2A 00 00 00 5D C3` (push rbp; mov rbp,rsp; mov eax,42; pop rbp; ret) with `language "x86:LE:64:default"`, `compiler "gcc"`. It then calls `program.analyze` and checks `program.list` has 1 entry with that language and `revision > 0`. Also `openUnknownProgramIdReturnsNotFound`.
- [ ] **Step 2: Run them and confirm they fail.** `$G :OpcodexEngine:test --offline --tests "*ProjectOpsTest"`
- [ ] **Step 3: Implement.** The test project lives under `createTempDirectory`. Program IDs are kept in a `ConcurrentHashMap`.
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat(engine): bootstrap headless engine with project and import operations`

### Task 4: Function, code, xref and string read operations

**Files:**
- Create: `ops/{FunctionOps,CodeOps,XrefOps,StringOps}.java`
- Test: `ops/FunctionOpsTest.java`, `ops/CodeOpsTest.java`. These use `ProgramBuilder("demo", ProgramBuilder._X64)` with 1,500 one-byte functions for paging, plus the Task 3 import for decompile.

**Interfaces:**
- Addresses are strings in `program.getAddressFactory().getAddress()` syntax (`"00401000"`, or `"ram:00401000"` when the space is ambiguous).
- `program.functions.list {programId, limit=200 (1..1000), cursor?, query?}` → `{items:[{entry, name, size, callers, callees}], nextCursor?}`. The cursor is the last entry address, and iteration is `FunctionManager.getFunctions(addr, true)`.
- `function.get {programId, address}` → `{entry, name, signature, body:[{start,end}], callers:[...], callees:[...]}`.
- `code.decompile {programId, address, timeoutSec=30}` → `{entry, c, tokens:[{text, kind, address?, varName?}]}`. It uses a pooled `DecompInterface` (pool size 2 per program, opened on first use, disposed on program close). Tokens are flattened from `getCCodeMarkup()` `ClangToken`s, with `kind` from the token class simple name.
- `code.disassemble {programId, address, count=64 (1..2000)}` → `{items:[{address, bytes, mnemonic, operands}]}`.
- `xrefs.to` / `xrefs.from {programId, address, limit, cursor}` → `{items:[{from, to, type}], nextCursor?}`.
- `program.strings.list {programId, minLength=4, limit, cursor}` → `{items:[{address, value, encoding}], nextCursor?}`.

- [ ] **Step 1: Write failing tests.**
  - `listFunctionsPagesWithCursor`: 1,500 functions; `limit 1000` returns 1000 plus a cursor; the next page returns 500 and no cursor.
  - `limitAbove1000IsInvalidParams`.
  - `decompileRawFunctionReturnsReturn42`: `c` contains `return 0x2a;`, and some token has `address` equal to the entry.
  - `disassembleCountBounds`.
  - `xrefsToCalledFunction` (ProgramBuilder with a call reference).
- [ ] **Step 2: Run them and confirm they fail.**
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat(engine): add function, decompile, disassembly, xref and string operations`

### Task 5: Edits, journal, history revert, approval policy

**Files:**
- Create: `journal/{JournalEntry,Journal}.java`, `policy/ApprovalPolicy.java`, `ops/{EditOps,HistoryOps}.java`; update `SystemOps` with `policy.get` / `policy.set`.
- Test: `ops/EditOpsTest.java`, `ops/HistoryOpsTest.java`, `policy/ApprovalPolicyTest.java`

**Interfaces:**
- `symbol.rename {programId, address, newName, expectedRevision}` (write) → `{revision, entryId}`. It renames the primary symbol (a function if one exists there) with `SourceType.USER_DEFINED`.
- `comment.set {programId, address, kind: eol|pre|post|plate|repeatable, text (≤ 64 KiB), expectedRevision}` (write) → `{revision, entryId}`.
- `history.list {programId, limit, cursor, actor?}` → entries, newest first.
- `history.revert {programId, entryId, expectedRevision}` (write) → `{revision, entryId}`. If the current value ≠ the entry's `after`, it returns `CONFLICT`.
- `JournalEntry` has `{id (ULID-like: millis + random), time, actor, op, programId, revisionBefore, revisionAfter, target:{address, kind}, before, after, revertOf?}`.
- `Journal.append(entry)` writes and fsyncs one JSON line. `Journal.read(...)` streams the file in reverse.
- Policy: `policy.get` (read) and `policy.set {mode}` (system) persist to `<project>.rep/opcodex/policy.json`. The default is `ask`. Under `ask`, a `write`/`run` call from a CLIENT credential returns `APPROVAL_REQUIRED` with `data.preview` (for example `"rename FUN_00401000 → parse_header"`). M1 has no desktop to approve, so `ask` simply blocks client writes. `allow-writes` permits `write`; `allow-all` permits `write` and `run`. DESKTOP writes are always allowed.
- Every write handler: check `expectedRevision == handle.revision()` (else `STALE_REVISION`), capture `before`, apply in `Workspace.write`, capture `after`, then append the journal entry. If the transaction fails, nothing is journaled.

- [ ] **Step 1: Write failing tests.**
  - `renameRecordsBeforeAndAfterInJournal`.
  - `renameWithStaleRevisionChangesNothing` (name unchanged, journal size unchanged).
  - `revertRestoresPreviousName`.
  - `revertConflictsWhenValueChangedSince`.
  - `revertSurvivesRestart`: close and reopen `Workspace`/`Journal`, then revert.
  - `clientWriteUnderAskReturnsApprovalRequiredWithPreview`.
  - `allowWritesPermitsClientRenameButNotRun`.
  - `commentOver64KiBIsInvalidParams`.
- [ ] **Step 2: Run them and confirm they fail.**
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat(engine): journal every edit with selective revert and approval policy`

### Task 6: Host launch from the dev tree

**Files:**
- Modify: `Ghidra/Features/OpcodexEngine/build.gradle`, adding a `runEngine` JavaExec task (`mainClass = 'ghidra.GhidraLauncher'`, args `io.opcodex.engine.EngineMain` plus pass-through `-Pargs`), with classpath `sourceSets.main.runtimeClasspath`, and a `printEngineClasspath` task that writes `build/engine-classpath.txt`.
- Create: `opcodex/tests/test_engine_launch.py` (Python unittest, like the existing tests in that folder).

**Interfaces:**
- Produces: `build/engine-classpath.txt`, which the CLI uses in dev through the env var `OPCODEX_ENGINE_CMD`, set to a full command line ending in `io.opcodex.engine.EngineMain`.
- If `GhidraLauncher` can't resolve the layout from the dev classpath, fall back to running `io.opcodex.engine.EngineMain` directly (it initialises the layout itself, per Task 3) and record which one worked in the module README.

- [ ] **Step 1: Write a failing test.** `test_tcp_mode_prints_ready_and_answers_engine_info`: run the command from `engine-classpath.txt` with `--tcp`, read the ready line, connect, send `engine.info` using the token from `client.token` (session dir overridden through the env var `OPCODEX_SESSION_DIR`), and assert `engineVersion == "12.1.4"`. Timeout 120 s.
- [ ] **Step 2: Run it and confirm it fails.** `python -m unittest opcodex.tests.test_engine_launch -v`
- [ ] **Step 3: Implement** the Gradle tasks and the `OPCODEX_SESSION_DIR` override in `SessionFiles.defaultDir()`.
- [ ] **Step 4: Run it and confirm it passes.**
- [ ] **Step 5: Commit.** `build(engine): add dev launch tasks and launch smoke test`

### Task 7: Rust `opcodex` CLI

**Files:**
- Create: `opcodex/cli/{Cargo.toml,src/main.rs,src/client.rs,src/catalog.rs,src/format.rs}`, `tests/cli_e2e.rs`

**Interfaces:**
- Consumes: `engine.json`, `client.token`, `catalog.list`, and the env vars `OPCODEX_SESSION_DIR` and `OPCODEX_ENGINE_CMD`.
- `client::connect() -> Result<Client>`. It tries `engine.json` first (TCP); if no host is running it spawns `OPCODEX_ENGINE_CMD --stdio` with a fresh CLIENT token. `Client::call(method, params) -> Result<Value>` maps JSON-RPC errors to exit code 2 and prints `error.data.code: message` to stderr.
- CLI surface:
  - `opcodex [--format json|jsonl|md|text|csv] [--project DIR/NAME] <operation> [--param value ...]`. `<operation>` is any catalog name, and params are parsed against the operation's `paramsSchema` (`--expected-revision` → `expectedRevision`; integers and booleans are typed from the schema).
  - `opcodex commands` lists the catalog.
  - `opcodex <op> --help` prints the description and parameters.
  - `--project` sends `project.open` first.
- Actor sent is `cli`.

- [ ] **Step 1: Write failing tests.** `cargo test` with `e2e_import_list_decompile` (`#[ignore]` unless `OPCODEX_ENGINE_CMD` is set): import the raw bytes, analyse, list functions, decompile, and assert stdout contains `0x2a`. `unknown_operation_exits_2`. Unit test `params_typed_from_schema`.
- [ ] **Step 2: Run them and confirm they fail.** `cargo test --manifest-path opcodex/cli/Cargo.toml`
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run them and confirm they pass.** Run the unit tests, then the e2e test with the env var set.
- [ ] **Step 5: Commit.** `feat(cli): add opcodex CLI generated from the engine catalog`

### Task 8: MCP server (`opcodex mcp`)

**Files:**
- Create: `opcodex/cli/src/mcp.rs`, tests in `tests/mcp_e2e.rs`

**Interfaces:**
- MCP over stdio (JSON-RPC 2.0, newline-delimited), protocol version `2025-06-18`. Handles:
  - `initialize`, which records `clientInfo.name` and sets actor = `mcp:<name sanitised to [a-z0-9._-]>`;
  - `notifications/initialized`;
  - `tools/list`, where each catalog operation except the `system` class becomes a tool: `name` is the operation name with `.` replaced by `_`, `description` comes from the catalog, `inputSchema` = `paramsSchema`, and `annotations.readOnlyHint = (cls == read)`;
  - `tools/call`, which returns `content:[{type:"text", text:<pretty JSON>}]`, and `isError:true` with the code and message for domain errors;
  - `ping`.
- `APPROVAL_REQUIRED` is returned as a tool error whose text explains that the project's approval policy blocks the write, and names `opcodex policy set` / the desktop.

- [ ] **Step 1: Write failing tests.** `mcp_lists_tools_without_system_ops`: `policy_set` absent, `symbol_rename` present with readOnlyHint false. `mcp_call_rename_then_revert` (e2e, ignored without env): set the policy to `allow-writes` via the engine with a DESKTOP credential spawned by the test, rename through MCP, check `history.list` shows actor `mcp:test-client`, then revert through MCP.
- [ ] **Step 2: Run them and confirm they fail.**
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat(cli): add MCP server exposing the engine catalog`

### Task 9: `opcodex policy set` with an interactive terminal check

**Files:**
- Modify: `opcodex/cli/src/main.rs`; engine `SystemOps`, adding `policy.set` acceptance through a one-time **TTY confirmation grant**.

**Interfaces:**
- The CLI asks the host for `policy.requestChange {mode}` (client, `system-request` path). The host returns `{challenge}` (6 random digits).
- The CLI prints `Type 482913 to allow agents to <mode> in <project>:` and reads the line from the terminal. It refuses unless `std::io::stdin().is_terminal()` and stdout is a TTY.
- The CLI sends `policy.confirmChange {challenge, typed}`. The host applies it only if they match within 60 s, journals actor `cli`, and invalidates the challenge after one attempt.
- The `policy.requestChange` / `policy.confirmChange` operations are hidden from MCP `tools/list`.

- [ ] **Step 1: Write failing tests.** Engine: `confirmWithWrongCodeFailsAndInvalidatesChallenge`, `challengeExpiresAfter60s` (injectable clock). CLI: `policy_set_refuses_without_tty`.
- [ ] **Step 2: Run them and confirm they fail.**
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run them and confirm they pass.**
- [ ] **Step 5: Commit.** `feat: add terminal-confirmed approval policy changes`

### Task 10: Milestone verification and docs

- [ ] Run the full module suite: `$G :OpcodexEngine:test --offline`. Expected: all pass. Save the report path.
- [ ] Run `cargo test` (unit) and the e2e tests with `OPCODEX_ENGINE_CMD`. Expected: all pass.
- [ ] Manual MCP check: point an MCP client (Claude Code: `claude mcp add opcodex -- <path>\opcodex.exe mcp`) at the dev build, list the tools, and decompile a function.
- [ ] Write `Ghidra/Features/OpcodexEngine/README.md` (how to run in dev, operation list, error codes, approval policy) and add `MODIFICATIONS.md` entries if any upstream file changed.
- [ ] Commit `docs(engine): document milestone 1 engine host, CLI and MCP`.

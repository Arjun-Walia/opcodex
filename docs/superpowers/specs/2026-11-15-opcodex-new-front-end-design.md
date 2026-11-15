# Opcodex Community: new front end, engine host and agent control

Status: design for owner approval · 15 November 2026 · Community `1.0.0-alpha.2`, engine 12.1.4 (JDK 21)

Companion document (private): `opcodex-pro/docs/superpowers/specs/2026-11-15-opcodex-pro-and-licensing-design.md`.

## 1. Intent

Opcodex Community becomes a modern reverse-engineering desktop app with a new front end that replaces the classic Swing workbench. Two products set the target:

- **Craft Anything 0.6.0**, installed on this PC. It's an AI-first investigation shell with about 330 tools, all reachable through its CLI and an MCP server. `setup` connects it to coding-agent apps. It keeps an investigation journal (evidence, hypotheses, findings, unknowns) and has clean-room and deep modes. It covers many formats, from native, Android, Apple, .NET and Java to web, firmware and games, and it uses Ghidra only as an optional "deep mode" engine. Its strengths are breadth and agent control. Its weakness is that it has no real analysis workbench of its own.
- **IDA Pro and Binary Ninja**, the tools companies pay for. Their strengths are a fast, polished code workbench, good decompilation, and scripting and automation.

Community must beat Craft Anything on agent control and match it on investigation workflow. It must also give an analyst a workbench that feels as fast and deliberate as IDA or Binary Ninja. Community is the free, open-source product. It drives adoption and is the base that Pro builds on.

Why now: the owner wants the open-source core finished and documented first. Only after that do pricing and Pro go public.

## 2. Goals, non-goals, anti-goals

**Goals (first Community release, "v1")**

1. A new desktop front end that becomes the only shipped interface.
2. One engine host process that owns projects and analysis, and exposes one versioned API.
3. Anything a person can do in the front end, an agent can do through the `opcodex` CLI or the MCP server, with the same results.
4. The v1 feature set in §6, documented on opcodex.xyz.
5. Existing project folders (`.gpr`/`.rep`) open without conversion.

**Later Community releases (not v1)**: live debugger (GDB, LLDB, WinDbg through the existing Trace RMI backends); Android, .NET, Java, web and firmware formats; emulator.

**Non-goals**: rewriting the decompiler, disassembler, loaders, processors or program database. Shipping the Swing workbench, or making the new front end render Swing plugins. Cloud analysis by default.

**Anti-goals** (failures even if everything technically works):

- Agents or the CLI silently get less than the UI, or the other way round.
- An edit made by an agent can't be seen, attributed or undone.
- Analysis data leaves the machine without explicit opt-in and a preview.
- The front end freezes on large binaries. Every list and code view must stay responsive at millions of lines.
- A fork of the engine that can no longer take upstream updates.
- "Ghidra" used as product branding. Attribution stays only where the license requires it (§3).

## 3. Constraints

- Apache 2.0. Keep `LICENSE`, `NOTICE`, the module notices and upstream headers. New files get the Opcodex Apache header. Any change to an upstream file gets an `OPCODEX:` marker and an entry in `MODIFICATIONS.md`. The About → Legal notices view in the new front end shows the attribution and notices.
- The engine stays on 12.1.4 and its technical identifiers. Upstream Swing modules stay in the source tree untouched, so upstream merges stay cheap. They're just never launched; many headless services still link against `Docking` and `Gui` classes.
- JDK 21 runtime, bundled. Gradle as the upstream tag requires.
- Local-first. The engine host binds to loopback only. Cloud model calls need explicit opt-in per backend and a preview of the outgoing text.
- Community never depends on Pro (§7 defines the extension points Pro uses).
- The existing release gates stay. The broad upstream Linux suite failure (missing external fixtures) is still an open, unsuppressed gate. Don't exclude tests, and don't ask the owner for the fixture bundle again.
- The design contract in the vault's `PRODUCT_DIRECTION` still applies to the new front end: a quiet, precise workspace; code gets most of the screen; Opcodex Light is the default and Opcodex Dark is black; one sharp vector icon family; useful tooltips; adjustable, saved layouts with an explicit reset.

## 4. Approaches considered

| Approach | Trade-off | Decision |
|---|---|---|
| A. Restyle and reorganize the Swing workbench | Fastest, keeps every capability, but limited by Swing docking and rendering | Rejected by owner |
| B. New front end over a headless engine | No limits on design or agent control. Debugger, version tracking UI and plugins must be rebuilt | **Chosen** |
| C. Hybrid: Swing workbench plus a new investigation/agent layer | Keeps depth with less work, but keeps the old look | Rejected by owner |

Transition: we considered shipping the classic workbench alongside the new one, to keep debugger and version tracking available during the rebuild. **The owner chose the new front end only.** As a result, the v1 scope (§6) defines what has to exist before Community ships.

The shell technology was my call, stated to the owner without objection:

- **Tauri 2 + React + TypeScript (chosen)**: a small installer, the same language as the website, full layout freedom, and system webviews (WebView2 on Windows, WebKitGTK on Linux, WKWebView on macOS).
- Electron: one consistent Chromium, but about 150 MB more per install and no benefit we need.
- egui (Craft Anything's choice): fast, but rich text, code views and accessibility are harder.

## 5. Architecture

```
┌──────────── Opcodex desktop (Tauri 2) ────────────┐   ┌──── opcodex CLI ────┐  ┌── agent apps ──┐
│ React/TS UI  ── typed client (generated) ──┐      │   │ commands + `mcp`    │  │ Claude Code,   │
│ Rust shell: starts/stops engine host,      │      │   │ (Rust binary)       │◄─┤ Codex, Cursor… │
│ keychain, file dialogs, updater            │      │   └─────────┬───────────┘  └────────────────┘
└────────────────────────────────────────────┼──────┘             │ stdio MCP / JSON-RPC
                                             ▼                    ▼
                     ┌──────────── Opcodex engine host (JVM, JDK 21) ────────────┐
                     │ Operation catalog (single source of truth)                 │
                     │ JSON-RPC 2.0 (NDJSON) over TCP on 127.0.0.1 + stdio mode   │
                     │ Session/auth · scheduler · event stream · journal          │
                     │ Adapters → upstream engine (Program DB, loaders,           │
                     │ auto-analysis, DecompInterface pool, VT correlators, …)    │
                     └────────────────────────────────────────────────────────────┘
```

### 5.1 Engine host

- A new fork-owned Gradle module, `Ghidra/Features/OpcodexEngine` (Java 21, Opcodex headers). It's packaged into the distribution like any feature module.
- Starts the framework headless through `ghidra.framework.HeadlessGhidraApplicationConfiguration` (Base). It opens and creates projects through the project APIs already used by `ghidra.base.project.GhidraProject` and `HeadlessAnalyzer` (Base). Imports go through `ghidra.app.util.importer.ProgramLoader`. Decompilation uses pooled `ghidra.app.decompiler.DecompInterface` instances. Diffing uses the Version Tracking API (`ghidra.feature.vt.api`) and `ghidra.program.util.ProgramDiff`. Python scripting uses the PyGhidra module. All of these classes exist in this tree (checked 15 Nov 2026). The builder confirms the exact method signatures in the source before using them; don't guess.
- One host process per user session. It serves the desktop, the CLI and MCP clients at the same time, so they share open programs and never fight over project locks. If no host is running, the CLI starts one headless.
- Concurrency:
  - Each open program has one writer queue, and transactions stay short.
  - A transaction is never held open while waiting on a model, a client or the network.
  - Reads run in parallel off a consistent view.
  - Long work (import, analysis, decompile-many, diff) runs as a cancellable job, with progress events.
- **Expected-revision writes.** Every write carries the program modification number the client last saw. A stale write is rejected with a structured error, and the server never guesses a new target.
- Undo and redo use the program's own transaction history, so a rename from the CLI can be undone in the UI and the other way round.

### 5.2 API and the operation catalog

- Every operation is declared once in the catalog with:
  - a stable dotted name (`program.functions.list`, `code.decompile`, `symbol.rename`, …);
  - a description written for people and agents;
  - JSON Schemas for its parameters and result;
  - a class: `read`, `write`, `run` (executes code or scripts) or `system`;
  - pagination rules;
  - an example.
- Generated from the catalog:
  - the TypeScript client used by the front end;
  - the CLI subcommands and help;
  - the MCP tool list;
  - the reference docs on the website.
  
  A CI check fails if any front-end action calls something outside the catalog. That's how parity is guaranteed.
- Transport: JSON-RPC 2.0 messages, one per line (newline-delimited JSON), over a TCP socket on `127.0.0.1` with a random port, plus a stdio mode for MCP and scripts. The webview never opens the socket itself. The Tauri shell relays calls and events through Tauri IPC, so web content can never reach the host directly. (Builder's call; it replaces the earlier WebSocket wording and avoids a server dependency.)
- Authentication: a per-session 256-bit token written to a user-only-readable file in the app data directory. Any connection without the token is refused.
- Event stream: analysis progress, job state, program-changed (with the new modification number), journal entries and approval requests.
- Versioning: `api.version` is negotiated at connect. Breaking changes need a new major version, and the previous major version is supported for one release.
- Errors: stable error codes (`STALE_REVISION`, `NOT_FOUND`, `APPROVAL_REQUIRED`, `CANCELLED`, `ENGINE_ERROR` …) with human-readable messages. Engine stack traces are kept in logs and are not sent to remote clients.
- Large results are paginated. Code views fetch windows by address range, never a whole program.

### 5.3 Agent control: CLI, MCP and approvals

- `opcodex` is a Rust binary shipped next to the desktop app:
  - `opcodex <operation> [args]`: every catalog operation as a subcommand, with output as `--format json|jsonl|md|text|csv`.
  - `opcodex mcp`: an MCP server over stdio that exposes the catalog as tools. Read tools are annotated read-only. Tool descriptions come from the catalog.
  - `opcodex setup` / `opcodex setup --uninstall`: registers or removes the MCP server and an Opcodex skill file in installed agent apps. Targets: Claude Code, Claude Desktop, Codex CLI, Cursor, VS Code, Windsurf, Gemini CLI. Each app's config location must be verified at build time; show a plan before writing.
  - `opcodex doctor`: checks the install, runtime, engine host, registrations and model backends.
- Approval policy per project:
  - `ask` (the default for `write` and `run`);
  - `allow-writes`;
  - `allow-all`.
  
  A request that needs approval raises an approval event. The desktop shows it with a preview (for example "rename `FUN_00401a20` → `parse_header`"). With no desktop running, the CLI needs `--allow-writes` / `--allow-run`.
- Every write records an actor (`human`, or `agent:<client name>`) in the journal, so the "History" view can filter by who made a change.
- The CLI and MCP must work fully headless (CI, servers, containers). The desktop is not required.

### 5.4 Desktop shell

- Tauri 2 shell in Rust. It:
  - starts and supervises the engine host (finds the bundled JRE, health-checks it, restarts it with a notice after a crash);
  - stores secrets in the OS keychain;
  - shows native file dialogs;
  - handles updates.
- React + TypeScript UI. The builder chooses libraries within these rules:
  - **Code views** (listing, decompiler, hex) are virtualized renderers driven by engine-provided tokens. The decompiler returns tokens with address and variable links (from the Clang token tree), so clicking, highlighting and renaming work on meaning, not on text.
  - **Graphs** (call graph, control-flow graph): a layout engine of the ELK class, rendered on canvas or WebGL, so a few thousand nodes stay smooth.
  - **Layout**: dockable, resizable panes with saved layouts per project, plus an explicit reset.
  - Keyboard-first. A command palette lists every catalog action available in the current context, with shortcuts. Visible focus everywhere. Screen-reader labels on all controls.
- Performance budgets on a mid-range laptop:
  - first window under 2 s after launch;
  - scrolling a 1-million-line listing at 60 fps;
  - a cached decompile under 100 ms, and a typical uncached function under 2 s;
  - search in the palette under 50 ms.

### 5.5 Project data

- Program data stays in upstream project folders, opened as-is.
- Opcodex-only data lives in `<project>.rep/opcodex/`:
  - `journal.jsonl`, append-only: evidence, hypotheses, findings, open questions, actions, each linked to program addresses;
  - layout files;
  - approval policy;
  - agent settings (secrets excluded).
- Report export writes Markdown or HTML from the journal.

## 6. v1 feature scope (agreed with owner)

Each item is a catalog operation set plus UI.

1. **Home and import.** Recent projects, new or open project, and drag-and-drop import with loader and language choice when it's ambiguous. Auto-analysis runs with live progress and can be cancelled.
2. **Triage summary**, shown first after import:
   - format, architecture, compiler spec, entry points;
   - sections with entropy (packing hint);
   - imports and exports;
   - notable strings (URLs, paths, keys, registry, crypto constants);
   - risky API usage grouped by category (process injection, network, crypto, persistence, anti-debug);
   - signer information for PE files.
3. **Workspace views:**
   - functions list (filter, sort, size, xref counts);
   - listing, decompiler and hex views, with linked cursors and back/forward;
   - cross-references to and from;
   - call graph and control-flow graph;
   - strings, symbols and imports;
   - data types and structures editor;
   - bookmarks and comments;
   - memory map.
4. **Editing:** rename, retype, edit function signature, set comments, create structures and apply them, define code and data, and patch bytes with export of the patched file. Every edit can be undone and is recorded in history.
5. **Search:**
   - a command palette for actions and navigation;
   - global search across symbols, strings, comments and decompiled text;
   - byte-pattern search with wildcards;
   - instruction search.
6. **Comparing builds.** Open two programs, run the Version Tracking correlators headless to match functions, and list them as matched, changed, added or removed. A side-by-side function diff covers decompiled code and disassembly. Names, comments and types can be ported from the old build to the new one.
7. **Investigation journal and reports:**
   - evidence, hypotheses, findings and open questions, linked to addresses;
   - a timeline of human and agent actions;
   - Markdown and HTML report export.
8. **Agents:**
   - the CLI, MCP server, setup and doctor (§5.3);
   - an agent chat panel that talks to:
     - (a) a local OpenAI-compatible endpoint (Ollama, LM Studio, llama.cpp server);
     - (b) the user's own API key for Anthropic or OpenAI, stored in the keychain;
     - (c) an installed agent CLI (Claude Code, Codex) driving Opcodex through MCP.
   - Cloud backends show a preview of the outgoing data before each request, and every backend starts disabled.
9. **Scripting.** Run Python (PyGhidra, with the user's CPython 3.9–3.14) and Java scripts against the open program. Output goes to a console, and scripts are listed and searchable. Running a script is a `run`-class operation, so it's subject to approval when an agent triggers it.
10. **Settings and themes.** Opcodex Light (default) and Opcodex Dark, font size, key bindings, model backends, privacy, and per-project approval policy.

## 7. Extension points Pro needs from Community

Community defines these, and only these. Community code never imports Pro.

- **Engine:** additional catalog operations are loaded from extension JARs through Java `ServiceLoader` (`io.opcodex.engine.spi.OperationProvider`). The interface owns the schemas, class and handler.
- **Front end:** a typed extension registry (panels, commands, triage-summary sections, report sections) that a build can populate at compile time. Pro composes its own app build from Community packages plus private packages, with no runtime code download.
- **Entitlement hook:** an `EntitlementProvider` SPI whose Community implementation returns "no paid features". Community contains no license logic.

## 8. Packaging and distribution

- Tauri bundles: Windows (MSI and the existing MSIX/Store path, adapted to the new binary), Linux (`.deb`, AppImage), macOS (`.dmg`, later notarized).
- Each bundle contains the shell, the CLI, a bundled JDK 21 runtime and the engine distribution. Both the CLI and the desktop find the bundled runtime; the user needs no Java install.
- The packaged source revision and checksums stay tied to each artifact, as today.
- Installer signing is still an open release check.

## 9. Website (opcodex-web)

- Replace the workspace docs and gallery with the new front end, using real captures from an installed build only.
- New sections:
  - getting started;
  - triage;
  - workspace;
  - comparing builds;
  - journal and reports;
  - agents: CLI, MCP, setup for each agent app, approvals, privacy;
  - scripting;
  - CLI/MCP reference generated from the catalog.
- Update the release-status, system-requirements and download pages.
- **Pricing stays hidden** until Community v1 has shipped (owner instruction).

## 10. Verification

- Engine host: a test for each catalog operation against fixture binaries built from source in `opcodex/tests/fixtures`, plus API contract tests generated from the schemas.
- Parity check in CI: every front-end action maps to a catalog operation; the CLI and MCP expose the whole catalog.
- MCP end-to-end: an MCP client lists tools, opens a fixture, renames a function and undoes it.
- Front end: component tests, plus end-to-end tests through the Tauri WebDriver against a real engine host. Visual checks at 100% and 200% scale, in both themes.
- Installed-package smoke tests on Windows and Linux before any release claim, as with current practice. Development overlays are labelled as such.
- Performance budgets (§5.4) measured on a fixed large fixture.

## 11. Milestones (order; the builder sizes each)

1. Engine host, catalog, transport, auth, events, jobs. CLI for import, analysis, functions and decompile.
2. MCP server, `setup`, `doctor`, approvals, journal actor tracking.
3. Desktop shell: home, import, triage, functions, listing, decompiler, hex, navigation.
4. Xrefs, graphs, strings, symbols, types editor, search, editing, undo, patch export.
5. Build comparison, journal UI, reports.
6. Agent chat panel and backends; scripting console.
7. Packaging (all bundles, Store), website docs and reference.
8. Release gates: installed smoke tests, upstream suite gate, security review of later engine fixes (12.2 archive and server changes), signing.

## 12. Decisions after review (builder's calls, 15 Nov 2026, owner away; owner may reverse)

1. **Approvals and trust boundary.**
   - Approvals stop overeager or mistaken agents and make every action visible. They aren't a sandbox against a hostile process running as the same OS user, which can always edit the user's files.
   - Even so, credentials are split by client class:
     - The **desktop credential** is handed by the Tauri shell to the host over the child process's stdin at spawn. It's never written to disk.
     - The **client credential** (CLI and MCP) lives in the user-only token file.
   - `system`-class operations need the desktop credential. These include approving requests, changing the approval policy, and editing model backends and settings.
   - Actors are recorded as `desktop`, `mcp:<client name from MCP initialize>` or `cli`.
   - Headless with no desktop under `ask`: `write`/`run` calls fail with `APPROVAL_REQUIRED`. Changing the policy needs either the desktop, or `opcodex policy set` typed at an interactive terminal with a confirmation prompt. That command refuses when stdin isn't a TTY.
2. **Undo beyond the engine's limit.**
   - The engine's undo stack (50 transactions, in memory, linear) stays for quick Undo and Redo.
   - In addition, every `write` operation appends a journal entry with the actor, the before and after values of what it changed, and the program modification number.
   - `history.revert` reverts one entry selectively. It first checks that the current value still equals the recorded "after" value; on a mismatch it returns `CONFLICT` and changes nothing. A revert is itself journaled.
   - This holds across restarts.
3. **Upstream test gate.**
   - Missing fixtures that can be rebuilt from source are rebuilt and committed with build recipes and licenses, for example `HelloWorld.class` from a checked-in `HelloWorld.java`.
   - The release gate covers every test of the modules the v1 package ships and runs, plus all Opcodex tests.
   - Test suites that only exercise the Swing workbench or the debugger GUI (not shipped in v1) still run in CI and are reported, but they no longer block a release.
   - Debugger tests rejoin the gate when the live debugger ships.
   - No test inside the gate may be excluded.
4. **macOS.** Not a v1 release platform. CI builds it but doesn't publish it. Windows and Linux are the v1 platforms.
5. **One engine host across editions.**
   - Host discovery uses a per-user `engine.json` (port, PID, edition, API version).
   - Pro replaces Community on install (it offers to remove Community). The `opcodex` CLI name and the MCP registration name stay `opcodex` in both editions.

## 13. Left to the builder

UI library choices within §5.4, exact screen layouts within the design contract, internal module structure of the engine host, and the CLI argument style. Also deciding whether the triage risk categories come from a shipped rules file (preferred, so they can be edited and tested).

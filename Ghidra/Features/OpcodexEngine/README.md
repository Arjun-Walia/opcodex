# Opcodex engine host

A headless engine process that serves one operation catalog over JSON-RPC 2.0. The Opcodex desktop, the `opcodex` CLI and MCP clients all use the same operations, so anything a person can do, an agent can do too.

## Running it from the development tree

```bash
gradle :OpcodexEngine:printEngineClasspath --offline
java @Ghidra/Features/OpcodexEngine/build/engine.args io.opcodex.engine.EngineMain --tcp
```

Use a real JDK (`%JAVA_HOME%\bin\java`), not the Oracle `javapath` launcher stub. The stub starts a second JVM, so the process ID recorded in `engine.json` is wrong.

| Mode | Behaviour |
|---|---|
| `--tcp` | Listens on `127.0.0.1` on a random port. Writes `engine.json` (port, pid, edition, API version) and `client.token` to the session folder, readable by the current user only. Prints `{"ready":true,"port":N}`. |
| `--tcp --desktop-token-stdin` | Same, plus a desktop credential read from the first stdin line. The desktop shell uses this. |
| `--stdio` | Reads `{"credential":"…","kind":"DESKTOP\|CLIENT"}` from the first stdin line, then serves on stdin/stdout. Logging goes to stderr. |

The session folder is `%LOCALAPPDATA%\Opcodex\engine` on Windows and `$XDG_RUNTIME_DIR/opcodex` on Linux. `OPCODEX_SESSION_DIR` overrides it.

## Requests

```json
{"jsonrpc":"2.0","id":1,"method":"code.decompile","params":{"programId":"…","address":"00401000"},"auth":"<token>","actor":"mcp:claude-code"}
```

Every request is checked against its operation's schema. Unknown parameters, missing required ones, wrong types and out-of-range numbers are rejected. Lines are limited to 8 MiB. Large results come in pages of up to 1000 items, using `limit` and `cursor`/`nextCursor`.

| Error code | Meaning |
|---|---|
| `UNAUTHORIZED` | Missing or wrong token. |
| `FORBIDDEN` | A `system` operation called with a client credential, or a wrong or expired policy code. |
| `NOT_FOUND` | No project open, or the program, function or history entry doesn't exist. |
| `INVALID_PARAMS` | The request doesn't match the schema, or a value isn't valid (for example, an address or a name). |
| `STALE_REVISION` | The program changed since the `expectedRevision` the caller read. Nothing was changed. |
| `CONFLICT` | The project already exists, the name is taken, or a revert target changed since the edit. |
| `APPROVAL_REQUIRED` | The project's approval policy doesn't let this client write or run code. `data.preview` describes the request. |
| `CANCELLED` | Timed out or interrupted. |
| `ENGINE_ERROR` | The engine failed. Details are in the engine log, never in the response. |

## Operations (milestone 1)

`engine.info`, `catalog.list`, `project.create` (refuses to replace an existing project), `project.open`, `project.close`, `program.list`, `program.info`, `program.import`, `program.analyze`, `program.functions.list`, `function.get`, `code.decompile`, `code.disassemble`, `xrefs.to`, `xrefs.from`, `program.strings.list`, `symbol.rename`, `comment.set`, `history.list`, `history.revert`, `policy.get`, `policy.set` (desktop only), `policy.requestChange` and `policy.confirmChange` (terminal confirmation; not offered to agents).

`catalog.list` returns each operation's description, class (`read`, `write`, `run` or `system`) and JSON Schema.

## Edits, history and approvals

Every write carries `expectedRevision` (from `program.info`). Each write runs on that program's own writer thread, in one transaction, and is saved when it commits. The change goes into `<project>.rep/opcodex/journal.jsonl` with:

- the actor (`desktop`, `cli` or `mcp:<client>`);
- the value before and after (`null` means a default name or no comment);
- a readable summary.

`history.revert` undoes one entry if the value still matches what the edit set. Otherwise it returns `CONFLICT` and changes nothing. History survives restarts.

The approval policy is stored per project in `opcodex/policy.json`:

| Mode | Effect |
|---|---|
| `ask` (default) | Clients can't write or run code. |
| `allow-writes` | Clients can write. |
| `allow-all` | Clients can write and run code. |

The desktop is always allowed. The policy is changed from the desktop, or with `opcodex policy set` typed at a terminal.

Approvals keep overeager agents in check and make every change visible. They aren't a sandbox against a hostile program running as the same OS user, which can always edit the user's files directly.

## Tests

```bash
gradle :OpcodexEngine:test :OpcodexEngine:integrationTest --offline
```

Upstream Gradle reports success even when tests fail. Check `build/test-results/**.xml` for failures.

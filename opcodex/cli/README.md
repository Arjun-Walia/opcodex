# opcodex CLI and MCP server

`opcodex` runs any Opcodex engine operation from a terminal, a script, CI or an AI agent. It uses the same catalog as the desktop.

```bash
opcodex commands                                   # every operation
opcodex code.decompile --help                      # parameters of one operation
opcodex --project D:/work/demo program.list        # open a project, then run
opcodex --format text code.decompile --program-id <id> --address 00401000
opcodex --format csv program.functions.list --program-id <id> --limit 1000
opcodex policy set allow-writes                    # typed by a person at a terminal
opcodex serve                                      # a shared engine for the CLI and agents
opcodex mcp                                        # MCP server on stdin/stdout
```

Parameters are `--kebab-case` versions of the schema names. `expectedRevision` becomes `--expected-revision`. `--params-json '{…}'` passes raw values.

Output formats: `json` (default), `jsonl`, `md`, `text` and `csv`.

Exit codes:

| Code | Meaning |
|---|---|
| 0 | Success. |
| 1 | A usage or connection problem. |
| 2 | The engine refused or failed. stderr shows `CODE: message`. |

## Finding an engine

1. If `engine.json` in the session folder points at a running engine (the desktop or `opcodex serve`), the CLI connects to it.
2. Otherwise it starts a private engine over stdio, using `OPCODEX_ENGINE_CMD`. That variable holds the full command, for example `"C:/Program Files/Java/jdk-25.0.2/bin/java.exe" @D:/opcodex/Ghidra/Features/OpcodexEngine/build/engine.args io.opcodex.engine.EngineMain`.

`OPCODEX_SESSION_DIR` overrides the session folder.

## MCP

`opcodex mcp` serves protocol version 2025-06-18 (2025-03-26 and 2024-11-05 are also accepted). Tools are the catalog operations with `.` replaced by `_` (`code_decompile`, `symbol_rename`, …). They carry the catalog description, the JSON Schema and a read-only hint.

- Operations that change engine policy or settings are never tools.
- Changes are recorded as `mcp:<client name>`.
- If the project's approval policy blocks a change, the tool returns an error that names `opcodex policy set`.

To register it with an agent app, run for example:

```bash
claude mcp add opcodex -- <path>/opcodex mcp
```

`opcodex setup` will do this for each installed agent app in milestone 2.

## Tests

```bash
cargo test                            # unit tests; end-to-end tests skip themselves
OPCODEX_ENGINE_CMD='…' cargo test     # end-to-end against a real engine
```

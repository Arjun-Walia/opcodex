// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
mod common;

use serde_json::{json, Value};
use std::io::{BufRead, BufReader, Write};
use std::process::{Child, ChildStdin, ChildStdout, Command, Stdio};

struct Mcp {
    child: Child,
    stdin: ChildStdin,
    stdout: BufReader<ChildStdout>,
    next_id: u64,
}

impl Mcp {
    fn start(session_dir: &std::path::Path) -> Mcp {
        let mut child = Command::new(env!("CARGO_BIN_EXE_opcodex"))
            .arg("mcp")
            .env("OPCODEX_SESSION_DIR", session_dir)
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .stderr(Stdio::inherit())
            .spawn()
            .unwrap();
        let stdin = child.stdin.take().unwrap();
        let stdout = BufReader::new(child.stdout.take().unwrap());
        let mut mcp = Mcp { child, stdin, stdout, next_id: 1 };
        let init = mcp.request("initialize", json!({
            "protocolVersion": "2025-06-18",
            "capabilities": {},
            "clientInfo": {"name": "Test Client", "version": "1.0"}
        }));
        assert_eq!("opcodex", init["serverInfo"]["name"]);
        assert!(init["capabilities"]["tools"].is_object());
        mcp.notify("notifications/initialized");
        mcp
    }

    fn notify(&mut self, method: &str) {
        let msg = json!({"jsonrpc": "2.0", "method": method}).to_string() + "\n";
        self.stdin.write_all(msg.as_bytes()).unwrap();
    }

    fn request(&mut self, method: &str, params: Value) -> Value {
        let id = self.next_id;
        self.next_id += 1;
        let msg = json!({"jsonrpc": "2.0", "id": id, "method": method, "params": params});
        self.stdin.write_all((msg.to_string() + "\n").as_bytes()).unwrap();
        self.stdin.flush().unwrap();
        loop {
            let mut line = String::new();
            assert!(self.stdout.read_line(&mut line).unwrap() > 0, "MCP server closed");
            let response: Value = serde_json::from_str(&line).unwrap();
            if response["id"] == json!(id) {
                assert!(response.get("error").is_none(), "MCP error: {response}");
                return response["result"].clone();
            }
        }
    }

    /// Calls a tool and returns its structured result, panicking if the tool reported an error.
    fn tool(&mut self, name: &str, arguments: Value) -> Value {
        let result = self.request("tools/call", json!({"name": name, "arguments": arguments}));
        assert_ne!(Some(true), result["isError"].as_bool(), "tool {name} failed: {result}");
        let text = result["content"][0]["text"].as_str().unwrap();
        serde_json::from_str(text).unwrap()
    }
}

impl Drop for Mcp {
    fn drop(&mut self) {
        let _ = self.child.kill();
        let _ = self.child.wait();
    }
}

#[test]
fn mcp_lists_tools_without_system_ops() {
    let Some(engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    let mut mcp = Mcp::start(&engine.session_dir);
    let tools = mcp.request("tools/list", json!({}))["tools"].as_array().unwrap().clone();
    let find = |name: &str| tools.iter().find(|t| t["name"] == name).cloned();
    assert!(find("policy_set").is_none(), "system operations must not be agent tools");
    let rename = find("symbol_rename").expect("symbol_rename tool");
    assert_eq!(Some(false), rename["annotations"]["readOnlyHint"].as_bool());
    assert_eq!("object", rename["inputSchema"]["type"]);
    let decompile = find("code_decompile").expect("code_decompile tool");
    assert_eq!(Some(true), decompile["annotations"]["readOnlyHint"].as_bool());
    assert!(decompile["description"].as_str().unwrap().len() > 10);
}

#[test]
fn mcp_call_rename_then_revert() {
    let Some(mut engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    engine.project_allowing_writes("mcp");
    let elf = common::write_return42_elf();
    let mut mcp = Mcp::start(&engine.session_dir);

    let imported = mcp.tool("program_import", json!({"path": elf.to_str().unwrap(), "name": "ret42.elf"}));
    let program_id = imported["programId"].as_str().unwrap().to_string();
    mcp.tool("program_analyze", json!({"programId": program_id}));
    let entry = mcp.tool("program_functions_list", json!({"programId": program_id}))["items"][0]["entry"]
        .as_str()
        .unwrap()
        .to_string();
    let revision = mcp.tool("program_info", json!({"programId": program_id}))["revision"].as_i64().unwrap();
    let renamed = mcp.tool("symbol_rename", json!({"programId": program_id, "address": entry,
        "newName": "answer", "expectedRevision": revision}));

    let history = mcp.tool("history_list", json!({"programId": program_id}));
    let newest = &history["items"][0];
    assert_eq!("mcp:test-client", newest["actor"]);
    assert_eq!(renamed["entryId"], newest["id"]);

    mcp.tool("history_revert", json!({"programId": program_id, "entryId": renamed["entryId"],
        "expectedRevision": renamed["revision"]}));
    let function = mcp.tool("function_get", json!({"programId": program_id, "address": entry}));
    assert_ne!("answer", function["name"]);
}

#[test]
fn mcp_reports_approval_required_as_a_tool_error() {
    let Some(mut engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    let dir = engine.projects_dir.to_string_lossy().to_string();
    engine.desktop("project.create", json!({"dir": dir, "name": "ask"}));
    let elf = common::write_return42_elf();
    let mut mcp = Mcp::start(&engine.session_dir);
    let result = mcp.request("tools/call", json!({"name": "program_import",
        "arguments": {"path": elf.to_str().unwrap(), "name": "ret42.elf"}}));
    assert_eq!(Some(true), result["isError"].as_bool());
    let text = result["content"][0]["text"].as_str().unwrap();
    assert!(text.contains("APPROVAL_REQUIRED"), "{text}");
    assert!(text.contains("opcodex policy set"), "{text}");
}

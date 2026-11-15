// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! MCP server over stdio: every engine operation an agent may use, as a tool.

use crate::client::{Client, EngineError};
use anyhow::Result;
use serde_json::{json, Value};
use std::io::{BufRead, Write};

pub const PROTOCOL_VERSION: &str = "2025-06-18";
const SUPPORTED_VERSIONS: [&str; 3] = ["2025-06-18", "2025-03-26", "2024-11-05"];

/// Operations agents never get: they change policy, need a person at a terminal, or would close
/// the project other people and agents are using.
const HIDDEN: [&str; 3] = ["policy.requestChange", "policy.confirmChange", "project.close"];

const INSTRUCTIONS: &str = "Opcodex analyses binaries. Start with program_list (or project_open, \
then program_import and program_analyze), then program_functions_list, code_decompile and \
xrefs_to. Writes such as symbol_rename need expectedRevision from program_info and follow the \
project's approval policy. Every change is recorded in history_list and can be reverted with \
history_revert.";

/// `mcp:<name>` for the engine's change history, from the MCP client's name.
pub fn actor_for(client_name: &str) -> String {
    let mut name: String = client_name
        .to_lowercase()
        .chars()
        .map(|c| if c.is_ascii_alphanumeric() || c == '.' || c == '_' || c == '-' { c } else { '-' })
        .collect();
    while name.contains("--") {
        name = name.replace("--", "-");
    }
    let name = name.trim_matches('-');
    let name: String = name.chars().take(64).collect();
    if name.is_empty() { "mcp:client".to_string() } else { format!("mcp:{name}") }
}

pub fn tool_name(operation: &str) -> String {
    operation.replace('.', "_")
}

/// Catalog operations as MCP tool descriptions.
pub fn tools(catalog: &Value) -> Vec<Value> {
    catalog
        .as_array()
        .map(|ops| {
            ops.iter()
                .filter(|op| op["class"] != "system")
                .filter(|op| !HIDDEN.contains(&op["name"].as_str().unwrap_or("")))
                .map(|op| {
                    let class = op["class"].as_str().unwrap_or("read");
                    json!({
                        "name": tool_name(op["name"].as_str().unwrap_or("")),
                        "title": op["name"],
                        "description": op["description"],
                        "inputSchema": op["params"],
                        "annotations": {
                            "readOnlyHint": class == "read",
                            "destructiveHint": false,
                            "idempotentHint": class == "read",
                            "openWorldHint": false
                        }
                    })
                })
                .collect()
        })
        .unwrap_or_default()
}

struct Server {
    client: Option<Client>,
    catalog: Option<Value>,
    actor: String,
    project: Option<Value>,
}

impl Server {
    fn engine(&mut self) -> Result<&mut Client> {
        if self.client.is_none() {
            let mut client = Client::connect(&self.actor)?;
            if let Some(project) = &self.project {
                client.call("project.open", project.clone())?;
            }
            self.client = Some(client);
        }
        Ok(self.client.as_mut().expect("connected"))
    }

    fn catalog(&mut self) -> Result<Value> {
        if self.catalog.is_none() {
            let catalog = self.engine()?.call("catalog.list", json!({}))?;
            self.catalog = Some(catalog);
        }
        Ok(self.catalog.clone().unwrap_or(Value::Null))
    }

    fn handle(&mut self, method: &str, params: &Value) -> std::result::Result<Value, (i64, String)> {
        match method {
            "initialize" => {
                if let Some(name) = params["clientInfo"]["name"].as_str() {
                    self.actor = actor_for(name);
                    if let Some(client) = self.client.as_mut() {
                        client.set_actor(&self.actor);
                    }
                }
                let requested = params["protocolVersion"].as_str().unwrap_or(PROTOCOL_VERSION);
                let version = if SUPPORTED_VERSIONS.contains(&requested) { requested } else { PROTOCOL_VERSION };
                Ok(json!({
                    "protocolVersion": version,
                    "capabilities": {"tools": {"listChanged": false}},
                    "serverInfo": {"name": "opcodex", "title": "Opcodex", "version": env!("CARGO_PKG_VERSION")},
                    "instructions": INSTRUCTIONS
                }))
            }
            "ping" => Ok(json!({})),
            "tools/list" => {
                let catalog = self.catalog().map_err(|e| (-32603, format!("{e:#}")))?;
                Ok(json!({"tools": tools(&catalog)}))
            }
            "tools/call" => Ok(self.call_tool(params)),
            _ => Err((-32601, format!("Method not found: {method}"))),
        }
    }

    fn call_tool(&mut self, params: &Value) -> Value {
        let name = params["name"].as_str().unwrap_or("");
        let catalog = match self.catalog() {
            Ok(catalog) => catalog,
            Err(e) => return tool_error(format!("Opcodex engine unavailable: {e:#}")),
        };
        let allowed = tools(&catalog);
        let Some(tool) = allowed.iter().find(|t| t["name"] == name) else {
            return tool_error(format!("Unknown tool {name}."));
        };
        let operation = tool["title"].as_str().unwrap_or("").to_string();
        let read_only = tool["annotations"]["readOnlyHint"].as_bool().unwrap_or(false);
        let arguments = if params["arguments"].is_object() { params["arguments"].clone() } else { json!({}) };
        let mut result = self.call_engine(&operation, arguments.clone());
        if let Err(error) = &result {
            if error.downcast_ref::<EngineError>().is_none() {
                // The engine went away (restart or crash). Reconnect for this and later calls.
                // Only reads are retried: a write may already have been applied.
                self.client = None;
                self.catalog = None;
                if read_only {
                    result = self.call_engine(&operation, arguments);
                }
            }
        }
        match result {
            Ok(value) => {
                let text = serde_json::to_string_pretty(&value).unwrap_or_default();
                let mut out = json!({"content": [{"type": "text", "text": text}], "isError": false});
                if value.is_object() {
                    out["structuredContent"] = value;
                }
                out
            }
            Err(error) => match error.downcast_ref::<EngineError>() {
                Some(engine) => {
                    let mut text = format!("{}: {}", engine.code, engine.message);
                    if let Some(preview) = engine.data["preview"].as_str() {
                        text.push_str(&format!("\nRequested: {preview}"));
                    }
                    tool_error(text)
                }
                None => tool_error(format!("{error:#}")),
            },
        }
    }
}

impl Server {
    fn call_engine(&mut self, operation: &str, arguments: Value) -> Result<Value> {
        let engine = self.engine()?;
        engine.call(operation, arguments)
    }
}

fn tool_error(text: String) -> Value {
    json!({"content": [{"type": "text", "text": text}], "isError": true})
}

/// Serves MCP on stdin/stdout until stdin closes.
pub fn serve(project: Option<Value>) -> Result<()> {
    let stdin = std::io::stdin();
    let mut stdout = std::io::stdout();
    let mut server = Server { client: None, catalog: None, actor: "mcp:client".into(), project };
    for line in stdin.lock().lines() {
        let line = line?;
        if line.trim().is_empty() {
            continue;
        }
        let message: Value = match serde_json::from_str(&line) {
            Ok(message) => message,
            Err(_) => {
                let reply = json!({"jsonrpc": "2.0", "id": null,
                    "error": {"code": -32700, "message": "Parse error"}});
                writeln!(stdout, "{reply}")?;
                stdout.flush()?;
                continue;
            }
        };
        let Some(id) = message.get("id").cloned() else {
            continue; // A notification such as notifications/initialized.
        };
        let method = message["method"].as_str().unwrap_or("");
        let params = message.get("params").cloned().unwrap_or(json!({}));
        let reply = match server.handle(method, &params) {
            Ok(result) => json!({"jsonrpc": "2.0", "id": id, "result": result}),
            Err((code, text)) => json!({"jsonrpc": "2.0", "id": id, "error": {"code": code, "message": text}}),
        };
        writeln!(stdout, "{reply}")?;
        stdout.flush()?;
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn actor_names_are_sanitised() {
        assert_eq!("mcp:claude-code", actor_for("Claude Code"));
        assert_eq!("mcp:cursor.vscode", actor_for("cursor.vscode"));
        assert_eq!("mcp:client", actor_for("  !!  "));
        assert_eq!(68, actor_for(&"x".repeat(200)).len());
    }

    #[test]
    fn system_and_hidden_operations_are_not_tools() {
        let catalog = json!([
            {"name": "program.list", "class": "read", "description": "d", "params": {}},
            {"name": "policy.set", "class": "system", "description": "d", "params": {}},
            {"name": "policy.requestChange", "class": "read", "description": "d", "params": {}}
        ]);
        let names: Vec<String> = tools(&catalog).iter().map(|t| t["name"].as_str().unwrap().to_string()).collect();
        assert_eq!(vec!["program_list".to_string()], names);
    }
}

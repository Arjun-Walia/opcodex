// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Opcodex command line: every engine operation as a subcommand, plus the MCP server.
//!
//! Exit codes: 0 success, 1 usage or connection problem, 2 the engine refused or failed.

mod catalog;
mod client;
mod format;
mod mcp;

use anyhow::{anyhow, bail, Result};
use client::{Client, EngineError};
use format::Format;
use serde_json::{json, Value};
use std::path::Path;

const USAGE: &str = "\
Usage: opcodex [--format json|jsonl|md|text|csv] [--project PATH] <command> [--param value ...]

Commands:
  commands              List every engine operation
  <operation> --help    Describe an operation and its parameters
  <operation> ...       Run an operation, for example: opcodex program.list
  serve                 Run a shared engine in this terminal for the desktop, CLI and agents
  mcp                   Run the MCP server on stdin/stdout

--project PATH opens <PATH>.gpr first (PATH without the .gpr extension).
Environment: OPCODEX_ENGINE_CMD starts an engine when none is running;
OPCODEX_SESSION_DIR overrides where a running engine is found.";

struct Global {
    format: Format,
    project: Option<String>,
    rest: Vec<String>,
}

fn parse_global(args: Vec<String>) -> Result<Global> {
    let mut format = Format::Json;
    let mut project = None;
    let mut i = 0;
    while i < args.len() {
        match args[i].as_str() {
            "--format" | "--output-format" => {
                let value = args.get(i + 1).ok_or_else(|| anyhow!("--format needs a value"))?;
                format = Format::parse(value)?;
                i += 2;
            }
            "--json" => {
                format = Format::Json;
                i += 1;
            }
            "--project" => {
                project = Some(args.get(i + 1).ok_or_else(|| anyhow!("--project needs a path"))?.clone());
                i += 2;
            }
            _ => break,
        }
    }
    Ok(Global { format, project, rest: args[i..].to_vec() })
}

/// Splits `D:\work\demo` (or `.../demo.gpr`) into the project folder and name.
fn project_location(path: &str) -> Result<Value> {
    let trimmed = path.strip_suffix(".gpr").unwrap_or(path);
    let full = std::path::absolute(Path::new(trimmed))?;
    let name = full.file_name().and_then(|n| n.to_str()).ok_or_else(|| anyhow!("--project needs a project name"))?;
    let dir = full.parent().ok_or_else(|| anyhow!("--project needs a folder"))?;
    Ok(json!({"dir": dir.to_string_lossy(), "name": name}))
}

fn run() -> Result<()> {
    let global = parse_global(std::env::args().skip(1).collect())?;
    let Some(command) = global.rest.first().cloned() else {
        println!("{USAGE}");
        return Ok(());
    };
    let args = &global.rest[1..];
    match command.as_str() {
        "help" | "--help" | "-h" => {
            println!("{USAGE}");
            Ok(())
        }
        "--version" | "-V" => {
            println!("opcodex {}", env!("CARGO_PKG_VERSION"));
            Ok(())
        }
        "serve" => serve(),
        "mcp" => {
            let project = global.project.as_deref().map(project_location).transpose()?;
            mcp::serve(project)
        }
        _ => run_operation(&global, &command, args),
    }
}

fn connect(global: &Global) -> Result<Client> {
    let mut client = Client::connect("cli")?;
    if let Some(project) = &global.project {
        client.call("project.open", project_location(project)?)?;
    }
    Ok(client)
}

fn run_operation(global: &Global, command: &str, args: &[String]) -> Result<()> {
    let mut client = connect(global)?;
    let catalog = client.call("catalog.list", json!({}))?;
    if command == "commands" {
        let ops = catalog.as_array().cloned().unwrap_or_default();
        for op in ops {
            println!(
                "{:<26} {:<6} {}",
                op["name"].as_str().unwrap_or(""),
                op["class"].as_str().unwrap_or(""),
                op["description"].as_str().unwrap_or("")
            );
        }
        return Ok(());
    }
    let Some(op) = catalog::find(&catalog, command) else {
        return Err(EngineError {
            code: "NOT_FOUND".into(),
            message: format!("Unknown operation {command}. Run `opcodex commands` to list them."),
            data: Value::Null,
        }
        .into());
    };
    if args.iter().any(|a| a == "--help" || a == "-h") {
        print!("{}", catalog::help(op));
        return Ok(());
    }
    let params = catalog::parse_params(op, args)?;
    let result = client.call(command, params)?;
    println!("{}", format::render(&result, global.format));
    Ok(())
}

/// Runs a shared engine host in the foreground until interrupted.
fn serve() -> Result<()> {
    let parts = client::engine_command()?;
    let status = std::process::Command::new(&parts[0]).args(&parts[1..]).arg("--tcp").status()?;
    if !status.success() {
        bail!("The engine stopped with {status}.");
    }
    Ok(())
}

fn main() {
    match run() {
        Ok(()) => {}
        Err(error) => {
            if let Some(engine) = error.downcast_ref::<EngineError>() {
                eprintln!("{engine}");
                std::process::exit(2);
            }
            eprintln!("opcodex: {error:#}");
            std::process::exit(1);
        }
    }
}

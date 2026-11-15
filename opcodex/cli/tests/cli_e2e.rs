// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
mod common;

use serde_json::Value;
use std::process::{Command, Output};

fn opcodex(session_dir: &std::path::Path, args: &[&str]) -> Output {
    Command::new(env!("CARGO_BIN_EXE_opcodex"))
        .args(args)
        .env("OPCODEX_SESSION_DIR", session_dir)
        .output()
        .expect("run opcodex")
}

fn json_out(output: &Output) -> Value {
    assert!(
        output.status.success(),
        "exit {:?}\nstdout: {}\nstderr: {}",
        output.status.code(),
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    serde_json::from_slice(&output.stdout).expect("json stdout")
}

#[test]
fn e2e_import_list_decompile() {
    let Some(mut engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    engine.project_allowing_writes("cli");
    let elf = common::write_return42_elf();
    let dir = engine.session_dir.clone();

    let imported = json_out(&opcodex(&dir, &["program.import", "--path",
        elf.to_str().unwrap(), "--name", "ret42.elf"]));
    let program_id = imported["programId"].as_str().unwrap().to_string();

    json_out(&opcodex(&dir, &["program.analyze", "--program-id", &program_id]));
    let functions = json_out(&opcodex(&dir, &["program.functions.list", "--program-id", &program_id]));
    let entry = functions["items"][0]["entry"].as_str().expect("a function").to_string();

    let out = opcodex(&dir, &["--format", "text", "code.decompile", "--program-id", &program_id,
        "--address", &entry]);
    assert!(out.status.success(), "{}", String::from_utf8_lossy(&out.stderr));
    assert!(String::from_utf8_lossy(&out.stdout).contains("0x2a"));

    let history = json_out(&opcodex(&dir, &["--format", "json", "history.list", "--program-id", &program_id]));
    let items = history["items"].as_array().unwrap();
    let ops: Vec<&str> = items.iter().map(|e| e["op"].as_str().unwrap()).collect();
    assert_eq!(vec!["program.analyze", "program.import"], ops);
    assert!(items.iter().all(|e| e["actor"] == "cli"));
}

#[test]
fn unknown_operation_exits_2() {
    let Some(engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    let out = opcodex(&engine.session_dir, &["no.such.operation"]);
    assert_eq!(Some(2), out.status.code());
    assert!(String::from_utf8_lossy(&out.stderr).contains("no.such.operation"));
}

#[test]
fn client_write_under_ask_exits_2_with_approval_code() {
    let Some(mut engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    let dir = engine.projects_dir.to_string_lossy().to_string();
    engine.desktop("project.create", serde_json::json!({"dir": dir, "name": "ask"}));
    let elf = common::write_return42_elf();
    let out = opcodex(&engine.session_dir, &["program.import", "--path", elf.to_str().unwrap(),
        "--name", "ret42.elf"]);
    assert_eq!(Some(2), out.status.code());
    assert!(String::from_utf8_lossy(&out.stderr).contains("APPROVAL_REQUIRED"));
}

#[test]
fn commands_lists_the_catalog() {
    let Some(engine) = common::Engine::start() else {
        eprintln!("skipped: set OPCODEX_ENGINE_CMD to run end-to-end tests");
        return;
    };
    let out = opcodex(&engine.session_dir, &["commands"]);
    assert!(out.status.success());
    let text = String::from_utf8_lossy(&out.stdout);
    assert!(text.contains("code.decompile"));
    assert!(text.contains("symbol.rename"));
}

#[test]
fn no_engine_and_no_command_explains_how_to_start() {
    let empty = std::env::temp_dir().join("opcodex-no-engine-session");
    let _ = std::fs::create_dir_all(&empty);
    let out = Command::new(env!("CARGO_BIN_EXE_opcodex"))
        .arg("engine.info")
        .env("OPCODEX_SESSION_DIR", &empty)
        .env_remove("OPCODEX_ENGINE_CMD")
        .output()
        .unwrap();
    assert_eq!(Some(1), out.status.code());
    assert!(String::from_utf8_lossy(&out.stderr).contains("OPCODEX_ENGINE_CMD"));
}

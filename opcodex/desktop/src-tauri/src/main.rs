// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Opcodex desktop: hosts the interface, supervises the engine and runs terminals.
#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod engine;
mod terminal;

use engine::Engine;
use serde_json::{json, Value};
use std::path::PathBuf;
use std::process::Command;
use std::sync::Mutex;
use tauri::{AppHandle, Manager, RunEvent, State};
use terminal::Terminals;

#[derive(Default)]
struct Shell {
    engine: Mutex<Option<Engine>>,
    terminals: Terminals,
}

const AGENT_IDS: [&str; 7] = ["claude-code", "claude-desktop", "cursor", "vscode", "windsurf", "gemini", "codex"];

fn exe_dir() -> PathBuf {
    std::env::current_exe().ok().and_then(|p| p.parent().map(PathBuf::from)).unwrap_or_default()
}

fn engine_command() -> Result<Vec<String>, String> {
    let exe = std::env::current_exe().map_err(|e| e.to_string())?;
    let env = std::env::var("OPCODEX_ENGINE_CMD").ok();
    match engine::resolve(&exe, env) {
        Ok(parts) => Ok(parts),
        #[cfg(debug_assertions)]
        Err(_) => {
            // Development build: run the engine from this checkout's Gradle output.
            let args = PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("../../../Ghidra/Features/OpcodexEngine/build/engine.args");
            let java = std::env::var_os("JAVA_HOME")
                .map(|h| PathBuf::from(h).join("bin").join(if cfg!(windows) { "java.exe" } else { "java" }))
                .map(|p| p.to_string_lossy().into_owned())
                .unwrap_or_else(|| "java".into());
            Ok(vec![java, format!("@{}", args.display()), "io.opcodex.engine.EngineMain".into()])
        }
        #[cfg(not(debug_assertions))]
        Err(e) => Err(e),
    }
}

/// The opcodex CLI shipped with this app (or the checkout's build during development).
fn cli_path() -> Option<PathBuf> {
    let name = if cfg!(windows) { "opcodex.exe" } else { "opcodex" };
    let candidates = [
        std::env::var_os("OPCODEX_CLI").map(PathBuf::from),
        Some(exe_dir().join(name)),
        Some(exe_dir().join("app").join("support").join(name)),
        Some(PathBuf::from(env!("CARGO_MANIFEST_DIR")).join("../../cli/target/release").join(name)),
    ];
    candidates.into_iter().flatten().find(|p| p.is_file())
}

fn error(message: impl Into<String>, code: &str) -> Value {
    json!({"error": {"message": message.into(), "data": {"code": code}}})
}

fn run_cli(args: &[&str]) -> Result<(bool, String, String), String> {
    let cli = cli_path().ok_or("The opcodex command-line tool is missing. Reinstall Opcodex.")?;
    let mut command = Command::new(cli);
    command.args(args);
    #[cfg(windows)]
    {
        use std::os::windows::process::CommandExt;
        command.creation_flags(0x0800_0000);
    }
    let out = command.output().map_err(|e| e.to_string())?;
    Ok((
        out.status.success(),
        String::from_utf8_lossy(&out.stdout).into_owned(),
        String::from_utf8_lossy(&out.stderr).into_owned(),
    ))
}

fn desktop_call(method: &str, params: &Value) -> Value {
    match method {
        "__desktop.defaultProjectsDir" => {
            let dir = dirs::document_dir().unwrap_or_else(|| dirs::home_dir().unwrap_or_default()).join("Opcodex Projects");
            json!({"result": dir.to_string_lossy()})
        }
        "__desktop.agents.status" => match run_cli(&["doctor", "--format", "json"]) {
            Ok((_, stdout, stderr)) => match serde_json::from_str::<Vec<Value>>(&stdout) {
                Ok(checks) => {
                    let apps = checks.iter().find(|c| c["name"] == "agents").map(|c| c["detail"].clone()).unwrap_or(json!([]));
                    json!({"result": {"executable": cli_path().map(|p| p.to_string_lossy().into_owned()), "apps": apps}})
                }
                Err(_) => error(if stderr.is_empty() { "opcodex doctor failed.".to_string() } else { stderr }, "ENGINE_ERROR"),
            },
            Err(e) => error(e, "ENGINE_ERROR"),
        },
        "__desktop.agents.connect" | "__desktop.agents.disconnect" => {
            let app = params["app"].as_str().unwrap_or_default();
            if !AGENT_IDS.contains(&app) {
                return error("Unknown agent app.", "INVALID_PARAMS");
            }
            let args: Vec<&str> = if method.ends_with("connect") && !method.ends_with("disconnect") {
                vec!["setup", "--only", app, "--yes"]
            } else {
                vec!["setup", "--uninstall", "--only", app, "--yes"]
            };
            match run_cli(&args) {
                Ok((true, stdout, _)) => json!({"result": {"output": stdout.trim()}}),
                Ok((false, stdout, stderr)) => error(if stderr.trim().is_empty() { stdout } else { stderr }.trim(), "ENGINE_ERROR"),
                Err(e) => error(e, "ENGINE_ERROR"),
            }
        }
        _ => error(format!("Unknown desktop command {method}"), "NOT_FOUND"),
    }
}

fn ensure_engine(app: &AppHandle, slot: &mut Option<Engine>) -> Result<(), String> {
    if let Some(engine) = slot.as_mut() {
        if engine.is_running() {
            return Ok(());
        }
    }
    let log = app.path().app_log_dir().ok().map(|dir| {
        let _ = std::fs::create_dir_all(&dir);
        dir.join("engine.log")
    });
    *slot = Some(Engine::start(&engine_command()?, log)?);
    Ok(())
}

#[tauri::command]
async fn engine_call(app: AppHandle, state: State<'_, Shell>, method: String, params: Value) -> Result<Value, String> {
    if method.starts_with("__desktop.") {
        return tauri::async_runtime::spawn_blocking(move || desktop_call(&method, &params)).await.map_err(|e| e.to_string());
    }
    let shell: &Shell = &state;
    let mut slot = shell.engine.lock().map_err(|e| e.to_string())?;
    ensure_engine(&app, &mut slot)?;
    let first = slot.as_ref().expect("engine started").call(&method, &params);
    match first {
        Ok(reply) => Ok(reply),
        Err(e) if e.kind() == std::io::ErrorKind::ConnectionRefused => {
            // The engine stopped between calls: start a fresh one and try once more.
            *slot = None;
            ensure_engine(&app, &mut slot)?;
            slot.as_ref().expect("engine started").call(&method, &params).map_err(|e| e.to_string())
        }
        Err(e) => Err(format!("The engine connection failed: {e}")),
    }
}

#[tauri::command]
fn terminal_open(app: AppHandle, state: State<'_, Shell>, cols: u16, rows: u16, command: Option<String>) -> Result<u32, String> {
    let cli_dir = cli_path().and_then(|p| p.parent().map(PathBuf::from));
    state.terminals.open(app, cols, rows, None, cli_dir, command)
}

#[tauri::command]
fn terminal_write(state: State<'_, Shell>, id: u32, data: String) -> Result<(), String> {
    state.terminals.write(id, &data)
}

#[tauri::command]
fn terminal_resize(state: State<'_, Shell>, id: u32, cols: u16, rows: u16) -> Result<(), String> {
    state.terminals.resize(id, cols, rows)
}

#[tauri::command]
fn terminal_close(state: State<'_, Shell>, id: u32) {
    state.terminals.close(id);
}

fn main() {
    let app = tauri::Builder::default()
        .plugin(tauri_plugin_dialog::init())
        .manage(Shell::default())
        .invoke_handler(tauri::generate_handler![engine_call, terminal_open, terminal_write, terminal_resize, terminal_close])
        .build(tauri::generate_context!())
        .expect("Opcodex could not start");
    app.run(|handle, event| {
        if let RunEvent::Exit = event {
            let shell = handle.state::<Shell>();
            shell.terminals.close_all();
            // Dropping the engine stops its process.
            let engine = shell.engine.lock().ok().and_then(|mut slot| slot.take());
            drop(engine);
        }
    });
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! `opcodex setup` against temporary home folders; no real agent app is touched.

use serde_json::Value;
use std::fs;
use std::path::{Path, PathBuf};
use std::process::{Command, Output};

struct Home {
    home: PathBuf,
    appdata: PathBuf,
}

impl Home {
    fn new() -> Home {
        let mut bytes = [0u8; 6];
        getrandom::fill(&mut bytes).unwrap();
        let suffix: String = bytes.iter().map(|b| format!("{b:02x}")).collect();
        let base = std::env::temp_dir().join(format!("opcodex-setup-{suffix}"));
        let home = base.join("home");
        let appdata = base.join("appdata");
        fs::create_dir_all(&home).unwrap();
        fs::create_dir_all(&appdata).unwrap();
        Home { home, appdata }
    }

    fn file(&self, relative: &str, content: &str) -> PathBuf {
        let path = self.home.join(relative);
        fs::create_dir_all(path.parent().unwrap()).unwrap();
        fs::write(&path, content).unwrap();
        path
    }

    fn run(&self, args: &[&str]) -> Output {
        Command::new(env!("CARGO_BIN_EXE_opcodex"))
            .args(args)
            .env("OPCODEX_SETUP_HOME", &self.home)
            .env("OPCODEX_SETUP_APPDATA", &self.appdata)
            .output()
            .unwrap()
    }
}

fn text(out: &Output) -> String {
    format!("{}{}", String::from_utf8_lossy(&out.stdout), String::from_utf8_lossy(&out.stderr))
}

fn keys(value: &Value) -> Vec<String> {
    value.as_object().unwrap().keys().cloned().collect()
}

fn read_json(path: &Path) -> Value {
    serde_json::from_str(&fs::read_to_string(path).unwrap()).unwrap()
}

#[test]
fn setup_preserves_other_servers_and_key_order() {
    let home = Home::new();
    let config = home.file(".cursor/mcp.json",
        r#"{"zeta": 1, "mcpServers": {"other": {"command": "x"}}, "alpha": {"nested": true}}"#);

    let out = home.run(&["setup", "--only", "cursor", "--yes"]);
    assert!(out.status.success(), "{}", text(&out));
    let after = read_json(&config);
    assert_eq!(vec!["zeta", "mcpServers", "alpha"], keys(&after));
    assert_eq!(vec!["other", "opcodex"], keys(&after["mcpServers"]));
    assert_eq!(serde_json::json!(["mcp"]), after["mcpServers"]["opcodex"]["args"]);
    let command = after["mcpServers"]["opcodex"]["command"].as_str().unwrap();
    assert!(Path::new(command).is_absolute(), "{command}");
    assert_eq!(serde_json::json!({"nested": true}), after["alpha"]);

    let out = home.run(&["setup", "--uninstall", "--only", "cursor", "--yes"]);
    assert!(out.status.success(), "{}", text(&out));
    let removed = read_json(&config);
    assert_eq!(vec!["zeta", "mcpServers", "alpha"], keys(&removed));
    assert_eq!(vec!["other"], keys(&removed["mcpServers"]));
}

#[test]
fn vscode_entry_uses_servers_with_stdio_type() {
    let home = Home::new();
    fs::create_dir_all(home.appdata.join("Code").join("User")).unwrap();
    let out = home.run(&["setup", "--only", "vscode", "--yes"]);
    assert!(out.status.success(), "{}", text(&out));
    let config = read_json(&home.appdata.join("Code").join("User").join("mcp.json"));
    assert_eq!("stdio", config["servers"]["opcodex"]["type"]);
}

#[test]
fn setup_skips_malformed_json() {
    let home = Home::new();
    let config = home.file(".gemini/settings.json", "{not json");
    let out = home.run(&["setup", "--only", "gemini", "--yes"]);
    assert_eq!(Some(1), out.status.code());
    assert!(text(&out).contains("isn't valid JSON"), "{}", text(&out));
    assert_eq!("{not json", fs::read_to_string(&config).unwrap());
}

#[test]
fn setup_is_idempotent() {
    let home = Home::new();
    let config = home.file(".claude.json", r#"{"numStartups": 3}"#);
    assert!(home.run(&["setup", "--only", "claude-code", "--yes"]).status.success());
    let first = fs::read(&config).unwrap();
    let out = home.run(&["setup", "--only", "claude-code", "--yes"]);
    assert!(out.status.success());
    assert!(text(&out).contains("already set up"), "{}", text(&out));
    assert_eq!(first, fs::read(&config).unwrap());
}

#[test]
fn codex_toml_block_added_and_removed() {
    let home = Home::new();
    let original = "model = \"o4\"\n\n[profiles.fast]\nmodel = \"mini\"\n";
    let config = home.file(".codex/config.toml", original);
    let out = home.run(&["setup", "--only", "codex", "--yes"]);
    assert!(out.status.success(), "{}", text(&out));
    let added = fs::read_to_string(&config).unwrap();
    assert!(added.starts_with(original));
    assert!(added.contains("[mcp_servers.opcodex]"));
    assert!(added.contains("args = [\"mcp\"]"));

    assert!(home.run(&["setup", "--uninstall", "--only", "codex", "--yes"]).status.success());
    assert_eq!(original, fs::read_to_string(&config).unwrap());
}

#[test]
fn codex_toml_existing_unmanaged_opcodex_table_is_reported_not_changed() {
    let home = Home::new();
    let original = "[mcp_servers.opcodex]\ncommand = \"elsewhere\"\n";
    let config = home.file(".codex/config.toml", original);
    let out = home.run(&["setup", "--only", "codex", "--yes"]);
    assert_eq!(Some(1), out.status.code());
    assert!(text(&out).contains("already has an opcodex server"), "{}", text(&out));
    assert_eq!(original, fs::read_to_string(&config).unwrap());
}

#[test]
fn uninstall_removes_only_our_entry_and_keeps_backup() {
    let home = Home::new();
    let original = r#"{"mcpServers": {"keep": {"command": "k"}}}"#;
    let config = home.file(".codeium/windsurf/mcp_config.json", original);
    assert!(home.run(&["setup", "--only", "windsurf", "--yes"]).status.success());
    let backup = PathBuf::from(format!("{}.opcodex-backup", config.display()));
    assert_eq!(original, fs::read_to_string(&backup).unwrap());
    assert!(home.run(&["setup", "--uninstall", "--only", "windsurf", "--yes"]).status.success());
    assert_eq!(vec!["keep"], keys(&read_json(&config)["mcpServers"]));
    assert!(backup.exists());
}

#[test]
fn dry_run_changes_nothing() {
    let home = Home::new();
    let config = home.file(".cursor/mcp.json", "{}");
    let out = home.run(&["setup", "--dry-run"]);
    assert!(out.status.success(), "{}", text(&out));
    assert!(text(&out).contains("Cursor"), "{}", text(&out));
    assert_eq!("{}", fs::read_to_string(&config).unwrap());
}

#[test]
fn non_tty_without_yes_refuses() {
    let home = Home::new();
    let config = home.file(".cursor/mcp.json", "{}");
    let out = home.run(&["setup"]);
    assert_eq!(Some(1), out.status.code());
    assert!(text(&out).contains("--yes"), "{}", text(&out));
    assert_eq!("{}", fs::read_to_string(&config).unwrap());
}

#[test]
fn missing_apps_are_skipped() {
    let home = Home::new();
    let out = home.run(&["setup", "--yes"]);
    assert!(out.status.success(), "{}", text(&out));
    assert!(text(&out).contains("not installed"), "{}", text(&out));
}

#[test]
fn unknown_target_is_rejected() {
    let home = Home::new();
    let out = home.run(&["setup", "--only", "notepad"]);
    assert_eq!(Some(1), out.status.code());
    assert!(text(&out).contains("claude-code"), "{}", text(&out));
}

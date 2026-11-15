// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

use anyhow::{anyhow, bail, Context, Result};
use serde_json::{json, Value};
use std::fs::{self, OpenOptions};
use std::io::{self, IsTerminal, Write};
use std::path::{Path, PathBuf};

const USAGE: &str = "Usage: opcodex setup [--only ID,ID] [--yes] [--dry-run] [--uninstall]";
const OPEN: &str = "# >>> opcodex setup";
const CLOSE: &str = "# <<< opcodex setup";

struct Target {
    id: &'static str,
    label: &'static str,
    path: PathBuf,
    detected_by: PathBuf,
}

fn folder(variable: &str, fallback: fn() -> Option<PathBuf>) -> Result<PathBuf> {
    std::env::var_os(variable)
        .filter(|value| !value.is_empty())
        .map(PathBuf::from)
        .or_else(fallback)
        .ok_or_else(|| anyhow!("Can't find the configuration folder. Set {variable}."))
}

fn targets() -> Result<[Target; 7]> {
    let home = folder("OPCODEX_SETUP_HOME", dirs::home_dir)?;
    let appdata = folder("OPCODEX_SETUP_APPDATA", dirs::config_dir)?;
    let target = |id, label, base: &Path, relative: &str, detected_by: &str| Target {
        id,
        label,
        path: base.join(relative),
        detected_by: base.join(detected_by),
    };
    Ok([
        target(
            "claude-code",
            "Claude Code",
            &home,
            ".claude.json",
            ".claude",
        ),
        target(
            "claude-desktop",
            "Claude Desktop",
            &appdata,
            "Claude/claude_desktop_config.json",
            "Claude",
        ),
        target("cursor", "Cursor", &home, ".cursor/mcp.json", ".cursor"),
        target(
            "vscode",
            "VS Code",
            &appdata,
            "Code/User/mcp.json",
            "Code/User",
        ),
        target(
            "windsurf",
            "Windsurf",
            &home,
            ".codeium/windsurf/mcp_config.json",
            ".codeium/windsurf",
        ),
        target(
            "gemini",
            "Gemini CLI",
            &home,
            ".gemini/settings.json",
            ".gemini",
        ),
        target("codex", "Codex CLI", &home, ".codex/config.toml", ".codex"),
    ])
}

fn read(path: &Path) -> Result<Option<String>> {
    match fs::read_to_string(path) {
        Ok(text) => Ok(Some(text)),
        Err(error) if error.kind() == io::ErrorKind::NotFound => Ok(None),
        Err(error) => Err(error).with_context(|| format!("Can't read {}", path.display())),
    }
}

pub fn registrations() -> Result<Vec<Value>> {
    let executable = std::env::current_exe()?;
    Ok(targets()?
        .iter()
        .map(|target| {
            let installed = target.installed();
            let mut result = json!({"id": target.id, "installed": installed,
            "registered": false, "pointsToThisExecutable": false});
            if installed {
                let entry = (|| -> Result<Option<String>> {
                    let Some(text) = read(&target.path)? else {
                        return Ok(None);
                    };
                    if target.id == "codex" {
                        let root = text
                            .parse::<toml::Table>()
                            .map_err(|_| anyhow!("Invalid TOML"))?;
                        return Ok(root
                            .get("mcp_servers")
                            .and_then(|servers| servers.get("opcodex"))
                            .map(|server| {
                                server
                                    .get("command")
                                    .and_then(toml::Value::as_str)
                                    .unwrap_or_default()
                                    .to_string()
                            }));
                    }
                    let root = json_config(&text)?;
                    Ok(root[target.json_key()]
                        .get("opcodex")
                        .map(|server| server["command"].as_str().unwrap_or_default().to_string()))
                })();
                match entry {
                    Ok(Some(command)) => {
                        result["registered"] = json!(true);
                        result["pointsToThisExecutable"] = json!(Path::new(&command) == executable);
                    }
                    Ok(None) => {}
                    Err(error) => result["configError"] = json!(error.to_string()),
                }
            }
            result
        })
        .collect())
}

fn backup_path(path: &Path) -> PathBuf {
    let mut backup = path.as_os_str().to_owned();
    backup.push(".opcodex-backup");
    backup.into()
}

enum Edit {
    Change(String),
    Skip(&'static str),
}

impl Target {
    fn installed(&self) -> bool {
        self.path.is_file() || self.detected_by.is_dir()
    }

    fn json_entry(&self, command: &str) -> Value {
        let mut entry = json!({"command": command, "args": ["mcp"]});
        if self.id == "vscode" {
            entry["type"] = json!("stdio");
        }
        entry
    }

    fn json_key(&self) -> &'static str {
        if self.id == "vscode" {
            "servers"
        } else {
            "mcpServers"
        }
    }

    fn edit(&self, original: &str, command: &str, uninstall: bool) -> Result<Edit> {
        if self.id == "codex" {
            return edit_toml(original, command, uninstall);
        }
        let mut config = json_config(original)?;
        let key = self.json_key();
        let entry = self.json_entry(command);
        let servers = config
            .as_object_mut()
            .unwrap()
            .entry(key)
            .or_insert_with(|| json!({}));
        let servers = servers
            .as_object_mut()
            .ok_or_else(|| anyhow!("{key} must be a JSON object"))?;
        if let Some(existing) = servers.get("opcodex") {
            let same_entry = existing.as_object().is_some_and(|map| {
                map.len() == entry.as_object().unwrap().len()
                    && map.iter().all(|(key, value)| entry.get(key) == Some(value))
            });
            if same_entry {
                if !uninstall {
                    return Ok(Edit::Skip("already set up"));
                }
                let backup = read(&backup_path(&self.path))?;
                if let Some(backup) = backup {
                    if json_config(&backup)?[key].get("opcodex").is_none() {
                        servers.shift_remove("opcodex");
                        return Ok(Edit::Change(format_json(original, &config)?));
                    }
                }
            }
            bail!("already has an opcodex server configured outside this setup");
        }
        if uninstall {
            return Ok(Edit::Skip("no Opcodex entry"));
        }
        servers.insert("opcodex".into(), entry);
        Ok(Edit::Change(format_json(original, &config)?))
    }
}

fn json_config(text: &str) -> Result<Value> {
    let value: Value = serde_json::from_str(text.trim_start_matches('\u{feff}'))
        .map_err(|error| anyhow!("isn't valid JSON ({error})"))?;
    if !value.is_object() {
        bail!("must contain a JSON object");
    }
    Ok(value)
}

fn format_json(original: &str, value: &Value) -> Result<String> {
    let bom = if original.starts_with('\u{feff}') {
        "\u{feff}"
    } else {
        ""
    };
    Ok(format!("{bom}{}\n", serde_json::to_string_pretty(value)?))
}

fn toml_entry(command: &str) -> toml::Value {
    let mut server = toml::Table::new();
    server.insert("command".into(), toml::Value::String(command.into()));
    server.insert(
        "args".into(),
        toml::Value::Array(vec![toml::Value::String("mcp".into())]),
    );
    toml::Value::Table(server)
}

fn edit_toml(original: &str, command: &str, uninstall: bool) -> Result<Edit> {
    let config = original
        .parse::<toml::Table>()
        .map_err(|_| anyhow!("isn't valid TOML"))?;
    let mut marker_lines = original
        .split_inclusive('\n')
        .scan(0, |offset, line| {
            let start = *offset;
            *offset += line.len();
            Some((start, *offset, line.trim_end_matches(['\r', '\n'])))
        })
        .filter(|(_, _, line)| *line == OPEN || *line == CLOSE);
    let entry = toml_entry(command);
    let existing = config
        .get("mcp_servers")
        .and_then(|servers| servers.get("opcodex"));
    if let Some((start, block_start, marker)) = marker_lines.next() {
        let Some((block_end, end, closing)) = marker_lines.next() else {
            bail!("contains an incomplete Opcodex setup block");
        };
        if marker != OPEN || closing != CLOSE || marker_lines.next().is_some() {
            bail!("contains conflicting Opcodex setup markers");
        }
        let expected = format!(
            "[mcp_servers.opcodex]\n{}",
            toml::to_string(entry.as_table().unwrap())?
        );
        if existing != Some(&entry)
            || original[block_start..block_end].replace("\r\n", "\n") != expected
        {
            bail!("already has an opcodex server with a modified setup block");
        }
        if !uninstall {
            return Ok(Edit::Skip("already set up"));
        }
        let before = original[..start]
            .strip_suffix('\n')
            .unwrap_or(&original[..start]);
        return Ok(Edit::Change(format!("{before}{}", &original[end..])));
    }
    if existing.is_some() {
        bail!("already has an opcodex server configured outside this setup");
    }
    if uninstall {
        return Ok(Edit::Skip("no Opcodex entry"));
    }
    let definition = toml::to_string(entry.as_table().unwrap())?;
    let updated = format!("{original}\n{OPEN}\n[mcp_servers.opcodex]\n{definition}{CLOSE}\n");
    updated
        .parse::<toml::Table>()
        .map_err(|_| anyhow!("can't add an Opcodex server to the existing TOML structure"))?;
    Ok(Edit::Change(updated))
}

fn private_file(path: &Path) -> io::Result<fs::File> {
    let mut options = OpenOptions::new();
    options.write(true).create_new(true);
    #[cfg(unix)]
    {
        use std::os::unix::fs::OpenOptionsExt;
        options.mode(0o600);
    }
    options.open(path)
}

fn write_config(path: &Path, previous: Option<&str>, updated: &str, fallback: &str) -> Result<()> {
    let temp = path.with_file_name(format!(".opcodex-{}.tmp", crate::client::new_token()?));
    let mut file = private_file(&temp)?;
    let result = (|| -> Result<()> {
        file.write_all(updated.as_bytes())?;
        if let Ok(metadata) = fs::metadata(path) {
            file.set_permissions(metadata.permissions())?;
        }
        file.sync_all()?;
        drop(file);

        if read(path)?.as_deref() != previous {
            bail!(
                "{} changed since the plan was shown. Run setup again.",
                path.display()
            );
        }
        let backup = backup_path(path);
        match private_file(&backup) {
            Ok(mut saved) => {
                let result = saved
                    .write_all(previous.unwrap_or(fallback).as_bytes())
                    .and_then(|()| saved.sync_all());
                drop(saved);
                if let Err(error) = result {
                    let _ = fs::remove_file(&backup);
                    return Err(error).context("Couldn't save the configuration backup");
                }
            }
            Err(error) if error.kind() == io::ErrorKind::AlreadyExists => {}
            Err(error) => return Err(error).context("Couldn't create the configuration backup"),
        }
        if read(path)?.as_deref() != previous {
            bail!(
                "{} changed while saving the backup. Run setup again.",
                path.display()
            );
        }
        fs::rename(&temp, path).with_context(|| format!("Couldn't replace {}", path.display()))
    })();
    if result.is_err() {
        let _ = fs::remove_file(&temp);
    }
    result
}

pub fn run(args: &[String]) -> Result<()> {
    let (mut yes, mut dry_run, mut uninstall) = (false, false, false);
    let mut only = Vec::new();
    let mut args = args.iter();
    while let Some(flag) = args.next() {
        match flag.as_str() {
            "--help" | "-h" => {
                println!("{USAGE}");
                return Ok(());
            }
            "--yes" => yes = true,
            "--dry-run" => dry_run = true,
            "--uninstall" => uninstall = true,
            "--only" => only.extend(
                args.next()
                    .context("--only needs a list of agent IDs")?
                    .split(','),
            ),
            _ => bail!("Unknown setup option {flag}. {USAGE}"),
        }
    }
    let targets = targets()?;
    if let Some(id) = only
        .iter()
        .find(|id| !targets.iter().any(|target| target.id == **id))
    {
        let ids = targets.iter().map(|target| target.id).collect::<Vec<_>>();
        bail!("Unknown agent {id}. Choose from {}.", ids.join(", "));
    }
    if !yes && !dry_run && !io::stdin().is_terminal() {
        bail!("Setup needs --yes when stdin is not a terminal. Use --dry-run to preview.");
    }
    let exe = std::env::current_exe()?;
    let command = exe
        .to_str()
        .context("The Opcodex path must be valid UTF-8")?;
    let mut changes = Vec::new();
    let operation = if uninstall {
        "remove Opcodex from"
    } else {
        "register Opcodex in"
    };
    for target in targets
        .iter()
        .filter(|target| only.is_empty() || only.contains(&target.id))
    {
        if !target.installed() {
            println!("{}: not installed", target.label);
            continue;
        }
        let old = read(&target.path)?;
        let fallback = if target.id == "codex" { "" } else { "{}" };
        match target
            .edit(old.as_deref().unwrap_or(fallback), command, uninstall)
            .with_context(|| format!("{} ({})", target.label, target.path.display()))?
        {
            Edit::Skip(reason) => println!("{}: {reason}", target.label),
            Edit::Change(updated) => {
                println!("{}: {operation} {}", target.label, target.path.display());
                changes.push((target, old, updated, fallback));
            }
        }
    }
    if dry_run || changes.is_empty() {
        return Ok(());
    }
    if !yes {
        print!("Apply these changes? [y/N] ");
        io::stdout().flush()?;
        let mut answer = String::new();
        io::stdin().read_line(&mut answer)?;
        if !matches!(answer.trim().to_ascii_lowercase().as_str(), "y" | "yes") {
            return Ok(());
        }
    }
    for (target, old, updated, fallback) in changes {
        write_config(&target.path, old.as_deref(), &updated, fallback)?;
        println!("{}: updated", target.label);
    }
    Ok(())
}

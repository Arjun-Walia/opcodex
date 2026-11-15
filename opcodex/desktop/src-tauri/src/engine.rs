// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Starts the engine host with a private desktop credential and relays JSON-RPC calls to it.
//! The credential exists only in this process and in the engine's memory.

use serde_json::{json, Value};
use std::io::{BufRead, BufReader, Write};
use std::net::{SocketAddr, TcpStream};
use std::path::{Path, PathBuf};
use std::process::{Child, Command, Stdio};
use std::time::Duration;

const ENGINE_MAIN: &str = "io.opcodex.engine.EngineMain";
const UTILITY_JAR: &str = "Ghidra/Framework/Utility/lib/Utility.jar";

pub struct Engine {
    child: Child,
    port: u16,
    token: String,
}

impl Drop for Engine {
    fn drop(&mut self) {
        let _ = self.child.kill();
        let _ = self.child.wait();
    }
}

pub fn new_token() -> Result<String, String> {
    let mut bytes = [0u8; 32];
    getrandom::fill(&mut bytes).map_err(|e| format!("no secure random source: {e}"))?;
    Ok(bytes.iter().map(|b| format!("{b:02x}")).collect())
}

/// Splits a command line on spaces, keeping double-quoted parts together.
pub fn split_command(line: &str) -> Vec<String> {
    let mut parts = Vec::new();
    let mut current = String::new();
    let mut quoted = false;
    for c in line.chars() {
        match c {
            '"' => quoted = !quoted,
            ' ' if !quoted => {
                if !current.is_empty() {
                    parts.push(std::mem::take(&mut current));
                }
            }
            _ => current.push(c),
        }
    }
    if !current.is_empty() {
        parts.push(current);
    }
    parts
}

fn java_file() -> &'static str {
    if cfg!(windows) { "java.exe" } else { "java" }
}

/// How to start the engine: OPCODEX_ENGINE_CMD, or the runtime and engine installed next to
/// this executable (`<root>/runtime` and `<root>/app`).
pub fn resolve(exe: &Path, env_command: Option<String>) -> Result<Vec<String>, String> {
    if let Some(line) = env_command.filter(|l| !l.trim().is_empty()) {
        return Ok(split_command(&line));
    }
    let root = exe.parent().unwrap_or(Path::new("."));
    let java = root.join("runtime").join("bin").join(java_file());
    let jar = root.join("app").join(UTILITY_JAR);
    if java.is_file() && jar.is_file() {
        return Ok(vec![
            java.to_string_lossy().into_owned(),
            "-Djava.system.class.loader=ghidra.GhidraClassLoader".into(),
            "-Djava.awt.headless=true".into(),
            "-Dfile.encoding=UTF8".into(),
            "-Xshare:off".into(),
            "-cp".into(),
            jar.to_string_lossy().into_owned(),
            "ghidra.Ghidra".into(),
            ENGINE_MAIN.into(),
        ]);
    }
    Err("The Opcodex engine wasn't found next to this app. Reinstall Opcodex.".into())
}

fn hide_console(command: &mut Command) {
    #[cfg(windows)]
    {
        use std::os::windows::process::CommandExt;
        const CREATE_NO_WINDOW: u32 = 0x0800_0000;
        command.creation_flags(CREATE_NO_WINDOW);
    }
    let _ = command;
}

impl Engine {
    pub fn start(parts: &[String], log: Option<PathBuf>) -> Result<Engine, String> {
        let token = new_token()?;
        let stderr = match log.and_then(|p| std::fs::File::create(p).ok()) {
            Some(file) => Stdio::from(file),
            None => Stdio::null(),
        };
        let mut command = Command::new(&parts[0]);
        command
            .args(&parts[1..])
            .arg("--tcp")
            .arg("--desktop-token-stdin")
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .stderr(stderr);
        hide_console(&mut command);
        let mut child = command.spawn().map_err(|e| format!("Couldn't start the engine: {e}"))?;
        let line = json!({"credential": token, "kind": "DESKTOP"}).to_string() + "\n";
        child
            .stdin
            .take()
            .ok_or("engine stdin unavailable")?
            .write_all(line.as_bytes())
            .map_err(|e| format!("Couldn't hand the engine its credential: {e}"))?;
        let mut ready = String::new();
        BufReader::new(child.stdout.take().ok_or("engine stdout unavailable")?)
            .read_line(&mut ready)
            .map_err(|e| format!("The engine didn't start: {e}"))?;
        let port = serde_json::from_str::<Value>(&ready)
            .ok()
            .and_then(|v| v["port"].as_u64())
            .ok_or_else(|| "The engine didn't report that it was ready.".to_string())?;
        Ok(Engine { child, port: port as u16, token })
    }

    pub fn is_running(&mut self) -> bool {
        matches!(self.child.try_wait(), Ok(None))
    }

    /// Sends one request and returns the engine's whole JSON-RPC reply.
    pub fn call(&self, method: &str, params: &Value) -> Result<Value, std::io::Error> {
        let address = SocketAddr::from(([127, 0, 0, 1], self.port));
        let mut stream = TcpStream::connect_timeout(&address, Duration::from_secs(3))?;
        let request = json!({"jsonrpc": "2.0", "id": 1, "method": method, "params": params, "auth": self.token});
        stream.write_all((request.to_string() + "\n").as_bytes())?;
        let mut reply = String::new();
        BufReader::new(stream).read_line(&mut reply)?;
        serde_json::from_str(&reply).map_err(|e| std::io::Error::new(std::io::ErrorKind::InvalidData, e))
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn env_command_is_split_with_quotes() {
        let parts = resolve(Path::new("C:/x/Opcodex.exe"), Some("\"C:/Program Files/j/java\" @a Main".into())).unwrap();
        assert_eq!(vec!["C:/Program Files/j/java", "@a", "Main"], parts);
    }

    #[test]
    fn installed_layout_uses_bundled_runtime() {
        let mut bytes = [0u8; 6];
        getrandom::fill(&mut bytes).unwrap();
        let root = std::env::temp_dir().join(format!("opcodex-shell-{}", bytes.iter().map(|b| format!("{b:02x}")).collect::<String>()));
        std::fs::create_dir_all(root.join("runtime/bin")).unwrap();
        std::fs::write(root.join("runtime/bin").join(java_file()), b"").unwrap();
        std::fs::create_dir_all(root.join("app/Ghidra/Framework/Utility/lib")).unwrap();
        std::fs::write(root.join("app").join(UTILITY_JAR), b"").unwrap();
        let parts = resolve(&root.join("Opcodex.exe"), None).unwrap();
        assert!(parts[0].ends_with(java_file()));
        assert_eq!(ENGINE_MAIN, parts.last().unwrap());
    }

    #[test]
    fn missing_engine_is_explained() {
        let error = resolve(Path::new("C:/nowhere/Opcodex.exe"), None).unwrap_err();
        assert!(error.contains("Reinstall"));
    }

    #[test]
    fn tokens_are_long_and_random() {
        let a = new_token().unwrap();
        assert_eq!(64, a.len());
        assert_ne!(a, new_token().unwrap());
    }
}

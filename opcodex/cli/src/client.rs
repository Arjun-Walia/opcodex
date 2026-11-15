// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Connects to a running engine host (TCP, found through engine.json) or starts one over stdio.

use anyhow::{anyhow, bail, Context, Result};
use serde_json::{json, Value};
use std::io::{BufRead, BufReader, Write};
use std::net::TcpStream;
use std::path::PathBuf;
use std::process::{Child, ChildStdin, Command, Stdio};
use std::time::Duration;

/// A domain error returned by the engine.
#[derive(Debug)]
pub struct EngineError {
    pub code: String,
    pub message: String,
    pub data: Value,
}

impl std::fmt::Display for EngineError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}: {}", self.code, self.message)
    }
}

impl std::error::Error for EngineError {}

enum Link {
    Tcp { reader: BufReader<TcpStream>, writer: TcpStream },
    Stdio { child: Child, stdin: Option<ChildStdin>, reader: BufReader<std::process::ChildStdout> },
}

pub struct Client {
    link: Link,
    token: String,
    actor: String,
    next_id: u64,
}

pub fn session_dir() -> PathBuf {
    if let Some(dir) = std::env::var_os("OPCODEX_SESSION_DIR").filter(|d| !d.is_empty()) {
        return PathBuf::from(dir);
    }
    if cfg!(windows) {
        let base = std::env::var_os("LOCALAPPDATA").map(PathBuf::from).unwrap_or_else(|| {
            PathBuf::from(std::env::var_os("USERPROFILE").unwrap_or_default()).join("AppData").join("Local")
        });
        return base.join("Opcodex").join("engine");
    }
    if let Some(runtime) = std::env::var_os("XDG_RUNTIME_DIR").filter(|d| !d.is_empty()) {
        return PathBuf::from(runtime).join("opcodex");
    }
    PathBuf::from(std::env::var_os("HOME").unwrap_or_default()).join(".local/state/opcodex/engine")
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

pub fn new_token() -> Result<String> {
    let mut bytes = [0u8; 32];
    getrandom::fill(&mut bytes).map_err(|e| anyhow!("no secure random source: {e}"))?;
    Ok(bytes.iter().map(|b| format!("{b:02x}")).collect())
}

const ENGINE_MAIN: &str = "io.opcodex.engine.EngineMain";
const UTILITY_JAR: &str = "Ghidra/Framework/Utility/lib/Utility.jar";

fn java_file() -> &'static str {
    if cfg!(windows) { "java.exe" } else { "java" }
}

/// The command that starts an engine host for this executable and environment.
pub fn engine_command() -> Result<Vec<String>> {
    let exe = std::env::current_exe().context("can't find the opcodex executable")?;
    resolve_engine_from(
        &exe,
        std::env::var("OPCODEX_ENGINE_CMD").ok(),
        std::env::var_os("JAVA_HOME").filter(|h| !h.is_empty()).map(PathBuf::from),
        std::env::var_os("PATH"),
    )
}

/// Finds how to start the engine: OPCODEX_ENGINE_CMD, then an installed Opcodex next to this
/// executable (bundled runtime), then a portable distribution (JAVA_HOME or java on PATH).
pub fn resolve_engine_from(
    exe: &std::path::Path,
    env_command: Option<String>,
    java_home: Option<PathBuf>,
    path: Option<std::ffi::OsString>,
) -> Result<Vec<String>> {
    if let Some(line) = env_command.filter(|l| !l.trim().is_empty()) {
        return Ok(split_command(&line));
    }
    let dir = exe.parent().map(PathBuf::from).unwrap_or_default();
    // Installed: the CLI is in <root>/app/support on Windows, with runtime in <root>/runtime.
    let mut roots = vec![dir.clone()];
    if let Some(root) = dir.parent().and_then(|app| app.parent()) {
        roots.push(root.to_path_buf());
    }
    for root in roots {
        let java = root.join("runtime").join("bin").join(java_file());
        let jar = root.join("app").join(UTILITY_JAR);
        if java.is_file() && jar.is_file() {
            return Ok(launch(&java, &jar));
        }
    }
    // Portable: <dist>/support/opcodex(.exe) with <dist>/Ghidra/...
    if let Some(dist) = dir.parent() {
        let jar = dist.join(UTILITY_JAR);
        if jar.is_file() {
            let java = java_home
                .map(|home| home.join("bin").join(java_file()))
                .filter(|java| java.is_file())
                .or_else(|| {
                    path.as_ref().and_then(|p| {
                        std::env::split_paths(p).map(|d| d.join(java_file())).find(|j| j.is_file())
                    })
                });
            let Some(java) = java else {
                bail!(
                    "This Opcodex distribution needs Java 21 or later. Set JAVA_HOME or put java \
                     on PATH."
                );
            };
            return Ok(launch(&java, &jar));
        }
    }
    bail!(
        "No Opcodex engine is running and none was found next to this opcodex executable. Start \
         the Opcodex desktop or `opcodex serve`, reinstall Opcodex, or set OPCODEX_ENGINE_CMD to \
         the engine command."
    )
}

fn launch(java: &std::path::Path, utility_jar: &std::path::Path) -> Vec<String> {
    vec![
        java.to_string_lossy().to_string(),
        "-Djava.system.class.loader=ghidra.GhidraClassLoader".into(),
        "-Djava.awt.headless=true".into(),
        "-Dfile.encoding=UTF8".into(),
        "-Xshare:off".into(),
        "-cp".into(),
        utility_jar.to_string_lossy().to_string(),
        "ghidra.Ghidra".into(),
        ENGINE_MAIN.into(),
    ]
}

impl Client {
    pub fn is_shared(&self) -> bool {
        matches!(self.link, Link::Tcp { .. })
    }

    /// Uses a running host if one answers; otherwise starts a private one over stdio.
    pub fn connect(actor: &str) -> Result<Client> {
        if let Some(client) = Self::connect_running(actor)? {
            return Ok(client);
        }
        Self::spawn_stdio(actor)
    }

    fn connect_running(actor: &str) -> Result<Option<Client>> {
        let dir = session_dir();
        let Ok(info) = std::fs::read_to_string(dir.join("engine.json")) else {
            return Ok(None);
        };
        let info: Value = serde_json::from_str(&info).context("engine.json is damaged")?;
        let Some(port) = info["port"].as_u64() else {
            return Ok(None);
        };
        let address = std::net::SocketAddr::from(([127, 0, 0, 1], port as u16));
        let Ok(stream) = TcpStream::connect_timeout(&address, Duration::from_secs(2)) else {
            return Ok(None); // Stale session file from an engine that has stopped.
        };
        let token = std::fs::read_to_string(dir.join("client.token"))
            .context("the engine's client.token can't be read")?
            .trim()
            .to_string();
        let writer = stream.try_clone()?;
        Ok(Some(Client {
            link: Link::Tcp { reader: BufReader::new(stream), writer },
            token,
            actor: actor.to_string(),
            next_id: 1,
        }))
    }

    fn spawn_stdio(actor: &str) -> Result<Client> {
        let parts = engine_command()?;
        let token = new_token()?;
        let mut child = Command::new(&parts[0])
            .args(&parts[1..])
            .arg("--stdio")
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn()
            .with_context(|| format!("couldn't start the engine with {}", parts[0]))?;
        let mut stdin = child.stdin.take().context("engine stdin")?;
        let credential = json!({"credential": token, "kind": "CLIENT"}).to_string() + "\n";
        stdin.write_all(credential.as_bytes())?;
        stdin.flush()?;
        let reader = BufReader::new(child.stdout.take().context("engine stdout")?);
        Ok(Client { link: Link::Stdio { child, stdin: Some(stdin), reader }, token, actor: actor.to_string(), next_id: 1 })
    }

    pub fn set_actor(&mut self, actor: &str) {
        self.actor = actor.to_string();
    }

    /// Calls one operation and returns its result or the engine's error.
    pub fn call(&mut self, method: &str, params: Value) -> Result<Value> {
        let id = self.next_id;
        self.next_id += 1;
        let request = json!({"jsonrpc": "2.0", "id": id, "method": method, "params": params,
            "auth": self.token, "actor": self.actor});
        let line = request.to_string() + "\n";
        match &mut self.link {
            Link::Tcp { writer, .. } => writer.write_all(line.as_bytes())?,
            Link::Stdio { stdin, .. } => {
                let stdin = stdin.as_mut().context("the engine connection is closed")?;
                stdin.write_all(line.as_bytes())?;
                stdin.flush()?;
            }
        }
        loop {
            let mut text = String::new();
            let read = match &mut self.link {
                Link::Tcp { reader, .. } => reader.read_line(&mut text)?,
                Link::Stdio { reader, .. } => reader.read_line(&mut text)?,
            };
            if read == 0 {
                bail!("The engine closed the connection.");
            }
            let response: Value = serde_json::from_str(&text).context("the engine sent invalid JSON")?;
            if response.get("id") != Some(&json!(id)) {
                continue; // An event or a response to something else.
            }
            if let Some(error) = response.get("error") {
                let code = error["data"]["code"]
                    .as_str()
                    .map(str::to_string)
                    .unwrap_or_else(|| error["code"].to_string());
                return Err(EngineError {
                    code,
                    message: error["message"].as_str().unwrap_or("").to_string(),
                    data: error["data"].clone(),
                }
                .into());
            }
            return Ok(response["result"].clone());
        }
    }
}

impl Drop for Client {
    fn drop(&mut self) {
        if let Link::Stdio { child, stdin, .. } = &mut self.link {
            // Closing stdin stops a private engine; give it a moment to save, then make sure.
            drop(stdin.take());
            for _ in 0..50 {
                if let Ok(Some(_)) = child.try_wait() {
                    return;
                }
                std::thread::sleep(Duration::from_millis(100));
            }
            let _ = child.kill();
        }
    }
}

#[cfg(test)]
#[path = "client_tests.rs"]
mod tests;

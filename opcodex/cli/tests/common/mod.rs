// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Starts a real engine host for end-to-end tests. Tests skip themselves when
//! OPCODEX_ENGINE_CMD is not set (see opcodex/cli/README.md).
#![allow(dead_code)]

use serde_json::{json, Value};
use std::io::{BufRead, BufReader, Write};
use std::net::TcpStream;
use std::path::PathBuf;
use std::process::{Child, Command, Stdio};

pub const DESKTOP_TOKEN: &str = "e2e-desktop-token-000000000000000000000";

pub struct Engine {
    child: Child,
    pub session_dir: PathBuf,
    pub projects_dir: PathBuf,
    port: u16,
    next_id: u64,
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

pub fn engine_cmd() -> Option<String> {
    std::env::var("OPCODEX_ENGINE_CMD").ok().filter(|s| !s.trim().is_empty())
}

fn temp_dir(prefix: &str) -> PathBuf {
    let mut bytes = [0u8; 8];
    getrandom::fill(&mut bytes).unwrap();
    let name: String = bytes.iter().map(|b| format!("{b:02x}")).collect();
    let dir = std::env::temp_dir().join(format!("{prefix}-{name}"));
    std::fs::create_dir_all(&dir).unwrap();
    dir
}

impl Engine {
    /// Starts `OPCODEX_ENGINE_CMD --tcp --desktop-token-stdin` with a private session dir.
    pub fn start() -> Option<Engine> {
        let cmd = engine_cmd()?;
        let parts = split_command(&cmd);
        let session_dir = temp_dir("opcodex-e2e-session");
        let projects_dir = temp_dir("opcodex-e2e-projects");
        let mut child = Command::new(&parts[0])
            .args(&parts[1..])
            .arg("--tcp")
            .arg("--desktop-token-stdin")
            .env("OPCODEX_SESSION_DIR", &session_dir)
            .stdin(Stdio::piped())
            .stdout(Stdio::piped())
            .stderr(Stdio::null())
            .spawn()
            .expect("start engine");
        let line = json!({"credential": DESKTOP_TOKEN, "kind": "DESKTOP"}).to_string() + "\n";
        child.stdin.as_mut().unwrap().write_all(line.as_bytes()).unwrap();
        let mut ready = String::new();
        BufReader::new(child.stdout.take().unwrap()).read_line(&mut ready).unwrap();
        let ready: Value = serde_json::from_str(&ready).expect("ready line");
        let port = ready["port"].as_u64().unwrap() as u16;
        Some(Engine { child, session_dir, projects_dir, port, next_id: 1 })
    }

    /// Calls the engine as the desktop and returns the result, panicking on errors.
    pub fn desktop(&mut self, method: &str, params: Value) -> Value {
        let mut stream = TcpStream::connect(("127.0.0.1", self.port)).unwrap();
        let id = self.next_id;
        self.next_id += 1;
        let request = json!({"jsonrpc": "2.0", "id": id, "method": method,
            "params": params, "auth": DESKTOP_TOKEN});
        stream.write_all((request.to_string() + "\n").as_bytes()).unwrap();
        let mut line = String::new();
        BufReader::new(stream).read_line(&mut line).unwrap();
        let response: Value = serde_json::from_str(&line).unwrap();
        assert!(response.get("error").is_none(), "engine error: {response}");
        response["result"].clone()
    }

    /// A project with writes allowed for clients.
    pub fn project_allowing_writes(&mut self, name: &str) {
        let dir = self.projects_dir.to_string_lossy().to_string();
        self.desktop("project.create", json!({"dir": dir, "name": name}));
        self.desktop("policy.set", json!({"mode": "allow-writes"}));
    }
}

impl Drop for Engine {
    fn drop(&mut self) {
        let _ = self.child.kill();
        let _ = self.child.wait();
        let _ = std::fs::remove_dir_all(&self.session_dir);
    }
}

/// A minimal x86-64 ELF executable whose entry point is
/// `push rbp; mov rbp,rsp; mov eax,42; pop rbp; ret`.
pub fn return42_elf() -> Vec<u8> {
    let base: u64 = 0x400000;
    let code: [u8; 11] = [0x55, 0x48, 0x89, 0xE5, 0xB8, 0x2A, 0, 0, 0, 0x5D, 0xC3];
    let header_len: u64 = 64 + 56;
    let total = header_len + code.len() as u64;
    let mut elf = Vec::new();
    elf.extend_from_slice(&[0x7f, b'E', b'L', b'F', 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0]);
    elf.extend_from_slice(&2u16.to_le_bytes()); // ET_EXEC
    elf.extend_from_slice(&0x3eu16.to_le_bytes()); // x86-64
    elf.extend_from_slice(&1u32.to_le_bytes());
    elf.extend_from_slice(&(base + header_len).to_le_bytes()); // entry
    elf.extend_from_slice(&64u64.to_le_bytes()); // phoff
    elf.extend_from_slice(&0u64.to_le_bytes()); // shoff
    elf.extend_from_slice(&0u32.to_le_bytes()); // flags
    elf.extend_from_slice(&64u16.to_le_bytes()); // ehsize
    elf.extend_from_slice(&56u16.to_le_bytes()); // phentsize
    elf.extend_from_slice(&1u16.to_le_bytes()); // phnum
    elf.extend_from_slice(&64u16.to_le_bytes()); // shentsize
    elf.extend_from_slice(&0u16.to_le_bytes()); // shnum
    elf.extend_from_slice(&0u16.to_le_bytes()); // shstrndx
    elf.extend_from_slice(&1u32.to_le_bytes()); // PT_LOAD
    elf.extend_from_slice(&5u32.to_le_bytes()); // R+X
    elf.extend_from_slice(&0u64.to_le_bytes()); // offset
    elf.extend_from_slice(&base.to_le_bytes()); // vaddr
    elf.extend_from_slice(&base.to_le_bytes()); // paddr
    elf.extend_from_slice(&total.to_le_bytes()); // filesz
    elf.extend_from_slice(&total.to_le_bytes()); // memsz
    elf.extend_from_slice(&0x1000u64.to_le_bytes()); // align
    elf.extend_from_slice(&code);
    assert_eq!(elf.len() as u64, total);
    elf
}

/// Writes the ELF to a temp file and returns its absolute path.
pub fn write_return42_elf() -> PathBuf {
    let path = temp_dir("opcodex-e2e-bin").join("ret42.elf");
    std::fs::write(&path, return42_elf()).unwrap();
    path
}

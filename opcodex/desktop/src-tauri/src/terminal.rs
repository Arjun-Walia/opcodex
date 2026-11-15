// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Real terminals on a pseudo-terminal, streamed to the interface as events.

use portable_pty::{native_pty_system, Child, CommandBuilder, MasterPty, PtySize};
use std::collections::HashMap;
use std::io::{Read, Write};
use std::path::PathBuf;
use std::sync::atomic::{AtomicU32, Ordering};
use std::sync::Mutex;
use tauri::{AppHandle, Emitter};

pub struct Terminal {
    master: Box<dyn MasterPty + Send>,
    writer: Box<dyn Write + Send>,
    child: Box<dyn Child + Send + Sync>,
}

#[derive(Default)]
pub struct Terminals {
    next: AtomicU32,
    open: Mutex<HashMap<u32, Terminal>>,
}

/// Splits a byte stream into valid UTF-8, keeping an incomplete trailing character for later.
pub fn take_utf8(pending: &mut Vec<u8>) -> String {
    match std::str::from_utf8(pending) {
        Ok(text) => {
            let out = text.to_string();
            pending.clear();
            out
        }
        Err(e) => {
            let valid = e.valid_up_to();
            if e.error_len().is_some() {
                // Invalid bytes, not just an unfinished character: replace and move on.
                let out = String::from_utf8_lossy(pending).into_owned();
                pending.clear();
                return out;
            }
            let out = String::from_utf8_lossy(&pending[..valid]).into_owned();
            pending.drain(..valid);
            out
        }
    }
}

fn default_shell() -> String {
    if cfg!(windows) {
        let pwsh = which("pwsh.exe");
        pwsh.unwrap_or_else(|| "powershell.exe".into())
    } else {
        std::env::var("SHELL").unwrap_or_else(|_| "/bin/bash".into())
    }
}

fn which(name: &str) -> Option<String> {
    let path = std::env::var_os("PATH")?;
    std::env::split_paths(&path).map(|d| d.join(name)).find(|p| p.is_file()).map(|p| p.to_string_lossy().into_owned())
}

impl Terminals {
    /// Opens a shell in `cwd` with `extra_path` first on PATH, optionally typing `command`.
    pub fn open(
        &self,
        app: AppHandle,
        cols: u16,
        rows: u16,
        cwd: Option<PathBuf>,
        extra_path: Option<PathBuf>,
        command: Option<String>,
    ) -> Result<u32, String> {
        let pair = native_pty_system()
            .openpty(PtySize { rows: rows.max(2), cols: cols.max(10), pixel_width: 0, pixel_height: 0 })
            .map_err(|e| format!("Couldn't open a terminal: {e}"))?;
        let mut cmd = CommandBuilder::new(default_shell());
        if let Some(dir) = cwd.or_else(dirs::home_dir) {
            cmd.cwd(dir);
        }
        if let Some(dir) = extra_path {
            let current = std::env::var_os("PATH").unwrap_or_default();
            let joined = std::env::join_paths(std::iter::once(dir).chain(std::env::split_paths(&current)))
                .map_err(|e| e.to_string())?;
            cmd.env("PATH", joined);
        }
        cmd.env("TERM", "xterm-256color");
        cmd.env("OPCODEX_TERMINAL", "1");
        let child = pair.slave.spawn_command(cmd).map_err(|e| format!("Couldn't start the shell: {e}"))?;
        drop(pair.slave);
        let mut reader = pair.master.try_clone_reader().map_err(|e| e.to_string())?;
        let mut writer = pair.master.take_writer().map_err(|e| e.to_string())?;
        if let Some(line) = command.filter(|c| !c.trim().is_empty()) {
            let _ = writer.write_all(format!("{line}\r").as_bytes());
        }
        let id = self.next.fetch_add(1, Ordering::SeqCst) + 1;
        std::thread::spawn(move || {
            let mut buffer = [0u8; 8192];
            let mut pending = Vec::new();
            loop {
                match reader.read(&mut buffer) {
                    Ok(0) | Err(_) => break,
                    Ok(n) => {
                        pending.extend_from_slice(&buffer[..n]);
                        let text = take_utf8(&mut pending);
                        if !text.is_empty() {
                            let _ = app.emit(&format!("terminal-data-{id}"), text);
                        }
                    }
                }
            }
            let _ = app.emit(&format!("terminal-exit-{id}"), ());
        });
        self.open.lock().map_err(|e| e.to_string())?.insert(id, Terminal { master: pair.master, writer, child });
        Ok(id)
    }

    pub fn write(&self, id: u32, data: &str) -> Result<(), String> {
        let mut open = self.open.lock().map_err(|e| e.to_string())?;
        let term = open.get_mut(&id).ok_or("That terminal is closed.")?;
        term.writer.write_all(data.as_bytes()).map_err(|e| e.to_string())?;
        term.writer.flush().map_err(|e| e.to_string())
    }

    pub fn resize(&self, id: u32, cols: u16, rows: u16) -> Result<(), String> {
        let open = self.open.lock().map_err(|e| e.to_string())?;
        let term = open.get(&id).ok_or("That terminal is closed.")?;
        term.master
            .resize(PtySize { rows: rows.max(2), cols: cols.max(10), pixel_width: 0, pixel_height: 0 })
            .map_err(|e| e.to_string())
    }

    pub fn close(&self, id: u32) {
        if let Ok(mut open) = self.open.lock() {
            if let Some(mut term) = open.remove(&id) {
                let _ = term.child.kill();
            }
        }
    }

    pub fn close_all(&self) {
        if let Ok(mut open) = self.open.lock() {
            for (_, mut term) in open.drain() {
                let _ = term.child.kill();
            }
        }
    }
}

#[cfg(test)]
mod tests {
    use super::take_utf8;

    #[test]
    fn keeps_a_split_character_for_the_next_chunk() {
        let euro = "€".as_bytes(); // 3 bytes
        let mut pending = vec![b'a', euro[0], euro[1]];
        assert_eq!("a", take_utf8(&mut pending));
        assert_eq!(2, pending.len());
        pending.push(euro[2]);
        assert_eq!("€", take_utf8(&mut pending));
        assert!(pending.is_empty());
    }

    #[test]
    fn replaces_invalid_bytes_instead_of_stalling() {
        let mut pending = vec![b'o', b'k', 0xff, b'!'];
        assert_eq!("ok\u{fffd}!", take_utf8(&mut pending));
        assert!(pending.is_empty());
    }
}

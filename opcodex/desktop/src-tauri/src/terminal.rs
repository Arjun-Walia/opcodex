// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Real terminals on a pseudo-terminal, streamed to the interface as events.

use portable_pty::{native_pty_system, Child, CommandBuilder, MasterPty, PtySize};
use std::collections::HashMap;
use std::io::{Read, Write};
use std::path::PathBuf;
use std::sync::atomic::{AtomicU32, Ordering};
use std::sync::{Arc, Mutex};
use tauri::{AppHandle, Emitter};

pub struct Terminal {
    master: Box<dyn MasterPty + Send>,
    writer: Box<dyn Write + Send>,
    child: Box<dyn Child + Send + Sync>,
    gate: Arc<Gate>,
}

/// What a terminal sends to the interface.
#[derive(Debug, PartialEq)]
pub enum Output {
    Data(String),
    Exit,
}

/// Holds a new shell's output until the interface is listening, then passes it on in order.
/// Without this, the banner and prompt printed before the interface subscribed were lost.
#[derive(Default)]
pub struct Gate {
    state: Mutex<GateState>,
}

#[derive(Default)]
struct GateState {
    ready: bool,
    held: String,
    exited: bool,
}

impl Gate {
    /// Passes output on, or holds it until `open`.
    pub fn send(&self, output: Output, deliver: &dyn Fn(Output)) {
        let Ok(mut state) = self.state.lock() else { return };
        if state.ready {
            deliver(output);
            return;
        }
        match output {
            Output::Data(text) => state.held.push_str(&text),
            Output::Exit => state.exited = true,
        }
    }

    /// The interface is listening: deliver what was held, then everything as it comes.
    pub fn open(&self, deliver: &dyn Fn(Output)) {
        let Ok(mut state) = self.state.lock() else { return };
        if state.ready {
            return;
        }
        state.ready = true;
        if !state.held.is_empty() {
            deliver(Output::Data(std::mem::take(&mut state.held)));
        }
        if state.exited {
            deliver(Output::Exit);
        }
    }
}

fn deliverer(app: AppHandle, id: u32) -> impl Fn(Output) {
    move |output| match output {
        Output::Data(text) => {
            let _ = app.emit(&format!("terminal-data-{id}"), text);
        }
        Output::Exit => {
            let _ = app.emit(&format!("terminal-exit-{id}"), ());
        }
    }
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
        let gate = Arc::new(Gate::default());
        let thread_gate = gate.clone();
        let deliver = deliverer(app, id);
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
                            thread_gate.send(Output::Data(text), &deliver);
                        }
                    }
                }
            }
            thread_gate.send(Output::Exit, &deliver);
        });
        self.open.lock().map_err(|e| e.to_string())?.insert(id, Terminal { master: pair.master, writer, child, gate });
        Ok(id)
    }

    /// Called once the interface listens for this terminal's output.
    pub fn ready(&self, app: AppHandle, id: u32) -> Result<(), String> {
        let gate = {
            let open = self.open.lock().map_err(|e| e.to_string())?;
            open.get(&id).ok_or("That terminal is closed.")?.gate.clone()
        };
        gate.open(&deliverer(app, id));
        Ok(())
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
mod gate_tests {
    use super::{Gate, Output};
    use std::sync::Mutex;

    #[test]
    fn output_before_the_interface_listens_is_held_then_delivered_in_order() {
        let got = Mutex::new(Vec::new());
        let deliver = |o: Output| got.lock().unwrap().push(o);
        let gate = Gate::default();
        gate.send(Output::Data("Windows PowerShell\r\n".into()), &deliver);
        gate.send(Output::Data("PS C:\\> ".into()), &deliver);
        assert!(got.lock().unwrap().is_empty());
        gate.open(&deliver);
        gate.send(Output::Data("dir".into()), &deliver);
        assert_eq!(
            *got.lock().unwrap(),
            vec![Output::Data("Windows PowerShell\r\nPS C:\\> ".into()), Output::Data("dir".into())]
        );
    }

    #[test]
    fn an_early_exit_is_not_lost() {
        let got = Mutex::new(Vec::new());
        let deliver = |o: Output| got.lock().unwrap().push(o);
        let gate = Gate::default();
        gate.send(Output::Data("bye".into()), &deliver);
        gate.send(Output::Exit, &deliver);
        gate.open(&deliver);
        gate.open(&deliver); // a second call changes nothing
        assert_eq!(*got.lock().unwrap(), vec![Output::Data("bye".into()), Output::Exit]);
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

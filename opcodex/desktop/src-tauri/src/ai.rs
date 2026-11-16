// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Model providers for the agent chat. API keys live in the OS credential store (Windows Credential
//! Manager) and never reach the web view: the interface sends a request body, this module adds the
//! key and sends it to a fixed provider address, and streams the response back as events.

use futures_util::StreamExt;
use serde_json::{json, Value};
use std::collections::HashMap;
use std::sync::Mutex;
use tauri::{AppHandle, Emitter};

const SERVICE: &str = "Opcodex";
const MAX_KEY_LEN: usize = 400;
const MAX_BODY_BYTES: usize = 4 * 1024 * 1024;

#[derive(Clone, Copy, Debug, PartialEq)]
pub enum Provider {
    Anthropic,
    OpenAi,
    Local,
}

impl Provider {
    pub fn parse(name: &str) -> Result<Provider, String> {
        match name {
            "anthropic" => Ok(Provider::Anthropic),
            "openai" => Ok(Provider::OpenAi),
            "local" => Ok(Provider::Local),
            _ => Err(format!("Unknown model provider {name}.")),
        }
    }
    fn account(self) -> &'static str {
        match self {
            Provider::Anthropic => "ai:anthropic",
            Provider::OpenAi => "ai:openai",
            Provider::Local => "ai:local",
        }
    }
}

/// Checks the shape of a key without saying anything about its value.
pub fn validate_key(key: &str) -> Result<&str, String> {
    let key = key.trim();
    if key.is_empty() {
        return Err("Paste a key first.".into());
    }
    if key.len() > MAX_KEY_LEN || key.chars().any(|c| c.is_whitespace() || c.is_control()) {
        return Err("That doesn't look like an API key.".into());
    }
    Ok(key)
}

fn entry(provider: Provider) -> Result<keyring::Entry, String> {
    keyring::Entry::new(SERVICE, provider.account()).map_err(|e| format!("Credential store unavailable: {e}"))
}

pub fn save_key(provider: Provider, key: &str) -> Result<(), String> {
    if provider == Provider::Local {
        return Err("Local models don't need a key.".into());
    }
    let key = validate_key(key)?;
    entry(provider)?.set_password(key).map_err(|e| format!("Couldn't save the key: {e}"))
}

pub fn remove_key(provider: Provider) -> Result<(), String> {
    match entry(provider)?.delete_credential() {
        Ok(()) | Err(keyring::Error::NoEntry) => Ok(()),
        Err(e) => Err(format!("Couldn't remove the key: {e}")),
    }
}

fn read_key(provider: Provider) -> Result<Option<String>, String> {
    match entry(provider)?.get_password() {
        Ok(k) => Ok(Some(k)),
        Err(keyring::Error::NoEntry) => Ok(None),
        Err(e) => Err(format!("Couldn't read the key: {e}")),
    }
}

/// Which providers have a key saved. Never returns the keys.
pub fn key_status() -> Value {
    let has = |p| read_key(p).ok().flatten().is_some();
    json!({ "anthropic": has(Provider::Anthropic), "openai": has(Provider::OpenAi) })
}

/// Only addresses on this computer are allowed for local models.
pub fn local_base(base: &str) -> Result<String, String> {
    let trimmed = base.trim().trim_end_matches('/');
    let rest = trimmed
        .strip_prefix("http://")
        .ok_or("A local model address starts with http:// (for example http://127.0.0.1:11434/v1).")?;
    let host = rest.split(['/', ':']).next().unwrap_or("");
    let host = if rest.starts_with('[') { rest.split(']').next().unwrap_or("").trim_start_matches('[') } else { host };
    if !matches!(host, "127.0.0.1" | "localhost" | "::1") {
        return Err("Local models must run on this computer (127.0.0.1 or localhost).".into());
    }
    Ok(trimmed.to_string())
}

/// The fixed address for a provider request.
pub fn endpoint(provider: Provider, local: Option<&str>) -> Result<String, String> {
    Ok(match provider {
        Provider::Anthropic => "https://api.anthropic.com/v1/messages".into(),
        Provider::OpenAi => "https://api.openai.com/v1/chat/completions".into(),
        Provider::Local => format!("{}/chat/completions", local_base(local.unwrap_or("http://127.0.0.1:11434/v1"))?),
    })
}

#[derive(Default)]
pub struct Requests {
    running: Mutex<HashMap<String, tauri::async_runtime::JoinHandle<()>>>,
}

impl Requests {
    pub fn cancel(&self, id: &str) {
        if let Some(handle) = self.running.lock().ok().and_then(|mut m| m.remove(id)) {
            handle.abort();
        }
    }
}

fn emit(app: &AppHandle, event: &str, payload: Value) {
    let _ = app.emit(event, payload);
}

/// Starts a streaming request; data arrives as `ai:data`, then `ai:done` or `ai:error`.
pub fn start(app: AppHandle, requests: &Requests, provider: Provider, local: Option<String>, body: Value, id: String)
    -> Result<(), String> {
    let url = endpoint(provider, local.as_deref())?;
    let text = body.to_string();
    if text.len() > MAX_BODY_BYTES {
        return Err("The conversation is too long to send. Start a new chat.".into());
    }
    let key = match provider {
        Provider::Local => None,
        p => Some(read_key(p)?.ok_or("Add an API key for this provider in Settings → AI models.")?),
    };
    let task_id = id.clone();
    let handle = tauri::async_runtime::spawn(async move {
        let client = reqwest::Client::new();
        let mut request = client.post(&url).header("content-type", "application/json").body(text);
        request = match (provider, key) {
            (Provider::Anthropic, Some(k)) => request.header("x-api-key", k).header("anthropic-version", "2023-06-01"),
            (Provider::OpenAi, Some(k)) => request.header("authorization", format!("Bearer {k}")),
            (_, _) => request,
        };
        match request.send().await {
            Err(e) => emit(&app, "ai:error", json!({"id": task_id, "message": friendly(&e)})),
            Ok(response) => {
                let status = response.status();
                if !status.is_success() {
                    let detail = response.text().await.unwrap_or_default();
                    let detail: String = detail.chars().take(2000).collect();
                    emit(&app, "ai:error", json!({"id": task_id, "status": status.as_u16(), "message": detail}));
                    return;
                }
                let mut stream = response.bytes_stream();
                while let Some(chunk) = stream.next().await {
                    match chunk {
                        Ok(bytes) => emit(&app, "ai:data", json!({"id": task_id, "chunk": String::from_utf8_lossy(&bytes)})),
                        Err(e) => {
                            emit(&app, "ai:error", json!({"id": task_id, "message": friendly(&e)}));
                            return;
                        }
                    }
                }
                emit(&app, "ai:done", json!({"id": task_id}));
            }
        }
    });
    if let Ok(mut running) = requests.running.lock() {
        running.retain(|_, h| !h.inner().is_finished());
        running.insert(id, handle);
    }
    Ok(())
}

fn friendly(e: &reqwest::Error) -> String {
    if e.is_connect() {
        "Couldn't reach the model. Check your connection, or that the local model server is running.".into()
    } else if e.is_timeout() {
        "The model took too long to answer.".into()
    } else {
        format!("The request failed: {e}")
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn keys_must_look_like_keys() {
        assert!(validate_key("  sk-ant-abc123  ").is_ok());
        assert!(validate_key("").is_err());
        assert!(validate_key("two words").is_err());
        assert!(validate_key(&"x".repeat(MAX_KEY_LEN + 1)).is_err());
    }

    #[test]
    fn local_models_stay_on_this_computer() {
        assert_eq!(local_base("http://127.0.0.1:11434/v1/").unwrap(), "http://127.0.0.1:11434/v1");
        assert!(local_base("http://localhost:1234/v1").is_ok());
        assert!(local_base("http://[::1]:8080/v1").is_ok());
        assert!(local_base("http://192.168.1.5:11434/v1").is_err());
        assert!(local_base("http://evil.example/v1").is_err());
        assert!(local_base("http://127.0.0.1.evil.example/v1").is_err());
        assert!(local_base("https://api.openai.com/v1").is_err());
        assert!(local_base("file:///etc/passwd").is_err());
    }

    #[test]
    fn cloud_providers_use_fixed_addresses() {
        assert_eq!(endpoint(Provider::Anthropic, Some("http://127.0.0.1")).unwrap(), "https://api.anthropic.com/v1/messages");
        assert_eq!(endpoint(Provider::OpenAi, None).unwrap(), "https://api.openai.com/v1/chat/completions");
        assert_eq!(endpoint(Provider::Local, None).unwrap(), "http://127.0.0.1:11434/v1/chat/completions");
        assert!(Provider::parse("anthropic").is_ok());
        assert!(Provider::parse("https://evil").is_err());
    }
}

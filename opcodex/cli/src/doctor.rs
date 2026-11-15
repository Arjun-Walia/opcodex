// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

use crate::{client, format::Format, setup};
use anyhow::{bail, Result};
use serde_json::{json, Value};

pub fn run(args: &[String], default_format: Format) -> Result<()> {
    let format = match args {
        [] => default_format,
        [help] if matches!(help.as_str(), "-h" | "--help") => {
            println!("Usage: opcodex doctor [--format json|text]");
            return Ok(());
        }
        [flag, value] if flag == "--format" => Format::parse(value)?,
        _ => bail!("Usage: opcodex doctor [--format json|text]"),
    };
    if !matches!(format, Format::Json | Format::Text) {
        bail!("Doctor supports JSON and text output.");
    }

    let executable = std::env::current_exe()?.display().to_string();
    let engine = client::engine_command();
    let connected = client::Client::connect("cli").and_then(|mut connection| {
        let info = connection.call("engine.info", json!({}))?;
        Ok((connection.is_shared(), info))
    });
    let agents = setup::registrations();
    let engine_detail = match &engine {
        Ok(command) => command
            .first()
            .map_or("No executable found", String::as_str)
            .to_string(),
        Err(error) => error.to_string(),
    };
    let connection_detail = match &connected {
        Ok((_, info)) => format!(
            "Engine {}",
            info["engineVersion"].as_str().unwrap_or("unknown")
        ),
        Err(error) => error.to_string(),
    };
    let checks = [
        json!({"name": "executable", "ok": true, "detail": executable,
            "version": env!("CARGO_PKG_VERSION")}),
        json!({"name": "engine", "ok": engine.is_ok(), "detail": engine_detail}),
        json!({"name": "connect", "ok": connected.is_ok(), "detail": connection_detail}),
        json!({"name": "session", "ok": connected.is_ok(), "detail": client::session_dir(),
            "shared": connected.as_ref().is_ok_and(|(shared, _)| *shared)}),
        match agents {
            Ok(targets) => json!({"name": "agents", "ok": true, "detail": targets}),
            Err(error) => json!({"name": "agents", "ok": false, "detail": error.to_string()}),
        },
    ];
    match format {
        Format::Json => println!("{}", serde_json::to_string_pretty(&checks)?),
        Format::Text => {
            for check in checks {
                let status = if check["ok"] == true { "ok" } else { "check" };
                let detail = check.get("detail").unwrap_or(&Value::Null);
                println!(
                    "{:<11} {status:<6} {}",
                    check["name"].as_str().unwrap_or_default(),
                    detail
                        .as_str()
                        .map(str::to_owned)
                        .unwrap_or_else(|| detail.to_string())
                );
            }
        }
        _ => unreachable!(),
    }
    if engine.is_err() || connected.is_err() {
        bail!("Doctor found a problem with the engine.");
    }
    Ok(())
}

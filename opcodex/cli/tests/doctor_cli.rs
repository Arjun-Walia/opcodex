// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

use serde_json::Value;
use std::process::Command;

fn doctor(engine: Option<String>) -> (Option<i32>, Value) {
    let session = std::env::temp_dir().join(format!("opcodex-doctor-{}", std::process::id()));
    let mut command = Command::new(env!("CARGO_BIN_EXE_opcodex"));
    command
        .args(["doctor", "--format", "json"])
        .env("OPCODEX_SETUP_HOME", &session)
        .env("OPCODEX_SETUP_APPDATA", &session)
        .env("OPCODEX_SESSION_DIR", &session);
    if let Some(engine) = engine {
        command.env("OPCODEX_ENGINE_CMD", engine);
    } else {
        command.env_remove("OPCODEX_ENGINE_CMD");
    }
    let output = command.output().unwrap();
    let checks = serde_json::from_slice(&output.stdout)
        .unwrap_or_else(|_| panic!("{}", String::from_utf8_lossy(&output.stderr)));
    (output.status.code(), checks)
}

#[test]
fn doctor_reports_missing_engine() {
    let (code, checks) = doctor(None);
    assert_eq!(Some(1), code);
    assert_eq!(
        false,
        checks
            .as_array()
            .unwrap()
            .iter()
            .find(|check| check["name"] == "engine")
            .unwrap()["ok"]
    );
}

#[test]
fn doctor_connects_to_engine() {
    let Ok(engine) = std::env::var("OPCODEX_ENGINE_CMD") else {
        return;
    };
    let (code, checks) = doctor(Some(engine));
    assert_eq!(Some(0), code, "{checks}");
    assert_eq!(
        true,
        checks
            .as_array()
            .unwrap()
            .iter()
            .find(|check| check["name"] == "connect")
            .unwrap()["ok"]
    );
}

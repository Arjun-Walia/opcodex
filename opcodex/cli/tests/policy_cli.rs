// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
use std::process::{Command, Stdio};

fn run(args: &[&str]) -> std::process::Output {
    Command::new(env!("CARGO_BIN_EXE_opcodex"))
        .args(args)
        .env("OPCODEX_SESSION_DIR", std::env::temp_dir().join("opcodex-policy-cli-none"))
        .env_remove("OPCODEX_ENGINE_CMD")
        .stdin(Stdio::piped())
        .output()
        .unwrap()
}

#[test]
fn policy_set_refuses_without_tty() {
    let out = run(&["policy", "set", "allow-writes"]);
    assert_eq!(Some(1), out.status.code());
    let stderr = String::from_utf8_lossy(&out.stderr);
    assert!(stderr.contains("terminal"), "{stderr}");
}

#[test]
fn policy_set_rejects_unknown_mode() {
    let out = run(&["policy", "set", "everything"]);
    assert_eq!(Some(1), out.status.code());
    assert!(String::from_utf8_lossy(&out.stderr).contains("allow-writes"));
}

#[test]
fn hidden_policy_operations_are_not_subcommands() {
    for op in ["policy.requestChange", "policy.confirmChange"] {
        let out = run(&[op, "--mode", "allow-all"]);
        assert_eq!(Some(1), out.status.code(), "{op}");
        assert!(String::from_utf8_lossy(&out.stderr).contains("opcodex policy set"), "{op}");
    }
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
use super::*;
use serde_json::json;

fn rename_op() -> Value {
    json!({
        "name": "symbol.rename",
        "description": "Renames.",
        "class": "write",
        "params": {
            "type": "object",
            "properties": {
                "programId": {"type": "string"},
                "expectedRevision": {"type": "integer"},
                "dryRun": {"type": "boolean"},
                "newName": {"type": "string"}
            },
            "required": ["programId", "expectedRevision", "newName"]
        }
    })
}

fn args(list: &[&str]) -> Vec<String> {
    list.iter().map(|s| s.to_string()).collect()
}

#[test]
fn params_typed_from_schema() {
    let params = parse_params(
        &rename_op(),
        &args(&["--program-id", "p1", "--expected-revision", "42", "--dry-run", "--new-name", "007"]),
    )
    .unwrap();
    assert_eq!(params["programId"], json!("p1"));
    assert_eq!(params["expectedRevision"], json!(42));
    assert_eq!(params["dryRun"], json!(true));
    assert_eq!(params["newName"], json!("007"));
}

#[test]
fn unknown_param_is_rejected_with_the_valid_names() {
    let err = parse_params(&rename_op(), &args(&["--colour", "red"])).unwrap_err();
    let text = err.to_string();
    assert!(text.contains("--colour"), "{text}");
    assert!(text.contains("--program-id"), "{text}");
}

#[test]
fn integer_param_must_be_a_number() {
    assert!(parse_params(&rename_op(), &args(&["--expected-revision", "ten"])).is_err());
}

#[test]
fn params_json_merges_raw_values() {
    let params = parse_params(
        &rename_op(),
        &args(&["--params-json", "{\"programId\":\"p\",\"newName\":\"x\"}", "--expected-revision", "1"]),
    )
    .unwrap();
    assert_eq!(params["programId"], json!("p"));
    assert_eq!(params["expectedRevision"], json!(1));
}

#[test]
fn kebab_and_camel_convert_both_ways() {
    assert_eq!(camel_from_kebab("expected-revision"), "expectedRevision");
    assert_eq!(kebab_from_camel("bytesBase64"), "bytes-base64");
}

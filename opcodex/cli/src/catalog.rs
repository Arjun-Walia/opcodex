// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Builds request parameters for a catalog operation from command-line flags.

use anyhow::{anyhow, bail, Result};
use serde_json::{Map, Value};

/// Finds an operation by name in the result of `catalog.list`.
pub fn find<'a>(catalog: &'a Value, name: &str) -> Option<&'a Value> {
    catalog.as_array()?.iter().find(|op| op["name"] == name)
}

pub fn camel_from_kebab(flag: &str) -> String {
    let mut out = String::new();
    let mut upper = false;
    for c in flag.chars() {
        if c == '-' {
            upper = true;
        } else if upper {
            out.extend(c.to_uppercase());
            upper = false;
        } else {
            out.push(c);
        }
    }
    out
}

pub fn kebab_from_camel(name: &str) -> String {
    let mut out = String::new();
    let mut previous_lower = false;
    for c in name.chars() {
        if c.is_ascii_uppercase() && previous_lower {
            out.push('-');
        }
        out.extend(c.to_lowercase());
        previous_lower = c.is_ascii_lowercase() || c.is_ascii_digit();
    }
    out
}

fn property_type<'a>(op: &'a Value, name: &str) -> Option<&'a str> {
    op["params"]["properties"].get(name).map(|p| p["type"].as_str().unwrap_or("any"))
}

fn valid_flags(op: &Value) -> String {
    op["params"]["properties"]
        .as_object()
        .map(|props| {
            props.keys().map(|k| format!("--{}", kebab_from_camel(k))).collect::<Vec<_>>().join(" ")
        })
        .unwrap_or_default()
}

/// Turns `--kebab-name value` pairs into a params object typed by the operation's schema.
/// `--params-json '{...}'` merges raw values first; later flags override them.
pub fn parse_params(op: &Value, args: &[String]) -> Result<Value> {
    let mut params = Map::new();
    let mut i = 0;
    while i < args.len() {
        let arg = &args[i];
        let Some(flag) = arg.strip_prefix("--") else {
            bail!("Unexpected argument {arg}. Parameters are given as --name value.");
        };
        if flag == "params-json" {
            let raw = args.get(i + 1).ok_or_else(|| anyhow!("--params-json needs a value"))?;
            let parsed: Value = serde_json::from_str(raw)
                .map_err(|e| anyhow!("--params-json isn't valid JSON: {e}"))?;
            let object = parsed.as_object().ok_or_else(|| anyhow!("--params-json must be an object"))?;
            params.extend(object.clone());
            i += 2;
            continue;
        }
        let name = camel_from_kebab(flag);
        let Some(kind) = property_type(op, &name) else {
            bail!(
                "{} has no parameter --{flag}. Valid parameters: {}",
                op["name"].as_str().unwrap_or("this operation"),
                valid_flags(op)
            );
        };
        let next = args.get(i + 1).filter(|v| !v.starts_with("--"));
        let value = match kind {
            "boolean" => match next.map(String::as_str) {
                Some("true") => {
                    i += 1;
                    Value::Bool(true)
                }
                Some("false") => {
                    i += 1;
                    Value::Bool(false)
                }
                _ => Value::Bool(true),
            },
            "integer" => {
                let raw = next.ok_or_else(|| anyhow!("--{flag} needs a number"))?;
                i += 1;
                let number: i64 = raw.parse().map_err(|_| anyhow!("--{flag} must be a whole number, not {raw}"))?;
                Value::from(number)
            }
            "object" | "array" => {
                let raw = next.ok_or_else(|| anyhow!("--{flag} needs a JSON value"))?;
                i += 1;
                serde_json::from_str(raw).map_err(|e| anyhow!("--{flag} isn't valid JSON: {e}"))?
            }
            _ => {
                let raw = args.get(i + 1).ok_or_else(|| anyhow!("--{flag} needs a value"))?;
                i += 1;
                Value::String(raw.clone())
            }
        };
        params.insert(name, value);
        i += 1;
    }
    Ok(Value::Object(params))
}

/// Takes `--format X`, `--output-format X` and `--json` out of an operation's arguments, so
/// output flags work after the operation name too. An operation with its own `format`
/// parameter keeps `--format`.
pub fn take_output_flags(op: &Value, args: &[String]) -> (Option<String>, Vec<String>) {
    let own_format = op["params"]["properties"].get("format").is_some();
    let mut format = None;
    let mut rest = Vec::new();
    let mut i = 0;
    while i < args.len() {
        match args[i].as_str() {
            "--json" => format = Some("json".to_string()),
            "--output-format" if i + 1 < args.len() => {
                format = Some(args[i + 1].clone());
                i += 1;
            }
            "--format" if !own_format && i + 1 < args.len() => {
                format = Some(args[i + 1].clone());
                i += 1;
            }
            _ => rest.push(args[i].clone()),
        }
        i += 1;
    }
    (format, rest)
}

/// Help text for one operation.
pub fn help(op: &Value) -> String {
    let mut out = format!(
        "{} ({})\n\n{}\n\nParameters:\n",
        op["name"].as_str().unwrap_or("?"),
        op["class"].as_str().unwrap_or("?"),
        op["description"].as_str().unwrap_or("")
    );
    let required: Vec<&str> = op["params"]["required"]
        .as_array()
        .map(|r| r.iter().filter_map(Value::as_str).collect())
        .unwrap_or_default();
    match op["params"]["properties"].as_object() {
        Some(props) if !props.is_empty() => {
            for (name, schema) in props {
                let marker = if required.contains(&name.as_str()) { " (required)" } else { "" };
                out.push_str(&format!(
                    "  --{:<22} {}{} {}\n",
                    kebab_from_camel(name),
                    schema["type"].as_str().unwrap_or("value"),
                    marker,
                    schema["description"].as_str().unwrap_or("")
                ));
            }
        }
        _ => out.push_str("  (none)\n"),
    }
    out
}

#[cfg(test)]
#[path = "catalog_tests.rs"]
mod tests;

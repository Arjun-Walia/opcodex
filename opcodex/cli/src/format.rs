// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
//! Renders results as json, jsonl, md, text or csv.

use anyhow::{bail, Result};
use serde_json::Value;

#[derive(Clone, Copy, Debug, PartialEq)]
pub enum Format {
    Json,
    Jsonl,
    Md,
    Text,
    Csv,
}

impl Format {
    pub fn parse(name: &str) -> Result<Format> {
        Ok(match name {
            "json" => Format::Json,
            "jsonl" => Format::Jsonl,
            "md" => Format::Md,
            "text" => Format::Text,
            "csv" => Format::Csv,
            other => bail!("Unknown format {other}. Use json, jsonl, md, text or csv."),
        })
    }
}

/// The rows of a result: its `items` array, the array itself, or the single value.
fn rows(value: &Value) -> Vec<&Value> {
    if let Some(items) = value.get("items").and_then(Value::as_array) {
        return items.iter().collect();
    }
    if let Some(array) = value.as_array() {
        return array.iter().collect();
    }
    vec![value]
}

fn columns(rows: &[&Value]) -> Vec<String> {
    let mut out: Vec<String> = Vec::new();
    for row in rows {
        if let Some(object) = row.as_object() {
            for key in object.keys() {
                if !out.contains(key) {
                    out.push(key.clone());
                }
            }
        }
    }
    out
}

fn cell(value: &Value) -> String {
    match value {
        Value::Null => String::new(),
        Value::String(s) => s.clone(),
        other => other.to_string(),
    }
}

fn csv_cell(value: &Value) -> String {
    let text = cell(value);
    if text.contains([',', '"', '\n']) {
        format!("\"{}\"", text.replace('"', "\"\""))
    } else {
        text
    }
}

pub fn render(value: &Value, format: Format) -> String {
    match format {
        Format::Json => serde_json::to_string_pretty(value).unwrap_or_default(),
        Format::Jsonl => rows(value).iter().map(|r| r.to_string()).collect::<Vec<_>>().join("\n"),
        Format::Text => text(value),
        Format::Md => table(value, " | ", true),
        Format::Csv => {
            let rows = rows(value);
            let columns = columns(&rows);
            let mut out = vec![columns.join(",")];
            for row in rows {
                out.push(columns.iter().map(|c| csv_cell(&row[c])).collect::<Vec<_>>().join(","));
            }
            out.join("\n")
        }
    }
}

fn text(value: &Value) -> String {
    // Decompiled code reads best as the code itself.
    if let Some(c) = value.get("c").and_then(Value::as_str) {
        return c.to_string();
    }
    if value.get("items").is_some() || value.is_array() {
        return table(value, "\t", false);
    }
    match value.as_object() {
        Some(object) => object.iter().map(|(k, v)| format!("{k}: {}", cell(v))).collect::<Vec<_>>().join("\n"),
        None => cell(value),
    }
}

fn table(value: &Value, separator: &str, markdown: bool) -> String {
    let rows = rows(value);
    let columns = columns(&rows);
    if columns.is_empty() {
        return rows.iter().map(|r| cell(r)).collect::<Vec<_>>().join("\n");
    }
    let mut out = Vec::new();
    let wrap = |line: String| if markdown { format!("| {line} |") } else { line };
    out.push(wrap(columns.join(separator)));
    if markdown {
        out.push(wrap(columns.iter().map(|_| "---").collect::<Vec<_>>().join(separator)));
    }
    for row in rows {
        let cells: Vec<String> = columns
            .iter()
            .map(|c| {
                let text = cell(&row[c]);
                if markdown { text.replace('|', "\\|") } else { text }
            })
            .collect();
        out.push(wrap(cells.join(separator)));
    }
    if let Some(next) = value.get("nextCursor").and_then(Value::as_str) {
        out.push(format!("(more: --cursor {next})"));
    }
    out.join("\n")
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;

    #[test]
    fn csv_quotes_cells_with_commas() {
        let value = json!({"items": [{"name": "a,b", "size": 2}]});
        assert_eq!(render(&value, Format::Csv), "name,size\n\"a,b\",2");
    }

    #[test]
    fn text_prints_decompiled_code_directly() {
        let value = json!({"entry": "1000", "c": "int f(void) { return 0x2a; }"});
        assert_eq!(render(&value, Format::Text), "int f(void) { return 0x2a; }");
    }

    #[test]
    fn markdown_table_shows_the_next_cursor() {
        let value = json!({"items": [{"entry": "1000"}], "nextCursor": "1000"});
        let md = render(&value, Format::Md);
        assert!(md.starts_with("| entry |\n| --- |\n| 1000 |"));
        assert!(md.ends_with("(more: --cursor 1000)"));
    }
}

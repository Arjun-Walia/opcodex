// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
use super::*;
use std::fs;
use std::path::Path;

fn touch(path: &Path) {
    fs::create_dir_all(path.parent().unwrap()).unwrap();
    fs::write(path, b"").unwrap();
}

fn temp_tree(name: &str) -> PathBuf {
    let mut bytes = [0u8; 6];
    getrandom::fill(&mut bytes).unwrap();
    let suffix: String = bytes.iter().map(|b| format!("{b:02x}")).collect();
    let dir = std::env::temp_dir().join(format!("opcodex-resolve-{name}-{suffix}"));
    fs::create_dir_all(&dir).unwrap();
    dir
}

fn java_name() -> &'static str {
    if cfg!(windows) { "java.exe" } else { "java" }
}

fn exe_name() -> &'static str {
    if cfg!(windows) { "opcodex.exe" } else { "opcodex" }
}

#[test]
fn installed_layout_resolves_bundled_runtime() {
    let root = temp_tree("installed");
    touch(&root.join("runtime").join("bin").join(java_name()));
    touch(&root.join("app/Ghidra/Framework/Utility/lib/Utility.jar"));
    let exe = root.join(exe_name());
    touch(&exe);

    let command = resolve_engine_from(&exe, None, None, None).unwrap();
    assert_eq!(root.join("runtime").join("bin").join(java_name()).to_string_lossy(), command[0]);
    assert!(command.contains(&"-Djava.system.class.loader=ghidra.GhidraClassLoader".to_string()));
    assert!(command.contains(&"-Djava.awt.headless=true".to_string()));
    let cp = command.iter().position(|a| a == "-cp").unwrap();
    assert!(command[cp + 1].ends_with("Utility.jar"));
    assert_eq!(["ghidra.Ghidra", "io.opcodex.engine.EngineMain"], command[command.len() - 2..]);
}

#[test]
fn installed_layout_also_works_from_app_support() {
    let root = temp_tree("installed-support");
    touch(&root.join("runtime").join("bin").join(java_name()));
    touch(&root.join("app/Ghidra/Framework/Utility/lib/Utility.jar"));
    let exe = root.join("app").join("support").join(exe_name());
    touch(&exe);
    let command = resolve_engine_from(&exe, None, None, None).unwrap();
    assert_eq!(root.join("runtime").join("bin").join(java_name()).to_string_lossy(), command[0]);
}

#[test]
fn portable_layout_uses_java_home() {
    let dist = temp_tree("portable");
    touch(&dist.join("Ghidra/Framework/Utility/lib/Utility.jar"));
    let exe = dist.join("support").join(exe_name());
    touch(&exe);
    let java_home = temp_tree("jdk");
    touch(&java_home.join("bin").join(java_name()));

    let command = resolve_engine_from(&exe, None, Some(java_home.clone()), None).unwrap();
    assert_eq!(java_home.join("bin").join(java_name()).to_string_lossy(), command[0]);
    let cp = command.iter().position(|a| a == "-cp").unwrap();
    assert_eq!(dist.join("Ghidra/Framework/Utility/lib/Utility.jar").to_string_lossy(), command[cp + 1]);
}

#[test]
fn portable_layout_falls_back_to_java_on_path() {
    let dist = temp_tree("portable-path");
    touch(&dist.join("Ghidra/Framework/Utility/lib/Utility.jar"));
    let exe = dist.join("support").join(exe_name());
    touch(&exe);
    let path_dir = temp_tree("pathdir");
    touch(&path_dir.join(java_name()));
    let command = resolve_engine_from(&exe, None, None, Some(path_dir.clone().into_os_string())).unwrap();
    assert_eq!(path_dir.join(java_name()).to_string_lossy(), command[0]);
}

#[test]
fn env_command_wins() {
    let root = temp_tree("env");
    let exe = root.join(exe_name());
    let command =
        resolve_engine_from(&exe, Some("\"C:/My Java/java\" @args Main".to_string()), None, None).unwrap();
    assert_eq!(vec!["C:/My Java/java", "@args", "Main"], command);
}

#[test]
fn nothing_found_explains() {
    let root = temp_tree("empty");
    let exe = root.join(exe_name());
    let error = resolve_engine_from(&exe, None, None, None).unwrap_err().to_string();
    assert!(error.contains("OPCODEX_ENGINE_CMD"), "{error}");
    assert!(error.contains("reinstall"), "{error}");
}

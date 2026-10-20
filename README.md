<!-- OPCODEX: replacement project README. Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

<p align="center"><picture><source media="(prefers-color-scheme: dark)" srcset="opcodex/branding/mark-dark-surface.svg"><img src="opcodex/branding/mark-light-surface.svg" width="112" alt="Opcodex ribbon mark"></picture></p>
<h1 align="center">Opcodex</h1>
<p align="center">A desktop workbench for reverse engineering.</p>
<p align="center"><a href="https://opcodex.xyz">Website</a> · <a href="https://github.com/Arjun-Walia/opcodex/releases">Releases</a> · <a href="https://github.com/Arjun-Walia/opcodex/issues">Issues</a> · <a href="docs/releases/RELEASING.md">Release process</a></p>

Opcodex builds on the stable Ghidra engine: disassembly, decompilation, program analysis, graph views, scripting and the existing processor and file-format ecosystem. The project adds its own desktop identity and is developing a more consistent interface while preserving engine behavior and extension compatibility.

**Status: v1 alpha development.** The first preview is `1.0.0-alpha.1`, based on **Ghidra 12.1.4**. The initial branding and release foundation does not include the full icon redesign, refined themes, AI assistance or Pro features. A download is available only when an actual platform ZIP is attached to a published Opcodex release; GitHub's automatic source archives are not installable applications.

Release promotion is currently blocked by the broader upstream regression checks; native build and Windows launch evidence is recorded in [verification](docs/releases/VERIFICATION.md). No final v1 release has been published.

## Download and run

1. Open [Opcodex Releases](https://github.com/Arjun-Walia/opcodex/releases) and select the package for your OS/architecture.
2. Install a **64-bit JDK 21** or newer supported JDK.
3. Verify the ZIP against its accompanying `.sha256` file.
4. Extract into a fresh directory.
5. Launch **`opcodexRun.bat`** on Windows, or **`./opcodexRun`** on Linux/macOS.

Package names identify both versions and the native platform:

```text
opcodex_1.0.0-alpha.1_ghidra12.1.4_win_x86_64.zip
opcodex_1.0.0-alpha.1_ghidra12.1.4_linux_x86_64.zip
opcodex_1.0.0-alpha.1_ghidra12.1.4_mac_arm_64.zip
```

These are naming examples, not a claim that all three builds have shipped. Check each release's assets and validation notes. See [release notes](docs/releases/1.0.0-alpha.1.md) and [verification](docs/releases/VERIFICATION.md) for current evidence and limitations.

## Build from source

The fork is pinned to [`Ghidra_12.1.4_build`](https://github.com/NationalSecurityAgency/ghidra/releases/tag/Ghidra_12.1.4_build), commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`. [upstream.json](opcodex/upstream.json) records this provenance. Opcodex's version is separate from the engine/API version used by extensions.

Requirements: JDK 21+, Python 3.9–3.14 with pip, and the checked-in Gradle wrapper. Linux/macOS also require GCC or Clang and make. Windows requires Visual Studio or C++ Build Tools with MSVC, the Windows SDK and C++ ATL. The selected stable tag's wrapper uses Gradle 9.7.1.

```bash
git clone --branch main https://github.com/Arjun-Walia/opcodex.git
cd opcodex
./gradlew -I gradle/support/fetchDependencies.gradle
./gradlew testOpcodex
./gradlew buildOpcodex
```

Use `.\gradlew.bat` in PowerShell. Override the packaging interpreter with `-PopcodexPython=/path/to/python` if needed. The package is written to **`build/releases/`**, with a checksum and JSON build manifest. Packaging requires committed tracked source so every artifact points to an identifiable revision. The underlying `buildGhidra` task remains available for engine-compatible development archives.

To prepare a source launch:

```bash
./gradlew prepdev buildNatives
./opcodexRun
```

Existing [developer documentation](DevGuide.md) explains upstream build internals. Java package names, module names, project formats, technical support commands and server protocols retain their upstream identifiers. They are compatibility interfaces. Opcodex settings use a separate application identity; existing Ghidra settings and saved layouts are not automatically overwritten.

## Opcodex v1

The v1 work proceeds in reviewable stages:

1. Stable engine baseline, product identity and release packaging.
2. Design system and toolbar/tree icon pilot for review.
3. Complete icon family and matched light/dark themes.
4. Layout defaults, discoverability, keyboard access and workflow polish.

Engine algorithms, decompiler implementation, loaders, processors and program storage remain upstream. Community is the open source foundation; private Pro extensions depend on Community. No license validation, paid feature logic or cloud AI integration is included in this repository's current implementation.

## Contributing and security

Open an issue with your platform, Opcodex version and engine version, or submit a focused change. Include checks and update [MODIFICATIONS.md](MODIFICATIONS.md) for upstream-file edits. Keep changes small and retain upstream notices. See [CONTRIBUTING.md](CONTRIBUTING.md) for the inherited engineering conventions.

For a vulnerability, use [GitHub's private reporting flow](https://github.com/Arjun-Walia/opcodex/security/advisories/new) where enabled; avoid public disclosure of sensitive details. The retained [security policy](SECURITY.md) and [upstream advisories](https://github.com/NationalSecurityAgency/ghidra/security/advisories) provide additional engine context. A stable baseline does not eliminate the need to follow advisories.

## License and attribution

Opcodex is an independent modified distribution of [Ghidra](https://github.com/NationalSecurityAgency/ghidra), created and maintained by the National Security Agency Research Directorate and its contributors. Opcodex is not endorsed by the NSA or the Ghidra project.

Original Opcodex additions are Apache-2.0. Retained components keep their existing licenses; bundled dependencies and GPL helper components have additional terms. The upstream **[LICENSE](LICENSE)**, **[NOTICE](NOTICE)**, copyright headers and dependency notices are preserved. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md), [licenses/](licenses/) and [MODIFICATIONS.md](MODIFICATIONS.md).

<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex

A desktop workbench for reverse engineering, built on Ghidra 12.1.4.

This is a **v1 alpha preview** of the branded stable engine. The planned icon redesign, refined themes and workflow improvements are being developed separately. Paid editions and AI integrations are not included in this build.

1. Install a 64-bit JDK 21 or newer supported JDK.
2. Verify the downloaded ZIP against its `.sha256` file.
3. Extract into a new directory; do not overlay an existing installation.
4. Run `opcodexRun.bat` on Windows or `./opcodexRun` on Linux/macOS.
5. Create a project, import a file and use the existing analysis tools.

`opcodex-release.json` records the product version, engine version, source revision, source-archive hash and native platform. `Ghidra/application.properties` retains the engine/API version for extension compatibility. Your settings live under the Opcodex application identity; existing Ghidra settings are not overwritten or automatically migrated.

Website: https://opcodex.xyz · Source/issues: https://github.com/Arjun-Walia/opcodex

Use `OPCODEX_MAXMEM` to override the JVM heap (for example `4G`), or `OPCODEX_JAVA_OPTIONS` for additional JVM options. The existing `GHIDRA_*` overrides are accepted for compatibility. Build tools, Node.js and the SVG renderer are not needed to run this distribution.

Upstream manuals remain in `docs/` and `GhidraDocs/` where included by the platform build. Technical package names, project formats, support commands and server protocols retain their upstream identifiers.

Opcodex is an independent modified distribution of Ghidra, created and maintained by the National Security Agency Research Directorate and its contributors. Opcodex is not endorsed by the NSA or the Ghidra project. See `LICENSE`, `NOTICE`, `licenses/`, `MODIFICATIONS.md` and `THIRD_PARTY_NOTICES.md` for terms, attribution and changes. Some bundled components have additional licenses.

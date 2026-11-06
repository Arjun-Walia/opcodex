<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex

A local desktop workbench for reverse engineering.

Verify this ZIP against its SHA-256 file, extract into a fresh directory and run `opcodexRun.bat` on Windows or `./opcodexRun` on Linux/macOS. Portable packages require a supported 64-bit JDK 21 or newer. The native Windows MSI and Linux Debian installers include their own Java runtimes.

Create or open a project, import a program and explore the listing, decompiler, symbols and graphs. New profiles use Opcodex Light; choose Light, Dark or Classic from Edit > Theme > Switch. Toolbars stay on one row; scroll controls reveal commands that do not fit. Hover a command for its name, description and shortcut. Existing keyboard shortcuts and configurable listing/decompiler fonts remain available.

The manifest `opcodex-release.json` records version, platform and source/component provenance. Product settings are separate from other applications' settings. The `OPCODEX_MAXMEM` and `OPCODEX_JAVA_OPTIONS` environment variables configure portable launches.

Website: https://opcodex.xyz · Source/issues: https://github.com/Arjun-Walia/opcodex

See RELEASE_NOTES.md for preview scope and validation limits. This is a development preview, not final v1. License terms, required attribution and modifications are included in LICENSE, NOTICE, licenses/, THIRD_PARTY_NOTICES.md and MODIFICATIONS.md.

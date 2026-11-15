<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Building Opcodex

Use JDK 21 or newer supported JDK, Python 3.9–3.14 and the checked-in Gradle wrapper. Windows also needs Visual Studio or C++ Build Tools with MSVC, the Windows SDK and ATL. Linux/macOS need GCC or Clang and make. The wrapper currently selects Gradle 9.7.1.

1. Fetch pinned dependencies: `./gradlew -I gradle/support/fetchDependencies.gradle`.
2. Run product checks: `./gradlew testOpcodex`.
3. Build native portable distribution: `./gradlew buildOpcodex`.
4. On Windows, build/install the native desktop app: `.\gradlew.bat installOpcodexDesktop`.
5. Read [verification](releases/VERIFICATION.md) for actual coverage and known regression limitations.

Use `.\gradlew.bat` in PowerShell. Release packaging requires committed source, includes a source manifest, and rejects stale or unbranded inputs. Node.js/Sharp is only needed when regenerating artwork; committed assets are sufficient for app builds.

For a source launch, run `./gradlew prepdev buildNatives`, then `./opcodexRun`. Portable packages accept `OPCODEX_MAXMEM` and `OPCODEX_JAVA_OPTIONS`. Desktop app installation bundles the build JDK's runtime and retains its legal files.

## Compatibility and provenance

Opcodex keeps the selected stable engine's APIs, module paths, project formats, support tools and protocols. Some source paths and technical interfaces retain upstream identifiers. These are compatibility details, not product branding. Do not globally rename them.

The exact engine baseline is recorded in [upstream.json](../opcodex/upstream.json) and [UPSTREAM.md](UPSTREAM.md); component attribution is in [third-party notices](../THIRD_PARTY_NOTICES.md). The product version is independent of the engine/API version used by extensions. The inherited [developer guide](../DevGuide.md) contains deeper technical build documentation.

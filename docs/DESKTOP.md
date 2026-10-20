<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Windows desktop app

With PowerShell 7 (`pwsh`) installed, run `.\gradlew.bat installOpcodexDesktop` from a clean committed checkout after fetching the pinned build dependencies. The task builds the portable Windows package, verifies its checksum and provenance, then uses the build JDK's jpackage tool to create a native **Opcodex.exe**.

The app includes a Java runtime and its original legal notices. It launches without a console window and uses the Opcodex icon in Windows. No separate Java installation is required to run that app image. Packaging/signing for public Windows distribution is separate; this local preview is unsigned.

The installer writes a versioned directory under `%LOCALAPPDATA%\Programs\Opcodex\versions\`, then creates **Opcodex** shortcuts on your Desktop and in the Start menu. It requires no administrator privileges. Projects and settings remain separate from the app installation. Existing versioned installations are kept.

To build the app image without creating shortcuts, use `.\gradlew.bat buildOpcodexDesktop`. Native images are cached under `build/desktop/` by the portable package hash. Each contains `desktop-build.json` with source and checksum provenance.

For portable launches, use `opcodexRun.bat`. Portable ZIPs do not bundle Java. Native app installation currently targets Windows x86-64; Linux/macOS remain portable build targets.

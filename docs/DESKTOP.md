<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Windows desktop app

With PowerShell 7 (`pwsh`) installed, run `.\gradlew.bat installOpcodexDesktop` from a clean committed checkout after fetching the pinned build dependencies. The task builds the portable Windows package, verifies its checksum and provenance, then uses the build JDK's jpackage tool to create a native **Opcodex.exe**.

The app includes a Java runtime and its original legal notices. It launches without a console window and uses the Opcodex icon in Windows. No separate Java installation is required to run that app image. Current packages are unsigned.

The installer writes a versioned directory under `%LOCALAPPDATA%\Programs\Opcodex\versions\`, then creates **Opcodex** shortcuts on your Desktop and in the Start menu. It requires no administrator privileges. Projects and settings remain separate from the app installation. Existing versioned installations are kept.

To build the app image without creating shortcuts, use `.\gradlew.bat buildOpcodexDesktop`. Native images are cached under `build/desktop/` by the portable package hash. Each contains `desktop-build.json` with source and checksum provenance.

For portable launches, use `opcodexRun.bat`. Portable ZIPs do not bundle Java. macOS remains a portable build target. The Linux installer is described below.

## Distributable Windows installer

`buildOpcodexInstaller` creates `build/releases/opcodex_1.0.0-alpha.2_win_x86_64_setup.msi`, its SHA-256 file and an installer provenance manifest. It packages the same native app image and bundled runtime. Recipients double-click the MSI; it installs for the current user, creates desktop/Start menu shortcuts and provides a Windows uninstall entry. No build tools or separate Java installation are needed to use it.

Building this installer requires JDK 25, PowerShell 7, WiX 5.0.2 and the matching `WixToolset.UI.wixext`/`WixToolset.Util.wixext` extensions. WiX is a build tool, not an application dependency. Put `wix.exe` on PATH or set `OPCODEX_WIX_HOME` to its directory. CI pins these tools. Use a clean committed checkout.

The Windows numeric installer version is `0.1.3` for product `1.0.0-alpha.2`, allowing the final `1.0.0` installer to upgrade it. Both identities and the stable upgrade UUID are recorded in `opcodex/windows-installer.json`. Keep the upgrade UUID across releases. The MSI application directory is `%LOCALAPPDATA%\Programs\Opcodex\Application`, separate from the existing `%LOCALAPPDATA%\opcodex` cache and the older versioned preview folders. Projects/settings live outside the installation directory.

Creating an MSI does not publish a release. Public promotion still requires the checks in [RELEASING](releases/RELEASING.md), and actual installation/launch evidence belongs in [VERIFICATION](releases/VERIFICATION.md).

## Linux desktop installer

On Ubuntu 24.04 x86-64, install the build prerequisites (`JDK 21`, Python 3, native C/C++ tools and `fakeroot`), fetch the pinned dependencies, then run `./gradlew buildOpcodexLinuxInstaller` from a clean committed checkout. It produces `build/releases/opcodex_1.0.0-alpha.2_linux_x86_64_setup.deb`, a SHA-256 file and a provenance manifest. The Debian package includes the runtime, compiler/debugger modules and original runtime notices. It uses the original minimal module-loader bootstrap.

After verifying its SHA-256 file, install with `sudo apt install ./opcodex_1.0.0-alpha.2_linux_x86_64_setup.deb`. Open **Opcodex** from the applications menu. No separate Java installation is needed for the installed desktop app. The application lives under `/opt/opcodex`; projects and settings stay in the user's profile. Linux distributions that cannot install Debian packages can use the portable Linux ZIP with JDK 21 or newer; broader distribution compatibility needs separate validation.

The Debian installer prepares the standard XDG system-menu directory when a minimal desktop does not already provide it. It preserves the build JDK's install/uninstall hooks and their resource substitutions.

CI installs the Debian package and launches its actual native launcher under Xvfb with a disposable profile and a controlled ELF analysis fixture. Checks cover the Light default, modern/Classic logo refresh, single-row toolbar resizing, hover labels, menus, nine panels, dividers, decompilation and graph navigation. The Windows-specific native file-picker test is only run on Windows. These checks supplement the full upstream unit-test gate.

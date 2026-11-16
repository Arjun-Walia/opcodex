# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""The Windows app ships the native Opcodex shell as Opcodex.exe beside its runtime and engine."""

import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SCRIPT = (ROOT / "opcodex" / "build-desktop.ps1").read_text(encoding="utf-8")
GRADLE = (ROOT / "opcodex" / "build.gradle").read_text(encoding="utf-8")


class DesktopShellPackagingTest(unittest.TestCase):

    def test_script_replaces_the_java_launcher_with_the_native_shell(self):
        self.assertIn("[string]$DesktopShellPath", SCRIPT)
        self.assertRegex(SCRIPT, r"Copy-Item -LiteralPath \$opcodexShell -Destination \(Join-Path \$opcodexImage 'Opcodex\.exe'\)")
        self.assertIn("desktopShellSha256", SCRIPT)

    def test_script_refuses_to_package_without_the_shell(self):
        self.assertIn("The native Opcodex app was not built", SCRIPT)

    def test_gradle_builds_the_shell_before_the_windows_app(self):
        self.assertIn("tasks.register('buildOpcodexDesktopShell'", GRADLE)
        self.assertRegex(GRADLE, r"dependsOn tasks\.named\('buildOpcodex'\), buildOpcodexDesktopShell")
        self.assertIn("'-DesktopShellPath'", GRADLE)

    def test_script_runs_on_the_windows_powershell_that_ships_with_windows(self):
        # Windows PowerShell 5.1 has no utf8NoBOM encoding, no $IsWindows and no overwrite overload.
        self.assertNotIn("utf8NoBOM", SCRIPT)
        self.assertNotRegex(SCRIPT, r"if \(-not \$IsWindows\)")
        self.assertNotRegex(SCRIPT, r"ExtractToDirectory\([^)]*,\s*\$true\)")

    def test_gradle_falls_back_to_windows_powershell_when_pwsh_is_missing(self):
        self.assertIn("powershell.exe", GRADLE)
        self.assertIn("'-ExecutionPolicy', 'Bypass'", GRADLE)

    def test_installer_version_upgrades_the_installed_preview(self):
        config = json.loads((ROOT / "opcodex" / "windows-installer.json").read_text(encoding="utf-8"))
        version = tuple(int(p) for p in config["installerVersion"].split("."))
        self.assertGreaterEqual(version, (0, 1, 7))


if __name__ == "__main__":
    unittest.main()

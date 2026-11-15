# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""The distribution license scan requires certification for engine sources and resources."""

import unittest
from pathlib import Path

MODULE = Path(__file__).resolve().parents[2] / "Ghidra" / "Features" / "OpcodexEngine"
ALLOWED_IP = {"Opcodex", "Apache License 2.0"}
UNCERTIFIED = {"build.gradle", "certification.manifest"}


def shipped_files():
    for path in MODULE.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(MODULE).as_posix()
        if relative.startswith(("build/", "bin/", ".")) or relative in UNCERTIFIED:
            continue
        yield relative


def certified():
    entries = {}
    for line in (MODULE / "certification.manifest").read_text(encoding="utf-8").splitlines():
        if not line or line.startswith("#"):
            continue
        fields = line.split("||")
        entries[fields[0]] = fields[1]
    return entries


class EngineModuleTest(unittest.TestCase):

    def test_engine_module_sources_and_resources_are_certified(self):
        entries = certified()
        missing = sorted(set(shipped_files()) - set(entries))
        self.assertEqual([], missing, "add these to certification.manifest")
        for name, ip in entries.items():
            self.assertIn(ip, ALLOWED_IP, name)


if __name__ == "__main__":
    unittest.main()

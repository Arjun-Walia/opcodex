"""Check genuine installed desktop identity across valid system data directories."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import importlib.util
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).parent / 'desktop/check_linux.py'
SPEC = importlib.util.spec_from_file_location('opcodex_linux_desktop_check', SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class LinuxDesktopEntryTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix='opcodex desktop ')
        self.addCleanup(self.temporary.cleanup)
        root = Path(self.temporary.name)
        self.launcher = root / 'installed app/bin/Opcodex'
        self.launcher.parent.mkdir(parents=True)
        self.launcher.touch()
        self.directories = [root / 'share/applications', root / 'local/share/applications']
        for path in self.directories:
            path.mkdir(parents=True)

    def write_entry(self, name='Opcodex', wm_class='opcodex-Opcodex', executable=None):
        executable = executable or self.launcher
        path = self.directories[1] / 'opcodex-Opcodex.desktop'
        path.write_text('[Desktop Entry]\nType=Application\nName=' + name + '\nExec="' +
                        executable.as_posix() + '" %F\nStartupWMClass=' + wm_class + '\n', encoding='utf-8')
        return path

    def test_alternate_system_directory_and_quoted_executable_are_validated(self):
        path = self.write_entry()
        result = MODULE.verify_desktop_entry(self.launcher, self.directories)
        self.assertEqual([entry['path'] for entry in result], [str(path)])

    def test_wrong_window_class_is_not_accepted(self):
        self.write_entry(wm_class='wrong-window-class')
        with self.assertRaises(ValueError):
            MODULE.verify_desktop_entry(self.launcher, self.directories)

    def test_wrong_product_name_is_not_accepted(self):
        self.write_entry(name='Wrong application')
        with self.assertRaises(ValueError):
            MODULE.verify_desktop_entry(self.launcher, self.directories)

    def test_unrelated_executable_does_not_satisfy_the_check(self):
        self.write_entry(executable=self.launcher.with_name('Other'))
        with self.assertRaises(FileNotFoundError):
            MODULE.verify_desktop_entry(self.launcher, self.directories)

    def test_missing_launcher_entry_still_fails(self):
        with self.assertRaises(FileNotFoundError):
            MODULE.verify_desktop_entry(self.launcher, self.directories)

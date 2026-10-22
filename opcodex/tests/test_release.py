"""Release-boundary tests use tiny synthetic distributions; they run no binaries."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import json
import stat
import struct
import subprocess
import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile, ZipInfo

from opcodex.package_release import ROOT, checked_members, digest, package_release, properties


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.repository = self.directory / 'repository'
        product_version = properties((ROOT / 'Ghidra/application.properties').read_text())['opcodex.version']
        for name in ('opcodex/upstream.json', 'Ghidra/application.properties', 'LICENSE', 'NOTICE',
                     'MODIFICATIONS.md', 'THIRD_PARTY_NOTICES.md', 'opcodex/DISTRIBUTION_README.md',
                     f'docs/releases/{product_version}.md', 'opcodex/WHATS_NEW.html',
                     'opcodexRun', 'opcodexRun.bat', 'opcodex/branding/opcodex.ico'):
            target = self.repository / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes((ROOT / name).read_bytes())
        self.source = self.directory / 'engine.zip'
        self.entries = {
            'engine/LICENSE': (ROOT / 'LICENSE').read_bytes(),
            'engine/Ghidra/application.properties': (ROOT / 'Ghidra/application.properties').read_bytes()
                + ('\napplication.revision.opcodex=' + 'a' * 40 + '\n').encode(),
            'engine/Ghidra/Features/Decompiler/os/linux_x86_64/decompile': b'fixture-not-executable',
            'engine/support/launch.sh': b'fixture-launch',
            'engine/ghidraRun': b'old-launcher',
            'engine/preserved.jar': b'unchanged-engine-payload',
        }

    def archive(self, entries=None):
        with ZipFile(self.source, 'w') as archive:
            for name, content in (self.entries if entries is None else entries).items():
                info = ZipInfo(name)
                info.create_system = 3
                info.external_attr = (stat.S_IFREG | 0o755) << 16
                archive.writestr(info, content)
        return self.source

    def package(self, platform='linux_x86_64'):
        return package_release(self.archive(), self.directory / 'output', platform,
                               self.repository, 'a' * 40)

    def test_package_preserves_payload_notices_modes_and_records_real_checksum(self):
        result = self.package()
        self.assertNotIn('ghidra', result.name.lower())
        with ZipFile(result) as archive:
            prefix = result.stem + '/'
            self.assertEqual(archive.read(prefix + 'preserved.jar'), b'unchanged-engine-payload')
            self.assertEqual(archive.read(prefix + 'LICENSE'), (ROOT / 'LICENSE').read_bytes())
            self.assertEqual(archive.read(prefix + 'NOTICE'), (ROOT / 'NOTICE').read_bytes())
            self.assertNotIn(prefix + 'ghidraRun', archive.namelist())
            self.assertIn(prefix + 'opcodexRun', archive.namelist())
            self.assertEqual((archive.getinfo(prefix + 'opcodexRun').external_attr >> 16) & 0o777, 0o755)
            self.assertEqual((archive.getinfo(prefix + 'preserved.jar').external_attr >> 16) & 0o777, 0o755)
            manifest = json.loads(archive.read(prefix + 'opcodex-release.json'))
            self.assertEqual(manifest['sourceRevision'], 'a' * 40)
            self.assertEqual(manifest['upstream']['version'], '12.1.4')
            self.assertEqual(manifest['sourceArchiveSha256'], digest(self.source))
        self.assertEqual(result.with_suffix('.zip.sha256').read_text().split()[0], digest(result))
        self.assertEqual(json.loads(result.with_suffix('.json').read_text())['archiveSha256'], digest(result))

    def test_rejects_stock_unbranded_engine(self):
        key = 'engine/Ghidra/application.properties'
        self.entries[key] = self.entries[key].replace(b'application.name=Opcodex', b'application.name=Ghidra')
        with self.assertRaisesRegex(ValueError, 'identity differs'):
            self.package()

    def test_rejects_incorrect_platform_instead_of_mislabelling_native_binaries(self):
        with self.assertRaisesRegex(ValueError, 'missing a required distribution file'):
            self.package('win_x86_64')

    def test_rejects_changed_license(self):
        self.entries['engine/LICENSE'] = b'changed'
        with self.assertRaisesRegex(ValueError, 'license differs'):
            self.package()

    def test_rejects_engine_version_drift(self):
        file = self.repository / 'Ghidra/application.properties'
        file.write_text(file.read_text().replace('application.version=12.1.4', 'application.version=12.3'))
        with self.assertRaisesRegex(ValueError, 'pinned upstream'):
            self.package()

    def test_rejects_a_stale_archive_even_when_product_version_matches(self):
        key = 'engine/Ghidra/application.properties'
        self.entries[key] = self.entries[key].replace(b'a' * 40, b'b' * 40)
        with self.assertRaisesRegex(ValueError, 'current source revision'):
            self.package()

    def test_untracked_source_cannot_be_misrepresented_as_a_clean_release(self):
        subprocess.run(['git', 'init', '-q', str(self.repository)], check=True)
        subprocess.run(['git', 'add', '.'], cwd=self.repository, check=True)
        subprocess.run(['git', '-c', 'user.name=Opcodex tests', '-c', 'user.email=tests@opcodex.invalid',
                        'commit', '-qm', 'Test fixture'], cwd=self.repository, check=True)
        (self.repository / 'uncommitted-source.java').write_text('untracked source')
        with self.assertRaisesRegex(ValueError, 'untracked files'):
            package_release(self.archive(), self.directory / 'output', 'linux_x86_64', self.repository)

    def test_rejects_unsafe_names_and_multiple_roots(self):
        for bad in ('engine/../escape', '/engine/absolute', 'engine\\windows', 'engine/C:drive', 'other/file'):
            with self.subTest(bad=bad):
                self.archive({**self.entries, bad: b'bad'})
                if '\\' in bad:
                    # ZipInfo normalizes Windows separators while writing; emulate a foreign ZIP.
                    self.source.write_bytes(self.source.read_bytes().replace(
                        bad.replace('\\', '/').encode(), bad.encode()))
                with ZipFile(self.source) as archive, self.assertRaises(ValueError):
                    checked_members(archive)

    def test_rejects_case_collisions_and_symlinks(self):
        self.archive({'engine/file': b'a', 'engine/FILE': b'b'})
        with ZipFile(self.source) as archive, self.assertRaisesRegex(ValueError, 'Duplicate'):
            checked_members(archive)
        with ZipFile(self.source, 'w') as archive:
            entry = ZipInfo('engine/link')
            entry.create_system = 3
            entry.external_attr = (stat.S_IFLNK | 0o777) << 16
            archive.writestr(entry, '../outside')
        with ZipFile(self.source) as archive, self.assertRaisesRegex(ValueError, 'symbolic-link'):
            checked_members(archive)

    def test_overlay_cannot_create_windows_case_collisions(self):
        self.entries['engine/readme.MD'] = b'old-readme'
        result = self.package()
        with ZipFile(result) as archive:
            names = archive.namelist()
            self.assertEqual(len(names), len({name.casefold() for name in names}))

    def test_rejects_missing_overlay_before_creating_package(self):
        (self.repository / 'NOTICE').unlink()
        with self.assertRaisesRegex(ValueError, 'Missing release overlay'):
            self.package()
        self.assertEqual(list((self.directory / 'output').glob('*.zip')), [])


class BrandingContracts(unittest.TestCase):
    def test_product_presentation_does_not_reintroduce_upstream_branding(self):
        for name in ('README.md', 'CONTRIBUTING.md', 'opcodex/DISTRIBUTION_README.md',
                     '.github/ISSUE_TEMPLATE/bug_report.md'):
            self.assertNotIn('ghidra', (ROOT / name).read_text(encoding='utf-8').lower(), name)

    def test_modern_icon_exports_are_complete_with_transparent_capability(self):
        catalog = json.loads((ROOT / 'ui-assets/icons/catalog.json').read_text())
        self.assertEqual(catalog['license'], 'Apache-2.0')
        resources = ROOT / 'Ghidra/Framework/Gui/src/main/resources/images/opcodex/ui'
        for glyph in set(catalog['mappings'].values()):
            self.assertIn(glyph, catalog['glyphs'])
            for theme in ('light', 'dark'):
                for size in (16, 20, 24, 32, 48, 64):
                    data = (resources / theme / f'{glyph}-{size}.png').read_bytes()
                    self.assertEqual(data[:8], b'\x89PNG\r\n\x1a\n')
                    self.assertEqual(struct.unpack('>II', data[16:24]), (size, size))
                    self.assertIn(data[25], (4, 6), 'Glyph must retain an alpha channel')

    def test_engine_and_licenses_remain_at_the_pinned_stable_baseline(self):
        upstream = json.loads((ROOT / 'opcodex/upstream.json').read_text())
        config = properties((ROOT / 'Ghidra/application.properties').read_text())
        self.assertEqual(config['application.version'], upstream['version'])
        self.assertEqual(config['application.name'], 'Opcodex')
        self.assertNotEqual(config['application.version'], config['opcodex.version'])
        result = subprocess.run(['git', 'diff', '--exit-code', upstream['commit'], '--',
                                 'LICENSE', 'NOTICE', 'licenses', 'GPL',
                                 'Ghidra/Features/Decompiler', 'Ghidra/Processors',
                                 'Ghidra/Framework/DB', 'Ghidra/Framework/SoftwareModeling'],
                                cwd=ROOT, capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stdout)

    def test_app_icon_sizes_are_correct_and_upstream_changes_are_documented(self):
        for theme in ('light', 'dark'):
            for size in (16, 24, 32, 40, 48, 64, 128, 256):
                data = (ROOT / f'Ghidra/Features/Base/src/main/resources/images/opcodex/app-{theme}-{size}.png').read_bytes()
                self.assertEqual(data[:8], b'\x89PNG\r\n\x1a\n')
                self.assertEqual(struct.unpack('>II', data[16:24]), (size, size))
        self.assertIn('OPCODEX:', (ROOT / 'Ghidra/application.properties').read_text())
        self.assertIn('Ghidra/application.properties', (ROOT / 'MODIFICATIONS.md').read_text())


if __name__ == '__main__':
    unittest.main()

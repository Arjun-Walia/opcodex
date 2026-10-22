"""Linux installer trust-boundary and launcher compatibility checks; no binaries run."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import json
import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile

from opcodex.build_linux import BOOTSTRAP, prepare_debian_resources, trim_classpath, verify_archive
from opcodex.package_release import digest


class LinuxPackageTests(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.directory = Path(temporary.name)
        self.archive = self.directory / 'opcodex.zip'
        self.manifest = dict(product='Opcodex', version='1.0.0-alpha.2',
                             platform='linux_x86_64', sourceRevision='a' * 40)
        self.write_package()

    def write_package(self, embedded=None, extra=None):
        with ZipFile(self.archive, 'w') as zipped:
            zipped.writestr('opcodex/opcodex-release.json', json.dumps(embedded or self.manifest))
            if extra:
                zipped.writestr(extra, 'untrusted')
        self.archive.with_suffix('.json').write_text(json.dumps(
            dict(self.manifest, archiveSha256=digest(self.archive))))
        self.archive.with_suffix('.zip.sha256').write_text(digest(self.archive) + '  opcodex.zip\n')

    def test_verified_payload_matches_external_source(self):
        manifest, checksum = verify_archive(self.archive)
        self.assertEqual(manifest['sourceRevision'], 'a' * 40)
        self.assertEqual(checksum, digest(self.archive))

    def test_changed_download_is_rejected(self):
        with self.archive.open('ab') as stream:
            stream.write(b'changed')
        with self.assertRaisesRegex(ValueError, 'checksum'):
            verify_archive(self.archive)

    def test_source_mismatch_is_rejected(self):
        self.write_package(embedded=dict(self.manifest, sourceRevision='b' * 40))
        with self.assertRaisesRegex(ValueError, 'provenance differ'):
            verify_archive(self.archive)

    def test_archive_traversal_is_rejected_before_extraction(self):
        self.write_package(extra='opcodex/../../outside')
        with self.assertRaisesRegex(ValueError, 'Unsafe archive'):
            verify_archive(self.archive)

    def test_launcher_keeps_only_original_module_bootstrap(self):
        config = self.directory / 'Opcodex.cfg'
        config.write_text('[Application]\n' + BOOTSTRAP +
                          '\napp.classpath=$APPDIR/unrelated.jar\n[JavaOptions]\njava-options=-Xmx2G\n')
        trim_classpath(config)
        self.assertEqual([line for line in config.read_text().splitlines()
                          if line.startswith('app.classpath=')], [BOOTSTRAP])
        self.assertIn('java-options=-Xmx2G', config.read_text())

    def test_missing_bootstrap_is_rejected(self):
        config = self.directory / 'Opcodex.cfg'
        config.write_text('app.classpath=$APPDIR/unrelated.jar\n')
        with self.assertRaisesRegex(ValueError, 'bootstrap'):
            trim_classpath(config)

    def test_installer_prepares_menu_directory_and_preserves_native_hooks(self):
        java_home = self.directory / 'jdk'
        (java_home / 'jmods').mkdir(parents=True)
        body = '# Original vendor notices\nset -e\nDESKTOP_COMMANDS_INSTALL\nexit 0\n'
        with ZipFile(java_home / 'jmods/jdk.jpackage.jmod', 'w') as module:
            module.writestr('classes/jdk/jpackage/internal/resources/template.postinst', '#!/bin/sh\n' + body)
        resources = prepare_debian_resources(java_home, self.directory / 'resources')
        script = (resources / 'postinst').read_text()
        self.assertTrue(script.startswith('#!/bin/sh\n'))
        self.assertIn(body, script)
        self.assertLess(script.index('mkdir -p /usr/share/desktop-directories'), script.index('DESKTOP_COMMANDS_INSTALL'))
        self.assertIn('if [ "${1:-}" = configure ]; then', script)
        self.assertIn('|| exit $?', script)
        prepare_debian_resources(java_home, resources)
        self.assertEqual((resources / 'postinst').read_text(), script)

    def test_unrecognized_native_install_template_is_rejected(self):
        java_home = self.directory / 'jdk'
        (java_home / 'jmods').mkdir(parents=True)
        with ZipFile(java_home / 'jmods/jdk.jpackage.jmod', 'w') as module:
            module.writestr('classes/jdk/jpackage/internal/resources/template.postinst', 'Unexpected format\n')
        with self.assertRaisesRegex(ValueError, 'template'):
            prepare_debian_resources(java_home, self.directory / 'resources')

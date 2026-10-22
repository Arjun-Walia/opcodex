#!/usr/bin/env python3
"""Build a native Linux app and Debian installer from a verified portable distribution."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
from __future__ import annotations

import argparse
import json
import os
import platform
import re
import shutil
import subprocess
import tempfile
from pathlib import Path
from zipfile import ZipFile

try:
    from opcodex.package_release import ROOT, checked_members, digest
except ModuleNotFoundError:
    from package_release import ROOT, checked_members, digest

BOOTSTRAP = 'app.classpath=$APPDIR/Ghidra/Framework/Utility/lib/Utility.jar'


def verify_archive(archive: Path) -> tuple[dict, str]:
    checksum = digest(archive)
    if archive.with_suffix('.zip.sha256').read_text().split()[0] != checksum:
        raise ValueError('Portable package checksum mismatch')
    manifest = json.loads(archive.with_suffix('.json').read_text())
    if (manifest.get('product') != 'Opcodex' or manifest.get('platform') not in
            {'linux_x86_64', 'linux_arm_64'} or manifest.get('archiveSha256') != checksum or
            not re.fullmatch(r'[0-9a-f]{40}', manifest.get('sourceRevision', '')) or
            not re.fullmatch(r'\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?', manifest.get('version', ''))):
        raise ValueError('Invalid Linux package provenance')
    with ZipFile(archive) as zipped:
        root, _ = checked_members(zipped)
        embedded = json.loads(zipped.read(root + '/opcodex-release.json'))
        for key in ('product', 'version', 'platform', 'sourceRevision'):
            if embedded.get(key) != manifest[key]:
                raise ValueError('Embedded and external package provenance differ')
    return manifest, checksum


def trim_classpath(config: Path) -> None:
    lines = config.read_text().splitlines()
    if BOOTSTRAP not in lines:
        raise ValueError('Native launcher is missing the supported bootstrap classpath')
    config.write_text('\n'.join(line for line in lines if not line.startswith('app.classpath=')
                                or line == BOOTSTRAP) + '\n')


def prepare_debian_resources(java_home: Path, destination: Path) -> Path:
    """Retain the build JDK's hooks and support desktops without an existing XDG menu directory."""
    with ZipFile(java_home / 'jmods/jdk.jpackage.jmod') as archive:
        template = archive.read('classes/jdk/jpackage/internal/resources/template.postinst').decode('utf-8')
    first, separator, remainder = template.partition('\n')
    if not separator or not first.startswith('#!'):
        raise ValueError('Unrecognized jpackage post-install template')
    setup = ('\n# OPCODEX: minimal desktops may not yet have the XDG system menu directory.\n'
             'if [ "${1:-}" = configure ]; then\n'
             '    mkdir -p /usr/share/desktop-directories || exit $?\n'
             'fi\n')
    destination.mkdir(parents=True, exist_ok=True)
    (destination / 'postinst').write_text(first + '\n' + setup + remainder)
    return destination


def build(archive: Path, java_home: Path, output: Path) -> Path:
    manifest, checksum = verify_archive(archive)
    expected = {'linux_x86_64': 'x86_64', 'linux_arm_64': 'aarch64'}[manifest['platform']]
    if platform.system() != 'Linux' or platform.machine() != expected:
        raise ValueError('Build the installer on its matching native Linux architecture')
    jpackage, java = java_home / 'bin/jpackage', java_home / 'bin/java'
    modules = [line.split('@')[0] for line in subprocess.check_output(
        [str(java), '--list-modules'], text=True).splitlines()]
    modules = [module for module in modules if module not in {'jdk.jlink', 'jdk.jpackage'}]
    if not {'java.desktop', 'jdk.compiler', 'jdk.jdi'}.issubset(modules):
        raise ValueError('The build JDK lacks required desktop/compiler/debugger modules')
    output.mkdir(parents=True, exist_ok=True)
    release, _, prerelease = manifest['version'].partition('-')
    package_release = '0.' + prerelease if prerelease else '1'
    with tempfile.TemporaryDirectory(prefix='opcodex-linux-') as temporary:
        work = Path(temporary)
        payload_parent = work / 'input'
        with ZipFile(archive) as zipped:
            root, members = checked_members(zipped)
            zipped.extractall(payload_parent)
            for member in members:
                path = payload_parent / member.filename
                mode = (member.external_attr >> 16) & 0o777
                if mode:
                    path.chmod(mode)
        payload = payload_parent / root
        arguments = [str(jpackage), '--type', 'app-image', '--name', 'Opcodex',
            '--app-version', release, '--vendor', 'Opcodex',
            '--description', 'Opcodex reverse-engineering workbench',
            '--copyright', 'Copyright 2026 Opcodex contributors',
            '--input', str(payload), '--dest', str(work / 'image'),
            '--main-jar', 'Ghidra/Framework/Utility/lib/Utility.jar',
            '--main-class', 'ghidra.Ghidra', '--arguments', 'ghidra.GhidraRun',
            '--add-modules', ','.join(modules),
            '--jlink-options', '--strip-debug --no-man-pages --no-header-files',
            '--icon', str(ROOT / 'Ghidra/Framework/Gui/src/main/resources/images/opcodex/brand/app-neutral-256.png')]
        for line in (payload / 'support/launch.properties').read_text().splitlines():
            if re.match(r'^VMARGS(?:_LINUX)?=', line):
                arguments.extend(['--java-options', line.split('=', 1)[1]])
        subprocess.run(arguments, check=True)
        image = work / 'image/Opcodex'
        trim_classpath(image / 'lib/app/Opcodex.cfg')
        record = {key: manifest[key] for key in ('product', 'version', 'platform', 'sourceRevision')}
        record.update(portableSha256=checksum, bundledRuntime=True,
                      runtime='Build JDK runtime with original legal notices',
                      packageVersion=release + '-' + package_release)
        (image / 'lib/app/desktop-build.json').write_text(json.dumps(record, indent=2) + '\n')
        resources = prepare_debian_resources(java_home, work / 'resources')
        subprocess.run([str(jpackage), '--type', 'deb', '--name', 'Opcodex',
            '--app-image', str(image), '--app-version', release, '--dest', str(work / 'installer'),
            '--vendor', 'Opcodex', '--description', 'Opcodex ' + manifest['version'],
            '--linux-package-name', 'opcodex', '--linux-app-release', package_release,
            '--linux-app-category', 'devel', '--linux-menu-group', 'Development',
            '--linux-shortcut', '--install-dir', '/opt/opcodex',
            '--resource-dir', str(resources), '--license-file', str(payload / 'LICENSE'),
            '--about-url', 'https://opcodex.xyz'], check=True)
        installers = list((work / 'installer').glob('*.deb'))
        if len(installers) != 1:
            raise ValueError('Expected exactly one Debian installer')
        target = output / f"opcodex_{manifest['version']}_{manifest['platform']}_setup.deb"
        shutil.copyfile(installers[0], target)
        record['installerSha256'] = digest(target)
        Path(str(target) + '.json').write_text(json.dumps(record, indent=2) + '\n')
        Path(str(target) + '.sha256').write_text(record['installerSha256'] + '  ' + target.name + '\n')
    return target


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--package', required=True, type=Path)
    parser.add_argument('--java-home', type=Path, default=os.environ.get('JAVA_HOME'))
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'build/releases')
    options = parser.parse_args()
    if options.java_home is None:
        parser.error('--java-home or JAVA_HOME is required')
    print(build(options.package.resolve(), options.java_home.resolve(), options.output_dir.resolve()))

#!/usr/bin/env python3
"""Package a source-built Opcodex distribution without altering its engine payload."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
from __future__ import annotations

import argparse
import copy
import hashlib
import json
import re
import shutil
import stat
import subprocess
import tempfile
from pathlib import Path, PurePosixPath
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

ROOT = Path(__file__).resolve().parents[1]
PLATFORMS = ('linux_x86_64', 'linux_arm_64', 'win_x86_64', 'mac_x86_64', 'mac_arm_64')


def properties(text: str) -> dict[str, str]:
    return dict(line.split('=', 1) for line in text.splitlines()
                if '=' in line and not line.lstrip().startswith(('#', '!')))


def digest(path: Path) -> str:
    checksum = hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b''):
            checksum.update(chunk)
    return checksum.hexdigest()


def checked_members(archive: ZipFile) -> tuple[str, list[ZipInfo]]:
    members = archive.infolist()
    seen: set[str] = set()
    roots: set[str] = set()
    for member in members:
        name = member.orig_filename
        parts = PurePosixPath(name).parts
        if not parts or name.startswith('/') or '\\' in name or ':' in name or '..' in parts:
            raise ValueError(f'Unsafe archive member: {name}')
        canonical = '/'.join(parts).casefold()
        if canonical in seen or stat.S_ISLNK(member.external_attr >> 16):
            raise ValueError(f'Duplicate or symbolic-link archive member: {name}')
        seen.add(canonical)
        roots.add(parts[0])
    if len(roots) != 1:
        raise ValueError('The input must contain exactly one distribution root')
    return roots.pop(), members


def package_release(source: Path, destination: Path, platform: str,
                    repository: Path = ROOT, revision: str | None = None) -> Path:
    if platform not in PLATFORMS:
        raise ValueError(f'Unsupported platform: {platform}')
    upstream = json.loads((repository / 'opcodex/upstream.json').read_text(encoding='utf-8'))
    config = properties((repository / 'Ghidra/application.properties').read_text(encoding='utf-8'))
    product_version = config['opcodex.version']
    if not re.fullmatch(r'\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?', product_version):
        raise ValueError('Invalid Opcodex semantic version')
    if config['application.version'] != upstream['version']:
        raise ValueError('Engine version does not match the pinned upstream release')
    if revision is None:
        if subprocess.check_output(['git', 'status', '--porcelain', '--untracked-files=no'],
                                   cwd=repository, text=True).strip():
            raise ValueError('Commit tracked source changes before packaging a release')
        revision = subprocess.check_output(['git', 'rev-parse', 'HEAD'],
                                           cwd=repository, text=True).strip()
    if not re.fullmatch(r'[0-9a-f]{40}', revision):
        raise ValueError('A full source commit is required')
    name = f'opcodex_{product_version}_ghidra{upstream["version"]}_{platform}'
    destination.mkdir(parents=True, exist_ok=True)
    target = destination / f'{name}.zip'
    manifest = {
        'product': 'Opcodex', 'version': product_version, 'platform': platform,
        'sourceRevision': revision, 'upstream': upstream,
        'sourceArchiveSha256': digest(source),
    }
    additions = {
        'NOTICE': repository / 'NOTICE',
        'README.md': repository / 'opcodex/DISTRIBUTION_README.md',
        'RELEASE_NOTES.md': repository / f'docs/releases/{product_version}.md',
        'docs/WhatsNew.html': repository / 'opcodex/WHATS_NEW.html',
        'MODIFICATIONS.md': repository / 'MODIFICATIONS.md',
        'THIRD_PARTY_NOTICES.md': repository / 'THIRD_PARTY_NOTICES.md',
        'opcodexRun': repository / 'opcodexRun',
        'opcodexRun.bat': repository / 'opcodexRun.bat',
        'support/opcodex.ico': repository / 'opcodex/branding/opcodex.ico',
    }
    replaced = {name.casefold() for name in additions}
    replaced.update(('ghidrarun', 'ghidrarun.bat', 'opcodex-release.json'))
    with ZipFile(source) as incoming:
        original_root, members = checked_members(incoming)
        prefix = original_root + '/'
        native = f'Ghidra/Features/Decompiler/os/{platform}/decompile'
        native += '.exe' if platform.startswith('win') else ''
        launch = 'support/launch.bat' if platform.startswith('win') else 'support/launch.sh'
        names = {member.filename for member in members}
        for required in ('LICENSE', 'Ghidra/application.properties', native, launch):
            if prefix + required not in names:
                raise ValueError(f'Input is missing a required distribution file: {required}')
        bundled = properties(incoming.read(prefix + 'Ghidra/application.properties').decode('utf-8'))
        for key in ('application.name', 'application.version', 'opcodex.version'):
            if bundled.get(key) != config.get(key) or bundled.get('application.name') != 'Opcodex':
                raise ValueError('Build the branded source before packaging; input identity differs')
        if incoming.read(prefix + 'LICENSE') != (repository / 'LICENSE').read_bytes():
            raise ValueError('The input license differs from the preserved source license')
        for added in additions.values():
            if not added.is_file():
                raise ValueError(f'Missing release overlay: {added.name}')
        with tempfile.TemporaryDirectory(prefix='opcodex-package-', dir=destination) as temporary:
            pending = Path(temporary) / target.name
            with ZipFile(pending, 'w', compression=ZIP_DEFLATED, compresslevel=6) as outgoing:
                for member in members:
                    relative = member.filename[len(prefix):] if member.filename.startswith(prefix) else ''
                    if not relative or member.is_dir() or relative.casefold() in replaced:
                        continue
                    entry = copy.copy(member)
                    entry.filename = f'{name}/{relative}'
                    entry.compress_type = ZIP_DEFLATED
                    with incoming.open(member) as reader, outgoing.open(entry, 'w') as writer:
                        shutil.copyfileobj(reader, writer)
                for relative, path in additions.items():
                    entry = ZipInfo(f'{name}/{relative}')
                    entry.create_system = 3
                    mode = 0o755 if relative == 'opcodexRun' else 0o644
                    entry.external_attr = (stat.S_IFREG | mode) << 16
                    outgoing.writestr(entry, path.read_bytes(), compress_type=ZIP_DEFLATED)
                outgoing.writestr(f'{name}/opcodex-release.json', json.dumps(manifest, indent=2) + '\n')
            pending.replace(target)
    checksum = digest(target)
    target.with_suffix('.zip.sha256').write_text(f'{checksum}  {target.name}\n', encoding='utf-8')
    manifest['archiveSha256'] = checksum
    target.with_suffix('.json').write_text(json.dumps(manifest, indent=2) + '\n', encoding='utf-8')
    return target


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--input', required=True, type=Path)
    parser.add_argument('--output-dir', required=True, type=Path)
    parser.add_argument('--platform', required=True, choices=PLATFORMS)
    args = parser.parse_args()
    print(package_release(args.input, args.output_dir, args.platform))


if __name__ == '__main__':
    main()

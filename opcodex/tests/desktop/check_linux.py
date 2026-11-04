#!/usr/bin/env python3
"""Exercise an installed Linux app with a disposable project and the real desktop agent."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import argparse
import configparser
import json
import os
import subprocess
import shlex
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]


def verify_desktop_entry(launcher: Path, directories=None):
    """Find the installed launcher in system XDG data directories, not one distro's path."""
    if directories is None:
        bases = os.environ.get('XDG_DATA_DIRS', '').split(':') + ['/usr/local/share', '/usr/share']
        directories = list(dict.fromkeys(Path(base) / 'applications' for base in bases
                                        if base and Path(base).is_absolute()))
    matches = []
    for directory in directories:
        for path in sorted(directory.glob('*.desktop')):
            entry = configparser.ConfigParser(interpolation=None, strict=False)
            try:
                entry.read(path, encoding='utf-8')
                desktop = entry['Desktop Entry']
                command = shlex.split(desktop.get('Exec', ''))
            except (configparser.Error, UnicodeError, KeyError, ValueError):
                continue  # Unrelated desktop entries may use another format or encoding.
            if not command or Path(command[0]).resolve() != launcher.resolve():
                continue
            if desktop.get('Name') != 'Opcodex' or desktop.get('Type') != 'Application':
                raise ValueError('The installed launcher has the wrong product identity: ' + str(path))
            if desktop.get('StartupWMClass') != 'opcodex-Opcodex':
                raise ValueError('The installed desktop entry has the wrong window class: ' + str(path))
            matches.append(dict(path=str(path), executable=command[0], wmClass='opcodex-Opcodex'))
    if not matches:
        raise FileNotFoundError('No system desktop entry points to the installed launcher: ' + str(launcher))
    return matches


def check(launcher: Path, output: Path, java_home: Path, development_overlay: bool = False):
    if output.exists():
        raise ValueError('Use a fresh output directory for each desktop test')
    output.mkdir(parents=True)
    app = launcher.parent.parent / 'lib/app'
    if (launcher.parent.parent / 'preview-origin.json').exists() and not development_overlay:
        raise ValueError('A modified test image requires --development-overlay; it is not installed-package evidence')
    if not development_overlay:
        entries = verify_desktop_entry(launcher)
        (output / 'linux-desktop-entry.json').write_text(json.dumps(entries, indent=2) + '\n')
        print('Validated system desktop entries:', ', '.join(entry['path'] for entry in entries), flush=True)
    projects, fixtures, classes = output / 'projects', output / 'fixtures', output / 'classes'
    for directory in (projects, fixtures, classes, output / 'headless-home'):
        directory.mkdir()
    fixture = fixtures / 'opcodex-demo.exe'  # Common fixture filename; this Linux file is ELF.
    subprocess.run(['gcc', '-g', '-O0', '-fno-inline', '-o', str(fixture),
                    str(ROOT / 'opcodex/tests/fixtures/interface-demo.c')], check=True)
    env = dict(os.environ, JAVA_HOME=str(java_home))
    env['JAVA_TOOL_OPTIONS'] = '-Duser.home=' + str(output / 'headless-home')
    with (output / 'headless.log').open('w') as log:
        subprocess.run([str(app / 'support/analyzeHeadless'), str(projects), 'OpcodexPreview',
                        '-import', str(fixture), '-max-cpu', '2'], env=env, stdout=log,
                       stderr=subprocess.STDOUT, check=True, timeout=180)
    source = ROOT / 'opcodex/tests/desktop'
    subprocess.run([str(java_home / 'bin/javac'), '--release', '21', '-d', str(classes)] +
                   [str(source / name) for name in ('OpcodexSmokeAgent.java',
                    'OpcodexPanelProbe.java', 'OpcodexAuditProbe.java')], check=True)
    manifest = output / 'agent.mf'
    manifest.write_text('Premain-Class: OpcodexSmokeAgent\n\n')
    agent = output / 'desktop-agent.jar'
    subprocess.run([str(java_home / 'bin/jar'), '--create', '--file', str(agent),
                    '--manifest', str(manifest), '-C', str(classes), '.'], check=True)
    # Only the tested process sees this profile and test agent. The fixture is never executed.
    env['JAVA_TOOL_OPTIONS'] = (f'-javaagent:{agent} -Duser.home={output}/home '
        f'-Dapplication.settingsdir={output}/settings -Dopcodex.smoke.output={output} '
        f'-Dopcodex.smoke.project={projects}')
    with (output / 'desktop.log').open('w') as log:
        process = subprocess.Popen([str(launcher)], env=env, stdout=log, stderr=subprocess.STDOUT)
        identity = None
        deadline = time.monotonic() + 420
        try:
            while process.poll() is None:
                if identity is None:
                    result = subprocess.run(['xprop', '-name', 'Opcodex: OpcodexPreview',
                                             'WM_CLASS', '_NET_WM_PID'], capture_output=True, text=True)
                    if result.returncode == 0 and 'WM_CLASS(STRING)' in result.stdout:
                        if '"opcodex-Opcodex", "opcodex-Opcodex"' not in result.stdout:
                            raise ValueError('The running window has the wrong WM_CLASS: ' + result.stdout)
                        if '= ' + str(process.pid) not in result.stdout:
                            raise ValueError('Window identity belongs to another process')
                        identity = dict(wmClass='opcodex-Opcodex', pidMatches=True,
                                        developmentOverlay=development_overlay)
                        (output / 'linux-window-identity.json').write_text(json.dumps(identity, indent=2) + '\n')
                if time.monotonic() >= deadline:
                    raise TimeoutError('Linux desktop checks exceeded 420 seconds')
                time.sleep(.5)
            if process.returncode != 0:
                raise subprocess.CalledProcessError(process.returncode, [str(launcher)])
            if identity is None:
                raise ValueError('No running product window identity was captured')
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait(timeout=10)
    result = json.loads((output / 'result.json').read_text())
    audit = json.loads((output / 'audit-result.json').read_text())
    assert result['startup'] and audit['singleRowToolbars'] and audit['nativeLogoRefresh']
    subprocess.run(['python3', str(ROOT / 'opcodex/check_panel_icons.py'), str(output)], check=True)
    print(('Development' if development_overlay else 'Installed') + ' Linux desktop checks passed')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--launcher', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, default=os.environ.get('JAVA_HOME'))
    parser.add_argument('--development-overlay', action='store_true',
                        help='Explicitly label a modified app image; never use this for installer verification')
    options = parser.parse_args()
    check(options.launcher.resolve(), options.output.resolve(), options.java_home.resolve(), options.development_overlay)

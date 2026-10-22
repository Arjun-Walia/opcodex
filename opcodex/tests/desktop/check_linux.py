#!/usr/bin/env python3
"""Exercise an installed Linux app with a disposable project and the real desktop agent."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import argparse
import json
import os
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]


def check(launcher: Path, output: Path, java_home: Path):
    if output.exists():
        raise ValueError('Use a fresh output directory for each desktop test')
    output.mkdir(parents=True)
    app = launcher.parent.parent / 'lib/app'
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
        subprocess.run([str(launcher)], env=env, stdout=log, stderr=subprocess.STDOUT,
                       check=True, timeout=420)
    result = json.loads((output / 'result.json').read_text())
    audit = json.loads((output / 'audit-result.json').read_text())
    assert result['startup'] and audit['singleRowToolbars'] and audit['nativeLogoRefresh']
    subprocess.run(['python3', str(ROOT / 'opcodex/check_panel_icons.py'), str(output)], check=True)
    print('Installed Linux desktop checks passed')


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--launcher', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, default=os.environ.get('JAVA_HOME'))
    options = parser.parse_args()
    check(options.launcher.resolve(), options.output.resolve(), options.java_home.resolve())

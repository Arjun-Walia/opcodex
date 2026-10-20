#!/usr/bin/env python3
"""Fail release verification when upstream Gradle ignored JUnit failures."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
from __future__ import annotations

import argparse
from pathlib import Path
import xml.etree.ElementTree as ET


def inspect_results(root: Path) -> dict:
    reports = sorted(root.glob('Ghidra/**/build/test-results/test/TEST-*.xml'))
    if not reports:
        raise ValueError('No upstream JUnit reports found')
    summary = {'reports': len(reports), 'tests': 0, 'skipped': 0, 'failures': []}
    for report in reports:
        suite = ET.parse(report).getroot()
        cases = list(suite.iter('testcase'))
        summary['tests'] += len(cases)
        summary['skipped'] += sum(case.find('skipped') is not None for case in cases)
        failed = [case for case in cases
                  if case.find('failure') is not None or case.find('error') is not None]
        summary['failures'].extend(
            f"{case.get('classname', report.stem)}.{case.get('name', '?')}"
            for case in failed)
        declared = sum(int(node.get(key, '0')) for node in suite.iter('testsuite')
                       for key in ('failures', 'errors'))
        if declared and not failed:
            summary['failures'].append(f'{report}: {declared} declared failures/errors')
    if not summary['tests']:
        raise ValueError('Upstream reports contain no test cases')
    if summary['tests'] == summary['skipped']:
        raise ValueError('Upstream reports contain no executed test cases')
    return summary


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path('.'))
    args = parser.parse_args()
    try:
        result = inspect_results(args.root)
    except (ValueError, OSError, ET.ParseError) as error:
        print(f'Upstream test verification failed: {error}')
        return 1
    print(f"Upstream JUnit: {result['tests']} tests in {result['reports']} reports; "
          f"{result['skipped']} skipped; {len(result['failures'])} failures/errors")
    for failure in result['failures']:
        print('FAILED:', failure)
    return 1 if result['failures'] else 0


if __name__ == '__main__':
    raise SystemExit(main())

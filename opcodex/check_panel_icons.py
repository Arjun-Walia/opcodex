# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""Reject repeated command symbols in the native desktop probe's panel captures."""
import argparse
import csv
from collections import defaultdict
from pathlib import Path


def check(directory: Path, theme: str) -> list[str]:
    captures = list(directory.glob(f'{theme}-icons-*.tsv'))
    if not captures:
        return [f'{theme}: no panel icon captures found']
    symbols = defaultdict(set)
    errors = []
    for capture in captures:
        with capture.open(encoding='utf-8', newline='') as stream:
            for row in csv.DictReader(stream, delimiter='\t'):
                symbols[(row['panel'], row['glyph'])].add(row['button'])
                if row['glyph'] == 'javax.swing.ImageIcon':
                    errors.append(f"{theme} / {row['panel']}: {row['button']} has a flattened bitmap")
    # Multiple captures may contain the same docked panel and the same command.
    # Shared row-type icons and dropdown affordances are not command buttons.
    return sorted(set(errors)) + [
        f'{theme} / {panel}: {glyph} is shared by {", ".join(sorted(commands))}'
        for (panel, glyph), commands in sorted(symbols.items()) if len(commands) > 1]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('directory', type=Path)
    args = parser.parse_args()
    errors = [error for theme in ('dark', 'light') for error in check(args.directory, theme)]
    for error in errors:
        print(error)
    print(f'Panel command icon check: {len(errors)} duplicate or missing-capture errors.')
    return int(bool(errors))


if __name__ == '__main__':
    raise SystemExit(main())

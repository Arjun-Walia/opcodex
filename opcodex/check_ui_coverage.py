# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""Audit original UI replacement coverage; incomplete v1 coverage fails by default."""
import argparse
import json
import re
import struct
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


def audit(root):
    catalog = json.loads((root / 'ui-assets/icons/catalog.json').read_text(encoding='utf-8'))
    definitions = {}
    for file in (root / 'Ghidra').rglob('*.theme.properties'):
        if 'build' in file.relative_to(root).parts:
            continue
        for line in file.read_text(encoding='utf-8').splitlines():
            match = re.match(r'^\s*(icon\.[\w.]+)\s*=\s*(.*)', line)
            if match:
                definitions.setdefault(match[1], []).append(match[2].split('#', 1)[0].strip())
    mapped = set(catalog['mappings'])
    errors = []
    for name, glyph in catalog['mappings'].items():
        if name not in definitions:
            errors.append('Unknown semantic ID: ' + name)
        if glyph not in catalog['glyphs']:
            errors.append('Unknown original glyph: ' + glyph)
    covered = mapped & definitions.keys()
    while True:
        before = set(covered)
        for name, expressions in definitions.items():
            if all(re.fullmatch(r'icon\.[\w.]+', expression) and expression in covered
                   for expression in expressions):
                covered.add(name)
        if covered == before:
            break
    for glyph in catalog['glyphs']:
        svg = ET.parse(root / 'ui-assets/icons/src' / (glyph + '.svg')).getroot()
        if svg.get('viewBox') != '0 0 24 24':
            errors.append('Invalid vector grid: ' + glyph)
        for theme in ('light', 'dark'):
            for size in catalog['sizes']:
                png = root / 'Ghidra/Framework/Gui/src/main/resources/images/opcodex/ui' / theme / f'{glyph}-{size}.png'
                data = png.read_bytes()
                if data[:8] != b'\x89PNG\r\n\x1a\n' or struct.unpack('>II', data[16:24]) != (size, size):
                    errors.append('Invalid PNG dimensions: ' + str(png.relative_to(root)))
                if data[25] not in (4, 6):
                    errors.append('Expected transparent PNG: ' + str(png.relative_to(root)))
    missing = sorted(definitions.keys() - covered)
    return {'uniqueIconIds': len(definitions), 'explicitOriginalMappings': len(mapped),
            'coveredIncludingPlainAliases': len(covered), 'missingCount': len(missing),
            'missingIconIds': missing, 'assetErrors': errors,
            'complete': not errors and not missing,
            'scope': 'Theme IDs and declared original exports. Raw-path callers and specialized workflows also require visual review.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--output', type=Path)
    parser.add_argument('--allow-incomplete', action='store_true', help='Report alpha progress; still fail for invalid assets')
    args = parser.parse_args()
    try:
        result = audit(args.root)
    except (OSError, ValueError, ET.ParseError, struct.error) as error:
        print('UI audit failed: ' + str(error), file=sys.stderr)
        return 1
    if args.output:
        args.output.write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')
    print(f"Original icon coverage: {result['coveredIncludingPlainAliases']}/{result['uniqueIconIds']} IDs; "
          f"{result['missingCount']} remain. Asset errors: {len(result['assetErrors'])}.")
    return 0 if not result['assetErrors'] and (result['complete'] or args.allow_incomplete) else 1


if __name__ == '__main__':
    raise SystemExit(main())

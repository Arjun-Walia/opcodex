# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""Compile original SVG geometry to Java2D paths; FontTools is a dev-only MIT tool."""
import json
from pathlib import Path
import xml.etree.ElementTree as ET
from fontTools.pens.recordingPen import RecordingPen
from fontTools.svgLib.path import parse_path

ROOT = Path(__file__).resolve().parents[1]
catalog = json.loads((ROOT / 'ui-assets/icons/catalog.json').read_text())
lines = ['/* ###', ' * IP: Apache License 2.0', ' * Copyright 2026 Opcodex contributors.',
         ' * SPDX-License-Identifier: Apache-2.0', ' */', 'package generic.theme.builtin;',
         'import java.awt.Shape;', 'import java.awt.geom.*;', 'import java.util.concurrent.*;',
         '/** Generated original geometry. No SVG parser or bitmap scaling at runtime. */',
         'final class OpcodexVectorPaths {',
         '\trecord Part(Shape shape, boolean fill, boolean stroke, float[] dash) {}',
         '\tprivate static final ConcurrentMap<String, Part[]> CACHE = new ConcurrentHashMap<>();',
         '\tstatic Part[] get(String name) { return CACHE.computeIfAbsent(name, OpcodexVectorPaths::create); }',
         '\tprivate static Part[] create(String name) { return switch (name) {']
for i, name in enumerate(catalog['glyphs']):
    lines.append(f'\t\tcase "{name}" -> glyph{i}();')
lines += ['\t\tdefault -> throw new IllegalArgumentException("Unknown glyph: " + name);', '\t}; }']

def num(value):
    return str(round(float(value), 6))

for i, (name, drawing) in enumerate(catalog['glyphs'].items()):
    lines.append(f'\tprivate static Part[] glyph{i}() {{')
    parts = []
    for j, element in enumerate(ET.fromstring('<svg>' + drawing + '</svg>')):
        a = element.attrib
        tag = element.tag
        var = f'p{j}'
        if tag == 'path':
            pen = RecordingPen()
            parse_path(a['d'], pen)
            lines.append(f'\t\tPath2D.Double {var} = new Path2D.Double();')
            for command, points in pen.value:
                method = {'moveTo': 'moveTo', 'lineTo': 'lineTo', 'curveTo': 'curveTo',
                          'qCurveTo': 'quadTo', 'closePath': 'closePath', 'endPath': None}[command]
                if method:
                    args = ', '.join(num(v) for point in points for v in point)
                    lines.append(f'\t\t{var}.{method}({args});')
        elif tag == 'circle':
            x, y, r = map(float, (a['cx'], a['cy'], a['r']))
            lines.append(f'\t\tShape {var} = new Ellipse2D.Double({x-r}, {y-r}, {2*r}, {2*r});')
        elif tag == 'rect':
            values = [a.get(k, '0') for k in ('x', 'y', 'width', 'height')]
            radius = float(a.get('rx', '0')) * 2
            lines.append(f'\t\tShape {var} = new RoundRectangle2D.Double({", ".join(map(num, values))}, {radius}, {radius});')
        elif tag in ('polyline', 'polygon', 'line'):
            points = ([float(a[k]) for k in ('x1', 'y1', 'x2', 'y2')] if tag == 'line' else
                      [float(n) for n in a['points'].replace(',', ' ').split()])
            lines.append(f'\t\tPath2D.Double {var} = new Path2D.Double();')
            for k in range(0, len(points), 2):
                lines.append(f'\t\t{var}.{"moveTo" if k == 0 else "lineTo"}({points[k]}, {points[k+1]});')
            if tag == 'polygon': lines.append(f'\t\t{var}.closePath();')
        else:
            raise ValueError(f'Unsupported original SVG element: {name}/{tag}')
        fill = a.get('fill', 'none') != 'none'
        stroke = a.get('stroke', 'currentColor') != 'none'
        dash = a.get('stroke-dasharray')
        dash_code = 'null' if not dash else 'new float[]{' + ','.join(num(v)+'f' for v in dash.replace(',', ' ').split()) + '}'
        parts.append(f'new Part({var}, {str(fill).lower()}, {str(stroke).lower()}, {dash_code})')
    lines.append('\t\treturn new Part[]{' + ', '.join(parts) + '};')
    lines.append('\t}')
lines.append('}')
target = ROOT / 'Ghidra/Framework/Gui/src/main/java/generic/theme/builtin/OpcodexVectorPaths.java'
target.write_text('\n'.join(lines) + '\n', encoding='utf-8')
print(f'Compiled {len(catalog["glyphs"])} original vector glyphs')

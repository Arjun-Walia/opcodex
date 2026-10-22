"""Keep the stable workbench's plugins and panels through layout changes."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import json
from pathlib import Path
import subprocess
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
BASELINE = json.loads((ROOT / 'opcodex/upstream.json').read_text(encoding='utf-8'))


class WorkbenchFeatureTests(unittest.TestCase):
    def test_bundled_tools_keep_every_plugin_and_supported_data_type(self):
        baseline = BASELINE.get('commit') or BASELINE.get('revision')
        self.assertTrue(baseline, 'Pinned engine revision is required')
        for path in (ROOT / 'Ghidra/Configurations/Public_Release/src/main/resources/defaultTools').glob('*.tool'):
            relative = path.relative_to(ROOT).as_posix()
            original = ET.fromstring(subprocess.check_output(['git', 'show', f'{baseline}:{relative}'], cwd=ROOT))
            current = ET.parse(path).getroot()
            for tag, attrs in [('PACKAGE', ['NAME']), ('INCLUDE', ['CLASS']),
                               ('EXCLUDE', ['CLASS']), ('SUPPORTED_DATA_TYPE', ['CLASS'])]:
                def identities(root):
                    return sorted(tuple(node.get(attr, '') for attr in attrs) for node in root.iter(tag))
                self.assertEqual(identities(original), identities(current), f'{path.name}: {tag}')

    def test_focused_layout_keeps_all_original_panels_and_their_identity(self):
        baseline = BASELINE.get('commit') or BASELINE.get('revision')
        relative = 'Ghidra/Configurations/Public_Release/src/main/resources/defaultTools/CodeBrowser.tool'
        original = ET.fromstring(subprocess.check_output(['git', 'show', f'{baseline}:{relative}'], cwd=ROOT))
        current = ET.parse(ROOT / relative).getroot()
        def identities(root):
            return sorted(tuple(node.get(attr, '') for attr in ['NAME', 'OWNER', 'INSTANCE_ID', 'GROUP'])
                          for node in root.iter('COMPONENT_INFO'))
        self.assertEqual(identities(original), identities(current))

    def test_classic_layout_is_the_original_stable_layout(self):
        baseline = BASELINE.get('commit') or BASELINE.get('revision')
        relative = 'Ghidra/Configurations/Public_Release/src/main/resources/defaultTools/CodeBrowser.tool'
        original = ET.fromstring(subprocess.check_output(['git', 'show', f'{baseline}:{relative}'], cwd=ROOT)).find('TOOL/ROOT_NODE')
        classic = ET.parse(ROOT / 'Ghidra/Configurations/Public_Release/src/main/resources/classicTools/CodeBrowser.xml').getroot().find('ROOT_NODE')
        def structure(node):
            return (node.tag, sorted(node.attrib.items()), [structure(child) for child in node])
        self.assertEqual(structure(original), structure(classic))

    def test_classic_uses_original_resources_not_repainted_bitmaps(self):
        baseline = BASELINE.get('commit') or BASELINE.get('revision')
        paths = ['Ghidra/Framework/Gui/data/gui.theme.properties',
                 'Ghidra/Framework/Docking/data/docking.theme.properties',
                 'Ghidra/Features/Base/data/base.icons.theme.properties',
                 'Ghidra/Framework/Project/data/project.theme.properties',
                 'Ghidra/Features/Base/src/main/resources/defaultTools/images/greenDragon24.png',
                 'Ghidra/Debug/Debugger/src/main/resources/images/debugger32.png']
        for relative in paths:
            original = subprocess.check_output(['git', 'show', f'{baseline}:{relative}'], cwd=ROOT)
            actual = (ROOT / relative).read_bytes()
            if relative.endswith('.properties'):
                def clean(data):
                    return [line.rstrip() for line in data.decode().splitlines()
                            if line.strip() and not line.startswith('# OPCODEX:')]
                self.assertEqual(clean(original), clean(actual), relative)
            else:
                self.assertEqual(original, actual, relative)

    def test_default_function_columns_keep_the_original_information(self):
        relative = 'Ghidra/Configurations/Public_Release/src/main/resources/defaultTools/CodeBrowser.tool'
        tool = ET.parse(ROOT / relative)
        preferences = next(node for node in tool.iter('PREFERENCE_STATE') if node.get('NAME', '').startswith('ghidra.app.plugin.core.functionwindow.FunctionTableModel:'))
        visible = {node.get('NAME') for node in preferences.iter('COLUMN') if node.get('VISIBLE') == 'true'}
        for name in ['Name', 'Location', 'Function Signature', 'Function Size']:
            self.assertTrue(any(column.endswith('.' + name) for column in visible), name)


if __name__ == '__main__':
    unittest.main()

"""Verify the release gate catches failures ignored by upstream Gradle."""
# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
from pathlib import Path
import tempfile
import unittest

from opcodex.check_test_results import inspect_results


class UpstreamResultGate(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.reports = self.root / 'Ghidra/Framework/Gui/build/test-results/test'
        self.reports.mkdir(parents=True)

    def report(self, xml):
        (self.reports / 'TEST-example.xml').write_text(xml, encoding='utf-8')

    def test_missing_or_empty_reports_do_not_pass(self):
        with self.assertRaisesRegex(ValueError, 'No upstream'):
            inspect_results(self.root)
        self.report('<testsuite tests="0"/>')
        with self.assertRaisesRegex(ValueError, 'no test cases'):
            inspect_results(self.root)
        self.report('<testsuite><testcase name="ignored"><skipped/></testcase></testsuite>')
        with self.assertRaisesRegex(ValueError, 'no executed test cases'):
            inspect_results(self.root)

    def test_success_and_skips_are_counted(self):
        self.report('<testsuite><testcase name="ok"/><testcase name="optional">'
                    '<skipped/></testcase></testsuite>')
        result = inspect_results(self.root)
        self.assertEqual((result['tests'], result['skipped'], result['failures']), (2, 1, []))

    def test_failure_and_error_elements_block_release(self):
        self.report('<testsuite failures="1" errors="1">'
                    '<testcase classname="Example" name="failed"><failure/></testcase>'
                    '<testcase classname="Example" name="error"><error/></testcase></testsuite>')
        self.assertEqual(inspect_results(self.root)['failures'], ['Example.failed', 'Example.error'])

    def test_declared_failure_without_case_element_still_blocks(self):
        self.report('<testsuite failures="1"><testcase name="incomplete"/></testsuite>')
        self.assertEqual(len(inspect_results(self.root)['failures']), 1)

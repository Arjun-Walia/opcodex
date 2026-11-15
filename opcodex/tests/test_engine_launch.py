# Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
"""Starts the engine host from the development tree and talks to it over loopback TCP."""

import json
import os
import shutil
import socket
import subprocess
import tempfile
import time
import unittest
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
ARG_FILE = REPO / "Ghidra" / "Features" / "OpcodexEngine" / "build" / "engine.args"
MAIN_CLASS = os.environ.get("OPCODEX_ENGINE_MAIN", "io.opcodex.engine.EngineMain")
STARTUP_SECONDS = 120


def java_executable() -> str:
    home = os.environ.get("JAVA_HOME")
    if home:
        candidate = Path(home) / "bin" / ("java.exe" if os.name == "nt" else "java")
        if candidate.exists():
            return str(candidate)
    found = shutil.which("java")
    if not found:
        raise unittest.SkipTest("No Java runtime on PATH")
    return found


@unittest.skipUnless(ARG_FILE.exists(),
                     "Run the :OpcodexEngine:printEngineClasspath Gradle task first")
class EngineLaunchTest(unittest.TestCase):

    def test_tcp_mode_prints_ready_and_answers_engine_info(self):
        session = Path(tempfile.mkdtemp(prefix="opcodex-session-"))
        env = dict(os.environ, OPCODEX_SESSION_DIR=str(session))
        process = subprocess.Popen(
            [java_executable(), f"@{ARG_FILE}", MAIN_CLASS, "--tcp"],
            cwd=REPO, env=env, stdout=subprocess.PIPE, stderr=subprocess.DEVNULL,
            stdin=subprocess.DEVNULL, text=True)
        try:
            deadline = time.monotonic() + STARTUP_SECONDS
            ready_line = process.stdout.readline()
            self.assertLess(time.monotonic(), deadline, "engine took too long to start")
            ready = json.loads(ready_line)
            self.assertTrue(ready["ready"])
            port = ready["port"]

            info_file = json.loads((session / "engine.json").read_text(encoding="utf-8"))
            self.assertEqual(port, info_file["port"])
            self.assertEqual(process.pid, info_file["pid"])
            token = (session / "client.token").read_text(encoding="utf-8").strip()

            with socket.create_connection(("127.0.0.1", port), timeout=60) as conn:
                request = {"jsonrpc": "2.0", "id": 1, "method": "engine.info",
                           "params": {}, "auth": token}
                conn.sendall((json.dumps(request) + "\n").encode("utf-8"))
                reply = json.loads(conn.makefile("r", encoding="utf-8").readline())
            self.assertEqual("12.1.4", reply["result"]["engineVersion"])
            self.assertEqual("Opcodex", reply["result"]["product"])
        finally:
            process.kill()
            process.wait(timeout=30)
            shutil.rmtree(session, ignore_errors=True)


if __name__ == "__main__":
    unittest.main()

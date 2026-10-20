:: Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
@echo off
setlocal
set "OPCODEX_LAUNCH=%~dp0support\launch.bat"
if not exist "%OPCODEX_LAUNCH%" set "OPCODEX_LAUNCH=%~dp0Ghidra\RuntimeScripts\Windows\support\launch.bat"
set "OPCODEX_HEAP=%GHIDRA_MAXMEM%"
if defined GHIDRA_GUI_MAXMEM set "OPCODEX_HEAP=%GHIDRA_GUI_MAXMEM%"
if defined OPCODEX_MAXMEM set "OPCODEX_HEAP=%OPCODEX_MAXMEM%"
set "OPCODEX_VMARGS=%GHIDRA_JAVA_OPTIONS% %GHIDRA_GUI_JAVA_OPTIONS% %OPCODEX_JAVA_OPTIONS%"
call "%OPCODEX_LAUNCH%" bg jdk Opcodex "%OPCODEX_HEAP%" "%OPCODEX_VMARGS%" ghidra.GhidraRun %*
exit /b %ERRORLEVEL%

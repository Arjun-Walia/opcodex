<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->
# Windows desktop smoke harness

These sources test a real, already built native app with a disposable settings directory and a controlled, analyzed fixture project. They are not included in application archives. The fixture is inspected, never executed. The harness exits its own app process after testing; use a separate profile and no user projects.

Compile all four Java files with `javac --release 21`, using the pinned JNA/JNA-platform 5.14.0 jars on the compiler classpath, then create a Java agent jar with `Premain-Class: OpcodexSmokeAgent`. Launch the native app with process-scoped `JAVA_TOOL_OPTIONS` containing:

- `-javaagent:<absolute-agent-jar>`
- `-Duser.home=<unique-output>/home`
- `-Dapplication.settingsdir=<unique-output>/settings`
- `-Dopcodex.smoke.output=<unique-output>`
- `-Dopcodex.smoke.project=<fixture-project-parent>` (project name `OpcodexPreview`)

Prepare that project using the built application's `support/analyzeHeadless.bat`, importing `build/ui-fixture/opcodex-demo.exe` from the original `interface-demo.c` fixture. Keep the desktop available during the run. The harness accepts the first-run agreement only in its disposable profile, exercises welcome commands, CodeBrowser, overflow commands, Go To, workspace reset/undo, theme switching (including Classic and a return to the saved modern layout), fonts, progress and About. It also verifies the four startup tool icons use live vectors, opens nine original panels in both themes, populates Function Graph from navigation, drags three independent dividers and tests their double-click reset, and compares the full registered command set before and after changing toolbar modes. Success requires `result.json` and exit code 0; a failure writes `failure.txt` and exits 2.

The native picker probe checks actual returned paths for single files, cancellation, folders, multiple files and a new filename. It verifies Swing continues dispatching while the native picker is modal. It only enumerates native windows owned by its own process. `native-picker-result.json` records successful cases. Native picker captures may show personal Windows navigation favorites: keep those captures local; do not publish them to the repository.

Screenshots capture real application windows. Revalidate/repaint before capturing moved Swing windows to avoid compositor artifacts. Do not label a development JAR overlay as a packaged or installed build. A JVM UI-scale override is distinct from changing Windows display settings.

`audit-result.json` records live selection contrast, expanded paths retained after a theme switch, exact layout restoration and 80 event-dispatch response samples. These samples measure the tested machine after settling; they are not a universal frame-rate claim.

Run `python opcodex/check_panel_icons.py <smoke-output>` after the probe. It compares the resolved geometry of visible commands, including split-control base icons and the complete main toolbar, in both Light and Dark. Different commands in one panel must not share a symbol. Repeated captures of the same command are counted once.

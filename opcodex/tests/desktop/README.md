<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->
# Desktop smoke harness

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

The current harness also verifies first-run Light, retired-theme migration, preservation of explicit Classic preferences, native window-logo changes, toolbar geometry at 700/1500/1900 logical pixels, enabled pointer cursors and a tooltip actually shown by hovering. CodeBrowser and Debugger both run. Provider owners distinguish separate dynamic/static Listing panes that share an internal name.

The pass-2 follow-up adds `pass2-result.json`: actual Runtime Information and controlled error Details must agree on product, engine and source revision; common buttons must set an actual AWT cursor property; navigation-rail shortcuts and its searchable panel picker open existing providers. The Light chooser's flat places sidebar must match the live theme. Script Manager Help is opened and its reading width checked. These run inside the same isolated profile and never submit an error report. The rail has its own icon-inventory scope, distinct from the main toolbar and docked provider headers.

On Linux, `check_linux.py` prepares a controlled ELF fixture, compiles the platform-independent agent classes and launches an installed native launcher. Run it in a disposable window-managed Xvfb session, with `openbox`, `x11-utils`, `gcc` and the build JDK installed:

```sh
xvfb-run -a -s '-screen 0 2560x1440x24' bash opcodex/tests/desktop/run_linux.sh \
  --launcher /opt/opcodex/opcodex/bin/Opcodex \
  --output /tmp/opcodex-installed-check
```

The wrapper starts and cleans up only its own window manager. A bare Xvfb server is not a complete desktop: a real tooltip hover failed there but passed with window management, without changing the installed application. Window identity, PID and desktop entry remain required. Windows-native picker checks are deliberately Windows-only. Use persistent test directories for retained app images and a fresh output directory for each run. An overlay is development evidence; only an unchanged installed artifact proves the installer. Panel checks also reject an empty overflow-only toolbar row in detached windows.

Run `python opcodex/check_panel_icons.py <smoke-output>` after the probe. It compares the resolved geometry of visible commands, including split-control base icons and the complete main toolbar, in both Light and Dark. Different commands in one panel must not share a symbol. Repeated captures of the same command are counted once.

The tabbed-ribbon follow-up starts at Home (Debug in the Debugger), selects All tools to verify the complete original toolbar, and runs the original Window → Function Graph command from search. The finder lists separate actions with the same short name distinctly; the test chooses the Window action by owner, rather than accidentally selecting an unavailable graph-context action. Debugger checks include the session/state defaults, its own rail, Classic separation and selected-tab restoration with bounded native divider rounding. A temporary DBTrace is opened/activated/closed through the original trace manager to verify empty-state transitions; no live process or sample is executed. `opcodex.smoke.debuggerOnly` is an explicitly limited development diagnostic and is not full smoke evidence.

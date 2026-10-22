<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex v1 foundation verification

## 2026-10-22: installed alpha.2 baseline and follow-up checks

Follow-up source `634f9b517b2ccedfa1f8317f337cfc26465965e7` completed `buildOpcodexInstaller`: **BUILD SUCCESSFUL in 51m24s**, 705 tasks (401 executed, 304 up-to-date), without excluding build or test tasks. The Windows ZIP SHA-256 is `cde29692f91f13450a8f73296fbf25f224f82512d32253cf76134a1e5c478c8c`; its MSI (installer version `0.1.3`) SHA-256 is `504806126eb6af511a5da0a927e5f0d72cd0c6a73b6d215bc4c64cf8fd675146`. Installation of the final follow-up artifact is recorded separately below when complete.

Development overlays of that application code passed the complete real desktop harness on Windows 11 and Ubuntu 24.04/OpenJDK 21/Xvfb, including both CodeBrowser and Debugger. The Linux fixture was compiled as ELF and analyzed, never executed. Both final inventories reported zero duplicate/flattened command errors after distinguishing provider owners: the dynamic and static Listing views have the same internal name but are separate providers. The catalog is 149 glyphs/886 declared bindings with zero asset errors. The contextual-filter test passed every match mode with its inversion indicator retained.

CI [39771565063](https://github.com/Arjun-Walia/opcodex/actions/runs/39771565063) built Windows ZIP/MSI, macOS ZIP and Linux ZIP/Debian packages from source `634f9b517b2ccedfa1f8317f337cfc26465965e7`. Its Linux install check failed because a minimal system lacked the XDG system-menu directory. The packaging follow-up preserves the build JDK's native post-install template, prepares that directory and retains installation failures. Its boundary suite has 32 passing tests. Corrected installed Linux checks still need a fresh CI run; the development overlay is not proof of a corrected installed package.

Source `3ae3b1319e0fc841188e55c95bbda53deccec68a` completed the full Windows native/installer build. Its portable ZIP SHA-256 is `12225fa2e358a9f2c92c03652d7b89d2e9a6218e03010f1b837d4bce0e8bfbde`; MSI SHA-256 is `7f856b291f8c0ed54357c518b1885b74f1438854298bf663326745a794fe8ec7`. The MSI installed successfully (exit 0) into `%LOCALAPPDATA%/Programs/Opcodex/Application`. Desktop and Start Menu shortcuts point to that native executable. The exact installed app passed the real desktop harness at normal scale and with a Java 200% UI-scale override. This override is not a Windows display-settings change. Both icon inventories had zero duplicate/flattened command errors across the tested CodeBrowser panels.

CI run [39742047691](https://github.com/Arjun-Walia/opcodex/actions/runs/39742047691) built the Windows ZIP/MSI and native Linux/macOS ZIPs. The broad Linux unit-test job failed and timed out. Missing external BSim/CodeCompare/HelloWorld fixtures and a tab-layout test precondition were among the recorded failures. This is not a passing release run.

The next follow-up changes make Light the default, refresh native branding after theme changes, keep toolbars on one row and improve hover labels/cursors. A Windows development overlay passed fresh Light startup, retired-theme migration, explicit Classic preservation, native-logo round trips, visible tooltips, CodeBrowser/Debugger resizing at 700/1500/1900 logical pixels, nine CodeBrowser panels, three dividers and the unchanged 922-command registry. The expanded Debugger inventory then identified additional symbol collisions; their corrections and the Linux installer are being validated separately. These development checks are not proof of a newly installed release artifact.

Focused Linux checks under Ubuntu 24.04/OpenJDK 21/Xvfb passed 22 toolbar/tab tests using the same compiled Java classes and real Linux Swing environment. The tab fixture now waits for native validation before asserting overflow, retaining its original assertions. Windows checks passed 13 theme tests and 23 toolbar/tab tests after the provider-header correction. The packaging boundary suite has 30 passing tests. None of these replace the broad upstream release gate. Exact follow-up installer, installation and Linux desktop results will be recorded against their source manifests.

## Historical alpha.1 verification

Date: 2026-10-20 (Asia/Calcutta). Product: `1.0.0-alpha.1`. Engine baseline: `Ghidra_12.1.4_build`, commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`.

The implementation tested here is commit `1a51e8bca5cd0586d80df0f4ba3fb0f62640f24f`. Later documentation and screenshot changes do not alter that implementation. The packaged application is a branding/release alpha; the complete icon/theme/UX redesign remains a later v1 stage.

## Local Windows results

Windows 11 x86-64, Oracle JDK 25.0.2, checked-in Gradle 9.7.1, Visual Studio Community 2026 C++ toolchain and Windows SDK.

| Check | Result |
|---|---|
| `python -m unittest discover -s opcodex/tests -v` | PASS: 13 original release/branding tests at the build revision; 17 tests pass after adding the JUnit failure gate (four gate tests). |
| `.\gradlew.bat --console=plain :Base:compileJava :Project:compileJava :Docking:compileJava` | PASS: all changed Java classes compiled; 15 tasks, 6m50s. |
| `.\gradlew.bat --console=plain buildOpcodex` | PASS: full native application and product package; 705 tasks (690 executed, 15 up-to-date), 39m44s. |
| `git diff --check` | PASS. |
| Upstream-file provenance | PASS: change record, OPCODEX markers and original Java headers checked. LICENSE, NOTICE and protected engine sources remain at the pinned baseline. |
| Stable UI inventory | PASS: 878 theme icon IDs and 612 runtime image candidates; source references and group totals checked. |
| Workflow and documentation | PASS: workflow YAML, embedded Python syntax, Bash syntax and local documentation links checked. |

The local package is `opcodex_1.0.0-alpha.1_ghidra12.1.4_win_x86_64.zip`, 577,004,843 bytes, SHA-256 `17dfa6c9edef97bb59cd5a1060d7a77b38fdc823ea498d1aa311848c48ac5a3b`. Its manifest names the tested source revision. Dependency downloads were checked against the upstream pinned SHA-256 values before the unmodified bootstrap/build consumed them. Existing upstream compiler/deprecation warnings did not prevent the build.

## Packaged desktop launch

Both the extracted local package and the exact Windows CI artifact were started through `opcodexRun.bat` at **125% Windows display scaling**. A temporary in-process Java/Swing harness used a disposable settings directory and empty project, then exited the application. No analysis sample was imported or executed. The harness is not included in the product.

Both launches passed. The committed screenshots show the Windows CI artifact, SHA-256 `95f3aa2ead798d07eb58732f6a943a170e57b1ec041fe886442034e091c0aff1`, 566,463,892 bytes.

Verified startup, separate Opcodex settings identity, first-run agreement, empty project creation, CodeBrowser launch, actual Flat Light/Flat Dark switching, and About's independent product/engine versions. Screenshots were visually inspected for product identity and readability. The screenshot capture dismissed the inherited Tip of the Day window so it did not obscure the project window.

Evidence: [light project](../ui/screenshots/light/project.png), [dark project](../ui/screenshots/dark/project.png), [light CodeBrowser](../ui/screenshots/light/codebrowser.png), [dark CodeBrowser](../ui/screenshots/dark/codebrowser.png), [light About](../ui/screenshots/light/about.png), [dark About](../ui/screenshots/dark/about.png), [agreement](../ui/screenshots/light/agreement.png), [harness result](../ui/screenshots/result.json), [runtime/display](../ui/screenshots/display.txt).

## CI native builds and engine tests

[CI run 39405401679](https://github.com/Arjun-Walia/opcodex/actions/runs/39405401679) builds the same source with Temurin JDK 21 on Windows 2022, Ubuntu 24.04 and macOS 15 ARM64. Windows and macOS native package jobs passed. Linux native packaging passed. **The full upstream suite is not passing and release promotion is blocked.** The captured log reports missing-fixture failures in `SimilarFunctionQueryServiceTest`, `DualDecompilerActionTest` and `TraceRmiLauncherServicePluginTest.testGetClassName` (including absent `TestResources/HelloWorld.class` and `CodeCompare/demangler_gnu_v2_24_fulldebug.gzf`), plus a debugger breakpoint assertion. The job was cancelled after it stopped producing output in `DebuggerWatchesProviderTest.testEditRegisterRepresentationEmu`. Missing resources are evidenced in the log; the cause of the other debugger failures has not been established. Preserved partial JUnit reports contain 4,505 test cases across 399 reports: 50 skipped and 21 failures/errors. These are partial-run counts, not a completed-suite total. The new gate was exercised on those actual reports and returned failure as required. See [regression summary](regression-2026-10-20.json). No engine change or test exclusion was made to hide them.

The upstream test configuration sets `ignoreFailures true` in `gradle/root/test.gradle`. New CI explicitly parses JUnit XML after the task and fails on test failures/errors, missing reports or no executed tests. The reusable draft workflow requires that explicit gate as well as all native builds; the earlier run cannot be promoted using task success alone. Native packages upload before the regression suite so failed diagnostics still leave inspectable build outputs. The regression step is bounded to 45 minutes and preserves reports. These CI changes do not alter the packaged analysis engine. Promotion was also exercised against the real cancelled run and correctly rejected it before checkout/download/release creation.

The downloaded macOS CI ZIP also passed SHA-256 and source-revision verification: `91514df9cfb4039453f1e83beccda3e8af53763afec401ef7447c9340154d85d`, 565,764,576 bytes. No macOS launch is claimed.

Platform artifacts include SHA-256 files and JSON manifests. A build success establishes compilation and packaging for that platform; it does not establish a GUI launch on that platform. The local Windows build above has its own checksum and is distinct from the CI Windows build despite sharing the source revision.

## Limits

- Windows GUI coverage is 125% on one machine with JDK 25.0.2. 100%, 150%, 200%, Linux/macOS GUI, full keyboard/accessibility, extension compatibility and populated analysis workflows have not been verified here.
- Existing Flat light/dark themes and action icons remain. This work adds the Opcodex brand; the later full design-system rollout is incomplete.
- Packages are unsigned. A SHA-256 checksum is an integrity record, not a publisher signature.
- Only actual attached native ZIPs are application downloads. GitHub source archives are source code. No stable/final v1 release is claimed by this alpha record.

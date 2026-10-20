<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex v1 foundation verification

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

<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Releasing Opcodex

## Versions and source

`Ghidra/application.properties` keeps `application.version` at the compatible upstream engine version. `opcodex.version` is the product semantic version. `opcodex/upstream.json` pins the stable source. Change the product version, notes and compatibility record deliberately; do not alter the engine version merely to label a product release.

Use `v<opcodex.version>` tags, for example `v1.0.0-alpha.1`. Alpha/beta/rc releases are prereleases. Initial work is on `codex/v1-stable`; the original `master` and earlier audit branch remain available. Integrate through review without rewriting the imported history or force-pushing existing branches.

## Build and verify

1. Work from committed source. Run `python -m unittest discover -s opcodex/tests -v` and `git diff --check`.
2. Bootstrap with the pinned wrapper: `./gradlew -I gradle/support/fetchDependencies.gradle`.
3. Build the native platform with `./gradlew buildOpcodex` (PowerShell: `.\gradlew.bat`). Outputs are in `build/releases/`.
4. Run `unitTestReport` in an appropriate desktop environment (Linux CI uses Xvfb). Packaging contracts are not the full engine test suite.
5. Extract and launch the ZIP. Check the project window, CodeBrowser, About, first-run agreement and light/dark icon readability. Record actual platform/scale coverage in `VERIFICATION.md`; preserve existing project data.
6. Confirm notices, platform native binaries, executable bits, build manifest and checksum. No imported analysis binary should be executed during these checks.

The packager rejects incompatible versions, unbranded inputs, missing platform binaries, changed license bytes, multiple archive roots, traversal paths, duplicate paths and symlink entries. It preserves the engine payload and file modes while adding the product launchers, notices, README, icon and manifest. Committed assets are sufficient for release builds.

## GitHub workflow

The Opcodex workflow runs on pushes/PRs and can be started manually. It builds native Windows x86-64, Linux x86-64 and macOS ARM64 packages. The Linux job also runs upstream unit tests. Each platform uploads its ZIP, `.sha256` and JSON manifest; test reports are separate artifacts.

To create a release, run the workflow manually with `create_draft` enabled, or push a matching version tag after review. The release job requires every build/test job to pass and creates a **draft** with the matching release notes and real native artifacts. It verifies that a tag-triggered version matches the source product version. Publishing the reviewed draft is a separate action. Do not attach an upstream stock ZIP under an Opcodex filename or call untested platforms verified.

Keep release notes consistent with the actual shipped features. Refresh `MODIFICATIONS.md` for every upstream-file change and retain upstream notices. Drafts must state missing GUI/platform checks and unsigned status. Before promoting v1 to stable, complete the later icon/theme/UX gates and their verification.

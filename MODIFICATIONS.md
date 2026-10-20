<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex modifications

Baseline: Ghidra_12.1.4_build, commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`. Date: 2026-10-20. Product: Opcodex 1.0.0-alpha.1.

All paths below are modified upstream files and carry an `OPCODEX:` marker. Original headers remain intact. The original stable LICENSE, NOTICE, dependency notices, engine, decompiler, loader, processor and program database implementation remain unchanged. New files live under opcodex/, docs/, .github/ and original-brand resource paths; their notices supplement upstream licensing.

| Upstream file | Modification |
|---|---|
| `Ghidra/Configurations/Public_Release/src/main/resources/UserAgreement.html` | Product document title; preserve upstream agreement and usage terms. |
| `Ghidra/Configurations/Public_Release/src/main/resources/defaultTools/CodeBrowser.tool` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Configurations/Public_Release/src/main/resources/splash.txt` | Add independent-product attribution while preserving original license text. |
| `Ghidra/Features/Base/data/base.icons.theme.properties` | Map existing application icon IDs to original transparent light/dark artwork. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/ArchiveDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/RestoreDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/runtimeinfo/VersionInfoPanel.java` | Themed original splash identity and separate product/engine versions; remove upstream decorative strips. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/GhidraApplicationInformationDisplayFactory.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/InfoPanel.java` | Themed original splash identity and separate product/engine versions; remove upstream decorative strips. |
| `Ghidra/Framework/Docking/data/docking.theme.properties` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Docking/src/main/java/docking/help/HelpManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FileActionManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndPlugin.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndTool.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/UserAgreementDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/wizard/project/SelectProjectPanel.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/plugintool/PluginTool.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/application.properties` | Product name, release identity and independent version; engine/API version remains 12.1.4. |
| `README.md` | Replace upstream project marketing/install instructions with Opcodex status, downloads, build, releases and attribution. |
| `build.gradle` | Apply isolated Opcodex packaging/test tasks; retain upstream distribution tasks. |

## Verification and release evidence

See [docs/releases/VERIFICATION.md](docs/releases/VERIFICATION.md) for exact commands/results and [release notes](docs/releases/1.0.0-alpha.1.md) for scope. Commit identities for this change series are in Git history and the release build manifests. No successful build, platform launch or published release is implied by this inventory.

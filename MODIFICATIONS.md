<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex modifications

Baseline: Ghidra_12.1.4_build, commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`. Date: 2026-10-20. Product: Opcodex 1.0.0-alpha.2.

All paths below are modified upstream files and carry an `OPCODEX:` marker (text or embedded image metadata). Original headers remain intact. The original stable LICENSE, NOTICE, dependency notices, engine, decompiler, loader, processor and program database implementation remain unchanged. New files live under opcodex/, docs/, .github/, ui-assets/ and original-brand UI resource/class paths; their notices supplement upstream licensing.

| Upstream file | Modification |
|---|---|
| `Ghidra/Framework/Docking/build.gradle` | Declare the existing audited JNA version for Windows shell file selection. |
| `Ghidra/Framework/Docking/Module.manifest` | Retain Apache 2.0 dependency notices for native picker support. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/filechooser/GhidraFileChooser.java` | Use Windows Common Item Dialog for supported local files and folders; preserve virtual models, custom filters and cancellation. |
| `Ghidra/Framework/Gui/src/main/java/ghidra/util/filechooser/GhidraFileFilter.java` | Optional exact native filter patterns; existing filters retain custom-model behavior. |
| `Ghidra/Framework/Gui/src/main/java/ghidra/util/filechooser/ExtensionFileFilter.java` | Export exact extension patterns for the native picker. |
| `Ghidra/Framework/Docking/src/main/java/docking/framework/AboutDialog.java` | Flat About surface with consistent dialog padding. |
| `Ghidra/Framework/Docking/src/main/java/docking/DialogComponentProvider.java` | Padded, flat dialog surfaces in product themes; retain dialog behavior. |
| `Ghidra/Framework/Docking/src/main/java/docking/SplitPanel.java` | Single hairline divider with hover feedback and a wider drag target; retain docking geometry. |
| `Ghidra/Framework/Docking/src/main/java/ghidra/util/task/ChompingBitsAnimationPanel.java` | Replace the animated mascot with an indeterminate progress bar; retain the public compatibility class and task lifecycle. |
| `Ghidra/Framework/Docking/src/main/java/docking/ComponentNode.java` | Flat pane boundaries in product themes; retain original borders in other themes. |
| `Ghidra/Framework/Docking/src/main/java/docking/StatusBar.java` | Quiet status strip with readable spacing instead of sunken boxes. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/tabbedpane/DockingTabRenderer.java` | Compact vector close controls and text-only provider tabs for product themes. |
| `Ghidra/Framework/Docking/src/main/java/docking/ComponentPlaceholder.java` | Short navigator tab labels; preserve technical provider names and persisted identity. |
| `Ghidra/Framework/Docking/src/main/java/docking/menu/MultipleActionDockingToolbarButton.java` | Preserve dropdown actions with flat chrome and vector disabled icons. |
| `Ghidra/Framework/Docking/src/main/java/docking/menu/ToolBarManager.java` | Focused toolbars, vector action sizing and flat panel chrome; preserve command dispatch and docking identities. |
| `Ghidra/Framework/Docking/src/main/java/docking/menu/ToolBarItemManager.java` | Focused toolbars, vector action sizing and flat panel chrome; preserve command dispatch and docking identities. |
| `Ghidra/Framework/Docking/src/main/java/docking/DockableToolBarManager.java` | Focused toolbars, vector action sizing and flat panel chrome; preserve command dispatch and docking identities. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/EmptyBorderButton.java` | Focused toolbars, vector action sizing and flat panel chrome; preserve command dispatch and docking identities. |
| `Ghidra/Framework/Docking/src/main/java/docking/GenericHeader.java` | Focused toolbars, vector action sizing and flat panel chrome; preserve command dispatch and docking identities. |
| `.github/ISSUE_TEMPLATE/bug_report.md` | Provide Opcodex contribution and issue guidance with this repository as the destination. |
| `.github/ISSUE_TEMPLATE/feature_request.md` | Provide Opcodex contribution and issue guidance with this repository as the destination. |
| `.github/ISSUE_TEMPLATE/question.md` | Provide Opcodex contribution and issue guidance with this repository as the destination. |
| `CONTRIBUTING.md` | Provide Opcodex contribution and issue guidance with this repository as the destination. |
| `Ghidra/Configurations/Public_Release/src/main/resources/UserAgreement.html` | Product document title; preserve upstream agreement and usage terms. |
| `Ghidra/Configurations/Public_Release/src/main/resources/defaultTools/CodeBrowser.tool` | Product-facing identity and focused default workspace; retain technical classes, help anchors and data formats. |
| `Ghidra/Configurations/Public_Release/src/main/resources/splash.txt` | Use a concise product-only About summary; complete original terms and attribution remain in the included LICENSE, NOTICE and third-party notices. |
| `Ghidra/Debug/Debugger/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Debug/Debugger/src/main/resources/images/debugger32.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Debug/Debugger/src/main/resources/images/emulator32.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Features/Base/data/base.icons.theme.properties` | Map common action/tree IDs to original transparent light/dark icons; preserve semantic IDs and aliases. |
| `Ghidra/Features/Base/src/main/help/help/TOC_Source.xml` | Rebrand visible help tree labels; preserve all technical IDs, anchor targets and links. |
| `Ghidra/Features/Base/src/main/help/help/topics/Misc/Welcome_to_Ghidra_Help.htm` | Use product-facing help-home text and original identity while retaining existing anchors. |
| `Ghidra/Features/Base/src/main/java/ghidra/GhidraRun.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/ArchiveDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/RestoreDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/totd/TipOfTheDayPlugin.java` | Make the workspace the first-run surface; preserve saved tip preferences and Help access. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/runtimeinfo/VersionInfoPanel.java` | Show product version and technical engine compatibility without an engine-brand headline. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/GhidraApplicationInformationDisplayFactory.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/InfoPanel.java` | Use original themed identity and product-only version presentation; retain complete legal files. |
| `Ghidra/Features/Base/src/main/resources/defaultTools/images/greenDragon24.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/src/main/resources/defaultTools/images/red-dragon.gif` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/src/main/resources/images/GHIDRA_1.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/src/main/resources/images/GHIDRA_2.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/src/main/resources/images/GHIDRA_3.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Features/Base/src/main/resources/images/GHIDRA_Splash.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Docking/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Framework/Docking/data/docking.theme.properties` | Map common action/tree IDs to original transparent light/dark icons; preserve semantic IDs and aliases. |
| `Ghidra/Framework/Docking/src/main/java/docking/help/HelpManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Gui/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Framework/Gui/data/gui.theme.properties` | Map common action/tree IDs to original transparent light/dark icons; preserve semantic IDs and aliases. |
| `Ghidra/Framework/Gui/src/main/java/generic/theme/ThemeManager.java` | Use Opcodex Dark for new profiles; saved theme preferences retain precedence. |
| `Ghidra/Framework/Gui/src/main/java/generic/theme/laf/FlatLookAndFeelManager.java` | Install product-theme sizing after FlatLaf loading and before component refresh. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon128.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon16.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon24.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon256.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon32.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon40.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon48.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Gui/src/main/resources/images/GhidraIcon64.png` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/Framework/Project/data/project.theme.properties` | Map common action/tree IDs to original transparent light/dark icons; preserve semantic IDs and aliases. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/AcceptUrlContentTask.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/EditPluginPathDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FileActionManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndPlugin.java` | Add welcome/project cards using existing Create/Open commands, cleaner spacing and product wording. |
| `Ghidra/Framework/Project/src/main/java/ghidra/app/plugin/gui/ThemeManagerPlugin.java` | Product-facing theme descriptions; retain plugin identifiers and theme commands. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndTool.java` | Use product wording; new profiles start in the workspace while saved startup preferences remain respected. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/ImportGhidraToolsDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/ProjectToolBar.java` | Use a quiet inset and labelled Code Browser button without changing tool launch behavior. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/RepositoryChooser.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/ToolActionManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/UserAgreementDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/projectdata/actions/VersionControlAction.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/wizard/project/SelectProjectPanel.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/plugintool/PluginTool.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/plugintool/dialog/PluginManagerComponent.java` | Display Opcodex Core while preserving the persisted plugin-package identifier. |
| `Ghidra/RuntimeScripts/Windows/support/ghidra.ico` | Replace inherited product/tool artwork with original Opcodex exports; retain legacy filenames for callers and embed modification metadata. |
| `Ghidra/RuntimeScripts/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/application.properties` | Product name, release identity and independent version; engine/API version remains 12.1.4. |
| `README.md` | Replace upstream project marketing/install instructions with Opcodex status, downloads, build, releases and attribution. |
| `build.gradle` | Apply isolated Opcodex packaging/test tasks; retain upstream distribution tasks. |

## Verification and release evidence

See [docs/releases/VERIFICATION.md](docs/releases/VERIFICATION.md) for exact commands/results and [release notes](docs/releases/1.0.0-alpha.1.md) for scope. Commit identities for this change series are in Git history and the release build manifests. No successful build, platform launch or published release is implied by this inventory.

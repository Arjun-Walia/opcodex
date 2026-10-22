<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex modifications

Baseline: Ghidra_12.1.4_build, commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`. Date: 2026-10-20. Product: Opcodex 1.0.0-alpha.2.

All paths below are modified upstream files and carry an `OPCODEX:` marker (text or embedded image metadata). Original headers remain intact. The original stable LICENSE, NOTICE, dependency notices, engine, decompiler, loader, processor and program database implementation remain unchanged. New files live under opcodex/, docs/, .github/, ui-assets/ and original-brand UI resource/class paths; their notices supplement upstream licensing.

| Upstream file | Modification |
|---|---|
| `Ghidra/Features/FunctionGraph/src/main/java/ghidra/app/plugin/core/functiongraph/graph/vertex/ListingGraphComponentPanel.java` | Flat themed node boundaries; retain graph interactions, listing content and grouping. |
| `Ghidra/Features/FunctionGraph/src/main/java/ghidra/app/plugin/core/functiongraph/graph/vertex/GroupedFunctionGraphComponentPanel.java` | Flat themed node boundaries; retain graph interactions, listing content and grouping. |
| `Ghidra/Framework/Docking/src/test/java/docking/widgets/tab/GTabPanelTest.java` | Keep overflow tests deterministic across product fonts by sizing from the tab strip. |
| `Ghidra/Framework/Help/src/test/java/help/HelpBuildUtilsTest.java` | Assert that runtime help references resolve the current refresh artwork. |
| `Ghidra/Framework/Docking/build.gradle` | Declare the existing audited JNA version for Windows shell file selection. |
| `Ghidra/Framework/Docking/Module.manifest` | Retain Apache 2.0 dependency notices for native picker support. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/filechooser/GhidraFileChooser.java` | Use Windows Common Item Dialog for supported local files and folders; preserve virtual models, custom filters and cancellation. |
| `Ghidra/Framework/Gui/src/main/java/ghidra/util/filechooser/GhidraFileFilter.java` | Optional exact native filter patterns; existing filters retain custom-model behavior. |
| `Ghidra/Framework/Gui/src/main/java/ghidra/util/filechooser/ExtensionFileFilter.java` | Export exact extension patterns for the native picker. |
| `Ghidra/Framework/Docking/src/main/java/docking/framework/AboutDialog.java` | Flat About surface with consistent dialog padding. |
| `Ghidra/Framework/Docking/src/main/java/docking/DialogComponentProvider.java` | Padded, flat dialog surfaces in product themes; retain dialog behavior. |
| `Ghidra/Framework/Docking/src/main/java/docking/SplitPanel.java` | Visible resize grips, hover feedback and a 10px drag target; retain docking geometry and support equal sizes on double-click. |
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
| `Ghidra/Features/Base/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Features/Base/data/base.icons.theme.properties` | OPCODEX: retain stable classic artwork mappings without affecting modern theme overrides. |
| `Ghidra/Features/Base/src/main/help/help/TOC_Source.xml` | Rebrand visible help tree labels; preserve all technical IDs, anchor targets and links. |
| `Ghidra/Features/Base/src/main/help/help/topics/Misc/Welcome_to_Ghidra_Help.htm` | Use product-facing help-home text and original identity while retaining existing anchors. |
| `Ghidra/Features/Base/src/main/java/ghidra/GhidraRun.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/ArchiveDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/archive/RestoreDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/totd/TipOfTheDayPlugin.java` | Make the workspace the first-run surface; preserve saved tip preferences and Help access. |
| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/runtimeinfo/VersionInfoPanel.java` | Show product version and technical engine compatibility without an engine-brand headline. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/GhidraApplicationInformationDisplayFactory.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Features/Base/src/main/java/ghidra/framework/main/InfoPanel.java` | Use original themed identity and product-only version presentation; retain complete legal files. |
| `Ghidra/Framework/Docking/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Framework/Docking/data/docking.theme.properties` | OPCODEX: preserve the original classic icon mappings; modern theme bindings remain isolated. |
| `Ghidra/Framework/Docking/src/main/java/docking/help/HelpManager.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Gui/certification.manifest` | Register original Apache-2.0 artwork and theme assets in the distribution license audit. |
| `Ghidra/Framework/Gui/data/gui.theme.properties` | OPCODEX: restore the original stable theme defaults for Classic; modern themes explicitly bind their own icons and colors. |
| `Ghidra/Framework/Gui/src/main/java/generic/theme/ThemeManager.java` | Use Opcodex Dark for new profiles; saved theme preferences retain precedence. |
| `Ghidra/Framework/Gui/src/main/java/generic/theme/laf/FlatLookAndFeelManager.java` | Install product-theme sizing after FlatLaf loading and before component refresh. |
| `Ghidra/Framework/Project/data/project.theme.properties` | OPCODEX: preserve original Classic resource mappings alongside independent modern bindings. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/AcceptUrlContentTask.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/EditPluginPathDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FileActionManager.java` | Product-facing identity and New/Open/Save Project toolbar access using the existing commands; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndPlugin.java` | Add welcome/project cards using existing Create/Open commands, cleaner spacing and product wording. |
| `Ghidra/Framework/Project/src/main/java/ghidra/app/plugin/gui/ThemeManagerPlugin.java` | Product-facing theme descriptions; retain plugin identifiers and theme commands. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/FrontEndTool.java` | Use product wording; new profiles start in the workspace while saved startup preferences remain respected. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/ImportGhidraToolsDialog.java` | Product-facing desktop/menu/dialog identity; retain technical classes, help anchors and data formats. |
| `Ghidra/Framework/Project/src/main/java/ghidra/framework/main/ProjectToolBar.java` | Use a quiet inset and consistently sized, labelled startup-tool tiles without changing launch behavior. |
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
| `Ghidra/Framework/Gui/src/main/java/resources/ResourceManager.java` | OPCODEX: adapt explicitly registered built-in filenames to live vectors while preserving URL identity, dimensions, user images and other themes; preserve vector scaling and disabled opacity through ImageIcon APIs. |
| `Ghidra/Framework/Gui/src/main/java/resources/icons/ReflectedIcon.java` | OPCODEX: paint reflected original vectors at device scale, retaining legacy reflection behavior for other images. |
| `Ghidra/Features/FunctionGraph/src/main/java/ghidra/app/plugin/core/functiongraph/graph/vertex/SetVertexMostRecentColorAction.java` | OPCODEX: replace the raised color swatch with an outline palette and current-color underline, keeping every color action. |

| `Ghidra/Configurations/Public_Release/certification.manifest` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/help/help/topics/GhidraScriptMgrPlugin/GhidraScriptMgrPlugin.htm` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/console/ConsoleComponentProvider.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/navigation/ProviderNavigationPlugin.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/progmgr/AbstractUndoRedoAction.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/references/ExternalReferencesProvider.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/scalartable/ScalarSearchDialog.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/strings/DefinedStringsProvider.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/app/plugin/core/totd/TipOfTheDayDialog.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/java/ghidra/plugins/importer/tasks/ImportBatchTask.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/Base/src/main/resources/ghidra/app/plugin/core/totd/tips.txt` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/DecompilerDependent/src/main/help/help/topics/DecompilerTaint/DecompilerTaint.html` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/PyGhidra/src/main/java/ghidra/pyghidra/PyGhidraScriptProvider.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Features/PyGhidra/src/main/java/ghidra/pyghidra/interpreter/PyGhidraInterpreter.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/DetachedWindowNode.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/DockingWindowManager.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/RootNode.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/theme/gui/ThemeValueEditor.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/widgets/filter/FilterTextField.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/widgets/table/GTableFilterPanel.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/widgets/tree/GTree.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/FileSystem/src/main/java/ghidra/framework/store/local/LocalFileSystem.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Gui/src/main/java/generic/theme/ApplicationThemeManager.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Gui/src/main/java/generic/theme/ThemePreferences.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Project/src/main/java/ghidra/framework/data/DefaultProjectData.java` | OPCODEX: address release-test findings in presentation and interaction while retaining original APIs, feature registration and license headers. |

| `Ghidra/Framework/Docking/src/main/java/docking/widgets/filter/FilterOptions.java` | OPCODEX: distinguish tree/category filter controls from table filter controls; preserve match mode and inversion badges. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/tree/DefaultGTreeFilterProvider.java` | OPCODEX: distinguish tree/category filter controls from table filter controls; preserve match mode and inversion badges. |
| `Ghidra/Framework/Docking/src/main/java/docking/widgets/tree/GTreeFilterFactory.java` | OPCODEX: distinguish tree/category filter controls from table filter controls; preserve match mode and inversion badges. |

| `Ghidra/Framework/Gui/src/main/java/generic/util/image/ImageUtils.java` | OPCODEX: keep themed/vector sources live through transparent icon transforms; preserve opacity and original bitmap behavior. |

## Verification and release evidence

See [docs/releases/VERIFICATION.md](docs/releases/VERIFICATION.md) for exact commands/results and [release notes](docs/releases/1.0.0-alpha.1.md) for scope. Commit identities for this change series are in Git history and the release build manifests. No successful build, platform launch or published release is implied by this inventory.

| `Ghidra/Framework/Docking/src/main/java/docking/framework/ApplicationInformationDisplayFactory.java` | OPCODEX: refresh native window branding and improve command hover labels and pointer affordances. |

| `Ghidra/Framework/Docking/src/main/java/docking/menu/DockingToolBarUtils.java` | OPCODEX: refresh native window branding and improve command hover labels and pointer affordances. |

| `Ghidra/Framework/Docking/src/main/java/docking/widgets/button/GButton.java` | OPCODEX: consistent pointer cursors for enabled welcome and dialog buttons. |

| `Ghidra/Debug/Debugger/src/main/java/ghidra/app/plugin/core/debug/gui/breakpoint/DebuggerBreakpointsProvider.java` | OPCODEX: distinguish location-filter controls while preserving matching and inversion settings. |

| `Ghidra/Framework/Docking/src/main/java/docking/help/DockingHelpBroker.java` | OPCODEX: resolve current theme branding for newly opened help windows. |

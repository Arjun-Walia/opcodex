# Release-test corrections

The external Linux audit tested alpha.2 source `56e307ed43`. The following corrections are being verified against a new build. This document does not claim a published or installable release.

| Finding | Correction | Verification |
| --- | --- | --- |
| OPC-001, OPC-002 | Explicit paired selection colors for menus, menu titles, lists, tables, trees, text fields and editors. | Real FlatLaf normalization contrast tests pass; native live-color checks pass in the isolated development preview. |
| OPC-003 (OPC-004 withdrawn) | Every existing global and provider toolbar action is visible by default. Compact mode remains optional. | Direct-action and split-control tests pass; nine native provider checks pass in the isolated development preview. |
| OPC-005 | Preserve expansion, selection and scroll state when themed tree renderers refresh. | Native expanded-path round trip passes in the development preview. |
| OPC-006 | Runtime Information reads the packaged source revision and uses the product development-mode label. | Built-app check pending. |
| OPC-007 | Remove panel-title aliases so headers and tabs use the original provider names from the Window menu. | Nine native panel checks pass in the development preview. |
| OPC-008 | Restore Function Signature and Function Size among the four default columns. | Default-resource regression test passes. Existing saved column preferences remain respected. |
| OPC-009, OPC-011 | Correct product wording in tips, project locks, save conflicts and filename validation. | License text and technical identifiers retained. |
| OPC-010 | Add selection-aware Console Copy and the platform keyboard shortcut. | Native context-menu check pending. |
| OPC-012 | Correct product prose in Script Manager and Decompiler Taint help; preserve URLs, anchors and API/integration names. | Help build passes. |
| OPC-013 | Valid compact agreement document body and reduced dialog padding; legal wording unchanged. | First-run native capture confirms the legal text appears together without the spurious inter-paragraph gap. |
| OPC-014 | Give the serialized Function Tags floating window a usable initial size. | Native check pending. |
| OPC-015 | Restore a distinct filter tint and add a persistent warning outline plus accessible no-results description. | Native filter check pending. |
| OPC-016 | Size Scalar Search from its controls so Search Scope fits. | Native check pending. |
| OPC-017 | Preserve full IDs and update the title when reusing the color editor. The reported first-open missing dot was unverified in the supplied audit. | Exact-title native check pending. |
| OPC-018 | Increase Dark selected-row fill contrast while retaining black canvas surfaces. | Contrast and native live-color tests pass; final installed-build review remains pending. |

## Themes and responsiveness

Bundled choices are Opcodex Dark, Opcodex Light and Ghidra Classic. Opcodex Light is the default. Retired built-in preferences migrate to Light; explicit Classic selections remain Classic. User-imported themes and the theme editor remain supported. Classic uses the original stable palette/resource mappings and original CodeBrowser docking layout; other tools already retain their stable layouts. Modern and Classic arrangements are retained separately in saved tool state. Dark/Light switching preserves positions without changing layout.

Icon measurement uses fixed logical dimensions; vector painting avoids bitmap decoding. Transparent themed icons preserve their live source instead of flattening to a small bitmap. Raw-resource vector painters and solid strokes are reused. The native smoke records actual event-dispatch response samples after settling, without promising a frame rate on untested hardware.

## Inherited findings

The audit identifies INH-01 through INH-16 as also present in stock 12.1.4. Display clamping, lock-path joining, batch-import progress, default strings viewport size, Undo/Redo operation labels and the default Ctrl+F6 collision have corrections in this branch. External Programs and Python integration messages use product wording; the technical PyGhidra integration name remains intact.

Concurrent application sessions, context-relevant action filtering, deferred debugger-service messages, the hidden splash owner, stale recent-project cleanup, and the decompiler's independent interpretation of code bytes still need individual disposition and verification. Clearing listing definitions does not erase underlying bytes; changing the analysis engine to force an empty decompiler is outside this presentation change. No crash, corruption or data-loss finding was reported in the supplied audit.

## Distinct panel icons

A real-panel inventory identified repeated geometry for different actions. The family now includes distinct console clearing, graph snapshots, listing/decompiler/instruction views, selection/highlight navigation, register management, API reference, editor integration, rename and rerun symbols. Category filtering differs from script/table filtering while retaining matching-mode and inversion indicators. The final development-preview inventory passes in both Light and Dark: no duplicate command symbols or flattened bitmap command icons across the complete main toolbar and nine principal panels. The native process exits successfully; final installed-build validation remains pending.

## Release gate

The previous Windows CI failure came from requesting build-only runtime modules from an already linked Temurin JDK. The app-image runtime now retains every available module except `jdk.jlink` and `jdk.jpackage`.

The broad Linux suite remains a separate release gate: the prior run includes missing external program fixtures (BSim and CodeCompare), a missing compiled HelloWorld fixture, a debugger breakpoint-state failure and a timeout. No tests are excluded and no failures are ignored by the release verifier. Final build, installation, merge and publication are pending.

## Single-row and Linux follow-up

Modern workspace and provider toolbars use one horizontal command strip with visible scroll controls when necessary. Actual original action/split buttons remain mounted; wheel scrolling and focus tracking expose off-screen commands. The Go To control is compact. Panel headers retain their title and window controls on the same line. Enabled commands use pointer cursors. Tooltips supplement the action name with its description and assigned shortcut, wrap long descriptions and stay visible longer.

Light is the first-run/retired-theme default. The native branding listener follows the real theme manager after bootstrap and resubmits images to existing window handles; new help windows also resolve current branding. Explicit Classic settings and its original assets/layout remain supported.

The expanded Debugger inventory separates clear-all/disable-all, attributes/elements and selected-breakpoint filtering. Location-filter matching modes have distinct artwork and retain inversion badges. Linux packaging adds a verified native Debian installer with its runtime, compiler/debugger modules, original notices and application-menu integration. Its installed desktop harness uses an isolated profile and a compiled ELF analysis fixture; Windows-only native-picker checks remain Windows-only. Full Linux release-suite failures remain blocking.

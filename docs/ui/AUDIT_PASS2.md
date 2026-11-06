# Pass 2 follow-up and workspace redesign

Updated 2026-11-04. Input: `AUDIT-pass2-a71cc37.md`, supplied by the project owner. This is the implementation record, not a replacement for the independent tester's evidence.

## Tested baseline

The tester installed the Linux Debian package built from `a71cc371737bbb80588046a53623f0ab26022ec5`, on Debian 13, Xvfb 1920×1200 at 100%. They recorded 76 passes, eight Opcodex defects, seven inherited failures, two blocked checks and two checks not run. No crash, data loss or corruption was observed in the exercised workflows. This was a focused retest, not a complete product certification. Windows identity and a second scale were not tested there.

## Corrections in the follow-up source

“Implemented” below means changed in source. Exact installed source `4bdb269187113c7e3fca6385b6cdcba0451601a3` passed the complete desktop harness on Windows 11 and Ubuntu 24.04/OpenJDK 21, at normal scale and with a Java 200% scale override. The next follow-up adds the tabbed ribbon, complete command finder, Debugger workflow/default layout and actionable no-trace view, and removes an empty detached-toolbar row. Its rebuilt packages require their own checks.

| Finding | Change | Verification required |
| --- | --- | --- |
| OPC-006 — missing revision | Packages stamp a canonical source revision independent of checkout-directory names. Shared lookup also accepts product and numbered checkout keys, with explicit development fallback. | Unit cases for numbered keys and product override; installed Runtime Information. |
| OPC-019 — error-report version | Error details identify the Opcodex version, engine compatibility and source revision separately. | Trigger an error and inspect/copy Details. |
| OPC-020 — selected shortcut contrast | Explicit selected accelerator foreground for ordinary, checkbox and radio menu items. | Real FlatLaf contrast test and hovered menu inspection in both themes. |
| OPC-021 — Linux identity | Product entry point `opcodex.Opcodex` produces X11 class `opcodex-Opcodex`; Debian desktop template includes matching StartupWMClass. No reflective JDK access. | Build/install unchanged Debian package and query X11 WM_CLASS. |
| OPC-013 — agreement | Named native title and 800-wide initial dialog measured from wrapped legal text; legal paragraphs retained. | Fresh profile, both paragraphs visible at normal and enlarged scale. |
| OPC-022 — current row | Stronger current-line band in both modern themes; distinct from multi-address selection. | Click one Listing row, then separately drag-select addresses. |
| OPC-023 — chooser sidebar | Theme-bound sidebar surface; flat left-aligned places with focus outlines, consistent row sizing and pointer state. | Linux Light chooser and theme round trip; native Windows picker remains supported. |
| OPC-024 — help layout | Modern Help opens at a usable reading size bounded by the display; navigation/document split has explicit minimums. | F1 from Script Manager, resize TOC, verify full left edge and document. |

An additional cursor issue was found during source review: AWT's native cursor manager bypasses overridden `getCursor()`. Common command buttons now set the actual cursor property on enablement and look-and-feel changes. The desktop probe checks that the property is explicitly set.

Linux no longer forces `sun.java2d.uiScale=1`; it honors display DPI. A deliberate override remains possible with `JAVA_TOOL_OPTIONS=-Dsun.java2d.uiScale=2`.

## Workspace changes beyond color

Modern CodeBrowser/Debugger/Emulator gain a dedicated left navigation rail. It opens existing providers, focuses their original contents and offers a searchable All panels picker. It does not instantiate replacement analysis views or remove Window-menu actions. Distinct vector glyphs identify Listing, Functions, Symbols, Types, Graph, Scripts and Console where those providers exist.

Provider surfaces have a quiet boundary and inset contents. Panel titles receive up to 40% of header width (capped at 420 logical pixels); their real actions remain on the same scrollable row. All saved docking geometry, detached windows, adjustable dividers and original plugin registrations remain in use. Classic hides the rail and retains its separately saved stock layout/assets. Dark/Light switches preserve the modern arrangement.

## Remaining coverage and release limits

OPC-011 A (Save Tool conflict) was not reproduced by the tester; OPC-011 B (lock dialog) and Types-tree expansion were not retested. Inherited INH-02 and INH-17 through INH-23 retain their original findings until individually verified. The audit did not cover VT sessions, BSim, archive/restore, stress or a full keyboard-only workflow. Do not mark these as passed based on an icon or palette change.

The engine remains pinned to 12.1.4. Upstream 12.2 includes archive extraction and server/security changes (GP-7332, GP-7132, GP-7206, GP-6829), confirmed in the [official change history](https://github.com/NationalSecurityAgency/ghidra/blob/Ghidra_12.2_build/Ghidra/Configurations/Public_Release/src/global/docs/ChangeHistory.md). They are **not included or claimed backported** here. An engine/security update requires separate compatibility review before shipment.

Native packaging and focused UI tests do not establish that the complete upstream test suite passes. Existing missing external program fixtures and broad Linux failures remain release gates; no test has been excluded to conceal them. See [release verification](../releases/VERIFICATION.md).

Public documentation lives on [the website](https://opcodex.xyz/docs/); the Obsidian project notes record internal installation/build evidence and handoff state. Raw tester screenshots were not supplied with this audit; their IDs are references to the tester's evidence, not locally verified captures.

Additional visual follow-up: Help commands/navigation tabs and local chooser file rows use the live vector family; Help typography uses a sans-serif reading hierarchy in modern themes. Classic retains its assets and original stylesheet. Help styles are applied before parsing the page to avoid late view-structure changes. An uncaught-EDT exception now fails the desktop harness rather than being lost behind a successful result.

Website documentation was published to the website main branch at `80d8970` and the live release-status route returned HTTP200 with the new independent-audit section on 2026-11-04. Local website checks: 40 pages, zero Astro diagnostics,6 content tests,9 browser tests including the accessibility matrix. This does not publish a desktop artifact.

Detached-panel follow-up: toolbar creation now returns a zero-height component when there are no actions. Script Manager keeps its real provider commands without a second empty window-toolbar row. A regression case covers both local and window toolbars; native panel checks also reject overflow-only strips. The Linux CI desktop check now starts its own Openbox inside disposable Xvfb, so real hover/activation behavior is tested in a window-managed desktop. No product or upstream test assertion was relaxed.

## Product workflow follow-up

The compact ribbon now has Home, Navigate, Analyze, Panels, Debug where applicable, and All tools. Home is deliberately concise; All tools retains every original toolbar button, and the original menus remain. This replaces the earlier default that displayed the whole flat toolbar. One icon row per ribbon page remains required. Search stays visible and indexes names, menu paths, shortcuts and plain descriptions once per dialog; it deduplicates entry points and retains unavailable actions with an explanation. Per-action contexts are captured through the original docking manager, and original action execution is retained.

The modern Debugger starts with session/execution commands and a layout for Connections, code, state, output and threads/stack. All 51 stock default provider identities remain, and the original Debugger.tool is unchanged for Classic. Customized saved layouts are preserved; the modern reset/previous controls also support Debugger. A focus fallback prevents theme changes from activating an unintended tab. Native size/divider rounding is allowed within two persisted units, while provider identity, visibility and selected tabs remain strict.

The Dynamic Listing explains the no-trace state and offers launcher search and Connections. A temporary in-memory trace in the desktop harness confirms that this overlay disappears on actual trace activation and returns on close; it does not run a binary or claim a live backend test. The existing listing and comparison contents remain mounted. The visible API Reference name replaces the old product-prefixed Help command while retaining its original target.

Development run `build/smoke-tabbed-ribbon2` passed the complete Windows desktop harness, including actual Function Graph execution from search, the trace-state transitions, theme/layout round trips, full original command/menu continuity and zero uncaught UI exceptions. Settled dispatch: 80 samples, P95 0.1582ms and maximum 3.8263ms. These are development-overlay results, not final installer evidence. The first oversized caption ribbon was rejected during visual review and replaced by the compact task-tab design. See [product direction](PRODUCT_DIRECTION.md) for the cross-tool contract.

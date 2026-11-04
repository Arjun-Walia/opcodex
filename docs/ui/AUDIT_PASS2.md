# Pass 2 follow-up and workspace redesign

Updated 2026-11-04. Input: `AUDIT-pass2-a71cc37.md`, supplied by the project owner. This is the implementation record, not a replacement for the independent tester's evidence.

## Tested baseline

The tester installed the Linux Debian package built from `a71cc371737bbb80588046a53623f0ab26022ec5`, on Debian 13, Xvfb 1920×1200 at 100%. They recorded 76 passes, eight Opcodex defects, seven inherited failures, two blocked checks and two checks not run. No crash, data loss or corruption was observed in the exercised workflows. This was a focused retest, not a complete product certification. Windows identity and a second scale were not tested there.

## Corrections in the follow-up source

“Implemented” below means changed in source. Exact packaged Windows/Linux retests remain required before these can be called release-verified fixes.

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

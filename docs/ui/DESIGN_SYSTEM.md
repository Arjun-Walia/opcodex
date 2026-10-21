<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex design system

A quiet, dense workbench for repeated reverse-engineering workflows. The program and analysis views remain the focus. Graphite and neutral light surfaces carry a small teal accent; status colors retain semantic meaning. No new animation or runtime icon dependency is introduced.

## Icon family

Original **outline** glyphs on a 24×24 grid, 2px rounded strokes and open interiors. Main geometry stays within the icon bounds; arrows and small node glyphs use optical adjustments. Monochrome controls use neutral ink; warning, error and confirmation use semantic colors. Do not use color alone to distinguish an action.

67 glyphs map to 163 existing IDs. Sources and the ID mapping live in [catalog.json](../../ui-assets/icons/catalog.json). Exports at 16/20/24/32/48/64px support the sizes actually used in the toolbar and trees. Product themes paint original Java2D geometry directly at the current display transform; disabled and sized wrappers preserve that painting. Multi-resolution images remain available for consumers that require images and for theme export. No SVG parser is bundled. Lazy geometry is generated with development-only FontTools (MIT).

Existing semantic IDs and legacy raw filenames remain compatible. Fallback product artwork and the Debugger/Emulator tool icons are replaced by original art. Specialized unconverted action icons remain outside this pilot; this is not a claim to have replaced all 878 IDs in the initial inventory.

See the [rendered catalog](ICON_CATALOG.md). Asset export uses Sharp/libvips as development tools; neither is added to the app runtime.

## Color tokens

| Role | Opcodex Light | Opcodex Dark |
|---|---|---|
| Canvas | #fbfcfd | #181a1d |
| Controls / surface | #f1f3f5 | #202327 |
| Text | #252930 | #e8eaed |
| Secondary text | #5d6672 | #a7aeb8 |
| Border | #a6afba | #454c56 |
| Accent / focus | #087582 | #75d9de |
| Selection background | #d7edf0 | #273e43 |
| Error | #b64248 | #f18b91 |
| Warning | #8a5a0c | #edbd6c |
| Keyword / mnemonic | #245b9c | #8bbcec |
| Comment | #606a76 | #a1a8b3 |
| Type / parameter | #714796 | #d2b8ee |
| Constant | #7a5313 | #eac286 |

The implementation uses theme IDs, not hardcoded per-component painting. Panel, toolbar, menu, title and tab surfaces explicitly reference the control token so shared FlatLaf default RGB values cannot accidentally bind them to the border group. A regression test exercises the real FlatLaf normalization for both themes. `OpcodexThemeTest` measures the actual theme colors: primary/control/selection/secondary text and representative syntax meet at least 4.5:1; focus against the control surface meets at least 3:1. This is token-level verification, not a complete accessibility certification of every specialized view.

Selection is quiet, with readable text; active docking headers and tabs use matched theme tokens. Existing listing/decompiler font settings remain configurable. This pilot covers common chrome and representative syntax; detailed graph/debugger palettes remain later work.

## Typography and spacing

UI: Segoe UI when available, with native/logical fallbacks, 13pt. Code: Cascadia Mono, Consolas, Menlo or DejaVu Sans Mono when available, with a logical monospaced fallback, 13pt. Users can keep their own font/theme preferences.

Use 4px increments for layout with 6px optical control padding where needed: 12px workspace inset, 8px group gaps, 12px action gaps, 24px tree/table defaults and 28px minimum regular buttons. Toolbar buttons use 20px vector glyphs with at least 32px hit targets. Tab close controls use compact 24px targets and 16px vectors. Corners use an 8px FlatLaf default; focus remains a visible 1px ring. Specialized widgets can retain their own user-configurable sizing.

Theme-specific UI defaults install after FlatLaf loading and before component updates. Switching to another theme clears UI defaults through the existing manager, preventing product spacing from leaking into unrelated themes.

## Workspace

A tabbed navigator occupies 18.5% of the default workspace; the remaining area is split evenly between disassembly and pseudocode. The console opens on demand. The main toolbar shows saving, history, undo/redo, graph, scripts and Go To. More groups secondary actions by their existing menu, preserves original dispatch/context and offers the complete toolbar. Existing layouts are respected; More > Restore focused workspace applies the new default and retains the previous layout for reversal within that tool session. Panel headers are flat with a small focus indicator and text-only provider tabs.

## First-run behavior

New profiles start with **Opcodex Dark**. **Opcodex Light** is available in Edit > Theme > Switch. Saved themes take precedence. Follow-system behavior is deferred; it is not inferred from native window colors.

The project welcome screen provides Create Project and Open Project through the existing commands, preserving Ctrl+N/Ctrl+O and adding accessible button labels/mnemonics. Opening a project restores the existing project views. The primary Code Browser tool gets a text label; secondary tools remain compact.

## Verification boundaries

Read [release verification](../releases/VERIFICATION.md) for actual build/launch results and display-scale coverage. Theme constructors, contrast, native installation and screenshots must be checked before claiming completion. Engine code, project formats and analysis behavior are unchanged.

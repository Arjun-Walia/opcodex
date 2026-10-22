<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex design system

A quiet, dense workbench for repeated reverse-engineering workflows. The program and analysis views remain the focus. Black and neutral light surfaces carry a small teal accent; status colors retain semantic meaning. No new animation or runtime icon dependency is introduced.

## Icon family

Original **outline** glyphs on a 24×24 grid, 2px rounded strokes and open interiors. Main geometry stays within the icon bounds; arrows and small node glyphs use optical adjustments. Monochrome controls use neutral ink; warning, error and confirmation use semantic colors. Do not use color alone to distinguish an action.

144 original glyphs and composed bindings cover all 882 declared theme IDs. Sources and the ID mapping live in [catalog.json](../../ui-assets/icons/catalog.json). Exports at 16/20/24/32/48/64px support the sizes actually used in the toolbar and trees. Product themes paint original Java2D geometry directly at the current display transform; disabled and sized wrappers preserve that painting. Multi-resolution images remain available for consumers that require images and for theme export. No SVG parser is bundled. Lazy geometry is generated with development-only FontTools (MIT).

Existing semantic IDs and legacy raw filenames remain compatible. Fallback product artwork and the Debugger/Emulator tool icons are replaced by original art. All 882 declared theme IDs have paired bindings, including debugger and version-tracking actions. Empty placeholders keep their dimensions, layered status indicators keep their placement, and custom extension images remain customizable. Registered built-in raw filenames, including the four startup tools, use live vector adapters.

See the [rendered catalog](ICON_CATALOG.md). Asset export uses Sharp/libvips as development tools; neither is added to the app runtime.

## Color tokens

| Role | Opcodex Light | Opcodex Dark |
|---|---|---|
| Canvas | #fbfcfd | #000000 |
| Controls / surface | #f1f3f5 | #080808 |
| Text | #252930 | #eaeaea |
| Secondary text | #5d6672 | #a3a3a3 |
| Border | #d5d9de | #242424 |
| Accent / focus | #087582 | #75d9de |
| Selection background | #d7edf0 | #21464b |
| Error | #b64248 | #f18b91 |
| Warning | #8a5a0c | #edbd6c |
| Keyword / mnemonic | #245b9c | #8bbcec |
| Comment | #606a76 | #a1a8b3 |
| Type / parameter | #714796 | #d2b8ee |
| Constant | #7a5313 | #eac286 |

The implementation uses theme IDs, not hardcoded per-component painting. Panel, toolbar, menu, title and tab surfaces explicitly reference the control token so shared FlatLaf default RGB values cannot accidentally bind them to the border group. A regression test exercises the real FlatLaf normalization for both themes. `OpcodexThemeTest` measures the actual theme colors: primary/control/selection/secondary text and representative syntax meet at least 4.5:1; focus against the control surface meets at least 3:1. This is token-level verification, not a complete accessibility certification of every specialized view.

Selection is quiet, with readable text; active docking headers and tabs use matched theme tokens. Existing listing/decompiler font settings remain configurable. Common chrome, text panes, graph backgrounds and entry/exit/group surfaces use matched roles. Semantic analysis colors remain meaningful. Native Windows review covers nine principal panels; further specialized workflows and macOS/Linux visual review remain.

## Typography and spacing

UI: Segoe UI when available, with native/logical fallbacks, 13pt. Code: Cascadia Mono, Consolas, Menlo or DejaVu Sans Mono when available, with a logical monospaced fallback, 13pt. Users can keep their own font/theme preferences.

Use 4px increments for layout with 10px optical control padding where needed: 12px workspace inset, 8px group gaps, 12px action gaps, 24px tree/table defaults and 28px minimum regular buttons. Toolbar buttons use 20px vector glyphs with at least 32px hit targets. Tab close controls use compact 24px targets and 10px vectors. Corners use an 8px FlatLaf default; focus remains a visible 1px ring. Specialized widgets can retain their own user-configurable sizing.

Theme-specific UI defaults install after FlatLaf loading and before component updates. Switching to another theme clears UI defaults through the existing manager, preventing product spacing from leaking into unrelated themes.

## Workspace

A tabbed function list, symbols and types navigator occupies 20% of the default workspace; the remaining area is split evenly between disassembly and pseudocode. The console opens on demand. The main toolbar and provider toolbars show all original actions by default, alongside direct Go To access. More offers a compact arrangement that groups secondary actions by their existing menu and preserves original dispatch/context. Existing layouts are respected; More > Restore focused workspace applies the new default and retains the previous layout for reversal within that tool session. Panel headers are flat with a small focus indicator and text-only provider tabs. Pane frames and scroll-pane boxes are removed. Resizable dividers show one subtle line with a 10px drag target and clear hover feedback. Indeterminate tasks use a thin progress bar with their status and existing cancellation behavior; there is no mascot animation. The title area uses the theme surface directly rather than FlatLaf's unified parent background.

## First-run behavior

New profiles start with **Opcodex Dark**. **Opcodex Light** and **Ghidra Classic** are available in Edit > Theme > Switch. Classic restores the stable palette, original assets and original docking arrangement; modern and Classic arrangements are saved separately. Retired built-in theme preferences migrate to Classic; imported themes remain supported. Saved themes take precedence. Follow-system behavior is deferred; it is not inferred from native window colors.

The project welcome screen provides Create Project and Open Project through the existing commands, preserving Ctrl+N/Ctrl+O and adding accessible button labels/mnemonics. Opening a project restores the existing project views. Each startup tool has an aligned vector icon, caption and consistent tile size.

## Verification boundaries

Read [release verification](../releases/VERIFICATION.md) for actual build/launch results and display-scale coverage. Theme constructors, contrast, native installation and screenshots must be checked before claiming completion. Engine code, project formats and analysis behavior are unchanged.

### File selection

Windows local selection uses the Common Item Dialog (`IFileOpenDialog`) with native folders, multiple selection and explicit extension patterns. Shell appearance follows Windows. COM objects live on a dedicated STA thread; an invisible modal owner keeps Swing responsive and blocks unrelated application actions. Cancellation returns no files. New names remain supported, with existing callers retaining overwrite and format validation. Custom models, subclasses, mixed file/directory selection and arbitrary filters keep the application picker; native initialization failures are logged and fall back. `-Dopcodex.filechooser.native=false` provides a diagnostic fallback.

Resize grips stay visible at the center of each docking divider. Drag horizontally or vertically to resize; double-click restores equal sizes within pane minimums. Split toolbar controls retain their real buttons and all original choices. The complete toolbar is the default; optional compact mode remains available through More. Opening Console reveals a separate lower pane so code views can stay visible.

Different commands in the same panel use distinct symbols. Clear Console, close, copy, graph snapshot, rerun, refresh, editor integrations and navigation ranges have separate geometry. Tree/category filter controls remain distinguishable from table filters in every matching mode. Shared stroke, logical grid and theme colors keep the family consistent.

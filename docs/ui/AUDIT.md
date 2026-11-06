<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex v1 stable UI inventory

Updated 2026-10-20 (Asia/Calcutta). The owner selected the stable Ghidra 12.1.4 baseline for Opcodex v1 and authorized the visible application/repository rebrand and release foundation. Upstream pin: `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`. See [baseline](../UPSTREAM.md), [modifications](../../MODIFICATIONS.md) and [release verification](../releases/VERIFICATION.md).

The earlier 12.3 DEV audit is preserved on the `codex/ui-audit` branch. It is historical, superseded evidence for the old baseline. These CSVs refresh the tracked stable source after the branding implementation at `e3463a7054347ba142457af28f3872fce642b8fc`.

## Current interface coverage

This inventory is the recorded initial stable snapshot, not a fresh count of the alpha.2 resources. The modern family uses 121 original outline glyphs and paired bindings for all 878 declared IDs. It paints vectors directly, retains multi-resolution compatibility images, and supplies the product themes and startup artwork. See [design system](DESIGN_SYSTEM.md), [catalog](ICON_CATALOG.md) and [interface changes](UX_CHANGES.md) for the current delta.

The strict coverage check now reports 878 of 878 declared theme IDs covered with no asset errors. Composed aliases preserve their overlays and dimensions; empty layout placeholders intentionally remain empty. Run `python opcodex/check_ui_coverage.py` for the strict completeness check, or add `--allow-incomplete` for an alpha progress report. Invalid original asset dimensions/transparency fail in either mode. See [coverage](coverage.json). Matching light/dark color/font/icon roles are checked against the real theme objects in `OpcodexThemeTest`.

## Measured scope

| Measurement | Count |
|---|---:|
| Tracked files at scan | 20198 |
| Images across all source/help/test scopes | 1388 |
| Runtime image candidates | 612 |
| Theme property files | 35 |
| Unique icon IDs | 878 |
| Section-specific icon definitions | 896 |
| Inventory rows including aliases/dependencies | 1003 |
| Color candidates requiring contextual review | 210 |

These are distinct populations: aliases reuse drawings, generated/native icons have no source bitmap, and runtime candidates include strips/illustrations. At the initial audit, only the product identity was replaced; current declared-icon coverage is recorded above.

## Inventories

- [Icon inventory](icon-inventory.csv): ID, path, size, format, where-used hint and module, plus definition/section and source references.
- [All image assets](all-image-assets.csv): measured metadata and scope; [module/format/size counts](icon-counts-by-module.csv) count unique runtime files.
- [Theme definitions](theme-definitions.csv), [Java icon references](icon-references.csv), [size requests](icon-size-requests.csv) and [summary counts](audit-counts.json).
- [Color candidates](hardcoded-color-candidates.csv): constructors/constants, including legitimate parsers, fallbacks, annotation data and theme initialization. This is not a defect count.

The local extraction command was `python C:\Users\pc\AppData\Local\Temp\opcodex-ui-audit.py`. Image metadata used the installed Pillow 12.1.1; SVGs were parsed as XML. Source inventory is based on tracked files; no runtime coverage claim is made. Image readability means metadata/XML readability, not a complete visual pixel review. Java references exclude comments; surface labels are nearby-context heuristics with uncertain uses marked VERIFY. Alias/overlay resolution unions definitions across sections and matching resource filenames rather than emulating runtime classpath/theme precedence. Computed paths, static-import aliases, third-party plugins and generated/native glyphs still require runtime follow-through.

## Verified integration points

- `Framework/Gui` contains Gui, GIcon, GColor, ThemeManager/ApplicationThemeManager, PropertyFileThemeDefaults and ResourceManager. Module `.theme.properties` supports default/dark/LaF sections; selectable user `.theme` files use a different metadata format. FlatLaf 3.5.4 light/dark themes already exist. SVG recoloring is not assumed by the current AWT raster loading path.
- Theme switching is registered by `ThemeManagerPlugin` in the project window at Edit > Theme > Switch. Appearance following the OS remains unverified; native default LaF selection is not proof of dark/light tracking.
- Existing application icon IDs are preserved and now map to original transparent assets at 16/24/32/40/48/64/128/256 px. Splash identity has matching dark/light mappings. Framework dialog artwork is provided within Docking rather than creating a dependency on Base resources.
- Toolbar paths normalize to 16 px; tool icons use 16/22/24 px. Table sort/filter glyphs and theme overlays require smaller 8/10/12/14 px exceptions. Cursors, strips, splash artwork and native application icons need their own size rules.
- Raw loader paths and programmatically drawn symbols require explicit handling beyond theme IDs. Confirmed raw UI color bypasses remain in `BrokenLinkIcon` and `ToolTipUtils`; Palette constants are already GColor-backed. Color pickers, serialization, ANSI colors and user annotations must not be blindly recolored.
- Searchable action selection already exists through ActionChooserDialog, with Ctrl+3 in SystemKeyBindings. The requested palette should evaluate that mechanism before adding a duplicate.
- Alpha.2 updates CodeBrowser.tool with a tabbed function/symbol/type navigator, equal code panes and an on-demand console. Provider identities and plugins remain intact; full icon replacement is still incomplete.

## Checks and limits

All recorded image paths must exist; every discovered icon ID and runtime candidate must have an inventory row; source-line references and grouped counts are checked before handoff. See release verification for real compilation/package/launch evidence. Static CSV checks are not substitutes for application tests, contrast measurements, keyboard testing or cross-platform/HiDPI screenshots.

No engine/decompiler/loader/processor code was modified. The source rebrand retains LICENSE, NOTICE and upstream copyright headers. This inventory is preparation for the remaining v1 design work, not a completed v1 redesign.

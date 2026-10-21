<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Interface changes - alpha.2

| Area | Before | After / purpose |
|---|---|---|
| First run | Empty project surface with no clear next action | Create/Open Project welcome screen using existing commands and keyboard shortcuts. |
| Startup noise | Tip and release popups interrupt a fresh workspace | New profiles open the welcome screen directly; existing saved preferences and Help actions remain available. |
| Appearance | Platform-dependent default, saturated active headers | Matched Opcodex Light/Dark with black/neutral-light surfaces, quiet selection and readable syntax; saved user themes remain respected. |
| Icons | Mixed small raster styles across common actions and trees | 121 original outline glyphs map to 878 existing IDs; direct Java2D painting preserves sharp geometry at fractional display scales, including disabled controls. |
| Workspace | Three stacked navigator panels, uneven code panes and a blank console | Tabbed function/symbol/type navigator, balanced disassembly/pseudocode panes, console opens in its own lower panel on demand; overview/entropy retain a separate header area. Existing saved layouts remain respected. |
| Toolbar | Dozens of tiny actions visible at once | Core actions and Go To stay visible; secondary single actions are grouped in More, with a reversible complete-toolbar option. Split controls stay visible with every original dropdown choice. Original menus, context handling and shortcuts remain intact. |
| Panel headers | Gradients, bevels and mixed provider artwork | Flat headers, a small active indicator, consistent vector action sizing and 32px button targets. |
| File selection | Custom Swing file browser with large stacked shortcuts | Windows Common Item Dialog for local files/folders, multiple selection and extension filters. Virtual files and arbitrary predicates retain their application-specific picker. |
| Loading | Animated mascot and a framed loading panel | Thin, accessible progress bar with task status; preserve completion and cancellation behavior. |
| Separation | Raised pane frames, etched dialog borders and several gray edges | Flat surfaces, padded dialogs and visible grips on resizable dividers, hover feedback and double-click equal sizing. |
| Controls | Dense defaults and an etched Tool Chest frame | Roomier common controls and a quieter inset; every startup tool has an aligned icon, caption and consistent tile size. Existing New/Open/Save Project commands also appear on the primary toolbar. |
| About | Product name accompanied by an engine-brand headline and large boilerplate | Product-only identity and a concise license summary; complete required terms/attribution remain in the included legal files. |
| Windows launch | Portable batch file requiring an installed JDK | Native Opcodex.exe with bundled runtime, desktop and Start menu shortcuts. Portable launches remain available. |
| Repository | Fork relationship, inherited contribution copy and engine-name filenames | Standalone repository presentation, Opcodex contribution/issue guidance and product-only archive names. |

No analysis algorithms, processor definitions, decompiler implementation, project storage or compatibility protocols are changed. All declared theme icon IDs have paired original bindings. Registered built-in filename callers, including the four startup tools, use live vector adapters. Custom extension artwork and historical documentation images are outside the replacement registry; nine principal panels receive native launch checks. Follow-system mode remains outside this release.

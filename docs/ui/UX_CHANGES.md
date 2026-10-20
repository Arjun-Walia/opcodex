<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Interface changes â€” alpha.2 pilot

| Area | Before | After / purpose |
|---|---|---|
| First run | Empty project surface with no clear next action | Create/Open Project welcome screen using existing commands and keyboard shortcuts. |
| Startup noise | Tip and release popups interrupt a fresh workspace | New profiles open the welcome screen directly; existing saved preferences and Help actions remain available. |
| Appearance | Platform-dependent default, saturated active headers | Matched Opcodex Light/Dark with neutral surfaces, quiet selection and readable syntax; saved user themes remain respected. |
| Icons | Mixed small raster styles across common actions and trees | 67 original outline glyphs map to 141 existing IDs; real multi-resolution variants preserve logical size. |
| Controls | Dense defaults and an etched Tool Chest frame | Roomier common controls and a quieter inset; the primary Code Browser tool has a text label. |
| About | Product name accompanied by an engine-brand headline and large boilerplate | Product-only identity and a concise license summary; complete required terms/attribution remain in the included legal files. |
| Windows launch | Portable batch file requiring an installed JDK | Native Opcodex.exe with bundled runtime, desktop and Start menu shortcuts. Portable launches remain available. |
| Repository | Fork relationship, inherited contribution copy and engine-name filenames | Standalone repository presentation, Opcodex contribution/issue guidance and product-only archive names. |

No analysis algorithms, processor definitions, decompiler implementation, project storage or compatibility protocols are changed. Remaining specialized icons, richer empty states in populated analysis tools, a full layout redesign and follow-system mode are outside this initial pilot.

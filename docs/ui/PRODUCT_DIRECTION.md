# Opcodex desktop direction

Opcodex owns its product identity, workspace organization and command discovery. The stable analysis engine, project formats, original capabilities and required attribution remain intact.

## Design contract

- Use a quiet, precise desktop workspace. Give code and investigation data most of the screen. Prefer clear hierarchy and useful defaults over decoration.
- Keep Opcodex Light as the fresh-profile default, a black Opcodex Dark, and one original Opcodex Classic. Modern themes share an arrangement; Classic retains its own layout and original assets.
- Use a compact ribbon with task tabs, short labels and visibly separated groups. Home contains common work; All tools retains every original toolbar control. Keep the original menus. A visible command finder searches names, menu paths and purposes and explains availability.
- Keep one row of action icons in a ribbon page or provider header. Use scrolling when necessary. Retain original split controls, enablement rules, shortcuts and action dispatch.
- Treat Debugger as a session workflow: session and execution commands first, clear dynamic/static code roles, useful state and thread panes, and an actionable no-trace state. Never simulate a connected target or hide real trace data behind a placeholder.
- Use the same sharp vector family, font hierarchy, spacing, focus treatment, pointer behavior and useful tooltips throughout the product. Different commands in the same panel need different symbols.
- Preserve adjustable dividers, docking, detached views and customized saved layouts. Offer an explicit reset and return to the previous workspace.

## Own the complete experience

Review Project, startup, CodeBrowser, Debugger, Emulator, Version Tracking, Functions, Types, Symbols, Graph, Scripts, Console, file selection, search, options, Help and error dialogs as one product. Fix recurring design problems at the shared component or workflow level. Do not wait for the owner to enumerate each inconsistent panel or icon.

Check first run, empty data, disabled controls, progress, cancellation, failure and populated views. Assess real application captures at ordinary and enlarged scale. A source change or website illustration alone does not establish a usable interface.

## Continuity and shipment

Record changed upstream files and preserve their original notices. Keep packaged source identity and checksums tied to the exact artifact. Verify fresh and upgraded installations, shortcuts, Linux desktop identity, full action/menu continuity and theme/layout restoration. Label development overlays explicitly.

Publish user guides on the website and keep engineering evidence and handoff state in the Opcodex Obsidian project. Clearly separate implemented features, installed-package evidence, independent audit results and open release checks. Never convert a focused UI pass into a claim that the broad upstream suite or live debugger backends passed.


## Workspace layout from the November 5 references

The modern workbench opens Program Trees beside Listing and Decompiler, with a short Console dock below the code panes. Provider tabs are at the top. Existing saved arrangements are available until the user chooses the focused layout reset. The dock frames, grouped ribbon and search field share the same spacing in the modern tools. Light and Dark retain their existing palettes. Classic retains the prior workspace structure.

The project window puts the real Tree and Table views beside a resizable launcher. Launcher cards are the existing tool buttons, with descriptions and room for names. Custom tools remain in the launcher. The existing running-tool and workspace controls sit together below the split.

The Windows development preview was exercised in Light, Dark and Classic with an isolated settings profile and a synthetic project. The captured Debugger shows its initial state without a live target. Installer verification remains a separate release check.


## Workspace presets and project icons

| Workspace | Modern starting layout |
| --- | --- |
| Project | Resizable Tree or Table explorer beside the tool launcher, with running tools and workspace controls below |
| CodeBrowser | Program Trees beside Listing and Decompiler, with Console below |
| Debugger | Session and execution controls, static and dynamic code, and separate thread and state views |
| Emulator | Program explorer beside the main code view, with trace state on the right and output below |
| Version Tracking | Matches alongside the Markup Items and Implied Matches tabs |

The ribbon exposes a command search and keeps each page on one row. Drag the pane dividers to resize the workspace. The Layout menu can restore the tool's preset and return to the previous arrangement. Classic has its own saved layout and original icons.

Project Tree and Table show vector file icons for recognized names and extensions. Unknown names keep their content icons. Checkout, link and read-only markers stay layered above the file icon. The matching rules adapt selected entries from Better File Icons 0.6.1, with its MIT notice in the distribution. The rendered glyphs are Opcodex artwork.

Use **Edit → Theme → Import…** with a standalone VS Code theme JSON or JSONC file containing a `colors` object. The importer applies supported workbench colors over the Opcodex Light or Dark base and saves an optional desktop theme. It reads opaque editor, sidebar, selection, border, tooltip, cursor and focus colors. It skips token rules, extension assets and referenced files. Imported palettes use the modern layout.

<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Stable engine baseline

Opcodex v1 starts from the official stable **Ghidra 12.1.4** tag, `Ghidra_12.1.4_build`, resolved to commit `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc` on 2026-10-20. The full pin is in `opcodex/upstream.json`.

The previous development checkout (`master`, `f70f07d07ca6284c1f26557ad19071885ed8590b`) identified itself as 12.3 DEV. Its audit remains preserved on `codex/ui-audit`. The owner selected stable for v1; `codex/v1-stable` starts directly from the stable tag rather than reverting files in the newer engine or rewriting published history.

The stable source requires JDK 21 or newer supported JDK, Gradle 8.5 minimum and Python 3.9–3.14. Its checked-in wrapper selects Gradle 9.7.1. The engine compatibility version remains 12.1.4. Product version and branding are a separate layer. The PUBLIC engine release channel is retained because the upstream wrapper supports PUBLIC/DEV; preview status is carried in the independent Opcodex semantic version.

Keep the stable tag's original LICENSE, NOTICE, copyright headers, dependency licenses and GPL helper terms. Changes to upstream files carry an `OPCODEX:` marker and are listed in MODIFICATIONS.md. No mass package/module renaming or analysis-engine modifications are part of branding. Existing application identity determines preference directories; Opcodex uses its own identity and does not automatically migrate another application's settings.

Future upgrades should follow an explicitly chosen stable tag, record the full source commit and repeat source, build, extension and desktop verification. Do not merge the old development branch into this stable release branch merely to import documentation. The previous audit is historical evidence for its own commit, not proof about this baseline.

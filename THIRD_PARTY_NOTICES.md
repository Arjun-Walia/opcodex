<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex additional notices

This file supplements the unchanged upstream `NOTICE`, `LICENSE`, module notices, `licenses/` and `GPL/` terms; it does not replace or relicense them.

Opcodex is built on Ghidra 12.1.4 (`Ghidra_12.1.4_build`, `8b6bbb857accdfa20dc5b2f5dea471178c2e9fbc`). Ghidra was created and is maintained by the National Security Agency Research Directorate and its contributors. Opcodex is an independent modified distribution and is not endorsed by the NSA or the Ghidra project.

The Opcodex ribbon SVG mark is original artwork, carried from the project's website with its Apache-2.0 copyright notice. The generated PNG/ICO variants introduce no third-party icon family. The upstream action icon collection and its licenses remain in place pending the separately reviewed redesign.

No new desktop runtime dependency is introduced. PNG export uses Sharp (Apache-2.0) and its libvips tooling (LGPL-2.1-or-later) during development only. Those tools, their Node dependencies and their native rendering libraries are not included in Opcodex release overlays. Release packaging itself uses Python's standard library.

For the actual dependencies included by the engine build, consult the distribution's `licenses/`, module manifests and generated upstream dependency/license inventory. The complete application ZIP is not exclusively covered by a single license.

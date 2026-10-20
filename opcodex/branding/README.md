<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Opcodex identity

Original angular ribbon mark used by the Opcodex website, copied from its approved `public/brand/mark.svg` and `mark-light.svg` assets. The embedded copyright/license notices remain intact. The surface suffix describes the background on which that transparent variant is displayed.

This artwork is separate from upstream Ghidra artwork. No third-party icon family is introduced by this branding pass. Existing action icons remain unchanged until the reviewed icon pilot and design system are complete.

`generate.cjs` renders the two SVG sources into transparent app icons at 16, 24, 32, 40, 48, 64, 128 and 256 px, plus themed splash headers. Generated PNGs are committed, so building the desktop application needs no Node.js or SVG renderer. To regenerate, install `sharp` as a development tool and run `node opcodex/branding/generate.cjs`. Sharp is Apache-2.0; its bundled libvips renderer is LGPL-2.1-or-later and is used only for development exports, not shipped as a runtime dependency. The Windows ICO is composed from the committed PNGs using the existing Pillow installation.

<!-- OPCODEX: contribution workflow for this project. Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->

# Contributing to Opcodex

Use [Opcodex issues](https://github.com/Arjun-Walia/opcodex/issues) for bugs, interface feedback and feature proposals. Include your app version, platform, display scaling and a clear reproduction. Do not upload proprietary binaries, credentials or private projects.

Submit focused pull requests against `main`. Explain the problem, resulting behavior and checks performed. For interface work, include light/dark screenshots and check keyboard focus, readable text and small icon sizes.

Keep compatibility-sensitive changes small. Prefer themes, resources and extensions. Changes to inherited files require an `OPCODEX:` marker and a record in [MODIFICATIONS.md](MODIFICATIONS.md). Preserve original copyright headers, LICENSE, NOTICE and component terms. Engine changes are outside the current UI work.

Run `python -m unittest discover -s opcodex/tests -v`, appropriate module tests and `git diff --check`. Build and launch interface changes before reporting them complete. The broad regression suite requires an explicit JUnit report check; the build task's exit status alone does not certify all tests.

Original additions use Apache-2.0; contributions use the same inbound/outbound terms. Only submit work and assets you have rights to contribute. Record new asset/dependency licenses in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md). See [SECURITY.md](SECURITY.md) for private vulnerability reporting.

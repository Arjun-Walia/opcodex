/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

import io.opcodex.engine.spi.EngineServices;

/** Who is calling and with which engine services. */
public record CallContext(Credential.Kind kind, String actor, EngineServices services) {
}

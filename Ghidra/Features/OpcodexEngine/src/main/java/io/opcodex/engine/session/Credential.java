/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.session;

/** A connection credential. DESKTOP is handed over privately by the shell; CLIENT is on disk. */
public record Credential(Kind kind, String token) {

	public enum Kind {
		DESKTOP, CLIENT
	}
}

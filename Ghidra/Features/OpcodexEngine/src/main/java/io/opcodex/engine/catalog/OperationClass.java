/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.catalog;

/** How an operation affects the user's data and what permission it needs. */
public enum OperationClass {
	/** Reads program or project data. */
	READ,
	/** Changes program data; journaled and subject to the approval policy. */
	WRITE,
	/** Runs code or scripts; subject to the approval policy. */
	RUN,
	/** Changes engine settings or policy; desktop credential only. */
	SYSTEM;

	/** Lower-case wire name. */
	public String wireName() {
		return name().toLowerCase();
	}
}

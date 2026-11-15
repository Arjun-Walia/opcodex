/* Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 */
package io.opcodex.engine.rpc;

/** Stable domain error codes sent in JSON-RPC {@code error.data.code}. */
public enum ErrorCode {
	UNAUTHORIZED, FORBIDDEN, NOT_FOUND, INVALID_PARAMS, STALE_REVISION, CONFLICT,
	APPROVAL_REQUIRED, CANCELLED, ENGINE_ERROR
}

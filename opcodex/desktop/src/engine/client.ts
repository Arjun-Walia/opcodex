// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// One way to reach the engine: through the Tauri shell in the app, or the dev bridge in a browser.

export class EngineError extends Error {
  constructor(
    readonly code: string,
    message: string,
    readonly data?: Record<string, unknown>,
  ) {
    super(message);
  }
}

interface RpcReply {
  result?: unknown;
  error?: { message?: string; data?: { code?: string } & Record<string, unknown> };
}

function unwrap<T>(reply: RpcReply): T {
  if (reply.error) {
    throw new EngineError(
      reply.error.data?.code ?? 'ENGINE_ERROR',
      reply.error.message ?? 'The engine reported an error.',
      reply.error.data,
    );
  }
  return reply.result as T;
}

function inTauri(): boolean {
  return typeof window !== 'undefined' && '__TAURI_INTERNALS__' in window;
}

/** Calls one engine operation and returns its result, or throws an EngineError. */
export async function call<T>(method: string, params: Record<string, unknown> = {}): Promise<T> {
  if (inTauri()) {
    const { invoke } = await import('@tauri-apps/api/core');
    const reply = await invoke<RpcReply>('engine_call', { method, params });
    return unwrap<T>(reply);
  }
  const response = await fetch('/__engine', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ method, params }),
  });
  return unwrap<T>((await response.json()) as RpcReply);
}

/** Lets the person pick a file or folder with the native dialog; null outside the app. */
export async function pickPath(options: {
  directory?: boolean;
  title: string;
  filters?: { name: string; extensions: string[] }[];
}): Promise<string | null> {
  if (!inTauri()) return null;
  const { open } = await import('@tauri-apps/plugin-dialog');
  const picked = await open({ multiple: false, ...options });
  return typeof picked === 'string' ? picked : null;
}

export const isDesktopApp = inTauri;

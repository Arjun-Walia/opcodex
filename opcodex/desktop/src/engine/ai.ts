// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The agent chat's link to model providers. In the app, requests go through the native shell,
// which adds the API key from the OS credential store; the key never reaches this page.
import type { Provider } from '../lib/agent';
import { call, isDesktopApp } from './client';

export type KeyStatus = { anthropic: boolean; openai: boolean };

export const KEY_LINKS: Record<'anthropic' | 'openai', { label: string; url: string }> = {
  anthropic: { label: 'console.anthropic.com', url: 'https://console.anthropic.com/settings/keys' },
  openai: { label: 'platform.openai.com', url: 'https://platform.openai.com/api-keys' },
};

export function keyStatus(): Promise<KeyStatus> {
  return call<KeyStatus>('__desktop.ai.status');
}

export function saveKey(provider: 'anthropic' | 'openai', key: string): Promise<KeyStatus> {
  return call<KeyStatus>('__desktop.ai.saveKey', { provider, key });
}

export function removeKey(provider: 'anthropic' | 'openai'): Promise<KeyStatus> {
  return call<KeyStatus>('__desktop.ai.removeKey', { provider });
}

export class ModelError extends Error {
  readonly status: number | undefined;
  constructor(status: number | undefined, message: string) {
    super(message);
    this.status = status;
  }
}

/** Explains a provider's error body in plain words. */
export function explain(status: number | undefined, body: string): string {
  let detail = body;
  try {
    const parsed = JSON.parse(body);
    detail = parsed?.error?.message ?? parsed?.message ?? body;
  } catch {
    // keep the raw text
  }
  if (status === 401 || status === 403) return 'The API key was refused. Check it in Settings → AI models.';
  if (status === 404) return `The model wasn't found. Check its name in Settings → AI models. (${detail})`;
  if (status === 429) return 'The provider is rate-limiting requests or your credit ran out. Try again shortly.';
  if (status && status >= 500) return 'The provider had a problem answering. Try again shortly.';
  return detail || 'The model request failed.';
}

let counter = 0;

type Payload = { id: string; chunk?: string; status?: number; message?: string };

/** Streams one request's raw response chunks. Cancel with the signal. */
export async function* stream(provider: Provider, localUrl: string, body: unknown, signal: AbortSignal): AsyncGenerator<string> {
  if (!isDesktopApp()) {
    throw new ModelError(undefined, 'The agent chat runs in the Opcodex app, not in this browser preview.');
  }
  const { invoke } = await import('@tauri-apps/api/core');
  const { listen } = await import('@tauri-apps/api/event');
  const id = `chat-${Date.now().toString(36)}-${counter++}`;
  const queue: string[] = [];
  let finished = false;
  let failure: ModelError | null = null;
  let wake: (() => void) | null = null;
  const nudge = () => {
    const w = wake;
    wake = null;
    w?.();
  };
  const unlisten = await Promise.all([
    listen<Payload>('ai:data', (e) => {
      if (e.payload.id === id && e.payload.chunk) {
        queue.push(e.payload.chunk);
        nudge();
      }
    }),
    listen<Payload>('ai:done', (e) => {
      if (e.payload.id === id) {
        finished = true;
        nudge();
      }
    }),
    listen<Payload>('ai:error', (e) => {
      if (e.payload.id === id) {
        failure = new ModelError(e.payload.status, explain(e.payload.status, e.payload.message ?? ''));
        finished = true;
        nudge();
      }
    }),
  ]);
  const onAbort = () => {
    finished = true;
    nudge();
  };
  let completed = false;
  signal.addEventListener('abort', onAbort);
  try {
    if (signal.aborted) return; // stopped before it began: send nothing
    await invoke('ai_start', { provider, local: provider === 'local' ? localUrl : null, body, id });
    while (true) {
      if (queue.length) {
        yield queue.shift()!;
        continue;
      }
      if (finished) break;
      await new Promise<void>((resolve) => {
        wake = resolve;
      });
    }
    if (failure) throw failure;
    completed = !signal.aborted;
  } finally {
    // Stopped, abandoned or failed early: make sure the provider request stops too.
    if (!completed) invoke('ai_cancel', { id }).catch(() => undefined);
    signal.removeEventListener('abort', onAbort);
    unlisten.forEach((u) => u());
  }
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Settings → AI models: keys go to the OS credential store, never to this page's storage.
import { useEffect, useState, type FormEvent } from 'react';
import { Check, Trash2 } from 'lucide-react';
import { KEY_LINKS, keyStatus, removeKey, saveKey, type KeyStatus } from '../engine/ai';
import { isDesktopApp } from '../engine/client';
import { usePrefs } from '../state/prefs';
import type { Provider } from '../lib/agent';

const CLOUD: { id: 'anthropic' | 'openai'; title: string; blurb: string; placeholder: string }[] = [
  {
    id: 'anthropic',
    title: 'Claude (Anthropic)',
    blurb: 'Lets the agent chat use Claude to read and explain code with Opcodex’s tools.',
    placeholder: 'Paste your Anthropic key',
  },
  {
    id: 'openai',
    title: 'OpenAI',
    blurb: 'Lets the agent chat use OpenAI models with the same tools.',
    placeholder: 'Paste your OpenAI key',
  },
];

function KeyRow({ provider, status, onStatus }: { provider: (typeof CLOUD)[number]; status?: boolean; onStatus: (s: KeyStatus) => void }) {
  const [value, setValue] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const link = KEY_LINKS[provider.id];

  async function save(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      onStatus(await saveKey(provider.id, value));
      setValue('');
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="ai-provider">
      <h3>{provider.title}</h3>
      <p className="muted">{provider.blurb}</p>
      {status ? (
        <div className="ai-saved">
          <Check size={14} /> Key saved in Windows Credential Manager.
          <button
            type="button"
            className="button ghost"
            onClick={async () => {
              try {
                onStatus(await removeKey(provider.id));
              } catch (err) {
                setError(err instanceof Error ? err.message : String(err));
              }
            }}
          >
            <Trash2 size={13} /> Remove
          </button>
        </div>
      ) : (
        <form className="ai-key-form" onSubmit={save}>
          <input
            type="password"
            autoComplete="off"
            spellCheck={false}
            value={value}
            onChange={(e) => setValue(e.target.value)}
            placeholder={provider.placeholder}
            aria-label={provider.placeholder}
          />
          <button type="submit" className="button" disabled={busy || !value.trim()}>
            Save
          </button>
        </form>
      )}
      {error && <p className="ai-error">{error}</p>}
      <a className="ai-link" href={link.url} target="_blank" rel="noopener noreferrer">
        Get a key at {link.label}
      </a>
    </div>
  );
}

export function AiModels() {
  const prefs = usePrefs();
  const { ai } = prefs;
  const [status, setStatus] = useState<KeyStatus | null>(null);
  const desktop = isDesktopApp();

  useEffect(() => {
    if (desktop) keyStatus().then(setStatus, () => setStatus({ anthropic: false, openai: false }));
  }, [desktop]);

  const model = (p: Provider, label: string) => (
    <label className="ai-model">
      <span>{label}</span>
      <input
        className="mono"
        value={ai.models[p]}
        spellCheck={false}
        onChange={(e) => prefs.setAi({ models: { ...ai.models, [p]: e.target.value } })}
        aria-label={`${label} model name`}
      />
    </label>
  );

  return (
    <section className="settings-section" id="ai-models">
      <div className="settings-head">
        <div>
          <h2>AI models</h2>
          <p>
            For the agent chat. Use your own Claude or OpenAI key, or a model running on this computer. Keys are stored by
            Windows, not in Opcodex’s settings.
          </p>
        </div>
      </div>
      {!desktop && <p className="muted">Keys can be saved in the Opcodex app.</p>}
      {desktop &&
        CLOUD.map((p) => (
          <KeyRow key={p.id} provider={p} status={status?.[p.id]} onStatus={setStatus} />
        ))}
      <div className="ai-provider">
        <h3>Local model</h3>
        <p className="muted">
          Any server with an OpenAI-compatible API on this computer, such as Ollama or LM Studio. Nothing leaves this
          computer.
        </p>
        <label className="ai-model">
          <span>Address</span>
          <input
            className="mono"
            value={ai.localUrl}
            spellCheck={false}
            onChange={(e) => prefs.setAi({ localUrl: e.target.value })}
            aria-label="Local model address"
          />
        </label>
      </div>
      <div className="ai-models">
        {model('anthropic', 'Claude model')}
        {model('openai', 'OpenAI model')}
        {model('local', 'Local model')}
      </div>
      {(ai.consent.anthropic || ai.consent.openai) && (
        <p className="muted">
          You allowed chats to send code and analysis to{' '}
          {[ai.consent.anthropic && 'Anthropic', ai.consent.openai && 'OpenAI'].filter(Boolean).join(' and ')}.{' '}
          <button type="button" className="link-button" onClick={() => prefs.setAi({ consent: {} })}>
            Withdraw
          </button>
        </p>
      )}
    </section>
  );
}

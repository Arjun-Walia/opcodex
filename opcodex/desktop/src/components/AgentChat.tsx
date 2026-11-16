// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The agent chat: talk to Claude, OpenAI or a local model that can use Opcodex's tools.
import { Fragment, useEffect, useRef, useState, type KeyboardEvent } from 'react';
import { Bot, Check, ChevronRight, Loader2, Plus, Send, ShieldAlert, Square, Wrench, X } from 'lucide-react';
import { call, isDesktopApp } from '../engine/client';
import { keyStatus, stream, type KeyStatus } from '../engine/ai';
import { insideThinking, stripThinking, systemPrompt, toolsFrom, type CatalogOp, type ChatMessage, type Provider, type Tool, type ToolCall, type ToolResult } from '../lib/agent';
import { runTurn, type Policy } from '../lib/agentLoop';
import { usePrefs } from '../state/prefs';
import { useSession } from '../state/session';

const PROVIDER_NAMES: Record<Provider, string> = { anthropic: 'Claude', openai: 'OpenAI', local: 'Local model' };
const COMPANY: Record<'anthropic' | 'openai', string> = { anthropic: 'Anthropic', openai: 'OpenAI' };

type Activity = { call: ToolCall; result?: ToolResult };
type Approval = { tool: Tool; input: Record<string, unknown>; resolve: (ok: boolean) => void };

const SUGGESTIONS = [
  'Explain what this function does',
  'Find the code that talks to the network',
  'Suggest better names for the functions I’m looking at',
  'Summarise this program for a triage report',
];

/** Renders model text as paragraphs, inline code and fenced code blocks. Never as HTML. */
function RichText({ text }: { text: string }) {
  const blocks = text.split(/```/);
  return (
    <>
      {blocks.map((block, i) => {
        if (i % 2 === 1) {
          const body = block.replace(/^[a-zA-Z0-9+#-]*\n/, '');
          return (
            <pre key={i} className="chat-code">
              {body.replace(/\n$/, '')}
            </pre>
          );
        }
        return block
          .split(/\n{2,}/)
          .filter((p) => p.trim())
          .map((para, j) => (
            <p key={`${i}-${j}`}>
              {para.split(/(`[^`\n]+`)/).map((piece, k) =>
                piece.startsWith('`') && piece.endsWith('`') && piece.length > 2 ? (
                  <code key={k}>{piece.slice(1, -1)}</code>
                ) : (
                  <Fragment key={k}>
                    {piece.split('\n').map((line, n) => (
                      <Fragment key={n}>
                        {n > 0 && <br />}
                        {line}
                      </Fragment>
                    ))}
                  </Fragment>
                ),
              )}
            </p>
          ));
      })}
    </>
  );
}

function describeCall(call: ToolCall): string {
  const op = call.name.replace(/_/g, '.');
  const at = typeof call.input.address === 'string' ? ` at ${call.input.address}` : '';
  return `${op}${at}`;
}

function ToolChip({ activity }: { activity: Activity }) {
  const [open, setOpen] = useState(false);
  const state = !activity.result ? 'running' : activity.result.isError ? 'error' : 'done';
  return (
    <div className={`chat-tool ${state}`}>
      <button type="button" className="chat-tool-head" onClick={() => setOpen((o) => !o)} aria-expanded={open}>
        {state === 'running' ? <Loader2 size={12} className="spin" /> : <Wrench size={12} />}
        <span className="mono">{describeCall(activity.call)}</span>
        <ChevronRight size={12} className={`chev${open ? ' open' : ''}`} />
      </button>
      {open && (
        <div className="chat-tool-body mono">
          <div className="muted">input</div>
          <pre>{JSON.stringify(activity.call.input, null, 2)}</pre>
          {activity.result && (
            <>
              <div className="muted">{activity.result.isError ? 'error' : 'result'}</div>
              <pre>{activity.result.content.slice(0, 4000)}</pre>
            </>
          )}
        </div>
      )}
    </div>
  );
}

export function AgentChat({ open, onClose, onOpenSettings }: { open: boolean; onClose: () => void; onOpenSettings: () => void }) {
  const prefs = usePrefs();
  const session = useSession();
  const { ai } = prefs;
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [streaming, setStreaming] = useState('');
  const [activity, setActivity] = useState<Record<string, Activity>>({});
  const [approval, setApproval] = useState<Approval | null>(null);
  const [draft, setDraft] = useState('');
  const [busy, setBusy] = useState(false);
  const [thinking, setThinking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [keys, setKeys] = useState<KeyStatus | null>(null);
  const abort = useRef<AbortController | null>(null);
  // Bumped by "New chat": a reply still finishing must not bring the old conversation back.
  const generation = useRef(0);
  const scroller = useRef<HTMLDivElement>(null);
  const input = useRef<HTMLTextAreaElement>(null);
  const desktop = isDesktopApp();

  useEffect(() => {
    if (!open || !desktop) return;
    keyStatus().then(setKeys, () => setKeys({ anthropic: false, openai: false }));
    setTimeout(() => input.current?.focus(), 60);
  }, [open, desktop]);

  useEffect(() => {
    scroller.current?.scrollTo({ top: scroller.current.scrollHeight });
  }, [messages, streaming, activity, approval]);

  const provider = ai.provider;
  const cloud = provider !== 'local';
  const hasKey = !cloud || (keys?.[provider as 'anthropic' | 'openai'] ?? false);
  const consented = !cloud || ai.consent[provider as 'anthropic' | 'openai'] === true;
  const program = session.programs.find((p) => p.programId === session.programId);

  async function send(text: string) {
    const prompt = text.trim();
    if (!prompt || busy) return;
    setDraft('');
    setError(null);
    setBusy(true);
    setThinking(false);
    setStreaming('');
    const controller = new AbortController();
    abort.current = controller;
    const mine = generation.current;
    const current = () => generation.current === mine;
    const history: ChatMessage[] = [...messages, { role: 'user', parts: [{ type: 'text', text: prompt }] }];
    setMessages(history);
    try {
      const catalog = await call<CatalogOp[]>('catalog.list');
      let policy: Policy = 'ask';
      if (session.programId) {
        policy = await call<{ mode: Policy }>('policy.get').then((p) => p.mode, () => 'ask' as Policy);
      }
      let changed = false;
      const result = await runTurn(
        {
          provider,
          model: ai.models[provider],
          system: systemPrompt({ program: program?.name, programId: session.programId, location: session.location }),
          tools: toolsFrom(catalog, { core: provider === 'local' }),
          policy,
          send: (body) => stream(provider, ai.localUrl, body, controller.signal),
          execute: async (op, params) => {
            const out = await call<unknown>(op, params);
            if (op !== 'program.info' && /^(symbol|comment|history|script|program\.analyze|program\.import)/.test(op)) changed = true;
            return out;
          },
          approve: (tool, params) => new Promise<boolean>((resolve) => setApproval({ tool, input: params, resolve })),
          onText: (delta) => current() && setStreaming((s) => s + delta),
          onThinking: () => current() && setThinking(true),
          onTool: ({ call: c, result: r }) => {
            if (!current()) return;
            setStreaming('');
            setActivity((a) => ({ ...a, [c.id]: { call: c, result: r } }));
          },
          cancelled: () => controller.signal.aborted,
        },
        history,
      );
      if (current()) setMessages(result);
      if (changed) session.bump();
    } catch (e) {
      if (current() && !controller.signal.aborted) setError(e instanceof Error ? e.message : String(e));
      if (current() && controller.signal.aborted) setMessages(history);
    } finally {
      if (current()) {
        setStreaming('');
        setBusy(false);
        setApproval(null);
        abort.current = null;
      }
    }
  }

  function stop() {
    abort.current?.abort();
    approval?.resolve(false);
    setApproval(null);
  }

  function newChat() {
    stop();
    generation.current += 1;
    setBusy(false);
    setStreaming('');
    abort.current = null;
    setMessages([]);
    setActivity({});
    setError(null);
  }

  function onKey(e: KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send(draft);
    }
  }

  const ready = desktop && hasKey && consented;

  return (
    <aside className={`chat-drawer${open ? ' open' : ''}`} aria-label="Agent chat" aria-hidden={!open}>
      <header className="chat-head">
        <Bot size={16} />
        <strong>Agent chat</strong>
        <div className="segmented chat-provider" role="radiogroup" aria-label="Model provider">
          {(['anthropic', 'openai', 'local'] as const).map((p) => (
            <button key={p} type="button" role="radio" aria-checked={provider === p} disabled={busy} onClick={() => prefs.setAi({ provider: p })}>
              {PROVIDER_NAMES[p]}
            </button>
          ))}
        </div>
        <span className="toolbar-spacer" />
        <button type="button" className="icon-button" aria-label="New chat" title="New chat" onClick={newChat}>
          <Plus size={15} />
        </button>
        <button type="button" className="icon-button" aria-label="Close chat" onClick={onClose}>
          <X size={15} />
        </button>
      </header>
      <div className="chat-model mono" title="Change models in Settings → AI models">
        {ai.models[provider]}
        {program ? ` · ${program.name}` : ' · no program open'}
      </div>

      <div className="chat-scroll" ref={scroller}>
        {!desktop && (
          <div className="chat-card">
            <p>The agent chat runs in the Opcodex app. This browser preview can’t reach model providers.</p>
          </div>
        )}
        {desktop && !hasKey && keys && (
          <div className="chat-card">
            <p>
              Add your {COMPANY[provider as 'anthropic' | 'openai']} API key to chat with {PROVIDER_NAMES[provider]}, or pick
              Local model to use one running on this computer.
            </p>
            <button type="button" className="button primary" onClick={onOpenSettings}>
              Open AI models settings
            </button>
          </div>
        )}
        {desktop && hasKey && !consented && (
          <div className="chat-card consent">
            <ShieldAlert size={18} />
            <p>
              Chats with {PROVIDER_NAMES[provider]} send your messages and the code and analysis the assistant reads to{' '}
              {COMPANY[provider as 'anthropic' | 'openai']}. Nothing is sent until you agree. A local model keeps everything on
              this computer.
            </p>
            <div className="button-row">
              <button type="button" className="button primary" onClick={() => prefs.setAi({ consent: { ...ai.consent, [provider]: true } })}>
                <Check size={14} /> Allow for {PROVIDER_NAMES[provider]}
              </button>
              <button type="button" className="button" onClick={() => prefs.setAi({ provider: 'local' })}>
                Use a local model
              </button>
            </div>
          </div>
        )}
        {ready && messages.length === 0 && (
          <div className="chat-empty">
            <p className="muted">
              Ask about the program you have open. The assistant reads code with Opcodex’s tools and asks before it changes
              anything, as your approval policy says.
            </p>
            {SUGGESTIONS.map((s) => (
              <button key={s} type="button" className="chat-suggestion" onClick={() => send(s)} disabled={!session.programId}>
                {s}
              </button>
            ))}
          </div>
        )}
        {messages.map((m, i) =>
          m.parts.map((p, j) => {
            const key = `${i}-${j}`;
            if (p.type === 'text') {
              return (
                <div key={key} className={`chat-msg ${m.role}`}>
                  <RichText text={p.text} />
                </div>
              );
            }
            if (p.type === 'tool_call') {
              return <ToolChip key={key} activity={activity[p.id] ?? { call: p }} />;
            }
            return null;
          }),
        )}
        {busy &&
          Object.values(activity)
            .filter((a) => !messages.some((m) => m.parts.some((p) => p.type === 'tool_call' && p.id === a.call.id)))
            .map((a) => <ToolChip key={a.call.id} activity={a} />)}
        {stripThinking(streaming).trim() && (
          <div className="chat-msg assistant">
            <RichText text={stripThinking(streaming)} />
          </div>
        )}
        {busy && (!stripThinking(streaming).trim() || insideThinking(streaming)) && !approval && (
          <div className="chat-thinking">
            <Loader2 size={14} className="spin" /> {thinking || insideThinking(streaming) ? 'Thinking…' : 'Working…'}
          </div>
        )}
        {approval && (
          <div className="chat-card approval" role="alertdialog" aria-label="Approve a change">
            <strong>{approval.tool.cls === 'run' ? 'Run a script?' : 'Make this change?'}</strong>
            <p className="muted">{approval.tool.description}</p>
            <pre className="mono">{`${approval.tool.op}\n${JSON.stringify(approval.input, null, 2)}`}</pre>
            <div className="button-row">
              <button type="button" className="button primary" onClick={() => { approval.resolve(true); setApproval(null); }}>
                <Check size={14} /> Approve
              </button>
              <button type="button" className="button" onClick={() => { approval.resolve(false); setApproval(null); }}>
                Decline
              </button>
            </div>
          </div>
        )}
        {error && (
          <div className="banner" role="alert">
            {error}
          </div>
        )}
      </div>

      <div className="chat-compose">
        <textarea
          ref={input}
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={onKey}
          placeholder={ready ? 'Ask about this program…' : 'Set up a model to start'}
          disabled={!ready}
          rows={2}
          aria-label="Message"
        />
        {busy ? (
          <button type="button" className="icon-button chat-send" aria-label="Stop" onClick={stop}>
            <Square size={14} />
          </button>
        ) : (
          <button type="button" className="icon-button chat-send" aria-label="Send" onClick={() => send(draft)} disabled={!ready || !draft.trim()}>
            <Send size={15} />
          </button>
        )}
      </div>
    </aside>
  );
}

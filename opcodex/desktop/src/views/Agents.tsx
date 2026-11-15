// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The agent harness: which agent apps can drive Opcodex, what they may change, what they did.
import { useEffect, useState } from 'react';
import { Bot, Check, Copy, Loader2, Plug, PlugZap, RefreshCw, ShieldCheck, SquareTerminal, Undo2, Wrench } from 'lucide-react';
import { call, EngineError } from '../engine/client';
import { openTerminal } from '../components/TerminalPanel';
import type { HistoryEntry, Page } from '../engine/types';
import { useEngine, useSession } from '../state/session';

interface AgentApp {
  id: string;
  installed: boolean;
  registered: boolean;
  pointsToThisExecutable: boolean;
}

interface CatalogEntry {
  name: string;
  description: string;
  class: 'read' | 'write' | 'run' | 'system';
}

const APP_NAMES: Record<string, string> = {
  'claude-code': 'Claude Code',
  'claude-desktop': 'Claude Desktop',
  codex: 'Codex CLI',
  cursor: 'Cursor',
  vscode: 'VS Code',
  windsurf: 'Windsurf',
  gemini: 'Gemini CLI',
};

/** Agents that run in a terminal, and the command that starts them. */
const TERMINAL_AGENTS: Record<string, string> = { 'claude-code': 'claude', codex: 'codex', gemini: 'gemini' };

const POLICIES = [
  { id: 'ask', label: 'Ask first', text: 'Agents can read everything. Changes wait for you.' },
  { id: 'allow-writes', label: 'Allow changes', text: 'Agents can rename, comment and analyse. Every change can be undone.' },
  { id: 'allow-all', label: 'Allow changes and scripts', text: 'Also lets agents run scripts. Use with agents you trust.' },
];

function message(e: unknown) {
  return e instanceof EngineError || e instanceof Error ? e.message : 'Something went wrong.';
}

function AppRow({ app, onChanged }: { app: AgentApp; onChanged: () => void }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string>();
  const connected = app.registered && app.pointsToThisExecutable;

  async function toggle() {
    setBusy(true);
    setError(undefined);
    try {
      await call(connected ? '__desktop.agents.disconnect' : '__desktop.agents.connect', { app: app.id });
      onChanged();
    } catch (e) {
      setError(message(e));
    } finally {
      setBusy(false);
    }
  }

  let status = 'Not installed';
  if (app.installed) status = connected ? 'Connected' : app.registered ? 'Points to another copy' : 'Not connected';

  return (
    <div className={`agent-row ${connected ? 'on' : ''}`}>
      <span className="agent-icon">{connected ? <PlugZap size={16} /> : <Plug size={16} />}</span>
      <div className="agent-text">
        <strong>{APP_NAMES[app.id] ?? app.id}</strong>
        <span className={`agent-status ${connected ? 'ok' : ''}`}>{status}</span>
        {error && <span className="agent-error">{error}</span>}
      </div>
      {app.installed && connected && TERMINAL_AGENTS[app.id] && (
        <button
          type="button"
          className="icon-button"
          aria-label={`Open ${APP_NAMES[app.id]} in a terminal`}
          title={`Open ${APP_NAMES[app.id]} in a terminal`}
          onClick={() => openTerminal({ title: APP_NAMES[app.id], command: TERMINAL_AGENTS[app.id] })}
        >
          <SquareTerminal size={15} />
        </button>
      )}
      {app.installed && (
        <button type="button" className={`button ${connected ? 'ghost' : 'primary'}`} disabled={busy} onClick={toggle}>
          {busy && <Loader2 size={14} className="spin" />}
          {connected ? 'Disconnect' : 'Connect'}
        </button>
      )}
    </div>
  );
}

export function Agents() {
  const session = useSession();
  const [apps, setApps] = useState<AgentApp[] | null>(null);
  const [appsError, setAppsError] = useState<string>();
  const [policy, setPolicy] = useState<string>();
  const [copied, setCopied] = useState(false);
  const [refresh, setRefresh] = useState(0);
  const tools = useEngine<CatalogEntry[]>('catalog.list', {}, []);
  const activity = useEngine<Page<HistoryEntry>>(
    session.programId ? 'history.list' : null,
    { programId: session.programId, limit: 100 },
    [session.revision, refresh],
  );

  useEffect(() => {
    let live = true;
    setAppsError(undefined);
    call<{ apps: AgentApp[] }>('__desktop.agents.status').then(
      (r) => live && setApps(r.apps),
      (e) => live && setAppsError(message(e)),
    );
    return () => {
      live = false;
    };
  }, [refresh]);

  useEffect(() => {
    if (!session.project) return;
    call<{ mode: string }>('policy.get').then((r) => setPolicy(r.mode), () => setPolicy(undefined));
  }, [session.project]);

  async function choosePolicy(mode: string) {
    try {
      await call('policy.set', { mode });
      setPolicy(mode);
    } catch (e) {
      setAppsError(message(e));
    }
  }

  const agentActions = (activity.data?.items ?? []).filter((h) => h.actor !== 'desktop');
  const agentTools = (tools.data ?? []).filter((t) => t.class !== 'system' && !t.name.startsWith('policy.'));
  const config = JSON.stringify({ mcpServers: { opcodex: { command: 'opcodex', args: ['mcp'] } } }, null, 2);

  return (
    <div className="settings screen">
      <div className="settings-inner">
        <div className="page-intro">
          <span className="page-icon">
            <Bot size={20} />
          </span>
          <div>
            <h1 className="page-title">Agents</h1>
            <p className="muted">
              Opcodex is a tool your AI agents can use. Connect the apps you work with; they can read any open
              program and, if you allow it, rename, comment and analyse. Everything they do shows up here and can be
              undone.
            </p>
          </div>
        </div>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Agent apps on this computer</h2>
              <p>Connecting adds Opcodex as an MCP server in that app. Restart the app afterwards.</p>
            </div>
            <button type="button" className="button ghost" onClick={() => setRefresh((n) => n + 1)}>
              <RefreshCw size={14} /> Check again
            </button>
          </div>
          {appsError && <p className="notice">{appsError}</p>}
          {!apps && !appsError && (
            <div className="agent-list">
              {[0, 1, 2].map((i) => (
                <div key={i} className="skeleton" style={{ height: 58 }} />
              ))}
            </div>
          )}
          {apps && (
            <div className="agent-list">
              {[...apps]
                .sort((a, b) => Number(b.installed) - Number(a.installed))
                .map((app) => (
                  <AppRow key={app.id} app={app} onChanged={() => setRefresh((n) => n + 1)} />
                ))}
            </div>
          )}
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>
                <ShieldCheck size={16} style={{ verticalAlign: -2 }} /> What agents may change
              </h2>
              <p>{session.project ? `For the project ${session.project.name}.` : 'Open a project to choose this.'}</p>
            </div>
          </div>
          <div className="policy-grid" role="radiogroup" aria-label="Agent permissions">
            {POLICIES.map((p) => (
              <button
                key={p.id}
                type="button"
                role="radio"
                aria-checked={policy === p.id}
                className="choice policy"
                disabled={!session.project}
                onClick={() => choosePolicy(p.id)}
              >
                <span className="choice-label">
                  {p.label}
                  {policy === p.id && <Check size={14} />}
                </span>
                <span className="choice-text">{p.text}</span>
              </button>
            ))}
          </div>
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Recent agent activity</h2>
              <p>Changes made by agents and the command line in the open program.</p>
            </div>
          </div>
          {!session.programId && <p className="muted">Open a program to see what agents have done in it.</p>}
          {session.programId && agentActions.length === 0 && (
            <p className="muted">No agent changes yet. When an agent renames or comments, it appears here.</p>
          )}
          <div className="timeline">
            {agentActions.slice(0, 30).map((h) => (
              <div className="timeline-item" key={h.id}>
                <span className="avatar">
                  <Bot size={12} />
                </span>
                <div>
                  <div>{h.summary ?? h.op}</div>
                  <div className="when">
                    {h.actor} · {new Date(h.time).toLocaleString()}
                  </div>
                </div>
                {(h.op === 'symbol.rename' || h.op === 'comment.set') && (
                  <button
                    type="button"
                    className="icon-button"
                    aria-label="Undo this change"
                    title="Undo this change"
                    onClick={async () => {
                      const info = await call<{ revision: number }>('program.info', { programId: session.programId });
                      await call('history.revert', { programId: session.programId, entryId: h.id, expectedRevision: info.revision }).catch(() => undefined);
                      session.bump();
                    }}
                  >
                    <Undo2 size={13} />
                  </button>
                )}
              </div>
            ))}
          </div>
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>
                <Wrench size={16} style={{ verticalAlign: -2 }} /> Tools agents get
              </h2>
              <p>{agentTools.length} operations, the same ones this app uses.</p>
            </div>
          </div>
          <div className="tool-grid">
            {agentTools.map((t) => (
              <div className="tool" key={t.name}>
                <div className="tool-name">
                  <code>{t.name.replace(/\./g, '_')}</code>
                  <span className={`chip ${t.class === 'read' ? '' : 'warn'}`}>{t.class === 'read' ? 'reads' : 'changes'}</span>
                </div>
                <p>{t.description}</p>
              </div>
            ))}
          </div>
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Other agents and harnesses</h2>
              <p>Anything that speaks MCP over stdio can use Opcodex with this configuration, or run `opcodex mcp` directly.</p>
            </div>
            <button
              type="button"
              className="button"
              onClick={() => navigator.clipboard?.writeText(config).then(() => setCopied(true))}
            >
              <Copy size={14} /> {copied ? 'Copied' : 'Copy'}
            </button>
          </div>
          <pre className="signature">{config}</pre>
        </section>
      </div>
    </div>
  );
}

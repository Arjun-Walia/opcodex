// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useState, type FormEvent } from 'react';
import { Bot, Check, History, Pencil, Undo2, User } from 'lucide-react';
import { call, EngineError } from '../engine/client';
import type { FunctionDetail, HistoryEntry, Page, Xref } from '../engine/types';
import { useEngine, useSession } from '../state/session';

function when(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function RenameForm({ fn, onDone }: { fn: FunctionDetail; onDone: () => void }) {
  const session = useSession();
  const [name, setName] = useState(fn.name);
  const [error, setError] = useState<string>();

  async function submit(event: FormEvent) {
    event.preventDefault();
    try {
      const info = await call<{ revision: number }>('program.info', { programId: session.programId });
      await call('symbol.rename', {
        programId: session.programId,
        address: fn.entry,
        newName: name.trim(),
        expectedRevision: info.revision,
      });
      session.bump();
      onDone();
    } catch (e) {
      setError(e instanceof EngineError ? e.message : 'Rename failed.');
    }
  }

  return (
    <form onSubmit={submit}>
      <div className="rename-form">
        <input className="input" autoFocus value={name} onChange={(e) => setName(e.target.value)} />
        <button type="submit" className="button primary" aria-label="Save name">
          <Check size={14} />
        </button>
      </div>
      {error && <p style={{ color: 'var(--danger)', fontSize: 12, margin: '6px 0 0' }}>{error}</p>}
    </form>
  );
}

export function Inspector() {
  const session = useSession();
  const [renaming, setRenaming] = useState(false);
  const fn = useEngine<FunctionDetail>(
    session.location ? 'function.get' : null,
    { programId: session.programId, address: session.location },
    [session.revision],
  );
  const xrefs = useEngine<Page<Xref>>(
    session.location ? 'xrefs.to' : null,
    { programId: session.programId, address: session.location, limit: 50 },
    [session.revision],
  );
  const history = useEngine<Page<HistoryEntry>>(
    'history.list',
    { programId: session.programId, limit: 12 },
    [session.revision],
  );

  async function revert(entry: HistoryEntry) {
    const info = await call<{ revision: number }>('program.info', { programId: session.programId });
    await call('history.revert', {
      programId: session.programId,
      entryId: entry.id,
      expectedRevision: info.revision,
    }).catch(() => undefined);
    session.bump();
  }

  const detail = fn.data;
  return (
    <aside className="inspector" aria-label="Inspector">
      {detail ? (
        <section className="panel">
          <h2 className="panel-title">
            <span>Function</span>
            <button
              type="button"
              className="icon-button"
              aria-label="Rename function"
              title="Rename (N)"
              onClick={() => setRenaming((r) => !r)}
            >
              <Pencil size={14} />
            </button>
          </h2>
          {renaming ? (
            <RenameForm key={detail.entry} fn={detail} onDone={() => setRenaming(false)} />
          ) : (
            <pre className="signature">{detail.signature}</pre>
          )}
          <dl className="kv" style={{ marginTop: 12 }}>
            <dt>Entry</dt>
            <dd className="mono">{detail.entry}</dd>
            <dt>Size</dt>
            <dd>
              {detail.body.reduce((n, r) => n + (parseInt(r.end, 16) - parseInt(r.start, 16) + 1), 0)} bytes
            </dd>
            <dt>Called by</dt>
            <dd>{detail.callers.length}</dd>
            <dt>Calls</dt>
            <dd>{detail.callees.length}</dd>
          </dl>
        </section>
      ) : (
        <section className="panel">
          <h2 className="panel-title">Function</h2>
          <p className="muted">Select a function to see its signature, callers and history.</p>
        </section>
      )}

      {detail && (
        <section className="panel">
          <h2 className="panel-title">Calls</h2>
          <div className="ref-list">
            {detail.callees.length === 0 && <span className="muted">Calls nothing.</span>}
            {detail.callees.slice(0, 30).map((c) => (
              <button type="button" key={c.entry} className="ref" onClick={() => session.goTo(c.entry)}>
                <span className="fn-name">{c.name}</span>
                <span className="fn-addr">{c.entry}</span>
              </button>
            ))}
          </div>
        </section>
      )}

      {detail && (
        <section className="panel">
          <h2 className="panel-title">
            <span>Referenced from</span>
            <span className="chip">{xrefs.data?.items.length ?? 0}</span>
          </h2>
          <div className="ref-list">
            {xrefs.data?.items.length === 0 && <span className="muted">Nothing refers to this address.</span>}
            {xrefs.data?.items.slice(0, 30).map((x) => {
              const caller = detail.callers.find((c) => x.from >= c.entry);
              return (
                <button
                  type="button"
                  key={x.from + x.type}
                  className="ref"
                  onClick={() => caller && session.goTo(caller.entry)}
                >
                  <span className="fn-addr">{x.from}</span>
                  <span className="muted" style={{ marginLeft: 'auto' }}>
                    {x.type.replace(/_/g, ' ').toLowerCase()}
                  </span>
                </button>
              );
            })}
          </div>
        </section>
      )}

      <section className="panel">
        <h2 className="panel-title">
          <span>
            <History size={12} style={{ verticalAlign: -1 }} /> History
          </span>
        </h2>
        <div className="timeline">
          {history.data?.items.length === 0 && (
            <span className="muted">Changes by you and your agents appear here, and can be undone one by one.</span>
          )}
          {history.data?.items.map((h) => {
            const agent = h.actor.startsWith('mcp:') || h.actor === 'cli';
            return (
              <div className="timeline-item" key={h.id}>
                <span className={`avatar ${agent ? 'agent' : ''}`} title={h.actor}>
                  {agent ? <Bot size={12} /> : <User size={12} />}
                </span>
                <div>
                  <div>{h.summary ?? h.op}</div>
                  <div className="when">
                    {h.actor} · {when(h.time)}
                  </div>
                </div>
                {(h.op === 'symbol.rename' || h.op === 'comment.set') && (
                  <button
                    type="button"
                    className="icon-button"
                    aria-label="Revert this change"
                    title="Revert this change"
                    onClick={() => revert(h)}
                  >
                    <Undo2 size={13} />
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </section>
    </aside>
  );
}

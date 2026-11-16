// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The debugger: emulates the program's code step by step, with breakpoints, registers and stack.
import { useCallback, useEffect, useRef, useState } from 'react';
import { Bug, CircleDot, FastForward, Loader2, Play, Square, StepForward, Redo2, Target } from 'lucide-react';
import { call } from '../engine/client';
import type { DebugState, Instruction } from '../engine/types';
import { useSession } from '../state/session';

const REASONS: Record<DebugState['reason'], string> = {
  started: 'Ready. Step or run when you like.',
  step: 'Paused after a step.',
  breakpoint: 'Paused at a breakpoint.',
  reached: 'Paused at the chosen address.',
  returned: 'The function returned.',
  exited: 'The program exited.',
  limit: 'Paused after the run limit. Run again to continue.',
  error: 'Stopped: the emulator could not go on.',
};

function isMissing(e: unknown) {
  return e instanceof Error && /No debugging session/.test(e.message);
}

export function DebugView() {
  const session = useSession();
  const [state, setState] = useState<DebugState | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<string | null>(null);
  const [listing, setListing] = useState<{ fn: string; items: Instruction[] }>({ fn: '', items: [] });
  const [editing, setEditing] = useState<string | null>(null);
  const rowRefs = useRef<Record<string, HTMLDivElement | null>>({});
  const programId = session.programId;

  const send = useCallback(
    async (method: string, params: Record<string, unknown> = {}) => {
      setBusy(true);
      setError(null);
      try {
        const next = await call<DebugState>(method, { programId, ...params });
        setState(next);
        return next;
      } catch (e) {
        if (isMissing(e)) setState(null);
        else setError(e instanceof Error ? e.message : String(e));
        return null;
      } finally {
        setBusy(false);
      }
    },
    [programId],
  );

  useEffect(() => {
    call<DebugState>('debug.state', { programId }).then(setState, () => setState(null));
  }, [programId]);

  // Keep the listing around the current instruction: the whole function when it's in one.
  useEffect(() => {
    const pc = state?.pc ?? session.location;
    const fn = state?.function ?? '';
    if (!pc || (listing.fn === fn && listing.items.some((i) => i.address === pc))) return;
    let live = true;
    (async () => {
      const start = await call<{ entry: string }>('function.get', { programId, address: pc }).then((f) => f.entry, () => pc);
      let out = await call<{ items: Instruction[] }>('code.disassemble', { programId, address: start, count: 800 });
      if (!out.items.some((i) => i.address === pc)) {
        // A very large function: list from the current instruction instead.
        out = await call<{ items: Instruction[] }>('code.disassemble', { programId, address: pc, count: 800 });
      }
      if (live) setListing({ fn, items: out.items });
    })().catch(() => undefined);
    return () => {
      live = false;
    };
  }, [state?.pc, state?.function, session.location, programId, listing]);

  useEffect(() => {
    if (state?.pc) rowRefs.current[state.pc]?.scrollIntoView({ block: 'center' });
  }, [state?.pc]);

  const startAt = selected ?? session.location;
  const start = () => startAt && send('debug.start', { address: startAt });
  const toggleBreakpoint = (address: string) =>
    state && send(state.breakpoints.includes(address) ? 'debug.breakpoint.clear' : 'debug.breakpoint.set', { address });
  const live = !!state?.running;

  const onKey = (e: KeyboardEvent) => {
    const target = e.target as HTMLElement | null;
    if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) return;
    if (e.key === 'F5' && e.shiftKey) {
      e.preventDefault();
      if (state) send('debug.stop').then(() => setState(null));
    } else if (e.key === 'F5') {
      e.preventDefault();
      if (live) send('debug.run');
      else start();
    } else if (e.key === 'F10' && live) {
      e.preventDefault();
      send('debug.stepOver');
    } else if (e.key === 'F11' && live) {
      e.preventDefault();
      send('debug.step');
    } else if (e.key === 'F9' && selected) {
      e.preventDefault();
      toggleBreakpoint(selected);
    }
  };

  const keyRef = useRef(onKey);
  keyRef.current = onKey;
  useEffect(() => {
    const listener = (e: KeyboardEvent) => keyRef.current(e);
    window.addEventListener('keydown', listener);
    return () => window.removeEventListener('keydown', listener);
  }, []);

  if (!session.location && !state)
    return (
      <div className="empty-state">
        <div>
          <Bug size={26} />
          <h4>Pick a function to debug</h4>
          <p>Opcodex emulates its code, so nothing runs on this computer.</p>
        </div>
      </div>
    );

  return (
    <div className="debug-view" aria-label="Debugger">
      <div className="debug-toolbar">
        {!state ? (
          <button type="button" className="button primary" onClick={start} disabled={busy || !startAt}>
            <Play size={14} /> Start debugging
          </button>
        ) : (
          <>
            <button type="button" className="button" onClick={() => send('debug.run')} disabled={busy || !live} title="Run (F5)">
              <FastForward size={14} /> Run
            </button>
            <button type="button" className="button" onClick={() => send('debug.stepOver')} disabled={busy || !live} title="Step over (F10)">
              <Redo2 size={14} /> Step over
            </button>
            <button type="button" className="button" onClick={() => send('debug.step')} disabled={busy || !live} title="Step into (F11)">
              <StepForward size={14} /> Step into
            </button>
            <button type="button" className="button" onClick={() => selected && send('debug.runTo', { address: selected })}
              disabled={busy || !live || !selected} title="Run to the selected line">
              <Target size={14} /> Run to line
            </button>
            <button type="button" className="button" onClick={start} disabled={busy || !startAt} title="Start again from the selected line or function">
              <Play size={14} /> Restart
            </button>
            <button type="button" className="button danger" onClick={() => send('debug.stop').then(() => setState(null))} disabled={busy} title="Stop (Shift+F5)">
              <Square size={13} /> Stop
            </button>
          </>
        )}
        <span className="toolbar-spacer" />
        {busy && <Loader2 size={14} className="spin" aria-label="Working" />}
        <span className="debug-status">
          {state ? (
            <>
              <strong>{REASONS[state.reason]}</strong> {state.steps.toLocaleString()} instructions
            </>
          ) : (
            'Emulated: nothing runs on this computer. F9 sets a breakpoint on the selected line.'
          )}
        </span>
      </div>
      {error && (
        <div className="banner" role="alert">
          {error}
        </div>
      )}
      <div className="debug-body">
        <div className="debug-listing" role="listbox" aria-label="Instructions">
          {listing.items.map((ins) => {
            const isPc = ins.address === state?.pc;
            const bp = state?.breakpoints.includes(ins.address);
            return (
              <div
                key={ins.address}
                ref={(el) => (rowRefs.current[ins.address] = el)}
                role="option"
                aria-selected={selected === ins.address}
                className={`debug-row${isPc ? ' is-pc' : ''}${selected === ins.address ? ' selected' : ''}`}
                onClick={() => setSelected(ins.address)}
              >
                <button
                  type="button"
                  className={`bp-gutter${bp ? ' on' : ''}`}
                  aria-label={bp ? `Remove breakpoint at ${ins.address}` : `Set breakpoint at ${ins.address}`}
                  onClick={(e) => {
                    e.stopPropagation();
                    if (state) toggleBreakpoint(ins.address);
                  }}
                  disabled={!state}
                >
                  <CircleDot size={11} />
                </button>
                <span className="asm-addr">{ins.address}</span>
                <span className="asm-mn">{ins.mnemonic.toLowerCase()}</span>
                <span className="asm-ops">{ins.operands}</span>
              </div>
            );
          })}
        </div>
        <aside className="debug-side">
          <section>
            <h5>Registers</h5>
            {!state && <p className="muted">Start debugging to see registers.</p>}
            <div className="reg-grid">
              {state?.registers.map((r) => (
                <div key={r.name} className={`reg${state.changed.includes(r.name) ? ' changed' : ''}`}>
                  <span className="reg-name">{r.name}</span>
                  {editing === r.name ? (
                    <input
                      className="reg-input mono"
                      autoFocus
                      defaultValue={r.value}
                      onBlur={() => setEditing(null)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          send('debug.register.set', { name: r.name, value: (e.target as HTMLInputElement).value });
                          setEditing(null);
                        } else if (e.key === 'Escape') setEditing(null);
                      }}
                    />
                  ) : (
                    <span className="reg-value mono" title="Double-click to change" onDoubleClick={() => live && setEditing(r.name)}>
                      {r.value}
                    </span>
                  )}
                </div>
              ))}
            </div>
          </section>
          <section>
            <h5>Stack</h5>
            {state?.stack.map((s) => (
              <div key={s.address} className="stack-row mono">
                <span className="muted">+{s.offset.toString(16)}</span>
                <span>{s.value}</span>
              </div>
            ))}
          </section>
          <section>
            <h5>Breakpoints</h5>
            {state && state.breakpoints.length === 0 && <p className="muted">None. Click the dot beside a line, or press F9.</p>}
            {state?.breakpoints.map((b) => (
              <div key={b} className="stack-row mono">
                <button type="button" className="link-button" onClick={() => setSelected(b)}>
                  {b}
                </button>
                <button type="button" className="icon-button" aria-label={`Remove breakpoint at ${b}`} onClick={() => toggleBreakpoint(b)}>
                  ×
                </button>
              </div>
            ))}
          </section>
          {state && state.events.length > 0 && (
            <section>
              <h5>Notes</h5>
              {state.events.map((e, i) => (
                <p key={i} className="debug-note">
                  {e}
                </p>
              ))}
            </section>
          )}
        </aside>
      </div>
    </div>
  );
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// A real terminal in the app: each tab is a shell on a pseudo-terminal in the native shell,
// with `opcodex` on its PATH and attached to this app's engine.
import { useEffect, useRef, useState, type PointerEvent as ReactPointerEvent } from 'react';
import { Plus, SquareTerminal, X } from 'lucide-react';
import { Terminal } from '@xterm/xterm';
import { FitAddon } from '@xterm/addon-fit';
import '@xterm/xterm/css/xterm.css';
import { isDesktopApp } from '../engine/client';
import { usePrefs } from '../state/prefs';

export interface TerminalRequest {
  title: string;
  command?: string;
}

/** Opens a terminal tab from anywhere in the app, optionally running a command. */
export function openTerminal(request: TerminalRequest = { title: 'Terminal' }): void {
  window.dispatchEvent(new CustomEvent<TerminalRequest>('opcodex:terminal', { detail: request }));
}

interface Session extends TerminalRequest {
  key: number;
}

function cssVar(name: string): string {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function xtermTheme() {
  return {
    background: cssVar('--bg-sunken') || '#0d0e10',
    foreground: cssVar('--text') || '#ececee',
    cursor: cssVar('--text') || '#ececee',
    selectionBackground: '#3a3f4b',
  };
}

function TerminalView({ session, active, onExit }: { session: Session; active: boolean; onExit: () => void }) {
  const host = useRef<HTMLDivElement>(null);
  const term = useRef<Terminal>();
  const fit = useRef<FitAddon>();
  const prefs = usePrefs();

  useEffect(() => {
    const element = host.current;
    if (!element) return;
    const xterm = new Terminal({
      fontFamily: "'JetBrains Mono', 'Cascadia Code', monospace",
      fontSize: 13,
      cursorBlink: true,
      allowProposedApi: false,
      theme: xtermTheme(),
    });
    const fitter = new FitAddon();
    xterm.loadAddon(fitter);
    xterm.open(element);
    fitter.fit();
    term.current = xterm;
    fit.current = fitter;

    let id: number | undefined;
    const cleanups: (() => void)[] = [];
    let disposed = false;
    (async () => {
      const { invoke } = await import('@tauri-apps/api/core');
      const { listen } = await import('@tauri-apps/api/event');
      id = await invoke<number>('terminal_open', { cols: xterm.cols, rows: xterm.rows, command: session.command ?? null });
      if (disposed) {
        invoke('terminal_close', { id });
        return;
      }
      cleanups.push(await listen<string>(`terminal-data-${id}`, (e) => xterm.write(e.payload)));
      cleanups.push(await listen(`terminal-exit-${id}`, () => onExit()));
      const input = xterm.onData((data) => invoke('terminal_write', { id, data }));
      const resize = xterm.onResize(({ cols, rows }) => invoke('terminal_resize', { id, cols, rows }));
      cleanups.push(() => input.dispose(), () => resize.dispose());
    })().catch((e) => xterm.write(`\r\nCouldn't start the terminal: ${String(e)}\r\n`));

    const observer = new ResizeObserver(() => {
      try {
        fitter.fit();
      } catch {
        // Hidden tabs have no size.
      }
    });
    observer.observe(element);
    return () => {
      disposed = true;
      observer.disconnect();
      cleanups.forEach((fn) => fn());
      if (id !== undefined) import('@tauri-apps/api/core').then(({ invoke }) => invoke('terminal_close', { id }));
      xterm.dispose();
    };
    // One terminal per session for its whole life.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (term.current) term.current.options.theme = xtermTheme();
  }, [prefs.theme]);

  useEffect(() => {
    if (active) {
      requestAnimationFrame(() => {
        fit.current?.fit();
        term.current?.focus();
      });
    }
  }, [active]);

  return <div className={`term-host ${active ? 'active' : ''}`} ref={host} />;
}

export function TerminalPanel({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [sessions, setSessions] = useState<Session[]>([]);
  const [active, setActive] = useState<number | null>(null);
  const [height, setHeight] = useState(300);
  const next = useRef(1);

  function add(request: TerminalRequest) {
    const session = { ...request, key: next.current++ };
    setSessions((list) => [...list, session]);
    setActive(session.key);
  }

  useEffect(() => {
    const onRequest = (e: Event) => add((e as CustomEvent<TerminalRequest>).detail);
    window.addEventListener('opcodex:terminal', onRequest);
    return () => window.removeEventListener('opcodex:terminal', onRequest);
  }, []);

  useEffect(() => {
    if (open && sessions.length === 0 && isDesktopApp()) add({ title: 'Terminal' });
    // Open a first shell when the panel first appears.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  function close(key: number) {
    setSessions((list) => {
      const rest = list.filter((s) => s.key !== key);
      if (active === key) setActive(rest[rest.length - 1]?.key ?? null);
      if (rest.length === 0) onClose();
      return rest;
    });
  }

  function startResize(event: ReactPointerEvent) {
    event.preventDefault();
    const startY = event.clientY;
    const startHeight = height;
    const move = (e: PointerEvent) => setHeight(Math.min(window.innerHeight * 0.75, Math.max(140, startHeight + startY - e.clientY)));
    const up = () => {
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', up);
    };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
  }

  return (
    <section className={`terminal-panel ${open ? 'open' : ''}`} style={{ height: open ? height : 0 }} aria-hidden={!open} aria-label="Terminal">
      <div className="terminal-resize" onPointerDown={startResize} />
      <div className="terminal-bar">
        <div className="terminal-tabs" role="tablist">
          {sessions.map((s) => (
            <div key={s.key} role="tab" aria-selected={s.key === active} className="terminal-tab" onClick={() => setActive(s.key)}>
              <SquareTerminal size={13} />
              <span>{s.title}</span>
              <button type="button" className="icon-button" aria-label={`Close ${s.title}`} onClick={(e) => { e.stopPropagation(); close(s.key); }}>
                <X size={12} />
              </button>
            </div>
          ))}
          {isDesktopApp() && (
            <button type="button" className="icon-button" aria-label="New terminal" onClick={() => add({ title: 'Terminal' })}>
              <Plus size={14} />
            </button>
          )}
        </div>
        <button type="button" className="icon-button" aria-label="Hide terminal" title="Hide (Ctrl `)" onClick={onClose}>
          <X size={14} />
        </button>
      </div>
      <div className="terminal-body">
        {!isDesktopApp() ? (
          <div className="empty-state">
            <div>
              <SquareTerminal size={24} />
              <h4>The terminal runs in the Opcodex app</h4>
              <p>This browser preview can't open a shell. In the app, each tab is a real terminal with opcodex ready to use.</p>
            </div>
          </div>
        ) : (
          sessions.map((s) => <TerminalView key={s.key} session={s} active={s.key === active} onExit={() => close(s.key)} />)
        )}
      </div>
    </section>
  );
}

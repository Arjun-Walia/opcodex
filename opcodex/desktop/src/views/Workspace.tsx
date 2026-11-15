// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useLayoutEffect, useRef, useState, type PointerEvent as ReactPointerEvent } from 'react';
import { PanelLeft, PanelRight, X } from 'lucide-react';
import { DecompileView, DisassemblyView, HexView } from '../components/CodeViews';
import { FunctionList } from '../components/FunctionList';
import { Inspector } from '../components/Inspector';
import { StringsView } from '../components/StringsView';
import { usePrefs } from '../state/prefs';
import { useSession, type Tab } from '../state/session';
import { Overview } from './Overview';

const TABS: { id: Tab; label: string; key: string }[] = [
  { id: 'overview', label: 'Overview', key: '1' },
  { id: 'decompile', label: 'Code', key: '2' },
  { id: 'disassembly', label: 'Assembly', key: '3' },
  { id: 'hex', label: 'Bytes', key: '4' },
  { id: 'strings', label: 'Strings', key: '5' },
];

function Tabs() {
  const session = useSession();
  const refs = useRef<Record<string, HTMLButtonElement | null>>({});
  const [pill, setPill] = useState({ left: 0, width: 0 });
  useLayoutEffect(() => {
    const el = refs.current[session.tab];
    if (el) setPill({ left: el.offsetLeft, width: el.offsetWidth });
  }, [session.tab]);
  return (
    <div className="tab-group" role="tablist" aria-label="Views">
      <span className="tab-pill" style={{ transform: `translateX(${pill.left}px)`, width: pill.width }} aria-hidden />
      {TABS.map((tab) => (
        <button
          type="button"
          key={tab.id}
          ref={(el) => (refs.current[tab.id] = el)}
          role="tab"
          className="tab"
          aria-selected={session.tab === tab.id}
          onClick={() => session.setTab(tab.id)}
          title={`${tab.label} (Alt ${tab.key})`}
        >
          {tab.label}
        </button>
      ))}
    </div>
  );
}

/** A thin handle between panels; dragging it resizes the panel and saves the width. */
function Splitter({ side }: { side: 'left' | 'right' }) {
  const prefs = usePrefs();
  function start(event: ReactPointerEvent) {
    event.preventDefault();
    const startX = event.clientX;
    const startWidth = side === 'left' ? prefs.layout.sidebarWidth : prefs.layout.inspectorWidth;
    const root = document.documentElement;
    root.classList.add('resizing');
    function move(e: PointerEvent) {
      const delta = side === 'left' ? e.clientX - startX : startX - e.clientX;
      const width = Math.min(480, Math.max(side === 'left' ? 200 : 240, startWidth + delta));
      root.style.setProperty(side === 'left' ? '--sidebar-width' : '--inspector-width', `${width}px`);
    }
    function up(e: PointerEvent) {
      window.removeEventListener('pointermove', move);
      window.removeEventListener('pointerup', up);
      root.classList.remove('resizing');
      const delta = side === 'left' ? e.clientX - startX : startX - e.clientX;
      const width = startWidth + delta;
      prefs.setLayout({
        ...prefs.layout,
        ...(side === 'left' ? { sidebarWidth: width } : { inspectorWidth: width }),
      });
    }
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
  }
  return <div className={`splitter ${side}`} role="separator" aria-orientation="vertical" onPointerDown={start} />;
}

export function Workspace() {
  const session = useSession();
  const prefs = usePrefs();
  const { layout } = prefs;
  if (!session.programId) {
    return (
      <div className="empty-state screen" style={{ flex: 1 }}>
        <div>
          <h4>No program in this project yet</h4>
          <p>Go Home and open a program to add it.</p>
        </div>
      </div>
    );
  }
  const toggle = (panel: 'functions' | 'inspector') =>
    prefs.setLayout({ ...layout, [panel]: !layout[panel], id: 'custom', name: 'Your own', description: 'Arranged by you.' });

  return (
    <div className={`workspace screen ${layout.functions ? '' : 'hide-functions'} ${layout.inspector ? '' : 'hide-inspector'}`}>
      <div className="slot slot-left">
        <FunctionList />
        <Splitter side="left" />
      </div>
      <section className="center">
        <div className="tabs">
          <button type="button" className="icon-button" aria-label="Show or hide functions" aria-pressed={layout.functions} onClick={() => toggle('functions')}>
            <PanelLeft size={15} />
          </button>
          <Tabs />
          <span className="spacer" />
          {session.programs.length > 1 && (
            <select
              className="input"
              style={{ width: 200, height: 30 }}
              value={session.programId}
              onChange={(e) => session.selectProgram(e.target.value)}
              aria-label="Program"
            >
              {session.programs.map((p) => (
                <option key={p.programId} value={p.programId}>
                  {p.name}
                </option>
              ))}
            </select>
          )}
          <button type="button" className="icon-button" aria-label="Show or hide details" aria-pressed={layout.inspector} onClick={() => toggle('inspector')}>
            <PanelRight size={15} />
          </button>
          <button type="button" className="icon-button" aria-label="Close project" onClick={session.closeProject}>
            <X size={15} />
          </button>
        </div>
        <div className="view" role="tabpanel" key={session.tab === 'strings' ? 'strings' : `${session.tab}:${session.location ?? ''}`}>
          {session.tab === 'overview' && <Overview />}
          {session.tab === 'decompile' && <DecompileView />}
          {session.tab === 'disassembly' && <DisassemblyView />}
          {session.tab === 'hex' && <HexView />}
          {session.tab === 'strings' && <StringsView />}
        </div>
      </section>
      <div className="slot slot-right">
        <Splitter side="right" />
        <Inspector />
      </div>
    </div>
  );
}

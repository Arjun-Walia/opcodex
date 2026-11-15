// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { Binary, Braces, Gauge, Hash, X } from 'lucide-react';
import { DecompileView, DisassemblyView, HexView } from '../components/CodeViews';
import { FunctionList } from '../components/FunctionList';
import { Inspector } from '../components/Inspector';
import { useSession, type Tab } from '../state/session';
import { Overview } from './Overview';

const TABS: { id: Tab; label: string; icon: JSX.Element; key: string }[] = [
  { id: 'overview', label: 'Overview', icon: <Gauge size={14} />, key: '1' },
  { id: 'decompile', label: 'Decompiled', icon: <Braces size={14} />, key: '2' },
  { id: 'disassembly', label: 'Disassembly', icon: <Binary size={14} />, key: '3' },
  { id: 'hex', label: 'Hex', icon: <Hash size={14} />, key: '4' },
];

export function Workspace() {
  const session = useSession();
  if (!session.programId) {
    return (
      <div className="empty-state" style={{ flex: 1 }}>
        <div>
          <h4>This project has no programs yet</h4>
          <p>Go Home and open a program to add it.</p>
        </div>
      </div>
    );
  }
  return (
    <div className="workspace">
      <FunctionList />
      <section className="center">
        <div className="tabs" role="tablist" aria-label="Views">
          {TABS.map((tab) => (
            <button
              type="button"
              key={tab.id}
              role="tab"
              className="tab"
              aria-selected={session.tab === tab.id}
              onClick={() => session.setTab(tab.id)}
              title={`${tab.label} (Alt ${tab.key})`}
            >
              {tab.icon}
              {tab.label}
            </button>
          ))}
          <span className="spacer" />
          {session.programs.length > 1 && (
            <select
              className="input"
              style={{ width: 220, height: 30 }}
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
          <button type="button" className="icon-button" aria-label="Close project" onClick={session.closeProject}>
            <X size={15} />
          </button>
        </div>
        <div className="view" role="tabpanel">
          {session.tab === 'overview' && <Overview />}
          {session.tab === 'decompile' && <DecompileView />}
          {session.tab === 'disassembly' && <DisassemblyView />}
          {session.tab === 'hex' && <HexView />}
        </div>
      </section>
      <Inspector />
    </div>
  );
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import type { ReactNode } from 'react';
import { ArrowLeft, ArrowRight, House, LayoutGrid, Search, Settings } from 'lucide-react';
import { useSession } from '../state/session';

export type Screen = 'home' | 'workspace' | 'settings';

interface FrameProps {
  screen: Screen;
  onNavigate: (screen: Screen) => void;
  onPalette: () => void;
  children: ReactNode;
}

function RailButton(props: {
  label: string;
  shortcut?: string;
  current?: boolean;
  disabled?: boolean;
  onClick: () => void;
  children: ReactNode;
}) {
  return (
    <button
      type="button"
      className="rail-button"
      aria-label={props.label}
      aria-current={props.current ? 'page' : undefined}
      disabled={props.disabled}
      onClick={props.onClick}
    >
      {props.children}
      <span className="tooltip" role="tooltip">
        {props.label}
        {props.shortcut && <kbd>{props.shortcut}</kbd>}
      </span>
    </button>
  );
}

export function Frame({ screen, onNavigate, onPalette, children }: FrameProps) {
  const session = useSession();
  const program = session.programs.find((p) => p.programId === session.programId);
  const busy = !!session.activity;
  const inWorkspace = screen === 'workspace';

  return (
    <div className="frame">
      <nav className="rail" aria-label="Main">
        <div className="rail-logo" aria-hidden>
          ox
        </div>
        <RailButton label="Home" current={screen === 'home'} onClick={() => onNavigate('home')}>
          <House size={18} />
        </RailButton>
        <RailButton
          label="Workspace"
          current={inWorkspace}
          disabled={!session.project}
          onClick={() => onNavigate('workspace')}
        >
          <LayoutGrid size={18} />
        </RailButton>
        <RailButton label="Search" shortcut="Ctrl K" onClick={onPalette}>
          <Search size={18} />
        </RailButton>
        <div className="rail-spacer" />
        <RailButton label="Settings" shortcut="Ctrl ," current={screen === 'settings'} onClick={() => onNavigate('settings')}>
          <Settings size={18} />
        </RailButton>
      </nav>

      <header className="topbar">
        {inWorkspace && (
          <>
            <button type="button" className="icon-button" aria-label="Back" disabled={session.back.length === 0} onClick={session.goBack}>
              <ArrowLeft size={16} />
            </button>
            <button type="button" className="icon-button" aria-label="Forward" disabled={session.forward.length === 0} onClick={session.goForward}>
              <ArrowRight size={16} />
            </button>
          </>
        )}
        <div className="crumbs">
          {inWorkspace && session.project ? (
            <>
              <strong>{program?.name ?? session.project.name}</strong>
              {session.location && (
                <>
                  <span className="sep">/</span>
                  <span className="crumb-fn">{session.location}</span>
                </>
              )}
            </>
          ) : (
            <strong>{screen === 'settings' ? 'Settings' : 'Opcodex'}</strong>
          )}
        </div>
        <button type="button" className="topbar-search" onClick={onPalette}>
          <Search size={14} />
          <span>Search</span>
          <kbd>Ctrl K</kbd>
        </button>
      </header>

      <main className="main">{children}</main>

      <footer className="statusbar">
        <span className={`dot ${busy ? 'busy' : ''}`} aria-hidden />
        <span>{busy ? session.activity?.title : 'Ready'}</span>
        {program?.language && <span className="right">{program.language}</span>}
      </footer>
    </div>
  );
}

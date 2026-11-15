// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import type { ReactNode } from 'react';
import { ArrowLeft, ArrowRight, Bot, House, LayoutGrid, Moon, Search, Sun } from 'lucide-react';
import { useSession } from '../state/session';

interface FrameProps {
  screen: 'home' | 'workspace';
  onHome: () => void;
  onWorkspace: () => void;
  onPalette: () => void;
  theme: 'dark' | 'light';
  onTheme: () => void;
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

export function Frame({ screen, onHome, onWorkspace, onPalette, theme, onTheme, children }: FrameProps) {
  const session = useSession();
  const program = session.programs.find((p) => p.programId === session.programId);
  const busy = !!session.activity;

  return (
    <div className="frame">
      <nav className="rail" aria-label="Main">
        <div className="rail-logo" aria-hidden>
          ox
        </div>
        <RailButton label="Home" current={screen === 'home'} onClick={onHome}>
          <House size={18} />
        </RailButton>
        <RailButton
          label="Workspace"
          current={screen === 'workspace'}
          disabled={!session.project}
          onClick={onWorkspace}
        >
          <LayoutGrid size={18} />
        </RailButton>
        <RailButton label="Search and commands" shortcut="Ctrl K" onClick={onPalette}>
          <Search size={18} />
        </RailButton>
        <RailButton label="Connect an agent (opcodex setup)" onClick={onPalette}>
          <Bot size={18} />
        </RailButton>
        <div className="rail-spacer" />
        <RailButton label={theme === 'dark' ? 'Light theme' : 'Dark theme'} onClick={onTheme}>
          {theme === 'dark' ? <Sun size={17} /> : <Moon size={17} />}
        </RailButton>
      </nav>

      <header className="topbar">
        <button
          type="button"
          className="icon-button"
          aria-label="Back"
          disabled={session.back.length === 0}
          onClick={session.goBack}
        >
          <ArrowLeft size={16} />
        </button>
        <button
          type="button"
          className="icon-button"
          aria-label="Forward"
          disabled={session.forward.length === 0}
          onClick={session.goForward}
        >
          <ArrowRight size={16} />
        </button>
        <div className="crumbs">
          {session.project ? (
            <>
              <span>{session.project.name}</span>
              {program && (
                <>
                  <span className="sep">/</span>
                  <strong>{program.name}</strong>
                </>
              )}
              {session.location && (
                <>
                  <span className="sep">/</span>
                  <span className="crumb-fn">{session.location}</span>
                </>
              )}
            </>
          ) : (
            <strong>Opcodex</strong>
          )}
        </div>
        <button type="button" className="topbar-search" onClick={onPalette}>
          <Search size={14} />
          <span>Search functions, commands…</span>
          <kbd>Ctrl K</kbd>
        </button>
      </header>

      <main className="main">{children}</main>

      <footer className="statusbar">
        <span className={`dot ${busy ? 'busy' : ''}`} aria-hidden />
        <span>{busy ? session.activity?.title : 'Engine ready'}</span>
        {program?.language && <span>{program.language}</span>}
        <div className="right">
          <span>Local only · nothing leaves this computer</span>
          <span>Opcodex Community</span>
        </div>
      </footer>
    </div>
  );
}

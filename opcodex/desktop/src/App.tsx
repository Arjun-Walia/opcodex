// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useState } from 'react';
import { pickPath } from './engine/client';
import { CommandPalette } from './components/CommandPalette';
import { DropTarget } from './components/DropTarget';
import { TerminalPanel } from './components/TerminalPanel';
import { Frame, type Screen } from './components/Frame';
import { ActivityView, AgentSheet } from './components/Overlays';
import { splitProjectPath } from './lib/recent';
import { PrefsProvider, usePrefs } from './state/prefs';
import { SessionProvider, useSession, type Tab } from './state/session';
import { Agents } from './views/Agents';
import { Home } from './views/Home';
import { Settings } from './views/Settings';
import { Welcome } from './views/Welcome';
import { Workspace } from './views/Workspace';

function Shell() {
  const session = useSession();
  const prefs = usePrefs();
  const [screen, setScreen] = useState<Screen>('home');
  const [palette, setPalette] = useState(false);
  const [agent, setAgent] = useState(false);
  const [terminal, setTerminal] = useState(false);

  useEffect(() => {
    if (session.programId && !session.activity) setScreen('workspace');
  }, [session.programId, session.activity]);

  useEffect(() => {
    if (!session.project && screen === 'workspace') setScreen('home');
  }, [session.project, screen]);

  useEffect(() => {
    const tabs: Record<string, Tab> = { '1': 'overview', '2': 'decompile', '3': 'disassembly', '4': 'hex', '5': 'strings', '6': 'graph', '7': 'debug' };
    function onKey(e: KeyboardEvent) {
      const mod = e.ctrlKey || e.metaKey;
      if (mod && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setPalette((p) => !p);
      } else if (e.ctrlKey && e.key === '`') {
        e.preventDefault();
        setTerminal((t) => !t);
      } else if (mod && e.key === ',') {
        e.preventDefault();
        setScreen('settings');
      } else if (e.altKey && tabs[e.key]) {
        e.preventDefault();
        session.setTab(tabs[e.key]);
      } else if (e.altKey && e.key === 'ArrowLeft') {
        session.goBack();
      } else if (e.altKey && e.key === 'ArrowRight') {
        session.goForward();
      }
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [session]);

  useEffect(() => {
    const show = () => setTerminal(true);
    window.addEventListener('opcodex:terminal', show);
    return () => window.removeEventListener('opcodex:terminal', show);
  }, []);

  async function openProgram() {
    setScreen('home');
    const picked = await pickPath({ title: 'Choose a file to analyse' });
    if (picked) await session.openBinary(picked);
  }

  async function openProject() {
    setScreen('home');
    const picked = await pickPath({
      title: 'Open an Opcodex project',
      filters: [{ name: 'Opcodex project', extensions: ['gpr'] }],
    });
    if (picked) {
      const { dir, name } = splitProjectPath(picked);
      await session.openProject(dir, name);
    }
  }

  if (!prefs.onboarded) return <Welcome />;

  let content: JSX.Element;
  if (session.activity) content = <ActivityView key="activity" />;
  else if (screen === 'settings') content = <Settings key="settings" onOpenAgents={() => setScreen('agents')} />;
  else if (screen === 'agents') content = <Agents key="agents" />;
  else if (screen === 'workspace' && session.project) content = <Workspace key="workspace" />;
  else content = <Home key="home" onConnectAgent={() => setScreen('agents')} />;

  const shown: Screen = screen === 'workspace' && !session.project ? 'home' : screen;

  return (
    <>
      <Frame
        screen={shown}
        onNavigate={setScreen}
        onPalette={() => setPalette(true)}
        terminalOpen={terminal}
        onTerminal={() => setTerminal((t) => !t)}
      >
        <div className="main-stack">
          <div className="main-content">{content}</div>
          <TerminalPanel open={terminal} onClose={() => setTerminal(false)} />
        </div>
      </Frame>
      {palette && (
        <CommandPalette
          onClose={() => setPalette(false)}
          onHome={() => setScreen('home')}
          onOpenProgram={openProgram}
          onOpenProject={openProject}
          onSettings={() => setScreen('settings')}
          onAgent={() => setScreen('agents')}
        />
      )}
      {agent && <AgentSheet onClose={() => setAgent(false)} />}
      <DropTarget onOpened={() => setScreen('home')} />
    </>
  );
}

export function App() {
  return (
    <PrefsProvider>
      <SessionProvider>
        <Shell />
      </SessionProvider>
    </PrefsProvider>
  );
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useState } from 'react';
import { isDesktopApp, pickPath } from './engine/client';
import { CommandPalette } from './components/CommandPalette';
import { Frame, type Screen } from './components/Frame';
import { ActivityView, AgentSheet } from './components/Overlays';
import { splitProjectPath } from './lib/recent';
import { PrefsProvider, usePrefs } from './state/prefs';
import { SessionProvider, useSession, type Tab } from './state/session';
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
  const [dragging, setDragging] = useState(false);

  useEffect(() => {
    if (session.programId && !session.activity) setScreen('workspace');
  }, [session.programId, session.activity]);

  useEffect(() => {
    if (!session.project && screen === 'workspace') setScreen('home');
  }, [session.project, screen]);

  useEffect(() => {
    const tabs: Record<string, Tab> = { '1': 'overview', '2': 'decompile', '3': 'disassembly', '4': 'hex' };
    function onKey(e: KeyboardEvent) {
      const mod = e.ctrlKey || e.metaKey;
      if (mod && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setPalette((p) => !p);
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

  // Files dropped onto the window (the app gives real paths; browsers don't).
  useEffect(() => {
    if (!isDesktopApp()) return;
    let unlisten: (() => void) | undefined;
    import('@tauri-apps/api/webview').then(({ getCurrentWebview }) =>
      getCurrentWebview()
        .onDragDropEvent((event) => {
          const { type } = event.payload;
          if (type === 'over' || type === 'enter') setDragging(true);
          else if (type === 'leave') setDragging(false);
          else if (type === 'drop') {
            setDragging(false);
            const path = event.payload.paths[0];
            if (!path) return;
            if (/\.gpr$/i.test(path)) {
              const { dir, name } = splitProjectPath(path);
              session.openProject(dir, name);
            } else session.openBinary(path);
          }
        })
        .then((fn) => (unlisten = fn)),
    );
    return () => unlisten?.();
  }, [session]);

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
  else if (screen === 'settings') content = <Settings key="settings" />;
  else if (screen === 'workspace' && session.project) content = <Workspace key="workspace" />;
  else content = <Home key="home" onConnectAgent={() => setAgent(true)} />;

  const shown: Screen = screen === 'workspace' && !session.project ? 'home' : screen;

  return (
    <>
      <Frame screen={shown} onNavigate={setScreen} onPalette={() => setPalette(true)}>
        {content}
      </Frame>
      {palette && (
        <CommandPalette
          onClose={() => setPalette(false)}
          onHome={() => setScreen('home')}
          onOpenProgram={openProgram}
          onOpenProject={openProject}
          onSettings={() => setScreen('settings')}
          onAgent={() => setAgent(true)}
        />
      )}
      {agent && <AgentSheet onClose={() => setAgent(false)} />}
      {dragging && <div className="drop-overlay">Drop to open</div>}
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

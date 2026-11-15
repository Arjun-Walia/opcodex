// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useState } from 'react';
import { isDesktopApp, pickPath } from './engine/client';
import { CommandPalette } from './components/CommandPalette';
import { Frame } from './components/Frame';
import { ActivityView, AgentSheet } from './components/Overlays';
import { splitProjectPath } from './lib/recent';
import { SessionProvider, useSession, type Tab } from './state/session';
import { Home } from './views/Home';
import { Workspace } from './views/Workspace';

type Theme = 'dark' | 'light';
const THEME_KEY = 'opcodex.theme';

function initialTheme(): Theme {
  try {
    const saved = localStorage.getItem(THEME_KEY);
    if (saved === 'light' || saved === 'dark') return saved;
  } catch {
    // Use the default.
  }
  return 'dark';
}

function Shell() {
  const session = useSession();
  const [screen, setScreen] = useState<'home' | 'workspace'>('home');
  const [palette, setPalette] = useState(false);
  const [agent, setAgent] = useState(false);
  const [dragging, setDragging] = useState(false);
  const [theme, setTheme] = useState<Theme>(initialTheme);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch {
      // Not persisted.
    }
  }, [theme]);

  useEffect(() => {
    if (session.programId && !session.activity) setScreen('workspace');
  }, [session.programId, session.activity]);

  useEffect(() => {
    const tabs: Record<string, Tab> = { '1': 'overview', '2': 'decompile', '3': 'disassembly', '4': 'hex' };
    function onKey(e: KeyboardEvent) {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setPalette((p) => !p);
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

  // Files dropped onto the window (Tauri gives real paths; browsers don't).
  useEffect(() => {
    if (!isDesktopApp()) return;
    let unlisten: (() => void) | undefined;
    import('@tauri-apps/api/webview').then(({ getCurrentWebview }) =>
      getCurrentWebview()
        .onDragDropEvent((event) => {
          if (event.payload.type === 'over' || event.payload.type === 'enter') setDragging(true);
          else if (event.payload.type === 'leave') setDragging(false);
          else if (event.payload.type === 'drop') {
            setDragging(false);
            const path = event.payload.paths[0];
            if (path) {
              if (/\.gpr$/i.test(path)) {
                const { dir, name } = splitProjectPath(path);
                session.openProject(dir, name);
              } else session.openBinary(path);
            }
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

  let content: JSX.Element;
  if (session.activity) content = <ActivityView />;
  else if (screen === 'workspace' && session.project) content = <Workspace />;
  else content = <Home onConnectAgent={() => setAgent(true)} />;

  return (
    <>
      <Frame
        screen={screen === 'workspace' && session.project ? 'workspace' : 'home'}
        onHome={() => setScreen('home')}
        onWorkspace={() => setScreen('workspace')}
        onPalette={() => setPalette(true)}
        theme={theme}
        onTheme={() => setTheme((t) => (t === 'dark' ? 'light' : 'dark'))}
      >
        {content}
      </Frame>
      {palette && (
        <CommandPalette
          onClose={() => setPalette(false)}
          onHome={() => setScreen('home')}
          onOpenProgram={openProgram}
          onOpenProject={openProject}
          onToggleTheme={() => setTheme((t) => (t === 'dark' ? 'light' : 'dark'))}
          onAgent={() => setAgent(true)}
        />
      )}
      {agent && <AgentSheet onClose={() => setAgent(false)} />}
      {dragging && <div className="drop-overlay">Drop to open in Opcodex</div>}
    </>
  );
}

export function App() {
  return (
    <SessionProvider>
      <Shell />
    </SessionProvider>
  );
}

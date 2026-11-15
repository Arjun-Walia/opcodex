// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useState, type FormEvent } from 'react';
import { AlertCircle, Bot, FileCode2, FolderOpen, X } from 'lucide-react';
import { isDesktopApp, pickPath } from '../engine/client';
import { relativeTime, splitProjectPath } from '../lib/recent';
import { useSession } from '../state/session';

export function Home({ onConnectAgent }: { onConnectAgent: () => void }) {
  const session = useSession();
  const [path, setPath] = useState('');
  const [mode, setMode] = useState<'binary' | 'project' | null>(null);

  async function chooseBinary() {
    const picked = await pickPath({ title: 'Choose a file to analyse' });
    if (picked) await session.openBinary(picked);
    else if (!isDesktopApp()) setMode('binary');
  }

  async function chooseProject() {
    const picked = await pickPath({
      title: 'Open an Opcodex project',
      filters: [{ name: 'Opcodex project', extensions: ['gpr'] }],
    });
    if (picked) {
      const { dir, name } = splitProjectPath(picked);
      await session.openProject(dir, name);
    } else if (!isDesktopApp()) setMode('project');
  }

  async function submitPath(event: FormEvent) {
    event.preventDefault();
    const value = path.trim().replace(/^"|"$/g, '');
    if (!value) return;
    if (mode === 'project') {
      const { dir, name } = splitProjectPath(value);
      await session.openProject(dir, name);
    } else {
      await session.openBinary(value);
    }
  }

  return (
    <div className="home screen">
      <div className="home-inner">
        {session.error && (
          <div className="banner" role="alert">
            <AlertCircle size={16} />
            <span style={{ flex: 1 }}>{session.error}</span>
            <button type="button" className="icon-button" aria-label="Dismiss" onClick={session.dismissError}>
              <X size={14} />
            </button>
          </div>
        )}
        <h1 className="hero-title">Open a program to begin</h1>
        <p className="hero-sub">Opcodex finds its functions and turns them into readable code. Nothing leaves this computer.</p>

        <div className="start-grid">
          <button type="button" className="start-card primary" onClick={chooseBinary}>
            <span className="card-icon">
              <FileCode2 size={18} />
            </span>
            <h3>Open a program</h3>
            <p>An .exe, .dll, .so, firmware image or any binary. You can also drop it here.</p>
          </button>
          <button type="button" className="start-card" onClick={chooseProject}>
            <span className="card-icon">
              <FolderOpen size={18} />
            </span>
            <h3>Open a project</h3>
            <p>Pick up where you left off.</p>
          </button>
        </div>

        {mode && (
          <form className="open-form" onSubmit={submitPath}>
            <input
              className="input mono"
              autoFocus
              placeholder={mode === 'project' ? 'Full path of a .gpr project file' : 'Full path of the file to analyse'}
              value={path}
              onChange={(e) => setPath(e.target.value)}
            />
            <button type="submit" className="button primary">
              Open
            </button>
          </form>
        )}

        {session.recent.length > 0 && (
          <>
            <div className="section-title">
              <span>Recent</span>
            </div>
            <div className="recent-list">
              {session.recent.map((recent, i) => (
                <button
                  type="button"
                  key={`${recent.dir}/${recent.name}`}
                  className="recent-item"
                  style={{ animationDelay: `${i * 30}ms` }}
                  onClick={() => session.openProject(recent.dir, recent.name)}
                >
                  <FolderOpen size={15} color="var(--text-faint)" />
                  <strong>{recent.name}</strong>
                  {recent.program && recent.program !== recent.name && <span className="muted">{recent.program}</span>}
                  <span className="meta">{relativeTime(recent.openedAt)}</span>
                </button>
              ))}
            </div>
          </>
        )}

        <button type="button" className="text-link" onClick={onConnectAgent}>
          <Bot size={14} /> Use Opcodex from Claude Code, Codex or Cursor
        </button>
      </div>
    </div>
  );
}

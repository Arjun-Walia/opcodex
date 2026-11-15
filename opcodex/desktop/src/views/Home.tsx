// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useState, type FormEvent } from 'react';
import { AlertCircle, Bot, FileCode2, FolderOpen, Sparkles, X } from 'lucide-react';
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
    <div className="home">
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
        <span className="eyebrow">
          <Sparkles size={14} /> Opcodex Community
        </span>
        <h1 className="hero-title">What are we taking apart today?</h1>
        <p className="hero-sub">
          Drop in a program and Opcodex finds its functions, decompiles them to readable C and shows
          you what it does: everything on this computer, and every step open to your agents.
        </p>

        <div className="start-grid">
          <button type="button" className="start-card primary" onClick={chooseBinary}>
            <span className="card-icon">
              <FileCode2 size={19} />
            </span>
            <h3>Open a program</h3>
            <p>Windows, Linux, macOS, firmware and raw binaries.</p>
            <span className="dropzone-hint">or drop a file anywhere in this window</span>
          </button>
          <button type="button" className="start-card" onClick={chooseProject}>
            <span className="card-icon">
              <FolderOpen size={19} />
            </span>
            <h3>Open a project</h3>
            <p>Continue where you left off, with names, comments and history.</p>
          </button>
          <button type="button" className="start-card agent" onClick={onConnectAgent}>
            <span className="card-icon">
              <Bot size={19} />
            </span>
            <h3>Connect an agent</h3>
            <p>Let Claude Code, Codex or Cursor drive Opcodex through MCP.</p>
          </button>
        </div>

        {mode && (
          <form className="open-form" onSubmit={submitPath}>
            <input
              className="input mono"
              autoFocus
              placeholder={
                mode === 'project' ? 'Full path of a .gpr project file' : 'Full path of the file to analyse'
              }
              value={path}
              onChange={(e) => setPath(e.target.value)}
            />
            <button type="submit" className="button primary">
              Open
            </button>
          </form>
        )}

        <div className="section-title">
          <span>Recent</span>
        </div>
        <div className="recent-list">
          {session.recent.length === 0 && (
            <div className="recent-empty">Projects you open appear here.</div>
          )}
          {session.recent.map((recent) => (
            <button
              type="button"
              key={`${recent.dir}/${recent.name}`}
              className="recent-item"
              onClick={() => session.openProject(recent.dir, recent.name)}
            >
              <FolderOpen size={16} color="var(--text-faint)" />
              <span>
                <strong>{recent.name}</strong>
                {recent.program && <span className="muted"> · {recent.program}</span>}
              </span>
              <span className="meta">{relativeTime(recent.openedAt)}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

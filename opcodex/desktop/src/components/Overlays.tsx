// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useState } from 'react';
import { Bot, Copy, Loader2, ScanSearch, X } from 'lucide-react';
import { useSession } from '../state/session';

export function ActivityView() {
  const session = useSession();
  const activity = session.activity!;
  const known = activity.maximum && activity.maximum > 0;
  const percent = known ? Math.min(100, Math.round(((activity.progress ?? 0) / activity.maximum!) * 100)) : 0;
  return (
    <div style={{ flex: 1, overflow: 'auto' }}>
      <div className="progress-card" role="status" aria-live="polite">
        {activity.jobId ? <ScanSearch size={28} color="var(--accent)" /> : <Loader2 size={28} className="spin" />}
        <h2>{activity.title}</h2>
        <p>{activity.detail}</p>
        <div className={`bar ${known ? '' : 'indeterminate'}`}>
          <i style={known ? { width: `${percent}%` } : undefined} />
        </div>
        {activity.jobId && (
          <button type="button" className="button ghost" style={{ marginTop: 18 }} onClick={session.cancelActivity}>
            Stop analysis
          </button>
        )}
      </div>
    </div>
  );
}

const SETUP = 'opcodex setup';

export function AgentSheet({ onClose }: { onClose: () => void }) {
  const [copied, setCopied] = useState(false);
  return (
    <div className="palette-backdrop" onMouseDown={onClose}>
      <div
        className="palette"
        style={{ padding: 28, maxHeight: 'none' }}
        onMouseDown={(e) => e.stopPropagation()}
        role="dialog"
        aria-label="Connect an agent"
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span className="card-icon" style={{ background: 'var(--agent-soft)', color: 'var(--agent)', width: 34, height: 34, display: 'grid', placeItems: 'center', borderRadius: 9 }}>
            <Bot size={18} />
          </span>
          <h2 style={{ margin: 0, fontSize: 18 }}>Connect an agent</h2>
          <button type="button" className="icon-button" style={{ marginLeft: 'auto' }} aria-label="Close" onClick={onClose}>
            <X size={15} />
          </button>
        </div>
        <p style={{ color: 'var(--text-muted)', margin: '14px 0' }}>
          Opcodex works as an MCP server. Run this once in a terminal to register it with the agent apps on
          this computer (Claude Code, Codex, Cursor, VS Code, Windsurf, Gemini CLI, Claude Desktop). It shows
          what it will change first.
        </p>
        <div style={{ display: 'flex', gap: 8 }}>
          <code className="signature" style={{ flex: 1, margin: 0, fontSize: 14 }}>
            {SETUP}
          </code>
          <button
            type="button"
            className="button"
            onClick={() => navigator.clipboard?.writeText(SETUP).then(() => setCopied(true))}
          >
            <Copy size={14} /> {copied ? 'Copied' : 'Copy'}
          </button>
        </div>
        <p className="muted" style={{ margin: '14px 0 0' }}>
          Agents can read everything. Changes follow each project's approval policy and appear in History, where
          any single change can be undone.
        </p>
      </div>
    </div>
  );
}

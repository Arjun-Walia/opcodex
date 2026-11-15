// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Small drawings of a theme or a layout, so people choose by looking rather than reading.
import type { Layout } from '../layout/layouts';
import { themeVariables, type Theme } from '../themes/themes';

export function ThemePreview({ theme }: { theme: Theme }) {
  const v = themeVariables(theme);
  const c = theme.code;
  const line = (parts: [string, number][]) => (
    <div style={{ display: 'flex', gap: 3, marginBottom: 4 }}>
      {parts.map(([color, w], i) => (
        <i key={i} style={{ display: 'block', height: 4, width: w, borderRadius: 2, background: color }} />
      ))}
    </div>
  );
  return (
    <div className="mini" style={{ background: v['--bg-app'], borderColor: v['--border'] }} aria-hidden>
      <div className="mini-side" style={{ background: v['--bg-panel'], borderColor: v['--border'] }}>
        <i style={{ background: theme.colors.accent, opacity: 0.85 }} />
        <i style={{ background: v['--text-faint'] }} />
        <i style={{ background: v['--text-faint'] }} />
        <i style={{ background: v['--text-faint'] }} />
      </div>
      <div className="mini-code">
        {line([[c.type, 14], [c.function, 22], [c.operator, 6]])}
        {line([[c.keyword, 10], [c.variable, 16], [c.number, 10]])}
        {line([[c.comment, 30]])}
        {line([[c.keyword, 12], [c.string, 24]])}
      </div>
    </div>
  );
}

export function LayoutPreview({ layout }: { layout: Layout }) {
  return (
    <div className="mini layout" aria-hidden>
      {layout.functions && <div className="mini-panel narrow" />}
      <div className="mini-panel main">
        <i />
        <i />
        <i style={{ width: '55%' }} />
        <i style={{ width: '70%' }} />
      </div>
      {layout.inspector && <div className="mini-panel narrow" />}
    </div>
  );
}

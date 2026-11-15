// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useRef, useState, type ChangeEvent } from 'react';
import { Check, Download, Pencil, Plus, Trash2, Upload } from 'lucide-react';
import { LayoutPreview, ThemePreview } from '../components/Previews';
import { LAYOUT_PRESETS, type Layout } from '../layout/layouts';
import { usePrefs } from '../state/prefs';
import { parseThemeFile, serializeTheme, type Theme } from '../themes/themes';
import { ThemeEditor } from './ThemeEditor';

function ThemeCard({ theme, onEdit, onDelete }: { theme: Theme; onEdit?: () => void; onDelete?: () => void }) {
  const prefs = usePrefs();
  const selected = prefs.theme.id === theme.id;
  return (
    <div className="theme-card">
      <button type="button" className="choice" aria-pressed={selected} onClick={() => prefs.setTheme(theme.id)}>
        <ThemePreview theme={theme} />
        <span className="choice-label">
          {theme.name}
          {selected && <Check size={14} />}
        </span>
        <span className="choice-text">{theme.author}</span>
      </button>
      {(onEdit || onDelete) && (
        <div className="theme-card-actions">
          {onEdit && (
            <button type="button" className="icon-button" aria-label={`Edit ${theme.name}`} onClick={onEdit}>
              <Pencil size={13} />
            </button>
          )}
          {onDelete && (
            <button type="button" className="icon-button" aria-label={`Delete ${theme.name}`} onClick={onDelete}>
              <Trash2 size={13} />
            </button>
          )}
        </div>
      )}
    </div>
  );
}

function Switch({ label, checked, onChange }: { label: string; checked: boolean; onChange: (on: boolean) => void }) {
  return (
    <label className="switch-row">
      <span>{label}</span>
      <button type="button" role="switch" aria-checked={checked} className="switch" onClick={() => onChange(!checked)}>
        <i />
      </button>
    </label>
  );
}

export function Settings() {
  const prefs = usePrefs();
  const [editing, setEditing] = useState<Theme | null>(null);
  const [notice, setNotice] = useState<string>();
  const fileInput = useRef<HTMLInputElement>(null);
  const opcodex = prefs.themes.filter((t) => t.source === 'opcodex');
  const community = prefs.themes.filter((t) => t.source === 'community');
  const yours = prefs.themes.filter((t) => t.source === 'custom');

  function custom(update: Partial<Layout>) {
    prefs.setLayout({ ...prefs.layout, ...update, id: 'custom', name: 'Your own', description: 'Arranged by you.' });
  }

  async function importTheme(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    event.target.value = '';
    if (!file) return;
    try {
      const theme = parseThemeFile(await file.text());
      prefs.saveTheme(theme);
      setNotice(`Added ${theme.name}.`);
    } catch (e) {
      setNotice(e instanceof Error ? e.message : 'That file could not be read.');
    }
  }

  function exportTheme() {
    const blob = new Blob([serializeTheme(prefs.theme)], { type: 'application/json' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `${prefs.theme.name.toLowerCase().replace(/[^a-z0-9]+/g, '-')}.opcodex-theme.json`;
    link.click();
    URL.revokeObjectURL(link.href);
  }

  return (
    <div className="settings screen">
      <div className="settings-inner">
        <h1 className="page-title">Settings</h1>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Theme</h2>
              <p>Opcodex themes, palettes from apps people know, and your own.</p>
            </div>
            <div className="row-actions">
              <button type="button" className="button" onClick={() => fileInput.current?.click()}>
                <Upload size={14} /> Import
              </button>
              <button type="button" className="button" onClick={exportTheme}>
                <Download size={14} /> Export
              </button>
              <button
                type="button"
                className="button primary"
                onClick={() => setEditing({ ...prefs.theme, id: `custom-${Date.now().toString(36)}`, name: `${prefs.theme.name} (mine)`, source: 'custom', author: 'You' })}
              >
                <Plus size={14} /> New theme
              </button>
              <input ref={fileInput} type="file" accept=".json,application/json" hidden onChange={importTheme} />
            </div>
          </div>
          {notice && <p className="notice">{notice}</p>}
          <h3 className="group-title">Opcodex</h3>
          <div className="choice-grid">{opcodex.map((t) => <ThemeCard key={t.id} theme={t} />)}</div>
          <h3 className="group-title">Community</h3>
          <div className="choice-grid">{community.map((t) => <ThemeCard key={t.id} theme={t} />)}</div>
          {yours.length > 0 && (
            <>
              <h3 className="group-title">Yours</h3>
              <div className="choice-grid">
                {yours.map((t) => (
                  <ThemeCard key={t.id} theme={t} onEdit={() => setEditing(t)} onDelete={() => prefs.deleteTheme(t.id)} />
                ))}
              </div>
            </>
          )}
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Workspace layout</h2>
              <p>Start from a layout, then adjust it. Drag the edges between panels to resize them.</p>
            </div>
          </div>
          <div className="choice-grid wide">
            {LAYOUT_PRESETS.map((l) => (
              <button key={l.id} type="button" className="choice" aria-pressed={prefs.layout.id === l.id} onClick={() => prefs.setLayout(l)}>
                <LayoutPreview layout={l} />
                <span className="choice-label">
                  {l.name}
                  {prefs.layout.id === l.id && <Check size={14} />}
                </span>
                <span className="choice-text">{l.description}</span>
              </button>
            ))}
          </div>
          <div className="custom-layout">
            <Switch label="Function list" checked={prefs.layout.functions} onChange={(functions) => custom({ functions })} />
            <Switch label="Details panel" checked={prefs.layout.inspector} onChange={(inspector) => custom({ inspector })} />
            <Switch label="Compact rows" checked={prefs.layout.density === 'compact'} onChange={(on) => custom({ density: on ? 'compact' : 'comfortable' })} />
            <label className="switch-row">
              <span>Code size</span>
              <span className="range">
                <input type="range" min={11} max={18} value={prefs.layout.codeSize} onChange={(e) => custom({ codeSize: Number(e.target.value) })} />
                <span className="mono">{prefs.layout.codeSize}px</span>
              </span>
            </label>
          </div>
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Motion and welcome</h2>
              <p>Animations are short and subtle. Turn them off if you prefer stillness.</p>
            </div>
          </div>
          <div className="custom-layout">
            <Switch label="Reduce motion" checked={prefs.reduceMotion} onChange={prefs.setReduceMotion} />
            <label className="switch-row">
              <span>Welcome tour</span>
              <button type="button" className="button" onClick={prefs.restartOnboarding}>
                Show again
              </button>
            </label>
          </div>
        </section>
      </div>
      {editing && <ThemeEditor initial={editing} onClose={() => setEditing(null)} />}
    </div>
  );
}

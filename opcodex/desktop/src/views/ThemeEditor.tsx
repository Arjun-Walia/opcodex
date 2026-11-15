// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { ThemePreview } from '../components/Previews';
import { usePrefs } from '../state/prefs';
import { applyTheme, type Theme } from '../themes/themes';

const BASE_FIELDS: [keyof Theme['colors'], string][] = [
  ['background', 'Background'],
  ['surface', 'Panels'],
  ['border', 'Lines'],
  ['text', 'Text'],
  ['accent', 'Accent'],
  ['danger', 'Warnings'],
];

const CODE_FIELDS: [keyof Theme['code'], string][] = [
  ['keyword', 'Keywords'],
  ['type', 'Types'],
  ['function', 'Functions'],
  ['variable', 'Variables'],
  ['string', 'Strings'],
  ['number', 'Numbers'],
  ['comment', 'Comments'],
  ['operator', 'Symbols'],
];

function ColorField({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
  return (
    <label className="color-field">
      <input type="color" value={value.slice(0, 7)} onChange={(e) => onChange(e.target.value)} />
      <span>{label}</span>
      <code>{value}</code>
    </label>
  );
}

export function ThemeEditor({ initial, onClose }: { initial: Theme; onClose: () => void }) {
  const prefs = usePrefs();
  const [draft, setDraft] = useState<Theme>(initial);
  const original = prefs.theme;

  // Live preview on the whole app; restore the previous theme if the editor is cancelled.
  useEffect(() => applyTheme(draft), [draft]);
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && cancel();
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  });

  function cancel() {
    applyTheme(original);
    onClose();
  }

  return (
    <div className="palette-backdrop" onMouseDown={cancel}>
      <div className="sheet" role="dialog" aria-label="Theme editor" onMouseDown={(e) => e.stopPropagation()}>
        <div className="sheet-head">
          <input
            className="input title-input"
            value={draft.name}
            onChange={(e) => setDraft({ ...draft, name: e.target.value.slice(0, 48) })}
            aria-label="Theme name"
          />
          <div className="segmented" role="radiogroup" aria-label="Light or dark">
            {(['dark', 'light'] as const).map((kind) => (
              <button key={kind} type="button" role="radio" aria-checked={draft.kind === kind} onClick={() => setDraft({ ...draft, kind })}>
                {kind === 'dark' ? 'Dark' : 'Light'}
              </button>
            ))}
          </div>
          <button type="button" className="icon-button" aria-label="Close" onClick={cancel}>
            <X size={15} />
          </button>
        </div>
        <div className="sheet-body">
          <div className="editor-preview">
            <ThemePreview theme={draft} />
            <p className="muted">The whole app previews your changes as you go.</p>
          </div>
          <div>
            <h3 className="group-title">Interface</h3>
            <div className="color-grid">
              {BASE_FIELDS.map(([key, label]) => (
                <ColorField key={key} label={label} value={draft.colors[key]}
                  onChange={(v) => setDraft({ ...draft, colors: { ...draft.colors, [key]: v } })} />
              ))}
            </div>
            <h3 className="group-title">Code</h3>
            <div className="color-grid">
              {CODE_FIELDS.map(([key, label]) => (
                <ColorField key={key} label={label} value={draft.code[key]}
                  onChange={(v) => setDraft({ ...draft, code: { ...draft.code, [key]: v } })} />
              ))}
            </div>
          </div>
        </div>
        <div className="sheet-foot">
          <button type="button" className="button ghost" onClick={cancel}>
            Cancel
          </button>
          <button
            type="button"
            className="button primary"
            onClick={() => {
              prefs.saveTheme({ ...draft, name: draft.name.trim() || 'My theme', source: 'custom' });
              onClose();
            }}
          >
            Save theme
          </button>
        </div>
      </div>
    </div>
  );
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { call } from '../engine/client';
import type { Provider } from '../lib/agent';
import { LAYOUT_PRESETS, normalizeLayout, type Layout } from '../layout/layouts';
import {
  applyTheme,
  BUILT_IN_THEMES,
  DEFAULT_THEME_ID,
  loadCustomThemes,
  parseThemeFile,
  saveCustomThemes,
  type Theme,
} from '../themes/themes';

const KEY = 'opcodex.preferences';

/** Agent chat settings. Keys are not here: they live in the OS credential store. */
export interface AiPrefs {
  provider: Provider;
  models: Record<Provider, string>;
  localUrl: string;
  /** The person agreed that chats with this cloud provider send code and analysis to it. */
  consent: Partial<Record<'anthropic' | 'openai', boolean>>;
}

export const DEFAULT_AI: AiPrefs = {
  provider: 'anthropic',
  models: { anthropic: 'claude-sonnet-5-5', openai: 'gpt-5', local: 'qwen2.5-coder:14b' },
  localUrl: 'http://127.0.0.1:11434/v1',
  consent: {},
};

function normalizeAi(raw: unknown): AiPrefs {
  const r = (raw && typeof raw === 'object' ? raw : {}) as Partial<AiPrefs>;
  const provider = r.provider === 'openai' || r.provider === 'local' ? r.provider : 'anthropic';
  const models = { ...DEFAULT_AI.models };
  for (const p of ['anthropic', 'openai', 'local'] as const) {
    const m = r.models?.[p];
    if (typeof m === 'string' && m.trim() && m.length <= 100) models[p] = m.trim();
  }
  const localUrl = typeof r.localUrl === 'string' && r.localUrl.length <= 200 ? r.localUrl : DEFAULT_AI.localUrl;
  const consent = { anthropic: r.consent?.anthropic === true, openai: r.consent?.openai === true };
  return { provider, models, localUrl, consent };
}

interface Stored {
  ai: AiPrefs;
  themeId: string;
  layout: Layout;
  onboarded: boolean;
  reduceMotion: boolean;
  lineNumbers: boolean;
  wrapCode: boolean;
}

interface Prefs extends Stored {
  themes: Theme[];
  theme: Theme;
  setTheme(id: string): void;
  saveTheme(theme: Theme): void;
  deleteTheme(id: string): void;
  setLayout(layout: Layout): void;
  finishOnboarding(): void;
  restartOnboarding(): void;
  setReduceMotion(on: boolean): void;
  setLineNumbers(on: boolean): void;
  setWrapCode(on: boolean): void;
  setAi(change: Partial<AiPrefs>): void;
  /** Reloads theme files from enabled plugins (after installing or toggling one). */
  reloadPluginThemes(): void;
}

function load(): Stored {
  try {
    const raw = JSON.parse(localStorage.getItem(KEY) ?? '{}') as Partial<Stored>;
    return {
      ai: normalizeAi(raw.ai),
      themeId: typeof raw.themeId === 'string' ? raw.themeId : DEFAULT_THEME_ID,
      layout: normalizeLayout(raw.layout),
      onboarded: raw.onboarded === true,
      reduceMotion: raw.reduceMotion === true,
      lineNumbers: raw.lineNumbers !== false,
      wrapCode: raw.wrapCode === true,
    };
  } catch {
    return {
      ai: DEFAULT_AI,
      themeId: DEFAULT_THEME_ID,
      layout: LAYOUT_PRESETS[0],
      onboarded: false,
      reduceMotion: false,
      lineNumbers: true,
      wrapCode: false,
    };
  }
}

const PrefsContext = createContext<Prefs | null>(null);

export function PrefsProvider({ children }: { children: ReactNode }) {
  const [stored, setStored] = useState<Stored>(load);
  const [custom, setCustom] = useState<Theme[]>(loadCustomThemes);
  const [pluginThemes, setPluginThemes] = useState<Theme[]>([]);
  const [pluginsVersion, setPluginsVersion] = useState(0);
  useEffect(() => {
    let live = true;
    call<{ plugin: string; file: string; json: string }[]>('plugin.themes')
      .then((files) => {
        const parsed = files.flatMap((f) => {
          try {
            const t = parseThemeFile(f.json);
            const id = `plugin-${f.plugin}-${f.file.replace(/\.json$/i, '')}`;
            return [{ ...t, id, source: 'plugin' as const }];
          } catch {
            return [];
          }
        });
        if (live) setPluginThemes(parsed);
      })
      .catch(() => {
        if (live) setPluginThemes([]);
      });
    return () => {
      live = false;
    };
  }, [pluginsVersion]);
  const themes = useMemo(() => [...BUILT_IN_THEMES, ...custom, ...pluginThemes], [custom, pluginThemes]);

  const theme = themes.find((t) => t.id === stored.themeId) ?? BUILT_IN_THEMES[0];

  useEffect(() => {
    try {
      localStorage.setItem(KEY, JSON.stringify(stored));
    } catch {
      // Preferences last for this session only.
    }
  }, [stored]);

  useEffect(() => {
    const root = document.documentElement;
    root.classList.add('theme-changing');
    applyTheme(theme);
    const timer = setTimeout(() => root.classList.remove('theme-changing'), 320);
    return () => clearTimeout(timer);
  }, [theme]);

  useEffect(() => {
    const root = document.documentElement;
    const { layout } = stored;
    root.style.setProperty('--sidebar-width', `${layout.sidebarWidth}px`);
    root.style.setProperty('--inspector-width', `${layout.inspectorWidth}px`);
    root.style.setProperty('--code-size', `${layout.codeSize}px`);
    root.dataset.density = layout.density;
    root.dataset.motion = stored.reduceMotion ? 'reduced' : 'full';
    root.dataset.lineNumbers = stored.lineNumbers ? 'on' : 'off';
    root.dataset.wrap = stored.wrapCode ? 'on' : 'off';
  }, [stored]);

  const value = useMemo<Prefs>(
    () => ({
      ...stored,
      themes,
      theme,
      setTheme: (themeId) => setStored((s) => ({ ...s, themeId })),
      saveTheme: (t) => {
        setCustom((list) => {
          const next = [...list.filter((x) => x.id !== t.id), t];
          saveCustomThemes(next);
          return next;
        });
        setStored((s) => ({ ...s, themeId: t.id }));
      },
      deleteTheme: (id) => {
        setCustom((list) => {
          const next = list.filter((x) => x.id !== id);
          saveCustomThemes(next);
          return next;
        });
        setStored((s) => (s.themeId === id ? { ...s, themeId: DEFAULT_THEME_ID } : s));
      },
      setLayout: (layout) => setStored((s) => ({ ...s, layout: normalizeLayout(layout) })),
      finishOnboarding: () => setStored((s) => ({ ...s, onboarded: true })),
      restartOnboarding: () => setStored((s) => ({ ...s, onboarded: false })),
      setReduceMotion: (reduceMotion) => setStored((s) => ({ ...s, reduceMotion })),
      setLineNumbers: (lineNumbers) => setStored((s) => ({ ...s, lineNumbers })),
      setWrapCode: (wrapCode) => setStored((s) => ({ ...s, wrapCode })),
      setAi: (change) => setStored((s) => ({ ...s, ai: normalizeAi({ ...s.ai, ...change }) })),
      reloadPluginThemes: () => setPluginsVersion((v) => v + 1),
    }),
    [stored, themes, theme],
  );

  return <PrefsContext.Provider value={value}>{children}</PrefsContext.Provider>;
}

export function usePrefs(): Prefs {
  const prefs = useContext(PrefsContext);
  if (!prefs) throw new Error('usePrefs outside PrefsProvider');
  return prefs;
}

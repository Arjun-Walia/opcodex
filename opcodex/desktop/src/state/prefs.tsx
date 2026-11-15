// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { LAYOUT_PRESETS, normalizeLayout, type Layout } from '../layout/layouts';
import {
  applyTheme,
  BUILT_IN_THEMES,
  DEFAULT_THEME_ID,
  loadCustomThemes,
  saveCustomThemes,
  type Theme,
} from '../themes/themes';

const KEY = 'opcodex.preferences';

interface Stored {
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
}

function load(): Stored {
  try {
    const raw = JSON.parse(localStorage.getItem(KEY) ?? '{}') as Partial<Stored>;
    return {
      themeId: typeof raw.themeId === 'string' ? raw.themeId : DEFAULT_THEME_ID,
      layout: normalizeLayout(raw.layout),
      onboarded: raw.onboarded === true,
      reduceMotion: raw.reduceMotion === true,
      lineNumbers: raw.lineNumbers !== false,
      wrapCode: raw.wrapCode === true,
    };
  } catch {
    return {
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
  const themes = useMemo(() => [...BUILT_IN_THEMES, ...custom], [custom]);
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

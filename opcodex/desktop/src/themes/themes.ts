// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Themes are a few base colours; every in-between shade is derived, so a custom theme needs
// only the colours a person would actually pick. Palettes of other apps are their published
// open-source colour values (MIT-licensed themes), credited in `author`.

export interface CodeColors {
  keyword: string;
  type: string;
  function: string;
  variable: string;
  string: string;
  number: string;
  comment: string;
  operator: string;
}

export interface Theme {
  id: string;
  name: string;
  author: string;
  kind: 'dark' | 'light';
  source: 'opcodex' | 'community' | 'custom' | 'plugin';
  colors: {
    background: string;
    surface: string;
    border: string;
    text: string;
    accent: string;
    success: string;
    warning: string;
    danger: string;
  };
  code: CodeColors;
}

function theme(
  id: string,
  name: string,
  author: string,
  kind: 'dark' | 'light',
  source: Theme['source'],
  [background, surface, border, text, accent]: string[],
  [keyword, type, fn, variable, string, number, comment, operator]: string[],
  status: string[] = kind === 'dark' ? ['#6fbf8e', '#d9b26a', '#e07a7a'] : ['#3f8f5f', '#9a6b1f', '#b84a4a'],
): Theme {
  return {
    id,
    name,
    author,
    kind,
    source,
    colors: { background, surface, border, text, accent, success: status[0], warning: status[1], danger: status[2] },
    code: { keyword, type, function: fn, variable, string, number, comment, operator },
  };
}

export const BUILT_IN_THEMES: Theme[] = [
  theme('opcodex-graphite', 'Opcodex Graphite', 'Opcodex', 'dark', 'opcodex',
    ['#111214', '#16171a', '#2a2b30', '#ececee', '#5b8def'],
    ['#c09ad8', '#8fc3b8', '#8fb3e8', '#d9d9de', '#a8c79a', '#d9a386', '#6b6b74', '#a1a1aa']),
  theme('opcodex-paper', 'Opcodex Paper', 'Opcodex', 'light', 'opcodex',
    ['#fafafa', '#ffffff', '#e4e4e7', '#18181b', '#2f6fe0'],
    ['#7c4dab', '#2d7a6d', '#2f5fb3', '#18181b', '#3f7a35', '#a14d24', '#8e8e96', '#52525b']),
  theme('github-dark-dimmed', 'GitHub Dark Dimmed', 'GitHub (Primer)', 'dark', 'community',
    ['#22272e', '#2d333b', '#444c56', '#adbac7', '#539bf5'],
    ['#f47067', '#f69d50', '#dcbdfb', '#adbac7', '#96d0ff', '#6cb6ff', '#768390', '#adbac7']),
  theme('github-light', 'GitHub Light', 'GitHub (Primer)', 'light', 'community',
    ['#ffffff', '#f6f8fa', '#d0d7de', '#1f2328', '#0969da'],
    ['#cf222e', '#953800', '#8250df', '#1f2328', '#0a3069', '#0550ae', '#6e7781', '#1f2328']),
  theme('vscode-dark-modern', 'VS Code Dark Modern', 'Microsoft', 'dark', 'community',
    ['#1f1f1f', '#181818', '#2b2b2b', '#cccccc', '#0078d4'],
    ['#569cd6', '#4ec9b0', '#dcdcaa', '#9cdcfe', '#ce9178', '#b5cea8', '#6a9955', '#d4d4d4']),
  theme('dracula', 'Dracula', 'Zeno Rocha', 'dark', 'community',
    ['#282a36', '#21222c', '#44475a', '#f8f8f2', '#bd93f9'],
    ['#ff79c6', '#8be9fd', '#50fa7b', '#f8f8f2', '#f1fa8c', '#bd93f9', '#6272a4', '#ff79c6'],
    ['#50fa7b', '#ffb86c', '#ff5555']),
  theme('nord', 'Nord', 'Arctic Ice Studio', 'dark', 'community',
    ['#2e3440', '#3b4252', '#434c5e', '#eceff4', '#88c0d0'],
    ['#81a1c1', '#8fbcbb', '#88c0d0', '#d8dee9', '#a3be8c', '#b48ead', '#616e88', '#81a1c1'],
    ['#a3be8c', '#ebcb8b', '#bf616a']),
  theme('one-dark', 'One Dark', 'Atom', 'dark', 'community',
    ['#282c34', '#21252b', '#3e4451', '#abb2bf', '#61afef'],
    ['#c678dd', '#e5c07b', '#61afef', '#e06c75', '#98c379', '#d19a66', '#5c6370', '#56b6c2']),
  theme('monokai', 'Monokai', 'Wimer Hazenberg', 'dark', 'community',
    ['#272822', '#1e1f1c', '#3e3d32', '#f8f8f2', '#a6e22e'],
    ['#f92672', '#66d9ef', '#a6e22e', '#f8f8f2', '#e6db74', '#ae81ff', '#75715e', '#f92672']),
  theme('solarized-dark', 'Solarized Dark', 'Ethan Schoonover', 'dark', 'community',
    ['#002b36', '#073642', '#0e4b5a', '#93a1a1', '#268bd2'],
    ['#859900', '#b58900', '#268bd2', '#93a1a1', '#2aa198', '#d33682', '#586e75', '#859900']),
  theme('solarized-light', 'Solarized Light', 'Ethan Schoonover', 'light', 'community',
    ['#fdf6e3', '#eee8d5', '#d9d2bd', '#586e75', '#268bd2'],
    ['#859900', '#b58900', '#268bd2', '#586e75', '#2aa198', '#d33682', '#93a1a1', '#859900']),
  theme('catppuccin-mocha', 'Catppuccin Mocha', 'Catppuccin', 'dark', 'community',
    ['#1e1e2e', '#181825', '#313244', '#cdd6f4', '#89b4fa'],
    ['#cba6f7', '#f9e2af', '#89b4fa', '#cdd6f4', '#a6e3a1', '#fab387', '#6c7086', '#89dceb'],
    ['#a6e3a1', '#f9e2af', '#f38ba8']),
  theme('tokyo-night', 'Tokyo Night', 'enkia', 'dark', 'community',
    ['#1a1b26', '#16161e', '#292e42', '#c0caf5', '#7aa2f7'],
    ['#bb9af7', '#2ac3de', '#7aa2f7', '#c0caf5', '#9ece6a', '#ff9e64', '#565f89', '#89ddff']),
  theme('gruvbox-dark', 'Gruvbox Dark', 'Pavel Pertsev', 'dark', 'community',
    ['#282828', '#1d2021', '#3c3836', '#ebdbb2', '#fabd2f'],
    ['#fb4934', '#83a598', '#fabd2f', '#ebdbb2', '#b8bb26', '#d3869b', '#928374', '#fe8019'],
    ['#b8bb26', '#fabd2f', '#fb4934']),
  theme('geist-dark', 'Geist Dark', 'Vercel', 'dark', 'community',
    ['#0a0a0a', '#111111', '#262626', '#ededed', '#ededed'],
    ['#c472fb', '#52a8ff', '#ededed', '#ededed', '#62c073', '#ff990a', '#707070', '#a1a1a1']),
];

export const DEFAULT_THEME_ID = 'opcodex-graphite';

const mix = (a: string, b: string, aPercent: number) => `color-mix(in srgb, ${a} ${aPercent}%, ${b})`;

/** CSS custom properties for a theme; shades between the base colours are derived. */
export function themeVariables(t: Theme): Record<string, string> {
  const { background: bg, surface, border, text, accent, success, warning, danger } = t.colors;
  const dark = t.kind === 'dark';
  return {
    '--bg-app': bg,
    '--bg-sunken': mix(bg, dark ? '#000' : text, dark ? 78 : 96),
    '--bg-panel': surface,
    '--bg-raised': mix(surface, text, dark ? 96 : 100),
    '--bg-hover': mix(surface, text, dark ? 93 : 95),
    '--bg-active': mix(surface, text, dark ? 88 : 91),
    '--bg-selection': mix(accent, surface, 14),
    '--bg-selection-strong': mix(accent, surface, 26),
    '--border-subtle': mix(border, bg, 60),
    '--border': border,
    '--border-strong': mix(border, text, 82),
    '--text': text,
    '--text-muted': mix(text, bg, 68),
    '--text-faint': mix(text, bg, 44),
    '--text-on-accent': dark && t.id === 'geist-dark' ? '#0a0a0a' : '#ffffff',
    '--accent': accent,
    '--accent-strong': mix(accent, text, 82),
    '--accent-soft': mix(accent, 'transparent', 12),
    '--ok': success,
    '--warn': warning,
    '--danger': danger,
    '--danger-soft': mix(danger, 'transparent', 11),
    '--warn-soft': mix(warning, 'transparent', 11),
    '--glow-accent': `0 0 0 2px ${mix(accent, 'transparent', 45)}`,
    '--shadow-popover': `0 16px 48px rgba(0, 0, 0, ${dark ? 0.5 : 0.12}), 0 0 0 1px ${border}`,
    '--code-keyword': t.code.keyword,
    '--code-type': t.code.type,
    '--code-function': t.code.function,
    '--code-variable': t.code.variable,
    '--code-param': t.code.number,
    '--code-number': t.code.number,
    '--code-string': t.code.string,
    '--code-comment': t.code.comment,
    '--code-op': t.code.operator,
    '--code-address': mix(text, bg, 44),
    '--code-mnemonic': t.code.function,
    '--code-register': t.code.number,
  };
}

/** Applies a theme to the whole document. */
export function applyTheme(t: Theme, root: HTMLElement = document.documentElement): void {
  for (const [name, value] of Object.entries(themeVariables(t))) root.style.setProperty(name, value);
  root.dataset.theme = t.kind;
  root.style.colorScheme = t.kind;
}

const HEX = /^#(?:[0-9a-f]{3}|[0-9a-f]{6}|[0-9a-f]{8})$/i;

function colour(value: unknown, field: string): string {
  if (typeof value !== 'string' || !HEX.test(value)) {
    throw new Error(`${field} must be a hex colour such as #1e1e2e.`);
  }
  return value;
}

/** Reads a shared theme file, accepting only hex colours, and marks it as yours. */
export function parseThemeFile(text: string): Theme {
  const data = JSON.parse(text) as Record<string, unknown>;
  const colors = data.colors as Record<string, unknown> | undefined;
  const code = data.code as Record<string, unknown> | undefined;
  if (!colors || !code || typeof data.name !== 'string') {
    throw new Error('This is not an Opcodex theme file.');
  }
  const name = data.name.trim().slice(0, 48) || 'Imported theme';
  const pick = <K extends string>(obj: Record<string, unknown>, keys: K[], prefix: string) =>
    Object.fromEntries(keys.map((k) => [k, colour(obj[k], `${prefix}.${k}`)])) as Record<K, string>;
  return {
    id: `custom-${name.toLowerCase().replace(/[^a-z0-9]+/g, '-')}-${Date.now().toString(36)}`,
    name,
    author: typeof data.author === 'string' ? data.author.slice(0, 64) : 'Unknown',
    kind: data.kind === 'light' ? 'light' : 'dark',
    source: 'custom',
    colors: pick(colors, ['background', 'surface', 'border', 'text', 'accent', 'success', 'warning', 'danger'], 'colors'),
    code: pick(code, ['keyword', 'type', 'function', 'variable', 'string', 'number', 'comment', 'operator'], 'code'),
  };
}

export function serializeTheme(t: Theme): string {
  const { name, author, kind, colors, code } = t;
  return JSON.stringify({ format: 'opcodex-theme/1', name, author, kind, source: t.source, colors, code }, null, 2);
}

const CUSTOM_KEY = 'opcodex.customThemes';

export function loadCustomThemes(): Theme[] {
  try {
    const raw = localStorage.getItem(CUSTOM_KEY);
    const list: unknown = raw ? JSON.parse(raw) : [];
    if (!Array.isArray(list)) return [];
    return list.flatMap((item) => {
      try {
        const parsed = parseThemeFile(JSON.stringify(item));
        return [{ ...parsed, id: (item as Theme).id ?? parsed.id }];
      } catch {
        return [];
      }
    });
  } catch {
    return [];
  }
}

export function saveCustomThemes(themes: Theme[]): void {
  try {
    localStorage.setItem(CUSTOM_KEY, JSON.stringify(themes));
  } catch {
    // Not persisted; themes still apply for this session.
  }
}

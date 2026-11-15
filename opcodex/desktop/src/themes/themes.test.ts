// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { describe, expect, it } from 'vitest';
import { BUILT_IN_THEMES, themeVariables, parseThemeFile, serializeTheme, type Theme } from './themes';
import { LAYOUT_PRESETS, normalizeLayout } from '../layout/layouts';

describe('themes', () => {
  it('ships graphite first and the well-known palettes', () => {
    expect(BUILT_IN_THEMES[0].id).toBe('opcodex-graphite');
    const names = BUILT_IN_THEMES.map((t) => t.name);
    for (const name of ['Opcodex Paper', 'GitHub Dark Dimmed', 'GitHub Light', 'Dracula', 'Nord',
      'One Dark', 'Monokai', 'Solarized Dark', 'Solarized Light', 'Catppuccin Mocha',
      'Tokyo Night', 'Gruvbox Dark', 'VS Code Dark Modern', 'Geist Dark']) {
      expect(names).toContain(name);
    }
    expect(new Set(BUILT_IN_THEMES.map((t) => t.id)).size).toBe(BUILT_IN_THEMES.length);
  });

  it('turns a theme into CSS variables, deriving the in-between shades', () => {
    const vars = themeVariables(BUILT_IN_THEMES[0]);
    expect(vars['--bg-app']).toBe('#111214');
    expect(vars['--accent']).toBe('#5b8def');
    expect(vars['--code-keyword']).toBe(BUILT_IN_THEMES[0].code.keyword);
    expect(vars['--bg-hover']).toMatch(/^color-mix\(in srgb, /);
    expect(vars['--text-muted']).toMatch(/^color-mix\(in srgb, /);
  });

  it('round-trips a theme file and rejects anything that is not a colour', () => {
    const custom: Theme = { ...BUILT_IN_THEMES[0], id: 'mine', name: 'Mine', source: 'custom' };
    const parsed = parseThemeFile(serializeTheme(custom));
    expect(parsed.name).toBe('Mine');
    expect(parsed.source).toBe('custom');
    expect(parsed.id).not.toBe('opcodex-graphite');

    const evil = JSON.parse(serializeTheme(custom));
    evil.colors.background = 'red; } body { display: none';
    expect(() => parseThemeFile(JSON.stringify(evil))).toThrow(/colour/);
    expect(() => parseThemeFile('{not json')).toThrow();
    expect(() => parseThemeFile(JSON.stringify({ name: 'x' }))).toThrow();
  });
});

describe('layouts', () => {
  it('offers explore, analyst, focus and review presets', () => {
    expect(LAYOUT_PRESETS.map((l) => l.id)).toEqual(['explore', 'analyst', 'focus', 'review']);
    const focus = LAYOUT_PRESETS.find((l) => l.id === 'focus')!;
    expect(focus.functions).toBe(false);
    expect(focus.inspector).toBe(false);
  });

  it('clamps saved custom layouts to sane sizes', () => {
    const layout = normalizeLayout({ id: 'custom', functions: true, inspector: true, sidebarWidth: 9999,
      inspectorWidth: 10, codeSize: 40, density: 'tight' });
    expect(layout.sidebarWidth).toBe(480);
    expect(layout.inspectorWidth).toBe(240);
    expect(layout.codeSize).toBe(18);
    expect(layout.density).toBe('comfortable');
    expect(normalizeLayout(null).id).toBe('explore');
  });
});

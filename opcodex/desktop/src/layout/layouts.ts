// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

export interface Layout {
  id: 'explore' | 'analyst' | 'focus' | 'review' | 'custom';
  name: string;
  description: string;
  functions: boolean;
  inspector: boolean;
  sidebarWidth: number;
  inspectorWidth: number;
  codeSize: number;
  density: 'comfortable' | 'compact';
}

const BASE = { sidebarWidth: 280, inspectorWidth: 320, codeSize: 13, density: 'comfortable' as const };

export const LAYOUT_PRESETS: Layout[] = [
  { ...BASE, id: 'explore', name: 'Explore', description: 'Functions on the left, code in the middle.',
    functions: true, inspector: false },
  { ...BASE, id: 'analyst', name: 'Analyst', description: 'Adds details, references and history on the right.',
    functions: true, inspector: true },
  { ...BASE, id: 'focus', name: 'Focus', description: 'Just the code. Jump anywhere with Ctrl K.',
    functions: false, inspector: false, codeSize: 14 },
  { ...BASE, id: 'review', name: 'Review', description: 'Code with details and history, for checking changes.',
    functions: false, inspector: true },
];

const clamp = (value: unknown, min: number, max: number, fallback: number) =>
  typeof value === 'number' && Number.isFinite(value) ? Math.min(max, Math.max(min, Math.round(value))) : fallback;

/** Repairs a saved layout so a damaged or hand-edited setting can't break the workspace. */
export function normalizeLayout(saved: unknown): Layout {
  const data = (saved && typeof saved === 'object' ? saved : {}) as Partial<Layout>;
  const preset = LAYOUT_PRESETS.find((p) => p.id === data.id);
  const base = preset ?? (data.id === 'custom' ? { ...LAYOUT_PRESETS[1], id: 'custom' as const, name: 'Your own',
    description: 'Arranged by you.' } : LAYOUT_PRESETS[0]);
  return {
    ...base,
    functions: typeof data.functions === 'boolean' ? data.functions : base.functions,
    inspector: typeof data.inspector === 'boolean' ? data.inspector : base.inspector,
    sidebarWidth: clamp(data.sidebarWidth, 200, 480, base.sidebarWidth),
    inspectorWidth: clamp(data.inspectorWidth, 240, 480, base.inspectorWidth),
    codeSize: clamp(data.codeSize, 11, 18, base.codeSize),
    density: data.density === 'compact' ? 'compact' : 'comfortable',
  };
}

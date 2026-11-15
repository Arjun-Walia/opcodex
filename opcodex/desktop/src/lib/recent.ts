// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

export interface RecentProject {
  dir: string;
  name: string;
  openedAt: number;
  program?: string;
}

const KEY = 'opcodex.recentProjects';
const LIMIT = 12;

export function addRecent(list: RecentProject[], project: RecentProject): RecentProject[] {
  const rest = list.filter((p) => !(p.dir === project.dir && p.name === project.name));
  return [project, ...rest].slice(0, LIMIT);
}

export function loadRecent(): RecentProject[] {
  try {
    const raw = localStorage.getItem(KEY);
    const parsed: unknown = raw ? JSON.parse(raw) : [];
    return Array.isArray(parsed) ? (parsed as RecentProject[]).slice(0, LIMIT) : [];
  } catch {
    return [];
  }
}

export function saveRecent(list: RecentProject[]): void {
  try {
    localStorage.setItem(KEY, JSON.stringify(list));
  } catch {
    // Storage can be unavailable; recents are a convenience.
  }
}

/** Splits `C:\work\demo.gpr` or `/home/me/demo` into the project folder and name. */
export function splitProjectPath(path: string): { dir: string; name: string } {
  const trimmed = path.replace(/[\\/]+$/, '').replace(/\.gpr$/i, '');
  const cut = Math.max(trimmed.lastIndexOf('\\'), trimmed.lastIndexOf('/'));
  return { dir: trimmed.slice(0, cut), name: trimmed.slice(cut + 1) };
}

export function relativeTime(at: number, now = Date.now()): string {
  const minutes = Math.round((now - at) / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes} min ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours} h ago`;
  const days = Math.round(hours / 24);
  return days === 1 ? 'yesterday' : `${days} days ago`;
}

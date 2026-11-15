// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Plain-language observations from the triage summary, most important first.
import type { ProgramSummary } from '../engine/types';

export interface Insight {
  level: 'attention' | 'note' | 'ok';
  title: string;
  detail: string;
}

const CAPABILITIES: Record<string, { title: string; detail: string; level: Insight['level'] }> = {
  'process-injection': {
    title: 'Can write into other processes',
    detail: 'Imports the calls used to allocate and run code inside another program.',
    level: 'attention',
  },
  'anti-debug': {
    title: 'Checks for debuggers',
    detail: 'It may behave differently when you debug it.',
    level: 'attention',
  },
  network: { title: 'Talks over the network', detail: 'Imports socket or web calls.', level: 'note' },
  persistence: {
    title: 'Can make itself start again',
    detail: 'Imports calls that write registry keys, services or hooks.',
    level: 'attention',
  },
  crypto: { title: 'Uses encryption', detail: 'Imports Windows cryptography calls.', level: 'note' },
  execution: { title: 'Starts other programs', detail: 'Imports calls that run commands or programs.', level: 'note' },
  'input-capture': { title: 'Reads the keyboard', detail: 'Imports key-state calls often used to capture input.', level: 'attention' },
  privilege: { title: 'Changes its privileges', detail: 'Imports token and privilege calls.', level: 'note' },
};

const ORDER: Record<Insight['level'], number> = { attention: 0, note: 1, ok: 2 };

export function insightsFor(s: ProgramSummary): Insight[] {
  const out: Insight[] = [];
  const packed = s.blocks.filter((b) => b.initialized && (b.entropy ?? 0) > 7.2);
  if (packed.length) {
    out.push({
      level: 'attention',
      title: 'Code may be packed or encrypted',
      detail: `${packed.map((b) => `${b.name} (${b.entropy?.toFixed(1)} of 8)`).join(', ')} look random. The real code may only appear when it runs.`,
    });
  }
  const rwx = s.blocks.filter((b) => b.permissions === 'rwx');
  if (rwx.length) {
    out.push({
      level: 'attention',
      title: 'Writable and executable memory',
      detail: `${rwx.map((b) => b.name).join(', ')} can be both changed and run, which self-modifying or unpacking code needs.`,
    });
  }
  if (s.imports.length === 0) {
    out.push({
      level: 'note',
      title: 'No imported libraries',
      detail: 'It may load what it needs at run time, which packed and malicious programs often do.',
    });
  }
  const categories = new Set(s.riskyApis.map((r) => r.category));
  for (const category of categories) {
    const known = CAPABILITIES[category];
    if (known) out.push({ level: known.level, title: known.title, detail: known.detail });
  }
  const urls = s.notableStrings.filter((n) => n.kind === 'url').length;
  if (urls) {
    out.push({
      level: 'note',
      title: `${urls} web address${urls === 1 ? '' : 'es'} in its strings`,
      detail: 'See Notable strings below; open Strings to find the code that uses them.',
    });
  }
  const ips = s.notableStrings.filter((n) => n.kind === 'ip').length;
  if (ips) {
    out.push({ level: 'note', title: `${ips} IP address${ips === 1 ? '' : 'es'} in its strings`, detail: 'Possible hard-coded servers.' });
  }
  if (out.length === 0) {
    out.push({
      level: 'ok',
      title: 'Nothing unusual at a glance',
      detail: 'No packing, risky API imports or hard-coded addresses. Read the functions to learn more.',
    });
  }
  return out.sort((a, b) => ORDER[a.level] - ORDER[b.level]);
}

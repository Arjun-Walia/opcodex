// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useMemo, useState } from 'react';
import {
  AlertTriangle, Boxes, ChevronRight, CircleCheck, Cpu, DoorOpen, Flame, Info, Link2, Loader2, Package, Quote, Ruler, Search,
} from 'lucide-react';
import type { FunctionSummary, Page, ProgramSummary } from '../engine/types';
import { insightsFor } from '../lib/insights';
import { useEngine, useSession } from '../state/session';

const CATEGORY_LABEL: Record<string, string> = {
  'process-injection': 'Process injection',
  'anti-debug': 'Anti-debugging',
  network: 'Network',
  persistence: 'Persistence',
  crypto: 'Cryptography',
  execution: 'Runs programs',
  'input-capture': 'Input capture',
  privilege: 'Privileges',
};

function bytes(n: number): string {
  if (n < 1024) return `${n} B`;
  if (n < 1024 * 1024) return `${(n / 1024).toFixed(1)} KB`;
  return `${(n / 1024 / 1024).toFixed(1)} MB`;
}

function Imports({ imports }: { imports: ProgramSummary['imports'] }) {
  const [filter, setFilter] = useState('');
  const [open, setOpen] = useState<string | null>(imports[0]?.library ?? null);
  const needle = filter.trim().toLowerCase();
  const shown = imports
    .map((lib) => ({
      ...lib,
      functions: needle ? lib.functions.filter((f) => f.toLowerCase().includes(needle)) : lib.functions,
    }))
    .filter((lib) => !needle || lib.functions.length > 0 || lib.library.toLowerCase().includes(needle));
  return (
    <>
      <div className="filter" style={{ marginBottom: 10 }}>
        <Search size={14} />
        <input
          className="input"
          placeholder={`Search ${imports.reduce((n, l) => n + l.functions.length, 0)} imports`}
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
        />
      </div>
      <div className="scroll-box">
        {shown.length === 0 && <p className="muted">No matching imports.</p>}
        {shown.map((lib) => {
          const expanded = needle !== '' || open === lib.library;
          return (
            <div className="import-lib" key={lib.library}>
              <button type="button" className="lib-toggle" aria-expanded={expanded}
                onClick={() => setOpen(expanded && !needle ? null : lib.library)}>
                <ChevronRight size={14} className="chev" />
                <span>{lib.library}</span>
                <span className="muted">{lib.functions.length}</span>
              </button>
              <div className={`fn-grid ${expanded ? 'open' : ''}`}>
                <div>
                  {lib.functions.map((f) => (
                    <code key={f}>{f}</code>
                  ))}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
}

function Hotspots({ programId, revision }: { programId: string; revision: number }) {
  const session = useSession();
  const { data } = useEngine<Page<FunctionSummary>>('program.functions.list', { programId, limit: 1000 }, [revision]);
  const items = data?.items ?? [];
  const used = useMemo(() => [...items].sort((a, b) => b.callers - a.callers).slice(0, 6), [items]);
  const largest = useMemo(() => [...items].sort((a, b) => b.size - a.size).slice(0, 6), [items]);
  const row = (fn: FunctionSummary, value: string) => (
    <button type="button" className="ref" key={fn.entry} onClick={() => session.goTo(fn.entry)}>
      <span className="fn-name">{fn.name}</span>
      <span className="muted" style={{ marginLeft: 'auto' }}>{value}</span>
    </button>
  );
  return (
    <>
      <section className="card span-6">
        <h3><Flame size={16} /> Most used functions</h3>
        <div className="ref-list">
          {!data && <div className="skeleton" style={{ height: 120 }} />}
          {used.map((fn) => row(fn, `${fn.callers} callers`))}
        </div>
      </section>
      <section className="card span-6">
        <h3><Ruler size={16} /> Largest functions</h3>
        <div className="ref-list">
          {!data && <div className="skeleton" style={{ height: 120 }} />}
          {largest.map((fn) => row(fn, bytes(fn.size)))}
        </div>
      </section>
    </>
  );
}

export function Overview() {
  const session = useSession();
  const { data: s, error, loading } = useEngine<ProgramSummary>(
    'program.summary',
    { programId: session.programId },
    [session.revision],
  );

  if (loading && !s)
    return (
      <div className="empty-state">
        <div>
          <Loader2 size={22} className="spin" />
          <p style={{ marginTop: 10 }}>Summarising the program…</p>
        </div>
      </div>
    );
  if (error || !s)
    return (
      <div className="empty-state">
        <div>
          <h4>Couldn't summarise this program</h4>
          <p>{error}</p>
        </div>
      </div>
    );

  const importCount = s.imports.reduce((n, l) => n + l.functions.length, 0);
  const insights = insightsFor(s);
  const byCategory = s.riskyApis.reduce<Record<string, string[]>>((acc, r) => {
    (acc[r.category] ??= []).push(r.name);
    return acc;
  }, {});

  return (
    <div className="triage">
      <section className="card span-12">
        <div className="identity">
          <div>
            <div className="title">{s.name}</div>
            <div className="chips" style={{ marginTop: 8 }}>
              <span className="chip">{s.format || 'Unknown format'}</span>
              <span className="chip"><Cpu size={12} /> {s.processor} · {s.addressSize}-bit · {s.endian}-endian</span>
              <span className="chip mono">{s.compiler}</span>
              <span className="chip mono">base {s.imageBase}</span>
            </div>
          </div>
          <div className="stats">
            <div className="stat"><span className="value">{s.functionCount.toLocaleString()}</span><span className="label">Functions</span></div>
            <div className="stat"><span className="value">{importCount.toLocaleString()}</span><span className="label">Imports</span></div>
            <div className="stat"><span className="value">{s.stringCount.toLocaleString()}</span><span className="label">Strings</span></div>
            <div className="stat"><span className="value">{s.riskyApis.length}</span><span className="label">Risky APIs</span></div>
          </div>
        </div>
      </section>

      <section className="card span-8">
        <h3><Info size={16} /> Insights</h3>
        <div className="insights">
          {insights.map((insight, i) => (
            <div className={`insight ${insight.level}`} key={insight.title} style={{ animationDelay: `${i * 40}ms` }}>
              <span className="insight-icon">
                {insight.level === 'attention' ? <AlertTriangle size={14} /> : insight.level === 'ok' ? <CircleCheck size={14} /> : <Info size={14} />}
              </span>
              <div>
                <strong>{insight.title}</strong>
                <p>{insight.detail}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="card span-4">
        <h3><AlertTriangle size={16} /> Risky API calls</h3>
        {s.riskyApis.length === 0 ? (
          <p className="muted">None of the commonly abused calls are imported.</p>
        ) : (
          <div className="scroll-box short">
            {Object.entries(byCategory).map(([category, names]) => (
              <div key={category} style={{ marginBottom: 12 }}>
                <div style={{ fontSize: 'var(--text-sm)', fontWeight: 600, marginBottom: 6 }}>{CATEGORY_LABEL[category] ?? category}</div>
                <div className="chips">{names.map((n) => <span key={n} className="chip mono">{n}</span>)}</div>
              </div>
            ))}
          </div>
        )}
      </section>

      <Hotspots programId={session.programId!} revision={session.revision} />

      <section className="card span-6">
        <h3><Package size={16} /> Imports by library</h3>
        {s.imports.length === 0 ? <p className="muted">No imported libraries.</p> : <Imports imports={s.imports} />}
      </section>

      <section className="card span-6">
        <h3><Boxes size={16} /> Sections</h3>
        <div className="scroll-box">
          {s.blocks.map((b) => (
            <div className="section-row" key={b.name + b.start} title={`Entropy ${b.entropy ?? 0} of 8`}>
              <span className="mono">{b.name}</span>
              <div className="entropy"><i style={{ width: `${((b.entropy ?? 0) / 8) * 100}%` }} /></div>
              <span className="perm">{b.permissions}</span>
              <span className="muted" style={{ textAlign: 'right' }}>{bytes(b.size)}</span>
            </div>
          ))}
        </div>
        <p className="muted" style={{ margin: '10px 0 0' }}>Bars show entropy; near full usually means packed or encrypted.</p>
      </section>

      <section className="card span-6">
        <h3><Quote size={16} /> Notable strings</h3>
        {s.notableStrings.length === 0 && <p className="muted">No web addresses, IPs, paths or registry keys.</p>}
        <div className="scroll-box">
          {s.notableStrings.map((str) => (
            <div className="string-row" key={str.address}>
              <span className="chip">{str.kind === 'url' && <Link2 size={11} />}{str.kind}</span>
              <code title={str.value}>{str.value}</code>
            </div>
          ))}
        </div>
      </section>

      <section className="card span-6">
        <h3><DoorOpen size={16} /> Entry points</h3>
        <div className="chips">
          {s.entryPoints.length === 0 && <span className="muted">No entry points recorded.</span>}
          {s.entryPoints.slice(0, 40).map((e) => (
            <button type="button" key={e.address} className="chip mono" style={{ cursor: 'pointer' }} onClick={() => session.goTo(e.address)}>
              {e.name} <span style={{ color: 'var(--text-faint)' }}>{e.address}</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}

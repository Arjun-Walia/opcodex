// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { AlertTriangle, Boxes, Cpu, DoorOpen, Link2, Loader2, Package, Quote } from 'lucide-react';
import type { ProgramSummary } from '../engine/types';
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

function entropyNote(e?: number): string {
  if (e === undefined) return 'Not in the file';
  if (e > 7.2) return 'Looks packed or encrypted';
  if (e > 6.2) return 'Dense: compressed data or code';
  return 'Ordinary code or data';
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
              <span className="chip accent">{s.format || 'Unknown format'}</span>
              <span className="chip">
                <Cpu size={12} /> {s.processor} · {s.addressSize}-bit · {s.endian}-endian
              </span>
              <span className="chip mono">{s.compiler}</span>
              <span className="chip mono">base {s.imageBase}</span>
            </div>
          </div>
          <div className="stats">
            <div className="stat">
              <span className="value">{s.functionCount.toLocaleString()}</span>
              <span className="label">Functions</span>
            </div>
            <div className="stat">
              <span className="value">{importCount.toLocaleString()}</span>
              <span className="label">Imports</span>
            </div>
            <div className="stat">
              <span className="value">{s.stringCount.toLocaleString()}</span>
              <span className="label">Strings</span>
            </div>
            <div className="stat">
              <span className="value">
                {s.riskyApis.length}
              </span>
              <span className="label">Risky APIs</span>
            </div>
          </div>
        </div>
      </section>

      <section className="card span-8">
        <h3>
          <Boxes size={16} /> Sections
        </h3>
        {s.blocks.map((b) => (
          <div className="section-row" key={b.name + b.start} title={entropyNote(b.entropy)}>
            <span className="mono">{b.name}</span>
            <div className="entropy" aria-label={`Entropy ${b.entropy ?? 0} of 8`}>
              <i style={{ width: `${((b.entropy ?? 0) / 8) * 100}%` }} />
            </div>
            <span className="perm">{b.permissions}</span>
            <span className="muted" style={{ textAlign: 'right' }}>
              {bytes(b.size)}
            </span>
          </div>
        ))}
        <p className="muted" style={{ margin: '10px 0 0' }}>
          The bar shows entropy: near the right end usually means packed or encrypted content.
        </p>
      </section>

      <section className="card span-4">
        <h3>
          <AlertTriangle size={16} /> What it can do
        </h3>
        {s.riskyApis.length === 0 ? (
          <p className="muted">No commonly abused API calls in the imports.</p>
        ) : (
          Object.entries(byCategory).map(([category, names]) => (
            <div key={category} style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 'var(--text-sm)', fontWeight: 600, marginBottom: 6 }}>
                {CATEGORY_LABEL[category] ?? category}
              </div>
              <div className="chips">
                {names.map((n) => (
                  <span key={n} className="chip mono">
                    {n}
                  </span>
                ))}
              </div>
            </div>
          ))
        )}
      </section>

      <section className="card span-6">
        <h3>
          <Package size={16} /> Imports by library
        </h3>
        {s.imports.length === 0 && <p className="muted">No imported libraries.</p>}
        {s.imports.slice(0, 12).map((lib) => (
          <div className="import-lib" key={lib.library}>
            <div className="lib">
              <span>{lib.library}</span>
              <span className="muted">{lib.functions.length}</span>
            </div>
            <div className="fns">
              {lib.functions.slice(0, 14).join('  ')}
              {lib.functions.length > 14 && ` … ${lib.functions.length - 14} more`}
            </div>
          </div>
        ))}
      </section>

      <section className="card span-6">
        <h3>
          <Quote size={16} /> Notable strings
        </h3>
        {s.notableStrings.length === 0 && <p className="muted">No URLs, addresses, paths or registry keys found.</p>}
        {s.notableStrings.slice(0, 16).map((str) => (
          <div className="string-row" key={str.address}>
            <span className="chip">
              {str.kind === 'url' && <Link2 size={11} />}
              {str.kind}
            </span>
            <code title={str.value}>{str.value}</code>
          </div>
        ))}
      </section>

      <section className="card span-12">
        <h3>
          <DoorOpen size={16} /> Entry points
        </h3>
        <div className="chips">
          {s.entryPoints.length === 0 && <span className="muted">No entry points recorded.</span>}
          {s.entryPoints.slice(0, 24).map((e) => (
            <button
              type="button"
              key={e.address}
              className="chip mono"
              style={{ cursor: 'pointer' }}
              onClick={() => session.goTo(e.address)}
            >
              {e.name} <span style={{ color: 'var(--text-faint)' }}>{e.address}</span>
            </button>
          ))}
        </div>
      </section>
    </div>
  );
}

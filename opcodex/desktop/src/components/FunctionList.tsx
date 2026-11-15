// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useMemo, useRef, useState } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';
import { Search } from 'lucide-react';
import { call } from '../engine/client';
import type { FunctionSummary, Page } from '../engine/types';
import { usePrefs } from '../state/prefs';
import { useSession } from '../state/session';

const MAX_LOADED = 50_000;

function isDefaultName(name: string): boolean {
  return /^(FUN|thunk_FUN|LAB|SUB)_[0-9a-f]+$/i.test(name);
}

export function FunctionList() {
  const session = useSession();
  const ROW = usePrefs().layout.density === 'compact' ? 24 : 30;
  const [functions, setFunctions] = useState<FunctionSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const scroller = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let live = true;
    setFunctions([]);
    setLoading(true);
    (async () => {
      let cursor: string | undefined;
      const all: FunctionSummary[] = [];
      do {
        const page: Page<FunctionSummary> = await call('program.functions.list', {
          programId: session.programId,
          limit: 1000,
          ...(cursor ? { cursor } : {}),
        });
        if (!live) return;
        all.push(...page.items);
        setFunctions([...all]);
        cursor = page.nextCursor;
      } while (cursor && all.length < MAX_LOADED);
      if (live) setLoading(false);
    })().catch(() => live && setLoading(false));
    return () => {
      live = false;
    };
  }, [session.programId, session.revision]);

  const shown = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    if (!needle) return functions;
    return functions.filter((f) => f.name.toLowerCase().includes(needle) || f.entry.includes(needle));
  }, [functions, filter]);

  const virtual = useVirtualizer({
    count: shown.length,
    getScrollElement: () => scroller.current,
    estimateSize: () => ROW,
    overscan: 16,
  });

  const named = functions.filter((f) => !isDefaultName(f.name)).length;

  return (
    <aside className="sidebar" aria-label="Functions">
      <div className="sidebar-head">
        <div className="sidebar-title">
          <span>Functions</span>
          <span className="chip">{functions.length.toLocaleString()}</span>
        </div>
        <div className="filter">
          <Search size={14} />
          <input
            className="input"
            placeholder="Filter by name or address"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
            aria-label="Filter functions"
          />
        </div>
      </div>
      <div className="list" ref={scroller} role="listbox" aria-label="Functions">
        <div style={{ height: virtual.getTotalSize(), position: 'relative' }}>
          {virtual.getVirtualItems().map((row) => {
            const fn = shown[row.index];
            return (
              <button
                type="button"
                key={fn.entry}
                role="option"
                aria-selected={session.location === fn.entry}
                className="list-row"
                style={{ top: row.start, height: ROW }}
                onClick={() => session.goTo(fn.entry)}
                title={`${fn.name} · ${fn.size} bytes · ${fn.callers} callers`}
              >
                <span className={`fn-name ${isDefaultName(fn.name) ? 'default' : ''}`}>{fn.name}</span>
                <span className="fn-addr">{fn.entry}</span>
              </button>
            );
          })}
        </div>
        {loading && functions.length === 0 && (
          <div style={{ padding: 12, display: 'grid', gap: 8 }}>
            {Array.from({ length: 10 }, (_, i) => (
              <div key={i} className="skeleton" style={{ height: 14, width: `${60 + ((i * 17) % 35)}%` }} />
            ))}
          </div>
        )}
      </div>
      <div className="list-footer">
        {loading ? 'Loading functions…' : `${named.toLocaleString()} named · ${shown.length.toLocaleString()} shown`}
      </div>
    </aside>
  );
}

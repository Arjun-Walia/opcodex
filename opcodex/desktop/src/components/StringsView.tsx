// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useMemo, useRef, useState } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';
import { CornerDownRight, Loader2, Search } from 'lucide-react';
import { call } from '../engine/client';
import type { FunctionDetail, Page, Xref } from '../engine/types';
import { useSession } from '../state/session';

interface StringEntry {
  address: string;
  value: string;
  encoding: string;
}

const MAX_LOADED = 50_000;
const ROW = 30;

export function StringsView() {
  const session = useSession();
  const [strings, setStrings] = useState<StringEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('');
  const [note, setNote] = useState<string>();
  const [resolving, setResolving] = useState<string | null>(null);
  const scroller = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let live = true;
    setStrings([]);
    setLoading(true);
    (async () => {
      let cursor: string | undefined;
      const all: StringEntry[] = [];
      do {
        const page: Page<StringEntry> = await call('program.strings.list', {
          programId: session.programId,
          limit: 1000,
          ...(cursor ? { cursor } : {}),
        });
        if (!live) return;
        all.push(...page.items);
        setStrings([...all]);
        cursor = page.nextCursor;
      } while (cursor && all.length < MAX_LOADED);
      if (live) setLoading(false);
    })().catch(() => live && setLoading(false));
    return () => {
      live = false;
    };
  }, [session.programId]);

  const shown = useMemo(() => {
    const needle = filter.trim().toLowerCase();
    return needle ? strings.filter((s) => s.value.toLowerCase().includes(needle)) : strings;
  }, [strings, filter]);

  const virtual = useVirtualizer({ count: shown.length, getScrollElement: () => scroller.current, estimateSize: () => ROW, overscan: 20 });

  async function openUser(entry: StringEntry) {
    setResolving(entry.address);
    setNote(undefined);
    try {
      const refs = await call<Page<Xref>>('xrefs.to', { programId: session.programId, address: entry.address, limit: 20 });
      for (const ref of refs.items) {
        try {
          const fn = await call<FunctionDetail>('function.get', { programId: session.programId, address: ref.from });
          session.goTo(fn.entry);
          session.setTab('decompile');
          return;
        } catch {
          // Not inside a function; try the next reference.
        }
      }
      setNote(`Nothing in the program's code refers to the string at ${entry.address}.`);
    } finally {
      setResolving(null);
    }
  }

  return (
    <div className="list-view">
      <div className="list-view-head">
        <div className="filter" style={{ flex: 1, maxWidth: 420 }}>
          <Search size={14} />
          <input className="input" placeholder="Search strings" value={filter} onChange={(e) => setFilter(e.target.value)} />
        </div>
        <span className="muted">
          {loading ? <Loader2 size={13} className="spin" /> : null} {shown.length.toLocaleString()} of {strings.length.toLocaleString()}
        </span>
      </div>
      {note && <p className="notice" style={{ padding: '0 16px' }}>{note}</p>}
      <div className="list-view-body" ref={scroller}>
        <div style={{ height: virtual.getTotalSize(), position: 'relative' }}>
          {virtual.getVirtualItems().map((row) => {
            const entry = shown[row.index];
            return (
              <button
                type="button"
                key={entry.address}
                className="string-line"
                style={{ top: row.start, height: ROW }}
                onClick={() => openUser(entry)}
                title="Open the code that uses this string"
              >
                <span className="fn-addr">{entry.address}</span>
                <span className="string-value">{entry.value}</span>
                <span className="muted">{entry.encoding}</span>
                {resolving === entry.address ? <Loader2 size={13} className="spin" /> : <CornerDownRight size={13} className="go" />}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

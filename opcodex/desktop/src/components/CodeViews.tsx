// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useMemo, useRef, useState } from 'react';
import { useVirtualizer } from '@tanstack/react-virtual';
import { Binary, Braces, Loader2 } from 'lucide-react';
import { call } from '../engine/client';
import type { Decompiled, Instruction } from '../engine/types';
import { classify, toLines } from '../lib/code';
import { formatHexRows, parseHex } from '../lib/hex';
import { useEngine, useSession } from '../state/session';

function Empty({ title, text, icon }: { title: string; text: string; icon: JSX.Element }) {
  return (
    <div className="empty-state">
      <div>
        {icon}
        <h4>{title}</h4>
        <p>{text}</p>
      </div>
    </div>
  );
}

function Loading({ text }: { text: string }) {
  return (
    <div className="empty-state">
      <div>
        <Loader2 size={22} className="spin" />
        <p style={{ marginTop: 10 }}>{text}</p>
      </div>
    </div>
  );
}

export function DecompileView() {
  const session = useSession();
  const [highlight, setHighlight] = useState<string | null>(null);
  const { data, error, loading } = useEngine<Decompiled>(
    session.location ? 'code.decompile' : null,
    { programId: session.programId, address: session.location },
    [session.revision],
  );
  const lines = useMemo(() => (data ? toLines(data.tokens) : []), [data]);

  if (!session.location)
    return (
      <Empty
        icon={<Braces size={26} />}
        title="Pick a function"
        text="Choose a function on the left to read it as C. Click any call to follow it."
      />
    );
  if (loading && !data) return <Loading text="Decompiling…" />;
  if (error) return <Empty icon={<Braces size={26} />} title="Couldn't decompile" text={error} />;

  return (
    <pre className="code" aria-label="Decompiled C">
      {lines.map((tokens, i) => (
        <div className="code-line" key={i}>
          <span className="line-no">{i + 1}</span>
          <span className="line-text">
            {tokens.map((t, j) => {
              const kind = classify(t);
              const isCall = kind === 'funcName' && t.address && t.address !== data?.entry;
              const name = t.text.trim();
              const hl = highlight && name === highlight ? ' tk-hl' : '';
              return (
                <span
                  key={j}
                  className={`tk-${kind}${isCall ? ' link' : ''}${hl}`}
                  onClick={() => {
                    if (isCall && t.address) session.goTo(t.address);
                    else if (kind === 'variable' || kind === 'funcName') setHighlight(name);
                  }}
                  title={isCall ? `Go to ${name}` : undefined}
                >
                  {t.text}
                </span>
              );
            })}
          </span>
        </div>
      ))}
    </pre>
  );
}

export function DisassemblyView() {
  const session = useSession();
  const { data, error, loading } = useEngine<{ items: Instruction[] }>(
    session.location ? 'code.disassemble' : null,
    { programId: session.programId, address: session.location, count: 600 },
    [session.revision],
  );
  if (!session.location)
    return (
      <Empty icon={<Binary size={26} />} title="Pick a function" text="Its instructions appear here." />
    );
  if (loading && !data) return <Loading text="Reading instructions…" />;
  if (error) return <Empty icon={<Binary size={26} />} title="Couldn't disassemble" text={error} />;
  return (
    <div style={{ padding: '8px 0', userSelect: 'text' }}>
      {data?.items.map((ins) => (
        <div key={ins.address} className={`asm-row ${ins.address === session.location ? 'is-entry' : ''}`}>
          <span className="asm-addr">{ins.address}</span>
          <span className="asm-bytes">{ins.bytes.replace(/(..)/g, '$1 ').trim()}</span>
          <span className="asm-mn">{ins.mnemonic.toLowerCase()}</span>
          <span className="asm-ops">
            {ins.operands}
            {ins.comment && <span className="asm-comment"> ; {ins.comment}</span>}
          </span>
        </div>
      ))}
    </div>
  );
}

const HEX_CHUNK = 4096;
const HEX_ROW = 22;

export function HexView({ start }: { start?: string }) {
  const session = useSession();
  const scroller = useRef<HTMLDivElement>(null);
  const address = session.location ?? start;
  const [bytes, setBytes] = useState<{ data: Uint8Array; base: bigint; error?: string }>();

  useEffect(() => {
    if (!address) return;
    let live = true;
    const base = BigInt('0x' + address.replace(/^.*:/, ''));
    setBytes({ data: new Uint8Array(), base });
    call<{ hex: string }>('memory.read', { programId: session.programId, address, length: HEX_CHUNK }).then(
      (r) => live && setBytes({ data: parseHex(r.hex), base }),
      (e: Error) => live && setBytes({ data: new Uint8Array(), base, error: e.message }),
    );
    return () => {
      live = false;
    };
  }, [address, session.programId, session.revision]);
  const rows = useMemo(() => (bytes ? formatHexRows(bytes.data, bytes.base) : []), [bytes]);
  const virtual = useVirtualizer({
    count: rows.length,
    getScrollElement: () => scroller.current,
    estimateSize: () => HEX_ROW,
    overscan: 20,
  });

  if (!address)
    return <Empty icon={<Binary size={26} />} title="Nothing selected" text="Pick a function to see its bytes." />;
  if (bytes?.error) return <Empty icon={<Binary size={26} />} title="No bytes here" text={bytes.error} />;

  return (
    <div ref={scroller} style={{ height: '100%', overflow: 'auto' }}>
      <div className="hex-row hex-head">
        <span>Address</span>
        <span>00 01 02 03 04 05 06 07  08 09 0a 0b 0c 0d 0e 0f</span>
        <span>Text</span>
      </div>
      <div style={{ height: virtual.getTotalSize(), position: 'relative', userSelect: 'text' }}>
        {virtual.getVirtualItems().map((v) => {
          const row = rows[v.index];
          return (
            <div key={row.offset} className="hex-row" style={{ position: 'absolute', top: v.start, left: 0, right: 0 }}>
              <span className="hex-off">{row.offset}</span>
              <span className="hex-bytes">
                {row.hex.map((h, i) => (
                  <span key={i} className={h === '00' ? 'zero' : undefined}>
                    {h}
                    {i === 7 ? '  ' : ' '}
                  </span>
                ))}
              </span>
              <span className="hex-ascii">{row.ascii}</span>
            </div>
          );
        })}
      </div>
    </div>
  );
}

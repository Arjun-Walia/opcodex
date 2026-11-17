// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Function graph (basic blocks and branches) and call graph, with pan and zoom.
import { useCallback, useEffect, useMemo, useRef, useState, type PointerEvent, type WheelEvent } from 'react';
import { GitBranch, Loader2, Maximize2, Minus, Network, Plus } from 'lucide-react';
import type { CallGraph, FunctionGraph } from '../engine/types';
import { BLOCK_PAD_X, BLOCK_PAD_Y, CHAR_WIDTH, LINE_HEIGHT, layoutBlocks, layoutCalls, pathOf, shownLines, type Layout } from '../lib/graph';
import { useEngine, useSession } from '../state/session';

type Mode = 'blocks' | 'calls';
type View = { x: number; y: number; k: number };
const MIN_ZOOM = 0.1;
const MAX_ZOOM = 2.5;

function useViewport(layout: Layout | null) {
  const host = useRef<HTMLDivElement>(null);
  const [view, setView] = useState<View>({ x: 0, y: 0, k: 1 });
  const drag = useRef<{ x: number; y: number; vx: number; vy: number } | null>(null);

  const fit = useCallback(() => {
    const el = host.current;
    if (!el || !layout) return;
    const k = Math.min(1, el.clientWidth / Math.max(layout.width, 1), el.clientHeight / Math.max(layout.height, 1));
    const kk = Math.max(MIN_ZOOM, k * 0.95);
    setView({ k: kk, x: (el.clientWidth - layout.width * kk) / 2, y: Math.max(8, (el.clientHeight - layout.height * kk) / 2) });
  }, [layout]);
  useEffect(fit, [fit]);

  const zoomAt = (factor: number, cx: number, cy: number) =>
    setView((v) => {
      const k = Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, v.k * factor));
      return { k, x: cx - ((cx - v.x) * k) / v.k, y: cy - ((cy - v.y) * k) / v.k };
    });
  const onWheel = (e: WheelEvent) => {
    const rect = host.current!.getBoundingClientRect();
    zoomAt(e.deltaY < 0 ? 1.12 : 1 / 1.12, e.clientX - rect.left, e.clientY - rect.top);
  };
  const onPointerDown = (e: PointerEvent) => {
    if ((e.target as Element).closest('[data-click]')) return;
    drag.current = { x: e.clientX, y: e.clientY, vx: view.x, vy: view.y };
    (e.currentTarget as Element).setPointerCapture(e.pointerId);
  };
  const onPointerMove = (e: PointerEvent) => {
    const d = drag.current;
    if (d) setView((v) => ({ ...v, x: d.vx + e.clientX - d.x, y: d.vy + e.clientY - d.y }));
  };
  const onPointerUp = () => {
    drag.current = null;
  };
  const center = () => {
    const el = host.current!;
    return [el.clientWidth / 2, el.clientHeight / 2] as const;
  };
  return {
    host, view, fit,
    zoomIn: () => zoomAt(1.25, ...center()),
    zoomOut: () => zoomAt(1 / 1.25, ...center()),
    handlers: { onWheel, onPointerDown, onPointerMove, onPointerUp, onPointerCancel: onPointerUp },
  };
}

function Markers() {
  return (
    <defs>
      {(['true', 'false', 'plain'] as const).map((k) => (
        <marker key={k} id={`arrow-${k}`} viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7" markerHeight="7" orient="auto-start-reverse">
          <path d="M0,0 L10,5 L0,10 z" className={`graph-arrow ${k}`} />
        </marker>
      ))}
    </defs>
  );
}

function edgeClass(kind: string) {
  return kind === 'true' ? 'true' : kind === 'false' ? 'false' : 'plain';
}

export function GraphView() {
  const session = useSession();
  const [mode, setMode] = useState<Mode>('blocks');
  const [selected, setSelected] = useState<string | null>(null);
  const blocks = useEngine<FunctionGraph>(
    session.location && mode === 'blocks' ? 'function.graph' : null,
    { programId: session.programId, address: session.location },
    [session.revision],
  );
  const calls = useEngine<CallGraph>(
    session.location && mode === 'calls' ? 'function.callgraph' : null,
    { programId: session.programId, address: session.location, depth: 2, limit: 120 },
    [session.revision],
  );
  const layout = useMemo(() => {
    if (mode === 'blocks') return blocks.data ? layoutBlocks(blocks.data) : null;
    return calls.data ? layoutCalls(calls.data) : null;
  }, [mode, blocks.data, calls.data]);
  const vp = useViewport(layout);
  const current = mode === 'blocks' ? blocks : calls;

  if (!session.location)
    return (
      <div className="empty-state">
        <div>
          <GitBranch size={26} />
          <h4>Pick a function</h4>
          <p>Its blocks and branches appear here as a graph.</p>
        </div>
      </div>
    );

  const blockOf = new Map((blocks.data?.blocks ?? []).map((b) => [b.start, b]));
  const lineLimit = blocks.data ? shownLines(blocks.data) : Number.MAX_SAFE_INTEGER;
  const callOf = new Map((calls.data?.nodes ?? []).map((n) => [n.entry, n]));

  return (
    <div className="graph-view">
      <div className="graph-toolbar">
        <div className="segmented" role="radiogroup" aria-label="Graph">
          <button type="button" role="radio" aria-checked={mode === 'blocks'} onClick={() => setMode('blocks')}>
            <GitBranch size={13} /> Blocks
          </button>
          <button type="button" role="radio" aria-checked={mode === 'calls'} onClick={() => setMode('calls')}>
            <Network size={13} /> Calls
          </button>
        </div>
        {mode === 'blocks' && (
          <div className="graph-legend" aria-hidden>
            <span className="swatch true" /> taken <span className="swatch false" /> not taken
            <span className="swatch plain" /> jump or fall-through
          </div>
        )}
        <span className="toolbar-spacer" />
        <button type="button" className="icon-button" aria-label="Zoom out" onClick={vp.zoomOut}>
          <Minus size={14} />
        </button>
        <span className="graph-zoom mono">{Math.round(vp.view.k * 100)}%</span>
        <button type="button" className="icon-button" aria-label="Zoom in" onClick={vp.zoomIn}>
          <Plus size={14} />
        </button>
        <button type="button" className="icon-button" aria-label="Fit to view" onClick={vp.fit}>
          <Maximize2 size={14} />
        </button>
      </div>
      <div className="graph-canvas" ref={vp.host} {...vp.handlers}>
        {current.loading && !layout && (
          <div className="empty-state">
            <div>
              <Loader2 size={22} className="spin" />
              <p style={{ marginTop: 10 }}>Laying out the graph…</p>
            </div>
          </div>
        )}
        {current.error && (
          <div className="empty-state">
            <div>
              <GitBranch size={26} />
              <h4>No graph here</h4>
              <p>{current.error}</p>
            </div>
          </div>
        )}
        {layout && (
          <svg width="100%" height="100%" role="img" aria-label={mode === 'blocks' ? 'Function graph' : 'Call graph'}>
            <Markers />
            <g transform={`translate(${vp.view.x},${vp.view.y}) scale(${vp.view.k})`}>
              {layout.edges.map((e, i) => (
                <path key={i} d={pathOf(e.points)} className={`graph-edge ${edgeClass(e.kind)}`} markerEnd={`url(#arrow-${edgeClass(e.kind)})`} />
              ))}
              {layout.nodes.map((n) => {
                const x = n.x - n.width / 2;
                const y = n.y - n.height / 2;
                if (mode === 'calls') {
                  const node = callOf.get(n.id);
                  const root = n.id === calls.data?.root;
                  return (
                    <g key={n.id} data-click transform={`translate(${x},${y})`} className={`graph-node call${root ? ' root' : ''}${node?.external ? ' external' : ''}`}
                      onClick={() => !node?.external && session.goTo(n.id)}>
                      <rect width={n.width} height={n.height} rx={8} />
                      <text x={BLOCK_PAD_X} y={BLOCK_PAD_Y + LINE_HEIGHT * 0.8} className="graph-name">{node?.name}</text>
                      <text x={BLOCK_PAD_X} y={BLOCK_PAD_Y + LINE_HEIGHT * 1.8} className="graph-addr">{node?.external ? 'imported' : n.id}</text>
                    </g>
                  );
                }
                const block = blockOf.get(n.id);
                const entry = n.id === blocks.data?.entry;
                return (
                  <g key={n.id} transform={`translate(${x},${y})`} className={`graph-node${entry ? ' root' : ''}`}>
                    <rect width={n.width} height={n.height} rx={8} />
                    {block && block.instructions.length > lineLimit && (
                      <text x={BLOCK_PAD_X} y={BLOCK_PAD_Y + LINE_HEIGHT * lineLimit + LINE_HEIGHT * 0.72} className="graph-addr">
                        … {block.instructions.length - lineLimit} more
                      </text>
                    )}
                    {block?.instructions.slice(0, lineLimit).map((ins, i) => {
                      const [mn, ...rest] = ins.text.split(' ');
                      const ty = BLOCK_PAD_Y + LINE_HEIGHT * i;
                      return (
                        <g key={ins.address} data-click className={`graph-line${selected === ins.address ? ' selected' : ''}`}
                          onClick={() => setSelected(ins.address)}
                          onDoubleClick={() => { session.goTo(ins.address); session.setTab('disassembly'); }}>
                          <rect x={4} y={ty} width={n.width - 8} height={LINE_HEIGHT} rx={4} className="graph-line-bg" />
                          <text x={BLOCK_PAD_X} y={ty + LINE_HEIGHT * 0.72} className="graph-addr">{ins.address}</text>
                          <text x={BLOCK_PAD_X + 10 * CHAR_WIDTH} y={ty + LINE_HEIGHT * 0.72}>
                            <tspan className="graph-mn">{mn.toLowerCase()}</tspan>
                            <tspan className="graph-ops"> {rest.join(' ')}</tspan>
                            {ins.comment && <tspan className="graph-comment"> ; {ins.comment}</tspan>}
                          </text>
                        </g>
                      );
                    })}
                  </g>
                );
              })}
            </g>
          </svg>
        )}
        {(mode === 'blocks' ? blocks.data?.truncated : calls.data?.truncated) && (
          <div className="graph-note">This graph is large; only part of it is shown.</div>
        )}
        {mode === 'blocks' && !blocks.data?.truncated && lineLimit !== Number.MAX_SAFE_INTEGER && (
          <div className="graph-note">A large function: each block shows its first lines. Double-click a line to read it all in Assembly.</div>
        )}
      </div>
    </div>
  );
}

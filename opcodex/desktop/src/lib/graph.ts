// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Lays out control-flow and call graphs with dagre. Pure: no DOM, so it's testable.
import dagre from '@dagrejs/dagre';
import type { CallGraph, FunctionGraph, EdgeKind } from '../engine/types';

export const CHAR_WIDTH = 7.4;
export const LINE_HEIGHT = 18;
export const BLOCK_PAD_X = 12;
export const BLOCK_PAD_Y = 10;
const ADDRESS_CHARS = 10; // address column, including the gap after it

export type LaidNode = { id: string; x: number; y: number; width: number; height: number };
export type LaidEdge = { from: string; to: string; kind: EdgeKind | 'call'; points: { x: number; y: number }[] };
export type Layout = { nodes: LaidNode[]; edges: LaidEdge[]; width: number; height: number };

function run(
  nodes: { id: string; width: number; height: number; rank?: number }[],
  edges: { from: string; to: string; kind: EdgeKind | 'call' }[],
  spacing: { nodesep: number; ranksep: number },
): Layout {
  const g = new dagre.graphlib.Graph({ multigraph: true });
  g.setGraph({ rankdir: 'TB', nodesep: spacing.nodesep, ranksep: spacing.ranksep, marginx: 24, marginy: 24 });
  g.setDefaultEdgeLabel(() => ({}));
  for (const n of nodes) g.setNode(n.id, { width: n.width, height: n.height });
  edges.forEach((e, i) => g.setEdge(e.from, e.to, {}, String(i)));
  dagre.layout(g);
  const laid = nodes.map((n) => {
    const p = g.node(n.id);
    return { id: n.id, x: p.x, y: p.y, width: n.width, height: n.height };
  });
  const laidEdges = edges.map((e, i) => ({
    ...e,
    points: (g.edge({ v: e.from, w: e.to, name: String(i) })?.points ?? []).map((p: { x: number; y: number }) => ({ x: p.x, y: p.y })),
  }));
  const graph = g.graph();
  return { nodes: laid, edges: laidEdges, width: graph.width ?? 0, height: graph.height ?? 0 };
}

export function blockSize(lines: string[]): { width: number; height: number } {
  const longest = lines.reduce((m, l) => Math.max(m, l.length), 0);
  return {
    width: Math.ceil((longest + ADDRESS_CHARS) * CHAR_WIDTH + BLOCK_PAD_X * 2),
    height: Math.max(1, lines.length) * LINE_HEIGHT + BLOCK_PAD_Y * 2,
  };
}

/** Above this many blocks, each block shows only its first lines so the window stays responsive. */
export const COMPACT_BLOCKS = 250;
export const COMPACT_LINES = 4;

export function shownLines(graph: FunctionGraph): number {
  return graph.blocks.length > COMPACT_BLOCKS ? COMPACT_LINES : Number.MAX_SAFE_INTEGER;
}

export function layoutBlocks(graph: FunctionGraph): Layout {
  const limit = shownLines(graph);
  const nodes = graph.blocks.map((b) => ({
    id: b.start,
    ...blockSize([
      ...b.instructions.slice(0, limit).map((i) => (i.comment ? `${i.text} ; ${i.comment}` : i.text)),
      ...(b.instructions.length > limit ? [`… ${b.instructions.length - limit} more`] : []),
    ]),
  }));
  const known = new Set(nodes.map((n) => n.id));
  const edges = graph.edges.filter((e) => known.has(e.from) && known.has(e.to));
  return run(nodes, edges, { nodesep: 36, ranksep: 48 });
}

export function layoutCalls(graph: CallGraph): Layout {
  const nodes = graph.nodes.map((n) => ({
    id: n.entry,
    width: Math.ceil(Math.max(n.name.length, 12) * CHAR_WIDTH + BLOCK_PAD_X * 2),
    height: LINE_HEIGHT * 2 + BLOCK_PAD_Y * 2,
  }));
  const known = new Set(nodes.map((n) => n.id));
  const edges = graph.edges
    .filter((e) => known.has(e.from) && known.has(e.to))
    .map((e) => ({ ...e, kind: 'call' as const }));
  return run(nodes, edges, { nodesep: 28, ranksep: 56 });
}

/** An SVG path through dagre's edge points. */
export function pathOf(points: { x: number; y: number }[]): string {
  if (points.length === 0) return '';
  return points.map((p, i) => `${i === 0 ? 'M' : 'L'}${p.x.toFixed(1)},${p.y.toFixed(1)}`).join(' ');
}

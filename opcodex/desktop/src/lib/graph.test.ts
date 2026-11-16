import { describe, expect, it } from 'vitest';
import { layoutBlocks, layoutCalls } from './graph';

const blocks = {
  entry: '00001000',
  name: 'main',
  truncated: false,
  blocks: [
    { start: '00001000', end: '00001008', instructions: [{ address: '00001000', text: 'PUSH RBP' }, { address: '00001007', text: 'JZ 0x00001010' }] },
    { start: '00001009', end: '0000100f', instructions: [{ address: '00001009', text: 'MOV EAX,0x1' }] },
    { start: '00001010', end: '00001014', instructions: [{ address: '00001010', text: 'MOV EAX,0x2' }] },
    { start: '00001015', end: '00001016', instructions: [{ address: '00001015', text: 'RET' }] },
  ],
  edges: [
    { from: '00001000', to: '00001010', kind: 'true' as const },
    { from: '00001000', to: '00001009', kind: 'false' as const },
    { from: '00001009', to: '00001015', kind: 'jump' as const },
    { from: '00001010', to: '00001015', kind: 'fall' as const },
  ],
};

describe('layoutBlocks', () => {
  it('places the entry block above the blocks it branches to', () => {
    const out = layoutBlocks(blocks);
    const entry = out.nodes.find((n) => n.id === '00001000')!;
    const ret = out.nodes.find((n) => n.id === '00001015')!;
    expect(entry.y).toBeLessThan(ret.y);
    expect(out.nodes).toHaveLength(4);
  });

  it('sizes blocks to their longest line and keeps every edge with its kind', () => {
    const out = layoutBlocks(blocks);
    const entry = out.nodes.find((n) => n.id === '00001000')!;
    const small = out.nodes.find((n) => n.id === '00001015')!;
    expect(entry.width).toBeGreaterThan(small.width);
    expect(entry.height).toBeGreaterThan(small.height);
    expect(out.edges.map((e) => e.kind).sort()).toEqual(['fall', 'false', 'jump', 'true']);
    for (const e of out.edges) expect(e.points.length).toBeGreaterThanOrEqual(2);
  });

  it('reports the drawing size', () => {
    const out = layoutBlocks(blocks);
    expect(out.width).toBeGreaterThan(0);
    expect(out.height).toBeGreaterThan(0);
  });
});

describe('layoutCalls', () => {
  it('puts callers above the function and callees below', () => {
    const out = layoutCalls({
      root: 'b',
      truncated: false,
      nodes: [
        { entry: 'b', name: 'middle', level: 0, external: false },
        { entry: 'a', name: 'caller', level: -1, external: false },
        { entry: 'c', name: 'callee', level: 1, external: true },
      ],
      edges: [
        { from: 'a', to: 'b' },
        { from: 'b', to: 'c' },
      ],
    });
    const y = (id: string) => out.nodes.find((n) => n.id === id)!.y;
    expect(y('a')).toBeLessThan(y('b'));
    expect(y('b')).toBeLessThan(y('c'));
  });
});

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { describe, expect, it } from 'vitest';
import { classify, toLines } from './code';
import { formatHexRows, parseHex } from './hex';
import { addRecent, splitProjectPath } from './recent';

describe('decompiler tokens', () => {
  it('splits on break tokens and keeps indentation', () => {
    const lines = toLines([
      { text: 'int', kind: 'type' },
      { text: ' ', kind: 'syntax' },
      { text: 'main', kind: 'funcName', address: '00401000' },
      { text: '(void)', kind: 'syntax' },
      { text: '\n  ', kind: 'break' },
      { text: 'return', kind: 'syntax' },
      { text: ' ', kind: 'syntax' },
      { text: '0x2a', kind: 'variable' },
    ]);
    expect(lines).toHaveLength(2);
    expect(lines[0].map((t) => t.text).join('')).toBe('int main(void)');
    expect(lines[1][0]).toEqual({ text: '  ', kind: 'syntax' });
    expect(lines[1][1].text).toBe('return');
  });

  it('drops blank lines at the start and end', () => {
    const lines = toLines([
      { text: '\n', kind: 'break' },
      { text: '\n  ', kind: 'break' },
      { text: 'int', kind: 'type' },
      { text: '\n', kind: 'break' },
      { text: '\n    ', kind: 'break' },
    ]);
    expect(lines).toHaveLength(1);
    expect(lines[0].map((t) => t.text).join('')).toBe('  int');
  });

  it('colours keywords, numbers and strings by their text', () => {
    expect(classify({ text: 'return', kind: 'syntax' })).toBe('keyword');
    expect(classify({ text: 'while', kind: 'op' })).toBe('keyword');
    expect(classify({ text: '0x2a', kind: 'variable' })).toBe('number');
    expect(classify({ text: '42', kind: 'variable' })).toBe('number');
    expect(classify({ text: '"hello"', kind: 'variable' })).toBe('string');
    expect(classify({ text: 'local_8', kind: 'variable' })).toBe('variable');
    expect(classify({ text: 'printf', kind: 'funcName' })).toBe('funcName');
    expect(classify({ text: '/* WARNING */', kind: 'comment' })).toBe('comment');
  });
});

describe('hex view', () => {
  it('parses hex and formats 16-byte rows with ascii', () => {
    const bytes = parseHex('48656c6c6f00ff');
    expect(Array.from(bytes)).toEqual([0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x00, 0xff]);
    const rows = formatHexRows(bytes, 0x1000n);
    expect(rows).toHaveLength(1);
    expect(rows[0].offset).toBe('00001000');
    expect(rows[0].hex.slice(0, 5)).toEqual(['48', '65', '6c', '6c', '6f']);
    expect(rows[0].ascii).toBe('Hello..');
  });
});

describe('recent projects', () => {
  it('keeps the newest first, without duplicates, at most 12', () => {
    let list = addRecent([], { dir: 'C:/p', name: 'a', openedAt: 1 });
    list = addRecent(list, { dir: 'C:/p', name: 'b', openedAt: 2 });
    list = addRecent(list, { dir: 'C:/p', name: 'a', openedAt: 3 });
    expect(list.map((p) => p.name)).toEqual(['a', 'b']);
    for (let i = 0; i < 20; i++) list = addRecent(list, { dir: 'C:/x', name: `n${i}`, openedAt: 10 + i });
    expect(list).toHaveLength(12);
  });

  it('splits a .gpr path into folder and name', () => {
    expect(splitProjectPath('C:\\work\\demo.gpr')).toEqual({ dir: 'C:\\work', name: 'demo' });
    expect(splitProjectPath('/home/me/proj/demo')).toEqual({ dir: '/home/me/proj', name: 'demo' });
  });
});

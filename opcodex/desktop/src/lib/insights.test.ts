// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { describe, expect, it } from 'vitest';
import type { ProgramSummary } from '../engine/types';
import { insightsFor } from './insights';

function summary(over: Partial<ProgramSummary> = {}): ProgramSummary {
  return {
    name: 'x.exe', format: 'Portable Executable (PE)', language: 'x86:LE:64:default', processor: 'x86',
    compiler: 'windows', endian: 'little', addressSize: 64, imageBase: '140000000', functionCount: 200,
    stringCount: 300,
    blocks: [{ name: '.text', start: '1000', end: '2000', size: 4096, permissions: 'r-x', initialized: true, entropy: 6.1 }],
    entryPoints: [{ address: '1000', name: 'entry' }],
    imports: [{ library: 'KERNEL32.DLL', functions: ['GetTickCount'] }],
    riskyApis: [], notableStrings: [], ...over,
  };
}

describe('insights', () => {
  it('flags packed code and capabilities in plain words, most important first', () => {
    const list = insightsFor(summary({
      blocks: [{ name: 'UPX1', start: '1000', end: '9000', size: 32768, permissions: 'rwx', initialized: true, entropy: 7.8 }],
      riskyApis: [
        { name: 'VirtualAllocEx', library: 'KERNEL32.DLL', category: 'process-injection' },
        { name: 'InternetOpenA', library: 'WININET.DLL', category: 'network' },
      ],
      notableStrings: [{ address: '5000', value: 'http://example.test/a', kind: 'url' }],
    }));
    expect(list[0].level).toBe('attention');
    expect(list.map((i) => i.title)).toEqual(expect.arrayContaining([
      'Code may be packed or encrypted',
      'Writable and executable memory',
      'Can write into other processes',
      'Talks over the network',
      '1 web address in its strings',
    ]));
  });

  it('notices a program that imports nothing', () => {
    const list = insightsFor(summary({ imports: [] }));
    expect(list.map((i) => i.title)).toContain('No imported libraries');
  });

  it('says so when nothing stands out', () => {
    const list = insightsFor(summary());
    expect(list).toHaveLength(1);
    expect(list[0].level).toBe('ok');
  });
});

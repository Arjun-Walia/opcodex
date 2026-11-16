import { describe, expect, it } from 'vitest';
import { inlines, parse } from './markdown';

describe('markdown for model replies', () => {
  it('reads bold and inline code', () => {
    expect(inlines('calls **18 functions** like `FUN_1400`.')).toEqual([
      { type: 'text', text: 'calls ' },
      { type: 'bold', text: '18 functions' },
      { type: 'text', text: ' like ' },
      { type: 'code', text: 'FUN_1400' },
      { type: 'text', text: '.' },
    ]);
  });

  it('builds headings, lists, paragraphs and code blocks', () => {
    const blocks = parse('## Summary\nIt parses headers.\n\n- reads the file\n- checks **magic**\n\n1. first\n2. second\n\n```c\nint x;\n```\nDone.');
    expect(blocks.map((b) => b.type)).toEqual(['heading', 'para', 'list', 'list', 'code', 'para']);
    expect(blocks[2]).toMatchObject({ type: 'list', ordered: false });
    expect(blocks[3]).toMatchObject({ type: 'list', ordered: true });
    expect(blocks[4]).toEqual({ type: 'code', text: 'int x;' });
  });

  it('keeps markup-looking text as text (no HTML)', () => {
    const blocks = parse('<img src=x onerror=alert(1)>');
    expect(blocks).toEqual([{ type: 'para', lines: [[{ type: 'text', text: '<img src=x onerror=alert(1)>' }]] }]);
  });
});

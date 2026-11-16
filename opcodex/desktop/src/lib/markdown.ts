// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// A small Markdown subset for model replies: code blocks, headings, lists, paragraphs, inline code
// and bold. Produces data only; the chat renders it as React elements, never as HTML.

export type Inline = { type: 'text' | 'code' | 'bold'; text: string };
export type Block =
  | { type: 'code'; text: string }
  | { type: 'heading'; inlines: Inline[] }
  | { type: 'list'; ordered: boolean; items: Inline[][] }
  | { type: 'para'; lines: Inline[][] };

export function inlines(text: string): Inline[] {
  const out: Inline[] = [];
  const pattern = /(`[^`\n]+`|\*\*[^*\n]+\*\*|__[^_\n]+__)/g;
  let last = 0;
  for (const m of text.matchAll(pattern)) {
    if (m.index! > last) out.push({ type: 'text', text: text.slice(last, m.index) });
    const token = m[0];
    out.push(token.startsWith('`') ? { type: 'code', text: token.slice(1, -1) } : { type: 'bold', text: token.slice(2, -2) });
    last = m.index! + token.length;
  }
  if (last < text.length) out.push({ type: 'text', text: text.slice(last) });
  return out;
}

const BULLET = /^\s*[-*•]\s+(.*)$/;
const NUMBERED = /^\s*\d+[.)]\s+(.*)$/;
const HEADING = /^\s*#{1,6}\s+(.*)$/;

export function parse(text: string): Block[] {
  const blocks: Block[] = [];
  const parts = text.split('```');
  parts.forEach((part, i) => {
    if (i % 2 === 1) {
      blocks.push({ type: 'code', text: part.replace(/^[a-zA-Z0-9+#-]*\n/, '').replace(/\n$/, '') });
      return;
    }
    let para: Inline[][] = [];
    let list: { ordered: boolean; items: Inline[][] } | null = null;
    const flush = () => {
      if (para.length) blocks.push({ type: 'para', lines: para });
      if (list) blocks.push({ type: 'list', ...list });
      para = [];
      list = null;
    };
    for (const line of part.split('\n')) {
      const heading = line.match(HEADING);
      const bullet = line.match(BULLET);
      const numbered = line.match(NUMBERED);
      if (!line.trim()) {
        flush();
      } else if (heading) {
        flush();
        blocks.push({ type: 'heading', inlines: inlines(heading[1]) });
      } else if (bullet || numbered) {
        const ordered = !!numbered;
        if (para.length || (list && list.ordered !== ordered)) flush();
        list = list ?? { ordered, items: [] };
        list.items.push(inlines((bullet ?? numbered)![1]));
      } else {
        if (list) flush();
        para.push(inlines(line));
      }
    }
    flush();
  });
  return blocks;
}

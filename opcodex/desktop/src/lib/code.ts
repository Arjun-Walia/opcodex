// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import type { DecompileToken } from '../engine/types';

const KEYWORDS = new Set([
  'if', 'else', 'while', 'for', 'do', 'return', 'break', 'continue', 'switch', 'case',
  'default', 'goto', 'sizeof', 'struct', 'union', 'enum', 'typedef', 'const', 'volatile',
  'static', 'extern', 'unsigned', 'signed',
]);
const NUMBER = /^(?:0x[0-9a-f]+|\d+(?:\.\d+)?)[ulf]*$/i;

/** The colour class of a decompiler token, from its kind and its text. */
export function classify(token: DecompileToken): string {
  const text = token.text.trim();
  if (token.kind === 'comment') return 'comment';
  if (token.kind === 'funcName') return 'funcName';
  if (token.kind === 'type') return 'type';
  if (token.kind === 'field') return 'field';
  if (token.kind === 'label') return 'label';
  if (KEYWORDS.has(text)) return 'keyword';
  if (NUMBER.test(text)) return 'number';
  if (text.startsWith('"') || text.startsWith("'")) return 'string';
  if (token.kind === 'variable') return 'variable';
  return token.kind === 'op' ? 'op' : 'syntax';
}

/** Splits the token stream into lines at break tokens, keeping their indentation. */
export function toLines(tokens: DecompileToken[]): DecompileToken[][] {
  const lines: DecompileToken[][] = [[]];
  for (const token of tokens) {
    if (token.kind === 'break') {
      const indent = token.text.replace(/^\n/, '');
      lines.push(indent ? [{ text: indent, kind: 'syntax' }] : []);
    } else {
      lines[lines.length - 1].push(token);
    }
  }
  while (lines.length > 1 && lines[0].length === 0) lines.shift();
  return lines;
}

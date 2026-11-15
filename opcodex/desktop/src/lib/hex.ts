// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0

export interface HexRow {
  offset: string;
  hex: string[];
  ascii: string;
}

export function parseHex(hex: string): Uint8Array {
  const out = new Uint8Array(hex.length / 2);
  for (let i = 0; i < out.length; i++) out[i] = parseInt(hex.slice(i * 2, i * 2 + 2), 16);
  return out;
}

/** Formats bytes as 16-byte rows starting at `base`, with printable ASCII beside them. */
export function formatHexRows(bytes: Uint8Array, base: bigint, width = 16): HexRow[] {
  const rows: HexRow[] = [];
  for (let i = 0; i < bytes.length; i += width) {
    const slice = bytes.slice(i, i + width);
    rows.push({
      offset: (base + BigInt(i)).toString(16).padStart(8, '0'),
      hex: Array.from(slice, (b) => b.toString(16).padStart(2, '0')),
      ascii: Array.from(slice, (b) => (b >= 0x20 && b < 0x7f ? String.fromCharCode(b) : '.')).join(''),
    });
  }
  return rows;
}

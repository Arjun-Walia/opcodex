// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Shapes of the engine results the interface uses (see catalog.list for the full schemas).

export interface ProgramEntry {
  programId: string;
  name: string;
  path: string;
  open: boolean;
  language?: string;
  revision?: number;
}

export interface FunctionSummary {
  entry: string;
  name: string;
  size: number;
  callers: number;
  callees: number;
}

export interface Page<T> {
  items: T[];
  nextCursor?: string;
}

export interface Block {
  name: string;
  start: string;
  end: string;
  size: number;
  permissions: string;
  initialized: boolean;
  entropy?: number;
}

export interface ProgramSummary {
  name: string;
  format: string;
  language: string;
  processor: string;
  compiler: string;
  endian: string;
  addressSize: number;
  imageBase: string;
  functionCount: number;
  stringCount: number;
  blocks: Block[];
  entryPoints: { address: string; name: string }[];
  imports: { library: string; functions: string[] }[];
  riskyApis: { name: string; library: string; category: string }[];
  notableStrings: { address: string; value: string; kind: string }[];
}

export interface DecompileToken {
  text: string;
  kind: string;
  address?: string;
  varName?: string;
}

export interface Decompiled {
  entry: string;
  name: string;
  c: string;
  tokens: DecompileToken[];
}

export interface Instruction {
  address: string;
  bytes: string;
  mnemonic: string;
  operands: string;
  /** Pre and end-of-line comments, joined. */
  comment?: string;
}

export interface FunctionDetail {
  entry: string;
  name: string;
  signature: string;
  body: { start: string; end: string }[];
  callers: { entry: string; name: string }[];
  callees: { entry: string; name: string }[];
}

export interface Xref {
  from: string;
  to: string;
  type: string;
}

export interface HistoryEntry {
  id: string;
  time: string;
  actor: string;
  op: string;
  summary?: string;
  revertOf?: string;
}

export interface Job {
  jobId: string;
  kind: string;
  state: 'running' | 'done' | 'failed' | 'cancelled';
  message?: string;
  progress?: number;
  maximum?: number;
}

// function.graph / function.callgraph
export type EdgeKind = 'true' | 'false' | 'jump' | 'fall';
export type GraphBlock = { start: string; end: string; instructions: { address: string; text: string; comment?: string }[] };
export type FunctionGraph = {
  entry: string;
  name: string;
  blocks: GraphBlock[];
  edges: { from: string; to: string; kind: EdgeKind }[];
  truncated: boolean;
};
export type CallGraph = {
  root: string;
  nodes: { entry: string; name: string; level: number; external: boolean }[];
  edges: { from: string; to: string }[];
  truncated: boolean;
};

// debug.*
export type DebugState = {
  pc: string;
  running: boolean;
  reason: 'started' | 'step' | 'breakpoint' | 'reached' | 'returned' | 'exited' | 'limit' | 'error';
  steps: number;
  instruction: string;
  function: string;
  registers: { name: string; value: string; bits: number }[];
  changed: string[];
  stack: { address: string; value: string; offset: number }[];
  breakpoints: string[];
  events: string[];
};

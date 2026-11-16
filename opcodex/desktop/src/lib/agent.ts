// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The agent chat's model side: tools from the engine catalog, request bodies for Anthropic and
// OpenAI-compatible APIs (OpenAI and local servers such as Ollama), and stream decoding. Pure.

export type Provider = 'anthropic' | 'openai' | 'local';

export type CatalogOp = { name: string; description: string; class: string; params: Record<string, unknown> };
export type Tool = { name: string; op: string; description: string; cls: string; schema: Record<string, unknown> };

export type TextPart = { type: 'text'; text: string };
export type ToolCall = { type: 'tool_call'; id: string; name: string; input: Record<string, unknown> };
export type ToolResult = { type: 'tool_result'; id: string; content: string; isError: boolean };
export type Part = TextPart | ToolCall | ToolResult;
export type ChatMessage = { role: 'user' | 'assistant'; parts: Part[] };

/** Operations an assistant may not call: policy and engine settings stay with the person. */
const HIDDEN = /^(policy\.|catalog\.list$|plugin\.(install|remove|setEnabled)$|script\.import$)/;

export function toolName(op: string): string {
  return op.replace(/[^a-zA-Z0-9_-]/g, '_').slice(0, 64);
}

/**
 * A focused set for local models: small models and servers with short context windows (Ollama
 * defaults to 4096 tokens) cope badly with dozens of tool definitions.
 */
export const CORE_TOOLS = new Set([
  'program.info',
  'program.summary',
  'program.functions.list',
  'function.get',
  'code.decompile',
  'code.disassemble',
  'xrefs.to',
  'xrefs.from',
  'program.strings.list',
  'symbol.rename',
  'comment.set',
  'history.list',
]);

export function toolsFrom(catalog: CatalogOp[], options: { core?: boolean } = {}): Tool[] {
  return catalog
    .filter((op) => op.class !== 'system' && !HIDDEN.test(op.name))
    .filter((op) => !options.core || CORE_TOOLS.has(op.name))
    .map((op) => ({ name: toolName(op.name), op: op.name, description: op.description, cls: op.class, schema: op.params }));
}

export function fromToolName(tools: Tool[], name: string): Tool | undefined {
  return tools.find((t) => t.name === name);
}

const MAX_TOKENS = 4096;

export function anthropicBody(model: string, system: string, messages: ChatMessage[], tools: Tool[]) {
  return {
    model,
    max_tokens: MAX_TOKENS,
    stream: true,
    system,
    tools: tools.map((t) => ({ name: t.name, description: t.description, input_schema: t.schema })),
    messages: messages.map((m) => ({
      role: m.role,
      content: m.parts.map((p) => {
        if (p.type === 'text') return { type: 'text', text: p.text };
        if (p.type === 'tool_call') return { type: 'tool_use', id: p.id, name: p.name, input: p.input };
        return { type: 'tool_result', tool_use_id: p.id, content: p.content, is_error: p.isError };
      }),
    })),
  };
}

type OpenAiMessage = Record<string, unknown> & { role: string; tool_calls?: { id: string; type: string; function: { name: string; arguments: string } }[] };

export function openaiBody(model: string, system: string, messages: ChatMessage[], tools: Tool[]) {
  const out: OpenAiMessage[] = [{ role: 'system', content: system }];
  for (const m of messages) {
    if (m.role === 'assistant') {
      const text = m.parts.filter((p): p is TextPart => p.type === 'text').map((p) => p.text).join('');
      const calls = m.parts.filter((p): p is ToolCall => p.type === 'tool_call');
      const msg: OpenAiMessage = { role: 'assistant', content: text || null };
      if (calls.length) {
        msg.tool_calls = calls.map((c) => ({ id: c.id, type: 'function', function: { name: c.name, arguments: JSON.stringify(c.input) } }));
      }
      out.push(msg);
      continue;
    }
    const text = m.parts.filter((p): p is TextPart => p.type === 'text').map((p) => p.text).join('');
    if (text) out.push({ role: 'user', content: text });
    for (const r of m.parts.filter((p): p is ToolResult => p.type === 'tool_result')) {
      out.push({ role: 'tool', tool_call_id: r.id, content: r.content });
    }
  }
  return {
    model,
    stream: true,
    messages: out,
    tools: tools.map((t) => ({ type: 'function', function: { name: t.name, description: t.description, parameters: t.schema } })),
  };
}

/** Splits a server-sent event stream into events, keeping partial lines between chunks. */
export class SseBuffer {
  private buffer = '';
  private event = '';
  private data: string[] = [];

  push(chunk: string): { event: string; data: string }[] {
    this.buffer += chunk;
    const out: { event: string; data: string }[] = [];
    let newline: number;
    while ((newline = this.buffer.indexOf('\n')) >= 0) {
      const line = this.buffer.slice(0, newline).replace(/\r$/, '');
      this.buffer = this.buffer.slice(newline + 1);
      if (line === '') {
        if (this.data.length) out.push({ event: this.event, data: this.data.join('\n') });
        this.event = '';
        this.data = [];
      } else if (line.startsWith('event:')) {
        this.event = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        this.data.push(line.slice(5).replace(/^ /, ''));
      }
    }
    return out;
  }
}

type Pending = { id: string; name: string; json: string };
export type Decoded = { toolCalls: ToolCall[]; stop: string };

function parseInput(json: string): Record<string, unknown> {
  try {
    const v = JSON.parse(json || '{}');
    return v && typeof v === 'object' && !Array.isArray(v) ? v : {};
  } catch {
    return {};
  }
}

function finish(pending: Map<number, Pending>, stop: string): Decoded {
  const toolCalls = [...pending.entries()]
    .sort(([a], [b]) => a - b)
    .map(([, p]) => ({ type: 'tool_call' as const, id: p.id, name: p.name, input: parseInput(p.json) }));
  return { toolCalls, stop };
}

type AnthropicState = { pending: Map<number, Pending>; stop: string; error?: string; thinking?: boolean };
type OpenAiState = { pending: Map<number, Pending>; stop: string; error?: string; thinking?: boolean };

/** Removes reasoning that some local models put in the text as <think>…</think>. */
export function stripThinking(text: string): string {
  return text.replace(/<think>[\s\S]*?(<\/think>\s*|$)/g, '');
}

/** True while the text ends inside an unfinished <think> block. */
export function insideThinking(text: string): boolean {
  const open = text.lastIndexOf('<think>');
  return open >= 0 && text.indexOf('</think>', open) < 0;
}

/** Stream decoders: `event` returns new text to show; `finish` returns tool calls and the stop reason. */
export const decode = {
  anthropic: {
    start: (): AnthropicState => ({ pending: new Map(), stop: '' }),
    event(state: AnthropicState, data: string): string {
      let e: Record<string, any>;
      try {
        e = JSON.parse(data);
      } catch {
        return '';
      }
      if (e.type === 'content_block_start' && e.content_block?.type === 'thinking') {
        state.thinking = true;
      } else if (e.type === 'content_block_start' && e.content_block?.type === 'tool_use') {
        state.pending.set(e.index, { id: e.content_block.id, name: e.content_block.name, json: '' });
      } else if (e.type === 'content_block_delta') {
        if (e.delta?.type === 'text_delta') return e.delta.text ?? '';
        if (e.delta?.type === 'input_json_delta') {
          const p = state.pending.get(e.index);
          if (p) p.json += e.delta.partial_json ?? '';
        }
      } else if (e.type === 'message_delta' && e.delta?.stop_reason) {
        state.stop = e.delta.stop_reason;
      } else if (e.type === 'error') {
        state.error = e.error?.message ?? 'The model reported an error.';
      }
      return '';
    },
    finish: (state: AnthropicState): Decoded & { error?: string } => ({ ...finish(state.pending, state.stop), error: state.error }),
  },
  openai: {
    start: (): OpenAiState => ({ pending: new Map(), stop: '' }),
    event(state: OpenAiState, data: string): string {
      if (data === '[DONE]') return '';
      let e: Record<string, any>;
      try {
        e = JSON.parse(data);
      } catch {
        return '';
      }
      if (e.error) {
        state.error = e.error.message ?? 'The model reported an error.';
        return '';
      }
      const choice = e.choices?.[0];
      if (!choice) return '';
      // Thinking models stream reasoning separately (Ollama: "reasoning"; others: "reasoning_content").
      if (choice.delta?.reasoning || choice.delta?.reasoning_content) state.thinking = true;
      for (const call of choice.delta?.tool_calls ?? []) {
        const index = call.index ?? 0;
        // Ids must be unique across the conversation; some local servers omit them.
        const p = state.pending.get(index) ?? { id: call.id ?? `call_${Math.random().toString(36).slice(2, 10)}_${index}`, name: '', json: '' };
        if (call.id) p.id = call.id;
        if (call.function?.name) p.name = call.function.name;
        if (call.function?.arguments) p.json += call.function.arguments;
        state.pending.set(index, p);
      }
      if (choice.finish_reason) state.stop = choice.finish_reason;
      return choice.delta?.content ?? '';
    },
    finish: (state: OpenAiState): Decoded & { error?: string } => ({ ...finish(state.pending, state.stop), error: state.error }),
  },
};

/** The system prompt: what the assistant is, what's open, and how to behave. */
export function systemPrompt(context: { program?: string; programId?: string; location?: string; revision?: number }): string {
  const lines = [
    'You are the analysis assistant inside Opcodex, a reverse-engineering app.',
    'Use the tools to read the program before answering; quote addresses you looked at.',
    'Never invent addresses, names or code. If a tool fails, say so and try another way.',
    'Changes (renames, comments, scripts) are recorded in history and may need the user’s approval.',
    'Write changes need expectedRevision: read it from program.info first and use the newest revision.',
    'Be concise. Use short paragraphs and code blocks for code.',
  ];
  if (context.programId) {
    lines.push(`Open program: ${context.program ?? 'unknown'} (programId "${context.programId}").`);
    if (context.revision !== undefined) lines.push(`Current revision: ${context.revision}.`);
    if (context.location) lines.push(`The user is looking at address ${context.location}.`);
  } else {
    lines.push('No program is open. Ask the user to open one, or use project and program tools.');
  }
  return lines.join('\n');
}

/** Tool results are capped so one large listing can't flood the conversation. */
export const MAX_RESULT_CHARS = 24_000;

export function resultText(value: unknown): string {
  const text = typeof value === 'string' ? value : JSON.stringify(value);
  return text.length <= MAX_RESULT_CHARS ? text : `${text.slice(0, MAX_RESULT_CHARS)}\n… cut at ${MAX_RESULT_CHARS} characters; ask for a smaller page.`;
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// One agent turn: send, stream the reply, run tool calls (asking first when the policy says so),
// feed the results back, and repeat until the model answers without tools.
import {
  anthropicBody,
  decode,
  fromToolName,
  openaiBody,
  resultText,
  SseBuffer,
  type ChatMessage,
  type Part,
  type Provider,
  type Tool,
  type ToolCall,
  type ToolResult,
} from './agent';

export const MAX_STEPS = 20;

export type Policy = 'ask' | 'allow-writes' | 'allow-all';

export type TurnDeps = {
  provider: Provider;
  model: string;
  system: string;
  tools: Tool[];
  policy: Policy;
  /** Sends a request body; yields raw response chunks. */
  send(body: unknown): AsyncIterable<string>;
  /** Runs an engine operation. */
  execute(op: string, input: Record<string, unknown>): Promise<unknown>;
  /** Asks the person to approve a change; resolves true to go ahead. */
  approve(tool: Tool, input: Record<string, unknown>): Promise<boolean>;
  onText(delta: string): void;
  onTool(event: { phase: 'start' | 'done'; call: ToolCall; result?: ToolResult }): void;
  cancelled(): boolean;
};

export function needsApproval(cls: string, policy: Policy): boolean {
  if (cls === 'read') return false;
  if (cls === 'write') return policy === 'ask';
  return policy !== 'allow-all'; // run (scripts) and anything unknown
}

async function oneReply(deps: TurnDeps, messages: ChatMessage[]): Promise<{ parts: Part[]; calls: ToolCall[] }> {
  const body =
    deps.provider === 'anthropic'
      ? anthropicBody(deps.model, deps.system, messages, deps.tools)
      : openaiBody(deps.model, deps.system, messages, deps.tools);
  const codec = deps.provider === 'anthropic' ? decode.anthropic : decode.openai;
  const state = codec.start() as never;
  const sse = new SseBuffer();
  let text = '';
  for await (const chunk of deps.send(body)) {
    if (deps.cancelled()) break;
    for (const e of sse.push(chunk)) {
      const delta = (codec.event as (s: never, d: string) => string)(state, e.data);
      if (delta) {
        text += delta;
        deps.onText(delta);
      }
    }
  }
  const out = (codec.finish as (s: never) => { toolCalls: ToolCall[]; error?: string })(state);
  if (out.error) throw new Error(out.error);
  const parts: Part[] = [];
  if (text) parts.push({ type: 'text', text });
  parts.push(...out.toolCalls);
  return { parts, calls: out.toolCalls };
}

async function runCall(deps: TurnDeps, call: ToolCall): Promise<ToolResult> {
  const tool = fromToolName(deps.tools, call.name);
  if (!tool) return { type: 'tool_result', id: call.id, content: `No tool named ${call.name}.`, isError: true };
  if (needsApproval(tool.cls, deps.policy) && !(await deps.approve(tool, call.input))) {
    return { type: 'tool_result', id: call.id, content: 'The user declined this change.', isError: true };
  }
  try {
    return { type: 'tool_result', id: call.id, content: resultText(await deps.execute(tool.op, call.input)), isError: false };
  } catch (e) {
    return { type: 'tool_result', id: call.id, content: e instanceof Error ? e.message : String(e), isError: true };
  }
}

/**
 * Makes a conversation valid to send again after a stop: no empty assistant messages, and every
 * tool call answered (providers refuse a tool call without its result).
 */
export function settle(messages: ChatMessage[]): ChatMessage[] {
  const out = messages.filter((m) => m.role !== 'assistant' || m.parts.length > 0);
  const last = out.at(-1);
  if (last?.role === 'assistant') {
    const open = last.parts.filter((p): p is ToolCall => p.type === 'tool_call');
    if (open.length) {
      return [
        ...out,
        { role: 'user', parts: open.map((c) => ({ type: 'tool_result', id: c.id, content: 'Stopped by the user.', isError: true }) as ToolResult) },
      ];
    }
  }
  return out;
}

/** Runs one user turn to completion and returns the full conversation afterwards. */
export async function runTurn(deps: TurnDeps, history: ChatMessage[]): Promise<ChatMessage[]> {
  let messages = history;
  for (let step = 0; step < MAX_STEPS; step++) {
    if (deps.cancelled()) return settle(messages);
    const reply = await oneReply(deps, messages);
    if (reply.parts.length) messages = [...messages, { role: 'assistant', parts: reply.parts }];
    if (deps.cancelled()) return settle(messages);
    if (reply.calls.length === 0) return messages;
    const results: ToolResult[] = [];
    for (const call of reply.calls) {
      deps.onTool({ phase: 'start', call });
      const result = deps.cancelled()
        ? ({ type: 'tool_result', id: call.id, content: 'Stopped by the user.', isError: true } as ToolResult)
        : await runCall(deps, call);
      deps.onTool({ phase: 'done', call, result });
      results.push(result);
    }
    messages = [...messages, { role: 'user', parts: results }];
  }
  return [...messages, { role: 'assistant', parts: [{ type: 'text', text: `Stopped after ${MAX_STEPS} steps. Ask me to continue if you want more.` }] }];
}

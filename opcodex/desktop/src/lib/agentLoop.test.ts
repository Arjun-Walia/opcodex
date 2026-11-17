import { describe, expect, it } from 'vitest';
import { toolsFrom, type ChatMessage } from './agent';
import { needsApproval, runTurn, settle, type TurnDeps } from './agentLoop';

const tools = toolsFrom([
  { name: 'code.decompile', description: 'd', class: 'read', params: { type: 'object' } },
  { name: 'symbol.rename', description: 'r', class: 'write', params: { type: 'object' } },
  { name: 'script.run', description: 's', class: 'run', params: { type: 'object' } },
]);

function sse(events: unknown[]): string {
  return events.map((e) => `data: ${JSON.stringify(e)}\n\n`).join('');
}

/** A fake OpenAI-compatible model that answers with the given replies in order. */
function deps(replies: string[], overrides: Partial<TurnDeps> = {}) {
  const calls: { op: string; input: unknown }[] = [];
  const asked: string[] = [];
  let i = 0;
  const d: TurnDeps = {
    provider: 'openai',
    model: 'm',
    system: 's',
    tools,
    policy: 'ask',
    send: async function* () {
      const reply = replies[i++] ?? '';
      // Deliver in two pieces to exercise chunking.
      yield reply.slice(0, 7);
      yield reply.slice(7);
    },
    execute: async (op, input) => {
      calls.push({ op, input });
      return { ok: true, op };
    },
    approve: async (tool) => {
      asked.push(tool.op);
      return true;
    },
    onText: () => {},
    onTool: () => {},
    cancelled: () => false,
    ...overrides,
  };
  return { d, calls, asked };
}

const toolCall = (name: string, args: unknown) =>
  sse([{ choices: [{ delta: { tool_calls: [{ index: 0, id: 'c1', function: { name, arguments: JSON.stringify(args) } }] }, finish_reason: 'tool_calls' }] }]);
const answer = (text: string) => sse([{ choices: [{ delta: { content: text }, finish_reason: 'stop' }] }]) + 'data: [DONE]\n\n';
const user: ChatMessage[] = [{ role: 'user', parts: [{ type: 'text', text: 'hi' }] }];

describe('approval rules', () => {
  it('reads never ask; writes ask under "ask"; scripts ask unless allow-all', () => {
    expect(needsApproval('read', 'ask')).toBe(false);
    expect(needsApproval('write', 'ask')).toBe(true);
    expect(needsApproval('write', 'allow-writes')).toBe(false);
    expect(needsApproval('run', 'allow-writes')).toBe(true);
    expect(needsApproval('run', 'allow-all')).toBe(false);
  });
});

describe('runTurn', () => {
  it('runs a read tool without asking, then returns the answer', async () => {
    const { d, calls, asked } = deps([toolCall('code_decompile', { address: '0x10' }), answer('It returns 2.')]);
    const out = await runTurn(d, user);
    expect(calls).toEqual([{ op: 'code.decompile', input: { address: '0x10' } }]);
    expect(asked).toEqual([]);
    expect(out.at(-1)).toEqual({ role: 'assistant', parts: [{ type: 'text', text: 'It returns 2.' }] });
    expect(out[2].parts[0]).toMatchObject({ type: 'tool_result', id: 'c1', isError: false });
  });

  it('asks before a write and reports a refusal to the model', async () => {
    const { d, calls, asked } = deps([toolCall('symbol_rename', { name: 'x' }), answer('OK, left it.')], {
      approve: async (tool) => {
        asked.push(tool.op);
        return false;
      },
    });
    const out = await runTurn(d, user);
    expect(asked).toEqual(['symbol.rename']);
    expect(calls).toEqual([]);
    expect(out[2].parts[0]).toMatchObject({ isError: true, content: 'The user declined this change.' });
  });

  it('refuses tools that are not offered', async () => {
    const { d, calls } = deps([toolCall('policy_set', { mode: 'allow-all' }), answer('Done.')]);
    const out = await runTurn(d, user);
    expect(calls).toEqual([]);
    expect(out[2].parts[0]).toMatchObject({ isError: true, content: 'No tool named policy_set.' });
  });

  it('turns engine errors into tool errors instead of failing the turn', async () => {
    const { d } = deps([toolCall('code_decompile', {}), answer('Sorry.')], {
      execute: async () => {
        throw new Error('No function contains 0x0');
      },
    });
    const out = await runTurn(d, user);
    expect(out[2].parts[0]).toMatchObject({ isError: true, content: 'No function contains 0x0' });
  });

  it('stops after the step limit', async () => {
    const { d } = deps(Array.from({ length: 30 }, () => toolCall('code_decompile', {})));
    const out = await runTurn(d, user);
    expect((out.at(-1)!.parts[0] as { text: string }).text).toMatch(/Stopped after 20 steps/);
  });

  it('a stop before any reply leaves no empty message and sends nothing more', async () => {
    let sends = 0;
    let stop = false;
    const { d } = deps([answer('never')], {
      send: async function* () {
        sends++;
        stop = true;
        yield '';
      },
      cancelled: () => stop,
    });
    const out = await runTurn(d, user);
    expect(out).toEqual(user);
    expect(sends).toBe(1);
  });

  it('a stop during a tool call answers it, so the next message is valid', async () => {
    let stop = false;
    const { d, calls } = deps([toolCall('symbol_rename', { name: 'x' }), answer('unused')], {
      approve: async () => {
        stop = true;
        return false;
      },
      cancelled: () => stop,
    });
    const out = await runTurn(d, user);
    expect(calls).toEqual([]);
    const lastCall = out[1].parts.find((p) => p.type === 'tool_call');
    expect(lastCall).toBeTruthy();
    expect(out.at(-1)!.role).toBe('user');
    expect(out.at(-1)!.parts[0]).toMatchObject({ type: 'tool_result', id: 'c1', isError: true });
  });

  it('settle drops empty replies and answers dangling tool calls', () => {
    const dangling: ChatMessage[] = [
      ...user,
      { role: 'assistant', parts: [] },
      { role: 'assistant', parts: [{ type: 'tool_call', id: 't9', name: 'code_decompile', input: {} }] },
    ];
    const out = settle(dangling);
    expect(out.some((m) => m.role === 'assistant' && m.parts.length === 0)).toBe(false);
    expect(out.at(-1)).toEqual({ role: 'user', parts: [{ type: 'tool_result', id: 't9', content: 'Stopped by the user.', isError: true }] });
  });

  it('surfaces a model error', async () => {
    const { d } = deps([sse([{ error: { message: 'invalid x-api-key' } }])]);
    await expect(runTurn(d, user)).rejects.toThrow('invalid x-api-key');
  });
});

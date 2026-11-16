import { describe, expect, it } from 'vitest';
import {
  anthropicBody,
  decode,
  fromToolName,
  openaiBody,
  SseBuffer,
  toolsFrom,
  toolName,
  type ChatMessage,
} from './agent';

const catalog = [
  { name: 'code.decompile', description: 'Decompiles.', class: 'read', params: { type: 'object', properties: { address: { type: 'string' } } } },
  { name: 'symbol.rename', description: 'Renames.', class: 'write', params: { type: 'object', properties: {} } },
  { name: 'policy.set', description: 'Policy.', class: 'system', params: { type: 'object' } },
  { name: 'catalog.list', description: 'Lists.', class: 'read', params: { type: 'object' } },
];

describe('tools', () => {
  it('maps operation names to names models accept, and back', () => {
    expect(toolName('program.functions.list')).toBe('program_functions_list');
    const tools = toolsFrom(catalog);
    expect(tools.map((t) => t.op)).toEqual(['code.decompile', 'symbol.rename']);
    expect(fromToolName(tools, 'code_decompile')?.op).toBe('code.decompile');
    expect(fromToolName(tools, 'policy_set')).toBeUndefined();
  });
});

const history: ChatMessage[] = [
  { role: 'user', parts: [{ type: 'text', text: 'What does main do?' }] },
  { role: 'assistant', parts: [{ type: 'text', text: 'Let me look.' }, { type: 'tool_call', id: 't1', name: 'code_decompile', input: { address: '0x1000' } }] },
  { role: 'user', parts: [{ type: 'tool_result', id: 't1', content: '{"c":"int main()"}', isError: false }] },
];

describe('request bodies', () => {
  it('builds an Anthropic messages request with tool use and results', () => {
    const body = anthropicBody('claude-sonnet-5-5', 'system text', history, toolsFrom(catalog));
    expect(body.stream).toBe(true);
    expect(body.system).toBe('system text');
    expect(body.tools[0]).toMatchObject({ name: 'code_decompile', input_schema: { type: 'object' } });
    expect(body.messages[1].content[1]).toEqual({ type: 'tool_use', id: 't1', name: 'code_decompile', input: { address: '0x1000' } });
    expect(body.messages[2].content[0]).toMatchObject({ type: 'tool_result', tool_use_id: 't1', is_error: false });
  });

  it('builds an OpenAI-compatible chat request with tool calls and tool messages', () => {
    const body = openaiBody('gpt-5', 'system text', history, toolsFrom(catalog));
    expect(body.messages[0]).toEqual({ role: 'system', content: 'system text' });
    expect(body.messages[2].tool_calls?.[0]).toEqual({
      id: 't1', type: 'function', function: { name: 'code_decompile', arguments: '{"address":"0x1000"}' },
    });
    expect(body.messages[3]).toEqual({ role: 'tool', tool_call_id: 't1', content: '{"c":"int main()"}' });
    expect(body.tools[1].function.name).toBe('symbol_rename');
  });
});

describe('streams', () => {
  it('splits server-sent events across chunk boundaries', () => {
    const sse = new SseBuffer();
    expect(sse.push('event: a\ndata: {"x"')).toEqual([]);
    expect(sse.push(':1}\n\ndata: [DONE]\n\n')).toEqual([{ event: 'a', data: '{"x":1}' }, { event: '', data: '[DONE]' }]);
  });

  it('decodes an Anthropic stream with text and a tool call', () => {
    const state = decode.anthropic.start();
    const events = [
      { type: 'content_block_start', index: 0, content_block: { type: 'text', text: '' } },
      { type: 'content_block_delta', index: 0, delta: { type: 'text_delta', text: 'Hello' } },
      { type: 'content_block_start', index: 1, content_block: { type: 'tool_use', id: 'tu_1', name: 'code_decompile' } },
      { type: 'content_block_delta', index: 1, delta: { type: 'input_json_delta', partial_json: '{"addr' } },
      { type: 'content_block_delta', index: 1, delta: { type: 'input_json_delta', partial_json: 'ess":"0x10"}' } },
      { type: 'message_delta', delta: { stop_reason: 'tool_use' } },
    ];
    let text = '';
    for (const e of events) text += decode.anthropic.event(state, JSON.stringify(e));
    const out = decode.anthropic.finish(state);
    expect(text).toBe('Hello');
    expect(out.toolCalls).toEqual([{ type: 'tool_call', id: 'tu_1', name: 'code_decompile', input: { address: '0x10' } }]);
    expect(out.stop).toBe('tool_use');
  });

  it('decodes an OpenAI stream with fragmented tool call arguments', () => {
    const state = decode.openai.start();
    const chunks = [
      { choices: [{ delta: { content: 'Looking' } }] },
      { choices: [{ delta: { tool_calls: [{ index: 0, id: 'c1', function: { name: 'code_decompile', arguments: '{"add' } }] } }] },
      { choices: [{ delta: { tool_calls: [{ index: 0, function: { arguments: 'ress":"0x20"}' } }] } }] },
      { choices: [{ delta: {}, finish_reason: 'tool_calls' }] },
    ];
    let text = '';
    for (const c of chunks) text += decode.openai.event(state, JSON.stringify(c));
    text += decode.openai.event(state, '[DONE]');
    const out = decode.openai.finish(state);
    expect(text).toBe('Looking');
    expect(out.toolCalls[0]).toEqual({ type: 'tool_call', id: 'c1', name: 'code_decompile', input: { address: '0x20' } });
    expect(out.stop).toBe('tool_calls');
  });

  it('treats unparseable tool arguments as an empty input rather than failing', () => {
    const state = decode.openai.start();
    decode.openai.event(state, JSON.stringify({ choices: [{ delta: { tool_calls: [{ index: 0, id: 'c2', function: { name: 'x', arguments: '{bad' } }] } }] }));
    expect(decode.openai.finish(state).toolCalls[0].input).toEqual({});
  });
});

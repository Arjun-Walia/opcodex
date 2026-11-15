// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Development only: runs the interface in a normal browser against a real engine host,
// holding the desktop credential in this Node process exactly as the Tauri shell does.
import { spawn, type ChildProcess } from 'node:child_process';
import { randomBytes } from 'node:crypto';
import { createConnection } from 'node:net';
import { homedir } from 'node:os';
import { join } from 'node:path';
import { createInterface } from 'node:readline';
import type { Plugin } from 'vite';

const DEFAULT_ENGINE =
  '"C:/Program Files/Java/jdk-25.0.2/bin/java.exe" @D:/opcodex/Ghidra/Features/OpcodexEngine/build/engine.args io.opcodex.engine.EngineMain';

function splitCommand(line: string): string[] {
  const parts: string[] = [];
  let current = '';
  let quoted = false;
  for (const c of line) {
    if (c === '"') quoted = !quoted;
    else if (c === ' ' && !quoted) {
      if (current) parts.push(current);
      current = '';
    } else current += c;
  }
  if (current) parts.push(current);
  return parts;
}

interface Engine {
  port: number;
  token: string;
  child: ChildProcess;
}

function startEngine(): Promise<Engine> {
  const parts = splitCommand(process.env.OPCODEX_ENGINE_CMD || DEFAULT_ENGINE);
  const token = randomBytes(32).toString('hex');
  const sessionDir = join(homedir(), '.opcodex-dev-session');
  const child = spawn(parts[0], [...parts.slice(1), '--tcp', '--desktop-token-stdin'], {
    env: { ...process.env, OPCODEX_SESSION_DIR: sessionDir },
    stdio: ['pipe', 'pipe', 'inherit'],
  });
  child.stdin!.write(JSON.stringify({ credential: token, kind: 'DESKTOP' }) + '\n');
  return new Promise((resolve, reject) => {
    const lines = createInterface({ input: child.stdout! });
    lines.once('line', (line) => {
      try {
        resolve({ port: JSON.parse(line).port, token, child });
      } catch (e) {
        reject(e);
      }
    });
    child.once('exit', (code) => reject(new Error(`engine exited with ${code}`)));
  });
}

function call(engine: Engine, method: string, params: unknown): Promise<string> {
  return new Promise((resolve, reject) => {
    const socket = createConnection({ host: '127.0.0.1', port: engine.port });
    let buffer = '';
    socket.setEncoding('utf8');
    socket.on('data', (chunk) => {
      buffer += chunk;
      const newline = buffer.indexOf('\n');
      if (newline >= 0) {
        resolve(buffer.slice(0, newline));
        socket.end();
      }
    });
    socket.on('error', reject);
    socket.write(
      JSON.stringify({ jsonrpc: '2.0', id: 1, method, params: params ?? {}, auth: engine.token }) + '\n',
    );
  });
}

const CLI = process.env.OPCODEX_CLI || 'D:/opcodex/opcodex/cli/target/release/opcodex.exe';
const AGENT_IDS = new Set(['claude-code', 'claude-desktop', 'cursor', 'vscode', 'windsurf', 'gemini', 'codex']);

function runCli(args: string[]): Promise<{ code: number; stdout: string; stderr: string }> {
  return new Promise((resolve) => {
    const child = spawn(CLI, args, { stdio: ['ignore', 'pipe', 'pipe'] });
    let stdout = '';
    let stderr = '';
    child.stdout!.on('data', (c) => (stdout += c));
    child.stderr!.on('data', (c) => (stderr += c));
    child.on('close', (code) => resolve({ code: code ?? 1, stdout, stderr }));
    child.on('error', (e) => resolve({ code: 1, stdout, stderr: String(e) }));
  });
}

/** Agent app status and registration through the opcodex CLI, as the desktop app does. */
async function agents(method: string, params: { app?: string }) {
  if (method === '__desktop.agents.status') {
    const out = await runCli(['doctor', '--format', 'json']);
    try {
      const checks = JSON.parse(out.stdout) as { name: string; detail: unknown }[];
      return { result: { executable: CLI, apps: checks.find((c) => c.name === 'agents')?.detail ?? [] } };
    } catch {
      return { error: { message: out.stderr || 'opcodex doctor failed.', data: { code: 'ENGINE_ERROR' } } };
    }
  }
  const app = params.app ?? '';
  if (!AGENT_IDS.has(app)) return { error: { message: 'Unknown agent app.', data: { code: 'INVALID_PARAMS' } } };
  const args = method === '__desktop.agents.connect'
    ? ['setup', '--only', app, '--yes']
    : ['setup', '--uninstall', '--only', app, '--yes'];
  const out = await runCli(args);
  return out.code === 0
    ? { result: { output: out.stdout.trim() } }
    : { error: { message: (out.stderr || out.stdout).trim(), data: { code: 'ENGINE_ERROR' } } };
}

export function engineBridge(): Plugin {
  let engine: Promise<Engine> | null = null;
  return {
    name: 'opcodex-engine-bridge',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use('/__engine', (req, res) => {
        if (req.method !== 'POST') {
          res.statusCode = 405;
          res.end();
          return;
        }
        let body = '';
        req.on('data', (c) => (body += c));
        req.on('end', async () => {
          try {
            const { method, params } = JSON.parse(body);
            if (method === '__desktop.defaultProjectsDir') {
              res.setHeader('content-type', 'application/json');
              res.end(JSON.stringify({ result: join(homedir(), 'Documents', 'Opcodex Projects') }));
              return;
            }
            if (method.startsWith('__desktop.agents.')) {
              res.setHeader('content-type', 'application/json');
              res.end(JSON.stringify(await agents(method, params ?? {})));
              return;
            }
            engine ??= startEngine();
            const reply = await call(await engine, method, params);
            res.setHeader('content-type', 'application/json');
            res.end(reply);
          } catch (e) {
            engine = null;
            res.statusCode = 502;
            res.end(JSON.stringify({ error: { message: String(e), data: { code: 'ENGINE_UNAVAILABLE' } } }));
          }
        });
      });
      server.httpServer?.once('close', async () => (await engine)?.child.kill());
    },
  };
}

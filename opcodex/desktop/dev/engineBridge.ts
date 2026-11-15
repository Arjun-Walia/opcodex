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

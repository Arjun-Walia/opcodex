// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from 'react';
import { call, EngineError } from '../engine/client';
import type { Job, ProgramEntry } from '../engine/types';
import { addRecent, loadRecent, saveRecent, type RecentProject } from '../lib/recent';

export type Tab = 'overview' | 'decompile' | 'disassembly' | 'hex' | 'strings' | 'graph' | 'debug';

/** Views that follow navigation instead of switching to Code. */
const STAYS: Tab[] = ['disassembly', 'graph', 'debug'];

interface Activity {
  title: string;
  detail: string;
  progress?: number;
  maximum?: number;
  jobId?: string;
}

interface SessionState {
  project?: { dir: string; name: string };
  programs: ProgramEntry[];
  programId?: string;
  activity?: Activity;
  error?: string;
  recent: RecentProject[];
  tab: Tab;
  /** The current function entry address, if any. */
  location?: string;
  back: string[];
  forward: string[];
  revision: number;
}

interface SessionApi extends SessionState {
  openProject(dir: string, name: string): Promise<void>;
  openBinary(path: string): Promise<void>;
  /** Opens a file's contents (used when a browser can't see the file's path). */
  openBytes(name: string, bytesBase64: string): Promise<void>;
  selectProgram(programId: string): void;
  goTo(entry: string): void;
  goBack(): void;
  goForward(): void;
  setTab(tab: Tab): void;
  closeProject(): Promise<void>;
  cancelActivity(): Promise<void>;
  dismissError(): void;
  showError(message: string): void;
  bump(): void;
}

const SessionContext = createContext<SessionApi | null>(null);

const POLL_MS = 400;

function message(error: unknown): string {
  if (error instanceof EngineError) return error.message;
  if (error instanceof Error) return error.message;
  return 'Something went wrong.';
}

function baseName(path: string): string {
  return path.split(/[\\/]/).pop() ?? path;
}

function projectNameFor(file: string): string {
  const stem = baseName(file).replace(/\.[^.]+$/, '');
  const clean = stem.replace(/[^A-Za-z0-9 ._-]/g, '_').replace(/^[^A-Za-z0-9]+/, '');
  return (clean || 'program').slice(0, 56);
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<SessionState>(() => ({
    programs: [],
    recent: loadRecent(),
    tab: 'overview',
    back: [],
    forward: [],
    revision: 0,
  }));
  const cancelled = useRef(false);

  const patch = useCallback((update: Partial<SessionState>) => {
    setState((s) => ({ ...s, ...update }));
  }, []);

  const remember = useCallback((dir: string, name: string, program?: string) => {
    setState((s) => {
      const recent = addRecent(s.recent, { dir, name, program, openedAt: Date.now() });
      saveRecent(recent);
      return { ...s, recent };
    });
  }, []);

  const waitForJob = useCallback(
    async (jobId: string, title: string) => {
      for (;;) {
        const job = await call<Job>('job.get', { jobId });
        if (job.state !== 'running') return job;
        patch({
          activity: {
            title,
            jobId,
            detail: job.message || 'Finding functions, references and strings',
            progress: job.progress,
            maximum: job.maximum,
          },
        });
        await new Promise((r) => setTimeout(r, POLL_MS));
      }
    },
    [patch],
  );

  const showProgram = useCallback(
    (programs: ProgramEntry[], programId?: string) => {
      patch({
        programs,
        programId: programId ?? programs[0]?.programId,
        activity: undefined,
        tab: 'overview',
        location: undefined,
        back: [],
        forward: [],
      });
    },
    [patch],
  );

  const openProject = useCallback(
    async (dir: string, name: string) => {
      patch({ error: undefined, activity: { title: `Opening ${name}`, detail: 'Loading the project' } });
      try {
        await call('project.open', { dir, name });
        const programs = await call<ProgramEntry[]>('program.list');
        patch({ project: { dir, name } });
        remember(dir, name, programs[0]?.name);
        showProgram(programs);
      } catch (e) {
        patch({ activity: undefined, error: message(e) });
      }
    },
    [patch, remember, showProgram],
  );

  const openSource = useCallback(
    async (file: string, source: { path: string } | { bytesBase64: string }) => {
      cancelled.current = false;
      patch({ error: undefined, activity: { title: `Opening ${file}`, detail: 'Creating a project' } });
      try {
        const dir = await call<string>('__desktop.defaultProjectsDir').catch(() => '');
        const base = projectNameFor(file);
        let name = base;
        for (let n = 2; ; n++) {
          try {
            await call('project.create', { dir, name });
            break;
          } catch (e) {
            if (e instanceof EngineError && e.code === 'CONFLICT' && n < 50) name = `${base}-${n}`;
            else throw e;
          }
        }
        patch({ project: { dir, name }, activity: { title: `Opening ${file}`, detail: 'Importing the file' } });
        const imported = await call<{ programId: string }>('program.import', { ...source, name: file });
        const started = await call<{ jobId: string }>('program.analyze', {
          programId: imported.programId,
          background: true,
        });
        const job = await waitForJob(started.jobId, `Analysing ${file}`);
        if (job.state === 'failed') throw new Error(job.message || 'Analysis failed.');
        const programs = await call<ProgramEntry[]>('program.list');
        remember(dir, name, file);
        showProgram(programs, imported.programId);
      } catch (e) {
        patch({ activity: undefined, error: message(e) });
      }
    },
    [patch, remember, showProgram, waitForJob],
  );

  const openBinary = useCallback((path: string) => openSource(baseName(path), { path }), [openSource]);
  const openBytes = useCallback(
    (name: string, bytesBase64: string) => openSource(baseName(name), { bytesBase64 }),
    [openSource],
  );

  const api = useMemo<SessionApi>(
    () => ({
      ...state,
      openProject,
      openBinary,
      openBytes,
      selectProgram: (programId) =>
        patch({ programId, location: undefined, back: [], forward: [], tab: 'overview' }),
      goTo: (entry) =>
        setState((s) =>
          s.location === entry
            ? { ...s, tab: STAYS.includes(s.tab) ? s.tab : 'decompile' }
            : {
                ...s,
                location: entry,
                back: s.location ? [...s.back, s.location].slice(-100) : s.back,
                forward: [],
                tab: STAYS.includes(s.tab) ? s.tab : 'decompile',
              },
        ),
      goBack: () =>
        setState((s) =>
          s.back.length === 0
            ? s
            : {
                ...s,
                location: s.back[s.back.length - 1],
                back: s.back.slice(0, -1),
                forward: s.location ? [s.location, ...s.forward] : s.forward,
              },
        ),
      goForward: () =>
        setState((s) =>
          s.forward.length === 0
            ? s
            : {
                ...s,
                location: s.forward[0],
                forward: s.forward.slice(1),
                back: s.location ? [...s.back, s.location] : s.back,
              },
        ),
      setTab: (tab) => patch({ tab }),
      closeProject: async () => {
        await call('project.close').catch(() => undefined);
        patch({ project: undefined, programs: [], programId: undefined, location: undefined });
      },
      cancelActivity: async () => {
        cancelled.current = true;
        const jobId = state.activity?.jobId;
        if (jobId) await call('job.cancel', { jobId }).catch(() => undefined);
      },
      dismissError: () => patch({ error: undefined }),
      showError: (error) => patch({ error }),
      bump: () => setState((s) => ({ ...s, revision: s.revision + 1 })),
    }),
    [state, openProject, openBinary, openBytes, patch],
  );

  return <SessionContext.Provider value={api}>{children}</SessionContext.Provider>;
}

export function useSession(): SessionApi {
  const session = useContext(SessionContext);
  if (!session) throw new Error('useSession outside SessionProvider');
  return session;
}

/** Loads engine data for the current inputs and ignores answers to outdated requests. */
export function useEngine<T>(
  method: string | null,
  params: Record<string, unknown>,
  deps: unknown[],
): { data?: T; error?: string; loading: boolean } {
  const [result, setResult] = useState<{ data?: T; error?: string; loading: boolean }>({
    loading: !!method,
  });
  const key = JSON.stringify([method, params, deps]);
  useEffect(() => {
    if (!method) {
      setResult({ loading: false });
      return;
    }
    let live = true;
    setResult((r) => ({ ...r, loading: true, error: undefined }));
    call<T>(method, params).then(
      (data) => live && setResult({ data, loading: false }),
      (e) => live && setResult({ error: message(e), loading: false }),
    );
    return () => {
      live = false;
    };
    // The key captures method, params and deps.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [key]);
  return result;
}

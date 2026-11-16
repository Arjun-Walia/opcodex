// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Plugins: install, turn on or off and remove them; run scripts from plugins or your own folder.
import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { AlertCircle, FileCode2, FolderOpen, Loader2, Package, Play, RotateCw, Trash2, Upload } from 'lucide-react';
import { call, isDesktopApp, pickPath } from '../engine/client';
import { usePrefs } from '../state/prefs';
import { useSession } from '../state/session';

type PluginInfo = {
  id: string;
  name: string;
  version: string;
  description: string;
  author: string;
  enabled: boolean;
  error?: string;
  operations: string[];
  hasOperations: boolean;
  scripts: string[];
  themes: string[];
};
type PluginList = { folder: string; scriptsFolder: string; plugins: PluginInfo[]; restartNeeded: boolean };
type Script = { id: string; name: string; plugin?: string; description: string };
type RunResult = { ok: boolean; output: string; error?: string; revision: number };

function message(e: unknown) {
  return e instanceof Error ? e.message : String(e);
}

export function Plugins() {
  const prefs = usePrefs();
  const session = useSession();
  const [list, setList] = useState<PluginList | null>(null);
  const [scripts, setScripts] = useState<Script[]>([]);
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [run, setRun] = useState<{ script: Script; result?: RunResult; error?: string } | null>(null);
  const [path, setPath] = useState('');
  const desktop = isDesktopApp();

  const reload = useCallback(async () => {
    try {
      const [plugins, found] = await Promise.all([call<PluginList>('plugin.list'), call<Script[]>('script.list')]);
      setList(plugins);
      setScripts(found);
    } catch (e) {
      setError(message(e));
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  async function act(label: string, work: () => Promise<unknown>, done?: string) {
    setBusy(label);
    setError(null);
    setNotice(null);
    try {
      await work();
      await reload();
      prefs.reloadPluginThemes();
      if (done) setNotice(done);
    } catch (e) {
      setError(message(e));
    } finally {
      setBusy(null);
    }
  }

  const install = (source: string) =>
    act('install', async () => {
      const p = await call<PluginInfo & { restartNeeded: boolean }>('plugin.install', { path: source });
      setNotice(`Installed ${p.name} ${p.version}.${p.restartNeeded ? ' Restart the engine to load its operations.' : ''}`);
    });

  async function choose(kind: 'folder' | 'zip') {
    const picked = await pickPath(
      kind === 'folder'
        ? { directory: true, title: 'Choose a plugin folder' }
        : { title: 'Choose a plugin .zip', filters: [{ name: 'Opcodex plugin', extensions: ['zip'] }] },
    );
    if (picked) install(picked);
  }

  function submitPath(e: FormEvent) {
    e.preventDefault();
    const value = path.trim().replace(/^"|"$/g, '');
    if (value) install(value);
  }

  async function restartEngine() {
    await act('restart', async () => {
      await call('__desktop.engine.restart');
      // The engine closed the project when it stopped; open it again where you were.
      if (session.project) await session.openProject(session.project.dir, session.project.name);
    }, 'The engine restarted with your plugins.');
  }

  async function importScript() {
    const picked = await pickPath({ title: 'Choose a Java script', filters: [{ name: 'Java script', extensions: ['java'] }] });
    if (picked) act('import', () => call('script.import', { path: picked }), 'Script added to your scripts.');
  }

  async function runScript(script: Script) {
    if (!session.programId) return;
    setRun({ script });
    try {
      const info = await call<{ revision: number }>('program.info', { programId: session.programId });
      const result = await call<RunResult>('script.run', {
        programId: session.programId,
        script: script.id,
        expectedRevision: info.revision,
        ...(session.location ? { address: session.location } : {}),
      });
      setRun({ script, result });
      if (result.ok) session.bump();
    } catch (e) {
      setRun({ script, error: message(e) });
    }
  }

  return (
    <div className="settings plugins screen">
      <div className="settings-inner">
        <h1 className="page-title">Plugins</h1>
        <p className="page-lead">
          Plugins add operations (for the app, CLI, MCP and agent chat), scripts and themes. They run with your
          permissions: install only plugins from people you trust.
        </p>

        {error && (
          <div className="banner" role="alert">
            <AlertCircle size={16} />
            <span>{error}</span>
          </div>
        )}
        {notice && (
          <div className="banner ok" role="status">
            <span>{notice}</span>
          </div>
        )}
        {list?.restartNeeded && (
          <div className="banner warn" role="status">
            <span style={{ flex: 1 }}>Plugin operations change when the engine restarts.</span>
            <button type="button" className="button" onClick={restartEngine} disabled={!!busy}>
              <RotateCw size={14} /> Restart engine
            </button>
          </div>
        )}

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Installed</h2>
            </div>
            <div className="row-actions">
              {desktop ? (
                <>
                  <button type="button" className="button primary" onClick={() => choose('folder')} disabled={!!busy}>
                    <FolderOpen size={14} /> Install from folder…
                  </button>
                  <button type="button" className="button" onClick={() => choose('zip')} disabled={!!busy}>
                    <Upload size={14} /> Install from .zip…
                  </button>
                  <button type="button" className="button ghost" onClick={() => call('__desktop.openFolder', { kind: 'plugins' }).catch((e) => setError(message(e)))}>
                    Open plugins folder
                  </button>
                </>
              ) : (
                <form className="open-form" onSubmit={submitPath}>
                  <input value={path} onChange={(e) => setPath(e.target.value)} placeholder="Full path of a plugin folder or .zip" aria-label="Plugin path" />
                  <button type="submit" className="button primary" disabled={!!busy || !path.trim()}>
                    Install
                  </button>
                </form>
              )}
            </div>
          </div>
          {!list && !error && <Loader2 size={18} className="spin" aria-label="Loading" />}
          {list && list.plugins.length === 0 && (
            <p className="muted">
              No plugins yet. A plugin is a folder with an <code>opcodex-plugin.json</code> file, and optionally{' '}
              <code>lib/</code> (operations), <code>scripts/</code> and <code>themes/</code>.
            </p>
          )}
          <div className="plugin-list">
            {list?.plugins.map((p) => (
              <article key={p.id} className={`plugin-card${p.enabled ? '' : ' off'}`}>
                <Package size={18} className="plugin-icon" />
                <div className="plugin-main">
                  <div className="plugin-title">
                    <strong>{p.name}</strong>
                    <span className="muted mono">{p.version}</span>
                    {p.author && <span className="muted">by {p.author}</span>}
                  </div>
                  {p.description && <p>{p.description}</p>}
                  <div className="plugin-meta muted">
                    {p.hasOperations && <span>{p.operations.length ? `${p.operations.length} operations` : 'operations after restart'}</span>}
                    {p.scripts.length > 0 && <span>{p.scripts.length} scripts</span>}
                    {p.themes.length > 0 && <span>{p.themes.length} themes</span>}
                  </div>
                  {p.error && (
                    <p className="plugin-error">
                      <AlertCircle size={13} /> {p.error}
                    </p>
                  )}
                </div>
                <button
                  type="button"
                  role="switch"
                  aria-checked={p.enabled}
                  aria-label={`${p.name} enabled`}
                  className="switch"
                  disabled={!!busy || (!!p.error && !p.version)}
                  onClick={() => act('toggle', () => call('plugin.setEnabled', { id: p.id, enabled: !p.enabled }))}
                >
                  <i />
                </button>
                <button
                  type="button"
                  className="icon-button"
                  aria-label={`Remove ${p.name}`}
                  disabled={!!busy}
                  onClick={() => {
                    if (window.confirm(`Remove ${p.name}? Its files are deleted from the plugins folder.`)) {
                      act('remove', () => call('plugin.remove', { id: p.id }), `Removed ${p.name}.`);
                    }
                  }}
                >
                  <Trash2 size={14} />
                </button>
              </article>
            ))}
          </div>
        </section>

        <section className="settings-section">
          <div className="settings-head">
            <div>
              <h2>Scripts</h2>
            </div>
            {desktop && (
              <div className="row-actions">
                <button type="button" className="button" onClick={importScript} disabled={!!busy}>
                  <FileCode2 size={14} /> Add a script…
                </button>
                <button type="button" className="button ghost" onClick={() => call('__desktop.openFolder', { kind: 'scripts' }).catch((e) => setError(message(e)))}>
                  Open scripts folder
                </button>
              </div>
            )}
          </div>
          <p className="muted">
            Java GhidraScripts run against the open program as one change: recorded in history, and undone completely if the
            script fails. Python scripts need PyGhidra and aren’t supported yet.
          </p>
          {scripts.length === 0 && <p className="muted">No scripts yet. Add a .java script or install a plugin that has some.</p>}
          <div className="script-list">
            {scripts.map((s) => (
              <div key={s.id} className="script-row">
                <FileCode2 size={14} className="muted" />
                <div className="script-main">
                  <strong>{s.name}</strong>
                  <span className="muted">{s.description || (s.plugin ? `from ${s.plugin}` : 'your script')}</span>
                </div>
                <button
                  type="button"
                  className="button"
                  disabled={!session.programId || (!!run && !run.result && !run.error)}
                  title={session.programId ? `Run on the open program${session.location ? ` at ${session.location}` : ''}` : 'Open a program first'}
                  onClick={() => runScript(s)}
                >
                  <Play size={13} /> Run
                </button>
              </div>
            ))}
          </div>
          {run && (
            <div className={`script-output${run.result && !run.result.ok || run.error ? ' failed' : ''}`} role="status">
              <div className="script-output-head">
                <strong>{run.script.name}</strong>
                {!run.result && !run.error && <Loader2 size={13} className="spin" />}
                {run.result?.ok && <span className="ok-text">Done</span>}
                {(run.error || (run.result && !run.result.ok)) && <span className="err-text">Failed: nothing was changed</span>}
              </div>
              {(run.result?.output || run.result?.error || run.error) && (
                <pre className="mono">{[run.result?.output, run.result?.error ?? run.error].filter(Boolean).join('\n')}</pre>
              )}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}

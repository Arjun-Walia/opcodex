// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// Drop a program or project anywhere in the window. The app gets real paths from Tauri;
// a browser only gets the file's contents, which are sent to the engine (up to 6 MB).
import { useEffect, useRef, useState } from 'react';
import { FileDown } from 'lucide-react';
import { isDesktopApp } from '../engine/client';
import { splitProjectPath } from '../lib/recent';
import { useSession } from '../state/session';

const BROWSER_LIMIT = 6 * 1024 * 1024;

function toBase64(buffer: ArrayBuffer): string {
  const bytes = new Uint8Array(buffer);
  let binary = '';
  for (let i = 0; i < bytes.length; i += 0x8000) {
    binary += String.fromCharCode(...bytes.subarray(i, i + 0x8000));
  }
  return btoa(binary);
}

export function DropTarget({ onOpened }: { onOpened: () => void }) {
  const session = useSession();
  const [state, setState] = useState<'idle' | 'over' | 'dropped'>('idle');
  const [label, setLabel] = useState('');
  const depth = useRef(0);

  function accept(name: string) {
    setLabel(name);
    setState('dropped');
    setTimeout(() => setState('idle'), 650);
    onOpened();
  }

  // Native app: Tauri reports drag and drop with real file paths.
  useEffect(() => {
    if (!isDesktopApp()) return;
    let unlisten: (() => void) | undefined;
    import('@tauri-apps/api/webview').then(({ getCurrentWebview }) =>
      getCurrentWebview()
        .onDragDropEvent((event) => {
          const { type } = event.payload;
          if (type === 'over' || type === 'enter') setState('over');
          else if (type === 'leave') setState('idle');
          else if (type === 'drop') {
            const path = event.payload.paths[0];
            if (!path) return setState('idle');
            accept(path.split(/[\\/]/).pop() ?? path);
            if (/\.gpr$/i.test(path)) {
              const { dir, name } = splitProjectPath(path);
              session.openProject(dir, name);
            } else session.openBinary(path);
          }
        })
        .then((fn) => (unlisten = fn)),
    );
    return () => unlisten?.();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.openBinary, session.openProject]);

  // Browser preview: HTML drag and drop with the file's bytes.
  useEffect(() => {
    if (isDesktopApp()) return;
    const hasFiles = (e: DragEvent) => Array.from(e.dataTransfer?.types ?? []).includes('Files');
    const enter = (e: DragEvent) => {
      if (!hasFiles(e)) return;
      e.preventDefault();
      depth.current++;
      setState('over');
    };
    const over = (e: DragEvent) => hasFiles(e) && e.preventDefault();
    const leave = (e: DragEvent) => {
      if (!hasFiles(e)) return;
      depth.current = Math.max(0, depth.current - 1);
      if (depth.current === 0) setState('idle');
    };
    const drop = async (e: DragEvent) => {
      if (!hasFiles(e)) return;
      e.preventDefault();
      depth.current = 0;
      const file = e.dataTransfer?.files[0];
      if (!file) return setState('idle');
      if (file.size > BROWSER_LIMIT) {
        setState('idle');
        onOpened();
        return session.showError(
          `${file.name} is larger than 6 MB. The browser preview can't pass a path; open it from the Opcodex app.`,
        );
      }
      accept(file.name);
      session.openBytes(file.name, toBase64(await file.arrayBuffer()));
    };
    window.addEventListener('dragenter', enter);
    window.addEventListener('dragover', over);
    window.addEventListener('dragleave', leave);
    window.addEventListener('drop', drop);
    return () => {
      window.removeEventListener('dragenter', enter);
      window.removeEventListener('dragover', over);
      window.removeEventListener('dragleave', leave);
      window.removeEventListener('drop', drop);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session.openBytes]);

  if (state === 'idle') return null;
  return (
    <div className={`drop-layer ${state}`} aria-live="polite">
      <div className="drop-card">
        <span className="drop-icon">
          <FileDown size={26} />
        </span>
        <strong>{state === 'over' ? 'Drop to open' : `Opening ${label}`}</strong>
        <span className="muted">{state === 'over' ? 'Programs, firmware or .gpr projects' : 'Starting analysis…'}</span>
      </div>
    </div>
  );
}

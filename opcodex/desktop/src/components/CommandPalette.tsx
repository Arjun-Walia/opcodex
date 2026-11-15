// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useEffect, useState } from 'react';
import { Command } from 'cmdk';
import { Binary, Bot, Braces, FileCode2, FolderOpen, FunctionSquare, Gauge, Hash, House, Moon } from 'lucide-react';
import { call } from '../engine/client';
import type { FunctionSummary, Page } from '../engine/types';
import { useSession } from '../state/session';

interface Props {
  onClose: () => void;
  onHome: () => void;
  onOpenProgram: () => void;
  onOpenProject: () => void;
  onSettings: () => void;
  onAgent: () => void;
}

export function CommandPalette({ onClose, onHome, onOpenProgram, onOpenProject, onSettings, onAgent }: Props) {
  const session = useSession();
  const [query, setQuery] = useState('');
  const [matches, setMatches] = useState<FunctionSummary[]>([]);

  useEffect(() => {
    if (!session.programId || query.trim().length < 2) {
      setMatches([]);
      return;
    }
    let live = true;
    const timer = setTimeout(() => {
      call<Page<FunctionSummary>>('program.functions.list', {
        programId: session.programId,
        query: query.trim(),
        limit: 40,
      }).then((page) => live && setMatches(page.items), () => undefined);
    }, 90);
    return () => {
      live = false;
      clearTimeout(timer);
    };
  }, [query, session.programId]);

  function run(action: () => void) {
    onClose();
    action();
  }

  return (
    <div className="palette-backdrop" onMouseDown={onClose}>
      <Command className="palette" label="Search and commands" onMouseDown={(e) => e.stopPropagation()} loop>
        <Command.Input
          autoFocus
          value={query}
          onValueChange={setQuery}
          placeholder={session.programId ? 'Jump to a function, or type a command…' : 'Type a command…'}
          onKeyDown={(e) => e.key === 'Escape' && onClose()}
        />
        <Command.List>
          <Command.Empty>No matches.</Command.Empty>
          {matches.length > 0 && (
            <Command.Group heading="Functions">
              {matches.map((fn) => (
                <Command.Item
                  key={fn.entry}
                  value={`fn ${fn.name} ${fn.entry}`}
                  onSelect={() => run(() => session.goTo(fn.entry))}
                >
                  <FunctionSquare size={15} />
                  <span className="mono" style={{ fontSize: 12.5 }}>
                    {fn.name}
                  </span>
                  <span className="hint mono">{fn.entry}</span>
                </Command.Item>
              ))}
            </Command.Group>
          )}
          {session.programId && (
            <Command.Group heading="Views">
              <Command.Item onSelect={() => run(() => session.setTab('overview'))}>
                <Gauge size={15} /> Overview <span className="hint">Alt 1</span>
              </Command.Item>
              <Command.Item onSelect={() => run(() => session.setTab('decompile'))}>
                <Braces size={15} /> Code <span className="hint">Alt 2</span>
              </Command.Item>
              <Command.Item onSelect={() => run(() => session.setTab('disassembly'))}>
                <Binary size={15} /> Assembly <span className="hint">Alt 3</span>
              </Command.Item>
              <Command.Item onSelect={() => run(() => session.setTab('hex'))}>
                <Hash size={15} /> Bytes <span className="hint">Alt 4</span>
              </Command.Item>
            </Command.Group>
          )}
          <Command.Group heading="Actions">
            <Command.Item onSelect={() => run(onOpenProgram)}>
              <FileCode2 size={15} /> Open a program…
            </Command.Item>
            <Command.Item onSelect={() => run(onOpenProject)}>
              <FolderOpen size={15} /> Open a project…
            </Command.Item>
            <Command.Item onSelect={() => run(onHome)}>
              <House size={15} /> Go home
            </Command.Item>
            <Command.Item onSelect={() => run(onSettings)}>
              <Moon size={15} /> Themes and layout <span className="hint">Ctrl ,</span>
            </Command.Item>
            <Command.Item onSelect={() => run(onAgent)}>
              <Bot size={15} /> Connect an agent
            </Command.Item>
          </Command.Group>
        </Command.List>
      </Command>
    </div>
  );
}

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import { useState } from 'react';
import { ArrowRight, Check } from 'lucide-react';
import { LayoutPreview, ThemePreview } from '../components/Previews';
import { LAYOUT_PRESETS } from '../layout/layouts';
import { usePrefs } from '../state/prefs';

const FEATURED = ['opcodex-graphite', 'opcodex-paper', 'github-dark-dimmed', 'vscode-dark-modern',
  'catppuccin-mocha', 'nord', 'tokyo-night', 'solarized-light'];

export function Welcome() {
  const prefs = usePrefs();
  const [step, setStep] = useState<0 | 1>(0);
  const themes = FEATURED.map((id) => prefs.themes.find((t) => t.id === id)!).filter(Boolean);

  return (
    <div className="welcome">
      <div className="welcome-inner" key={step}>
        <div className="steps" aria-label={`Step ${step + 1} of 2`}>
          <i className={step === 0 ? 'on' : 'done'} />
          <i className={step === 1 ? 'on' : ''} />
        </div>
        {step === 0 ? (
          <>
            <h1 className="hero-title">Welcome to Opcodex</h1>
            <p className="hero-sub">Pick a look. You can change it, or make your own, any time in Settings.</p>
            <div className="choice-grid">
              {themes.map((t) => (
                <button
                  key={t.id}
                  type="button"
                  className="choice"
                  aria-pressed={prefs.theme.id === t.id}
                  onClick={() => prefs.setTheme(t.id)}
                >
                  <ThemePreview theme={t} />
                  <span className="choice-label">
                    {t.name}
                    {prefs.theme.id === t.id && <Check size={14} />}
                  </span>
                </button>
              ))}
            </div>
          </>
        ) : (
          <>
            <h1 className="hero-title">How do you like to work?</h1>
            <p className="hero-sub">Choose a starting layout. Panels can be resized, hidden or rearranged later.</p>
            <div className="choice-grid wide">
              {LAYOUT_PRESETS.map((l) => (
                <button
                  key={l.id}
                  type="button"
                  className="choice"
                  aria-pressed={prefs.layout.id === l.id}
                  onClick={() => prefs.setLayout(l)}
                >
                  <LayoutPreview layout={l} />
                  <span className="choice-label">
                    {l.name}
                    {prefs.layout.id === l.id && <Check size={14} />}
                  </span>
                  <span className="choice-text">{l.description}</span>
                </button>
              ))}
            </div>
          </>
        )}
        <div className="welcome-actions">
          {step === 1 && (
            <button type="button" className="button ghost" onClick={() => setStep(0)}>
              Back
            </button>
          )}
          <button
            type="button"
            className="button primary"
            onClick={() => (step === 0 ? setStep(1) : prefs.finishOnboarding())}
          >
            {step === 0 ? 'Next' : 'Start using Opcodex'} <ArrowRight size={15} />
          </button>
        </div>
      </div>
    </div>
  );
}

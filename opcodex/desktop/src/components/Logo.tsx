// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
// The Opcodex ribbon mark (opcodex/branding/mark-*-surface.svg), drawn inline so it follows
// the theme: light ribbons on dark themes, dark ribbons on light themes.

interface LogoProps {
  size?: number;
  /** Which background the mark sits on. */
  surface: 'dark' | 'light';
  title?: string;
}

export function Logo({ size = 28, surface, title = 'Opcodex' }: LogoProps) {
  const face = surface === 'dark' ? '#f3f3ec' : '#24282b';
  const shade = surface === 'dark' ? '#c4c8c6' : '#171b1e';
  const line = '#00cddd';
  return (
    <svg width={size} height={size} viewBox="40 30 432 452" role="img" aria-label={title} className="logo">
      <path d="M104 113 189 66 250 100 195 132v113l-91-61Z" fill={face} />
      <path d="m104 184 91-52v113Z" fill={shade} />
      <path d="m196 62 24-14h71l117 65v71l-91 61V135Z" fill={face} />
      <path d="m317 135 91 49-91 61Z" fill={shade} />
      <path d="m258 108 52 32v31l-51-32Z" fill={face} />
      <path d="M408 194v150l-28 24-116-93Z" fill={face} />
      <path d="m408 194-30 89-76 57" fill="none" stroke={line} strokeWidth={5} strokeLinejoin="miter" />
      <path d="M104 263v81l24 24 66-41 63 57 34-33Z" fill={shade} />
      <path d="m194 327 13 11-88 67 39 59 99-80Z" fill={face} />
      <path d="m194 327 63 57-19 16-44-45-62 60-13-10 88-67Z" fill={shade} />
      <path d="m104 194 289 211-39 59-250-201Z" fill={face} />
      <path d="m104 263 250 201 24-37Z" fill={shade} />
      <path d="m105 263 273 164" fill="none" stroke={line} strokeWidth={5} />
    </svg>
  );
}

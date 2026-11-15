// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
import react from '@vitejs/plugin-react';
import { defineConfig } from 'vitest/config';
import { engineBridge } from './dev/engineBridge';

export default defineConfig({
  plugins: [react(), engineBridge()],
  clearScreen: false,
  server: { port: 1420, strictPort: true },
  build: { target: 'es2022', sourcemap: false },
  test: { environment: 'jsdom', globals: true },
});

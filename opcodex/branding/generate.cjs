// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
const fs = require('node:fs/promises');
const path = require('node:path');
const sharp = require('sharp');

async function generate() {
  const destination = path.resolve(__dirname, '../../Ghidra/Features/Base/src/main/resources/images/opcodex');
  await fs.mkdir(destination, { recursive: true });
  for (const surface of ['light', 'dark']) {
    const source = await fs.readFile(path.join(__dirname, `mark-${surface}-surface.svg`), 'utf8');
    for (const size of [16, 24, 32, 40, 48, 64, 128, 256]) {
      await sharp(Buffer.from(source)).resize(size, size).png()
        .toFile(path.join(destination, `app-${surface}-${size}.png`));
    }
    const ink = surface === 'dark' ? '#f3f3ec' : '#24282b';
    const inner = source.replace(/<svg[^>]*>/, '').replace(/<\/svg>\s*$/, '');
    const header = `<svg xmlns="http://www.w3.org/2000/svg" width="440" height="156" viewBox="0 0 440 156"><svg x="38" y="14" width="128" height="128" viewBox="0 0 512 512">${inner}</svg><text x="180" y="90" font-family="Arial, sans-serif" font-size="38" font-weight="600" fill="${ink}">Opcodex</text></svg>`;
    await sharp(Buffer.from(header)).png().toFile(path.join(destination, `splash-${surface}.png`));
    const framework = path.resolve(__dirname, '../../Ghidra/Framework/Docking/src/main/resources/images');
    await sharp(Buffer.from(source)).resize(256, 256).png()
      .toFile(path.join(framework, `opcodex-framework-${surface}-256.png`));
  }
  const toolSource = (await fs.readFile(path.join(__dirname, 'mark-light-surface.svg'), 'utf8'))
    .replace(/#[0-9a-f]{6}/gi, '#178697');
  await sharp(Buffer.from(toolSource)).resize(24, 24).png().toFile(path.join(destination, 'tool-24.png'));
}

generate().catch(error => { console.error(error); process.exitCode = 1; });

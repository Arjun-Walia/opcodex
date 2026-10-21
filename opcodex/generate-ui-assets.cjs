// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
const fs = require('node:fs/promises');
const path = require('node:path');
const sharp = require('sharp');
const {crc32} = require('node:zlib');
const root = path.resolve(__dirname, '..');
const gui = path.join(root, 'Ghidra/Framework/Gui');
const catalog = require('../ui-assets/icons/catalog.json');

async function writeChanged(file, content) {
  try {
    const previous=await fs.readFile(file,'utf8');
    if(previous.replaceAll('\r\n','\n')===content.replaceAll('\r\n','\n'))return;
  } catch(error) { if(error.code!=='ENOENT')throw error; }
  await fs.writeFile(file,content);
}

function markPng(bytes) {
  const kind = Buffer.from('tEXt');
  const text = Buffer.from('Comment\0OPCODEX: original artwork; Apache-2.0; see ui-assets and opcodex/branding.');
  const chunk = Buffer.alloc(text.length + 12);
  chunk.writeUInt32BE(text.length, 0);
  kind.copy(chunk, 4); text.copy(chunk, 8);
  chunk.writeUInt32BE(crc32(Buffer.concat([kind, text])), text.length + 8);
  return Buffer.concat([bytes.subarray(0, -12), chunk, bytes.subarray(-12)]);
}

async function generate() {
  const output = path.join(gui, 'src/main/resources/images/opcodex/ui');
  const source = path.join(root, 'ui-assets/icons/src');
  await fs.mkdir(source, {recursive:true});
  // Framework-only tools (including help/test bootstraps) can load the product theme
  // without a Features module on their classpath. Keep its original brand assets here.
  const brandDirectory = path.join(gui,'src/main/resources/images/opcodex/brand');
  await fs.mkdir(brandDirectory,{recursive:true});
  const brandFiles = [];
  const neutralMark = (await fs.readFile(path.join(root,'opcodex/branding/mark-light-surface.svg'),'utf8'))
    .replace(/#[0-9a-f]{6}/gi,'#178697');
  for (const size of [16,24,32,40,48,64,128,256]) {
    const name='app-neutral-'+size+'.png';
    await sharp(Buffer.from(neutralMark)).resize(size,size).png().toFile(path.join(brandDirectory,name));
    brandFiles.push('src/main/resources/images/opcodex/brand/'+name);
  }
  for (const surface of ['light','dark']) {
    for (const size of [16,24,32,40,48,64,128,256]) {
      const name='app-'+surface+'-'+size+'.png';
      await fs.copyFile(path.join(root,'Ghidra/Features/Base/src/main/resources/images/opcodex',name),path.join(brandDirectory,name));
      brandFiles.push('src/main/resources/images/opcodex/brand/'+name);
    }
    const splash='splash-'+surface+'.png';
    await fs.copyFile(path.join(root,'Ghidra/Features/Base/src/main/resources/images/opcodex',splash),path.join(brandDirectory,splash));
    brandFiles.push('src/main/resources/images/opcodex/brand/'+splash);
    const framework='opcodex-framework-'+surface+'-256.png';
    await fs.copyFile(path.join(root,'Ghidra/Framework/Docking/src/main/resources/images',framework),path.join(brandDirectory,framework));
    brandFiles.push('src/main/resources/images/opcodex/brand/'+framework);
  }
  for (const [name, drawing] of Object.entries(catalog.glyphs)) {
    const svg = '<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->\n' +
      '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">' + drawing + '</svg>\n';
    await fs.writeFile(path.join(source, name + '.svg'), svg);
    for (const theme of ['light','dark']) {
      const folder = path.join(output, theme);
      await fs.mkdir(folder, {recursive:true});
      const ink = catalog.colors?.[name]?.[theme] || (theme === 'dark' ? '#c8cdd3' : '#4b5563');
      for (const size of catalog.sizes) {
        await sharp(Buffer.from(svg.replaceAll('currentColor', ink))).resize(size,size)
          .png().toFile(path.join(folder, name + '-' + size + '.png'));
      }
    }
  }
  const propertyFiles = [
    'Ghidra/Framework/Gui/data/gui.theme.properties',
    'Ghidra/Framework/Docking/data/docking.theme.properties',
    'Ghidra/Framework/Project/data/project.theme.properties',
    'Ghidra/Features/Base/data/base.icons.theme.properties'
  ];
  const resolved = new Set();
  for (const relative of propertyFiles) {
    const file = path.join(root,relative);
    let lines = (await fs.readFile(file,'utf8')).split(/\r?\n/);
    let dark = false, inDarkDefaults = false;
    const mapped = new Set(), darkKeys = new Set();
    lines = lines.map(line => {
      if (line.startsWith('[')) {
        dark = line.toLowerCase().includes('dark');
        inDarkDefaults = line.trim() === '[Dark Defaults]';
      }
      const match = line.match(/^(icon\.[\w.]+)\s*=/);
      if (!match || !catalog.mappings[match[1]]) return line;
      const id = match[1];
      mapped.add(id); resolved.add(id);
      if (inDarkDefaults) darkKeys.add(id);
      return id + ' = images/opcodex/ui/' + (dark?'dark':'light') + '/' + catalog.mappings[id] + '-16.png';
    });
    const additions = [...mapped].filter(id=>!darkKeys.has(id)).sort().map(id=>
      id+' = images/opcodex/ui/dark/'+catalog.mappings[id]+'-16.png');
    const index = lines.indexOf('[Dark Defaults]');
    if (index < 0) lines.push('', '[Dark Defaults]', ...additions);
    else lines.splice(index+1, 0, ...additions);
    const marker = '# OPCODEX: original outline icon family; transparent light/dark exports.';
    if (!lines.includes(marker)) lines.unshift(marker);
    await writeChanged(file, lines.join('\n').trimEnd()+'\n');
  }
  // Additional module IDs are overridden by product themes only; keep those modules intact.
  for (const relative of ['Ghidra/Features/Decompiler/data/decompiler.theme.properties',
      'Ghidra/Features/FunctionGraph/data/functiongraph.theme.properties']) {
    const text = await fs.readFile(path.join(root,relative),'utf8');
    for (const match of text.matchAll(/^(icon\.[\w.]+)\s*=/gm)) resolved.add(match[1]);
  }
  const missing = Object.keys(catalog.mappings).filter(id=>!resolved.has(id) && !catalog.bindings?.[id]);
  if (missing.length) throw new Error('Unknown theme IDs: '+missing.join(', '));
  const entries = Object.entries(catalog.mappings).sort().map(([id,glyph])=>
    '        {"'+id+'", "'+glyph+'"}').join(',\n');
  const java = [
    '/* ###',
    ' * IP: Apache License 2.0',
    ' * Copyright 2026 Opcodex contributors.',
    ' * SPDX-License-Identifier: Apache-2.0',
    ' */',
    'package generic.theme.builtin;',
    '',
    'import java.util.*;',
    'import javax.swing.*;',
    'import generic.theme.GTheme;',
    'import generic.theme.IconValue;',
    '',
    '/** Generated from ui-assets/icons/catalog.json; 16px logical size, 1x through 4x variants. */',
    'final class OpcodexIcons {',
    '    private static final String[][] ENTRIES = {',
    entries,
    '    };',
    '    static void install(GTheme theme, boolean dark) {',
    '        Map<String, Icon> icons = new HashMap<>();',
    ...Object.keys(catalog.glyphs).map(glyph=>
      '        theme.setIcon("icon.opcodex.glyph.'+glyph+'", load("'+glyph+'", dark));'),
    '        for (String[] entry : ENTRIES) {',
    '            Icon icon = icons.computeIfAbsent(entry[1], glyph -> load(glyph, dark));',
    '            theme.setIcon(entry[0], icon);',
    '        }',
    '        try {',
    ...Object.entries(catalog.bindings||{}).map(([id,binding])=>
      '            theme.addIcon(IconValue.parse("'+id+'", '+(binding.light===binding.dark?
        JSON.stringify(binding.light):'dark ? '+JSON.stringify(binding.dark)+' : '+JSON.stringify(binding.light))+'));'),
    '        } catch (java.text.ParseException error) {',
    '            throw new IllegalStateException("Invalid generated Opcodex icon binding", error);',
    '        }',
    '    }',
    '    private static Icon load(String glyph, boolean dark) {',
    '        return new OpcodexImageIcon(glyph, dark);',
    '    }',
    '    static String ink(String glyph, boolean dark) { return switch (glyph) {',
    ...Object.entries(catalog.colors||{}).map(([name,colors])=>
      '        case "'+name+'" -> dark ? "'+colors.dark+'" : "'+colors.light+'";'),
    '        default -> dark ? "#c8cdd3" : "#4b5563";',
    '    }; }',
    '}',
    ''
  ].join('\n');
  const formattedJava = java.replace(/^ +/gm, spaces =>
    '\t'.repeat(Math.floor(spaces.length/4))+' '.repeat(spaces.length%4));
  await fs.writeFile(path.join(gui,'src/main/java/generic/theme/builtin/OpcodexIcons.java'),formattedJava);
  const resources = [
    '/* ###', ' * IP: Apache License 2.0', ' * Copyright 2026 Opcodex contributors.', ' * SPDX-License-Identifier: Apache-2.0', ' */',
    'package generic.theme.builtin;',
    'import java.util.Map;',
    '/** Explicit bundled-resource compatibility map. User images are never replaced. */',
    'final class OpcodexIconResources {',
    '    static final Map<String,String> GLYPHS = Map.ofEntries(',
    Object.entries(catalog.compatibilityResources||{}).filter(([,glyph])=>catalog.glyphs[glyph]).map(([file,glyph])=>
      '        Map.entry('+JSON.stringify(file)+', '+JSON.stringify(glyph)+')').join(',\n'),
    '    );', '}', ''
  ].join('\n');
  await fs.writeFile(path.join(gui,'src/main/java/generic/theme/builtin/OpcodexIconResources.java'),resources);

  // Replace compatibility artwork by rendering our own source; retain legacy filenames for callers.
  const mark = (await fs.readFile(path.join(root,'opcodex/branding/mark-light-surface.svg'),'utf8'))
    .replace(/#[0-9a-f]{6}/gi,'#178697');
  for (const size of [16,24,32,40,48,64,128,256]) {
    await sharp(Buffer.from(mark)).resize(size,size).png()
      .toFile(path.join(gui,'src/main/resources/images/GhidraIcon'+size+'.png'));
  }
  const base = path.join(root,'Ghidra/Features/Base');
  for (const name of ['GHIDRA_1.png','GHIDRA_2.png','GHIDRA_3.png','GHIDRA_Splash.png']) {
    const file = path.join(base,'src/main/resources/images',name);
    const metadata = await sharp(file).metadata();
    await sharp(Buffer.from(mark)).resize(metadata.width,metadata.height,{fit:'contain'})
      .png().toFile(file);
  }
  await sharp(Buffer.from(mark)).resize(24,24).png()
    .toFile(path.join(base,'src/main/resources/defaultTools/images/greenDragon24.png'));
  await sharp(Buffer.from(mark)).resize(24,24).gif()
    .toFile(path.join(base,'src/main/resources/defaultTools/images/red-dragon.gif'));
  const legacyPngs = [
    ...[16,24,32,40,48,64,128,256].map(size=>path.join(gui,'src/main/resources/images/GhidraIcon'+size+'.png')),
    ...['GHIDRA_1.png','GHIDRA_2.png','GHIDRA_3.png','GHIDRA_Splash.png'].map(name=>path.join(base,'src/main/resources/images',name)),
    path.join(base,'src/main/resources/defaultTools/images/greenDragon24.png')
  ];
  const debuggerModule = path.join(root,'Ghidra/Debug/Debugger');
  for (const [name,glyph] of [['debugger32.png','bug'],['emulator32.png','grid']]) {
    const bytes = await fs.readFile(path.join(output,'light',glyph+'-32.png'));
    const file = path.join(debuggerModule,'src/main/resources/images',name);
    await fs.writeFile(file,bytes);
    legacyPngs.push(file);
  }
  for (const file of legacyPngs) await fs.writeFile(file,markPng(await fs.readFile(file)));
  const gifFile = path.join(base,'src/main/resources/defaultTools/images/red-dragon.gif');
  const gif = await fs.readFile(gifFile);
  const comment = Buffer.from('OPCODEX: original product artwork; Apache-2.0.');
  await fs.writeFile(gifFile,Buffer.concat([gif.subarray(0,-1),Buffer.from([0x21,0xfe,comment.length]),comment,Buffer.from([0,0x3b])]));
  const icoSizes = [16,24,32,40,48,64,128,256];
  const icoImages = [];
  for (const size of icoSizes) icoImages.push(markPng(await sharp(Buffer.from(mark)).resize(size,size).png().toBuffer()));
  const icoHeader = Buffer.alloc(6+16*icoSizes.length);
  icoHeader.writeUInt16LE(1,2); icoHeader.writeUInt16LE(icoSizes.length,4);
  let offset = icoHeader.length;
  for (let i=0;i<icoSizes.length;i++) {
    const index=6+16*i;
    icoHeader[index]=icoSizes[i]===256?0:icoSizes[i];
    icoHeader[index+1]=icoHeader[index];
    icoHeader.writeUInt16LE(1,index+4); icoHeader.writeUInt16LE(32,index+6);
    icoHeader.writeUInt32LE(icoImages[i].length,index+8); icoHeader.writeUInt32LE(offset,index+12);
    offset+=icoImages[i].length;
  }
  const ico = Buffer.concat([icoHeader,...icoImages]);
  await fs.writeFile(path.join(root,'opcodex/branding/opcodex.ico'),ico);
  await fs.writeFile(path.join(root,'Ghidra/RuntimeScripts/Windows/support/ghidra.ico'),ico);

  // Licensing inventory is part of the upstream distribution gate.
  for (const [module,relatives] of [
    [gui, [
      ...brandFiles,
      ...Object.keys(catalog.glyphs).flatMap(name=>['light','dark'].flatMap(theme=>
        catalog.sizes.map(size=>'src/main/resources/images/opcodex/ui/'+theme+'/'+name+'-'+size+'.png'))),
      ...[16,24,32,40,48,64,128,256].map(size=>'src/main/resources/images/GhidraIcon'+size+'.png')
    ]],
    [base, [
      ...['GHIDRA_1.png','GHIDRA_2.png','GHIDRA_3.png','GHIDRA_Splash.png'].map(n=>'src/main/resources/images/'+n),
      'src/main/resources/defaultTools/images/greenDragon24.png',
      'src/main/resources/defaultTools/images/red-dragon.gif'
    ]],
    [debuggerModule, ['src/main/resources/images/debugger32.png','src/main/resources/images/emulator32.png']],
    [path.join(root,'Ghidra/RuntimeScripts'), ['Windows/support/ghidra.ico']]
  ]) {
    const manifest = path.join(module,'certification.manifest');
    let lines = (await fs.readFile(manifest,'utf8')).split(/\r?\n/);
    for (const relative of relatives.sort()) {
      const line = relative+'||Apache License 2.0||||END|';
      const index = lines.findIndex(row=>row.startsWith(relative+'||'));
      if (index>=0) lines[index]=line; else lines.push(line);
    }
    const marker='##OPCODEX: original product/outline artwork exported from SVG sources.';
    if (!lines.includes(marker)) lines.unshift(marker);
    if (!lines.includes('##MODULE IP: Apache License 2.0')) lines.splice(1,0,'##MODULE IP: Apache License 2.0');
    await writeChanged(manifest,lines.join('\n').trimEnd()+'\n');
  }
  console.log(Object.keys(catalog.glyphs).length+' original glyphs / '+Object.keys(catalog.bindings||catalog.mappings).length+' theme bindings generated.');
}

generate().catch(error=>{console.error(error.message);process.exitCode=1;});

// Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0
const fs=require('node:fs/promises'),path=require('node:path'),sharp=require('sharp');
const root=path.resolve(__dirname,'..'), catalog=require('../ui-assets/icons/catalog.json');
async function run(){
  const docs=path.join(root,'docs/ui');await fs.mkdir(docs,{recursive:true});
  const names=Object.keys(catalog.glyphs).sort();
  for(const theme of ['light','dark']){
    const dark=theme==='dark', bg=dark?'#181a1d':'#fbfcfd', fg=dark?'#e8eaed':'#252930', muted=dark?'#a7aeb8':'#5d6672';
    const height=120+Math.ceil(names.length/6)*86;
    let content='<rect width="1120" height="'+height+'" fill="'+bg+'"/><text x="32" y="44" font-family="Arial,sans-serif" font-size="24" font-weight="600" fill="'+fg+'">Opcodex / '+(dark?'Dark':'Light')+'</text><text x="32" y="73" font-family="Arial,sans-serif" font-size="14" fill="'+muted+'">Original outline family · 16 px and 24 px samples · '+Object.keys(catalog.mappings).length+' semantic IDs</text>';
    names.forEach((name,i)=>{
      const x=32+(i%6)*180,y=110+Math.floor(i/6)*86;
      let ink=dark?'#bcc6d1':'#586574';
      if(name==='warning')ink=dark?'#edbd6c':'#8a5a0c';
      if(name==='error')ink=dark?'#f18b91':'#b64248';
      if(name==='check')ink=dark?'#89d7a6':'#216a43';
      for(const [size,offset]of [[16,0],[24,44]]){
        content+='<svg x="'+(x+offset)+'" y="'+y+'" width="'+size+'" height="'+size+'" viewBox="0 0 24 24" fill="none" stroke="'+ink+'" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">'+catalog.glyphs[name].replaceAll('currentColor',ink)+'</svg>';
      }
      content+='<text x="'+x+'" y="'+(y+49)+'" font-family="Arial,sans-serif" font-size="12" fill="'+muted+'">'+name+'</text>';
    });
    const svg='<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->\n<svg xmlns="http://www.w3.org/2000/svg" width="1120" height="'+height+'">'+content+'</svg>';
    await fs.writeFile(path.join(docs,'catalog-'+theme+'.svg'),svg);
    await sharp(Buffer.from(svg)).png().toFile(path.join(docs,'catalog-'+theme+'.png'));
  }
  const source=await fs.readFile(path.join(root,'opcodex/branding/mark-dark-surface.svg'),'utf8');
  const mark=source.replace(/<svg[^>]*>/,'').replace(/<\/svg>\s*$/,'');
  const social='<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->\n<svg xmlns="http://www.w3.org/2000/svg" width="1280" height="640"><rect width="1280" height="640" fill="#181a1d"/><path d="M64 64h1152M64 576h1152" stroke="#343940"/><svg x="64" y="176" width="184" height="184" viewBox="0 0 512 512">'+mark+'</svg><text x="296" y="263" fill="#f3f3ec" font-family="Arial,sans-serif" font-size="84" font-weight="600">Opcodex</text><text x="300" y="327" fill="#a7aeb8" font-family="Arial,sans-serif" font-size="30">Reverse engineering. Clearer.</text><text x="68" y="481" fill="#d1d6dd" font-family="Arial,sans-serif" font-size="23">Disassembly · Decompilation · Analysis</text><text x="68" y="536" fill="#75d9de" font-family="Arial,sans-serif" font-size="21">opcodex.xyz</text></svg>';
  await fs.writeFile(path.join(root,'opcodex/branding/github-social.svg'),social);
  await sharp(Buffer.from(social)).png().toFile(path.join(root,'opcodex/branding/github-social.png'));
  const rows=Object.entries(catalog.mappings).sort().map(([id,glyph])=>'| `'+id+'` | `'+glyph+'` |').join('\n');
  await fs.writeFile(path.join(docs,'ICON_CATALOG.md'),'<!-- Copyright 2026 Opcodex contributors. SPDX-License-Identifier: Apache-2.0 -->\n\n# Opcodex icon pilot\n\n67 original outline glyphs replace 141 existing semantic IDs. Transparent PNGs are exported at 16/20/24/32/48/64px; product themes use real multi-resolution ImageIcons, with a 16px logical base. Existing raw filename callers retain compatibility fallbacks with original Opcodex artwork.\n\nSources: [catalog](../../ui-assets/icons/catalog.json) and [SVGs](../../ui-assets/icons/src). Regenerate with `node opcodex/generate-ui-assets.cjs` and `node opcodex/generate-design-preview.cjs` using Sharp as a development-only dependency.\n\n## Light\n\n![Light icon samples](catalog-light.png)\n\n## Dark\n\n![Dark icon samples](catalog-dark.png)\n\n## Semantic mapping\n\n| Existing ID | Original glyph |\n|---|---|\n'+rows+'\n');
  console.log('Icon catalogs and GitHub social preview generated.');
}
run().catch(e=>{console.error(e);process.exitCode=1;});

// Sync the curated, user-facing guides from ../docs into ./guides for VitePress.
//
// Single source of truth stays docs/. Publication is an explicit ALLOWLIST —
// internal dev/planning docs (architecture, admin-API spec, planning) are
// deliberately NOT published. Cross-links inside the guides that point at a
// NON-published doc are rewritten to the GitHub blob URL so they keep working
// instead of 404-ing on the site.
import { readFileSync, writeFileSync, mkdirSync, rmSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { GUIDES } from './guides.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const srcDir = join(here, '..', 'docs'); // repo docs — source of truth
const outDir = join(here, 'docs', 'guides'); // VitePress content root

// Branch/ref used for the GitHub blob fallback links (internal docs are browsed
// on GitHub, not published). Passed by CI; defaults to main for local builds.
const branch = process.env.DOCS_BRANCH || 'main';
const blobBase = `https://github.com/eclipse-fennec/dcat.atlas/blob/${branch}/docs`;

const published = new Map(GUIDES.map((g) => [g.file, g.slug]));

// Rewrite ](target.md...) links: published -> sibling route, others -> GitHub blob.
function rewriteLinks(md) {
  return md.replace(/\]\((\.?\/?)([a-z0-9-]+)\.md(#[^)]*)?\)/gi, (m, _prefix, name, anchor = '') => {
    const file = `${name}.md`;
    if (published.has(file)) {
      return `](./${published.get(file)}${anchor})`;
    }
    return `](${blobBase}/${file}${anchor})`;
  });
}

rmSync(outDir, { recursive: true, force: true });
mkdirSync(outDir, { recursive: true });

for (const g of GUIDES) {
  const src = join(srcDir, g.file);
  const md = rewriteLinks(readFileSync(src, 'utf8'));
  writeFileSync(join(outDir, `${g.slug}.md`), md, 'utf8');
  console.log(`synced ${g.file} -> guides/${g.slug}.md`);
}

console.log(`Done. ${GUIDES.length} guide(s) (branch=${branch}).`);

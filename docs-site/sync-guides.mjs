// Sync the curated, user-facing guides from ../docs into ./guides for VitePress.
//
// Single source of truth stays docs/. Publication is an explicit ALLOWLIST —
// internal dev/planning docs (architecture, admin-API spec, planning) are
// deliberately NOT published. Cross-links inside the guides that point at a
// NON-published doc are rewritten to the GitHub blob URL so they keep working
// instead of 404-ing on the site.
import { existsSync, readFileSync, writeFileSync, mkdirSync, rmSync } from 'node:fs';
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

// A guide whose source is absent is skipped rather than fatal. docs/ is no longer
// in this repository - it moved to DataInMotion/xdp under docs/projects/<repo>/ in
// 603050f - and nothing here fetches it back, so every entry below currently has no
// source. Failing the build on that takes the whole site down, including the pages
// that do not come from docs/ at all; skipping publishes what exists and leaves the
// allowlist intact, so restoring a source is all that is needed to restore its guide.
const missing = [];

for (const g of GUIDES) {
  const src = join(srcDir, g.file);
  if (!existsSync(src)) {
    missing.push(g.file);
    console.warn(`SKIPPED ${g.file}: no such file under ${srcDir}`);
    continue;
  }
  const md = rewriteLinks(readFileSync(src, 'utf8'));
  writeFileSync(join(outDir, `${g.slug}.md`), md, 'utf8');
  console.log(`synced ${g.file} -> guides/${g.slug}.md`);
}

console.log(`Done. ${GUIDES.length - missing.length}/${GUIDES.length} guide(s) (branch=${branch}).`);

if (missing.length > 0) {
  console.warn(
    `\n${missing.length} guide source(s) missing, so those guides are NOT published: ` +
      `${missing.join(', ')}.\nThey are expected under ${srcDir}, which this repository no ` +
      `longer carries. Until their new home is wired up, links to them from index.md resolve ` +
      `to nothing.`,
  );
}


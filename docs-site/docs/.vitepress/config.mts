import { existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import { defineConfig } from 'vitepress'
import { GUIDES } from '../../guides.mjs'

// Per-project docs are served under a versioned sub-path, matching the org
// convention (https://eclipse-fennec.github.io/<repo>/<version>/). The snapshot
// branch publishes to /dcat.atlas/snapshot/; tagged releases / `latest` get
// added once the first release lands.
const version = process.env.DOCS_BRANCH || 'snapshot'
const base = `/dcat.atlas/${version}/`

// Canonical published origin. Links that point OUTSIDE the current docs base
// (other doc versions, external sites) must be full URLs — VitePress
// auto-prepends `base` to any root-absolute (`/…`) link, which would otherwise
// double the path (e.g. /dcat.atlas/snapshot/dcat.atlas/…). Links to pages
// WITHIN this version stay base-relative (e.g. `/guides/user-guide`).
const SITE = 'https://eclipse-fennec.github.io/dcat.atlas'

// Version selector. Only `snapshot` is deployed today; keep as data so adding
// `latest` and tagged versions later is a one-liner.
const versions = [{ text: 'snapshot', link: `${SITE}/snapshot/` }]

// Only guides the sync actually wrote. guides.mjs is the allowlist, not a promise
// that the source exists: docs/ moved out of this repository in 603050f, so an entry
// there can currently have nothing behind it. Advertising such a guide in the nav
// would link to a page that was never generated.
const guidesDir = join(dirname(fileURLToPath(import.meta.url)), '..', 'guides')
const guideItems = GUIDES.filter((g) => existsSync(join(guidesDir, `${g.slug}.md`))).map((g) => ({
  text: g.title,
  link: `/guides/${g.slug}`,
}))

// Single topic → render the guide as a direct nav link. A second entry in
// guides.mjs automatically switches this back to a "Guides" dropdown. None at all →
// no entry, rather than an empty dropdown.
const guidesNav =
  guideItems.length === 1 ? guideItems[0] : { text: 'Guides', items: guideItems }

export default defineConfig({
  title: 'Fennec DCAT.Atlas',
  description:
    'DCAT-AP 3 open-data portal for EMF — Apache Jena persistence with JSON-LD, Turtle and N3 output, and an OSGi + REST admin API.',
  lang: 'en-US',
  base,
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,

  markdown: {
    // Shiki has no grammar for Turtle / N3; render them as plain text rather than
    // mangling under an unrelated grammar. JSON-LD reads fine as JSON.
    languageAlias: { turtle: 'text', ttl: 'text', n3: 'text', jsonld: 'json' },
  },

  head: [
    ['link', { rel: 'icon', type: 'image/png', href: `${base}fennec-logo.png` }],
    ['meta', { name: 'theme-color', content: '#c0631c' }],
    ['meta', { property: 'og:type', content: 'website' }],
    ['meta', { property: 'og:title', content: 'Fennec DCAT.Atlas' }],
    [
      'meta',
      {
        property: 'og:description',
        content:
          'DCAT-AP 3 open-data portal for EMF — Apache Jena persistence with JSON-LD, Turtle and N3 output.',
      },
    ],
  ],

  themeConfig: {
    logo: '/fennec-logo.png',
    siteTitle: 'Fennec DCAT.Atlas',

    nav: [
      { text: 'Home', link: '/' },
      ...(guideItems.length > 0 ? [guidesNav] : []),
      { text: `version: ${version}`, items: versions },
    ],

    sidebar: {
      '/guides/': [{ text: 'Documentation', items: guideItems }],
    },

    socialLinks: [{ icon: 'github', link: 'https://github.com/eclipse-fennec/dcat.atlas' }],

    search: { provider: 'local' },

    editLink: {
      pattern: 'https://github.com/eclipse-fennec/dcat.atlas/edit/main/docs/:path',
      text: 'Edit this page on GitHub',
    },

    footer: {
      message:
        'Released under the EPL-2.0 License. Eclipse Fennec is part of the Eclipse Foundation.',
      copyright: 'Copyright © Eclipse Foundation and contributors',
    },
  },
})

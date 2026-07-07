// The published, user-facing guides (allowlist). Shared by the sync script and
// the VitePress config so the set and its order are defined exactly once.
//   file  — source markdown in ../docs
//   slug  — route name under /guides/
//   title — sidebar / nav label
//
// DCAT.Atlas has a single published topic — one entry here. The nav renders it
// as a direct link (not a dropdown); adding a second entry later restores the
// dropdown automatically (see .vitepress/config.mts).
export const GUIDES = [
  { file: 'opendata-portal-user-guide.md', slug: 'user-guide', title: 'User Guide' },
];

// The published, user-facing guides (allowlist). Shared by the sync script and
// the VitePress config so the set and its order are defined exactly once.
//   file  — source markdown in ../docs
//   slug  — route name under /guides/
//   title — sidebar / nav label
//
// Two published topics: the portal, and the client that talks to it. With more
// than one entry the nav renders a "Guides" dropdown rather than a direct link
// (see .vitepress/config.mts). Order here is the order in the nav and sidebar.
export const GUIDES = [
  { file: 'opendata-portal-user-guide.md', slug: 'user-guide', title: 'User Guide' },
  { file: 'client-guide.md', slug: 'client-guide', title: 'Client Guide' },
];

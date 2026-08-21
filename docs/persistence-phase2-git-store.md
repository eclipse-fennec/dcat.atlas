# Phase 2 — the git-backed store

**Status:** design agreed 2026-08-19, not yet implemented.

This document is the implementation plan for Phase 2 of
[`persistence-and-sparql-implementation-plan.md`](persistence-and-sparql-implementation-plan.md).
It **supersedes §3** of that document, which is left as written: §3 was a survey done on
2026-08-10, and keeping it intact preserves the reasoning that led here. Where the two
disagree, this document is current, and each disagreement is called out below under
[Departures from §3](#departures-from-3).

Phases 1 and 3 in that plan are unaffected. The constraints G1–G5 still hold; in particular
**G1** — files are authoritative, the graph is a rebuildable projection — is what makes this
change safe to make incrementally.

---

## 1. Goal

The same resources, stored as blobs in a git repository instead of files in a directory,
with every write landing as a commit. One commit per API operation, an audit trail for free,
and a storage model that a read-only replica can consume unchanged in Phase 3.

**Non-goals.** Any change to the REST surface, to the identity scheme, to validation, or to
the SPARQL projection. Multi-writer. Branch-per-stage (draft vs published) — the git model
makes it possible, but it is not this phase.

---

## 2. What changed since §3 was written

Two things §3 recorded as blockers are resolved, both by work now in
`/opt/git/fennec-emf.util`:

- **`org.eclipse.fennec.jgit` has a write path.** `GitService.commit(CommitRequest)`,
  `writeFile`, `deleteFile`, `push`, with per-commit author override and a `pushOnCommit`
  default. `GitCommitWriter` assembles an in-core `DirCache` from the branch head's tree,
  applies the changes, inserts the tree and commit, and moves the ref with
  `setExpectedOldObjectId` — so a concurrent update raises `GitConflictException` instead of
  being clobbered. §3's "write support does not exist anywhere in the ecosystem" is no
  longer true.
- **The webhook bundles were extracted to a neutral BSN** —
  `org.eclipse.fennec.git.webhook.{model,rest}` plus the GitHub and GitLab payload models.
  That is option (b) of §4's "Reuse decision — webhook handling", so that decision is made.
  It matters to Phase 3, not to this phase.

---

## 3. Decisions

### D1 · Read and write through the object database, not a working tree

**§3 said the opposite** and it is the one decision that had to be revisited.

`GitCommitWriter` writes straight into the object database: for a local repository "the
branch moves but the working tree and index are left untouched", and for a remote the
repository is an `InMemoryRepository` with no working tree at all. So §3's plan — keep
`DcatHelper`'s `java.nio.file` I/O and add `add` + `commit` after each write — would leave
the files on disk and the branch permanently disagreeing.

The store therefore reads through `GitService` too. This is more work now and it pays for
itself twice: it is the same storage model Phase 3's read replicas need, and it removes the
`ReadWriteMany` volume from the deployment story.

**Consequence to accept:** the store's reads are no longer `Files.readAllBytes`. They are
blob reads out of a repository that, for a remote, lives in the heap. See
[§7 Failure semantics](#7-failure-semantics) and upstream issue **EU-7**.

### D2 · Identity and location are decoupled; a `URIHandler` is the seam

Today `StoreResourceSets.create` welds them: it puts
`http://dcat.atlas/datasets/` → `file:/<root>/datasets/` into the `URIConverter` URI map, so
the IRI segment *is* the directory name.

Phase 2 replaces the URI map with a **`URIHandler`** installed at the front of the
converter's handler list, claiming `DcatIds.LOGICAL_BASE`. Cross-resource references keep
working exactly as they do now — this is the ecosystem's established integration point
(`model.atlas/…management.git/GitURIHandler` does the same, and the Mongo/JDBC/JPA handlers
in `org.gecko.emf.persistence` are the same shape for other backends). **No
`ResourceFactory` or `Resource` subclass is involved**; `RootFragmentXmiResource` and
`AbsoluteUriHandler` in `StoreResourceSets` stay exactly as they are.

> **Deliberate divergence from model.atlas.** Its URI is `git://{commitId}/{path}` — the
> commit SHA is the URI *host*. DCAT.Atlas must not adopt that. Stored `href`s are written
> into the files themselves, and `StoreResourceSets` goes out of its way to keep those
> identities deployment-independent; a commit id in the URI would be frozen into every
> stored link and re-frozen on every write. The handler claims the existing
> `http://dcat.atlas/` base and reads at branch tip. Commit-pinned reads stay available
> where they are actually wanted — Phase 3 replicas, SPARQL snapshots — just not as the
> storage identity.

### D3 · A configurable base path; fixed collection folders below it

```
<repo>/<basePath>/catalogs/<id>.xmi
<repo>/<basePath>/datasets/<id>.xmi
<repo>/<basePath>/data-services/<id>.xmi
<repo>/<basePath>/dataset-series/<id>.xmi
```

`basePath` is configuration (default `dcat/`, may be empty for repository root). The four
collection folder names stay fixed.

**Why not fully configurable.** Each of the ten store components `@Designate`s `StoreConfig`
separately, but `References.referrersTo` scans *every* collection to find dangling links —
so any one store needs the whole collection→folder map, not just its own. Ten copies of that
map is precisely the drift `StoreConfig`'s existing javadoc argues against when it explains
why there is one root rather than a directory per service. A full map would need a new
single-instance DS service to hold it; a prefix needs nothing and covers the actual
requirement, which is sharing a repository with other content.

Should per-collection renaming ever be wanted, the migration is one `CommitRequest` that
puts every blob at its new path and deletes the old — atomic, and visible in the history as
a rename.

**Validation:** reject a `basePath` that is absolute, contains `..`, or does not normalise
to a clean relative path. Collection folders cannot nest (`datasets` vs `dataset-series` are
distinct at a segment boundary, which is what `PathFilter.create` compares), so no further
check is needed while the names are fixed.

### D4 · Stored blobs get a `.xmi` extension

Store files currently have **no extension**, deliberately: `StoreResourceSets.resourceUri`
explains that EMF derives cross-resource hrefs from the target's resource URI, so an `.xmi`
would end up inside a stored — and, rebased, served — identity.

D2 removes that constraint. The URI stays `http://dcat.atlas/datasets/air`; only the repo
path gains the extension. A git repository is browsed and diffed by humans, so this is worth
taking. The javadoc explaining the old constraint should be updated rather than deleted —
the reasoning is still correct, it simply no longer applies.

### D5 · One commit per API operation, not per resource write

**§3's P2-2 said "start with per-mutation".** Per *resource* mutation is wrong:
`deleteCatalog(cascade)` runs `References.detach` and then `delete`, and the membership
`add*` endpoints create-then-link. Those are several resource writes that must land together
or the store has an externally visible half-state — and with the SPARQL projection reading
it, a visible one.

`CommitRequest` is built for exactly this. `DcatHelper.Store` becomes a unit of work:
`put`/`delete` buffer changes, and the session flushes one commit when the operation
completes. The 28 `store()` call sites in `…impl` become explicit, closed sessions.

**A session that buffers no changes must not commit at all** —
`CommitRequest.Builder.build()` rejects an empty change list. Belt and braces: since EU-5,
`commit()` also skips a commit whose tree equals its parent's, so an operation that buffers
a write which changes nothing still leaves no trace. This also covers the idempotent no-op `add`, which
today correctly leaves the ETag unchanged.

### D6 · The ETag is the git blob id

**§3's P2-3 proposed the commit SHA.** That would break conditional GETs: the ETag is
per-resource (`DcatHelper.etag(root, collection, id)`, consumed by `ConditionalRequests` and
`DcatConditionalFilter` on every GET and every `If-Match`), and a commit SHA changes for
*every* resource whenever *any* resource is written. Every cached representation in the
estate would be invalidated by an unrelated write.

The git blob id is a content hash of exactly that resource, already computed by git, and
changes iff the stored bytes change — which is the property `DcatHelper.etag`'s javadoc
already states as the requirement. It also preserves the existing behaviour that two
deployments rendering different public IRIs agree on the ETag.

A Distribution's ETag is its Dataset's blob id, exactly as
`DistributionReadOnlyServiceImpl.etag` already returns its Dataset's file digest today.

⚠️ **`GitService` does not expose blob ids** — `TreeResult` carries paths and the commit id
only. **Resolved upstream 2026-08-19:** `GitService.blobId(commitId, path)` returns it
directly, and `TreeResult.getEntries()` carries it for a whole listing in one tree walk, so
`list` and the ETags it implies cost one walk rather than one call per resource.

### D7 · Both local and remote repositories, local bare by default

`GitServiceImpl.isRemote()` already branches on this, so it is one config value. The
semantics differ in ways the store must respect:

| | local on-disk | remote |
|---|---|---|
| repository | `FileRepositoryBuilder`, durable | `InMemoryRepository`, rebuilt by fetch on activation |
| a commit is durable | immediately | **only after push** |
| working tree | left untouched by commits — use a **bare** repo | none |
| `push()` | no-op | required |
| conflict surfaces as | `RefUpdate` CAS → `GitConflictException` | push rejection → `GitConflictException` |
| history growth | `git gc` works normally | grows in the heap (**EU-7**) |

**Default: a local bare repository.** Durability with no network dependency on the write
path. Remote is supported and is what unlocks Phase 3, where it pays off properly — a read
replica then needs no volume at all.

**For a remote, `pushOnCommit` must be true**, otherwise a write returns 201 and is lost on
restart. That puts the remote's availability on the write path; see §7.

⚠️ **A local repository must be bare.** Commits go straight to the object database and leave
the working tree and index untouched, so a non-bare local repo will show its entire tree as
deleted in `git status`. Configure a bare repo; the docker volume becomes one.

### D8 · Single writer, unchanged

§3's P2-4 stands and is now load-bearing rather than advisory. Two runtimes committing to
one repository is out of scope, and §4's non-goal (active/active writers) is unchanged. In
this phase that means: one `GitService` instance, bound by all ten store components.

---

## 4. The seam

Four files own essentially all the filesystem coupling. Everything else in `…impl` goes
through them.

```
AbstractEntityStore ──── store() ────► DcatHelper.Store   (unit of work; D5)
                                              │
                                              ├─ StoreResourceSets.create ──► URIConverter
                                              │                                  │
                                              │                          DcatGitUriHandler (D2)
                                              │                                  │
                                              └─ StoreLayout.repoPath ──────► GitService
                                                        (D3, D4)
```

| file | today | after |
|---|---|---|
| `StoreResourceSets` | URI map → `file:` URIs | installs `DcatGitUriHandler`; rest unchanged |
| `StoreLayout` | `directory(root, c)`, `file(root, c, id)` | `repoPath(collection, id)` → `<basePath>/<collection>/<id>.xmi` |
| `DcatHelper.Store` | `Files.*` | buffers a `CommitRequest`; reads via `GitService` |
| `StoreHealth` | directory readable/writable | repository reachable, branch resolvable |

---

## 5. Work items

### W1 · Build wiring — ✅ done 2026-08-19

Adding **`fennecUtil`** to `-library:` in `cnf/ext/fennec.bnd`, plus
`org.eclipse.fennec.util:org.eclipse.fennec.util.workspace.library:1.0.0-SNAPSHOT` in
`cnf/ext/libraries.maven`, brings the whole JGit stack in one entry — the library's
`fennecUtil.maven` already declares `org.eclipse.jgit`, `org.eclipse.jgit.ssh.apache`,
`sshd-osgi`, `sshd-sftp`, `JavaEWAH`, `bcprov` **and** `org.eclipse.fennec.jgit` itself.

> An earlier attempt pinned those eight coordinates directly in `cnf/ext/central.mvn`. That
> was wrong and has been reverted: it duplicates what the library already declares, and two
> sources for the same bundles is exactly the version drift the library exists to prevent.
> `libraries.maven` is also the right home for a `-SNAPSHOT`, which is what the `Libraries`
> repo (DIM Nexus + Central snapshots) is configured for.

`org.eclipse.fennec.jgit;version=latest` is on `…impl`'s `-buildpath`, and
`:…impl:compileJava` succeeds.

⚠️ **Two bundles named `org.eclipse.fennec.jgit` are now on the path**, and which one wins
matters:

| source | version | has EU-1…EU-6 |
|---|---|---|
| `cnf/local` (hand-placed, §9) | `1.0.0.202608190642-SNAPSHOT`, exports `…jgit.api` **1.1.0** | **yes** |
| Maven, via `fennecUtil` | `1.0.0.202608181239-SNAPSHOT`, exports `…jgit.api` 1.0.0 | no |

`version=latest` resolves to the higher version, so the Local jar wins — verified by
compiling a throwaway probe against `exists`, `blobId`, `FileEntry` and `getRemoteHead`,
which the published snapshot does not have. It is a two-hour margin, not a guarantee: if the
Local jar is ever removed before the published snapshot catches up, the build silently falls
back to the old bundle. The failure would at least be loud (missing symbols), not silent
misbehaviour — but W11 is what actually removes the hazard.

**EU-9 — resolved upstream 2026-08-19.** `GitServiceImpl` used to hold an
`SshdSessionFactory` field, so the class could not be *loaded* without
`org.eclipse.jgit.ssh.apache`; the whole unit suite failed with `NoClassDefFoundError`
against a bare local repository that never opens a connection. The field is now typed
`org.eclipse.jgit.transport.SshSessionFactory` (jgit core) with the sshd types confined to
`SshdSessionFactoryProvider`. The unit tests deliberately do **not** carry the SSH bundle,
which is what keeps that fixed. `-runrequires` still asks for it, now as a deliberate choice
so switching `GIT_REPO` to an SSH remote needs no re-resolve.

⚠️ **Only one source may provide a `-library`.** The `fennecUtil` library is declared by
`org.eclipse.fennec.util.workspace.library`, and having that bundle in **both** `cnf/local`
and `cnf/ext/libraries.maven` breaks the build outright:

```
cnf/ext/libraries.bnd:8: error: Error loading -library for fennecUtil:
  java.lang.NullPointerException: Cannot invoke "Version.compareTo(Version)" because "o.version" is null
```

These libraries declare `Provide-Capability: bnd.library;bnd.library=fennecUtil;path=template`
with **no version attribute** — true of `fennecCodec` and the others too — so bnd cannot order
two candidates and NPEs. One candidate is fine; two is not. While the local copy is the
authoritative one, its Maven coordinate must be **absent** from `libraries.maven` (and
restored as part of W11).

**Still outstanding in W1:** `base.bndrun`'s `-runrequires`, and a **re-resolve in
bndtools** — `resolve.*` from gradle does not update `-runbundles`. Update `DEPENDENCIES`
and check dash-licenses (JGit is EDL, Apache MINA sshd is Apache-2.0, BouncyCastle is MIT —
all compatible, but the file has to say so).

### W2 · `DcatGitUriHandler` — ✅ done 2026-08-19

New class in `…impl.helper`, extending `URIHandlerImpl`.

- `canHandle(uri)` — the URI starts with `DcatIds.LOGICAL_BASE`.
- `createInputStream` — `StoreLayout.repoPath(...)` then `gitService.readLatestFile(path)`,
  translating `GitFileNotFoundException` to `FileNotFoundException` so EMF's own
  "resource does not exist" handling is unchanged.
- `createOutputStream` — a `ByteArrayOutputStream` that, on `close()`, adds a `PUT` to the
  session's `CommitRequest.Builder`. This is the shape the persistence backends already use
  (`org.gecko.emf.persistence.mongo/.../streams/*OutputStream` persists on `flush()`/`close()`);
  **no existing git handler does this — every one of them throws** (see upstream **EU-8**).
- `exists(uri, options)` — `gitService.exists(null, path)`, a tree lookup rather than a blob read.

Installed with `resourceSet.getURIConverter().getURIHandlers().add(0, handler)`, matching
`GitURIHandler` in model.atlas.

### W3 · `StoreLayout` and `StoreConfig` — ✅ done 2026-08-19

- `StoreLayout`: drop `directory(Path, String)` and `file(Path, String, String)`, add
  `repoPath(basePath, collection, id)`. The `DcatIds` re-exports and `requireSafeId` are
  untouched — the identity vocabulary does not change.
- `StoreConfig`: replace `root()` with `basePath()` (default `dcat`), keep
  `validateOnWrite()` unchanged, and add nothing else — the repository itself is
  `GitConfig`'s business, reached through a `gitService.target` reference.
- Validate `basePath` on activation (D3).

### W4 · `DcatHelper.Store` as a unit of work — ✅ done 2026-08-19

| method | change |
|---|---|
| `open` | takes a `GitService` and `basePath` instead of a `Path` |
| `get` | `Files.isRegularFile` guard → tree lookup via the handler's `exists` |
| `list` | `Files.list(dir)` → `gitService.getFiles(basePath + "/" + collection + "/")`, ids from the path tails |
| `put` | unchanged in its validation ordering; `save` now buffers |
| `delete` | existence check, then buffer a `DELETE`; still returns whether it existed |
| `prepare` | **deleted** — git has no empty directories |
| `etag` | instance method over `gitService.blobId(null, repoPath)` (D6) |
| *new* `commit(message)` | flush; no-op when nothing was buffered (D5) |

Commit messages name the operation and the resource (`Store dataset air`,
`Delete catalog gov and unlink its referrers`) — this is the audit trail, and it is cheap to
get right at the point where the operation is known.

> **Deviation: `Store` is not `AutoCloseable`.** The plan called for try-with-resources at
> the 28 call sites. There is nothing to release — no file handles, and the `ResourceSet` is
> garbage — so `close()` would have had no work to do, and the only candidate behaviour
> (flush on close) is wrong: a session must commit *nothing* when the operation throws part
> way through. `commit(message)` is the flush, an uncommitted session simply never writes,
> and the exception path needs no `finally` to be safe. The atomicity the plan wanted is
> intact; the ceremony is not.
>
> **Where the commit goes.** Membership operations commit inside the shared `connect(…)`
> helper rather than in each public method, because `connect` is the tail of both `addX` and
> `linkX` and the commit must precede `reproject(…)` — the projection re-reads the store, so
> invalidating before the commit would cache pre-commit state. `connect` therefore takes the
> commit message as a parameter, and the early return for an already-linked member became an
> `if` so that an `addX` whose membership was a no-op still commits the member it stored.

### W5 · Health check — ✅ done 2026-08-19

`StoreHealth` stops asking about directories. New checks: a `GitService` is bound; the
configured branch resolves; for a remote, the last fetch (and push, if `pushOnCommit`)
succeeded. Keep the existing CRITICAL-not-WARN judgement and the reasoning behind it —
`AbstractEntityStore.execute()` and `StoreHealthTest` follow.

### W6 · Failure mapping — ✅ done 2026-08-19

Translated at the store boundary into two new `…api` exceptions —
`StoreConflictException` (409) and `StoreUnavailableException` (503) — each with a mapper in
`…rest.filter` alongside the existing `ReferentialIntegrityException` one.

The translation happens in `Store.commit`, **not** in a mapper over the git exception,
because the REST adapter renders refusals and must not learn that the store is a git
repository. That is the boundary `ReferentialIntegrityException` already draws.

The split §7 wanted — 503 for "written locally, push failed" against 500 for "nothing was
written" — **is** in place since **EU-10** landed upstream (2026-08-19). `Store.commit`
catches, in order:

1. `GitConflictException` → `StoreConflictException` → **409**
2. `GitPushException` → `StoreUnavailableException` → **503**
3. plain `GitWriteException` → **not caught**

The third is deliberate. A failed object-database write is a server fault, not a refusal, so
it should surface as an unmapped **500** carrying its stack trace into the log. Wrapping it in
a mapped exception would render a tidy message and lose the diagnosis, and there is nothing a
client could do with the detail. Order matters: both of the first two extend
`GitWriteException`, and `GitConflictException` is the more specific of the pair.

### W7 · Configuration — ✅ done 2026-08-19

`GitConfig~dcatStore` factory configuration in `…config.local` (default
`/tmp/dcat-store.git`) and `…config.docker` (default `/opt/dcat/store.git`, the mounted
volume — now a **bare repository** rather than a directory of files). `STORE_FOLDER` is gone
from both; `STORE_BASE_PATH` replaces it and defaults to `dcat`.

The configuration sets `"id": "dcatStore"`, which DS propagates to the registered service, and
each store carries `"gitService.target": "(id=dcatStore)"` — the same pattern the HTTP
configuration already uses with `(id=dcatHttp)`.

⚠️ **The `@Reference` needed an explicit `name = "gitService"`.** DS names an unnamed
constructor reference by position, so the generated XML had `name="$001"` and the
configurable property would have been `$001.target` — unreadable, and it shifts the moment a
constructor parameter is added ahead of it. Verified in the generated component XML.

⚠️ `secrets.bndrun` already carries the SHACL shapes paths for local runs; SSH key material
belongs there too, not in `…config.local`.

### W8 · SPARQL reconcile gets its fingerprint — ✅ done 2026-08-19

P1-7 wanted a cheap store fingerprint and settled for a full re-read every interval. The
store's commit is exactly that fingerprint: unchanged ⇒ nothing to do.

Exposed as a new `…api` service, **`StoreRevision`** (one opaque token, equality only),
implemented in `…impl` over `GitService`. Not a `GitService` reference in the SPARQL bundle:
the projection has no business knowing the store is a git repository, the same boundary W6
draws for the REST layer. `DcatGraphServiceImpl` binds it **optionally** — without it the
poll behaves exactly as before, which keeps the optimisation from becoming a correctness
dependency.

Only the *poll* may skip. The startup build and `rebuild()` always do the work, because
`rebuild` is the repair path an operator reaches for and a repair that decided it had nothing
to do would be useless. The skip is safe because every way the projection can drift also
moves the store version — including the one drift P1-4 tolerates by design, a write that
committed but whose `invalidate` failed. `projectedRevision` is recorded only after a refresh
that completed, and cleared on failure, so a failed pass is always retried.

A write costs one full pass at the following tick and nothing after that: `invalidate` repairs
its own graph but does not claim the new store version, having no way to know the commit its
write produced. Five tests in `StoreRevisionReconcileTest` pin the behaviour, including the
three "cannot prove it is unchanged" cases — no revision service, an unreadable one, and a
store with no content, where two empty answers are two absences of evidence rather than
evidence of sameness.

### W9 · Migration — ✅ done 2026-08-19

`tools/migrate-file-store-to-git.sh <store-root> <target-repo> [base-path] [branch]`.

Copies every resource byte for byte from the old `<root>/<collection>/<id>` layout to
`<basePath>/<collection>/<id>.xmi` in a fresh repository, as one initial commit. The stored
identity lives *inside* the file and is untouched, so this is a move and not a rewrite —
nothing has to be re-validated afterwards.

Deliberate refusals, each verified:

- a target that already has commits on the branch — importing on top of real history would
  interleave a migration with edits and leave no clean point to roll back to;
- a file whose name is not a usable id (the `DcatIds.isSafeId` rule, mirrored with a pointer
  to the source of truth) — renaming it would change a resource's identity, which is a
  decision for a person. It aborts having imported **nothing**, rather than leaving a partial
  store whose catalogs point at datasets that were skipped;
- a store with no resources in it.

Verified end to end: migrated a store, started the portal against it, and the catalog served
`200` with `ETag` equal to `git rev-parse HEAD:dcat/catalogs/example.xmi`, with SPARQL
projecting it — so a migrated store is indistinguishable from one written in place.

### W10 · Tests — ✅ done 2026-08-19

**Unit.** The ten `@TempDir` classes run against a bare local repository per test through a
new `TestGitStore` fixture; `StoreHealthTest` was rewritten for git readiness;
`GitCommitBoundaryTest` (5) covers the D5 claim nothing else asserts, and
`StoreRevisionReconcileTest` (5) covers W8. **188 unit tests, 0 failures.**

**Integration (OSGi).** `…rest.tests` now runs against a git store: the config carries
`GitConfig~dcatStore` and `basePath`/`gitService.target` in place of `root`, and the
repository is created — and **deleted first** — before every run by the build. **159 tests,
0 skipped, 0 failures**, unchanged from the file-store baseline, which is the point: the
storage swap is invisible to the API. A run leaves ~320 commits behind, one per operation.

⚠️ **The shared-store hazard is gone.** The old suite shared `/tmp/rdf` with live runs and
with its own previous run, so a suite that left data behind only failed the *second* time.
Recreating the repository per run removes that class of failure at the source; verified by
running twice in a row, green both times.

⚠️ **Two task names, and they were not equivalent.** `resolve.test` writes its output to
`generated/test.bndrun` (this project sets `outputBndrun`), which `testOSGi` launches — but
`testrun.test` launches the **source** `test.bndrun`. So a resolve that added the jgit
bundles left `testrun.test` failing with `Unable to resolve …impl: missing requirement
org.eclipse.fennec.jgit.api`, which reads like a version conflict and is not one. The source
`-runbundles` has been brought back in step, and the repository-creation hook is attached to
**both** task names, so either entry point works. Both verified.

⚠️ **Watch for silently skipped tests.** When the store services cannot activate — an
unopenable repository, say — the suite reports `159 tests` and `159 skipped` while the task
fails for an unrelated-looking reason. Read the skipped count, not just failures.

### W11 · Return to a published snapshot

Replace the hand-placed Local jar with a `central.mvn` coordinate once emf.util publishes a
snapshot carrying EU-1…EU-6, and empty `cnf/local`. See §9 — this is the one item that must
not be skipped before a release branch.

---

## 6. Configuration reference

| key | component | default | notes |
|---|---|---|---|
| `repo` | `GitConfig~dcatStore` | — | path to a **bare** local repo, or a remote URL |
| `branch` | `GitConfig~dcatStore` | `main` | |
| `pushOnCommit` | `GitConfig~dcatStore` | `false` local / **`true` remote** | D7 |
| `authorName` / `authorEmail` | `GitConfig~dcatStore` | Fennec defaults | shows up in the audit trail |
| `privateKey`, `privateKeyPassphrase`, `knownHosts` | `GitConfig~dcatStore` | — | remote over SSH only (**EU-2**) |
| `username` / `password` | `GitConfig~dcatStore` | — | remote over https only |
| `basePath` | the ten store components | `dcat` | D3 |
| `validateOnWrite` | the ten store components | `true` | unchanged |

`STORE_FOLDER` disappears from both config bundles and from the docker documentation. The
volume becomes a bare repository rather than a directory of files.

---

## 7. Failure semantics

§3's P2-8 asked what the API returns when the write succeeds but the commit fails. With D1
there is no longer a separate file write — the commit **is** the write — which removes the
half-committed state the question was about. What remains, each distinguishable since EU-10:

| failure | cause | response |
|---|---|---|
| `GitConflictException` on `commit` | branch moved under the session | **409**, with the existing conflict rendering. Should not occur under D8; if it does, it is a misconfiguration, not a race to retry through. |
| `GitConflictException` on `push` | remote rejected as non-fast-forward | **409**. Recovery is `fetch()` → `getRemoteHead()` → `resetToRemote()` → re-apply → push, and it is the caller's decision — see §8. Not automatic under D8. |
| `GitPushException` | remote unreachable | **503** via `StoreUnavailableException`. The commit is already on the branch, so a later `push()` completes it; for a remote (in-memory) repository that is durable only until restart. The readiness check reports the divergence. |
| `GitWriteException` on `commit` | object database write failed | **500**, unmapped and logged with its stack trace. Nothing was written. |

Validation failures (both layers) are unchanged: they run before anything is buffered, so an
invalid entity never reaches a commit — the property `DcatHelper.Store.put` already
documents.

---

## 8. Upstream dependencies

**Status 2026-08-19: EU-1 … EU-6 are implemented** in `org.eclipse.fennec.jgit` and the
bundle is available to this workspace as
`cnf/local/org.eclipse.fennec.jgit/org.eclipse.fennec.jgit-1.0.0.jar`
(`1.0.0.202608190642-SNAPSHOT`, exported `…jgit.api` now at `1.1.0`). It is a hand-placed
build from an uncommitted working tree, not a published snapshot — see
[§9 Provenance](#9-provenance).

The API this plan codes against is therefore now concrete:

| need | API |
|---|---|
| existence without reading (EU-4) | `boolean exists(String commitId, String path)` |
| per-resource ETag (EU-3) | `Optional<String> blobId(String commitId, String path)` |
| listing with ETags in one walk (EU-3) | `TreeResult.getEntries()` → `List<FileEntry(path, blobId)>` |
| empty repository (EU-1) | `getFiles` returns an empty `TreeResult` with a `null` commit id; `exists` returns `false` |
| no-op operation (EU-5) | `commit()` skips a commit whose tree equals its parent's, unless `CommitRequest.isAllowEmpty()` |
| safe conflict recovery (EU-6) | `fetch()` is fast-forward-only; `getRemoteHead()` reads the remote's side; `resetToRemote()` is the explicit destructive step |

**EU-6 landed better than sketched, and it changes §7.** The sketch asked only that the
misleading "fetch and retry" advice be corrected. What was implemented instead makes
`fetch()` non-destructive — a branch carrying unpushed commits is not moved — and puts the
discard behind an explicit `resetToRemote()`. A bounded automatic retry after a rejected
push is therefore *possible* now: fetch, `resetToRemote()`, re-apply the operation, push.

It is still **not** in scope for this phase. Under D8 (single writer) a rejected push means
something else is writing to the branch, and silently re-applying our operation on top of an
unknown other writer's commit is not a recovery — it is the clobbering §4 of the original
plan lists as a non-goal. Return 409 and let an operator look. The API being safe means a
future multi-writer phase has a path; it does not make this phase want one.

**EU-9 and EU-10 also landed** (2026-08-19): the SSH stack is no longer needed to load the
service, and `GitPushException` separates a failed push from a failed commit — which is what
lets W6 render 503 and 500 for the two.

**EU-7 and EU-8 remain open**, both by choice:

- **EU-7** (an in-memory repository accumulates history in the heap) is why D7 defaults to a
  local bare repository. It bounds the remote deployment shape, and Phase 3 has to answer it.
- **EU-8** (no EMF write path over git anywhere) is what W2 builds. It stays here until the
  shape has been exercised against a real workload, then it is a candidate for extraction as
  `org.eclipse.fennec.jgit.emf`.

## 8a. Verified end to end (2026-08-19)

The runtime was exported and run against a bare `git init --bare /tmp/dcat-store.git`, with
`SHACL_ENFORCE=false` to separate storage behaviour from conformance. What it showed:

| checked | result |
|---|---|
| readiness on an **empty** repository | `OK` — `repository … has no commits yet on main; dcat/catalogs/ will be created by the first write` (the D7 first-boot case) |
| `POST /admin/catalogs` | `201`, `ETag: "8bc2ee82…"` |
| **the ETag is the blob id** | `git rev-parse HEAD:dcat/catalogs/example.xmi` → `8bc2ee82…`, identical (D6) |
| commit message and author | `Store catalog example`, `DCAT.Atlas <dcat-atlas@example.org>` — from the configuration |
| stored identity | `about="http://dcat.atlas/catalogs/example"` in the blob, while the response rendered `http://localhost:8085/…` (the D2 separation) |
| layout | `dcat/catalogs/example.xmi` — configured `basePath`, fixed collection folder, `.xmi` (D3, D4) |
| duplicate `POST` | `409` |
| stale `If-Match` | `412` |
| `POST /admin/catalogs/{id}/datasets` | `200`, and **one** commit touching *both* `catalogs/example.xmi` and `datasets/{new}.xmi` (D5) |
| membership form | `<dataset href="http://dcat.atlas/datasets/…#/"/>` — a reference, not a copy |
| repeating the link | `200`, commit count unchanged at 2 (D5 / EU-5) |
| `DELETE` | `204`, commit `Delete catalog example` with `D dcat/catalogs/example.xmi`; subsequent `GET` → `404` |
| readiness with content | `store readable: … at eca4015… on main, 1 resource(s) in dcat/catalogs/` |

⚠️ **Pre-existing gap found while testing, not caused by this work:** `cascade` is not
exposed over HTTP at all. Every `@DELETE` in `…rest` calls `deleteX(id, false)` with the flag
hard-coded, and there is no `@QueryParam`, so a delete of a referenced resource is always
`409` and the cascade path is unreachable from the API. The service-level cascade works and
is covered by unit tests (`cascadeUnlinksThenDeletes`, `aCascadeDeleteIsOneCommit`). It is an
API gap, not a storage one, and is drafted as an issue at the end of
[`DCAT-github-issues.md`](DCAT-github-issues.md) rather than fixed here.

⚠️ **Correction to an earlier note:** `resolve.*` *does* write `-runbundles`, but only for
the bndrun that declares them. `resolve.local` succeeded and changed nothing because
`-runbundles` lives in `base.bndrun`; `resolve.base` wrote all seven jgit-stack bundles in.
Only `bcprov` was needed — `bcpkix`/`bcutil` did not turn out to be required for this
configuration.

## 9. Provenance

⚠️ The jgit bundle in `cnf/local` was built from an **uncommitted working tree** in
`/opt/git/fennec-emf.util` and placed by hand, deliberately, so upstream can keep changing
while this is implemented. Two consequences:

- The build is **not reproducible from a clean checkout** — a fresh clone of dcat.atlas
  cannot resolve `org.eclipse.fennec.jgit`. Do not let this state reach a release branch.
- `cnf/local` was previously empty and documented as unused; it is now load-bearing.

**Exit criterion:** when emf.util publishes a snapshot carrying EU-1…EU-6 and EU-9, **delete
both Local jars, empty `cnf/local`, and restore
`org.eclipse.fennec.util:org.eclipse.fennec.util.workspace.library:1.0.0-SNAPSHOT` to
`cnf/ext/libraries.maven`** — it was removed because a `-library` may have only one provider
(see W1) — nothing else is needed, because `fennecUtil` already
declares the bundle and will then resolve it from Maven on its own. Until that happens the
Local jar is the only copy with the new API, and it wins only by being the higher version
(see W1).
Tracked as W11.

## Departures from §3

| §3 | here | why |
|---|---|---|
| Working tree, `DcatHelper` I/O unchanged, add+commit after each write | Object-database reads and writes (D1) | `GitCommitWriter` does not touch a working tree, and a remote repository has none |
| P2-2 "start with per-mutation" commits | One commit per API operation (D5) | Cascade delete and create-then-link are multi-resource operations with a visible half-state |
| P2-3 commit SHA as the ETag | Blob id (D6) | A commit SHA invalidates every cached representation on any write |
| P2-8 "file write succeeds but commit fails" | Does not arise (§7) | There is no separate file write any more |
| §3 findings: `org.gecko.jgit` is read-only, contribute writes upstream | Done, in `org.eclipse.fennec.jgit` | Superseded by the work in fennec-emf.util |

## Open questions

1. **Push failure on a remote: 503 or fail the write?** §7 proposes 503 with the commit
   durable locally. For a remote (in-memory) repository "durable locally" means "until
   restart", which may be too weak a promise to return 2xx-adjacent semantics for.
2. **Retention (§3 P2-5).** A commit per operation is a lot of commits over years. Local
   repositories can `git gc`; a remote's in-memory mirror cannot (**EU-7**). Decide whether
   history is pruned, and if so by what.
3. **Does the docker image ship `git`?** Nothing in the runtime needs it — JGit is pure Java
   — but an operator inspecting the volume will want it.

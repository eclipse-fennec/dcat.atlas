# Implementation plan — persistence and SPARQL

> **Status:** implementation plan, 2026-08-10. Agreed direction for **WP-DCAT-3**
> (persistence, issue #32), **WP-DCAT-5** (SPARQL) and decision **C1**.
> Internal document, not published to the docs site.
>
> Companion: [`sparql-and-fennec-persistence.md`](../sparql-and-fennec-persistence.md) — the
> feasibility findings this plan acts on. **This plan supersedes §6/§7 of that document:**
> the store of record stays file-based (later git-based) and Fennec Persistence is *not*
> adopted. Everything else in the findings doc still holds, in particular §4 (why SPARQL is
> not translatable into the persistence query IR) and §5.2 (the in-memory graph).

---

## 1. Direction in one paragraph

Files remain the source of truth. SPARQL is served by Apache Jena ARQ over an **in-memory
graph that is a disposable projection** of those files: built at startup, updated at every
write. No second database, and no SPARQL→query-IR translation anywhere. Later, the file store
moves into git (Phase 2), and later still the write and read roles become separately
deployable runtimes (Phase 3).

### Guiding constraints

| # | Constraint | Why |
|---|---|---|
| G1 | Files are authoritative; the graph is a rebuildable projection | Losing the graph must never mean losing data. Rebuild-from-store is the recovery path for every failure mode. |
| G2 | Graph maintenance happens at the **persistence boundary**, never in the REST layer | `CatalogAdminServiceImpl` etc. are `@Component(service = …AdminService.class)` OSGi services; REST is only one consumer. A hook in the REST resource leaves the graph stale for every direct OSGi caller. |
| G3 | Every graph update is idempotent | Replace-this-resource's-named-graph converges regardless of prior state, so retries, duplicate webhooks and reconcile polls are all harmless. |
| G4 | SPARQL scope is the *rooted* subset (findings §4.4) | e.g. "datasets whose publisher is X and whose distribution format is CSV". **Not enforced** — ARQ executes any SPARQL. This is a scope and performance expectation, not a guard. |
| G5 | No second database | See findings §5. If a TDB2 projection is ever added it must stay strictly derived, one-way. |

---

## 2. Phase 1 — in-memory Jena graph over the existing file store

> ## ✅ Complete (2026-08-17)
>
> All nine work items are implemented in `org.eclipse.fennec.dcat.atlas.sparql`, and every
> acceptance criterion below is covered by a test. 15 unit tests in that bundle plus
> `SparqlEndpointIntegrationTest` in the OSGi suite.
>
> **Four deviations from the plan as written, all deliberate:**
>
> 1. **P1-1** names `EObjectRDFModelBuilder.toModel(...)` as the dependency. That class was
>    deleted by the EMF-native rework; the bundle uses `EObjectToJena` instead.
> 2. **P1-2** says the graph name is the resource's `rdf:about`. It is the **public** IRI:
>    the projection runs `PublicView.render` before `EObjectToJena.toModel`, so graph names
>    match the IRIs the REST API serves and a graph name is also a URL a client can `GET`.
>    Projecting the logical base would have leaked an internal identity into query results.
> 3. **P1-5** says `CONSTRUCT`/`DESCRIBE` serialise through the existing message body writers.
>    They do not: those map *EMF objects* to RDF, whereas a `CONSTRUCT` already yields a Jena
>    `Model` and a SPARQL result set is not RDF at all. `RDFDataMgr` and `ResultSetFormatter`
>    cover every syntax directly.
> 4. **P1-7** suggests a cheap store fingerprint (file count + newest mtime). The reconcile
>    pass instead re-reads the stores and re-projects, which is not free — it is the same
>    idempotent refresh as startup and reindex, so there is no separate, less-tested repair
>    path. The interval (default 300s) is the knob; a fingerprint remains a possible
>    optimisation, not a correctness gap.
>
> **Not in Phase 1 and still true:** the projection is derived and never authoritative. A
> stale query result therefore always means a stale *file*, which is worth remembering when
> the store becomes git-backed in Phase 2.

**Goal:** a working SPARQL endpoint with no change to how data is stored.

**Non-goals:** git, any runtime split, any storage swap, query pushdown into a query IR.

### Work items

✅ **P1-1 · New bundle for the graph and SPARQL endpoint.**
Add `org.eclipse.fennec.dcat.atlas.sparql` rather than growing `…impl`. It depends on
`…msg.body.writer` for `EObjectRDFModelBuilder.toModel(...)` (already an exported package)
and keeps Jena query concerns out of the CRUD services. Exports a small service interface —
`DcatGraphService` — with `rebuild()`, `replace(resourceId, EObject)`, `remove(resourceId)`
and query execution.

✅ **P1-2 · Transactional dataset, one named graph per resource.**
Use `DatasetFactory.createTxnMem()`, **not** a bare `Model`: SPARQL reads will race admin
writes, and a plain `Model` is not safe for concurrent read/write. The transactional
in-memory dataset also gives named graphs, which is what makes G3 a one-liner (replace the
graph) and is the natural representation for **FR-14**. Graph name = the resource's
`rdf:about` IRI.

✅ **P1-3 · Startup build.**
Scan the store directory per entity type through the existing `DcatHelper` list/read path,
`toModel(...)` each resource, insert as its named graph. Run off the activation thread; see
P1-6 for readiness.

✅ **P1-4 · Maintenance hook at the persistence boundary (G2).**
Hook create/update/delete in the `…impl` admin services — or inside `DcatHelper`'s
write/delete, which is the single choke point — ordered as:

1. validation, both layers in `DcatHelper.Store.put` (2026-08-18) — the model's own
   constraints (OCL + multiplicities) then SHACL on-write enforcement (FR-4)
2. file write / delete
3. replace or remove the named graph

Graph maintenance must not fail the write once the file is on disk; log and rely on the
reindex path (P1-6) instead, since startup rebuild already makes divergence self-healing.

✅ **P1-5 · SPARQL endpoint.**
A JAX-RS resource accepting `GET ?query=` and `POST` (`application/sparql-query`).
`SELECT`/`ASK` → SPARQL JSON / XML / CSV via content negotiation; `CONSTRUCT`/`DESCRIBE` →
RDF through the existing message body writers, so all serializations come for free. Enforce
a query timeout and a result-size cap from the start.

✅ **P1-6 · Readiness gating and reindex.**
An empty graph answers queries *successfully with zero results* — it does not error. So the
endpoint must return 503 until the initial build completes, and readiness must not go green
before then. This is the same failure class the issue review already recorded for
unconfigured SHACL shapes (a silently unvalidating portal); do not repeat it. Add an admin
reindex operation so recovery does not need a restart.

✅ **P1-7 · Periodic reconciliation (cheap safety net).**
Compare a cheap store fingerprint — file count plus newest mtime, or a directory digest —
against what the graph was built from, and rebuild on mismatch. Converts "the graph might be
silently wrong" into "wrong for at most one interval".

✅ **P1-8 · Tests.**
Unit: graph replace is idempotent; delete removes the graph; `toModel` output matches the
REST RDF representation. Integration (in `…rest.tests`, alongside the existing 62): write
then SPARQL sees it; delete then it is gone; restart rebuilds; a query before readiness gets
503, not an empty result set; concurrent write + query smoke test.

✅ **P1-9 · Configuration.**
SPARQL endpoint on/off, query timeout, result cap, reconcile interval. Store directory keeps
using the existing `StoreConfig`.

### Acceptance criteria

All met; the test each one rests on is named beside it.

- [x] SPARQL answers the G4 class of query against data created through the admin API —
      `aRestWriteIsImmediatelyVisibleToSparql`, `selectReturnsSparqlResultsJson`,
      `constructReturnsRdfInTheNegotiatedSyntax`.
- [x] Kill and restart → identical results, rebuilt from files —
      `refreshProjectsEveryStoreAndReportsReady`, `refreshDropsResourcesDeletedBehindItsBack`.
- [x] Delete the graph state and reindex → identical results, no restart —
      `repeatedRefreshIsIdempotent`, `reindexIsAcceptedAndKeepsAnsweringMeanwhile`.
- [x] A query issued during startup returns 503 — `readinessIsCriticalUntilTheProjectionIsBuilt`,
      `readinessReportsTheProjection`.
- [x] Graph stays correct when a mutation goes through the OSGi admin service **without** REST —
      `aMutationThroughTheOsgiServiceWithoutRestIsProjected`. This is the criterion that would
      have silently regressed had the hook been put in the REST resources, and it is the one
      that found a leak of its own: it set `about` to the *public* IRI, which the service does
      not recognise as ours, so the store minted a fresh id and its cleanup deleted nothing.

Two things the criteria did not ask for and that turned out to matter:

- A resource that cannot be projected is counted and surfaced (WARN on `/health/ready`), not
  silently dropped — `resourceWithoutAboutIsSkippedRatherThanFailing`.
- Changing a resource's `about` renames its graph rather than leaving the old one behind —
  `changingAboutRenamesTheGraphRatherThanDuplicating`. Named-graph-per-resource makes this
  possible to get wrong in exactly one place, which is the argument for it.

### Phase-3 enablers that must not slip

**P1-4** (boundary hook) and **P1-6** (reindex/rebuild) are what make Phase 3 a deployment
topology change rather than a redesign. They are cheap now and expensive to retrofit.

✅ Both landed as specified, and P1-4 in particular went in at the persistence boundary rather
than in the REST resources — the distinction Phase 3 depends on, and the one an acceptance
criterion was written to catch.

### Interaction with N7

`accessService` containment-vs-pointer decides whether a `DataService` is its own file and
named graph or embedded inside a distribution's. Settle N7 before P1-3, or the graph
granularity has to change afterwards.

✅ Settled before P1-3, as required: `accessService` is a pointer, so a `DataService` is its own
file and its own named graph, and unlinking never deletes it. Distributions went the other way
— they are contained in their Dataset (FR-10), have no file of their own, and are projected as
a named graph under the dataset's IRI (`distributionsAreProjectedUnderTheirDataset`).

---

## 3. Phase 2 — git-backed store

> **Status 2026-08-17: not started, and deliberately so.** Phase 1 is complete and the
> file store is doing its job; the git work depends on groundwork in other projects that has
> to come first. Nothing in Phase 1 presumes a file store beyond `DcatHelper.Store` being the
> single write choke point, which is the seam Phase 2 replaces.
>
> One thing Phase 1 establishes that is worth carrying in: the projection is derived and
> rebuildable from whatever the store is, so a git-backed store changes where files come from
> and not how the graph is maintained.

**Goal:** the same files, in a git working tree, committed on write.

### Design decision: working tree, not object reads

A git working tree *is* a directory of files, so `DcatHelper`'s I/O stays as it is and Phase 2
adds `add` + `commit` after each successful write. This is deliberately **not** the
model.atlas design (`git://{commitId}/{path}`, no working-tree checkout, content streamed from
git objects) — that shape serves read-only replicas and belongs to Phase 3's read side.

### Findings from the existing projects

Surveyed 2026-08-10.

**`/opt/git/org.gecko.jgit` — read-only.** `GitService` is `getFiles()`,
`getFiles(prefix)`, `getBranches()`, `readLatestFile(file)`, `readFile(commitId, file)`,
`loadFile(commitId, file, out)`, `getBranch()`, `getGitUrl()`, `getRef()`, `fetch()`.
**No `add`, `commit`, `push` or `checkout`.** `GitConfig` does carry what write auth needs:
`repo()`, `branch()`, `privateKey()`, `privateKeyPassphrase()`, `knownHosts()`.

**`/opt/git/model.atlas/org.eclipse.fennec.model.atlas.management.git` — read-only by
design.** Its javadoc states "every write throws". Worth borrowing conceptually:
`GitURIHandler` (EMF URI handler over `git://{commitId}/{path}`), commit SHA used as the
object *version*, and branches modelled as *stages* (a possible fit for draft-vs-published
catalogs later).

**Consequence:** write support does not exist anywhere in the ecosystem today. JGit itself
does it easily (`Git.add()`, `commit()`, `push()`), but neither wrapper exposes it.

**Recommendation:** contribute write operations upstream to `org.gecko.jgit`, since a
read-only git service is a gap other Fennec consumers will hit too, and dcat.atlas would
otherwise carry a private fork of the same idea. Fall back to a local git-write service only
if upstreaming is slow. Note that committing does not strictly require a working tree — JGit
can build a tree through the index — so the upstream API should not assume one.

### Work items

- **P2-1** Git write service: stage + commit per mutation, optional push to a remote.
- **P2-2** Decide commit granularity: one commit per mutation (simple, precise audit trail,
  more objects) vs batched. Start with per-mutation.
- **P2-3** ETag strategy: adopt the commit SHA as the strong ETag, satisfying **FR-16** and
  giving an audit trail from the same mechanism. Decide how `If-Match` maps to it.
- **P2-4** Single-writer assumption written into the configuration reference — two processes
  committing to one working tree is not safe.
- **P2-5** Repository growth and `gc` policy; retention of history.
- **P2-6** Migration: import the existing store directory as the initial commit.
- **P2-7** Auth and secrets: SSH key handling, following the `GitConfig` precedent.
- **P2-8** Failure semantics: what the API returns when the file write succeeds but the
  commit fails.

### Open decisions

- Upstream write support in `org.gecko.jgit` vs a local service (P2-recommendation above).
- Whether a remote push is part of a write's success criteria or an asynchronous mirror.

---

## 4. Phase 3 — separable write and read runtimes

**Goal:** admin/write in one runtime (single instance), read-only REST + SPARQL in a
horizontally scalable runtime. Both deployable from the same codebase.

```
┌─ write runtime (1 instance) ────┐        ┌─ read runtime (N instances) ──┐
│ admin REST + SHACL + ETag       │  git   │ read-only REST + SPARQL       │
│ writes files, commits           │ commit │ in-memory graph @ commit SHA  │
│ serves its own reads            │───────▶│ pull, reload changed graphs   │
└─────────────────────────────────┘        └───────────────────────────────┘
```

The commit history is the change feed, and `git diff --name-only <old>..<new>` yields exactly
the resources to reload. This matches the persistence concept's principle P3 — *between
systems use the persisted log (pull/replay), never fire-and-forget events*.

### Why a per-instance clone rather than a shared directory

Each read instance holding its own clone means everything it serves is *as of commit N* —
REST GET and SPARQL agree. A shared directory instead produces an instance that disagrees
with itself: GETs read through to current files while SPARQL lags. It also removes the need
for `ReadWriteMany` shared storage when scaling.

### Work items

- **P3-1** Split the REST surface by role. `…rest` currently holds both the admin resources
  and `CatalogReadOnlyResource`, and `…impl` holds both `*AdminServiceImpl` and
  `*ReadOnlyServiceImpl` (`CatalogAdminServiceImpl extends CatalogReadOnlyServiceImpl`, so
  they are genuinely coupled). Cheapest route: ship the same bundles and gate which JAX-RS
  resources register, by configuration — unused admin services in a read runtime are harmless
  because they are only reachable in-process. A bundle split is the alternative.
- **P3-2** Two bndruns (`base.bndrun` plus a read variant), or one with configuration
  profiles.
- **P3-3** Read-side resync: webhook receiver **plus** a reconcile poll as backstop, with a
  tip-commit comparison so a webhook and a poll for the same push are idempotent — the
  pattern `EObjectGitStorageService` already implements (`notify(topic, WebhookPayload)` →
  `reconcile(branch)`, `startReconcilePoll(intervalSeconds)`).
- **P3-4** Incremental reload of only the changed named graphs, from the commit diff.
- **P3-5** Commit SHA as the read side's strong ETag.
- **P3-6** State the bounded-staleness requirement as a number.
- **P3-7** Document that there is no read-your-writes across the split. Mitigations: the
  write runtime serves its own reads for the admin UI, or writes return the commit SHA and
  the client waits for the read side to reach it.

### Reuse decision — webhook handling

**Open — to be discussed, current leaning against.** Taking
`org.eclipse.fennec.model.atlas.management.git` would import model.atlas's *workflow* domain:
the bundle is `-workingset: workflow`, its buildpath requires both
`org.eclipse.fennec.model.atlas.management` and `org.eclipse.fennec.model.atlas.workflow`, and
`GitStorageHelper` is built around stages and scopes (86 references). DCAT.Atlas has no
workflow, stage or scope concept and does not want one, so the coupling buys a read-only git
reader at the price of an unrelated domain model. Its *ideas* are still worth borrowing
(§3 findings) — the code is not.

**Under consideration, and materially cleaner:** `…management.git.webhook.model` (+ the
GitHub/GitLab payload models)
and `…management.git.webhook.rest`, which provide provider-neutral webhook payloads,
`WebhookTopics`, and GitHub/GitLab endpoints *with signature verification*
(`AbstractWebhookSignatureFilter`, `VerifyGithubWebhookSignature`,
`VerifyGitlabWebhookSignature`) — security-sensitive code worth not rewriting. It already
builds on `org.eclipse.fennec.codec.rest`, which is in our runbundles today.

Unlike `management.git`, these carry **none** of the workflow baggage: `webhook.rest` builds
only against the three webhook payload models plus `codec.rest`, JAX-RS, typed events and DS,
and `webhook.model` builds against nothing but `org.osgi.service.condition`. Only their *names*
carry the `management.git` prefix.

**Concern:** it would still couple dcat.atlas to a *domain* repository for code that is
entirely generic — GitHub and GitLab webhook parsing has nothing to do with model.atlas. The
ecosystem has precedent for the fix: persistence decision R5 gave
`org.eclipse.fennec.expression.model` a repo-neutral BSN precisely so consumers depend on a
neutral artifact. Raising the same extraction for the webhook bundles is cheaper **before**
anyone builds on them, since afterwards a rename breaks two repos instead of one.

**To decide:** (a) depend on the webhook bundles as they are, (b) request the neutral
extraction upstream first, or (c) implement webhook receipt locally — small, except for the
signature verification, which is the part worth not re-deriving.

### Non-goals

Active/active writers. Two processes committing to one repository, or two writers on shared
storage, reintroduces clobbering — `If-Match` checks on both can pass before either writes.
If write HA is needed, active/passive.

---

## 5. Cross-cutting risks and open questions

| # | Item | Notes |
|---|---|---|
| R1 | **No documented scale requirement** | Nothing in `opendata-portal-anforderungen.en.md` or `…planung.md` states catalog volume, load or response-time targets. Memory sizing for the graph, and whether Phase 3 is ever needed, both depend on it. Worth writing down. |
| R2 | Graph memory footprint | Rule of thumb, not a measurement: a few thousand datasets at ~50 triples each is a few hundred thousand triples, comfortable for Jena. Revisit against R1. |
| R3 | Startup time grows with catalog size | Readiness gating (P1-6) makes it safe; rolling restarts must respect it. |
| R4 | Unbounded SPARQL queries | G4 is not enforced; a pathological query can materialize a lot. Hence the timeout and result cap in P1-5. |
| R5 | N7 / `accessService` | Determines graph granularity — settle before P1-3 (§2). |
| R6 | Jena API verification | The `DatasetFactory.createTxnMem()` and `GraphBase`/`graphBaseFind` names should be checked against the bundled Jena 6.1.0 before being relied on. |

## 6. What this plan deliberately does not do

- **Adopt Fennec Persistence.** Deferred, not rejected. Should it return, the findings doc
  §7 prerequisite still applies: no `iD="true"` exists in any of our ecores, so identity
  would have to be declared per EClass.
- **Introduce a second database.** No TDB2, no Mongo.
- **Translate SPARQL into a query IR.** Structurally unsound as a general approach — findings
  §4. The rooted subset (§4.4) remains available for pushdown if it is ever needed as an
  optimization.

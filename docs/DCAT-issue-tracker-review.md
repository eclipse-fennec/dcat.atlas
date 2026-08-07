# DCAT — Issue Tracker Review

> As of: 2026-08-07
> Board: <https://github.com/DataInMotion/xdp/issues> (issue snapshot: `docs/DCAT-github-issues.md`)
> Purpose: reconcile the 13 DCAT issues on the board against **what is actually in
> `/opt/git/dcat.atlas`**, decide which are genuinely open, and sketch the issues still
> missing to cover work packages **WP-DCAT-1…13**.
>
> Companion documents: `docs/status-gap-analysis.md` (FR/F-level status, contradictions
> C1…C11 — **now partly stale**, see §4) and `docs/development-guide.md` (change log).

**Legend:** ✅ delivered · 🟡 partially delivered · ⬜ not started

---

## 1. Summary

- **All 7 closed issues are genuinely closed** in the sense that the thing they asked
  for exists and is tested. The one piece of untracked **residue** is that #29 never
  delivered the `accessService` link endpoint (FR-10) — see §2.
- **The three open issues are all genuinely open**, but #5 is now nearly empty: its own
  scope (CRUD REST + OSGi service) is done, and the only thing left under it is its
  sub-issue #32 (persistence). #5 should either be narrowed or closed in favour of #32.
- **The board covers roughly half the plan.** Issues exist for WP-DCAT-1/2/3/4/6.
  There is **no issue at all** for WP-DCAT-5 (SPARQL + delivery), 7 (endpoint
  awareness), 8 (catalog browser), 9 (ops/Docker), 10 (documentation), 11 (EMF editor),
  12 (OData query UI), 13 (admin UI), or the cross-cutting QA line.
- §3 sketches **24 new issues** to close that gap, with a recommended
  *create now / create later* split so the board does not get flooded.

---

## 2. Existing issues — verified against the code

### 2.1 Closed issues

| # | Title | Verdict | Evidence in the repo |
|---|-------|---------|----------------------|
| 1 | WP-DCAT-02 — DCAT-AP model to v3 | ✅ correctly closed | `org.eclipse.fennec.dcat.atlas.dcatap.de.model` — generated DCAT-AP.**de** 3.0 model incl. `DatasetSeries`, `Relationship`, `CatalogRecord`, plus adms/foaf/locn/odrl/owl/prov/rdf/schema/skos/spdx/terms/vcard sub-packages. |
| 2 | WP-DCAT-1 — initial gradle setup | 🟡 closed early | bnd workspace + per-bundle Gradle + `cnf` ✅. But WP-DCAT-1 also asks for **CI (build + tests)** and a **standalone bndrun**: `.github/workflows/` contains only `docs-pages.yml`, and a full-workspace `./gradlew` build still fails resolving a `-SNAPSHOT` gecko library. → new issues **N1**, **N2**. |
| 3 | Use Apache Jena for N3, Turtle, JSON-LD | ✅ correctly closed | `…msg.body.writer`: `TurtleMessageBodyWriter`, `N3MessageBodyWriter`, `JsonLdMessageBodyWriter`, `NTriplesMessageBodyWriter`, `RdfXmlMessageBodyWriter` + `RdfXmlMessageBodyReader`, all over `EObjectRDFModelBuilder`. |
| 4 | OSGi-fied Jena deps → gecko libraries | ✅ correctly closed | Patched Jena 6.1.0 bundles live in the separate repo `/opt/git/geckoprojects-ibraries` (SPI-Fly `osgi.serviceloader` capabilities) and are consumed here. **Caveat:** they must be published to the local repo for this repo to build — an undocumented onboarding trap, folded into **N2**. |
| 9 | AP-DCAT-1 — repo + documentation setup | 🟡 closed early | `docs/` (dev guide, user guide, admin-API spec DE/EN, requirements, planning) + `docs-site/` ✅. `README.md` is two lines. → **N22**. |
| 29 | WP-DCAT-4 — relationship/membership endpoints | 🟡 closed with residue | FR-9 ✅ (`…/admin/catalogs/{id}/{datasets,services,catalogs}`), FR-11 ✅ (via `Dataset.inSeries`, `…/admin/dataset-series/{id}/datasets/{dsId}`), FR-10 ✅ *for composition* (`…/admin/datasets/{id}/distributions`, no dataset-less create). **Not delivered: the `accessService` link** — `grep -i accessservice` over `api`/`impl`/`rest` returns nothing, although FR-10 names it explicitly. → **N7**. |
| 30 | WP-DCAT-4 — SHACL input validation | ✅ correctly closed | FR-5 dry-run: `ValidationResource` → `POST /admin/validate/{type}` returns the native Jena `ValidationReport` negotiated across Turtle/JSON-LD/RDF-XML/N3/NT + `X-SHACL-Conforms`. FR-4 on-write: `helper/WriteValidation` → 422 + report before persist, config-gated via `ShapesConfig.enforceOnWrite`, MUSS(`sh:Violation`) blocks / SOLL(`sh:Warning`) does not. Plus controlled-vocabulary validation (F-22) via `ShapesConfig.vocabularyDirectory`. Integration-tested. **Two operational caveats, neither tracked:** the GovData shapes are AGPLv3 so they cannot be vendored and must be supplied by the operator, and `owl:imports` are inert — without the upstream `dcat-ap-SHACL.ttl` and the EU/GovData authority tables in those directories, base DCAT-AP rules silently do not fire. → **N27**. |

Also worth fixing in `docs/DCAT-github-issues.md`: the #30 entry has a duplicated
heading marker (`### ### [DCAT] …`).

### 2.2 Open issues

| # | Title | Verdict |
|---|-------|---------|
| 5 | WP-DCAT-4 — CRUD Operations for DCAT | **Effectively done — narrow or close.** All five entities (Catalog, Dataset, DatasetSeries, DataService, Distribution) have read-only + admin OSGi services (`…api` / `…impl`) and REST resources (`…rest`), XML/JSON in and all RDF formats out, upsert/idempotency, ETag/If-Match, SHACL enforcement, 62 green OSGi integration tests. The issue text's remaining clause is "*dcat data should be persisted, either via fennec persistence or file-based*" — file-based **is** implemented (`impl/helper/DcatHelper`, one `<id>.rdf` per resource), and the upgrade path is exactly sub-issue #32. Recommendation: **close #5**, let #32 carry persistence, and open the residual FR-4-scope items as their own issues (**N7**, **N8**, **N9**, **N10**). |
| 32 | WP-DCAT-3 — Persistence of DCAT objects | **Genuinely open and the single highest-priority item.** Nothing in the product code references TDB2, a dataset, or transactions (`grep -ril 'tdb2\|fuseki\|QueryExecution'` over `src` → no hits; the only "sparql" matches are string literals in tests). This is also decision **C1** — requirements F-10 say JPA/PostgreSQL, WP §3.2 and admin-API D5/D6 say Jena TDB2, and the code is neither. Recommendation: make #32 explicitly a **decision + ADR** issue with the implementation split into **N4/N5/N6**, because it blocks FR-6 (transactions), FR-12 (by-URI dedup), FR-14 (named-graph replace), WP-DCAT-5 (SPARQL) and FR-1's 409-when-referenced. |
| 7 | WP-DCAT-06 — Client library | **Genuinely open, untouched.** No client bundle exists. Blocker for WP-DA-10, WP-MA-5 and WP-SN-4 — i.e. three other modules are waiting on it. Recommendation: do **N11** (freeze the contract) before or alongside it, otherwise the client library locks in an API the spec still contradicts. |
| 8 | Convert Ecore into RDF | **Genuinely open, untouched**, and correctly marked low priority. Note it overlaps WP-MA-5 (Model-Atlas registering schemas as DataService/Distribution) — worth a cross-link so it is not solved twice. |

---

## 3. Proposed new issues

Ready-to-paste sketches. "Now" = create on the board immediately (near-term critical
path or already-identified residue); "Later" = create when the preceding WP starts, so
the board stays readable.

### WP-DCAT-1 — build & CI

**N1 · [DCAT] WP-DCAT-1 — CI pipeline for build and tests** — *Now*
> `.github/workflows/` only has `docs-pages.yml`; nothing builds or tests on push/PR.
> Add a workflow that builds all bundles and runs unit + OSGi integration tests.
> Blocker to resolve first: a full-workspace `./gradlew` build fails resolving a
> `-SNAPSHOT` gecko library (`org.gecko.emf.util.jakartars…`) — pin it or publish it —
> and the patched Jena 6.1.0 bundles from `geckoprojects-ibraries` must be available to
> CI. Also add the Eclipse compliance-header check.
> *Evidence:* green build + test run on a PR.

**N2 · [DCAT] WP-DCAT-1 — Reproducible local build & run documentation** — *Now*
> Document the two non-obvious prerequisites that currently only live in people's heads:
> (a) the patched OSGi Jena bundles live in the separate `geckoprojects-ibraries` repo
> and must be published to the local repo first; (b) the full-workspace gradle build is
> broken, so build per bundle / in bndtools. Cover running the portal via
> `org.eclipse.fennec.dcat.atlas.runtime/local.bndrun` (port 8085, `/dcat/rest`) and the
> `SHACL_SHAPES_DIR` / `SHACL_VOCAB_DIR` / `SHACL_ENFORCE` knobs.
> *Evidence:* a fresh clone can be built and run from the README alone.

### WP-DCAT-3 — persistence (all depend on #32)

**N4 · [DCAT] WP-DCAT-3 — Named-graph layout and transactional writes** — *Later (after #32 decision)*
> One named graph per catalog (admin-API D6); all write operations atomic (FR-6),
> including the multi-entity membership operations. Replaces the current non-atomic
> file-per-entity writes.

**N5 · [DCAT] WP-DCAT-3 — EMF⇄RDF roundtrip fidelity tests** — *Later*
> WP-DCAT-3 lists roundtrip fidelity as its evidence and it is currently untested:
> blank nodes, language tags, typed literals, and the `rdf:resource` vs. nested-object
> distinction (see `docs/` RDF/XML body mapping notes) must survive EMF → RDF → EMF.

**N6 · [DCAT] WP-DCAT-3 — By-URI deduplication of embedded references (FR-12)** — *Later*
> An entity referenced from two catalogs must be stored once and resolved by URI, not
> duplicated per containment tree. Natural in a triple store, needs explicit design in a
> relational one — hence blocked on #32.

### WP-DCAT-4 — admin interface (residue from #5/#29/#30)

**N7 · [DCAT] WP-DCAT-4 — `accessService` link endpoint on Distribution (FR-10)** — *Now*
> FR-10 requires `Distribution.accessService` to optionally reference a DataService.
> Issue #29 was closed without it (no `accessService` anywhere in `api`/`impl`/`rest`).
> Add the link/unlink endpoint under the nested distribution path, ETag-guarded on the
> distribution, consistent with the FR-9/FR-11 membership endpoints.

**N8 · [DCAT] WP-DCAT-4 — Error taxonomy: cascade delete, 409-when-referenced, structured 400 (FR-19)** — *Later (needs #32)*
> `deleteX(id, cascade)` currently accepts `cascade` and ignores it — there are literal
> `// TODO FR-1` markers in all four admin impls — and 409 is never returned. Implement
> referential checks and cascade semantics once the real store lands, plus the structured
> 400 error body from admin-API §6.

**N9 · [DCAT] WP-DCAT-4 — Bulk graph ingest (FR-13/14/15)** — *Later (needs #32)*
> `CatalogIngestService` + `POST /ingest`: ingest a whole DCAT graph, named-graph
> replace/delete with `?purge`, and differential upsert. Nothing exists today.

**N10 · [DCAT] WP-DCAT-4 — OpenAPI descriptor for the admin and read API (F-15)** — *Now*
> `io.swagger.core.v3.swagger-annotations` is already on the `…rest` buildpath but no
> resource uses it. Annotate the resources and serve the descriptor. Prerequisite for the
> admin UI (WP-DCAT-13) and useful documentation for the client library (#7).

**N11 · [DCAT] WP-DCAT-4 — Reconcile the admin-API spec with the implementation** — *Now (blocks #7)*
> `docs/status-gap-analysis.md` records contradictions C4…C9 between
> `opendata-portal-admin-api_EN.md` and the code: top-level `/distributions` vs. nested
> (spec contradicts itself), aggregate `CatalogAdminService`/`CatalogRelationService` vs.
> the per-entity services actually built, link-by-id vs. link-with-member-body on the
> membership endpoints, `seriesMember` vs. `inSeries`, and the missing `/api/v1` base
> path. On the membership shape specifically: **both designs satisfy FR-9** — neither
> re-sends the catalog — but the spec's `PUT /catalogs/{id}/datasets/{datasetId}` (and
> `CatalogRelationService.linkDatasetToCatalog(catalogId, datasetId)` in §6) sends *no
> body at all* and therefore presupposes the dataset already exists as an independently
> stored resource, whereas the code sends the member's content and embeds it as
> containment. That is a consequence of C1 (file store vs. triple store), so decide it
> together with #32. Also settle C6: is If-Match "mandatory" = *honour if present* (today) or
> *reject without it* (adds 428)? Freeze this **before** the client library (#7) hardens
> the contract.

**N12 · [DCAT] WP-DCAT-4 — CatalogRecord / audit provenance (FR-20, optional)** — *Later, low*
> The model has `CatalogRecord` but no service or endpoint uses it. Optional per the spec.

### WP-DCAT-5 — catalog delivery (no issue exists today)

**N13 · [DCAT] WP-DCAT-5 — SPARQL endpoint** — *Later (needs #32)*
> ARQ over the store, with the Fuseki option evaluated. Core WP-DCAT-5 deliverable and a
> prerequisite for the browser's search/facets (N19).

**N14 · [DCAT] WP-DCAT-5 — Pagination and caching on the read collections** — *Now*
> `GET /{collection}` currently loads and serialises **every** resource
> (`CatalogReadOnlyResource.listCatalogs` and its four siblings). Add pagination and
> cache headers before the catalog grows.

**N15 · [DCAT] WP-DCAT-5 — Dereferenceable resource URIs** — *Later*
> A minted `about` URI must resolve to the resource with content negotiation. Pairs with
> N16.

### WP-DCAT-7 — endpoint awareness (no issue exists today)

**N16 · [DCAT] WP-DCAT-7 — Public base URL and endpoint self-awareness (FR-16/17/18)** — *Later*
> The base URI is derived from the incoming request `UriInfo`; there is no configured
> `publicBaseUrl`, so URIs minted behind a proxy or in a container are wrong. Add
> configuration, internal→public resolution, and the optional reachability health check
> of distribution `accessURL`s.

### WP-DCAT-8 — catalog browser (no issue exists today)

**N17 · [DCAT] WP-DCAT-8 — UI stack decision and scaffold** — *Later*
> Decision issue: pick the stack (shared with the admin UI, WP-DCAT-13) and stand up the
> scaffold. Record as an ADR.

**N18 · [DCAT] WP-DCAT-8 — Catalog overview, dataset/series detail, distribution views** — *Later*
> Includes the "view source" format toggle, responsive layout, WCAG AA basics (F-27),
> DE/EN i18n (F-28), CSS customisation hook (F-26) and the legal pages (F-29) — those
> product requirements have no other home.

**N19 · [DCAT] WP-DCAT-8 — Search and facets over SPARQL** — *Later (needs N13)*

### WP-DCAT-9 — operations & deployment (no issue exists today)

**N20 · [DCAT] WP-DCAT-9 — Docker image and container configuration** — *Now*
> There is no Dockerfile, and `org.eclipse.fennec.dcat.atlas.config.docker/configs/config.json`
> is still the generated stub (`"ExampleConfig": {"your.prop.here": "text"}`). Build a
> container over the `…runtime` bundle, fill the docker config bundle (env-driven:
> store location, shapes/vocabulary dirs, `SHACL_ENFORCE`, public base URL), and mount a
> volume for the store (F-23/F-24).

**N21 · [DCAT] WP-DCAT-9 — Health and readiness endpoints (F-25)** — *Now*
> No health/readiness endpoint exists. Needed for the container orchestrator; readiness
> should reflect store availability and whether the SHACL shapes actually loaded.

### WP-DCAT-10 — documentation

**N22 · [DCAT] WP-DCAT-10 — README, operations manual and integration guide** — *Later*
> `README.md` is two lines. Needs: architecture overview, the ops manual, the integration
> guide for Data-/Model-Atlas/SensiNact (pairs with #7), and the generated OpenAPI
> reference (N10). End-user docs for the browser and SPARQL follow WP-DCAT-8.

### WP-DCAT-11 / 12 / 13 — editors and UIs

**N23 · [DCAT] WP-DCAT-11 — EMF editor for the DCAT-AP.de 3 model** — *Later*
> Adapt the generated edit/editor bundles: labels, icons, cleaned-up property views,
> guided creation, validation feedback.

**N24 · [DCAT] WP-DCAT-12 — OData query UI in the catalog browser** — *Later (blocked on N17/N18 + WP-OD-2)*

**N25 · [DCAT] WP-DCAT-13 — Admin web UI** — *Later (needs N10, N17)*
> Form-based CRUD for all five entities incl. memberships, PUT-upsert + If-Match conflict
> handling (412 dialog), SHACL feedback before saving.

**N26 · [DCAT] WP-DCAT-13 — Authentication and authorisation wiring (F-5/6/7/12, FR-21)** — *Later*
> The `/admin/**` vs. `/{collection}` path split already prepares an APISix/Keycloak PEP,
> but nothing is wired: no login, no OAuth client-credentials for machine clients (which
> the client library #7 will need), no roles.

### Cross-cutting

**N27 · [DCAT] Cross-cutting — SHACL conformance in CI, test corpus, and shapes provisioning** — *Now*
> Three linked gaps left over from #30: (a) the official GovData DCAT-AP.de shapes are
> **AGPLv3 and cannot be vendored**, so document/automate the operator-side download of
> both the shapes and the `owl:imports` reference data (EU/GovData authority tables);
> (b) because those imports are inert, the upstream `dcat-ap-SHACL.ttl` must be present
> or base DCAT-AP rules silently never fire — this needs a startup warning or a
> readiness signal (see N21); (c) build a corpus of real DCAT examples and run SHACL
> conformance in CI (needs N1).

---

## 4. Work-package coverage matrix

| WP | Existing issues | Proposed | Coverage after |
|----|-----------------|----------|----------------|
| WP-DCAT-1 Setup & build | #2 ✅, #9 ✅ | N1, N2 | complete |
| WP-DCAT-2 Model v3 | #1 ✅ | (conformance corpus → N27) | complete |
| WP-DCAT-3 Jena persistence & bridge | #3 ✅, #4 ✅, **#32 open** | N4, N5, N6 | complete |
| WP-DCAT-4 Admin interface | #5 open (→close), #6 ✅, #28 ✅, #29 ✅, #30 ✅ | N7, N8, N9, N10, N11, N12 | complete |
| WP-DCAT-5 Delivery + SPARQL | **none** | N13, N14, N15 | complete |
| WP-DCAT-6 Client library | **#7 open** | (N11 first) | complete |
| WP-DCAT-7 Endpoint awareness | **none** | N16 | complete |
| WP-DCAT-8 Catalog browser | **none** | N17, N18, N19 | complete |
| WP-DCAT-9 Operations & deployment | **none** | N20, N21 | complete |
| WP-DCAT-10 Documentation | **none** | N22 | complete |
| WP-DCAT-11 EMF editor | **none** | N23 | complete |
| WP-DCAT-12 OData query UI | **none** | N24 | complete |
| WP-DCAT-13 Admin UI | **none** | N25, N26 | complete |
| Cross-cutting QA | **none** | N27 | complete |
| *(unmapped)* | #8 Ecore→RDF | — | cross-link to WP-MA-5 |

## 5. Appendix — draft comment for issue #30 (SHACL shapes: licensing & runtime loading)

To be posted on <https://github.com/DataInMotion/xdp/issues/30>. Kept here so the
reasoning survives outside the issue tracker.

> **Note on the SHACL shapes: licensing, and why they are loaded at runtime**
>
> Recording the reasoning behind the shapes-loading design, since it is not obvious from
> the code alone.
>
> **The problem.** The official GovData DCAT-AP.de 3.0 SHACL shapes are published under
> AGPL-3.0. We cannot vendor them into this repository — the bundle ships under EPL-2.0
> and committing AGPL artifacts into the distribution would create a licensing conflict.
>
> **The design.** No shape file is committed. `DcatValidationServiceImpl` loads them at
> bundle activation from an operator-configured directory (`ShapesConfig.shapesDirectory`,
> env `SHACL_SHAPES_DIR`): every `*.ttl` in that directory is merged into a single Jena
> `Shapes` graph. If no directory is configured the service is a no-op and reports
> conformance, so the portal still starts. The shapes are therefore *deployment input*,
> never part of what we build or redistribute.
>
> The same applies to the controlled-vocabulary reference data
> (`ShapesConfig.vocabularyDirectory`, env `SHACL_VOCAB_DIR`). This is needed because the
> CV constraints check `skos:inScheme` / `sh:class` on the **value** node, and those
> triples live in the EU/GovData authority tables that the shapes reach via `owl:imports`.
> Jena does not resolve `owl:imports` — there is no auto-fetch — so without local copies
> every vocabulary value would report as a false violation. `validate()` runs the shapes
> against `ModelFactory.createUnion(entity, vocab)`.
>
> **How it was verified.** Tested against the real GovData shapes and the real authority
> tables, both downloaded to local directories outside the repository and loaded through
> the two config properties. Confirmed: a genuine `…/frequency/ANNUAL` conforms, a bogus
> value produces the MUSS `#kv-frequency` violation.
>
> **What is in the repo instead.** The unit and integration tests use tiny self-authored
> shapes, so nothing licensed is committed and CI needs no external files. The real-data
> regression test `ControlledVocabularyRealDataTest` is `@EnabledIfEnvironmentVariable` on
> `SHACL_SHAPES_DIR` / `SHACL_VOCAB_DIR` and simply skips where those are unset.
>
> **Consequence to be aware of.** Because `owl:imports` are inert, only the shapes
> physically present in the directory actually fire. The DCAT-AP.de-native shapes are
> self-contained, but the German files carry only the message/severity *overrides* for the
> upstream SEMIC base rules, not the rule bodies — so to enforce base DCAT-AP 3 constraints
> the operator must also place `dcat-ap-SHACL.ttl` in the same directory. Follow-up work
> (documenting/automating the operator-side download, and surfacing "shapes loaded / not
> loaded" as a startup warning or readiness signal so a silently unvalidating portal is
> visible) is tracked separately.

**Two open points to confirm before posting:** (a) the shape files carry no inline
license header, so the AGPL-3.0 claim rests on the GovData source repository — verify it
is still current; (b) the upstream SEMIC `dcat-ap-SHACL.ttl` comes from a different
publisher and may be under different terms — if it turns out to be redistributable, it
could be vendored, which would remove one manual provisioning step.

---

## 6. Recommended board actions, in order

1. **Close #5** (its scope is delivered; #32 carries the remainder) and open the residue
   as N7, N8, N9, N10.
2. **Re-scope #32** into a decision + ADR on the store backend (C1: Jena TDB2 vs.
   JPA/PostgreSQL), with N4/N5/N6 as follow-ups. It is the critical path — six other
   requirements are blocked behind it.
3. **Create the "Now" issues:** N1, N2, N7, N10, N11, N14, N20, N21, N27.
4. **Do N11 before #7** so the client library does not freeze a contract the spec still
   contradicts, and remember #7 blocks WP-DA-10, WP-MA-5 and WP-SN-4.
5. Create the "Later" issues as each work package starts.
6. Refresh `docs/status-gap-analysis.md` — it is dated 2026-07-09 and predates on-write
   SHACL enforcement (FR-4), controlled-vocabulary validation (F-22), the native Jena
   `ValidationReport`, and the runtime/config bundles, all of which it still lists as
   open.

# DCAT.Atlas — Status & Gap Analysis

> As of: **2026-08-18** (previous revisions: 2026-08-07, 2026-07-09)
> Purpose: reconcile **what is implemented**, **what is still open**, and **where the
> source documents contradict each other or the code**, across the three planning
> inputs. This is an internal working document; it is not the requirements spec and
> does not override it — where it flags a contradiction, the resolution is a decision
> for review.

**What changed in 2026-08-18:** a second validation layer — the model's own constraints,
declared as OCL invariants on the ecore and enforced at the persistence boundary. This matters
for how FR-4/F-21 should be read: SHACL is **operator-configured and absent by default**, so
until now a deployment without a shapes directory validated nothing. The model constraints ship
inside the model and always apply. See FR-4, FR-19 and F-21 below.

**What changed in 2026-08-07:** SHACL validation completed end to end (FR-4 on-write
enforcement, FR-5 dry-run, F-22 controlled vocabularies, native Jena `ValidationReport`);
runtime + configuration bundles added; JSON read path fixed. [C7](#c7) rewritten — the
earlier reading of FR-9 was wrong. The read/write format asymmetry is now spelled out
under FR-8 as the intended design. Issue-board coverage is tracked separately in
`docs/DCAT-issue-tracker-review.md`.

## Sources reconciled

1. **Requirements** — `docs/opendata-portal-anforderungen.en.md` (product requirements, IDs **F-1…F-29**).
2. **Admin API spec** — `docs/opendata-portal-admin-api_EN.md` (technical/API spec, IDs **FR-1…FR-21**, REST + OSGi contract).
3. **Work packages** — the GitHub module plan (**WP-DCAT-1…13**, from `docs/opendata-portal-planung.md`).
4. **Issue board** — `DataInMotion/xdp` (snapshot in `docs/DCAT-github-issues.md`); the
   issue-by-issue verification and the proposed new issues live in
   `docs/DCAT-issue-tracker-review.md`.

> ⚠️ **Three numbering schemes.** `F-n` (product requirements) and `FR-n` (API spec)
> are **different** lists that overlap only partly — e.g. product **F-16** (ETag) ≈ API
> **FR-7**; product **F-10** (JPA/Postgres) conflicts with the WP/API triple-store
> decision (see [C1](#c1)). Always cite the scheme. This doc keys the detailed tables
> off the API `FR-n` and the `WP-DCAT-n`, and maps the product `F-n` separately.

**Legend:** ✅ done · 🟡 partial · ⬜ not started · ⚠️ divergence/contradiction (see §5).

---

## 1. Executive summary

- **The write side of the admin API is essentially complete.** Per-entity CRUD,
  upsert/idempotency, URI minting, RDF content negotiation on read, the FR-9/10/11
  relationship/composition endpoints, ETag/If-Match conditional requests, and SHACL
  validation (both dry-run and on-write enforcement) are implemented and tested
  (unit + 62 OSGi integration tests).
- **Validation is now a finished feature, not a foundation.** FR-4, FR-5, F-21 and F-22
  are all delivered: `DcatValidationService` returns the native Jena `ValidationReport`,
  serialised in any RDF syntax; writes are rejected with 422 before persist when a MUSS
  (`sh:Violation`) constraint fails, while SOLL (`sh:Warning`) is reported and allowed.
  The shapes and the controlled-vocabulary reference data are **operator-supplied at
  runtime** (AGPL licensing — see `docs/DCAT-issue-tracker-review.md` §5).
- **The single biggest gap is still persistence.** The store remains a **file-per-entity
  RDF/XML** placeholder, and the target backend is **contradicted between the sources**
  (Jena TDB2 vs. JPA/PostgreSQL — [C1](#c1)). This blocks the transactional (FR-6),
  named-graph (FR-14/D6) and by-URI-dedup (FR-12) guarantees, the 409-when-referenced
  and cascade semantics (FR-1/[C11](#c11)), and SPARQL (WP-5). It is the critical path.
- **The read side is half-built:** per-resource and collection GET with content
  negotiation in all RDF formats works; **SPARQL, pagination, and search/facets do not
  exist**, and the collection endpoints serialise everything with no paging.
- **The portal now runs.** A `…runtime` bundle plus `config.local` (HTTP :8085, context
  `dcat/`, Jersey `rest`) make it startable and manually testable; `config.docker` is
  still the generated stub and there is no Dockerfile, no health/readiness endpoint and
  no build/test CI.
- **Everything user-facing remains unstarted:** catalog browser (F-1/2/3), admin UI
  (F-4/8), auth (F-5/6/7, FR-21), OpenAPI (F-15), client library (WP-6),
  i18n/a11y/legal (F-27/28/29).
- **Model:** DCAT-AP.de v3 (`org.eclipse.fennec.dcat.atlas.dcatap.de.model`) — a
  *national profile* and a *different bundle* than the plan named ([C2](#c2), [C3](#c3)).

**Rough completion:** admin write-path ≈ 90%; validation ≈ 100%; read delivery ≈ 40%;
persistence ≈ 10% (placeholder only); ops ≈ 20%; everything else ≈ 0%.

---

## 2. Status by API requirement (FR-1…FR-21)

| FR | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-1 | CRUD per entity | 🟡 | All five entities CRUD-able, but on the file store; `DELETE ?cascade` ignored (literal `// TODO FR-1` in all four admin impls) and 409-when-referenced not implemented ([C11](#c11)). |
| FR-2 | Upsert / idempotency | ✅ | `PUT …/{id}` create-or-replace; repeat → same state. |
| FR-2b | Replace-only, no PATCH | ✅ | No PATCH exposed. |
| FR-3 | URI assignment / minting | ✅ | Mints UUID under the request base; adopts client `{id}`. Namespace/collision validation minimal. |
| FR-4 | Validation | ✅ | SHACL rejects a write with **422 + `sh:ValidationReport`** before persist, config-gated by `ShapesConfig.enforceOnWrite` → `DcatValidationService.isWriteEnforced()`. **Since 2026-08-18 the check runs in `DcatHelper.Store.put` (`helper/ShaclValidation`), not in the REST resources** — it used to cover HTTP callers only, so an entity handed straight to `upsertDataset` was stored unvalidated; `ShaclViolationException` carries the report out and `ShaclViolationExceptionMapper` renders it, so the negotiated 422 body is unchanged. Blocks only on `sh:Violation` (MUSS); `sh:Warning` (SOLL) is reported, not blocked. Validation runs **after** the `about` IRI is stamped and, on PUT, **after** If-Match (412 precedes 422). Membership endpoints reach the same store, so they are covered by construction now rather than by a per-endpoint gate. **Second layer since 2026-08-18:** the model's own constraints — the ecore's declared multiplicities plus 44 OCL invariants annotated on the ecores — run in `DcatHelper.Store.put` via EMF's `Diagnostician` and reject with **422 + a plain-text violation list** (`ModelConstraintException` → `ModelConstraintExceptionMapper`). Config-gated by `StoreConfig.validateOnWrite` (**default `true`**; briefly `false` while the test fixtures were still building spec-invalid entities). Unlike SHACL it needs no operator setup, applies at the **persistence** boundary rather than the REST adapter (so every caller of the OSGi services is covered), and fails **closed** if the OCL engine is missing. |
| FR-5 | Dry-run validation | ✅ | `POST /admin/validate/{catalogs\|datasets\|dataset-series\|data-services\|distributions}` (`ValidationResource`) → 200 + full report + `X-SHACL-Conforms`; 503 if the service is unbound. Deviates from the spec's `?validate=only` ([C12](#c12)). |
| FR-6 | Transactionality | ⬜ | File writes are not transactional; multi-entity ops not atomic. Needs the real store. |
| FR-7 | ETag concurrency (**mandatory**) | 🟡 | ETag on GET + after write ✅; `If-Match`→412 ✅; `If-None-Match`/304 ✅. **But not mandatory** — unconditional writes are allowed (no 428) ([C6](#c6)). |
| FR-8 | Format agnosticism | ✅ | **By design, the Jena RDF syntaxes are read-only.** Reads produce RDF/XML, Turtle, N-Triples, JSON-LD and N3 (+JSON/XML); writes — and the dry-run endpoint — consume `application/json`, `application/xml` and `application/rdf+xml` only. Per the project decision recorded in issue #5: "All the endpoints will ask/provide XML. **Only** the GET endpoints, in addition, will provide jsonLD, Turtle and N3 (via Apache Jena)." Implemented consistently across all five admin resources and `ValidationResource`; there is an `RdfXmlMessageBodyReader` but deliberately no Turtle/JSON-LD/N-Triples reader. Client-library authors (WP-6) must respect this asymmetry. |
| FR-9 | Catalog membership | ✅ | `…/admin/catalogs/{id}/{datasets,services,catalogs}` add/remove; only the member is sent, never the catalog. Endpoint shape differs from the spec's link-by-id ([C7](#c7)). |
| FR-10 | Distribution composition | ✅ | Nested `…/admin/datasets/{id}/distributions`, no dataset-less create ✅; `accessService` link ✅ (`PUT`/`DELETE …/distributions/{distId}/access-service/{serviceId}`, N7, 2026-08-10) — stored as a `dcat:accessService rdf:resource` pointer after correcting the model, which had it as *required* containment against the spec's Optional reference (§4.6.24). Still contradicts the spec's top-level `/distributions` ([C4](#c4)). |
| FR-11 | Series membership | ✅ | Via `Dataset.inSeries`; spec's `seriesMember`/`/members/` differs ([C8](#c8)). |
| FR-12 | Embedded references | 🟡 | "Embedded, not top-level" ✅ (containment in the model); **by-URI dedup ⬜** — belongs to the graph store. |
| FR-13 | Graph ingest | ⬜ | No `CatalogIngestService` / `POST /ingest`. |
| FR-14 | Graph replace/delete | ⬜ | No named-graph model, no `?purge`. |
| FR-15 | Differential upsert | ⬜ | — |
| FR-16 | Public URLs unaltered | 🟡 | Stored verbatim ✅; no explicit endpoint-awareness config. |
| FR-17 | Reachability check (opt.) | ⬜ | — |
| FR-18 | Self-awareness / base URL | 🟡 | Derived from the request `UriInfo`; no configured `publicBaseUrl` (AP7) — URIs minted behind a proxy or in a container will be wrong. |
| FR-19 | Error taxonomy | 🟡 | 404/412 ✅; **422 ✅** in both flavours — full SHACL report (negotiated across all five RDF syntaxes) and, since 2026-08-18, a plain-text model-constraint violation list; 409-referenced and structured 400 ⬜. |
| FR-20 | Audit / provenance (opt.) | ⬜ | `CatalogRecord` exists in the model, unused by any service or endpoint ([C10](#c10)). |
| FR-21 | Security (opt.) | 🟡 | `/admin/**` path split prepares an APISix/Keycloak PEP; no auth wiring in-app. |

## 3. Status by product requirement (F-1…F-29)

| F | Requirement | Status | Notes |
|---|-------------|--------|-------|
| F-1..F-3 | Catalog browser (browse, detail, search) | ⬜ | WP-DCAT-8. |
| F-4 | Admin CRUD UI | ⬜ | REST admin exists; **UI** ⬜ (WP-DCAT-13). |
| F-5..F-7 | Login, OAuth (APISix/Keycloak), permissions | ⬜ | Path split only. |
| F-8 | Guided mandatory/recommended fields | ⬜ | SHACL is now available to drive it; needs the UI. |
| F-9 | Durable, lossless storage | 🟡 | File store is durable + RDF round-trips; not the mandated DB. |
| F-10 | JPA / PostgreSQL | ⬜ ⚠️ | **Conflicts with the Jena TDB2 decision** — [C1](#c1). |
| F-11 | REST CRUD | ✅ | Read + admin REST. |
| F-12 | Client auth (OAuth client-credentials) | ⬜ | Upstream PEP; not wired. Needed by the client library (WP-6). |
| F-13 | JSON **and** XML in/out | ✅ | Via the fennec codec. JSON read fixed 2026-07-14 (`DcatHelper.read` now returns a detached copy so the codec does not serialise the `RDFRoot` storage wrapper). |
| F-14 | Idempotency | ✅ | = FR-2. |
| F-15 | OpenAPI description | ⬜ | `swagger-annotations` is on the `…rest` buildpath but unused. |
| F-16 | ETag / If-Match | ✅ | = FR-7 (see the "mandatory" nuance, [C6](#c6)). |
| F-17 | Replace-only | ✅ | = FR-2b. |
| F-18/19 | RDF formats + content negotiation | ✅ | = FR-8. All RDF syntaxes on read; XML/JSON/RDF-XML on write, by design. |
| F-20 | DCAT-AP.de 3.0 compliance | 🟡 | Model is DCAT-AP.de v3; conformance now *checkable* via SHACL, but no test corpus runs in CI. The 2026-08-18 pass corrected seven Distribution/DataService cardinalities against the spec PDF (§4.4/§4.6) and moved the Dataset-only `description` obligation into OCL, so the ecore's multiplicities now match the profile and are enforced. |
| F-21 | Input validation | ✅ | Two layers: model constraints (multiplicities + OCL, always on, no configuration, 422) and SHACL on write (422) plus dry-run; = FR-4/FR-5. The model layer also closes the silent gap where an `AnyURI` holding a non-IRI was written out as a plain literal rather than refused. UI feedback pending the UI. |
| F-22 | License-vocabulary validation | ✅ | Controlled vocabularies (license, theme, language, frequency, availability, access-rights, format, status) validated against the DCAT-AP.de authority tables, loaded from `ShapesConfig.vocabularyDirectory` and unioned with the entity at validation time. Verified against real data. |
| F-23/24 | Docker, env configuration | 🟡 | Env-driven configuration ✅ (`STORE_FOLDER`, `SHACL_SHAPES_DIR`, `SHACL_VOCAB_DIR`, `SHACL_ENFORCE`, `MODEL_VALIDATE` via the configurator, with a nested `$[env:…;default=$[prop:…]]` fallback so both container env and bndrun `-D` work). **No Dockerfile**; `config.docker/configs/config.json` is still the generated stub. |
| F-25 | Health / readiness | ✅ | `GET /health/live` and `GET /health/ready` via the Apache Felix Health Check executor servlet (`healthcheck.api` 2.0.4, `.core` 2.3.0, `.generalchecks` 3.0.8) — N21, 2026-08-10. Readiness = 5 store checks + `shacl` + `admin-write` (2026-08-18: explains the `404` when `validationService.cardinality.minimum=1` leaves the admin services unsatisfied) + a `ServicesCheck`; `httpStatusMapping` maps `WARN:200, CRITICAL:503`. Shapes status is split: *not configured* is `WARN` (validation is a documented no-op), *configured but nothing loaded* is `CRITICAL` — the misconfiguration where an operator believes the portal validates and it silently does not. |
| F-26 | CSS customization | ⬜ | No UI. |
| F-27/28 | Accessibility (AA), DE/EN i18n | ⬜ | No UI. |
| F-29 | Legal pages (Impressum/privacy) | ⬜ | No UI. |

## 4. Status by work package (WP-DCAT-1…13)

| WP | Goal | Status | Notes |
|----|------|--------|-------|
| WP-DCAT-1 | Project setup & build | 🟡 | bnd workspace, per-bundle Gradle, bundle scheme ✅; runnable `…runtime` bundle + `local.bndrun` ✅. **No build/test CI** (only `docs-pages.yml`); a full-workspace `./gradlew` build still fails resolving a `-SNAPSHOT` gecko library; README is two lines. |
| WP-DCAT-2 | DCAT-AP-3 model upgrade | ✅ | DCAT-AP.de v3 model + `DatasetSeries`/`inSeries`; validation against the official shapes is now possible (see cross-cutting). Bundle identity differs ([C3](#c3)). |
| WP-DCAT-3 | Jena persistence & EMF⇄RDF bridge | 🟡 | EMF⇄RDF **serialization** bridge ✅ (`msg.body.writer`, EMF + Jena RIOT). **Jena TDB2 store, named graphs, transactions ⬜** — still file-per-entity. Roundtrip fidelity untested. |
| WP-DCAT-4 | Admin interface (OSGi + REST) | 🟡 | CRUD + FR-9/10/11 + ETag + **SHACL (FR-4/5)** + `accessService` link ✅; bulk ingest (FR-13/14/15), full error taxonomy (FR-19), OpenAPI ⬜. Service decomposition differs ([C5](#c5)). |
| WP-DCAT-5 | Catalog delivery (formats + SPARQL) | 🟡 | Read REST + content negotiation + RIOT ✅; **SPARQL ⬜**, caching/pagination ⬜ (collection GETs return everything). |
| WP-DCAT-6 | Client library | ⬜ | Blocks Data-Atlas (WP-DA-10), Model-Atlas (WP-MA-5) and SensiNact (WP-SN-4). Freeze the contract ([C4](#c4)–[C9](#c9)) first; note the write-format restriction under FR-8. |
| WP-DCAT-7 | Endpoint/microservice awareness | 🟡 | Base URL from request; configured base URL, discovery, reachability ⬜. |
| WP-DCAT-8 | Frontend / catalog browser | ⬜ | — |
| WP-DCAT-9 | Operations & deployment | 🟡 | Runtime bundle + local/docker config bundles + env-driven config + health/readiness (F-25) ✅; Docker image, docker config content, store volume, config docs ⬜. |
| WP-DCAT-10 | Documentation | 🟡 | Dev guide, user guide, admin-API spec, this analysis, issue review ✅; README, OpenAPI, ops manual, integration guide ⬜. |
| WP-DCAT-11 | EMF editor | ⬜ | — |
| WP-DCAT-12 | OData query UI | ⬜ | — |
| WP-DCAT-13 | Admin web UI | ⬜ | — |
| Cross-cutting | QA | 🟡 | Unit + 62 OSGi integration tests ✅; **SHACL conformance in CI and a real DCAT corpus ⬜** (blocked on CI existing at all). |

---

## 5. Contradictions & divergences

Ordered by impact. Each notes what the sources say, what the code does, and a suggested
resolution. Resolving [C4](#c4)–[C9](#c9) is a prerequisite for the client library
(WP-DCAT-6), which will otherwise harden a contract the spec still contradicts.

### C1
**Storage backend: Jena TDB2 vs JPA/PostgreSQL** — ⚠️ highest impact, still open
- **Requirements F-10:** "Data storage is implemented via a **JPA-based relational database** (reference: PostgreSQL)."
- **WP §3.2 + Admin-API D5/D6:** "**Apache Jena TDB2** as the triple store (*single source of truth*)"; atomic Jena transactions; one **named graph per catalog**.
- **Code:** neither — a file-per-entity RDF/XML placeholder.
- **These are mutually exclusive architectures.** A triple store gives by-URI dedup (FR-12), named-graph replace (FR-14) and SPARQL (WP-5) natively; a JPA/relational store does not, and would need a different data model.
- **Resolution needed (decision):** pick the authoritative backend. The newer, more detailed sources (WP + Admin-API) point to **Jena TDB2**; the requirements' F-10 reads like an earlier assumption. Recommend confirming TDB2 and correcting F-10 (or explicitly recording the change).
- **Resolution**: investigate which is the best solution
- **Tracked as:** issue #32. It also gates [C7](#c7) and [C11](#c11), so decide those together.

### C2
**DCAT-AP vs DCAT-AP.de**
- Requirements + Admin-API title say **DCAT-AP.de 3.0** (German national profile); the WP overview says generic "**DCAT-AP-3** … validate against the official SHACL shapes."
- **Code:** model is DCAT-AP.**de** v3.
- **Update (2026-08-07):** in practice *both* now apply. Because Jena does not resolve `owl:imports`, the DCAT-AP.de shapes alone enforce only the DE-native constraints — the German files carry the message/severity overrides for the upstream SEMIC rules but not the rule bodies. Loading the upstream `dcat-ap-SHACL.ttl` alongside them makes the base DCAT-AP 3 rules fire too.
- **Resolution:** confirm the profile, and record which shape set a deployment is expected to load (DE-only vs. DE + SEMIC base) — it materially changes what a write is rejected for.

### C3
**Model bundle identity / package namespace**
- **Admin-API §2 / §6 OSGi sample:** model is `org.eclipse.fennec.data.atlas.dcat.model`, EMF package `org.eclipse.fennec.data.atlas.dcat.dcat.*`; WP-2 says "raise `dcat.model` from 2.x … in place."
- **Code:** separate repo/module `dcat.atlas`, bundle `org.eclipse.fennec.dcat.atlas.dcatap.de.model`, generated EMF package `dcat.*`.
- **Impact:** the published OSGi contract (imports in the spec) and the client library (WP-6) reference names that don't exist as written.
- **Resolution:** decide whether the portal owns a standalone DCAT-AP.de model (current reality) and update the spec's package/bundle references accordingly.

### C4
**Distribution: standalone collection vs dataset composition**
- **Admin-API §2 table, §5.1 ("applies uniformly to … distributions"), §6 OSGi (`getDistribution(id)`/`deleteDistribution(id)`)** treat Distribution as a top-level `/distributions` collection.
- **Admin-API FR-10 + §5.2 (`POST /datasets/{id}/distributions`)** treat it as composed under a Dataset. **The spec contradicts itself.**
- **Code:** follows FR-10 — nested `…/datasets/{id}/distributions`, no dataset-less create, no top-level `/distributions`.
- **Resolution:** update §2/§5.1/§6 to drop the standalone distribution collection (align the spec to FR-10, which the code implements).
- **Resolution**: double check with dcat specs, but distribution should be in the dataset context
- **Knock-on:** the spec's `accessService` endpoint is written as `/distributions/{id}/access-service/{serviceId}`; under the nested model it became `…/datasets/{dsId}/distributions/{distId}/access-service/{serviceId}` — implemented that way (N7, 2026-08-10), so this divergence is now shipped, not hypothetical.

### C5
**OSGi service decomposition**
- **Admin-API §6 + WP-4:** a single `CatalogAdminService` (all entities), plus `CatalogRelationService`, `CatalogIngestService`, `DcatValidationService`.
- **Code:** **per-entity** read-only + admin services (`CatalogAdminService`, `DatasetAdminService`, …); relationship methods folded **into** the per-entity admin services; `CatalogRelationService` **deleted**; `DcatValidationService` now exists ✅; no ingest service.
- **Impact:** the documented contract (and the client library that will target it) differs from what's built.
- **Resolution:** reconcile — either re-introduce the aggregate/relation/ingest service names in the spec, or update the spec to the per-entity shape. Minor naming note: the DatasetSeries components are registered as `DataSeriesAdminService`/`DataSeriesReadOnlyService` (not `DatasetSeries…`); the config PIDs match, so this is cosmetic, but it is inconsistent with the class names.

### C6
**ETag/If-Match "mandatory"**
- **Requirements F-16, Admin-API D7/FR-7/§3.5:** If-Match is **mandatory** on `PUT`/`DELETE`.
- **Code:** honored when present (→412), but **absence is allowed** (no 428 Precondition Required).
- **Resolution:** clarify "mandatory" = *server must honor if present* (current behaviour, RFC-style) vs. *client must always send it* (would add 428). Cheap to tighten if the latter.

### C7
**Membership endpoints: link-by-id vs. link-with-member-body**
- **Admin-API §5.2/§6:** `PUT /catalogs/{id}/datasets/{datasetId}` and `CatalogRelationService.linkDatasetToCatalog(catalogId, datasetId)` — **no body at all**.
- **Code:** `POST /admin/catalogs/{id}/datasets` carrying the **member** (`addDatasetToCatalog(catalogId, Dataset)`) + `DELETE …/{datasetId}`; series membership under `…/dataset-series/{id}/datasets/{datasetId}`.
- **Both satisfy FR-9** ("assigned to / removed from a catalog **without** re-sending the target resource in full") — neither re-sends the catalog, which is the resource being modified. *(An earlier revision of this document read FR-9 as requiring link-by-id and called the implementation a violation; that was wrong.)*
- **The real difference:** link-by-id presupposes the member already exists as an independently stored resource that can be pointed at; the containment/file model has no such standalone member, so the content is passed inline. Tied to [C1](#c1).
- **Resolution:** settle once the store is decided; then align verbs and paths.

### C8
**`seriesMember` vs `inSeries`**
- **Admin-API §2/§5.2** reference `seriesMember` and `/members/{datasetId}`; the model has only `Dataset.inSeries` (no back-reference on `DatasetSeries`).
- **Code:** membership via `inSeries`; path `…/dataset-series/{id}/datasets/{datasetId}`.
- **Resolution:** minor — align the spec path to `datasets` or add a derived `seriesMember` view.

### C9
**Base path / API versioning**
- **Admin-API §5:** base `{publicBaseUrl}/admin/api/v1`.
- **Code:** read `/{collection}`, admin `/admin/{collection}`, under Jersey context `/rest` (so locally `http://localhost:8085/dcat/rest/admin/catalogs`); **no `/api/v1`**.
- **Resolution:** decide whether to introduce the `/api/v1` version segment before the contract is published.

### C10
**CatalogRecord**
- **Admin-API §2** lists `CatalogRecord` (`/catalog-records`, "usually derived"); FR-20 uses `CatalogRecord.modified` for audit.
- **Code:** the class exists in the generated model but no service or endpoint uses it.
- **Resolution:** low priority (optional/derived); note as a gap.

### C11
**Cascade delete & 409-when-referenced**
- **Admin-API §5.1:** `DELETE …?cascade=true` deletes composites; `409` when deleting a referenced resource.
- **Code:** `cascade` accepted but ignored (explicit `// TODO FR-1` markers); never returns 409.
- **Resolution:** implement with the real store (referential checks are natural in a graph/relational backend).

### C12
**Dry-run endpoint shape**
- **Admin-API §5.1:** dry run via `?validate=only` on the normal write endpoints.
- **Code:** a dedicated `POST /admin/validate/{type}`, chosen to keep `text/turtle` and the other report media types off the write methods' negotiation.
- **Resolution:** minor; align the spec to the dedicated endpoint, or accept both.

---

## 6. Suggested priorities

1. **Resolve [C1](#c1) (backend)** — issue #32. It is the critical path: FR-6, FR-12,
   FR-14, [C7](#c7), [C11](#c11) and WP-DCAT-5's SPARQL all sit behind it.
2. **Then WP-DCAT-3 (the real store + named graphs)** to replace the file store, with
   roundtrip fidelity tests.
3. **Reconcile the published contracts** ([C4](#c4), [C5](#c5), [C7](#c7), [C9](#c9),
   [C12](#c12)) **before** WP-DCAT-6 (client library) locks them in — remembering that
   three other modules are blocked on that library.
4. **Close the small, unblocked gaps:** ~~`accessService` (N7 ✅)~~, OpenAPI (F-15),
   pagination, health/readiness, and CI — none of which depend on the store decision.
5. UI, auth and ops (WP-8/13, F-5/6, WP-9) follow.

> Keep this document updated as decisions land; each resolved contradiction should
> either update the corresponding spec section or be recorded in the
> `development-guide.md` change log. Issue-level tracking lives in
> `docs/DCAT-issue-tracker-review.md`.

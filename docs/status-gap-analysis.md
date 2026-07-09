# DCAT.Atlas — Status & Gap Analysis

> As of: 2026-07-09
> Purpose: reconcile **what is implemented**, **what is still open**, and **where the
> source documents contradict each other or the code**, across the three planning
> inputs. This is an internal working document; it is not the requirements spec and
> does not override it — where it flags a contradiction, the resolution is a decision
> for review.

## Sources reconciled

1. **Requirements** — `docs/opendata-portal-anforderungen.en.md` (product requirements, IDs **F-1…F-29**).
2. **Admin API spec** — `docs/opendata-portal-admin-api_EN.md` (technical/API spec, IDs **FR-1…FR-21**, REST + OSGi contract).
3. **Work packages** — the GitHub module plan (**WP-DCAT-1…13**, from `docs/opendata-portal-planung.md`).

> ⚠️ **Three numbering schemes.** `F-n` (product requirements) and `FR-n` (API spec)
> are **different** lists that overlap only partly — e.g. product **F-16** (ETag) ≈ API
> **FR-7**; product **F-10** (JPA/Postgres) conflicts with the WP/API triple-store
> decision (see [C1](#c1)). Always cite the scheme. This doc keys the detailed tables
> off the API `FR-n` and the `WP-DCAT-n`, and maps the product `F-n` separately.

**Legend:** ✅ done · 🟡 partial · ⬜ not started · ⚠️ divergence/contradiction (see §5).

---

## 1. Executive summary

- **The write side of the admin API is the most complete part.** Per-entity CRUD,
  upsert/idempotency, URI minting, RDF content negotiation, the FR-9/10/11
  relationship/composition endpoints, and ETag/If-Match conditional requests are
  implemented and tested (unit + OSGi integration).
- **The single biggest gap is persistence.** The current store is a **file-per-entity
  RDF/XML** placeholder. The target backend is **contradicted between the sources**
  (Jena TDB2 triple store vs. JPA/PostgreSQL — [C1](#c1)) and neither is built. This
  blocks the transactional (FR-6), named-graph (FR-14/D6) and by-URI-dedup (FR-12)
  guarantees, all of which assume the real store.
- **The read side is half-built:** per-resource GET with content negotiation in all
  RDF formats works; **SPARQL, pagination, and search/facets do not exist.**
- **Everything user-facing and cross-cutting is unstarted:** catalog browser (F-1/2/3),
  admin UI (F-4/8), auth (F-5/6/7, FR-21), SHACL validation (F-21/FR-4/5), OpenAPI
  (F-15), Docker/ops (F-23/24/25), client library (WP-6), i18n/a11y/legal (F-27/28/29).
- **Model:** upgraded to **DCAT-AP.de v3** (`org.eclipse.fennec.dcat.atlas.dcatap.de.model`)
  — note this is a *national profile* and a *different bundle* than the plan named
  ([C2](#c2), [C3](#c3)).

**Rough completion:** admin write-path ≈ 70%; read delivery ≈ 40%; persistence ≈ 10%
(placeholder only); everything else ≈ 0%.

---

## 2. Status by API requirement (FR-1…FR-21)

| FR | Requirement | Status | Notes |
|----|-------------|--------|-------|
| FR-1 | CRUD per entity | 🟡 | All five entities CRUD-able, but on the file store; `DELETE ?cascade` ignored and 409-when-referenced not implemented ([C11](#c11)). |
| FR-2 | Upsert / idempotency | ✅ | `PUT …/{id}` create-or-replace; repeat → same state. |
| FR-2b | Replace-only, no PATCH | ✅ | No PATCH exposed. |
| FR-3 | URI assignment / minting | ✅ | Mints UUID under the request base; adopts client `{id}`. Namespace/collision validation minimal. |
| FR-4 | SHACL validation | 🟡 | `DcatValidationService` (Jena SHACL over the official GovData v3.0 shapes, loaded from an external configured dir) built + unit-tested; on-write 422 wiring (config-gated) pending. |
| FR-5 | Dry-run validation | ✅ | `POST /admin/validate/{type}` (`ValidationResource`) returns 200 + Turtle SHACL report + `X-SHACL-Conforms` header, no write. (Deviates from spec's `?validate=only` — [C4-ish], reconcile later.) |
| FR-6 | Transactionality | ⬜ | File writes are not transactional; multi-entity ops not atomic. Needs the real store. |
| FR-7 | ETag concurrency (**mandatory**) | 🟡 | ETag on GET + after write ✅; `If-Match`→412 ✅; `If-None-Match`/304 ✅. **But not mandatory** — unconditional writes are allowed (no 428) ([C6](#c6)). |
| FR-8 | Format agnosticism | ✅ | RDF/XML, Turtle, JSON-LD, N-Triples (+N3, +JSON/XML). |
| FR-9 | Catalog membership | ✅ | `…/catalogs/{id}/{datasets,services,catalogs}` add/remove; endpoint shape differs from spec ([C7](#c7)). |
| FR-10 | Distribution composition | 🟡 | Nested `…/datasets/{id}/distributions`, no dataset-less create ✅; **`accessService` link endpoint not implemented**; contradicts the spec's top-level `/distributions` ([C4](#c4)). |
| FR-11 | Series membership | ✅ | Via `Dataset.inSeries`; spec's `seriesMember`/`/members/` differs ([C8](#c8)). |
| FR-12 | Embedded references | 🟡 | "Embedded, not top-level" ✅ (containment in the model); **by-URI dedup ⬜** — belongs to the graph store. |
| FR-13 | Graph ingest | ⬜ | No `CatalogIngestService` / `POST /ingest`. |
| FR-14 | Graph replace/delete | ⬜ | No named-graph model, no `?purge`. |
| FR-15 | Differential upsert | ⬜ | — |
| FR-16 | Public URLs unaltered | 🟡 | Stored verbatim ✅; no explicit endpoint-awareness config. |
| FR-17 | Reachability check (opt.) | ⬜ | — |
| FR-18 | Self-awareness / base URL | 🟡 | Derived from the request `UriInfo`; no configured `publicBaseUrl` (AP7). |
| FR-19 | Error taxonomy | 🟡 | 404/412 ✅; 409-referenced, 422 SHACL report, structured 400 ⬜. |
| FR-20 | Audit / provenance (opt.) | ⬜ | No `CatalogRecord` handling. |
| FR-21 | Security (opt.) | 🟡 | `/admin/**` path split prepares an APISix/Keycloak PEP; no auth wiring in-app. |

## 3. Status by product requirement (F-1…F-29)

| F | Requirement | Status | Notes |
|---|-------------|--------|-------|
| F-1..F-3 | Catalog browser (browse, detail, search) | ⬜ | WP-DCAT-8. |
| F-4 | Admin CRUD UI | ⬜ | REST admin exists; **UI** ⬜ (WP-DCAT-13). |
| F-5..F-7 | Login, OAuth (APISix/Keycloak), permissions | ⬜ | Path split only. |
| F-8 | Guided mandatory/recommended fields | ⬜ | Needs SHACL + UI. |
| F-9 | Durable, lossless storage | 🟡 | File store is durable + RDF round-trips; not the mandated DB. |
| F-10 | JPA / PostgreSQL | ⬜ ⚠️ | **Conflicts with the Jena TDB2 decision** — [C1](#c1). |
| F-11 | REST CRUD | ✅ | Read + admin REST. |
| F-12 | Client auth (OAuth client-credentials) | ⬜ | Upstream PEP; not wired. |
| F-13 | JSON **and** XML in/out | ✅ | Via the fennec codec. |
| F-14 | Idempotency | ✅ | = FR-2. |
| F-15 | OpenAPI description | ⬜ | — |
| F-16 | ETag / If-Match | ✅ | = FR-7 (see the "mandatory" nuance, [C6](#c6)). |
| F-17 | Replace-only | ✅ | = FR-2b. |
| F-18/19 | RDF formats + content negotiation | ✅ | = FR-8. |
| F-20 | DCAT-AP.de 3.0 compliance | 🟡 | Model is DCAT-AP.de v3; field-level compliance unvalidated. |
| F-21 | Input validation | 🟡 | SHACL validation service done (= FR-4); UI/REST feedback wiring pending. |
| F-22 | License-vocabulary validation | ⬜ | — |
| F-23/24/25 | Docker, env config, health/readiness | ⬜ | No Dockerfile; `STORE_FOLDER` env used in tests only; no health endpoints. |
| F-26 | CSS customization | ⬜ | No UI. |
| F-27/28 | Accessibility (AA), DE/EN i18n | ⬜ | No UI. |
| F-29 | Legal pages (Impressum/privacy) | ⬜ | No UI. |

## 4. Status by work package (WP-DCAT-1…13)

| WP | Goal | Status | Notes |
|----|------|--------|-------|
| WP-DCAT-1 | Project setup & build | 🟡 | bnd workspace, per-bundle Gradle, bundle scheme ✅; **no build/test CI** (only a docs-pages workflow); no production standalone bndrun (only `rest.tests/test.bndrun`). |
| WP-DCAT-2 | DCAT-AP-3 model upgrade | ✅ | DCAT-AP.de v3 model + `DatasetSeries`/`inSeries`; official-SHACL model validation ⬜. Bundle identity differs ([C3](#c3)). |
| WP-DCAT-3 | Jena persistence & EMF⇄RDF bridge | 🟡 | EMF⇄RDF **serialization** bridge ✅ (`msg.body.writer`, EMF + Jena RIOT). **Jena TDB2 store, named graphs, transactions ⬜** — the store is file-per-entity. |
| WP-DCAT-4 | Admin interface (OSGi + REST) | 🟡 | CRUD + FR-9/10/11 + ETag ✅; SHACL (FR-4/5), bulk ingest (FR-13/14/15), full error taxonomy (FR-19), OpenAPI ⬜. Service decomposition differs ([C5](#c5)). |
| WP-DCAT-5 | Catalog delivery (formats + SPARQL) | 🟡 | Read REST + content negotiation + RIOT ✅; **SPARQL ⬜**, caching/pagination ⬜. |
| WP-DCAT-6 | Client library | ⬜ | Blocks Data-/Model-Atlas/SensiNact integration. |
| WP-DCAT-7 | Endpoint/microservice awareness | 🟡 | Base URL from request; configured base URL, discovery, reachability ⬜. |
| WP-DCAT-8 | Frontend / catalog browser | ⬜ | — |
| WP-DCAT-9 | Operations & deployment | 🟡 | Test bndrun ✅; Docker, prod bndrun, config docs ⬜. |
| WP-DCAT-10 | Documentation | 🟡 | Dev guide, user guide, admin-API spec ✅; OpenAPI, ops manual, integration guide ⬜. |
| WP-DCAT-11 | EMF editor | ⬜ | — |
| WP-DCAT-12 | OData query UI | ⬜ | — |
| WP-DCAT-13 | Admin web UI | ⬜ | — |
| Cross-cutting | QA | 🟡 | Unit + OSGi integration tests ✅; SHACL-in-CI + real DCAT corpus ⬜. |

---

## 5. Contradictions & divergences

Ordered by impact. Each notes what the sources say, what the code does, and a suggested resolution.

### C1
**Storage backend: Jena TDB2 vs JPA/PostgreSQL** — ⚠️ highest impact
- **Requirements F-10:** "Data storage is implemented via a **JPA-based relational database** (reference: PostgreSQL)."
- **WP §3.2 + Admin-API D5/D6:** "**Apache Jena TDB2** as the triple store (*single source of truth*)"; atomic Jena transactions; one **named graph per catalog**.
- **Code:** neither — a file-per-entity RDF/XML placeholder.
- **These are mutually exclusive architectures.** A triple store gives by-URI dedup (FR-12), named-graph replace (FR-14) and SPARQL (WP-5) natively; a JPA/relational store does not, and would need a different data model.
- **Resolution needed (decision):** pick the authoritative backend. The newer, more detailed sources (WP + Admin-API) point to **Jena TDB2**; the requirements' F-10 reads like an earlier assumption. Recommend confirming TDB2 and correcting F-10 (or explicitly recording the change).
- **Resolution**: investigate which is the best solution

### C2
**DCAT-AP vs DCAT-AP.de**
- Requirements + Admin-API title say **DCAT-AP.de 3.0** (German national profile); the WP overview says generic "**DCAT-AP-3** … validate against the official SHACL shapes."
- **Code:** model is DCAT-AP.**de** v3.
- **Resolution:** confirm the profile and, for FR-4/F-21, **which SHACL shapes** (DCAT-AP.de vs. base DCAT-AP 3) are authoritative. Affects validation and F-20 compliance claims.

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

### C5
**OSGi service decomposition**
- **Admin-API §6 + WP-4:** a single `CatalogAdminService` (all entities), plus `CatalogRelationService`, `CatalogIngestService`, `DcatValidationService`.
- **Code:** **per-entity** read-only + admin services (`CatalogAdminService`, `DatasetAdminService`, …); relationship methods folded **into** the per-entity admin services; `CatalogRelationService` **deleted**; no ingest/validation services yet.
- **Impact:** the documented contract (and the client library that will target it) differs from what's built.
- **Resolution:** reconcile — either re-introduce the aggregate/relation/ingest/validation service names in the spec, or update the spec to the per-entity shape.

### C6
**ETag/If-Match "mandatory"**
- **Requirements F-16, Admin-API D7/FR-7/§3.5:** If-Match is **mandatory** on `PUT`/`DELETE`.
- **Code:** honored when present (→412), but **absence is allowed** (no 428 Precondition Required).
- **Resolution:** clarify "mandatory" = *server must honor if present* (current behaviour, RFC-style) vs. *client must always send it* (would add 428). Cheap to tighten if the latter.

### C7
**Relationship endpoint verbs/paths/bodies**
- **Admin-API §5.2:** `PUT /catalogs/{id}/datasets/{datasetId}` (link **by id**, no body); membership under `/dataset-series/{id}/members/{datasetId}`; a `…/distributions/{id}/access-service/{serviceId}` endpoint.
- **Code:** `POST /admin/catalogs/{id}/datasets` (**with the full member body**) + `DELETE …/{datasetId}`; series membership under `…/dataset-series/{id}/datasets/{datasetId}`; **no `accessService` endpoint**.
- **Root cause:** the spec's link-**by-id** assumes members are independent stored resources (a graph store); our containment/file model passes the full member. Tied to [C1](#c1).
- **Resolution:** settle once the store is decided; then align verbs/paths. Note FR-9's wording ("without re-sending the target") is better matched by link-by-id.

### C8
**`seriesMember` vs `inSeries`**
- **Admin-API §2/§5.2** reference `seriesMember` and `/members/{datasetId}`; the model has only `Dataset.inSeries` (no back-reference on `DatasetSeries`).
- **Code:** membership via `inSeries`; path `…/dataset-series/{id}/datasets/{datasetId}`.
- **Resolution:** minor — align the spec path to `datasets` or add a derived `seriesMember` view.

### C9
**Base path / API versioning**
- **Admin-API §5:** base `{publicBaseUrl}/admin/api/v1`.
- **Code:** read `/{collection}`, admin `/admin/{collection}`, under Jersey context `/rest`; **no `/api/v1`**.
- **Resolution:** decide whether to introduce the `/api/v1` version segment before the contract is published.

### C10
**CatalogRecord**
- **Admin-API §2** lists `CatalogRecord` (`/catalog-records`, "usually derived"); FR-20 uses `CatalogRecord.modified` for audit.
- **Code:** no `CatalogRecord` service/endpoint.
- **Resolution:** low priority (optional/derived); note as a gap.

### C11
**Cascade delete & 409-when-referenced**
- **Admin-API §5.1:** `DELETE …?cascade=true` deletes composites; `409` when deleting a referenced resource.
- **Code:** `cascade` accepted but ignored; never returns 409.
- **Resolution:** implement with the real store (referential checks are natural in a graph/relational backend).

---

## 6. Suggested priorities

1. **Resolve [C1](#c1) (backend) and [C2](#c2) (profile/SHACL) first** — they gate WP-3/4/5 and FR-4/6/12/14. Everything durable depends on this decision.
2. **Then WP-DCAT-3 (Jena TDB2 store + named graphs)** to replace the file store; this unlocks FR-6 transactionality, FR-12 dedup, FR-14 graph ops, and WP-5 SPARQL.
3. **SHACL validation (FR-4/5, F-21)** — needed for FR-19's 422 report and F-8 guided editing.
4. **Reconcile the published contracts** ([C4](#c4), [C5](#c5), [C7](#c7), [C9](#c9)) before WP-6 (client library) locks them in.
5. UI/auth/ops (WP-8/13, F-5/6, WP-9) and OpenAPI (F-15) follow.

> Keep this document updated as decisions land; each resolved contradiction should
> either update the corresponding spec section or be recorded in the
> `development-guide.md` change log.

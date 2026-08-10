# DCAT — Issue Tracker Review

> As of: 2026-08-07
> Board: <https://github.com/DataInMotion/xdp/issues> (issue snapshot: `docs/DCAT-github-issues.md`)
> Purpose: reconcile the 13 DCAT issues on the board against **what is actually in
> `/opt/git/dcat.atlas`**, decide which are genuinely open, and sketch the issues still
> missing to cover work packages **WP-DCAT-1…13**.
>
> Companion documents: `docs/status-gap-analysis.md` (FR/F-level status, contradictions
> C1…C12 — refreshed 2026-08-07) and `docs/development-guide.md` (change log).

**Legend:** ✅ delivered · 🟡 partially delivered · ⬜ not started

---

## 1. Summary

- **All 7 closed issues are genuinely closed** in the sense that the thing they asked
  for exists and is tested. The one piece of untracked **residue** was that #29 never
  delivered the `accessService` link endpoint (FR-10) — see §2. **Closed 2026-08-10 as N7**,
  which also corrected two model deviations from DCAT-AP.de §4.6.24 and surfaced **N28**.
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
| 2 | WP-DCAT-1 — initial gradle setup | 🟡 closed early | bnd workspace + per-bundle Gradle + `cnf` ✅; runnable `…runtime` bundle + `local.bndrun` ✅. WP-DCAT-1 also asks for **CI (build + tests)**, which was missing (`.github/workflows/` held only `docs-pages.yml`) and a clean `./gradlew build` failed at `rest.tests:resolve.test` (a bnd `-dependson` task-wiring gap). Both **fixed 2026-08-10 via N1**: CI now verifies every branch and PR on JDK 21 + 25 with the OSGi tests, gated by the Eclipse header check. |
| 3 | Use Apache Jena for N3, Turtle, JSON-LD | ✅ correctly closed | `…msg.body.writer`: `TurtleMessageBodyWriter`, `N3MessageBodyWriter`, `JsonLdMessageBodyWriter`, `NTriplesMessageBodyWriter`, `RdfXmlMessageBodyWriter` + `RdfXmlMessageBodyReader`, all over `EObjectRDFModelBuilder`. |
| 4 | OSGi-fied Jena deps → gecko libraries | ✅ correctly closed | The OSGi Jena 6.1.0 bundles (SPI-Fly `osgi.serviceloader` capabilities) are published under the `org.geckoprojects.libraries` groupId and consumed through the `geckoLibraries` `-library` (`cnf/ext/libraries.bnd`), resolved from Maven Central + the publicly readable DIM Nexus. **No local publish step is required** and `cnf/local` is empty — an earlier arrangement did need one, so that stale instruction is worth retiring. Verified 2026-08-07. |
| 9 | AP-DCAT-1 — repo + documentation setup | 🟡 closed early | `docs/` (dev guide, user guide, admin-API spec DE/EN, requirements, planning) + `docs-site/` ✅. `README.md` is two lines. → **N22**. |
| 29 | WP-DCAT-4 — relationship/membership endpoints | 🟡 closed with residue | FR-9 ✅ (`…/admin/catalogs/{id}/{datasets,services,catalogs}`), FR-11 ✅ (via `Dataset.inSeries`, `…/admin/dataset-series/{id}/datasets/{dsId}`), FR-10 ✅ *for composition* (`…/admin/datasets/{id}/distributions`, no dataset-less create). ~~**Not delivered: the `accessService` link**~~ — was missing entirely from `api`/`impl`/`rest`; **delivered 2026-08-10 via N7** (`PUT`/`DELETE …/distributions/{distId}/access-service/{serviceId}`). Residue now cleared. |
| 30 | WP-DCAT-4 — SHACL input validation | ✅ correctly closed | FR-5 dry-run: `ValidationResource` → `POST /admin/validate/{type}` returns the native Jena `ValidationReport` negotiated across Turtle/JSON-LD/RDF-XML/N3/NT + `X-SHACL-Conforms`. FR-4 on-write: `helper/WriteValidation` → 422 + report before persist, config-gated via `ShapesConfig.enforceOnWrite`, MUSS(`sh:Violation`) blocks / SOLL(`sh:Warning`) does not. Plus controlled-vocabulary validation (F-22) via `ShapesConfig.vocabularyDirectory`. Integration-tested. **Two operational caveats, neither tracked:** the GovData shapes are AGPLv3 so they cannot be vendored and must be supplied by the operator, and `owl:imports` are inert — without the upstream `dcat-ap-SHACL.ttl` and the EU/GovData authority tables in those directories, base DCAT-AP rules silently do not fire. → **N27**. |

Also worth fixing in `docs/DCAT-github-issues.md`: the #30 entry has a duplicated
heading marker (`### ### [DCAT] …`).

### 2.2 Open issues

| # | Title | Verdict |
|---|-------|---------|
| 5 | WP-DCAT-4 — CRUD Operations for DCAT | **Effectively done — narrow or close.** All five entities (Catalog, Dataset, DatasetSeries, DataService, Distribution) have read-only + admin OSGi services (`…api` / `…impl`) and REST resources (`…rest`), XML/JSON in and all RDF formats out, upsert/idempotency, ETag/If-Match, SHACL enforcement, 69 green OSGi integration tests. The issue text's remaining clause is "*dcat data should be persisted, either via fennec persistence or file-based*" — file-based **is** implemented (`impl/helper/DcatHelper`, one `<id>.rdf` per resource), and the upgrade path is exactly sub-issue #32. Recommendation: **close #5**, let #32 carry persistence, and open the residual FR-4-scope items as their own issues (**N7**, **N8**, **N9**, **N10**). |
| 32 | WP-DCAT-3 — Persistence of DCAT objects | **Genuinely open and the single highest-priority item.** Nothing in the product code references TDB2, a dataset, or transactions (`grep -ril 'tdb2\|fuseki\|QueryExecution'` over `src` → no hits; the only "sparql" matches are string literals in tests). This is also decision **C1** — requirements F-10 say JPA/PostgreSQL, WP §3.2 and admin-API D5/D6 say Jena TDB2, and the code is neither. Recommendation: make #32 explicitly a **decision + ADR** issue with the implementation split into **N4/N5/N6**, because it blocks FR-6 (transactions), FR-12 (by-URI dedup), FR-14 (named-graph replace), WP-DCAT-5 (SPARQL) and FR-1's 409-when-referenced. |
| 7 | WP-DCAT-06 — Client library | **Genuinely open, untouched.** No client bundle exists. Blocker for WP-DA-10, WP-MA-5 and WP-SN-4 — i.e. three other modules are waiting on it. Recommendation: do **N11** (freeze the contract) before or alongside it, otherwise the client library locks in an API the spec still contradicts. |
| 8 | Convert Ecore into RDF | **Genuinely open, untouched**, and correctly marked low priority. Note it overlaps WP-MA-5 (Model-Atlas registering schemas as DataService/Distribution) — worth a cross-link so it is not solved twice. |

---

## 3. Proposed new issues

Ready to paste. For each: the **bold line is the issue title**, and everything from there
to the horizontal rule is the issue body. "Now" = create on the board immediately
(near-term critical path or already-identified residue); "Later" = create when the
preceding WP starts, so the board stays readable.

### WP-DCAT-1 — build & CI

#### N1 · *Now*

**[DCAT] WP-DCAT-1 — CI pipeline for build and tests**

`.github/workflows/` only has `docs-pages.yml`; nothing builds or tests on push/PR. Add a workflow that builds all bundles and runs unit + OSGi integration tests.

Dependencies resolve remotely (Maven Central + the publicly readable DIM Nexus, via the `geckoLibraries` `-library`), so CI needs network access but no credentials and no local publish step.

**Known blocker, diagnosed 2026-08-07.** `:org.eclipse.fennec.dcat.atlas.rest.tests:resolve.test` fails from a clean state with:

```
⇒ Bundle: org.eclipse.fennec.dcat.atlas.validation cannot be resolved
   osgi.identity: (osgi.identity=org.eclipse.fennec.dcat.atlas.validation)
```

Cause: `test.bndrun` requires `bnd.identity;id='org.eclipse.fennec.dcat.atlas.validation'` in `-runrequires`, but that project is on no other bundle's `-buildpath`, so gradle never pulls it into the task graph and `…validation/generated/` holds no jar when the resolve runs. bndtools always sees workspace projects, which is why resolving `test.bndrun` in the IDE succeeds — this is a gradle task-wiring gap, not a repository or dependency problem. It passes as soon as the jar exists (verified: `./gradlew :…validation:jar :…rest:jar :…rest.tests:resolve.test` → BUILD SUCCESSFUL). Fix by making the resolve task depend on that project's `jar`, and beware that a green local run may only reflect a stale artifact from an earlier invocation — CI must prove it from clean.

Remaining reproducibility risk: `cnf/ext/libraries.maven` pins `org.gecko.libraries.workspace.library:1.0.0-SNAPSHOT`, so the build can change under you when the snapshot moves. (The second entry, `org.gecko.emf.util.jakartars.bnd.library.workspace`, was unused and removed on 2026-08-07.)

Also add the Eclipse compliance-header check.

*Evidence:* green build + test run on a PR, from a clean checkout.

---

### WP-DCAT-3 — persistence (all depend on #32)

#### N4 · *Later (after the #32 decision)*

**[DCAT] WP-DCAT-3 — Named-graph layout and transactional writes**

One named graph per catalog (admin-API D6); all write operations atomic (FR-6), including the multi-entity membership operations. Replaces the current non-atomic file-per-entity writes.

---

#### N5 · *Later*

**[DCAT] WP-DCAT-3 — EMF⇄RDF roundtrip fidelity tests**

WP-DCAT-3 lists roundtrip fidelity as its evidence and it is currently untested: blank nodes, language tags, typed literals, and the `rdf:resource` vs. nested-object distinction must survive EMF → RDF → EMF.

---

#### N6 · *Later*

**[DCAT] WP-DCAT-3 — By-URI deduplication of embedded references (FR-12)**

An entity referenced from two catalogs must be stored once and resolved by URI, not duplicated per containment tree. Natural in a triple store, needs explicit design in a relational one — hence blocked on #32.

---

### WP-DCAT-4 — admin interface (residue from #5/#29/#30)

#### N7 · ✅ *Done (2026-08-10)*

**[DCAT] WP-DCAT-4 — `accessService` link endpoint on Distribution (FR-10)**

FR-10 requires `Distribution.accessService` to optionally reference a DataService. Issue #29 was closed without it — `getAccessService()` exists on the generated `Distribution` class, but nothing in `api`/`impl`/`rest` references it, so there is no way to set the link through the API.

Add the link/unlink endpoint under the nested distribution path (`…/admin/datasets/{dsId}/distributions/{distId}/access-service/{serviceId}`), ETag-guarded on the distribution, idempotent on re-add, consistent with the FR-9/FR-11 membership endpoints.

**Delivered.** `PUT`/`DELETE …/admin/datasets/{dsId}/distributions/{distId}/access-service/{serviceId}`, plus `DistributionAdminService.addAccessServiceToDistribution` / `deleteAccessServiceFromDistribution`. 7 unit + 7 OSGi integration tests (69 integration tests total, green on JDK 21 and 25).

Two model corrections were needed first, both checked against DCAT-AP.de 3.0 §4.6.24:

- `accessService` had `lowerBound="1"`, i.e. *required*, while the spec's Verbindlichkeit is **Optional** → now 0.
- It was direct containment of `#//DataService`, which would have embedded a *copy* of the service in every distribution referencing it. Changed to containment of `rdf.ecore#//Resource` — the URI-pointer wrapper `Dataset.distribution` already uses — so it serializes as `<dcat:accessService rdf:resource="…"/>` and the service stays a single catalog entity. The spec supports this reading: §4.6.24 says the property *"verweist auf"* the service, and the `dcat:DataService` class is *"eingebunden über"* `dcat:service`, `dcat:accessService` and `foaf:primaryTopic`. Containment vs reference is invisible to the spec, which constrains the RDF graph and not the XML syntax.

Semantics as implemented: the target must exist as a DataService resource, but **catalog membership is deliberately not required** — per the "eingebunden über" list, a service bound only from a distribution is legitimate. Unlinking never deletes the service.

Follow-up: **N28** (the same embedded-copy problem still applies to `Catalog.service` and friends).

---

#### N8 · *Later (needs #32)*

**[DCAT] WP-DCAT-4 — Error taxonomy: cascade delete, 409-when-referenced, structured 400 (FR-19)**

`deleteX(id, cascade)` currently accepts `cascade` and ignores it — there are literal `// TODO FR-1` markers in all four admin impls — and 409 is never returned. Implement referential checks and cascade semantics once the real store lands, plus the structured 400 error body from admin-API §6.

---

#### N9 · *Later (needs #32)*

**[DCAT] WP-DCAT-4 — Bulk graph ingest (FR-13/14/15)**

`CatalogIngestService` + `POST /ingest`: ingest a whole DCAT graph, named-graph replace/delete with `?purge`, and differential upsert. Nothing exists today.

---

#### N10 · *Now*

**[DCAT] WP-DCAT-4 — OpenAPI descriptor for the admin and read API (F-15)**

`io.swagger.core.v3.swagger-annotations` is already on the `…rest` buildpath but no resource uses it. Annotate the resources and serve the descriptor.

Prerequisite for the admin UI (WP-DCAT-13) and useful documentation for the client library (#7). Remember to document the write-format restriction: the Jena RDF syntaxes are read-only, writes accept `application/json`, `application/xml` and `application/rdf+xml`.

---

#### N11 · *Now (blocks #7)*

**[DCAT] WP-DCAT-4 — Reconcile the admin-API spec with the implementation**

`docs/status-gap-analysis.md` records contradictions C4…C9 between `opendata-portal-admin-api_EN.md` and the code:

- top-level `/distributions` vs. nested under Dataset (the spec contradicts itself);
- aggregate `CatalogAdminService`/`CatalogRelationService` vs. the per-entity services actually built;
- link-by-id vs. link-with-member-body on the membership endpoints;
- `seriesMember` vs. `inSeries`;
- the missing `/api/v1` base path.

On the membership shape specifically: both designs satisfy FR-9 — neither re-sends the catalog, which is the resource being modified. But the spec's `PUT /catalogs/{id}/datasets/{datasetId}` (and `CatalogRelationService.linkDatasetToCatalog(catalogId, datasetId)` in §6) sends no body at all and therefore presupposes the dataset already exists as an independently stored resource, whereas the code sends the member's content and embeds it as containment. That is a consequence of C1 (file store vs. triple store), so decide it together with #32.

Also settle C6: is If-Match "mandatory" = honour if present (today's behaviour) or reject without it (adds 428)?

Freeze all of this before the client library (#7) hardens the contract.

---

#### N12 · *Later, low*

**[DCAT] WP-DCAT-4 — CatalogRecord / audit provenance (FR-20, optional)**

The generated model has `CatalogRecord` but no service or endpoint uses it. Optional per the spec.

---

### WP-DCAT-5 — catalog delivery (no issue exists today)

#### N13 · *Later (needs #32)*

**[DCAT] WP-DCAT-5 — SPARQL endpoint**

ARQ over the store, with the Fuseki option evaluated. Core WP-DCAT-5 deliverable and a prerequisite for the browser's search/facets (N19).

---

#### N14 · *Now*

**[DCAT] WP-DCAT-5 — Pagination and caching on the read collections**

`GET /{collection}` currently loads and serialises every resource (`CatalogReadOnlyResource.listCatalogs` and its four siblings). Add pagination and cache headers before the catalog grows.

---

#### N15 · *Later*

**[DCAT] WP-DCAT-5 — Dereferenceable resource URIs: harden and decide the open policies**

🟡 **The base mechanism already exists** — this issue is verification plus three policy
decisions, not a build from scratch. On create, `CatalogAdminResource.createCatalog`
mints `about = {base}/catalogs/{id}` via `readUri`; on upsert the same URI is forced onto
the payload regardless of what the client sent (D1/D2). That URL *is*
`CatalogReadOnlyResource`'s `GET /{id}`, which negotiates JSON, XML, RDF/XML, Turtle,
N-Triples, JSON-LD and N3, with `Vary: Accept` and the ETag stamped by
`DcatConditionalFilter`. Same pattern across all five collections.

What is actually left:

1. **Correctness behind a proxy — blocks everything else.** The base comes from
   `uriInfo.getBaseUriBuilder()`, i.e. the *inbound* request. Behind APISIX/nginx/Traefik/
   an Ingress that is the internal hop (`http://dcat-atlas:8080/…`), so host, scheme
   (TLS terminated at the proxy), port and a stripped path prefix can each be wrong.
   Fixed by the configured `publicBaseUrl` in **N16**; until then a minted `about` is
   unresolvable from outside.
2. **Re-minting when the base changes.** `about` is persisted verbatim, so any base change
   leaves stale URIs on disk: adopting `publicBaseUrl` on an existing store, a later domain
   or scheme change, promoting/restoring a store across environments, or ingesting another
   instance's dump. Mechanically cheap — the id is the last segment and `DcatHelper.idOf`
   already keys off it — but the rewrite must reach nested references (`dataset`/`service`/
   `catalog` links, `inSeries`, distribution `about`s), not just the top-level field.
   **Policy question:** in RDF the URI *is* the identity, so a re-stamp breaks every external
   consumer that harvested the old URI. Decide between a hard rename and keeping the old URI
   resolvable (`owl:sameAs` or a redirect). *(Storing relative and rendering absolute at read
   time is not a fix — it hides the same identity change behind the current config.)*
3. **`text/html` representation.** `Accept: text/html` returns 406 today. Full linked-data
   dereferenceability expects an HTML view alongside the RDF, which is the browser
   (**N17**/**N18**). Sub-decisions: one URI with `Vary: Accept` vs. distinct URIs behind a
   303, and whether the HTML embeds schema.org JSON-LD (the prerequisite for Google Dataset
   Search and similar crawlers). Either scope this issue to RDF only and defer HTML to
   WP-DCAT-8, or declare the dependency.
4. **Distribution identity is parent-derived.** `about` is
   `{base}/datasets/{datasetId}/distributions/{id}`, so re-parenting a distribution or
   re-creating its dataset under a new id silently changes the distribution's URI, and two
   datasets referencing one distribution cannot share a URI. Note that URI shape and creation
   route are separable: FR-10's composition rule (no dataset-less create) can stay exactly as
   it is while `about` is minted flat as `{base}/distributions/{id}`. **Decide:** does a
   distribution have standalone identity?

Pairs with **N16** (which point 1 depends on entirely) and **N17**/**N18** for point 3.

---

### WP-DCAT-7 — endpoint awareness (no issue exists today)

#### N16 · *Later*

**[DCAT] WP-DCAT-7 — Public base URL and endpoint self-awareness (FR-16/17/18)**

The base URI is derived from the incoming request `UriInfo`; there is no configured `publicBaseUrl`, so URIs minted behind a proxy or in a container are wrong. Add configuration, internal→public resolution, and the optional reachability health check of distribution `accessURL`s.

---

### WP-DCAT-8 — catalog browser (no issue exists today)

#### N17 · *Later*

**[DCAT] WP-DCAT-8 — UI stack decision and scaffold**

Decision issue: pick the stack (shared with the admin UI, WP-DCAT-13) and stand up the scaffold. Record as an ADR.

---

#### N18 · *Later*

**[DCAT] WP-DCAT-8 — Catalog overview, dataset/series detail, distribution views**

Includes the "view source" format toggle, responsive layout, WCAG AA basics (F-27), DE/EN i18n (F-28), CSS customisation hook (F-26) and the legal pages (F-29) — those product requirements have no other home.

---

#### N19 · *Later (needs N13)*

**[DCAT] WP-DCAT-8 — Search and facets over SPARQL**

Faceted search across the catalog (theme, publisher, format, license) driven by SPARQL queries against the store.

---

### WP-DCAT-9 — operations & deployment (no issue exists today)

#### N20 · *Now*

**[DCAT] WP-DCAT-9 — Docker image and container configuration**

There is no Dockerfile, and `org.eclipse.fennec.dcat.atlas.config.docker/configs/config.json` is still the generated stub (`"ExampleConfig": {"your.prop.here": "text"}`).

Build a container over the `…runtime` bundle, fill the docker config bundle (env-driven: store location, shapes/vocabulary directories, `SHACL_ENFORCE`, public base URL), and mount a volume for the store (F-23/F-24).

---

#### N21 · ✅ *Done (2026-08-10)*

**[DCAT] WP-DCAT-9 — Health and readiness endpoints (F-25)**

No health/readiness endpoint exists. Needed for the container orchestrator. Readiness should reflect store availability and whether the SHACL shapes actually loaded — a portal running with no shapes silently validates nothing.

**Delivered.** `GET /health` (liveness — checks no dependencies, because a failing liveness probe means "restart me" and a missing store is not fixed by a restart) and `GET /ready` (readiness — 200 when every contributor is ready, else 503 with a per-check JSON body). Readiness aggregates `DcatHealthContributor` services, so a subsystem that appears later registers one and the endpoint needs no change; the Phase 1 SPARQL graph will use exactly that. 6 + 4 unit and 5 integration tests (74 integration tests total).

Two decisions worth recording:

- **Split shapes status.** Shapes are operator-supplied deployment input, and running without them is a documented no-op — so *not configured* is ready, reported as a warning, while *configured but nothing loaded* (bad path, no `*.ttl`) is **not ready**. Failing readiness for the first case would break every deployment that deliberately runs unvalidated, including the integration tests; not failing the second leaves the silent-no-validation trap this issue was filed about. Known limitation: unparseable shapes make the validation component fail activation outright, so the service — and its contributor — is simply absent rather than reporting unready.
- **A missing store directory is ready.** Stores are created lazily (`DcatHelper.write` does not mkdir; EMF's file URI handler creates the parent on first save), so a fresh install has no store directories at all. Readiness therefore accepts "absent but creatable" and only fails when the path exists and is not a usable directory, or cannot be created. Not-writable is also ready — a read-only mount is legitimate for a read runtime — and is reported in the detail text.

---

### WP-DCAT-10 — documentation

#### N22 · *Later*

**[DCAT] WP-DCAT-10 — README, operations manual and integration guide**

`README.md` is two lines. Needs: architecture overview, the ops manual, the integration guide for Data-Atlas / Model-Atlas / SensiNact (pairs with #7), and the generated OpenAPI reference (N10). End-user docs for the browser and SPARQL follow WP-DCAT-8.

---

### WP-DCAT-11 / 12 / 13 — editors and UIs

#### N23 · *Later*

**[DCAT] WP-DCAT-11 — EMF editor for the DCAT-AP.de 3 model**

Adapt the generated edit/editor bundles: labels, icons, cleaned-up property views, guided creation, validation feedback.

---

#### N24 · *Later (blocked on N17/N18 + WP-OD-2)*

**[DCAT] WP-DCAT-12 — OData query UI in the catalog browser**

Detect OData endpoints on DataService/Distribution, discover `$metadata` (EDMX), and offer an interactive read-only query builder with a result preview and a copyable query URL.

---

#### N25 · *Later (needs N10, N17)*

**[DCAT] WP-DCAT-13 — Admin web UI**

Form-based CRUD for all five entities including memberships, PUT-upsert with If-Match conflict handling (412 dialog), and SHACL feedback before saving.

---

#### N26 · *Later*

**[DCAT] WP-DCAT-13 — Authentication and authorisation wiring (F-5/6/7/12, FR-21)**

The `/admin/**` vs. `/{collection}` path split already prepares an APISix/Keycloak PEP, but nothing is wired: no login, no OAuth client-credentials for machine clients (which the client library #7 will need), no roles.

**Mostly infrastructure, but not entirely.** F-6 and F-12 say explicitly that enforcement is
handled upstream (APISix as PEP, Keycloak as PDP), so the backend needs no token validation,
no login endpoint and no OAuth dance. Four items still land outside APISix config:

1. **F-7 permission granularity — the decision this issue must force.** F-7 requires
   configuring "who is allowed to create, change, or delete **which objects**", and the
   requirements note the role/permission structure is *still to be defined*. A gateway
   matches path, method and role claim; it cannot know that a given catalog belongs to
   publisher A. If the model is coarse (role `editor` writes anything under `/admin/**`),
   APISix covers F-7 completely and there is no backend work. If it is per-object or
   per-publisher ownership, only the backend can enforce it. **Decide this first — it
   determines whether the rest of the issue has any code in it.**
2. **FR-20 author.** Logging changes with timestamp *and author* requires the backend to
   consume the identity APISix forwards (JWT claims or headers) — not validate it. Nothing
   reads such a header today.
3. **Direct access is unauthenticated by design.** Anything that can reach the port can
   write: in-cluster traffic, a port-forward, a second ingress, a misconfigured route.
   Normally mitigated by network policy/mTLS rather than app code, but state it as an
   explicit deployment constraint — *the backend MUST NOT be routable except through the
   PEP* — rather than leaving it implicit (pairs with **N20**).
4. **Error-shape consistency.** APISix returns its own 401/403 bodies, which will not match
   the API's negotiated error format — the client library (#7) sees a different shape for
   auth failures than for everything else. Decide whether that is acceptable.

Trusted-proxy handling is shared with **N16**: if the gateway forwards identity headers *and*
`X-Forwarded-*` for the public base URL, both must be trusted only from the gateway and
stripped from direct client requests — one configuration serving both concerns.

---

### Cross-cutting

#### N27 · *Now*

**[DCAT] Cross-cutting — SHACL conformance in CI, test corpus, and shapes provisioning**

Three linked gaps left over from #30:

1. The official GovData DCAT-AP.de shapes are AGPL-3.0 and cannot be vendored, so document and ideally automate the operator-side download of both the shapes and the `owl:imports` reference data (EU/GovData authority tables).
2. Because those imports are inert in Jena, the upstream `dcat-ap-SHACL.ttl` must be present or the base DCAT-AP rules silently never fire. This needs a startup warning or a readiness signal (see N21).
3. Build a corpus of real DCAT examples and run SHACL conformance in CI (needs N1).

---

#### N28 · *Later (after N7; a real migration, not a rename)*

**[DCAT] Cross-cutting — membership references embed copies instead of pointing at the entity**

Surfaced while doing **N7**. The model links first-class entities in two incompatible ways, and most of them embed a full copy:

| Feature | eType | Effect |
|---|---|---|
| `Dataset.distribution` | `rdf.ecore#//Resource` | ✅ URI pointer |
| `Distribution.accessService` | `rdf.ecore#//Resource` | ✅ URI pointer (changed by N7) |
| `Catalog.service` | `#//DataService` | ❌ embedded copy |
| `Catalog.dataset` | `#//DatasetContainer` → `Dataset` | ❌ embedded copy |
| `Catalog.catalog` | `#//Catalog` | ❌ embedded copy |
| `Dataset.inSeries` | `#//DatasetSeries` | ❌ embedded copy |

Every entity involved is *also* stored standalone by its own admin service (`DcatHelper.write(…, DCATAP_ROOT__*, …)`, one `<id>.rdf` each), so the embedded copies are duplicates that drift. Concretely, after cataloguing a DataService and linking it to a distribution:

- `PUT /admin/data-services/{id}` updates the standalone resource;
- the distribution's `accessService` pointer resolves to the new state;
- the catalog's embedded copy under `dcat:service` is now **stale**.

A merged RDF graph then contains two different descriptions of the same IRI, which is poor for Interoperabilitätslevel A consumers even though the spec does not forbid it.

Proposal: migrate these features to the `rdf.ecore#//Resource` pointer convention, so there is exactly one representation of every entity and one way to express membership.

**Why this is bigger than N7.** `accessService` was free to change because nothing referenced it — that was N7's whole premise. These features *are* referenced by working code and tests: `CatalogAdminServiceImpl.addDatasetToCatalog` / `addDataServiceToCatalog` / `addSubCatalogToCatalog`, `DatasetSeriesAdminServiceImpl`, the corresponding REST resources, and their unit + integration tests. Changing the eType changes those service signatures (`addDataServiceToCatalog(String, DataService)` would become id- or URI-based), so it needs a migration plan and a decision about already-stored data.

Also settle at the same time whether `dcat:servesDataset` should be maintained on the DataService, and if so that it is derived ("datasets owning the distributions that reference me") rather than a straight inverse of `accessService`, which points at distributions, not datasets.

---

## 4. Work-package coverage matrix

| WP | Existing issues | Proposed | Coverage after |
|----|-----------------|----------|----------------|
| WP-DCAT-1 Setup & build | #2 ✅, #9 ✅ | N1 | complete |
| WP-DCAT-2 Model v3 | #1 ✅ | (conformance corpus → N27) | complete |
| WP-DCAT-3 Jena persistence & bridge | #3 ✅, #4 ✅, **#32 open** | N4, N5, N6 | complete |
| WP-DCAT-4 Admin interface | #5 open (→close), #6 ✅, #28 ✅, #29 ✅, #30 ✅ | N7 ✅, N8, N9, N10, N11, N12, N28 | complete |
| WP-DCAT-5 Delivery + SPARQL | **none** | N13, N14, N15 | complete |
| WP-DCAT-6 Client library | **#7 open** | (N11 first) | complete |
| WP-DCAT-7 Endpoint awareness | **none** | N16 | complete |
| WP-DCAT-8 Catalog browser | **none** | N17, N18, N19 | complete |
| WP-DCAT-9 Operations & deployment | **none** | N20, N21 ✅ | complete |
| WP-DCAT-10 Documentation | **none** | N22 | complete |
| WP-DCAT-11 EMF editor | **none** | N23 | complete |
| WP-DCAT-12 OData query UI | **none** | N24 | complete |
| WP-DCAT-13 Admin UI | **none** | N25, N26 | complete |
| Cross-cutting QA | **none** | N27 | complete |
| *(unmapped)* | #8 Ecore→RDF | — | cross-link to WP-MA-5 |

## 5. Appendix — draft comment for issue #30 (SHACL shapes: licensing & runtime loading)

To be posted on <https://github.com/DataInMotion/xdp/issues/30>. Kept here so the
reasoning survives outside the issue tracker.

**Note on the SHACL shapes: licensing, and why they are loaded at runtime**

Recording the reasoning behind the shapes-loading design, since it is not obvious from
the code alone.

**The problem.** The official GovData DCAT-AP.de 3.0 SHACL shapes are published under
AGPL-3.0. We cannot vendor them into this repository — the bundle ships under EPL-2.0
and committing AGPL artifacts into the distribution would create a licensing conflict.

**The design.** No shape file is committed. `DcatValidationServiceImpl` loads them at
bundle activation from an operator-configured directory (`ShapesConfig.shapesDirectory`,
env `SHACL_SHAPES_DIR`): every `*.ttl` in that directory is merged into a single Jena
`Shapes` graph. If no directory is configured the service is a no-op and reports
conformance, so the portal still starts. The shapes are therefore *deployment input*,
never part of what we build or redistribute.

The same applies to the controlled-vocabulary reference data
(`ShapesConfig.vocabularyDirectory`, env `SHACL_VOCAB_DIR`). This is needed because the
CV constraints check `skos:inScheme` / `sh:class` on the **value** node, and those
triples live in the EU/GovData authority tables that the shapes reach via `owl:imports`.
Jena does not resolve `owl:imports` — there is no auto-fetch — so without local copies
every vocabulary value would report as a false violation. `validate()` runs the shapes
against `ModelFactory.createUnion(entity, vocab)`.

**How it was verified.** Tested against the real GovData shapes and the real authority
tables, both downloaded to local directories outside the repository and loaded through
the two config properties. Confirmed: a genuine `…/frequency/ANNUAL` conforms, a bogus
value produces the MUSS `#kv-frequency` violation.

**What is in the repo instead.** The unit and integration tests use tiny self-authored
shapes, so nothing licensed is committed and CI needs no external files. The real-data
regression test `ControlledVocabularyRealDataTest` is `@EnabledIfEnvironmentVariable` on
`SHACL_SHAPES_DIR` / `SHACL_VOCAB_DIR` and simply skips where those are unset.

**Consequence to be aware of.** Because `owl:imports` are inert, only the shapes
physically present in the directory actually fire. The DCAT-AP.de-native shapes are
self-contained, but the German files carry only the message/severity *overrides* for the
upstream SEMIC base rules, not the rule bodies — so to enforce base DCAT-AP 3 constraints
the operator must also place `dcat-ap-SHACL.ttl` in the same directory. Follow-up work
(documenting/automating the operator-side download, and surfacing "shapes loaded / not
loaded" as a startup warning or readiness signal so a silently unvalidating portal is
visible) is tracked separately.

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
3. **Create the "Now" issues:** N1 ✅ *(done)*, N7 ✅ *(done)*, N21 ✅ *(done)*, N10, N11, N14, N20, N27.
4. **Do N11 before #7** so the client library does not freeze a contract the spec still
   contradicts, and remember #7 blocks WP-DA-10, WP-MA-5 and WP-SN-4.
5. Create the "Later" issues as each work package starts.
6. Refresh `docs/status-gap-analysis.md` — it is dated 2026-07-09 and predates on-write
   SHACL enforcement (FR-4), controlled-vocabulary validation (F-22), the native Jena
   `ValidationReport`, and the runtime/config bundles, all of which it still lists as
   open.

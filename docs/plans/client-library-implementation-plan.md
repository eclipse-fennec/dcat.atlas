# Client library for integration — implementation plan

**Status:** design exploration, 2026-08-21. Not implemented, on branch `dcat_client`.
**Issue:** eclipse-fennec/dcat.atlas#27 — *Client Library for integration (Data-Atlas,
Model-Atlas, SensiNact)*.
**Depended on:** #26 (pagination and caching on the read collections) — **done**, so the
read contract this is written against is the shipped one. See [§8](#8-the-read-contract-26).
**Worked example:** [`model-atlas-dcat-mapping.md`](model-atlas-dcat-mapping.md) — what a
model.atlas EPackage becomes in DCAT, what MDO did instead, and the mapping that follows.

---

## 1. Goal

A Java library through which Data-Atlas, Model-Atlas and SensiNact register DCAT elements
with the portal, holding EMF objects rather than composing HTTP requests. Usable both as a
plain library (no OSGi) and as a DS service.

**Non-goals.** Any change to the REST surface. A UI. Authentication on the portal side
(see [§6.8](#68-auth-is-a-hook-with-nothing-behind-it)). Local caching of the catalogue, or
offline queueing of registrations.

---

## 2. The reference, and what to take from it

The issue points at MDO's
[`PiveauRestConnector`](https://github.com/de-jena/MDO/blob/main/de.jena.piveau.rest.jakarta/src/de/jena/piveau/rest/jakarta/PiveauRestConnector.java):
a DS component wrapping a JAX-RS `WebTarget`, implementing two per-entity interfaces
(`DatasetConnector`, `DistributionConnector`), each operation in a sync and a
`Promise`-returning flavour, `Location` header copied onto the entity's `about`, and a
`switch` on the response status.

**Keep:** the entity-oriented surface, the async variants, the `Location` → `about` handling.

**Drop:** the error handling. Those methods return `null` (or `false`) on anything that is
not `CREATED`/`NO_CONTENT` and print the status to `System.out`, so a caller cannot separate
"the portal rejected your metadata" from "the portal is down". Against DCAT.Atlas that is
the wrong trade: SHACL enforcement and model validation are **on by default**, so a
rejected registration with a report body is a routine outcome, not an exception path. See
[§6.3](#63-typed-errors-one-of-which-is-dcat-specific).

---

## 3. Shape: mirror the model.atlas client

`model.atlas` already ships this exact thing as
`org.eclipse.fennec.model.atlas.rest.client.{api,impl,osgi}` plus two test bundles. Same
split here, same reasons:

| bundle | contents |
|---|---|
| `org.eclipse.fennec.dcat.atlas.client.api` | `DcatAtlasClient`, `ClientConfiguration`, `AuthType`, the typed exception hierarchy, the `JakartaRsClientProvider` SPI. **No OSGi dependencies** — DS, framework or ConfigAdmin — so it is consumable as a plain library. |
| `org.eclipse.fennec.dcat.atlas.client.impl` | Plain-Java implementation: builder + `ServiceLoader` factory behind `DcatAtlasClient.builder()`, XMI (de)serialisation through an EMF `ResourceSet`, ETag handling, status→exception mapping. Only the provider SPI base is exported; the rest is `Private-Package`. |
| `org.eclipse.fennec.dcat.atlas.client.osgi` | ConfigAdmin factory component (PID `org.eclipse.fennec.dcat.atlas.client`), one client per portal instance, `ClientBuilder` sourced from the Whiteboard, `Promise`-returning facade. |
| `…client.tests`, `…client.osgi.tests` | Plain-Java suite against a stub server; OSGi integration suite against the real runtime. |

The dependency on `org.eclipse.fennec.dcat.atlas.dcatap.de.model` is unavoidable and
intended — the whole point is that callers hand over `Catalog`/`Dataset` instances. The
client must **not** depend on `…atlas.impl` or `…atlas.rest`.

### 3.1 Two alternatives considered and rejected

- **`org.eclipse.fennec.openapi.client`** (fennec-emf.util) invokes OpenAPI operations
  generically over the JDK `HttpClient`, driven by a descriptor — and since #21 we serve
  one at `/rest/openapi.{json,yaml}`. Zero code for the read side. But it (de)serialises
  **JSON** through the Fennec codec, while every admin write is `@Consumes(application/xmi)`
  only, so it cannot register anything. Worth using as a smoke test of the descriptor;
  not the basis of the client.
- **`openapi-generator` codegen.** The request and response bodies are EMF objects; generated
  DTOs would be a second, worse copy of the model bundle, and the descriptor cannot express
  the EMF containment semantics the API relies on.

---

## 4. Surface sketch

```java
public interface DcatAtlasClient extends AutoCloseable {

    // Registration — idempotent, PUT-based (§6.1)
    Catalog       registerCatalog(String id, Catalog catalog);
    Dataset       registerDataset(String id, Dataset dataset);
    DataService   registerDataService(String id, DataService service);
    DatasetSeries registerDatasetSeries(String id, DatasetSeries series);
    Distribution  registerDistribution(String datasetId, String id, Distribution distribution);

    // Membership — the link endpoints, not a full re-send of the container (§6.5)
    void linkDatasetToCatalog(String catalogId, String datasetId);
    void linkDataServiceToCatalog(String catalogId, String serviceId);
    void linkSubCatalog(String catalogId, String childId);
    void linkDatasetToSeries(String seriesId, String datasetId);
    void linkAccessService(String datasetId, String distributionId, String serviceId);
    // … and the unlink counterpart of each

    // Reads
    Optional<Catalog> catalog(String id);              // conditional GET, ETag-aware
    Page<Dataset>     datasets(PageRequest request);   // the api.read types, as shipped (§8)

    // Deletion
    void delete(DcatCollection collection, String id, DeleteMode mode); // mode = SINGLE | CASCADE

    // Startup gate
    boolean ready();                                   // GET /health/ready
}
```

`Promise`-returning variants live in `…client.osgi` only, so `.api` stays free of
`org.osgi.util.promise` — and there is no `CompletionStage` twin of them (§10).

---

## 5. Transport and serialisation

- **Transport:** JAX-RS client via a `JakartaRsClientProvider` SPI, as in model.atlas — the
  default implementation calls `ClientBuilder.newBuilder()` with timeouts, and the OSGi
  front-end overrides it to take the `ClientBuilder` from the Whiteboard. The JDK
  `HttpClient` is the alternative (it is what the integration tests here already use, and
  it needs no bundle); the SPI means that choice is not baked into the API.
- **Serialisation:** `application/xmi` in both directions, produced and consumed by a plain
  EMF `ResourceSet` — XMI is EMF's native format, so the Fennec codec is not needed on the
  client side. Note the media type is **`application/xmi`**, never `application/xml`: the
  server picks its codec by media type and will 415 the latter.
- **Reads** may equally ask for RDF/XML, Turtle, JSON-LD or N-Triples; the client should
  expose that as a configurable `Accept` for callers who want the RDF form rather than
  EObjects.

---

## 6. Decisions that actually matter

### 6.1 `PUT /admin/{collection}/{id}` is the registration primitive

Consumers arrive with their own stable identifier (MDO calls it `originalId`). `PUT` is
create-or-replace with the **path** deciding identity, which makes `register(…)` idempotent
with no bookkeeping. `POST` is for the case where the caller wants the server to mint an id —
and it is actively wrong for a re-registration loop, because a repeat POST carrying an
`about` we already hold is a **409 Conflict** by design.

### 6.2 `about` is normally left unset

The path carries the id, so the body does not need an `about`; `PUT` accepts one that is
absent or equal and refuses a different or foreign one. When a caller does want it set, the
client computes it from its configured base URL — that is safe because the server folds the
public form back to the logical one (`PublicIris.toLogical`). Keeping this in one place
prevents three consumers from each learning that a foreign `about` on a root resource is a
400.

### 6.3 Typed errors, one of which is DCAT-specific

| status | exception | notes |
|---|---|---|
| **422** with an RDF body | `DcatShaclException` | SHACL enforcement refused the write. **Not a 400** — measured: `ShaclViolationExceptionMapper` answers `422 Unprocessable Entity` with `X-SHACL-Conforms: false`, and the body is the native `sh:ValidationReport` in whichever RDF syntax the request's `Accept` allows (`WriteValidation.reportType`, first supported wins, with a default). The header, not body sniffing, is the discriminator. |
| **422** with `text/plain` | `DcatModelConstraintException` | The model's own OCL constraints refused it. `ModelConstraintExceptionMapper` joins the violation lines as plain text — already strings, so nothing to parse. |
| 400 | `BadRequestException` | A malformed request rather than invalid metadata: an unconvertible query parameter, which `QueryParamExceptionMapper` renders as 400 rather than the 404 JAX-RS prescribes. |
| 404 | `NotFoundException` | |
| 409 | `ConflictException` | Repeat POST of an `about` we already hold, or a dangling reference refused by referential integrity. |
| 412 | `PreconditionFailedException` | Stale `If-Match`; feeds the retry in §6.4. |
| 415 | `TransportException` | Almost always `application/xml` where `application/xmi` was meant. |
| 503 | `RetryableException` | **Easy to miss:** the git remote push failed. The commit *is* durable locally and the portal will push it later, so this is retryable and must not be reported as data loss. |
| other 5xx / IO | `TransportException` | |

Two of those statuses were wrong in the first draft of this plan, which said 400 for a
validation failure. They are 422, and the fix matters to a client: 400 and 422 mean
different things here, and conflating them would have had callers retry-or-report the wrong
one.

### 6.4 An optimistic-locking helper

Read the ETag, mutate, `PUT` with `If-Match`, retry once on 412. Without it every consumer
either skips `If-Match` entirely — losing the protection F-16 exists for — or reimplements
the loop slightly differently.

### 6.5 Membership through the link endpoints

FR-9/FR-11 exist so a member can be attached without re-sending the container. The
asymmetries are worth hiding: not every container takes members on every path, and
`inSeries` lives on the **dataset**, so linking a dataset to a series edits and returns the
*dataset* — and its ETag, not the series'. A caller should not have to know that.

### 6.6 Cascade delete is a mode, not a boolean parameter

`DELETE …?cascade=true` unlinks referrers as well. `DeleteMode.SINGLE|CASCADE` reads better
at the call site and leaves room for a future dry-run. The response carries the list of
resources that were unlinked; the client should return it rather than discard it.

### 6.7 `ready()` before the first registration

The portal reports CRITICAL on `/health/ready` until the SHACL shapes are mounted and the
store is reachable, and refuses to start at all without `PUBLIC_BASE_URL`. A consumer that
registers into a not-yet-ready portal gets confusing failures; one readiness call at
activation turns that into a clear log line.

### 6.8 Auth is a hook with nothing behind it

MDO authenticated with Keycloak and an `X-API-Key` header. **DCAT.Atlas has no
authentication at all today** — no security filter, no credentials, the admin API is open to
whoever can reach the port. The client should carry a pluggable auth contributor
(`AuthType` + header supplier) so consumers are not rewritten later, but the portal-side gap
is a separate issue and should be filed as one rather than smuggled in here.

---

### 6.9 No Jena in the client, and only some of the API's exceptions reused

The api bundle was split by concern on 2026-08-21, and the split made visible that every
failure this client reports already exists there as a type —
`api.validation.ShaclViolationException`, `api.integrity.ResourceInUseException` and
`ReferentialIntegrityException`, `api.identity.ForeignIdentityException`,
`api.store.StoreUnavailableException`. Throwing those would give a caller the same type
whether it went over HTTP or called the OSGi service directly, which is worth something to
Data-Atlas and Model-Atlas, either of which could do both.

One of them cannot be reused: `ShaclViolationException` carries a Jena `ValidationReport`,
so throwing it would put Jena in every consumer of the client. It does not need to be,
because the report's syntax follows the request's `Accept` — the client asks for Turtle (or
JSON-LD) and **carries the report as it arrived**, bytes plus media type, with no parsing.
Callers that want structure bring their own parser; callers that want to log or display a
refusal — which is most of them — need nothing. That also means the API needs no
machine-readable error body added for the client's sake.

So: reuse the exceptions that carry only strings, and define client-side types for the two
validation failures. Worth revisiting against a real consumer rather than settling harder
than that on paper.

## 7. Testing and the issue's "evidence"

1. **Plain-Java unit suite** against a stub HTTP server: status→exception mapping, XMI round
   trip, the `If-Match` retry, `about` computation.
2. **OSGi integration suite** against the real runtime, reusing the existing `test.bndrun`
   harness in `…rest.tests` (the shared `/tmp/rdf`-style store discipline applies: assert on
   deltas, clean up what you create).
3. **One genuine end-to-end registration from model.atlas**, which is what the issue asks
   for as evidence and the only test that exercises the assumption in §9.

---

## 8. The read contract (#26)

Pagination landed on 2026-08-21, so the client is written against a contract that exists
rather than one that is coming. What it has to honour:

- **`Page<T>`** (`api.read`) carries `items`, `nextAfter` and `total`; **`PageRequest`**
  carries `after` and `limit`, defaults to 50 and clamps at 500. Reuse both — they are
  exported, and a second definition of a page in the client would only drift.
- **Keyset, not offset.** The cursor is `after=<id>`, the last id of the previous page.
- **Follow `Link rel="next"`, do not synthesise it.** Its absence, not a short page, is how
  a walk ends. `Link rel="first"` shows the limit actually applied after clamping, and
  `X-Total-Count` carries the collection total.
- **The ETag identifies the page**, not the collection, so `If-None-Match` while walking
  behaves: 304 for the page you hold, 200 for the next one. A client may cache per page.
- **An empty page is `204 No Content`** — for an empty collection and equally for a cursor
  past the end. Status before body, always.
- **A limit that is not a number is a 400**, not a 404.

## 9. Phasing

| phase | content |
|---|---|
| **P1** | The three bundles, `ClientConfiguration`, the XMI round trip, and one end-to-end registration. Transport and error mapping get settled here; the rest is repetition. The target should be the first slice of the [model.atlas mapping](model-atlas-dcat-mapping.md) — a scope as a Catalog, one EPackage-in-a-stage as a Dataset with its formats as Distributions, and the model.atlas API as the DataService — rather than Catalog-because-it-is-first-in-the-list. It exercises membership, `inSeries` and `accessService` in P1 instead of discovering them in P2. |
| **P2** | The other four entity types and all the membership endpoints. |
| **P3** | OSGi front-end: ConfigAdmin factory component, Whiteboard `ClientBuilder`, `Promise` facade, readiness gate. |
| **P4** | The evidence of §7, including the registration from model.atlas. |

Smaller than the model.atlas client, which additionally carries EPackage resolution, caching
and drift detection that none of this needs — roughly a third of it.

**P1 is still the phase to do before committing to the rest**, though no longer because of
the XMI question — that is settled (§10). It is where the transport and the error mapping
get decided against a running portal, and those two are what the other three phases repeat.

---

## 10. Open questions

- ~~Does SensiNact hold EMF DCAT objects, or RDF?~~ **Answered 2026-08-21:** "SensiNact" in
  the issue means **event.atlas**, the mapping runtime, rather than sensiNact itself — and
  it is EMF throughout, like the other two consumers. So all three hold EMF objects and
  XMI-only writes are no obstacle anywhere. *Follow-on, and a better question:* event.atlas
  maps southbound payloads (MQTT, REST) onto EMF models, so what does it register — a
  DataService per adapter endpoint, a Dataset per mapped profile, or both? That decides
  whether it needs the whole surface or mostly DataService and Dataset upserts.
- ~~Sync-only in `.api`, or `CompletionStage` there as well as `Promise` in `.osgi`?~~
  **Answered 2026-08-21: `.api` is synchronous, `.osgi` adds a `Promise` facade, and there
  is no `CompletionStage`.** Every operation existing three times is not worth it when all
  three consumers are OSGi and `Promise` is the idiom there — it is what MDO offered. A
  plain-Java consumer that wants async wraps the sync call in its own executor. What makes
  this low-stakes: registration happens when a package is released or an adapter deployed,
  never per message, so async is about not blocking a DS `@Activate` rather than about
  volume.
- ~~Should the client expose the SPARQL endpoint (`/rest/sparql`) as a query method?~~
  **Answered 2026-08-21: no.** Out of scope for a registration library. It is a different
  concern, its results are not EMF (so it would drag a result-format decision in), and if a
  consumer needs it later it can go on a separate optional interface without touching this
  API.
- ~~One client per portal, or a multi-portal facade?~~ **Answered 2026-08-21: one client
  per portal.** A ConfigAdmin factory component, one configuration per portal, each service
  tagged so a consumer names the one it means
  (`@Reference(target = "(dcat.portal=jena)")`); publishing to two portals is then an
  explicit loop over two references. A facade would have to define what "it worked" means
  when one portal accepts and another refuses — all-or-nothing, per-portal results,
  first-failure-wins — and that is a semantics to invent only once somebody asks for it. It
  can be layered on later without touching `.api`.
- ~~Does a validation failure carry a parsed report or a raw one?~~ **Answered:** raw, see
  §6.9. The report's syntax follows the client's `Accept`, so no Jena and no API change.

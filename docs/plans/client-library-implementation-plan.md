# Client library for integration — implementation plan

**Status:** P1 implemented 2026-08-24 on branch `dcat_client`; P2–P4 outstanding.
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

### 6.4 ~~An optimistic-locking helper~~ — withdrawn P1 2026-08-24; its cause fixed 2026-08-24

> **The reason for the withdrawal no longer holds.** The portal now hands the shapes the
> `rdf:type` of every resource a submitted body references, so a linked resource can be read,
> edited and `PUT` back — verified against the container. Re-adding a read-modify-write helper
> to the client is therefore possible again, and is an open question rather than a settled no:
> the registration loop of §6.4a needs no read and the conditional register of §6.4b already
> covers the concurrency case, so it would be a convenience rather than a necessity. The
> record below is kept because it is why the client looks the way it does.


The plan said: read the ETag, mutate, `PUT` with `If-Match`, retry once on 412. It was
built that way in P1 and **it does not work**, for a reason that has nothing to do with
ETags. Measured against a running portal with SHACL enforced:

```
sh:resultMessage "dcat:Dataset: dcat:inSeries MUSS auf eine Klasse vom Typ
                  dcat:DatasetSeries verweisen."@de ;
sh:resultPath    dcat:inSeries ;
```

Reading a linked resource and `PUT`ting it back sends `dcat:inSeries <series>` in a graph
that says nothing about `<series>`, so the profile's "MUSS auf eine Klasse vom Typ …
verweisen" rule fails — the same trap the user guide documents as *Modifying a resource
that has members*. It fires for `dcat:accessService` on a distribution too. Any entity
that has been linked to anything is therefore un-round-trippable, which after registration
is most of them.

`updateDataset` was removed from the API rather than shipped with that caveat. **What
replaces it is the registration loop** (below), which needs no read at all.

### 6.4a The registration loop, measured

A `PUT` replaces, and two consequences follow that a consumer has to plan for:

1. **Distributions go away** — `dcat:distribution` is containment, so a body without them
   says the resource has none.
2. **Membership links go away** — `dcat:inSeries`, `dcat:dataset`, `dcat:servesDataset`.

So the loop is **register the entity → register its distributions → assert its links**,
and every step is idempotent, so re-running the whole thing is the intended usage rather
than a workaround. Verified: after a re-register the dataset came back with
`inSeries=0 distributions=0`, and re-linking restored it.

### 6.4b Conditional *registration* — built 2026-08-24

A conditional register is the only shape optimistic locking can sensibly take here: the
caller already holds the full entity, so nothing needs reading, and the read-back that
breaks §6.4 never happens. Ilenia's call was to build it now — "just so we are prepared in
case someone else modifies the dcat in the meantime" — and to **log** rather than throw when
the validator no longer matches.

So every `registerX` has a three-argument form taking an `ifMatch`, and returns
`Registration<T>` (the stored entity plus the new `ETag`) instead of a bare entity. The
validator comes from the *previous* registration's response, so a loop needs no extra
request.

**A refused write is not an exception.** `Registration.applied()` comes back `false`, the
client logs it at WARNING, and the loop carries on to the next resource. Unwinding on a 412
would stop a publisher from updating every later resource because one of them had been
edited elsewhere. Every other non-success status still throws, and a 412 on an
*unconditional* write does too — no precondition was sent, so the portal cannot have
evaluated one and something else is wrong.

Measured against the running portal:

```
first register                  applied=true  etag="b7a1903f…"
another writer registers        applied=true  etag="904a3078…"
register with the stale etag    applied=false      <- nothing written, WARNING logged
  portal still says             "version two"      <- confirmed by reading it back
register with the current etag  applied=true
  portal now says               "version three"
re-register identical content   applied=true  sameEtag=true
```

Two properties worth keeping in mind. The **ETag is content-based**, so an idle loop that
re-registers identical content gets the same validator back and never invalidates its own
token — the last line above. And a **412 carries no body and no `ETag`**, so a refusal
cannot report the portal's current validator.

### 6.4c Where the validator lives — `etagOf`

**The client stores nothing.** `Registration.etag()` hands the validator to the caller and
the client forgets it; there is no cache, because a cache inside the client would be empty
after a restart anyway and would only move the problem somewhere less visible.

Which raised the question Ilenia asked, and it found a hole: with `Registration` as the only
source of a validator, conditional registration worked *within* one process lifetime and not
across a restart. A publisher coming back up held nothing, so its first write had to be
unconditional — precisely the overwrite the precondition exists to prevent.

`Optional<String> etagOf(DcatCollection, String)` closes it with a `HEAD`, plus
`etagOfDistribution(datasetId, id)` for the nested case. Header only, no entity — so it is
cheap and stays clear of the read-modify-write trap, since there is nothing parsed and
nothing to send back. **Empty means "no validator to guard with"**, whether the resource is
absent or simply carries none; a caller passing the empty result straight to a conditional
register gets an unconditional write, which is what a create needs, so the pattern takes no
branch:

```java
String validator = client.etagOf(DcatCollection.DATASETS, id).orElse(null);
Registration<Dataset> result = client.registerDataset(id, dataset, validator);
```

Measured against the running portal — a create, a foreign write, then a restart:

```
etagOf before create           Optional.empty      -> the create goes unconditional
created                        etag="589f56f7…"
(somebody else registers)
etagOf after restart           etag="f40dac37…"    -> differs, as it should
guarded write                  applied=true
etagOf a missing resource      Optional.empty
```

**So: hold it in memory and re-seed it with one `etagOf` per resource at startup.** Nothing
to persist, nothing to keep in sync, and it is self-correcting — whatever happened while the
publisher was down, it picks up the current truth. Persisting the validator next to the
publisher's own state (model.atlas already tracks a `PackageDescriptor.fingerprint`) is
possible but buys nothing: a stored validator can go stale in a way a `HEAD` cannot.

Worth saying plainly for model.atlas: it already detects its own changes through the
fingerprint, so this guard is not protecting it from its own staleness — it protects
*foreign* edits from being clobbered. If nothing else ever writes those resources, the
unconditional two-argument form is the honest choice and none of this is needed.

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

**Revisited in P1, 2026-08-24 — the reuse is not implementable, and the client defines its
own hierarchy.** Read from the mappers rather than the service layer:

- **409 has two causes** — a repeat identity (`StoreConflictExceptionMapper`) and a dangling
  reference (`ReferentialIntegrityExceptionMapper`) — and both answer `text/plain` with **no
  discriminating header**.
- **400 likewise** — a foreign `about` (`ForeignIdentityExceptionMapper`) and an
  unconvertible query parameter (`QueryParamExceptionMapper`).

From a response the two cannot be told apart without matching on a human-readable message,
which breaks the first time somebody rewords it. So there is one client type per status
carrying the message verbatim. Giving those mappers a discriminating header would make the
finer split possible later; that is a portal change, not a client one.

The one discrimination the plan *did* get right is the important one: **`X-SHACL-Conforms`
separates the two 422s**, and it has to be the header rather than the body's media type,
because the SHACL branch can itself answer `text/plain`. Confirmed live — a bare dataset
was refused by **OCL** (`DcatModelConstraintException`, two violation lines) while a
complete one that broke the profile was refused by **SHACL**
(`DcatShaclException`, a Turtle report). Note the ordering that implies: OCL runs first, so
a very incomplete entity never reaches SHACL at all.

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

### P1, done 2026-08-24

`client.api` and `client.impl` are implemented and the mapping slice runs end to end
against a portal in a container: scope → Catalog, EPackage → DatasetSeries, EPackage-in-a-
stage → Dataset, the API → DataService, content type → Distribution, plus all five
membership links. 29 plain-Java tests drive the real Jersey client against a stub portal.

What the phase settled, beyond the code:

- **§6.4 is withdrawn**, §6.4a (the registration loop) and §6.4b (conditional
  registration) replace it — see above. The one substantive plan change.
- **§6.9's exception reuse is not implementable**; the client owns its hierarchy.
- **A write must ask for `application/xmi`.** The admin endpoints `@Produces` XMI, JSON,
  XML and RDF/XML — *not* Turtle — so `Accept: text/turtle` on a write is a `406` that
  reads like a rejected registration. Costs nothing on the report path: `reportType` falls
  back to Turtle when the `Accept` holds no RDF type, so an XMI write still gets a Turtle
  report and no compound `Accept` is needed.
- **A membership link sends no body at all** — those endpoints declare no `@Consumes`.
- **`ready()` looks beside the REST application, not under it**: with base `…/rest/` the
  checks are at `…/health/ready`.
- **`dcat:inSeries`, `dcat:accessService` and sub-catalogues all work** — the three things
  §6 of the mapping document noted nothing had exercised yet. Read back after linking:
  `inSeries=1 distributions=1 accessService=1`.
- **A cascade delete reports referrers, and `inSeries` is not one of them.** Deleting a
  dataset linked to a catalog, a series and a service unlinked **two** — the series link
  lives *on* the dataset, so it dies with it and there is nothing to rewrite.
- `@ServiceProvider` alone does not make `builder()` work off a plain classpath: bnd emits
  the `META-INF/services` entry into the *jar*, and a plain-Java consumer runs against
  classes. The descriptor is written out as a file as well.

Not in P1, and unchanged from the plan: the paged collection reads of §8 (nothing in the
mapping slice walks a collection, and `Page`/`PageRequest` would pull the portal's api
bundle into the client — worth deciding with a caller in hand), and the whole of P3.

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

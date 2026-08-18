# Open Data Portal — Admin Interface (DCAT-AP 3)

> As of: 2026-06-27 · Status: Draft for review
> Belongs to `docs/opendata-portal-planung.md` → **AP3**.
> Purpose: build-ready specification for the **OSGi service API** and **REST adapter** for maintaining the catalog holdings.

## 1. Purpose & Scope

This interface maintains (create/update/delete/read-back) the portal's DCAT-AP 3 catalog holdings. It is the **write side**; the machine-/human-readable **read side** (content negotiation, SPARQL, UI) is AP4/AP9 and is only referenced here.

**Two interchangeable façades over the same logic (§7 of the planning doc):**
- **OSGi service API** — the primary contract; for embedded callers (Model/Data Atlas in the same process), EMF-typed.
- **REST adapter** — a thin layer over the OSGi API; accepts/returns RDF (DCAT-XML among others), for distributed microservice operation.

Both façades expose **the same operations**. Whoever can use one can use both.

## 2. Managed Entities (DCAT-AP 3)

Derived from `org.eclipse.fennec.data.atlas.dcat.model`. The common base is `DcatResource` (identifier, title, description, theme, keyword, creator, publisher, issued, modified, license, …).

| Entity | EClass | Key relationships (Ecore) | REST collection |
|---|---|---|---|
| **Catalog** | `Catalog` | `dataset`, `service`, `catalog` (sub-catalogs), `record`, `themeTaxonomy`, `homepage` | `/catalogs` |
| **Dataset** | `Dataset` | `distribution`, `spatial`, `temporal`, `accrualPeriodicity`; **`inSeries` → DatasetSeries** (AP1) | `/datasets` |
| **DatasetSeries** | `DatasetSeries` *(new in AP1)* | `seriesMember` → Dataset | `/dataset-series` |
| **DataService** | `DataService` | `servesDataset`, `endpointURL`, `endpointDescription` | `/data-services` |
| **Distribution** | `Distribution` | `accessURL`, `downloadURL`, `accessService` → DataService, `format`, `license` | `/distributions` |
| **CatalogRecord** | `CatalogRecord` | `primaryTopic`, `conformsTo` | `/catalog-records` *(usually derived)* |

> **Agent** (`foaf:Agent`: publisher/creator/contactPoint), `Concept`/vocabularies (`skos`), `Standard` etc. are **not** managed as their own top-level resources, but maintained as a component or reference within the entities above (see FR-12).

> **DatasetSeries** depends on **AP1** (DCAT-AP 3 upgrade). Until then, the associated endpoints/methods are part of the contract but not yet implementable.

## 3. Design Principles (proposal, to confirm in review)

- **D1 — Linked-Data-compliant URIs:** A resource's REST URL **is** its dereferenceable DCAT URI: `{publicBaseUrl}/{collection}/{id}`. The `{id}` is the last path segment. → Endpoint awareness from AP7 provides `publicBaseUrl`.
- **D2 — ID strategy:** The client may supply an `id`/URI (`PUT …/{id}` or `dct:identifier`/`@id` in the payload). If missing, the server **mints** a stable ID under `publicBaseUrl`.
- **D3 — Idempotency/upsert:** `PUT …/{id}` is *create-or-replace* and repeatable (core requirement: the Data Atlas "publishes", possibly multiple times).
- **D4 — RDF as wire format:** Content is exchanged as RDF; content negotiation via `application/rdf+xml` (= "DCAT-XML", default input), `text/turtle`, `application/ld+json`, `application/n-triples`.
- **D5 — Transactional & validated:** Every write operation is atomic (Jena TDB2 transaction) and is checked against DCAT-AP 3 SHACL before commit.
- **D6 — Multi-catalog/named graphs:** Each `Catalog` = one named graph; catalog-wide delete/replace is a graph operation.
- **D7 — Optimistic concurrency:** Every resource carries an `ETag`; mutating operations accept `If-Match` (mandatory, see FR-7).
- **D8 — Replace-only (no PATCH):** Updating happens exclusively via **complete replacement** (`PUT` upsert). There is **no** partial modification (PATCH). To change something, send the complete resource or the complete (sub)graph.

## 4. Functional Requirements

Decomposable into issues; each FR is uniquely referenceable.

### General
- **FR-1 (CRUD per entity):** Create, read, replace (upsert) and delete for Catalog, Dataset, DatasetSeries, DataService, Distribution.
- **FR-2 (Upsert/idempotency):** Repeated create/replace with the same ID produces the same state, no duplicates (D3).
- **FR-2b (Replace-only, no PATCH):** Updating happens **exclusively** by fully replacing the resource or the (sub)graph (`PUT` upsert). Partial updates (PATCH) are **not** part of the interface (D8). Benefit: simple, unambiguous semantics and conflict-free re-publishing from the Data Atlas.
- **FR-3 (URI assignment):** The server mints missing IDs under `publicBaseUrl`; supplied IDs are adopted, provided they are collision-free and within the allowed namespace (D1/D2).
- **FR-4 (Validation):** Write operations validate against DCAT-AP 3 constraints (SHACL); on violation there is **no** commit and a structured validation report is returned.
- **FR-5 (Pure validation run):** Input can be validated **without** writing (dry run).
- **FR-6 (Transactionality):** Every operation is atomic; partial states are never visible.
- **FR-7 (ETag concurrency, mandatory):** Every resource returns an `ETag` on `GET` and after each write. Mutating operations (`PUT`/`DELETE`) **must** evaluate `If-Match` and reject with `412 Precondition Failed` on mismatch; this prevents lost updates (D7).
- **FR-8 (Format agnosticism):** Input/output in RDF/XML, Turtle, JSON-LD, N-Triples (D4).

### Relationships & composition
- **FR-9 (Catalog membership):** Dataset, DataService and sub-catalog can be assigned to / removed from a catalog **without** re-sending the target resource in full.
- **FR-10 (Distribution composition):** Distributions are created/deleted in the context of their Dataset; a Distribution without a Dataset is not allowed. `accessService` optionally references a DataService.
- **FR-11 (Series membership):** Datasets can be assigned to / removed from a DatasetSeries (`inSeries`/`seriesMember`) — *depends on AP1*.
- **FR-12 (Embedded references):** Agents (publisher/creator/contactPoint), Concepts/Themes, licenses, standards are maintained as part of the entity; identical Agents/Concepts are deduplicated (by URI).

### Bulk maintenance (Data/Model Atlas integration)
- **FR-13 (Graph ingest):** A complete DCAT document (whole/partial catalog) can be upserted in **one** transactional operation (core for "publishing" from the Data Atlas, AP5).
- **FR-14 (Graph replace/delete):** A catalog graph can be fully replaced or deleted (re-sync of a tenant/catalog, D6).
- **FR-15 (Differential upsert):** Repeated ingest updates only what changed; removed resources are optionally purgeable via "replace" semantics.

### Endpoint/microservice aspects
- **FR-16 (Public URLs):** The `accessURL`/`downloadURL` of Distributions are adopted as **public** URLs and not altered (AP7).
- **FR-17 (Reachability check, optional):** On request, the operation checks the reachability of the Distribution URLs and reports the result (configurable as non-blocking).
- **FR-18 (Self-awareness):** The minted URI namespace follows the configured public base URL, even when running embedded.

### Cross-cutting
- **FR-19 (Error taxonomy):** Uniform error model (validation 422, conflict 409, not found 404, precondition 412 …) in both façades.
- **FR-20 (Audit/provenance, optional):** Changes are logged with timestamp/author (`CatalogRecord.modified`/audit log).
- **FR-21 (Security, planning §11.7):** Write access can be secured (e.g. a Keycloak token as in MDO); a hook is foreseen, the design is an open decision.

## 5. REST Interface

Base: `{publicBaseUrl}/admin/api/v1`. All bodies are RDF (D4); `Accept`/`Content-Type` control the serialization.

### 5.1 Standard operations per collection

Applies uniformly to `catalogs`, `datasets`, `dataset-series`, `data-services`, `distributions`:

| Method & path | Purpose | Success | Key errors |
|---|---|---|---|
| `GET /{coll}` | List (paginated: `?page`, `?size`, filters `?q`, `?theme`, `?publisher`) | 200 | — |
| `POST /{coll}` | Create; ID taken from the payload's `about` when it names one of ours, minted only when `about` is absent | 201 + `Location` | 400 (foreign/unusable `about`), 409 (identity taken), 422 validation |
| `GET /{coll}/{id}` | Single resource (content negotiation) | 200 + `ETag` | 404 |
| `PUT /{coll}/{id}` | **Upsert** (create-or-replace, idempotent; the only way to change, FR-2b). The ID comes from the path; the body's `about` must be absent or name that same resource | 200/201 + `ETag` | 400 (`about` ≠ the path's resource), 409, 412, 422 |
| `DELETE /{coll}/{id}` | Delete (`?cascade=true` deletes composites) | 204 | 404, 409 (referenced) |

There is **no** `PATCH` — changes happen exclusively via a full `PUT` replace (D8/FR-2b). `PUT`/`DELETE` evaluate `If-Match: <etag>` (FR-7, mandatory). `?validate=only` performs FR-5 (dry run) → 200 with a report, no writing.

A `409` from a create carries a **`Location`** header too — the read URL of the resource
already holding that identity, the same URL the `201` would have returned. A client whose
retry conflicts therefore still ends up with the URL, and the ID in it, that it needs to go
on and add members; no `GET` in between, and no parsing the message to recover it. The other
refusals carry no `Location`: a `400` names no resource of ours to point at.

### 5.2 Relationship/membership endpoints (FR-9/10/11)

| Method & path | Purpose |
|---|---|
| `GET  /catalogs/{id}/datasets` | Datasets of the catalog |
| `PUT  /catalogs/{id}/datasets/{datasetId}` | Add a dataset to the catalog (link, without re-upload) |
| `DELETE /catalogs/{id}/datasets/{datasetId}` | Remove the assignment |
| `PUT/DELETE /catalogs/{id}/services/{serviceId}` | DataService assignment |
| `PUT/DELETE /catalogs/{id}/catalogs/{subId}` | Sub-catalog assignment |
| `GET  /datasets/{id}/distributions` | Distributions of a dataset |
| `POST /datasets/{id}/distributions` | Create a distribution **in context** (FR-10) |
| `PUT/DELETE /dataset-series/{id}/members/{datasetId}` | Series membership (FR-11, AP1) |
| `PUT/DELETE /distributions/{id}/access-service/{serviceId}` | `accessService` link |

Each membership also has a `POST /catalogs/{id}/datasets` (likewise `/services`,
`/catalogs`) taking the member in the body: it **creates the member** and then links it,
so the identity rules of `POST /datasets` apply — 400 for a foreign `about`, **409 when
the member already exists**. An existing member is never overwritten through this path:
use the `PUT` above to link it, `PUT /datasets/{datasetId}` to change it. That
separation is deliberate — one dataset may be referenced from several catalogs, series
and services, and a change made through a single membership path would silently change
it for all of them.

**How a link is written in each format.** XMI serializes a membership as
`<dataset href="{base}/datasets/{id}#/"/>`, RDF as
`<dcat:dataset rdf:resource="{base}/datasets/{id}"/>`. The trailing `#/` is XMI pointer
syntax — a document URL plus a fragment naming the object inside it, `/` being that
document's root — and is **not** part of the identity; the identity is the URL with the
fragment removed. A fragment is never transmitted to a server, so the two forms address
the same resource over HTTP. On a write, an `href` is accepted either way.

### 5.3 Bulk ingest (FR-13/14/15)

| Method & path | Purpose |
|---|---|
| `POST /ingest?catalog={catalogId}&mode=merge\|replace` | Upsert a whole DCAT document transactionally; `replace` = graph replacement (FR-14) |
| `POST /ingest?validate=only` | Dry-run validation of a whole document (FR-5) |
| `DELETE /catalogs/{id}?purge=true` | Fully remove the catalog graph (FR-14) |

### 5.4 Example — upsert a Distribution (DCAT-XML)

```http
PUT /admin/api/v1/distributions/baum-kataster-csv HTTP/1.1
Content-Type: application/rdf+xml
If-Match: "a1b2c3"

<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dcat="http://www.w3.org/ns/dcat#"
         xmlns:dct="http://purl.org/dc/terms/">
  <dcat:Distribution rdf:about="https://portal.example/admin/api/v1/distributions/baum-kataster-csv">
    <dct:title xml:lang="de">Baumkataster (CSV)</dct:title>
    <dcat:accessURL rdf:resource="https://data.example/baeume.csv"/>
    <dcat:downloadURL rdf:resource="https://data.example/baeume.csv"/>
    <dcat:mediaType>text/csv</dcat:mediaType>
  </dcat:Distribution>
</rdf:RDF>
```

```http
HTTP/1.1 200 OK
ETag: "d4e5f6"
Location: https://portal.example/admin/api/v1/distributions/baum-kataster-csv
```

### 5.5 Error model (FR-19)

| HTTP | Meaning | Body |
|---|---|---|
| 400 | Syntactically invalid RDF | Problem+JSON / RDF |
| 404 | Unknown resource | — |
| 409 | Conflict (e.g. deleting a referenced resource) | reference list |
| 412 | `If-Match` does not match (FR-7) | current ETag |
| 422 | DCAT-AP 3 validation failed (FR-4) | **SHACL report** (RDF/JSON) |
| 422 | Model constraint violated — a declared multiplicity or an OCL invariant | violation list (`text/plain`, one per line) |

Both validation layers answer `422`, and they are told apart by the body: SHACL returns an
RDF `sh:ValidationReport`, the model constraints a plain-text list. **Both are checked at the
persistence boundary**, so they hold for every caller of the OSGi services and not only for
requests arriving over REST — what the REST adapter contributes is rendering the refusal
(content negotiation for the report). Unlike SHACL, the model constraints need no operator
configuration — they are annotated on the model itself — and they have no dry-run equivalent
(FR-5 covers the shapes only).

An operator can require SHACL rather than merely enable it, by raising the admin services'
validation reference to a mandatory minimum
(`validationService.cardinality.minimum=1`): the admin services then do not start without a
validation service, so their REST resources are unregistered and the write endpoints answer
`404`. The `admin-write` health check on the `ready` tag reports this as `CRITICAL` and names
the cause.

## 6. OSGi Service API

The primary contract (design principle "OSGi first"). EMF-typed against `dcat.model`; the REST adapter from §5 calls exclusively these services. Split by responsibility.

```java
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.data.atlas.dcat.dcat.Catalog;
import org.eclipse.fennec.data.atlas.dcat.dcat.Dataset;
import org.eclipse.fennec.data.atlas.dcat.dcat.DatasetSeries;   // from AP1
import org.eclipse.fennec.data.atlas.dcat.dcat.DataService;
import org.eclipse.fennec.data.atlas.dcat.dcat.Distribution;

/**
 * CRUD + upsert per DCAT-AP 3 entity (FR-1..FR-8).
 * Implementation: transactional against Jena TDB2, SHACL-validated before commit.
 */
public interface CatalogAdminService {

    // --- Catalog -----------------------------------------------------------
    Catalog upsertCatalog(Catalog catalog);                 // FR-1/2/3/4
    Optional<Catalog> getCatalog(String id);
    List<Catalog> listCatalogs(Query query);
    void deleteCatalog(String id, boolean cascade);         // FR-1, 409 if referenced

    // --- Dataset -----------------------------------------------------------
    Dataset upsertDataset(Dataset dataset);
    Optional<Dataset> getDataset(String id);
    List<Dataset> listDatasets(Query query);
    void deleteDataset(String id, boolean cascade);

    // --- DatasetSeries (from AP1) -----------------------------------------
    DatasetSeries upsertDatasetSeries(DatasetSeries series);
    Optional<DatasetSeries> getDatasetSeries(String id);
    void deleteDatasetSeries(String id, boolean cascade);

    // --- DataService -------------------------------------------------------
    DataService upsertDataService(DataService service);
    Optional<DataService> getDataService(String id);
    List<DataService> listDataServices(Query query);
    void deleteDataService(String id, boolean cascade);

    // --- Distribution (always in the context of a Dataset, FR-10) ----------
    Distribution upsertDistribution(String datasetId, Distribution distribution);
    Optional<Distribution> getDistribution(String id);
    void deleteDistribution(String id);
}
```

```java
/**
 * Relationship/membership maintenance without re-uploading the target resource (FR-9/10/11).
 */
public interface CatalogRelationService {
    void linkDatasetToCatalog(String catalogId, String datasetId);
    void unlinkDatasetFromCatalog(String catalogId, String datasetId);
    void linkServiceToCatalog(String catalogId, String serviceId);
    void unlinkServiceFromCatalog(String catalogId, String serviceId);
    void linkSubCatalog(String catalogId, String subCatalogId);
    void unlinkSubCatalog(String catalogId, String subCatalogId);
    void addSeriesMember(String seriesId, String datasetId);     // from AP1
    void removeSeriesMember(String seriesId, String datasetId);
    void setAccessService(String distributionId, String serviceId);
}
```

```java
import java.io.InputStream;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Bulk ingest of whole DCAT documents (FR-13/14/15) — Data/Model Atlas integration.
 * Embedded: pass an EMF Resource. Over REST: RDF stream + media type.
 */
public interface CatalogIngestService {

    enum Mode { MERGE, REPLACE }

    IngestResult ingest(String catalogId, Resource dcatDocument, Mode mode);     // FR-13/14
    IngestResult ingest(String catalogId, InputStream rdf, String mediaType, Mode mode);
    void purgeCatalog(String catalogId);                                          // FR-14
}
```

```java
/**
 * Validation as a standalone service (FR-4/5) — used by the admin and ingest paths,
 * also callable standalone as a dry run.
 */
public interface DcatValidationService {
    ValidationReport validate(org.eclipse.emf.ecore.EObject resource);   // SHACL DCAT-AP 3
    ValidationReport validate(org.eclipse.emf.ecore.resource.Resource document);
    boolean isValid(org.eclipse.emf.ecore.EObject resource);
}
```

**Companion types:** `Query` (filtering/pagination per §5.1), `IngestResult` (created/updated/rejected + ETags), `ValidationReport` (SHACL-compliant findings). **Errors** as runtime exceptions that the REST adapter maps to §5.5:

```java
public class ValidationException extends RuntimeException { /* carries ValidationReport -> 422 */ }
public class ConflictException   extends RuntimeException { /* reference/ETag -> 409/412        */ }
public class NotFoundException    extends RuntimeException { /* -> 404                          */ }
```

## 7. Mapping REST ↔ OSGi

| REST (§5) | OSGi (§6) |
|---|---|
| `PUT /{coll}/{id}` | `…AdminService.upsertX(...)` (ID from path/payload, D2) |
| `GET /{coll}/{id}` | `getX(id)` + RIOT serialization |
| `DELETE /{coll}/{id}?cascade` | `deleteX(id, cascade)` |
| `PUT /catalogs/{id}/datasets/{dsId}` | `CatalogRelationService.linkDatasetToCatalog` |
| `POST /ingest` | `CatalogIngestService.ingest(...)` |
| `?validate=only` | `DcatValidationService.validate(...)` |
| `If-Match` / `ETag` | optimistic lock in the impl (FR-7, mandatory) |
| *(no `PATCH`)* | change only via `upsertX(...)` replace (FR-2b) |
| RDF body ⇄ EMF | RIOT ⇄ EMF-DCAT bridge (AP2) |

The REST adapter contains **no business logic** — only serialization (RDF⇄EMF), status-code mapping and the auth hook (FR-21). This makes embedded and distributed operation behave identically.

That is a constraint on where rules live, not just a description. The **identity rule is one
of them**: `DcatIds.idForWrite` honours an `about` naming one of ours, mints only when
`about` is absent, and throws `ForeignIdentityException` for anything else. The *service*
enforces it, so an importer or another bundle calling `upsertDataset(...)` directly gets the
same answer as `POST /admin/datasets`; the adapter only turns the refusal into `400`. It was
not always so — the rule lived in the adapter alone, and the service underneath quietly
minted a fresh id for a foreign `about`, which is precisely the kind of drift this principle
exists to prevent.

## 8. Open Points (review)

1. ~~**PATCH form**~~ — **decided (2026-06-27): no PATCH, replacement only via `PUT` upsert** (D8/FR-2b). ETag/`If-Match` mandatory (FR-7).
2. **CatalogRecord:** maintained automatically by the portal (provenance, FR-20) or explicitly via the API?
3. **`replace` ingest semantics:** Purges missing resources per catalog graph — is that the desired default behavior?
4. **Reachability check (FR-17):** synchronous blocking, asynchronous reporting, or purely optional?
5. **Auth (FR-21 / planning §11.7):** Keycloak as in MDO? Which roles (read/write/admin)?
6. **ID scheme:** slugs (readable) vs. opaque UUIDs for the minted part (D2/FR-3).
7. **API versioning:** is the `…/v1` path confirmed?

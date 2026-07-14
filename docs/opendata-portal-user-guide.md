# Fennec DCAT.Atlas — User Guide

> Status: draft. This is the single published, user-facing guide for the DCAT.Atlas
> open-data portal. Internal planning and API-specification documents live
> alongside it in `docs/` and are browsed on GitHub, not published to the site.

## Overview

Fennec DCAT.Atlas is a **DCAT-AP 3** compliant open-data portal. It ingests DCAT
descriptions (as RDF/XML) from the Fennec Data-Atlas and Model-Atlas, persists them
in an [Apache Jena](https://jena.apache.org/) TDB2 triplestore, and serves the
catalog both machine-readably (JSON-LD, Turtle and N3) and human-readably
(a catalog browser UI).

For the architecture, scope and work-package plan, see the internal
[planning document](./opendata-portal-planung.md). For the write-side contract,
see the [admin-API specification](./opendata-portal-admin-api.md).

## Getting started

The portal is an OSGi application. For local development it is launched from the
runtime bundle's bnd run descriptor: run
`org.eclipse.fennec.dcat.atlas.runtime/local.bndrun` (from bndtools, or `bnd run`).
It composes `base.bndrun` (the framework and all DCAT.Atlas bundles) with
`secrets.bndrun` (local paths — see [Validating metadata](#validating-metadata))
and the local configuration bundle.

With the default local configuration the REST API is served at:

```
http://localhost:8085/dcat/rest
```

(HTTP port `8085`, servlet context `dcat/`, JAX-RS application path `rest`.) Every
path in the examples below is relative to that base. Reads live under
`/{collection}` and are open; writes live under `/admin/{collection}` so an upstream
gateway (e.g. APISix/Keycloak) can require authentication there while leaving reads
public.

> _TODO: standalone microservice and container (Docker) deployment._

## Managing the catalog

The portal maintains the catalog through an **admin interface** exposed as two
interchangeable façades over the same operations:

- **OSGi service API** — the primary, EMF-typed contract for embedded callers.
- **REST adapter** — a thin RDF layer over the OSGi API for distributed use.

Managed entities (DCAT-AP 3): `Catalog`, `Dataset`, `DatasetSeries`, `DataService`
and `Distribution`.

The REST examples below use `Catalog`; the other entities follow the same shape
under their own collection (`/admin/datasets`, `/admin/data-services`, …). Bodies
may be sent as `application/rdf+xml`, `application/json` or `application/xml`.

### Creating a catalog

`POST /admin/catalogs` with an RDF/XML body. The server mints an id, sets the
resource's public read URL as its `rdf:about`, stores it, and returns `201 Created`
with a `Location` header and an `ETag`.

`catalog.rdf`:

```xml
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dcat="http://www.w3.org/ns/dcat#"
         xmlns:dct="http://purl.org/dc/terms/"
         xmlns:foaf="http://xmlns.com/foaf/0.1/">
  <dcat:Catalog rdf:about="http://localhost:8085/dcat/rest/catalogs/example">
    <dct:title xml:lang="en">Example Catalog</dct:title>
    <dct:title xml:lang="de">Beispiel-Katalog</dct:title>
    <dct:description xml:lang="en">A catalog created via the admin API.</dct:description>
    <dct:language rdf:resource="http://publications.europa.eu/resource/authority/language/ENG"/>
    <dct:language rdf:resource="http://publications.europa.eu/resource/authority/language/DEU"/>
    <dct:issued>2026-07-14T10:00:00.000+02:00</dct:issued>
    <dct:publisher>
      <foaf:Organization rdf:about="https://data-in-motion.biz">
        <foaf:name xml:lang="en">Data In Motion</foaf:name>
      </foaf:Organization>
    </dct:publisher>
  </dcat:Catalog>
</rdf:RDF>
```

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/catalogs \
  -H 'Content-Type: application/rdf+xml' \
  -H 'Accept: application/rdf+xml' \
  --data-binary @catalog.rdf
```

```
HTTP/1.1 201 Created
Location: http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-…
ETag: "9a1c4d…e7"
```

The `{id}` (last segment of `Location`) and the `ETag` are what you use for the
read, update and delete operations below. On a `POST` the `rdf:about` you send is
overwritten with the minted read URL, so its value does not matter here.

#### Writing RDF/XML bodies

Request bodies are parsed onto the DCAT-AP EMF model, so how a property is written
depends on its type. Three rules cover almost everything (getting one wrong yields a
`400 Bad Request` with a `Feature '…' not found` cause):

- **Text with a language** (`dct:title`, `dct:description`) → an element with
  `xml:lang`: `<dct:title xml:lang="en">…</dct:title>`.
- **A URI reference** (`dct:language`, `dcat:homepage`, `dct:hasPart`) → the
  `rdf:resource` attribute: `<dct:language rdf:resource="http://…"/>`.
- **A nested object** (`dct:publisher`, which is a `foaf:Agent`) → a nested typed
  element, *not* `rdf:resource`. `<dct:publisher>` is itself the agent, so put the
  organisation directly inside it (do not add a `<foaf:Agent>` wrapper):

  ```xml
  <dct:publisher>
    <foaf:Organization rdf:about="https://data-in-motion.biz">
      <foaf:name xml:lang="en">Data In Motion</foaf:name>
    </foaf:Organization>
  </dct:publisher>
  ```

### Updating a catalog (optimistic locking)

`PUT /admin/catalogs/{id}` replaces the catalog under `{id}` (create-or-replace).
Send conditional headers to coordinate concurrent writers (all optional):

- **`If-Match: "<etag>"`** — apply the change only if you hold the current version.
  If someone else has written since you read it, the ETag no longer matches and you
  get `412 Precondition Failed` instead of silently overwriting their change.
- **`If-None-Match: *`** — create only; if the id already exists, `412`. Use this to
  claim a specific id without clobbering an existing catalog.

```bash
curl -i -X PUT http://localhost:8085/dcat/rest/admin/catalogs/3f2b1c8e-… \
  -H 'Content-Type: application/rdf+xml' \
  -H 'Accept: application/rdf+xml' \
  -H 'If-Match: "9a1c4d…e7"' \
  --data-binary @catalog.rdf
```

Returns `200 OK` (replaced) or `201 Created` (new) with the new `ETag`. The ETag is
a strong validator over the stored representation, so re-`PUT`ting byte-identical
content returns the *same* ETag.

> ETag values are quoted strings; keep the double quotes in the header
> (`-H 'If-Match: "…"'`). An unquoted value is rejected as `400`.

### Removing a catalog

`DELETE /admin/catalogs/{id}` → `204 No Content` (or `404` if unknown). It also
honours `If-Match` for optimistic locking.

```bash
curl -i -X DELETE http://localhost:8085/dcat/rest/admin/catalogs/3f2b1c8e-…
```

### Managing catalog membership

Datasets, data services and sub-catalogs are members of a catalog. Add or remove one
without re-sending the whole catalog:

- `POST   /admin/catalogs/{id}/datasets`  — body is a `dcat:Dataset`
- `POST   /admin/catalogs/{id}/services`  — body is a `dcat:DataService`
- `POST   /admin/catalogs/{id}/catalogs`  — body is a sub-`dcat:Catalog`
- `DELETE /admin/catalogs/{id}/datasets/{memberId}` (and `/services/…`, `/catalogs/…`)

Unlike the top-level create, these keep the `rdf:about` you send: its **last path
segment** is the `{memberId}` used to remove the member later, and re-adding the same
`about` is an idempotent no-op. An add returns `200 OK` with the updated catalog and
its new `ETag`.

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/catalogs/3f2b1c8e-…/datasets \
  -H 'Content-Type: application/rdf+xml' -H 'Accept: application/rdf+xml' \
  --data-binary @dataset.rdf
```

## Consuming the catalog

Reads are open and live under the collection path — e.g. `GET /catalogs/{id}` for a
single catalog, `GET /catalogs` for the collection.

### Content negotiation

Ask for a representation with the `Accept` header. The same catalog is available in
several RDF syntaxes plus JSON:

| `Accept` | Format | Notes |
|---|---|---|
| `text/turtle` | Turtle | Most readable; best for eyeballing by hand. |
| `application/ld+json` | JSON-LD | **Standards-based RDF-in-JSON** — use this for interoperable JSON. |
| `application/rdf+xml` | RDF/XML | The on-disk storage syntax. |
| `application/n-triples` | N-Triples | One triple per line; good for diffing/streaming. |
| `text/n3` | N3 | Turtle plus rule/logic features (unused by DCAT data). |
| `application/json` | EMF JSON | Internal object encoding (typed `_type` fields); for round-tripping through this stack — **not** interoperable DCAT. For public JSON, prefer JSON-LD. |

```bash
curl http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-… -H 'Accept: text/turtle'
curl http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-… -H 'Accept: application/ld+json'
```

Turtle, JSON-LD, RDF/XML and N-Triples of the same catalog all encode the identical
RDF graph — pick whichever your consumer prefers.

### Conditional reads (caching)

Every read carries an `ETag`. Send it back on the next request as `If-None-Match` and
the server answers `304 Not Modified` (no body) when nothing has changed:

```bash
curl -i http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-… \
  -H 'If-None-Match: "9a1c4d…e7"'
#   -> HTTP/1.1 304 Not Modified
```

### Catalog browser UI

Human-readable presentation of the catalog.

> _TODO: browser UI URL and screenshots._

## Validating metadata

Metadata is validated against the DCAT-AP.de SHACL shapes. Shapes and the controlled
vocabularies they reference are loaded from operator-configured directories (set in
`secrets.bndrun` for local runs, as `SHACL_SHAPES_DIR` and `SHACL_VOCAB_DIR`); with no
shapes configured, validation is a no-op that reports conformance.

### Dry-run validation

`POST /admin/validate/{collection}` validates a submitted entity **without storing
it**. It always returns `200 OK` with a SHACL `sh:ValidationReport` (in the RDF syntax
you negotiate) and an `X-SHACL-Conforms: true|false` header for a quick check.

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/validate/datasets \
  -H 'Content-Type: application/rdf+xml' -H 'Accept: text/turtle' \
  --data-binary @dataset.rdf
```

Each `sh:ValidationResult` in the report names the failing node (`sh:focusNode`), the
property at fault (`sh:resultPath`), the rule (`sh:sourceShape`) and its severity.
DCAT-AP.de distinguishes **MUSS** (`sh:Violation` — mandatory) from **SOLL**
(`sh:Warning` — recommended); both appear in the report.

### On-write enforcement

When enforcement is enabled (`enforceOnWrite`), `POST`/`PUT` create and replace
operations validate the entity first and reject a non-conformant one with
`422 Unprocessable Entity` and the report — nothing is stored. Only a hard
`sh:Violation` (MUSS) blocks a write; a `sh:Warning` (SOLL) is reported but allowed.

## Further reading

- [Planning document](./opendata-portal-planung.md) — goals, scope, architecture,
  work packages.
- [Admin-API specification](./opendata-portal-admin-api.md) — OSGi + REST write side.

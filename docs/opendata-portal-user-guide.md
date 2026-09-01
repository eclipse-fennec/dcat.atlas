# Fennec DCAT.Atlas — User Guide

> Status: draft. This is the single published, user-facing guide for the DCAT.Atlas
> open-data portal. Internal planning and API-specification documents live
> alongside it in `docs/` and are browsed on GitHub, not published to the site.

## Overview

Fennec DCAT.Atlas is a **DCAT-AP 3** compliant open-data portal. It takes DCAT
descriptions from the Fennec Data-Atlas and Model-Atlas, stores each resource as a
file in the EMF model's own XMI encoding, and serves the catalog machine-readably
(Turtle, JSON-LD, RDF/XML, N-Triples, N3), queryably
([SPARQL](#querying-with-sparql), over an in-memory [Apache
Jena](https://jena.apache.org/) projection of that store) and human-readably (a
catalog browser UI).

Writes are validated twice over: against the model's own constraints, which travel with
the model and need no setup, and against the DCAT-AP.de SHACL shapes, which the operator
supplies. See [Validating metadata](#validating-metadata).

A Java or OSGi application does not have to compose these requests itself: the DCAT.Atlas
**client** hands EMF objects to the portal and maps its answers onto typed exceptions. See
the [Client Guide](./client-guide.md).

For the architecture, scope and work-package plan, see the internal
planning document. For the write-side contract,
see the admin-API specification.

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

### The base URL, and the identities you get back

Every example here uses `http://localhost:8085/dcat/rest`, which is what `run.local`
serves. **That base is configuration, not a constant**, and two things follow from it.

The servlet context and the JAX-RS path are set per deployment, so the prefix differs: the
container image serves `/rest` with no `/dcat` segment by default. Take the base from
whoever runs the portal rather than from this guide.

More importantly, the base is what every `rdf:about` you read is rendered under. Resources
are *stored* with host-free identities (`http://dcat.atlas/catalogs/{id}`) so the same data
can be served from any host, and the configured public base is substituted on the way out.
So:

- the `about` you read back is the URL clients should dereference — it is not necessarily
  the URL you sent the request to (behind a reverse proxy it is deliberately not);
- an `about` you *send* is accepted if it sits under either the public base or the
  host-free one, and refused with `400` otherwise (see below);
- if the `about` values look wrong for your deployment — pointing at `localhost`, or at the
  wrong host — that is the portal's public base URL being misconfigured, not your request.
  It is a single setting (`PUBLIC_BASE_URL` in the shipped configurations) and the portal
  refuses to start without it.

## Managing the catalog

The portal maintains the catalog through an **admin interface** exposed as two
interchangeable façades over the same operations:

- **OSGi service API** — the primary, EMF-typed contract for embedded callers.
- **REST adapter** — a thin RDF layer over the OSGi API for distributed use.

Managed entities (DCAT-AP 3): `Catalog`, `Dataset`, `DatasetSeries`, `DataService`
and `Distribution`.

The REST examples below use `Catalog`. `Dataset`, `DatasetSeries` and `DataService`
follow the same shape under their own collection — `/admin/datasets`,
`/admin/dataset-series`, `/admin/data-services` — and are shown in
[Other entities](#other-entities). `Distribution` is the exception: it has no
collection of its own and is always created inside its dataset, at
`POST /admin/datasets/{datasetId}/distributions`.

**Writes take XMI, reads give you RDF.** A request body is sent as
`application/xmi` — the DCAT-AP EMF model's own XML encoding, which is also the
on-disk format, so a stored entity can be sent straight back. Any other content type
on a write is answered `415 Unsupported Media Type`. Reads are the other half of the
story and are unaffected: you negotiate Turtle, JSON-LD, RDF/XML, N-Triples or N3 with
`Accept`, as [Content negotiation](#content-negotiation) describes, and a write
response honours the same `Accept`.

> `application/xmi`, not `application/xml`. The codec selects by media type, and
> `application/xml` picks a plain-XML codec that does not understand `xmi:version` or a
> literal written in attribute form.

### Creating a catalog

`POST /admin/catalogs` with an XMI body. The server takes the id from the
`about` you send when that names a resource of ours, mints one when you send no
`about` at all, stores the catalog, and returns `201 Created` with a `Location`
header and an `ETag`. Sending the same body twice therefore creates once: the second
`POST` is answered `409 Conflict`, because the identity it names is already taken.
That `409` carries the same `Location` as the `201` did — the catalog that is in the
way — so a retry that conflicts still hands you the URL, and the id at the end of it,
that the next section needs to add datasets.

`catalog.xmi`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
              xmi:version="2.0"
              about="http://localhost:8085/dcat/rest/catalogs/example"
              homepage="https://example.org/opendata">
  <title lang="en" value="Example Catalog"/>
  <title lang="de" value="Beispiel-Katalog"/>
  <description lang="en" value="A catalog created via the admin API."/>
  <language>http://publications.europa.eu/resource/authority/language/ENG</language>
  <language>http://publications.europa.eu/resource/authority/language/DEU</language>
  <issued value="2026-07-14T10:00:00.000+02:00"/>
  <publisher about="https://data-in-motion.biz">
    <name lang="en" value="Data In Motion"/>
  </publisher>
  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
</dcat:Catalog>
```

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/catalogs \
  -H 'Content-Type: application/xmi' \
  -H 'Accept: application/rdf+xml' \
  --data-binary @catalog.xmi
```

```
HTTP/1.1 201 Created
Location: http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-…
ETag: "9a1c4d…e7"
```

The `{id}` (last segment of `Location`) and the `ETag` are what you use for the
read, update and delete operations below.

The `about` on the entity you are storing must be one of ours — a read URL this
portal served, or the logical form behind it — or absent. Any other URI (a publisher's
homepage, the catalogue's own website on another portal) is refused with
`400 Bad Request`: no id is ever carved out of a URL the portal does not own, and
answering `201` while quietly filing your catalogue under an identity you did not
choose would leave you unable to tell one of your catalogues from another. Omit
`about` to have an id minted for you.

This applies to the entity being stored, and to nothing else in the body. The resources
*inside* it — `publisher`, `license`, `contactPoint`, `conformsTo` — are external things
by nature and keep the `about` you send them with, unchanged.
The example above stores a publisher identified by `https://data-in-motion.biz` and
leaves it exactly so.

To replace a catalogue rather than create it, `PUT /admin/catalogs/{id}` — which also
creates, answering `201 Created` when that id was free and `200 OK` when it was not.

A `PUT` takes the identity from the **path**, so the body's `about` must either be
absent or name that same resource; anything else is `400 Bad Request`. In particular an
`about` naming a *different* catalogue of ours is refused rather than written to the
path's id under the other one's name.

This costs you nothing in reach: the path already says which catalogue is meant, so a
catalogue whose natural IRI belongs to somebody else is stored by `PUT`ting it to an id
of your choosing, with no `about` at all. Where that original IRI matters, keep it in
`dct:identifier` or `adms:identifier` — those are data, and are never rewritten.

#### Writing XMI bodies

A body is the EMF model serialised, so how a property is written follows from its type
in the model rather than from RDF syntax. Property elements are **unprefixed** — only
the root element carries the `dcat:` prefix — and getting a name wrong yields a
`400 Bad Request` with a `Feature '…' not found` cause. Four rules cover almost
everything:

- **The identity** → an `about` attribute on the root element (no `rdf:` prefix):
  `about="http://localhost:8085/dcat/rest/catalogs/example"`. See the identity rules
  above for when it is honoured, minted or refused.
- **Text with a language** (`title`, `description`, `keyword`, `name`) → an empty
  element carrying both parts as attributes: `<title lang="en" value="Example Catalog"/>`.
  Repeat the element for each language or each keyword.
- **A URI-valued property** (`language`, `theme`, `themeTaxonomy`, `hasPart`,
  `accessRights`) → the URI as element text, `<language>http://…</language>`, repeated
  for a property that takes several. Where the model allows only one — `homepage`,
  `accrualPeriodicity` — it may also be written as a plain attribute on the element, as
  `homepage` is above.
- **A nested object** (`publisher` and `creator`, which are agents; `license`;
  `contactPoint`) → an element named after the property, carrying that object's own
  `about` and properties. There is no wrapper and no type name, because the model
  already says what the property holds:

  ```xml
  <publisher about="https://data-in-motion.biz">
    <name lang="en" value="Data In Motion"/>
  </publisher>
  ```

  A publisher is a `foaf:Agent`, which is what you get by default. Add
  `xsi:type="foaf:Organization"` (declaring `xmlns:xsi` and `xmlns:foaf`) only when you
  want the narrower type.

A **date** is a literal like the text ones, but with a single `value`:
`<issued value="2026-07-14T10:00:00.000+02:00"/>`.

Membership properties (`dataset`, `service`, `catalog` on a Catalog; `inSeries` on a
Dataset) are references to entities stored in their own right, not nested copies.
Writing one inline links it — the content you put inside is *not* written to the
referenced entity, and a reference to something that does not exist is refused. Prefer
the membership endpoints below.

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
  -H 'Content-Type: application/xmi' \
  -H 'Accept: application/rdf+xml' \
  -H 'If-Match: "9a1c4d…e7"' \
  --data-binary @catalog.xmi
```

Returns `200 OK` (replaced) or `201 Created` (new) with the new `ETag`. The ETag is
a strong validator over the stored representation, so re-`PUT`ting byte-identical
content returns the *same* ETag.

> ETag values are quoted strings; keep the double quotes in the header
> (`-H 'If-Match: "…"'`). An unquoted value is rejected as `400`.

`If-Match` guards the resource it names. The one operation where that is narrower than it
looks is a cascading delete, which also rewrites resources you did not name — see
[Deleting something that is still referenced](#deleting-something-that-is-still-referenced).

### Removing a catalog

`DELETE /admin/catalogs/{id}` → `204 No Content` (or `404` if unknown). It also
honours `If-Match` for optimistic locking.

```bash
curl -i -X DELETE http://localhost:8085/dcat/rest/admin/catalogs/3f2b1c8e-…
```

#### Deleting something that is still referenced

A resource other resources point at is **not** deleted: the request is refused with
`409 Conflict`, and the body names every referrer. That is the referential-integrity rule
(FR-1) — no stored link is ever left resolving to nothing.

To delete it *and* clear the way, ask for a cascade:

```bash
curl -i -X DELETE 'http://localhost:8085/dcat/rest/admin/datasets/luftqualitaet-2026?cascade=true'
```

The cascade unlinks every referrer and then deletes, all in **one commit** — so no reader
ever observes a state where the dataset is gone and a catalog still lists it.

Because a cascade changes resources beyond the one in the URL, it reports them:

| | |
|---|---|
| `?cascade=true` that unlinked something | `200 OK`, `text/plain`, one URL per line — the resources that were **modified** |
| `?cascade=true` with nothing to unlink | `204`, exactly like a plain delete: nothing else changed |
| `cascade` absent or `false` | `204` (or `409` if referenced) — unchanged |

```
HTTP/1.1 200 OK
Content-Type: text/plain

http://localhost:8085/dcat/rest/catalogs/gov
```

**Read that body carefully — two things it is not.**

*Those resources were not deleted.* Only the resource in the URL is. Each one listed was
*modified*: the reference to the deleted resource was removed from it. In the example above
the dataset is gone and `catalogs/gov` still exists, one `dcat:dataset` entry lighter.

*The body carries no ETags* — URLs only, and the `200` sets no `ETag` header either, because
it is a report rather than a representation of anything. Each listed resource does have a
new ETag, but publishing it here would be a trap: an ETag identifies a *representation*, so
recording the new one against the copy you are still holding would make your next
conditional `GET` send `If-None-Match` with it, collect a `304`, and leave you serving the
old body believing it current. The right move is a plain `GET` of each URL, which returns
the new content and its ETag together.

So what the list gives you is exactly *"these URLs changed; drop what you hold for them"* —
enough to evict or re-fetch, in the one round trip the delete already cost you. `cascade`
defaults to `false`, so existing callers are unaffected.

> **`If-Match` covers the target only.** The precondition is evaluated against the ETag of
> the resource named in the URL. A cascade also rewrites the referrers, whose ETags you
> never saw and therefore cannot have checked — so the optimistic-locking guarantee (F-16)
> applies to the resource being deleted and *not* to the resources being unlinked.
>
> That is deliberate: requiring an ETag for every referrer would mean discovering them all
> first, which is the work the cascade exists to avoid. But it is a genuine narrowing of
> what `If-Match` promises on this one operation, so do not read a successful cascade as
> confirmation that the referrers were unchanged when you last saw them.

### Managing membership

Datasets, data services and sub-catalogs are members of a catalog. Add or remove one
without re-sending the whole catalog:

- `POST   /admin/catalogs/{id}/datasets`  — body is a **new** `dcat:Dataset`
- `PUT    /admin/catalogs/{id}/datasets/{memberId}` — no body; adds one that exists
- `DELETE /admin/catalogs/{id}/datasets/{memberId}`
- the same three under `/services/…` (`dcat:DataService`) and `/catalogs/…` (sub-catalog)

**Catalogs are not the only container.** A dataset series lists its datasets and a data
service serves them, and both use the identical three verbs on their own membership path:

| Container | Membership path | Member type | Relation | Held on | Returned / `ETag` |
|---|---|---|---|---|---|
| Catalog | `/admin/catalogs/{id}/datasets/…` | `dcat:Dataset` | `dcat:dataset` | the catalog | the catalog |
| Catalog | `/admin/catalogs/{id}/services/…` | `dcat:DataService` | `dcat:service` | the catalog | the catalog |
| Catalog | `/admin/catalogs/{id}/catalogs/…` | `dcat:Catalog` | `dcat:catalog` (sub-catalog) | the catalog | the catalog |
| DatasetSeries | `/admin/dataset-series/{id}/datasets/…` | `dcat:Dataset` | `dcat:inSeries` | **the dataset** | **the dataset** |
| DataService | `/admin/data-services/{id}/datasets/…` | `dcat:Dataset` | `dcat:servesDataset` | the data service | the data service |

Mind the fourth row. `dcat:inSeries` is a property of the *dataset*, not of the series, so
adding a dataset to a series edits and returns the **dataset** — and `If-Match` on those
requests keys on the dataset's `ETag`, not the series'. The other rows behave the way the URL
reads, editing the container named in the path. The same asymmetry shows up on
`access-service` below, where the link lives on the distribution.

A `Dataset` has **no** membership path of its own: nothing is a member of a dataset. Its
distributions are containment rather than membership — see
[Creating a distribution](#creating-a-distribution) — and its links *out* to a series or a
service are made from the other end, through the rows above.

#### Modifying a resource that has members

**A resource carrying member references can be read, edited and `PUT` straight back.** Keep
the `<dataset href="…"/>`, `<service href="…"/>`, `<catalog href="…"/>` and `<inSeries
href="…"/>` elements in the body and the write is accepted with SHACL enforcement on:

```bash
curl -s .../catalogs/gov -H 'Accept: application/xmi' > catalog.xmi
# edit the title, leave the member references alone
curl -i -X PUT .../admin/catalogs/gov \
  -H 'Content-Type: application/xmi' --data-binary @catalog.xmi                  # 200
```

This used to be refused `422`. A member appears as an `href` that names the target and
nothing else — it cannot carry the target's `rdf:type` — and the body is validated as a graph
in its own right, so `dcat:dataset` pointed at an IRI that, *within that graph*, was not a
`dcat:Dataset`. The write path now hands the shapes the `rdf:type` of every resource the body
references, read from the store, so the class constraint can be answered. Only the type is
borrowed: a member's own non-conformance is never reported against your write.

**The constraint still bites when it should.** A reference to a resource that does not exist
is a `409`, and a reference to one that exists but is the wrong class is still a `422`:

```
# <inSeries href="…/catalogs/gov#/"/> — a catalog where a series belongs
dcat:Dataset: dcat:inSeries MUSS auf eine Klasse vom Typ dcat:DatasetSeries verweisen.
```

**What has not changed: `PUT` replaces.** Leave the member elements out and the links are gone
once it succeeds — the members themselves are untouched and still answer `200` on their own
URLs, but the container no longer lists them. So either send the references you want kept, or
re-link afterwards:

```bash
curl -i -X PUT .../admin/catalogs/gov/datasets/luftqualitaet-2026                # 200
```

The same applies to a dataset's **distributions**, which are containment rather than
references: a `PUT` without them removes them, so a body meant to preserve them has to carry
them.

The `POST` **stores the member and then links it**, so it follows exactly the rules of
`POST /admin/datasets`: the `about` must be one of ours or absent (`400` otherwise),
and a member that already exists is refused with `409 Conflict` rather than replaced.
Both requests that could be meant are named in that `409`:

- to add a dataset that already exists, `PUT /admin/catalogs/{id}/datasets/{memberId}` —
  it carries no body and leaves the dataset's content alone;
- to change the dataset, `PUT /admin/datasets/{memberId}`.

Its `Location` header points at the member that is in the way — `/datasets/{memberId}`,
the dataset's own read URL, not the membership path you posted to.

That second one is deliberately not reachable through this path. A dataset can be a
member of several catalogs, and be listed in a series and served by a data service, all
at once — there is one dataset, referenced from many places. Changing it through
`/admin/datasets/{id}` says so; changing it through one catalogue's membership path
would read as a change to that catalogue while silently rewriting what everyone else
sees.

An add returns `200 OK` with the updated catalog and its new `ETag`.

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/catalogs/3f2b1c8e-…/datasets \
  -H 'Content-Type: application/xmi' -H 'Accept: application/rdf+xml' \
  --data-binary @dataset.xmi
```

### How a membership looks in each format

A member is *referenced*, never copied, so the catalog carries a link rather than the
dataset itself. The two formats spell that link differently, and the difference is worth
knowing before it surprises you:

```xml
<!-- XMI -->
<dataset href="http://localhost:8085/dcat/rest/datasets/luftqualitaet-2026#/"/>

<!-- RDF/XML -->
<dcat:dataset rdf:resource="http://localhost:8085/dcat/rest/datasets/luftqualitaet-2026"/>
```

Both name the same dataset. The trailing **`#/` is XMI pointer syntax, not part of the
id**: an XMI `href` addresses *an object inside a document*, written as the document's
URL, then `#`, then a fragment identifying the object within it. Our documents hold one
object, so that fragment is always `/` — "the root of the document at this URL". RDF has
no such notion; a resource is named by its IRI, so `rdf:resource` carries the bare IRI.

Practical consequences:

- **The identity is the URL without the fragment.** To get the dataset's id, strip
  anything from `#` onward and take the last path segment.
- **You are not looking at two addresses.** A fragment is never sent to a server, so
  requesting the `#/` form and the bare form makes the byte-identical HTTP request. Your
  client discards the fragment before it connects.
- **You may send either form.** On a write, an `href` is accepted with or without `#/`;
  both are normalised to the same stored reference.
- **The fragment is what makes the link resolvable to an EMF client.** If you load an XMI
  response as an EMF model and resolve the reference, EMF needs the fragment to know
  which object in the target document is meant. That is why it is there, and why a
  non-EMF client is usually better served by asking for RDF.

### Other entities

`Dataset`, `DatasetSeries` and `DataService` are created, replaced and deleted exactly as
`Catalog` is — same `POST`/`PUT`/`DELETE`, same identity rules, same `ETag` handling — under
`/admin/datasets`, `/admin/dataset-series` and `/admin/data-services`. The payload differs,
because each type has its own mandatory properties, and so does membership: `DatasetSeries`
and `DataService` each take members on one path, `Catalog` on three, and `Dataset` on none
(see [Managing membership](#managing-membership)).

A **data service** needs at least a title, a description, a publisher and one
`dcat:endpointURL`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dcat:DataService xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
    xmlns:dcat="http://www.w3.org/ns/dcat#"
    about="http://localhost:8085/dcat/rest/data-services/luftqualitaet-api">
  <title value="Luftqualität API" lang="de"/>
  <description value="Abfrageschnittstelle für die Messwerte der Luftqualität." lang="de"/>
  <publisher about="https://www.umweltbundesamt.de/">
    <name value="Umweltbundesamt" lang="de"/>
  </publisher>
  <endpointURL>https://example.org/api/luftqualitaet</endpointURL>
  <endpointDescription>https://example.org/api/luftqualitaet/openapi.json</endpointDescription>
</dcat:DataService>
```

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/data-services \
  -H 'Content-Type: application/xmi' --data-binary @data-service.xmi
```

### Creating a distribution

A distribution is the one entity with **no collection of its own**. It belongs to a
dataset, so it is created in that dataset's context (FR-10) and its `about` nests
accordingly — there is no `POST /admin/distributions`:

```bash
POST /admin/datasets/{datasetId}/distributions
```

`distribution.xmi`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<dcat:Distribution xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
    xmlns:dcat="http://www.w3.org/ns/dcat#"
    about="http://localhost:8085/dcat/rest/datasets/luftqualitaet-2026/distributions/csv"
    format="http://publications.europa.eu/resource/authority/file-type/CSV">
  <title value="CSV-Download" lang="de"/>
  <description value="Alle Messwerte als CSV-Datei." lang="de"/>
  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
  <accessURL>https://example.org/data/luftqualitaet-2026.csv</accessURL>
  <downloadURL>https://example.org/data/luftqualitaet-2026.csv</downloadURL>
</dcat:Distribution>
```

```bash
curl -i -X POST \
  http://localhost:8085/dcat/rest/admin/datasets/luftqualitaet-2026/distributions \
  -H 'Content-Type: application/xmi' --data-binary @distribution.xmi
```

`dcterms:license` is mandatory on a distribution in DCAT-AP.de, and at least one
`dcat:accessURL` is expected. `404` if the dataset does not exist — a distribution
cannot be created before the thing it distributes.

> **XMI shape, once:** a single-valued property is an XML **attribute** (`format`), a
> multi-valued one is a **child element** (`accessURL`, `downloadURL`, `endpointURL`),
> and a reference is a child element (`title`, `license`, `publisher`). That rule
> explains every example above, and it is the usual reason a body that looks right is
> rejected.

Read distributions back under the same nesting:

```bash
GET /datasets/{datasetId}/distributions          # the dataset's distributions
GET /datasets/{datasetId}/distributions/{id}     # one of them
```

#### Linking a distribution to the service that serves it

`dcat:accessService` says which `dcat:DataService` gives programmatic access to a
distribution. It is a link between two things that already exist, so there is no `POST` —
only the two verbs that make and break it:

- `PUT    /admin/datasets/{datasetId}/distributions/{id}/access-service/{serviceId}`
- `DELETE /admin/datasets/{datasetId}/distributions/{id}/access-service/{serviceId}`

Neither carries a body, and the response is the updated **distribution** with its new
`ETag` — the data service is referenced by URI and never modified. `If-Match` therefore keys
on the distribution too.

The data service must already be catalogued: a link to one that is not there is refused with
`404` rather than left dangling. `404` also answers an unknown `datasetId` or distribution
`{id}`, so all three path segments have to name something that exists.

```bash
curl -i -X PUT \
  'http://localhost:8085/dcat/rest/admin/datasets/luftqualitaet-2026/distributions/csv/access-service/luftqualitaet-api'
```

## Consuming the catalog

Reads are open and live under the collection path — e.g. `GET /catalogs/{id}` for a
single catalog, `GET /catalogs` for the collection.

> **A collection with nothing to serve answers `204 No Content`, not `200` with an empty
> list.** There is no body at all, so a client that unconditionally parses the response
> fails on a fresh deployment rather than on a broken one. Check the status before reading
> the body. That covers both an empty collection and a page cursor that has walked past the
> end. `GET /catalogs/{id}` for an id that does not exist is a plain `404`.

### Paging through a collection

`GET /catalogs` answers **one page**, not the whole collection. Without parameters that is
the first 50 entries in id order; `limit` asks for a different size, up to 500.

```bash
curl -i 'http://localhost:8085/dcat/rest/catalogs?limit=2'
#   -> HTTP/1.1 200 OK
#      X-Total-Count: 137
#      Link: <http://localhost:8085/dcat/rest/catalogs?limit=2>; rel="first"
#      Link: <http://localhost:8085/dcat/rest/catalogs?limit=2&after=8c1f…>; rel="next"
#      ETag: "b41e…:2:"
#      Cache-Control: no-cache
```

Two headers carry everything a client needs:

| header | meaning |
|---|---|
| `X-Total-Count` | how many entries the whole collection holds, not just this page |
| `Link … rel="next"` | the URL of the following page. **Absent on the last page** — that, rather than a short page, is how a walk ends |
| `Link … rel="first"` | the collection from the start, at the same page size |

**Follow `rel="next"` rather than building the URL yourself.** The cursor is `after=<id>`,
the last id of the page you just read, and not a numeric offset. That is deliberate: an
offset shifts when somebody inserts or deletes a resource that sorts before your cursor, so
an offset-based walk can skip an entry or serve one twice. Resuming from an id cannot,
because an id does not move when its neighbours change.

A few consequences worth knowing:

- **A cursor stays usable after the resource it names is deleted.** `after` is a position in
  a sorted list, not a reference, so the next page still starts in the right place.
- **A limit outside 1–500 is corrected, never refused.** `?limit=0` serves one entry and
  `?limit=100000` serves 500; the `rel="first"` link always shows the size actually applied.
  A limit that is not a number at all is a `400`, with the parameter named in the body —
  not the `404` that JAX-RS would otherwise give you for an unconvertible query parameter.
- **A cursor past the end is `204 No Content`**, the same as an empty collection.
- **The `ETag` identifies the page, not the collection.** Two pages of an unchanged
  collection have different validators, so `If-None-Match` while walking does what you
  would expect: `304` for the page you already hold, `200` for the next one.

The RDF representations carry no paging vocabulary — a page of Catalogs is a graph of
Catalogs and nothing else, so nothing about this portal's paging ends up in a harvester's
graph. Everything above is in the headers, identically for all eight formats.

### Content negotiation

Ask for a representation with the `Accept` header. The same catalog is available in
several RDF syntaxes, plus JSON, plus HTML for a browser:

| `Accept` | Format | Notes |
|---|---|---|
| `text/turtle` | Turtle | Most readable; best for eyeballing by hand. |
| `application/ld+json` | JSON-LD | **Standards-based RDF-in-JSON** — use this for interoperable JSON. |
| `application/rdf+xml` | RDF/XML | Widely supported; the format the DCAT-AP.de tooling expects. |
| `application/n-triples` | N-Triples | One triple per line; good for diffing/streaming. |
| `text/n3` | N3 | Turtle plus rule/logic features (unused by DCAT data). |
| `application/json` | EMF JSON | Internal object encoding (typed `_type` fields); for round-tripping through this stack — **not** interoperable DCAT. For public JSON, prefer JSON-LD. |
| `application/xmi` | XMI | The model's own encoding, and **the only format writes accept** — so this is the one to ask for when you intend to read, edit and `PUT` back, which round-trips including member references. One thing to know before you build on it: `PUT` *replaces*, so anything you leave out of the body is removed — see [Modifying a resource that has members](#modifying-a-resource-that-has-members). |
| `application/xml` | EMF XML | The model's plain-XML encoding — the same object graph as XMI, written by a different codec. Like XMI it is **not an RDF syntax**: see the note below. On writes, use `application/xmi`. |
| `text/html` | HTML | A page for a browser, **on a single resource only** — a collection answers `406`. It is the same graph as the RDF syntaxes rendered as a property table, so it shows every property the resource has, and it carries a schema.org `application/ld+json` block for crawlers. Ask for RDF if you are parsing; this is for reading. |

```bash
curl http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-… -H 'Accept: text/turtle'
curl http://localhost:8085/dcat/rest/catalogs/3f2b1c8e-… -H 'Accept: application/ld+json'
```

Turtle, JSON-LD, RDF/XML and N-Triples of the same catalog all encode the identical
RDF graph — pick whichever your consumer prefers.

> **The two XML forms are model encodings, not RDF syntaxes.** `application/xmi` and
> `application/xml` serialise the EMF object graph, and by design an RDF parser will not
> read them: their element names sit in the DCAT, DCT and FOAF namespaces and they carry
> `rdf:about`, which makes them *look* like RDF/XML, but references are encoded the EMF way
> — as element text with a resource fragment such as `…/luftqualitaet-2026#/` — rather than
> as `rdf:resource`. Feed one to Jena and it will refuse it. That is the intended split
> (project decision in issue #5): every endpoint speaks XML, and the `GET` endpoints
> *additionally* offer the Jena RDF syntaxes. So choose by what is consuming the bytes — an
> RDF tool wants `application/rdf+xml`, Turtle, JSON-LD or N-Triples; this stack's own
> clients want `application/xmi`.

### Querying with SPARQL

`GET`/`POST /rest/sparql` answers SPARQL 1.1 queries over the whole catalogue at once,
which is what content negotiation on a single resource cannot do: "every dataset with
this theme, published since March, that has a CSV distribution" is one query here and a
crawl otherwise.

It is **read-only by construction** — the endpoint parses queries, so a SPARQL `UPDATE`
is rejected as a parse error rather than being refused by a rule someone could
misconfigure. The store stays the source of truth; SPARQL runs over an in-memory
projection of it.

#### One named graph per resource

This is the thing to know before anything else. Each resource is projected into **its
own named graph**, named by the resource's IRI, and **the default graph is empty**. So
the obvious first query matches nothing:

```sparql
SELECT * WHERE { ?s ?p ?o }               # 0 results, always
SELECT * WHERE { GRAPH ?g { ?s ?p ?o } }  # what you meant
```

Graph names are the same public IRIs the REST API serves, so a graph name is also a URL
you can `GET`.

#### Sending a query

Three forms, all standard SPARQL 1.1 protocol:

```bash
# 1. GET with ?query= (simplest; URL-encode it)
curl -G http://localhost:8085/dcat/rest/sparql \
  --data-urlencode 'query=SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }'

# 2. POST the query as the body — best for anything long
curl -X POST http://localhost:8085/dcat/rest/sparql \
  -H 'Content-Type: application/sparql-query' \
  --data-binary @query.rq

# 3. POST form-encoded
curl -X POST http://localhost:8085/dcat/rest/sparql \
  --data-urlencode 'query=ASK { GRAPH ?g { ?s ?p ?o } }'
```

In a GUI client such as Postman, form 2 needs the `Content-Type` set by hand — the raw
body dropdown has no SPARQL entry, so choose *Text* and add the header yourself.

#### Choosing the response format

`Accept` decides. A client that sends `Accept: */*` (Postman's default) gets the
fallback for the query form:

| Query form | Default | Also available |
|---|---|---|
| `SELECT`, `ASK` | `application/sparql-results+json` | `…+xml`, `text/csv`, `text/tab-separated-values` |
| `CONSTRUCT`, `DESCRIBE` | `text/turtle` | `application/rdf+xml`, `application/ld+json`, `application/n-triples`, `text/n3` |

`SELECT` and `ASK` return **result sets**, which are not RDF — that is why they have
their own media types rather than reusing the ones above.

#### Queries to start from

```sparql
# Which resources exist?
SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }

# Every dataset and its title
PREFIX dcat: <http://www.w3.org/ns/dcat#>
PREFIX dct:  <http://purl.org/dc/terms/>
SELECT ?dataset ?title WHERE {
  GRAPH ?g { ?dataset a dcat:Dataset ; dct:title ?title }
}

# Everything about one resource, as RDF
CONSTRUCT { ?s ?p ?o } WHERE {
  GRAPH <http://localhost:8085/dcat/rest/datasets/luftqualitaet-2026> { ?s ?p ?o }
}

# Does anything carry this title?
PREFIX dct: <http://purl.org/dc/terms/>
ASK { GRAPH ?g { ?s dct:title "GovData Katalog"@de } }
```

#### What the responses mean

| Status | Meaning |
|---|---|
| `200` | Results, in the negotiated format. |
| `400` | The query did not parse. Also what a SPARQL `UPDATE` gets — this endpoint reads only. |
| `503` | The projection is still being built; retry shortly. Deliberately *not* an empty result set, which you could not tell apart from "nothing matches". |
| `500` | Execution failed, or hit the query timeout. |
| `404` | SPARQL is disabled on this deployment. |

Two limits protect the instance: a **30-second** timeout per query, and a **10,000-row**
cap on `SELECT`, applied by narrowing the query's `LIMIT` — a smaller `LIMIT` of your own
always wins.

The projection follows the store automatically, including writes made through the OSGi
services rather than REST. If you ever need to force a rebuild:

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/sparql/reindex
#   -> HTTP/1.1 202 Accepted
```

It runs in the background and queries keep being answered from the previous projection
meanwhile; watch the `sparql` check on `/health/ready` for completion.

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

A write passes through **two independent layers**. They overlap deliberately, and they
fail differently, so it is worth knowing which one rejected a request.

| | Model constraints | SHACL shapes |
|---|---|---|
| What they check | the model's own rules — mandatory properties, and that every IRI-valued field really holds an IRI | the DCAT-AP.de profile, including controlled-vocabulary membership |
| Where they come from | inside the model; nothing to install | operator-supplied `.ttl` files |
| Switched on by | `validateOnWrite` (env `MODEL_VALIDATE`) | `enforceOnWrite` (env `SHACL_ENFORCE`) |
| Also needs | — | a shapes directory (`SHACL_SHAPES_DIR`); **without one, nothing is checked** |
| Enforced at | the store — so every writer is covered, not only HTTP callers | the store, likewise |
| Rejection | `422` with a plain-text list of violations | `422` with an RDF `sh:ValidationReport` |
| Dry run | not available | `POST /admin/validate/{collection}` |

The practical consequence: a portal with no shapes directory configured still enforces the
model constraints, but performs **no profile validation at all** — SHACL reports
conformance because it has nothing to compare against. Configure shapes before treating a
green validation as meaningful.

**What is checked, and what is not.** Both layers apply to every write that *adds or
replaces* content, wherever that content lands — including a `Distribution`, which is
stored inside its `Dataset`, and a membership link, which rewrites its container. A write
that only *removes* content is deliberately exempt: unlinking a member, deleting a
distribution, and the unlinking a [cascade](#removing-a-catalog) does on your behalf are
never refused because of what they are removing. So metadata that is already stored and
does not conform can always be deleted — validation gates what goes in, and never locks
anything in.

### Model constraints

These are declared on the model itself — as multiplicities, and as OCL invariants
annotated on the ecore and evaluated by the [Fennec
M2X](https://github.com/eclipse-fennec/emf.m2x) OCL engine. They are checked when the
entity is stored, so every writer is covered, not only requests arriving over HTTP.

What they enforce:

- **Mandatory properties, per class.** A Catalog, Dataset or DatasetSeries needs
  `dct:title`, `dct:description` and `dct:publisher`. A DataService needs `dct:title`,
  `dct:publisher` and `dcat:endpointURL` — but **not** `dct:description`, which DCAT-AP.de
  does not make mandatory for it. A Distribution needs `dcat:accessURL` and `dct:license`.
- **IRI-valued fields really hold IRIs.** `dcat:theme`, `dcat:accessURL`,
  `dcat:endpointURL`, `dct:language` and some forty other properties are IRI references. A
  bare token like `ENVI`, a relative path, or a value containing a space is refused —
  previously such a value was silently written out as a plain literal instead.
- **Scheme-specific fields.** `foaf:mbox` and `vcard:hasEmail` must be `mailto:` IRIs,
  `foaf:phone` and `vcard:hasTelephone` must be `tel:` IRIs.
- **Every stored entity carries an identity.**

A rejection lists one line per violated rule, naming the property to fix:

```
The required feature 'publisher' of 'Dataset http://dcat.atlas/datasets/air' must be set
The 'ThemeIsIri' constraint is violated on 'Dataset http://dcat.atlas/datasets/air'
The 'HasDescription' constraint is violated on 'Dataset http://dcat.atlas/datasets/air'
```

A named constraint (`ThemeIsIri`, `HasDescription`) is one of the OCL invariants; a
*required feature* message is a multiplicity the model declares. The IRI shown is the
resource's stored identity, not the public one you called.

Enforcement is on in the shipped local and container configurations. Set
`MODEL_VALIDATE=false` to load a corpus that predates a constraint.

> If the OCL engine is missing from a deployment, writes are **refused**, not silently
> accepted unvalidated — every constraint reports as *delegate not found*. A portal that
> suddenly rejects every write with `422` is missing the engine bundle, not carrying bad
> data.

### Profile validation with SHACL

Metadata is also validated against the DCAT-AP.de SHACL shapes. Shapes and the controlled
vocabularies they reference are loaded from operator-configured directories (set in
`secrets.bndrun` for local runs, as `SHACL_SHAPES_DIR` and `SHACL_VOCAB_DIR`); with no
shapes configured, validation is a no-op that reports conformance.

Enforcement happens where the entity is stored, so it covers every writer — an importer or
a migration tool calling the OSGi services directly is checked exactly as an HTTP request
is. Only the *rendering* of a refusal belongs to the REST layer, which is why a rejected
`POST` still comes back as a `sh:ValidationReport` in the RDF syntax you asked for.

#### Requiring the validation service

With shapes configured, a portal should not silently keep accepting writes if the
validation service goes away — a failed shapes reload, a bundle that did not start. The
shipped configurations therefore require it:

```json
"DatasetAdminService": {
    "validationService.cardinality.minimum:int": 1
}
```

That is an OSGi Declarative Services setting: it raises the admin service's optional
reference to a mandatory one, so **without a validation service the admin services do not
start at all** and no write can slip past unchecked. Reads are unaffected.

The symptom, though, is blunt: the admin endpoints answer `404`, not `503`, because the
REST layer unregisters a resource whose service is missing. `GET /health/ready` explains
it — the `admin-write` check reports `CRITICAL` and names both the collections that are
unavailable and whether a missing validation service is the reason. Set the minimum back
to `0` to let writes proceed unvalidated instead.

#### Dry-run validation

`POST /admin/validate/{collection}` validates a submitted entity **without storing
it**. It always returns `200 OK` with a SHACL `sh:ValidationReport` (in the RDF syntax
you negotiate) and an `X-SHACL-Conforms: true|false` header for a quick check.

```bash
curl -i -X POST http://localhost:8085/dcat/rest/admin/validate/datasets \
  -H 'Content-Type: application/xmi' -H 'Accept: text/turtle' \
  --data-binary @dataset.xmi
```

Each `sh:ValidationResult` in the report names the failing node (`sh:focusNode`), the
property at fault (`sh:resultPath`), the rule (`sh:sourceShape`) and its severity.
DCAT-AP.de distinguishes **MUSS** (`sh:Violation` — mandatory) from **SOLL**
(`sh:Warning` — recommended); both appear in the report.

#### On-write enforcement

When enforcement is enabled (`enforceOnWrite`), `POST`/`PUT` create and replace
operations validate the entity first and reject a non-conformant one with
`422 Unprocessable Entity` and the report — nothing is stored. Only a hard
`sh:Violation` (MUSS) blocks a write; a `sh:Warning` (SOLL) is reported but allowed.

## Endpoint reference

Every route, generated from the resource classes. Reads are open; everything under
`/admin` is the write side, which an upstream policy enforcement point is expected to
protect (F-6/F-12). Paths are relative to the base described in
[The base URL](#the-base-url-and-the-identities-you-get-back).

> **There is also a machine-readable descriptor** at `GET /openapi.json` and
> `GET /openapi.yaml` ([#21](https://github.com/eclipse-fennec/dcat.atlas/issues/21)) —
> OpenAPI 3.0, generated from the resources, so it lists every route and the media types
> each one accepts and produces without being able to drift from the code. Use it to
> generate a client. It deliberately carries **no body schemas** (the bodies are XMI of an
> EMF model, which does not reduce to a useful JSON Schema) and no status-code semantics,
> so the identity rules, conditional requests and error taxonomy described elsewhere in
> this guide remain the reference for behaviour. The `servers` entry is the configured
> public base, not the address you happened to call.

**Reads** — `Accept`: `application/xmi`, `application/xml`, `application/json`,
`application/rdf+xml`, `text/turtle`, `application/n-triples`, `application/ld+json`,
`text/n3`. An empty collection answers `204`. A single resource additionally serves
`text/html`; a collection does not, and answers `406` for it.

| | |
|---|---|
| `GET /catalogs` · `GET /catalogs/{id}` | catalogs |
| `GET /datasets` · `GET /datasets/{id}` | datasets |
| `GET /dataset-series` · `GET /dataset-series/{id}` | dataset series |
| `GET /data-services` · `GET /data-services/{id}` | data services |
| `GET /datasets/{datasetId}/distributions` · `GET /datasets/{datasetId}/distributions/{id}` | distributions of a dataset |

**Writes** — `Content-Type: application/xmi` only. `POST` creates (`201`, `Location`,
`ETag`; `409` on a taken identity, `400` on a foreign `about`), `PUT` creates-or-replaces
(`201`/`200`), `DELETE` returns `204` (`404` if unknown, `409` if still referenced). All
honour `If-Match` and `If-None-Match: *`.

The four collection `DELETE`s also take **`?cascade=true`**, which unlinks every referrer
first and answers `200` with the identities it changed — see
[Deleting something that is still referenced](#deleting-something-that-is-still-referenced).
A distribution has no `cascade`: it is contained in its dataset rather than referenced.

`PUT` of a resource that carries member references is refused `422` under SHACL enforcement —
replace it without them and re-link, per
[Modifying a resource that has members](#modifying-a-resource-that-has-members).

| | |
|---|---|
| `POST /admin/catalogs` · `PUT`/`DELETE /admin/catalogs/{id}` | catalogs |
| `POST /admin/datasets` · `PUT`/`DELETE /admin/datasets/{id}` | datasets |
| `POST /admin/dataset-series` · `PUT`/`DELETE /admin/dataset-series/{id}` | dataset series |
| `POST /admin/data-services` · `PUT`/`DELETE /admin/data-services/{id}` | data services |
| `POST /admin/datasets/{datasetId}/distributions` · `PUT`/`DELETE /admin/datasets/{datasetId}/distributions/{id}` | distributions (no collection of their own) |

**Membership and links** — see [Managing membership](#managing-membership) for which entity
each one edits, and therefore which `ETag` applies. `POST` takes a new member in the body;
`PUT` and `DELETE` take no body.

| | |
|---|---|
| `POST /admin/catalogs/{id}/datasets` · `PUT`/`DELETE …/datasets/{datasetId}` | `dcat:dataset` |
| `POST /admin/catalogs/{id}/services` · `PUT`/`DELETE …/services/{serviceId}` | `dcat:service` |
| `POST /admin/catalogs/{id}/catalogs` · `PUT`/`DELETE …/catalogs/{subCatalogId}` | `dcat:catalog` (sub-catalog) |
| `POST /admin/dataset-series/{id}/datasets` · `PUT`/`DELETE …/datasets/{datasetId}` | `dcat:inSeries` — **edits the dataset** |
| `POST /admin/data-services/{id}/datasets` · `PUT`/`DELETE …/datasets/{datasetId}` | `dcat:servesDataset` |
| `PUT`/`DELETE /admin/datasets/{datasetId}/distributions/{id}/access-service/{serviceId}` | `dcat:accessService` — edits the distribution |

**Validation** — dry run, stores nothing. `Content-Type: application/xmi`; the report
negotiates over the RDF syntaxes.

| | |
|---|---|
| `POST /admin/validate/catalogs` · `/datasets` · `/dataset-series` · `/data-services` · `/distributions` | see [Dry-run validation](#dry-run-validation) |

**SPARQL** — see [Querying with SPARQL](#querying-with-sparql).

| | |
|---|---|
| `GET /sparql?query=…` | query in the URL |
| `POST /sparql` (`application/sparql-query`) | query as the body |
| `POST /sparql` (`application/x-www-form-urlencoded`) | form-encoded `query=` |
| `POST /admin/sparql/reindex` | rebuild the projection (`202`) |

**Operations** — not part of the DCAT API, but useful to a client: `GET /health/live` and
`GET /health/ready` (`200`/`503`), served beside `/rest` rather than under it.

## Further reading

- [Client Guide](./client-guide.md) — registering metadata from Java or OSGi: the
  configuration keys, authentication, readiness, and the registration loop.
- Planning document — goals, scope, architecture,
  work packages.
- Admin-API specification — OSGi + REST write side.
- `GET /openapi.json` · `GET /openapi.yaml` — the generated OpenAPI 3.0 descriptor
  ([#21](https://github.com/eclipse-fennec/dcat.atlas/issues/21)), for client generation.

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

> _TODO: installation / run instructions (standalone microservice and embedded
> OSGi deployment)._

## Managing the catalog

The portal maintains the catalog through an **admin interface** exposed as two
interchangeable façades over the same operations:

- **OSGi service API** — the primary, EMF-typed contract for embedded callers.
- **REST adapter** — a thin RDF layer over the OSGi API for distributed use.

Managed entities (DCAT-AP 3): `Catalog`, `Dataset`, `DatasetSeries`, `DataService`
and `Distribution`.

> _TODO: worked CRUD examples for each entity._

## Consuming the catalog

- **Content negotiation** — `application/ld+json`, `text/turtle`, `text/n3`.
- **Catalog browser UI** — human-readable presentation.

> _TODO: endpoint URLs and content-negotiation examples._

## Further reading

- [Planning document](./opendata-portal-planung.md) — goals, scope, architecture,
  work packages.
- [Admin-API specification](./opendata-portal-admin-api.md) — OSGi + REST write side.

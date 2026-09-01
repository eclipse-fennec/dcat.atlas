---
layout: home

hero:
  name: Fennec DCAT.Atlas
  text: DCAT-AP Open Data Portal
  tagline: A DCAT-AP 3 compliant open-data portal fed from the Fennec Data-Atlas and Model-Atlas — EMF-modelled catalogs in a git-backed store, served as JSON-LD, Turtle, RDF/XML, N-Triples and N3, and queryable with SPARQL.
  image:
    src: /fennec-logo.png
    alt: Eclipse Fennec logo
  actions:
    - theme: brand
      text: User Guide
      link: /guides/user-guide
    - theme: alt
      text: View on GitHub
      link: https://github.com/eclipse-fennec/dcat.atlas

features:
  - icon: 🗂️
    title: DCAT-AP 3 Catalog Core
    details: DCAT-AP 3 EMF model as the single source of truth — Catalog, Dataset, DatasetSeries, DataService and Distribution, each stored as one XMI file in a git-backed store, a commit per operation.
    link: /guides/user-guide
    linkText: User Guide
  - icon: 🔌
    title: Admin API (OSGi + REST)
    details: Two interchangeable façades over the same logic — an EMF-typed OSGi service for embedded callers and a thin RDF REST adapter for the distributed microservice. A client library hands EMF objects to either.
    link: /guides/client-guide
    linkText: Client Guide
  - icon: 🌐
    title: Machine- & Human-readable
    details: Content-negotiated output as JSON-LD, Turtle, RDF/XML, N-Triples, N3 and XMI, an HTML representation of every entity, and a SPARQL endpoint over an in-memory Apache Jena projection.
  - icon: 🦊
    title: Standalone or embedded
    details: Runs as an independent microservice or container. The Model-Atlas and Data-Atlas feed it through the client library rather than by being harvested — it publishes internally provided data only.
---

## About Fennec DCAT.Atlas

Fennec DCAT.Atlas (`org.eclipse.fennec.dcat.atlas`) is the **publication layer**
over the Fennec **Data-Atlas** and **Model-Atlas**. DCAT descriptions arrive as
`application/xmi` — the DCAT-AP EMF model's own encoding — and each resource is stored
as **one XMI file in a git-backed store**, which is the one truth of the catalog
holdings. From there the catalog is delivered **machine-readable** (JSON-LD, Turtle,
RDF/XML, N-Triples, N3), **queryable** (a SPARQL endpoint over a disposable in-memory
**Apache Jena** projection of that store) and **human-readable** (an HTML
representation of every entity, and a catalog browser UI).

The catalog holdings are maintained through the **admin interface** — an OSGi
service API and a REST adapter over the same operations — for creating, updating
and deleting `Catalog`, `DataService`, `Dataset`, `DatasetSeries` and
`Distribution`. A Java and OSGi **client library** hands EMF objects to it directly;
see the [Client Guide](/guides/client-guide).

> **Status:** early development. See the [User Guide](/guides/user-guide) for the
> portal's current capabilities and the [Client Guide](/guides/client-guide) for
> registering metadata with it; the planning and admin-API specifications in the
> repository carry the design details.

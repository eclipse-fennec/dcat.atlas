---
layout: home

hero:
  name: Fennec DCAT.Atlas
  text: DCAT-AP Open Data Portal
  tagline: A DCAT-AP 3 compliant open-data portal fed from the Fennec Data-Atlas and Model-Atlas — EMF-modelled catalogs on an Apache Jena triplestore, served as JSON-LD, Turtle and N3.
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
    details: DCAT-AP 3 EMF model as the single source of truth — Catalog, Dataset, DatasetSeries, DataService and Distribution, persisted in an Apache Jena TDB2 triplestore.
    link: /guides/user-guide
    linkText: User Guide
  - icon: 🔌
    title: Admin API (OSGi + REST)
    details: Two interchangeable façades over the same logic — an EMF-typed OSGi service for embedded callers and a thin RDF REST adapter for the distributed microservice.
  - icon: 🌐
    title: Machine- & Human-readable
    details: Content-negotiated output as JSON-LD, Turtle and N3, alongside a human-readable catalog browser UI.
  - icon: 🦊
    title: Standalone or embedded
    details: Runs as an independent microservice or embedded as OSGi services inside the Model-Atlas or Data-Atlas. No harvesting — it publishes internally provided data only.
---

## About Fennec DCAT.Atlas

Fennec DCAT.Atlas (`org.eclipse.fennec.dcat.atlas`) is the **publication layer**
over the Fennec **Data-Atlas** and **Model-Atlas**. DCAT descriptions arrive as
RDF/XML ("DCAT-XML"), are loaded into an **Apache Jena** model, persisted in a
**TDB2 triplestore** as the one truth of the catalog holdings, and delivered both
**machine-readable** (JSON-LD, Turtle, N3) and **human-readable**
(catalog browser UI).

The catalog holdings are maintained through the **admin interface** — an OSGi
service API and a REST adapter over the same operations — for creating, updating
and deleting `Catalog`, `DataService`, `Dataset`, `DatasetSeries` and
`Distribution`. An optional outbound connector can forward to Fraunhofer Piveau.

> **Status:** early development. See the [User Guide](/guides/user-guide) for the
> current capabilities, and the planning and admin-API specifications in the
> repository for design details.

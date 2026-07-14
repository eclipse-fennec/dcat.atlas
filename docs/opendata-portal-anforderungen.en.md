# DCAT-AP.de Open Data Portal — Requirements

> As of: 2026-07-06 · Status: Draft
> Belongs to the "DCAT portal" module from `docs/fennec-module-arbeitspakete.md` (§3).
> Project/license: Eclipse Fennec project; developed as open source under the Eclipse Public License 2.0 (EPL-2.0).
> Purpose: a standalone, purely functional requirements document for the module. Describes **what** the portal must be able to do — the technical implementation (architecture, frameworks, concrete classes/bundles) is deliberately **not** the subject of this document and remains the domain of the technical planning docs (`docs/opendata-portal-planung.md`, `docs/opendata-portal-admin-api.md`).

## 1. Purpose

The portal publishes catalogs of open data according to the German national metadata standard **DCAT-AP.de, version 3.0**. It is the publication layer for metadata from upstream systems and addresses both human visitors (catalog browsing) and machines/applications (further processing, display in third-party systems, open-data search engines).

## 2. Managed objects

The portal manages the central DCAT-AP.de object types:

- **Catalog** (`Catalog`) — a collection of datasets and services; may contain sub-catalogs.
- **Dataset** (`Dataset`) — a published dataset with description, themes, keywords, license.
- **Dataset series** (`DatasetSeries`) — a grouping of multiple related datasets (e.g. recurring surveys).
- **Distribution** — a concrete, retrievable form of a dataset (e.g. a CSV file, an endpoint), including access/download address, format, and license.
- **Data service** (`DataService`) — a queryable service (e.g. an API) that serves datasets.

In addition, related information (publisher, creator, contact point, themes/keywords, licenses) is maintained.

## 3. User groups

- **Visitor (anonymous):** browses and reads the catalog without logging in.
- **Editor/administrator (logged in, authorized):** creates, changes, and deletes catalog entries, according to their assigned permissions.
- **Machine consumers:** external systems (Data-Atlas, Model-Atlas, SensiNact, third-party portals, search engines) that consume catalog data, either reading or writing, via the interface.

## 4. Functional requirements

### 4.1 Catalog browser (human reading)

- **F-1:** There is a web interface through which visitors can browse catalogs, datasets, dataset series, distributions, and data services without logging in.
- **F-2:** The interface provides an overview as well as detail views per object (e.g. a dataset with its distributions, a catalog with its contained datasets).
- **F-3:** A search/filter function (e.g. by theme, keyword, publisher, catalog) helps in finding datasets.

### 4.2 Administration interface (CRUD with permissions)

- **F-4:** There is a web interface through which authorized users can create, view, change, and delete catalogs, datasets, dataset series, distributions, and data services (CRUD).
- **F-5:** Access to the administration interface requires logging in.
- **F-6:** Login happens via OAuth. Enforcement of authentication/authorization is handled by the upstream infrastructure: APISix as the Policy Enforcement Point (PEP), Keycloak as the Identity Provider/Policy Decision Point (PDP).
- **F-7:** Access to write operations is permission-controlled — it can be configured who is allowed to create, change, or delete which objects.
- **F-8:** When creating/editing, the interface guides the user through the mandatory and recommended fields required by DCAT-AP.de 3.0 and flags missing/invalid entries before saving.

### 4.3 Persistent data storage

- **F-9:** All catalog data is stored durably and without loss.
- **F-10:** Data storage is implemented via a JPA-based relational database (reference: PostgreSQL).

### 4.4 Machine-readable interface (REST, CRUD)

- **F-11:** There is a REST-based interface through which the same CRUD operations (create, read, update, delete) as in the administration interface can be performed programmatically.
- **F-12:** Write operations on the REST interface by machine consumers are secured via client authentication (OAuth client credentials); enforcement is likewise handled by the upstream infrastructure (APISix as PEP, Keycloak as PDP, cf. F-6).
- **F-13:** This interface supports both **JSON** and **XML** for input and output.
- **F-14:** Repeated creation/modification of the same object results in a consistent end state (no duplicates from repeated execution).
- **F-15:** The REST interface is documented via an OpenAPI description.
- **F-16:** Modifying and deleting operations check the last-read state of the object (ETag/If-Match) to prevent concurrent changes from silently overwriting each other; if the state is stale, the operation is rejected.
- **F-17:** A change always replaces the entire object (replace-only). Partially modifying individual fields (PATCH) is not supported.

### 4.5 Machine-readable RDF formats (read side)

- **F-18:** The reading (querying) operations of the interface deliver the catalog data, in addition to JSON/XML, in the RDF standard formats mandatory for DCAT-AP:
  - **RDF/XML**
  - **Turtle**
  - **N-Triples**
  - **JSON-LD**
- **F-19:** Which format is delivered is determined by the requester via the `Accept` MIME type of the request (content negotiation).

### 4.6 Standards compliance

- **F-20:** All managed objects and their mandatory/recommended fields comply with **DCAT-AP.de 3.0**.
- **F-21:** Input (via UI or interface) is validated against the requirements of DCAT-AP.de 3.0; on violation, nothing is saved and a comprehensible response indicates which field is missing or invalid.
- **F-22:** License information on datasets/distributions is validated against a controlled license vocabulary (standard licenses, e.g. GovData and Creative Commons licenses).

### 4.7 Delivery/operations

- **F-23:** The portal is delivered as a Docker image so it can be operated in a containerized fashion.
- **F-24:** Container configuration (e.g. database connection, public base URL) is done via environment variables.
- **F-25:** The portal provides health and readiness endpoints for operational monitoring.

### 4.8 UI customization

- **F-26:** The web interface can be easily customized to match the operator's look and feel via CSS (including header, logo, font).

### 4.9 Accessibility & multilingualism

- **F-27:** The catalog browser and the administration interface meet accessibility requirements (BITV 2.0 / WCAG), targeting at least conformance level **AA**.
- **F-28:** The interfaces are usable in two languages (German, English); users can switch the language.

### 4.10 Legal

- **F-29:** The portal offers a legal notice ("Impressum") and a privacy policy as dedicated pages; their content can be customized by the operator, or alternatively an external URL can be linked instead of a dedicated page.

## 5. Out of scope

- **No harvesting** — only data that is either self-maintained or fed in from Data-Atlas/Model-Atlas/SensiNact is published; no automatic ingestion of external catalogs.
- **No storage of the actual payload data** — the portal stores only metadata and references (access/download URLs); the actual files/data reside externally.
- **No partial updates** — a change always affects the entire object, not individual fields (cf. F-17).

## 6. Open questions

- The concrete role/permission structure (which roles exist, how granular permissions are assigned) is still to be defined.
- The scope of the search/filter function in the catalog browser (e.g. full-text search) is still to be defined.
- Whether the portal must be externally harvestable by third parties (e.g. GovData.de, European Data Portal) is still to be clarified.

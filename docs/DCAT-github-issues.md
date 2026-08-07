# DCAT Github Issues

### [DCAT] WP-DCAT-02 - Update DCAT-AP model to version 3 (https://github.com/DataInMotion/xdp/issues/1) - Status CLOSED

https://www.dcat-ap.de/def/dcatde/3.0/spec/

Create a model project similar to the MDO one:
 https://github.com/de-jena/MDO/tree/main/de.jena.mdo.dcatap.de.model

### [DCAT] WP-DCAT-1 - Create initial project setup gradle (https://github.com/DataInMotion/xdp/issues/2) - Status CLOSED

### [DCAT] Use Apache Jena for N3, Turtle and JsonLD (https://github.com/DataInMotion/xdp/issues/3) - Status CLOSED

[Apache Jena](https://jena.apache.org/download/index.cgi) does the serialization of dcat in jsonLD, Turtle and N3.

### [DCAT] Update Osgi-fied Apache Jena Dependencies and move them in the gecko library project (https://github.com/DataInMotion/xdp/issues/4) - Status CLOSED

An osgified version of the apache jena dependencies exists in our [RDF project](https://github.com/geckoprojects-org/org.gecko.rdf).
 We have to update them to the latest version (currently 6.1.0) and move them to the gecko libraries project

### [DCAT] AP-DCAT-1 - Setup Repo and Documentation (https://github.com/DataInMotion/xdp/issues/9) - Status CLOSED

### [DCAT]  WP-DCAT-4 Relationship/Memberships endpoints (https://github.com/DataInMotion/xdp/issues/29) - Status CLOSED

As per points FR9/10/11 of the document opendata-portal-admin-api_EN.md:

- **FR-9 (Catalog membership):** Dataset, DataService and sub-catalog can be assigned to / removed from a catalog **without** re-sending the target resource in full.
- **FR-10 (Distribution composition):** Distributions are created/deleted in the context of their Dataset; a Distribution without a Dataset is not allowed. `accessService` optionally references a DataService.
- **FR-11 (Series membership):** Datasets can be assigned to / removed from a DatasetSeries (`inSeries`/`seriesMember`) — *depends on AP1*.

### ### [DCAT] WP-DCAT-4 SHACL Input Validation  (https://github.com/DataInMotion/xdp/issues/30) - Status CLOSED

[opendata-portal-admin-api_EN.md](https://github.com/user-attachments/files/29841003/opendata-portal-admin-api_EN.md)

As per points FR4/5 of the document:

- **FR-4 (Validation):** Write operations validate against DCAT-AP 3 constraints (SHACL); on violation there is **no** commit and a structured validation report is returned.
- **FR-5 (Pure validation run):** Input can be validated **without** writing (dry run).

## [DCAT] WP-DCAT-4 - CRUD Operations for DCAT (https://github.com/DataInMotion/xdp/issues/5) - Status OPEN

We have to provide a REST api with CRUD operations for DCAT.
 All the endpoints will ask/provide XML.
 **Only** the GET endpoints, in addition, will provide jsonLD, Turtle and N3 (via apache Jena).
 The actual logic should be embedded in an OSGi service, and dcat data  should be persisted, either via fennec persistence or file-based.

### Sub-Issues

#### [DCAT] Message Body Writer using Apache Jena (https://github.com/DataInMotion/xdp/issues/6) - Status CLOSED

We need to provide a Message Body Writer for the jsonLD, Turtle and N3  so that the rest endpoints which provide these formats as Accept header  can serialize the payload.

#### [DCAT] AP-DCAT-4 Add ETags to endpoints (https://github.com/DataInMotion/xdp/issues/28) - Status CLOSED

We need a ETag mechanism in the rest api to cover point **F-16** of the opendata-portal-anforderungen.md document:

**F-16:** Modifying and deleting operations  check the last-read state of the object (ETag/If-Match) to prevent  concurrent changes from silently overwriting each other; if the state is stale, the operation is rejected.

#### [DCAT] WP-DCAT-3 Persistence of Dcat objects (https://github.com/DataInMotion/xdp/issues/32) - Status OPEN

Currently only a simple file persistence has been implemented.
 We need to investigate whether fennec persistence of jena is better and implement that.



### [DCAT] WP-DCAT-06 Client Library for integration (Data-Atlas, Model-Atlas, SensiNact)  (https://github.com/DataInMotion/xdp/issues/7) - Status OPEN

We need a client, like the one in [MDO](https://github.com/de-jena/MDO/blob/main/de.jena.piveau.rest.jakarta/src/de/jena/piveau/rest/jakarta/PiveauRestConnector.java)

- *Goal:* a simple Java client library through which Data-Atlas, Model-Atlas, and SensiNact register DCAT elements with the portal.
- Develop the client library (counterpart to the DCAT provider in the  MDO); DCAT registration via a simple Java library instead of direct REST handling.
- Evidence: registration of DCAT elements via the client library; end-to-end test.

> Integrating the consumers (Data-Atlas, Model-Atlas,  SensiNact) with the portal is a work package within the respective  module (Data-Atlas WP-DA-10, Model-Atlas WP-MA-5, SensiNact WP-SN-4) and each depends as a blocker on the client library **WP-DCAT-6**.

### [DCAT] Convert Ecore into RDF  (https://github.com/DataInMotion/xdp/issues/8) - Status OPEN

[Lower priority with respect to the other DCAT issues]
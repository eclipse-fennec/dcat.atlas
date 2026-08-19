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
### [DCAT] `cascade` delete is implemented but not reachable over HTTP - Status DRAFT (not yet filed)

*Found 2026-08-19 while smoke-testing the git-backed store against a running portal.*

**Summary.** Every DCAT resource can be deleted with a cascade — unlink the referrers first,
then remove the resource — and the whole path is implemented, tested and working at the OSGi
service layer. It cannot be triggered through the REST API. Deleting a resource that anything
still references therefore always fails with `409 Conflict`, and a client has no way to ask
for the cascade.

**Where it is.** Each admin resource in `org.eclipse.fennec.dcat.atlas.rest` hard-codes the
flag, e.g. `DatasetAdminResource.deleteDataset`:

```java
@DELETE
@Path("/{id}")
public Response deleteDataset(@PathParam("id") String id, @Context Request request) {
    ...
    datasetAdminService.deleteDataset(id, false);   // <- always false
    return Response.noContent().build();
}
```

`grep -rn "cascade\|QueryParam" org.eclipse.fennec.dcat.atlas.rest/src` returns nothing: there
is no query parameter anywhere in the REST layer. The same hard-coded `false` appears in the
catalog, data-service, dataset and dataset-series admin resources.

**What works today.** `CatalogAdminService.deleteCatalog(String, boolean)` and its siblings
take the flag and honour it; `References.detach(store, collection, id, cascade)` implements
both halves — refuse when `cascade` is false, unlink every referrer when it is true. Covered
by `CatalogAdminServiceImplTest.cascadeUnlinksThenDeletes` and, since the git store landed, by
`GitCommitBoundaryTest.aCascadeDeleteIsOneCommit` (which also shows the cascade is a single
commit, so it is atomic).

**Reproduce.**

```bash
# create a catalog, add a dataset to it, then try to delete the dataset
curl -X DELETE "$BASE/admin/datasets/$ID"                 # 409, correct
curl -X DELETE "$BASE/admin/datasets/$ID?cascade=true"    # 409 - the parameter is ignored
```

**Impact.** A client that has linked a dataset into a catalog cannot delete it through the
API at all: it must first discover every referrer, unlink each one, and only then delete —
which is exactly the sequence the service-side cascade exists to perform atomically. Doing it
client-side is also several requests and several commits where the service does one.

**Proposed fix.** Add `@QueryParam("cascade") @DefaultValue("false") boolean cascade` to the
five admin `@DELETE` methods and pass it through. Defaulting to `false` keeps the current
behaviour for existing callers, so this is additive.

**Decided 2026-08-19:**

**1. A cascade returns `200` with the identities it modified, not `204`.**
A cascade can unlink an arbitrary number of other resources, and every one of their ETags
moves. A `204` tells the client nothing, so a client holding any of those resources would go
on using a stale ETag until something 412s at it. Returning what changed makes the side
effects of an operation the client explicitly asked for visible to it, and lets it invalidate
its own caches in one round trip.

- `?cascade=true` that actually unlinked something → `200` + the modified identities.
- `?cascade=true` that had nothing to unlink → `204`, same as a plain delete. Nothing else
  changed, so there is nothing to report, and the response stays honest about that.
- Plain delete (`cascade` absent or `false`) → `204` as today. Unchanged for every existing
  caller.

The identities to report are exactly what `ResourceInUseException.getReferencedBy()` already
computes for the refusal — the same list, on the other branch of the same decision. The `409`
body already names them as text, so the information is not new; it is only unavailable on the
path that acts on it.

*Representation is the one implementation choice left.* The delete endpoints have no
`@Produces` today, and a list of IRIs is not a DCAT entity, so the collection pattern
(`GenericEntity<List<Catalog>>` through the RDF body writers) does not fit. Cheapest
consistent option: `text/plain`, one IRI per line, matching how the `409` body already
reports the same list. If a machine-readable form is wanted, it should be a deliberate small
model type rather than an ad-hoc JSON shape — do not invent one in passing.

**2. `If-Match` keeps its current meaning, and it gets documented.**
The precondition is evaluated against the *target's* ETag only. A cascade also rewrites the
referrers, whose ETags the caller never saw and therefore cannot have checked — so the
optimistic-locking guarantee of F-16 covers the resource being deleted and not the resources
being unlinked.

That is the right semantics: the caller is explicitly asking for the side effects, and
requiring it to supply an ETag for every referrer would make a cascade impossible to perform
without first enumerating them — which is precisely the work the cascade exists to avoid.
But it is a real narrowing of what `If-Match` promises, so it must be written down rather
than inferred:

- in the user guide, next to the delete operations and the F-16 description;
- in the javadoc of the `deleteX(String, boolean)` service methods, since direct OSGi callers
  see no HTTP preconditions at all.

**Not a regression.** The REST layer has never exposed the flag; this was found by testing
the API rather than the service, not by breaking anything.

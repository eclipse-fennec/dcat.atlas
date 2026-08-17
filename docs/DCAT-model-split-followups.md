# Follow-ups after the `rdf:Resource` split (N28)

Working note, 2026-08-12. Resume point for the model work.

## Status, 2026-08-12 (second pass)

Sections 1 and 4-of-the-plan are **done**; the whole workspace builds and **85 tests pass**
(`sparql` 16, `impl` 52, `msg.body.writer` 17).

- **Section 1 applied.** `AgentType`, `LicenseDocumentType`, `Distribution` → `NodeResource`;
  `ContributorID` → `RefResource`; plus `DcatResource` → `NodeResource`, which was not in the
  table below and is how Catalog/Dataset/DataService/DatasetSeries now get their `about`.
  `AgentType.name` also gained `upperBound="-1"` per §4.9.
- **Plan step 4 applied.** `Catalog.catalog`, `Catalog.dataset`, `Catalog.service` and
  `Dataset.inSeries` are all `RefResource` now. The last feature that emitted Jena-rejected RDF
  is gone.
- **Plan step 3 applied.** The 12 remaining compile errors (down from 21) were migrated through
  `RdfPointers` in `CatalogAdminServiceImpl`, `DatasetSeriesAdminServiceImpl` and
  `DistributionAdminServiceImpl`, following the `addDatasetToCatalog` template; five call sites
  in three test classes moved from `getAbout()` to `getResource()`.
- **`UnprojectableResourceTest` rewritten.** It used to build its unprojectable resource by
  embedding a `DataService` in `dcat:service` — the pointer model makes that unrepresentable, so
  it now uses a malformed `rdf:about`. The `assertEquals(1L, graphs)` assertion keeps it honest.

- **Section 4 coverage added**, and it immediately earned its keep — see the new defect below.
  `PointerModelRdfComplianceTest` is now 23 tests (was 17): `publisherKeepsItsIri`,
  `contributorIdKeepsItsIri`, `distributionKeepsItsOwnRdfAbout`,
  `licenseKeepsItsIriThroughTheWrapper`, `identityBearingFeaturesSurviveTheRoundTrip`, and
  `licenseKeepsItsIri` (`@Disabled`, pinned to the defect). Workspace total: 91 tests, 1 skipped,
  0 failures.

## Status, third pass (2026-08-12) — sections 0, 1, 2 and 4 all closed

- **`Distribution.license` fixed** — retyped to the `terms.LicenseDocument` wrapper.
  `licenseKeepsItsIri` is re-enabled and **passes**, so the defect is confirmed gone by test, not
  by inspection. Nothing is `@Disabled` any more.
- **`AgentType` spec deviations fixed** — `mbox` lost `lowerBound="1"`, `name` gained
  `upperBound="-1"`.
- **Section 2 complete for every real feature.** Verified by script: **0** non-derived features
  still typed against the abstract `Resource`, down from 15. `AgentType.phone` and `.mbox` are
  `RefResource` (`foaf.ecore:58`, `:66`) — correct, since both point at `tel:`/`mailto:` IRIs and
  are never described nodes. Only the **18 derived document-root holders** remain, which are
  `volatile`/`transient`/`derived` XSD-import artifacts and harmless.
- **Workspace green: 91 tests, 0 skipped, 0 failures** (sparql 16, impl 52, msg.body.writer 23).

**Still open:** section 3 (8 classes with a local `about` — cosmetic), the 18 derived holders
(cosmetic), and the triples→EMF Jena reader.

## 0. `Distribution.license` emitted a bogus empty literal — FIXED

Kept as the worked example of this failure mode, which is the one to watch for. Not an N28
regression; a pre-existing defect the new coverage exposed.

`Distribution.license` (`dcatap.ecore:292`) is typed **directly** as
`terms.ecore#//LicenseDocumentType`, whereas `DcatResource.license` (`dcatap.ecore:703`) goes
through the `terms.ecore#//LicenseDocument` **wrapper**. Without the wrapper there is no extra
element level, so `<dct:license>` *is* the node element and `rdf:about` sits on a property
element, where RDF/XML does not allow it. Measured output:

```turtle
<…/distributions/x1>  rdf:type       dcat:Distribution ;
                      terms:license  "" ;          # ← the licence IRI, gone
                      terms:title    "CSV download"@en .
```

Jena drops the attribute and the empty element content becomes the literal `""`. Because
`dct:license` is `lowerBound="1"` on Distribution, **every** distribution that sets a licence
emits this. Note it is not even a blank node — asserting "the object is an IRI" is what catches
it; asserting "it parsed" does not.

Fix: retype the feature to `terms.ecore#//LicenseDocument` and regenerate.
`licenseKeepsItsIriThroughTheWrapper` already proves that shape works on `Dataset`. This is a
**breaking change** to `Distribution.setLicense(LicenseDocumentType)` → `setLicense(LicenseDocument)`,
so callers need a sweep. Re-enable `licenseKeepsItsIri` afterwards — it passes as written.

Checked for the same mistake elsewhere (2026-08-12). There are 12 wrapper/`…Type` pairs in the
workspace — `adms.Identifier`, `foaf.Document`, `locn.Address`, `rdf.Statement`,
`skos.ConceptScheme`, `terms.LicenseDocument`/`Location`/`PeriodOfTime`/`ProvenanceStatement`/
`RightsStatement`/`Standard`, `vcard.Address`. Every other feature typed with one of those
`…Type` classes is the wrapper's *own* inner feature, which is correct by design.
**`Distribution.license` is the only consumer feature that bypasses its wrapper**, so this is a
one-line fix, not a class of defects.

## What was already done in the first pass

`rdf.ecore` now has abstract `Resource` + concrete `RefResource` (`resource`, `lowerBound="1"`)
and `NodeResource` (`about`). The reification family was made consistent with it:

| class | state |
|---|---|
| `Description`, `StatementType` | `extends NodeResource`, local `about` removed ✅ |
| `SubjectType`, `PredicateType` | `extends RefResource` ✅ |
| `ObjectType` | supertype dropped (it is the literal branch; `value` + `datatype`) ✅ |
| `Statement`, `RDFRoot` | unchanged, correct — wrapper / XML document element ✅ |

`dcatap.genmodel` is in sync, `RdfFactory` now exposes `createRefResource()` /
`createNodeResource()` and no longer `createResource()`.

Verified green: `:org.eclipse.fennec.dcat.atlas.dcatap.de.model:compileJava` and
`:org.eclipse.fennec.dcat.atlas.msg.body.writer:test`.

**The green build is not proof of correctness here** — see the test gap at the end.

## Scope of the split: what it does *not* fix

In RDF every entity may appear either as a described node or as a bare IRI reference. **The EMF
model cannot represent that duality, and the split does not change this** — one EMF feature maps
to one XML shape, and inline vs reference differ by a nesting level that neither subtyping nor
`xsi:type` can bridge (see [[dcat-atlas-rdfxml-body-mapping]]).

It was not representable before the split either. The old concrete `Resource` was `kind="empty"`:
it could carry `resource` or `about` but never content, so a link slot could never hold a
described node. And `about` in a link slot is not the "node alternative" — it parses to an
**empty object**, silently losing the link. So the old type offered *link*, plus one broken
spelling of link.

What the split buys is therefore narrow, and entirely **write-side**:

1. the silently-lossy shape is gone (`setAbout` is no longer offered on a link type, and the
   factory cannot build one);
2. entities no longer inherit a *mandatory* `resource`, which would make every conforming entity
   emit `rdf:resource` in node position → unparseable;
3. entities can no longer be dropped into a link slot (they currently can: `Distribution` and
   `AgentType` both extend `Resource`), which emits `xsi:type` content inside a property element
   and is rejected by Jena.

Caveat on (1): `lowerBound="1"` is enforced only by EMF's `Diagnostician`, not on load or save. A
`RefResource` with nothing set still serializes as `<dcat:catalog/>`. The guardrail is
compile-time and API-shape, not runtime.

Reading foreign documents in either form remains the job of the triples→EMF Jena reader (step 5
below). The split does not compete with that; it stops the *writer* emitting invalid RDF until it
exists.

---

## 1. Four classes lost their identity attribute — must fix

These still declare `eSuperTypes="rdf.ecore#//Resource"`. They used to inherit `resource` and
`about` from the old *concrete* `Resource`; now they inherit nothing. Confirmed: zero
`getAbout`/`getResource` in each generated interface.

| class | location | what it can no longer express | change to |
|---|---|---|---|
| `foaf.AgentType` (→ `Organization`, `Person`) | `foaf.ecore:37` | the publisher's IRI | `rdf.ecore#//NodeResource` |
| `terms.LicenseDocumentType` | `terms.ecore:510` | the license IRI | `rdf.ecore#//NodeResource` |
| `dcatde.ContributorID` | `dcatap.de.ecore:4` | **everything** — no features of its own, now an empty class | `rdf.ecore#//RefResource` |
| `dcat.Distribution` | `dcatap.ecore:198` | its own `rdf:about` | `rdf.ecore#//NodeResource` |

Priority order: `AgentType` first — `dct:publisher` is `lowerBound="1"` (mandatory per spec) on
Dataset and DataService, and `<foaf:Organization rdf:about="…">` is the documented working
request-body form, so this is a live regression. `ContributorID` next: `<dcatde:contributorID/>`
currently cannot name a contributor at all.

`NodeResource` for the first three because they are described nodes carrying element content;
`ContributorID` is a bare pointer, hence `RefResource`.

### `AgentType` checked against the spec (2026-08-12)

`NodeResource` confirmed correct, for two independent reasons. Structurally, `AgentType` is
reachable only through a containment wrapper (`foaf.ecore:7`, `:203`), so it always serializes as
a *node element* — and RDF/XML forbids `rdf:resource` there. Per spec §4.9 *Verantwortliche
Stelle*, `foaf:name` is **Pflicht [1..\*]**, so an agent always carries content and can never be a
content-free pointer.

The link form of a publisher is `<dct:publisher rdf:resource="…"/>` — an attribute on the
*property* element, governed by `publisher` being typed to the `foaf.Agent` containment wrapper,
not by `AgentType`'s supertype. Not worth changing: there is no Agent admin service (agents are
never standalone stored entities here), and §4.9's mandatory `foaf:name` means a bare publisher
IRI yields a document whose publisher has no name. Reading that form is the Jena reader's job.

Three unrelated deviations found while checking. §4.9 lists exactly two properties —
`foaf:name` (Pflicht [1..\*]) and `dcterms:type` (Optional [0..1]):

| model | spec | fix |
|---|---|---|
| `mbox` `lowerBound="1"` (`foaf.ecore:66`) | absent from §4.9 | drop the lower bound — a mandatory email on every agent, a GeoNetwork XSD leftover |
| `name` single-valued (`foaf.ecore:42`) | `[1..*]` | `upperBound="-1"`; language variants are inexpressible today |
| `phone` (`foaf.ecore:58`) | absent from §4.9 | none needed, already optional |

The other three rows of the table above have **not** been spec-checked yet.

## 2. Finish narrowing link features from abstract `Resource` to `RefResource`

10 of the 19 in `dcatap.ecore` were narrowed (the entity links: `catalog`, `dataset`, `service`,
`hasPart`, `homepage`, `distribution`, `inSeries`, `hasVersion`, `accessService`, `servesDataset`).
33 features workspace-wide still point at the abstract `Resource`.

An abstract eType makes these *harder* to use than before the split: the factory cannot create
one, so callers must reach for `createRefResource()` while the declared type says `Resource`.
It also leaves the loophole open — a `NodeResource`, or any entity, is still structurally
allowed in a link slot, which is the containment/duplication problem sneaking back in.

**Priority: low.** Nothing here is a regression — these slots behave exactly as they did before
the split. This is consistency work; it can wait behind section 1 and behind the Jena reader.

### Real features — 15, all pure IRI slots

| file | feature | line |
|---|---|---|
| `dcatap.ecore` | `Distribution.downloadURL` | 347 |
| | `Distribution.accessURL` | 355 |
| | `Distribution.applicableLegislation` | 393 |
| | `CatalogRecord.primaryTopic` (`lowerBound="1"`) | 494 |
| | `CatalogRecord.language` | 510 |
| | `DcatResource.applicableLegislation` | 651 |
| | `DcatResource.relation` | 734 |
| | `DcatResource.isReferencedBy` | 750 |
| | `DcatResource.language` | 758 |
| `foaf.ecore` | `AgentType.phone` | 58 |
| | `AgentType.mbox` | 66 |
| `vcard.ecore` | `OrganizationType.hasEmail` | 121 |
| | `OrganizationType.hasURL` | 129 |
| `skos.ecore` | `ConceptType.type` | 68 |
| `spdx.ecore` | `Checksum.algorithm` | 53 |

### Generated document-root holders — 18, low priority

All `volatile="true" transient="true" derived="true"` global-element artifacts of the XSD
import. Harmless; narrow them for consistency whenever the rest is regenerated.

- `terms.ecore` `DocumentRoot`: `extent` 189, `hasPart` 216, `hasVersion` 225, `isPartOf` 261,
  `isReferencedBy` 270, `isReplacedBy` 279, `isVersionOf` 306, `language` 315, `references` 379,
  `relation` 388, `replaces` 397, `source` 433
- `foaf.ecore` `DocumentRoot`: `homepage` 152, `mbox` 161, `page` 177, `phone` 185,
  `primaryTopic` 194
- `dcatap.de.ecore` `DCATDERoot`: `contributorID` 47

Line numbers are from 2026-08-12; re-grep before editing:

```
grep -n 'eType="ecore:EClass rdf.ecore#//Resource"' model/*.ecore
grep -n 'eSuperTypes="[^"]*rdf.ecore#//Resource"' model/*.ecore
```

## 3. Consistency, not broken

Eight classes still carry a local `about` attribute instead of extending `NodeResource` — the
same duplication that was removed from `Description` and `StatementType`. Fold each onto
`rdf.ecore#//NodeResource` and drop the local attribute.

| file | class | local `about` at |
|---|---|---|
| `dcatap.ecore` | `Relationship` | 438 |
| | `CatalogRecord` | 518 |
| `foaf.ecore` | `DocumentType` | 109 |
| `terms.ecore` | `LocationType` | 590 |
| | `PeriodOfTimeType` | 640 |
| | `ProvenanceStatementType` | 682 |
| | `RightsStatementType` | 732 |
| | `StandardType` | 782 |

The first pass listed only the first two. Verified 2026-08-12 that no class both extends
`NodeResource` *and* declares a local `about` — there is no duplicate-feature hazard today, which
is why this is consistency work rather than a defect.

**Priority: low**, same reasoning as section 2.

## 4. Test gap — this is what let the regression through

Nothing in `org.eclipse.fennec.dcat.atlas.msg.body.writer/test` sets `about` on a publisher, a
license, a contributorID or a distribution. Every `setAbout` call in that suite is on a Catalog,
Dataset or DataService, so all four broken classes are uncovered.

Add to `PointerModelRdfComplianceTest` (that bundle does not depend on `…impl`, so it stays
testable while the rest of the build is red):

- a Dataset with `dct:publisher` → `foaf:Organization` carrying `rdf:about`, round-tripped and
  asserted on the **parsed triple's object**, not merely "it parsed";
- the same for `dct:license`, `dcatde:contributorID` and a Distribution's own `rdf:about`;
- compare with `Model.isIsomorphicWith`, never text.

Recall the trap this guards against: an `about` in a link position parses cleanly and yields an
empty object — the link is lost silently, with no exception and no warning.

---

## Then, in order

1. ~~Apply section 1, regenerate.~~ Done. ~~Finish the pointer migration in the service/REST
   code.~~ Done. ~~Decide `Catalog.catalog`.~~ Done — it is `RefResource`.
2. ~~Add the section 4 coverage.~~ Done — publisher, contributorID and the distribution's own
   `rdf:about` are verified on the parsed triple's object; `dct:license` is not, because of the
   defect in section 0.
3. ~~Fix `Distribution.license` and `AgentType.mbox`.~~ Done, and `licenseKeepsItsIri` re-enabled.
4. ~~Section 2.~~ Done for every real feature. Section 3 and the 18 derived holders whenever the
   model is regenerated anyway — neither is a regression.
5. **Build the triples→EMF reader** (see below) — now the top item, and the only thing that makes
   the node/link duality readable at all.

## Background: why the model stays as it is

The decision was to keep the pointer model and add a **Jena-based exchange layer** rather than a
second EMF model:

- `EObjectRDFModelBuilder.toModel` (`msg.body.writer`, line ~122) already does EMF→triples, via an
  RDF/XML hop — "EMF renders RDF/XML, Jena parses". The missing half is triples→EMF.
- That accepts any legal RDF in any syntax; a second ecore would buy only one extra RDF/XML shape
  and still fail on Turtle, JSON-LD, blank nodes, `rdf:parseType`, property attributes.
- It also removes a current fragility: because `toModel` round-trips through EMF's RDF/XML, any
  resource the model cannot serialize validly drops out of the SPARQL graph
  (`DcatGraphServiceImpl.java:113` unprojectable list, warning at `:322`).
- A second EMF model would additionally collide on nsURI — both packages would want
  `http://www.w3.org/ns/dcat#`, and EMF's registry maps namespace → package.

The mapper should build its `predicate IRI → EStructuralFeature` index reflectively from the
existing `ExtendedMetaData` `name` + `namespace` annotations. Those annotations stop describing
XML shape and become the predicate table — another reason not to churn the model further than
sections 1–3.

Afterwards, replace the RDF/XML hop in `toModel` with direct EMF→triples so both directions go
through Jena.

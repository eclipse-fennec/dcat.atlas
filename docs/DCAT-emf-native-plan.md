# EMF-native model + Jena converter

Decided 2026-08-12 with the product owner. This supersedes the approach in
`DCAT-model-split-followups.md`, which is kept as the record of how we got here — read it for
context, not for instructions.

## The decision

**The EMF model stops trying to serialize to valid RDF/XML.** Its job is to carry the DCAT-AP.de
attributes and to model relationships the way EMF models relationships. A **converter** turns EMF
into a Jena `Model` at read time — for `text/turtle`, `application/rdf+xml`, `text/n3`,
JSON-LD, and for building the in-memory SPARQL graph.

Why this is better than what we were doing: every painful thing in the previous approach —
`RefResource`/`NodeResource`, wrapper classes like `terms.LicenseDocument`, `rdf:about` being
legal in one position and silently lossy in another — was an artifact of using EMF's XML writer
as an RDF/XML writer. None of it is DCAT. Removing that constraint lets the model say what the
spec says, and moves RDF correctness into one component that can assert and throw. Jena writes
from triples, so it *structurally cannot* emit the malformed shapes that kept biting us.

One component to get right, one place to look when a graph is wrong.

## 1. The model as it now stands

`rdf.ecore` is down to RDF **data model** concepts only — no syntax: `PlainLiteral`,
`TypedLiteral`, `DateOrDateTimeLiteral`, the `Datatype` enum, the `DateOrDateTime` datatype, and
abstract `IdentifiedResource` carrying `about`.

Deleted across the workspace, all of it RDF/XML scaffolding:

- `RefResource`, `NodeResource`, `RDFRoot`, `Description`, and the reification family
  (`Statement`, `StatementType`, `SubjectType`, `PredicateType`, `ObjectType`);
- **13 wrapper classes** that existed only to supply an RDF/XML nesting level — `foaf.Agent`,
  `foaf.Document`, `terms.LicenseDocument`/`Location`/`PeriodOfTime`/`ProvenanceStatement`/
  `RightsStatement`/`Standard`, `vcard.Address`/`Organization`, `locn.Address`,
  `adms.Identifier`, `skos.ConceptScheme`. Their 16 consumer features were retyped at the
  `…Type` class directly, and the 14 freed names were then taken by those classes
  (`AgentType`→`Agent`);
- **13 `*Root` classes** (`DCATAPRoot`, `DCATDERoot`, eleven `DocumentRoot`s);
- `skos.Concept`/`ConceptType`/`InScheme` — controlled-vocabulary references became `AnyURI`
  attributes (`theme`, `type`, `format`, `accessRights`, `availability`, `status`,
  `accrualPeriodicity`, `themeTaxonomy`);
- `skos.ecore` and `dcatap.de.ecore` as whole files. The `dcatde:` predicates survived losing
  their package, because the namespace lives on the *feature* annotation — which is the clearest
  demonstration that the annotations, not the package layout, are the predicate table.

`about` was also relaxed to `[0..1]` (keeping `iD="true"`) and the 11 classes that carried a local
copy now inherit it, so identity lives in exactly one place. See §5 for why the relaxation needs
the OCL check.

### Three rules the model now follows

**A link to something we model → non-containment `EReference`.**
**A link to an external IRI → `EAttribute` of `AnyURI`.**
Under RDF/XML these had to look identical, because EMF needed an element to hang `rdf:resource`
on. They are different things and are now modelled differently.

**Contained iff owned by exactly one parent.**

| contained | non-containment |
|---|---|
| `Catalog.record → CatalogRecord` | `Catalog.catalog`, `Catalog.dataset`, `Catalog.service` |
| `Dataset.distribution → Distribution` | `Dataset.inSeries → DatasetSeries` |
| `DcatResource.qualifiedRelation → Relationship` | `Distribution.accessService → DataService` |
| | `DataService.servesDataset → Dataset` |
| | `CatalogRecord.primaryTopic → DcatResource` |

`primaryTopic` is typed `DcatResource` because spec §4.13.2 gives its range as exactly
*dcat:Dataset, dcat:DataService, dcat:DatasetSeries oder dcat:Catalog* — the four classes that
extend it. Being abstract it correctly excludes `Distribution`.

## 2. The converter

### EMF → Jena is the product; Jena → EMF is for testing

Decided 2026-08-12: **the admin endpoints accept XMI only.** `application/rdf+xml` is no longer
a request format, so `RdfXmlMessageBodyReader` goes away. RDF is an *output* format.

That makes the reverse direction optional, and the requirements confirm it — harvesting is
explicitly out of scope (`opendata-portal-anforderungen.en.md:97`: *"No harvesting … no automatic
ingestion of external catalogs"*). We build it anyway, for two reasons that are worth the cost:

- it is the only honest way to test the forward direction (round-trip + `isIsomorphicWith`);
- it unlocks the five reference documents in `…dcatap.de.model/test/de/jena/mdo/…` as a real
  conformance corpus.

Still open at `anforderungen.en.md:105` is whether the portal must be harvestable *by* third
parties (GovData.de, EDP). That is the outbound direction, so it is covered either way.

### XMI on the wire: the model becomes the API

With RDF, moving or renaming a property was invisible to clients. With XMI, clients serialize
against the ecore itself — class names, `xsi:type`, feature names, containment shape — so
**structural model changes become breaking API changes**. Additive changes stay safe; moves and
renames do not.

The whole 2026-08-12 reshape was breaking on this axis — 14 class renames, the wrapper collapse,
the `originator`/`custodian` move — and it was free only because nothing had integrated yet. That
window is now nearly closed: **the distribution fold is the last breaking change still pending**,
so it should land with the storage rework rather than after it.

Clients also have to express non-containment references as XMI `href`s
(`<dataset href="…/datasets/d1#/"/>`). They must send the **public** URL and the server rebases
inward — see §3; otherwise the internal logical base leaks into the write API, which is the same
mistake as leaking file paths.

### The predicate table

`ExtendedMetaData` `name` + `namespace` annotations stop describing XML shape and become purely
the predicate table: feature → predicate IRI. They are load-bearing in a new way, so set them
deliberately rather than inheriting whatever the XSD import produced.

### The one typing rule

> **`AnyURI`-typed attribute → emit as an IRI node. Every other datatype → emit as a literal.**

The EMF datatype is the marker; nothing extra needs annotating. This is the new silent-failure
spot — get it wrong and you emit `foaf:mbox "mailto:x@y.de"` as a string literal instead of
`<mailto:x@y.de>`. Valid RDF, wrong graph, no error. Same class of bug as the `terms:license ""`
defect. Assert `object.isURIResource()`, never just "it parsed".

## 3. Identity and public URLs

Today every admin resource does `setAbout(readUri(uriInfo, id).toString())`
(`CatalogAdminResource:99`/`:124`, `DatasetAdminResource:87`/`:112`, and the same in the
DataService, Distribution and DatasetSeries resources), so the **absolute request URI is frozen
into the stored file**. Consequences: data is not portable between environments, Phase 3 read
replicas would serve the writer's hostname, and behind a reverse proxy without `X-Forwarded-*`
handling `UriInfo` is already the internal address.

The public IRI must be computed **at render time**, not at write time.

### Design

- Resources get a **logical** URI (e.g. `http://dcat.atlas/datasets/d1`), not
  `createFileURI` as `DcatHelper:98`/`:146` does now.
- A `URIConverter.getURIMap()` entry maps the logical base → `file:/<store>/`. EMF resolves
  cross-resource references against the *logical* URI and normalizes to the file only at I/O.
  Non-containment references then serialize as logical IRIs automatically — no file paths leak,
  with no per-reference work.
- The converter rebases logical → public using a configured `PUBLIC_BASE_URL`.

This also lets `DcatIds.idOf` go away: the id stops being something we parse out of a URL and
becomes the resource's actual identity.

### Which IRIs get rebased

**Structural rule, no configuration:** rebase iff the IRI is under the internal logical base.
Foreign IRIs — harvested datasets, publisher IRIs, licences from controlled vocabularies — are
by construction not under it and pass through **verbatim**.

`ADDITIONAL_OWNED_BASES` (config, empty by default) covers the two cases the structural rule
can't: absorbing legacy data that has `http://localhost:8085/dcat/rest/…` baked in, and hostname
changes. It is a migration aid, not the main path — if a fresh deployment works with it empty,
the design is right. A *required* list would be a silent-failure generator.

**Rebasing runs in both directions, from one owned-bases set.** A client POSTing a Catalog whose
`dataset` href is `https://public-host/dcat/rest/datasets/d1#/` is referring to something of
ours; if we only rebase outbound we store the public host verbatim and we are back to today's
problem, now arriving from outside. So: `normalizeInbound` (public/owned → logical) and
`renderOutbound` (logical → public). Inbound also gives a natural validation point — a reference
under an owned base pointing at a resource that does not exist is a `400`, not a dangling
reference discovered later.

Note inbound now operates on **XMI hrefs**, not RDF objects, since writes are XMI-only. Same
rule, different carrier — and it is what keeps the internal logical base invisible to clients.

**Match on path-segment boundaries**, not raw string prefixes: `http://example.org/dcat`
prefix-matches `http://example.org/dcatalog/…`. Require owned bases to end in `/`.

## 4. Storage rework

Deleting `RDFRoot` removes `DcatHelper`'s `<rdf:RDF>` + `AnyType` wrapper and
`EObjectRDFModelBuilder`'s assembly, so the storage format becomes an open decision. Whatever it
is, `DcatHelper` needs three changes independent of format:

- a **shared/managed `ResourceSet`** instead of `createResourceSet()` per operation
  (`:73`, `:87`, `:97`, `:146`) — cross-resource references cannot resolve otherwise;
- **logical resource URIs + the URIMap** instead of `createFileURI`;
- the `EcoreUtil.copy` on both paths (`:104` write, `:160` read) revisited — `EcoreUtil.Copier`
  resolves proxies by default, so copying a Catalog would dereference every dataset link.

**Distributions fold into their Dataset.** `Dataset.distribution` is containment and
`DistributionAdminServiceImpl:95` writes each Distribution to its own file — model and store
currently disagree. The store follows the model: drop the standalone distribution store. ETags
get simpler too, since a distribution change becomes a dataset change.

`RdfPointers` is deleted with `RefResource`; its call sites become plain `EReference`
add/remove.

`DcatGraphServiceImpl` builds the graph through the converter instead of the RDF/XML hop. Its
`skipped` counter changes meaning: "unprojectable" used to mean the model could not express
something in RDF/XML, which is now impossible — **any skip is a converter defect**, so treat it
as an error signal rather than an expected condition.

**Done 2026-08-13**, ahead of the storage rework, because `EObjectRDFModelBuilder` was deleted and
both `DcatGraphServiceImpl` and `DcatValidationServiceImpl` still called it. Both now call
`EObjectToJena.toModel(object)` and lost their `ResourceSetFactory` reference, as the writers did.
The `skipped`-as-error-signal half is **still open**, and the IRI guard below gives that counter a
second meaning to disentangle: bad data that got past validation, not only a converter defect.

`UnprojectableResourceTest` was deleted with the repoint. It built its unprojectable resource by
giving a Catalog an `rdf:about` containing a space, which failed when the projection parsed the
emitted RDF/XML — a hop that no longer exists. The remaining skip path (a resource with no
`about`) is covered by `resourceWithoutAboutIsSkippedRatherThanFailing`.

## 5. Validation at the write boundary (done 2026-08-18)

Constraints the ecore cannot express are declared in **OCL** and evaluated by
[`emf.m2x`](/opt/git/emf.m2x) — a spec-compliant OCL 2.5 engine, decoupled from the Eclipse
platform, standalone Java 21 with optional OSGi DS. It reaches the workspace through the
`fennecM2X` library, which `cnf/ext/fennec.bnd` already declared; adding it a second time in
`libraries.bnd` fails the build with a cyclic-include error.

**44 constraints across six ecores**, one per feature so a violation names the property to fix:
`rdf` 2, `dcat` 34 (DcatResource 12, Distribution 11, Catalog 3, Dataset 4, DataService 3,
CatalogRecord 1), `foaf` 3, `vcard` 3, `spdx` 1, `terms` 1.

### Deviation: annotations and the Diagnostician, not a direct `OclEngine` call

This plan said to **call `OclEngine` directly from the admin services, not through EMF's
`Diagnostician`**, on the grounds that a validation delegate is diagnostic only and would let a
bad object be written. That reasoning was half right and the conclusion did not follow. What is
true is that nothing calls the Diagnostician on its own — `Resource.save()` never does. What does
not follow is that the delegate is therefore unusable: calling `Diagnostician.INSTANCE.validate`
*explicitly at the write boundary and throwing* enforces exactly as a direct engine call would,
and buys three things a direct call does not:

- the constraints live **in the model**, as delegate annotations under
  `http://www.eclipse.org/fennec/m2x/ocl/1.0`, rather than in a document beside it;
- the generator emits `validate<Class>_<Name>` methods and chains inherited constraints into
  subclasses, across packages, for free;
- the same call also enforces the ecore's **declared multiplicities**, via
  `validate_EveryMultiplicityConforms` — which nothing had ever enforced, since lower bounds are
  inert without the Diagnostician.

That last point is why the cardinality corrections had to land first, and why
`DcatResource.description` had to be relaxed to `[0..*]`: DCAT-AP.de makes description Pflicht
for Catalog/Dataset/DatasetSeries but not for DataService, and an ecore cannot relax a lower
bound in a subclass. It is stated as `Dataset::HasDescription` instead, and declaring it on
`Dataset` is what excludes DataService.

It also **fails closed**, which a direct call would have had to implement by hand: with the
engine absent, `EObjectValidator` reports every annotated constraint as *constraint delegate not
found* at `ERROR`, so a deployment missing the bundle refuses writes rather than accepting
unvalidated ones.

### Where it is hooked

At the **persistence boundary** as planned — `DcatHelper.Store.put`, the single write choke
point, rather than the REST layer, because the admin services are OSGi services with other
possible callers. Same rule as the SPARQL graph hook. Ordered after the `about` is stamped
(`HasIdentity` needs it) and before `References.requireResolvable` and the file write.
`ModelValidation.check` throws `api.ModelConstraintException`, which
`rest.filter.ModelConstraintExceptionMapper` renders as **422** — the status on-write SHACL
already uses, since the failure is the same kind.

Gated by `StoreConfig.validateOnWrite`, default `false`, `true` in the shipped local and
container configurations — the `ShapesConfig.enforceOnWrite` arrangement. Off by default only
because the existing test fixtures build deliberately minimal entities; making them conformant
would let the default flip.

### What it covers

- **`about` present on entities.** `IdentifiedResource.about` is `[0..1]` because value nodes
  (`PeriodOfTime`, `Checksum`, a vcard contact point) are legitimately blank. Ecore cannot
  strengthen a lower bound in a subclass, so "every `DcatResource` has an IRI" has to live here.
  Without it an unset `about` degrades the XMI href to a positional path (`#//@dataset.0`) — no
  error, and every reference silently retargets when a collection is reordered.
- **`AnyURI` scheme and shape.** `AnyURI` accepts any string, so `mbox` will happily hold
  `ilenia@example.com` with no scheme, or plain garbage. `mbox` and `vcard:hasEmail` must be
  `mailto:` IRIs, `phone` and `vcard:hasTelephone` `tel:` IRIs, and the other ~35 `AnyURI`
  attributes absolute IRIs — matched against `[A-Za-z][A-Za-z0-9+.\-]*:\S*`. Note m2x's
  `matches` is `String.matches`, i.e. a **full** match, so anchors are redundant.
- **Spec obligations the ecore lost** — `Dataset::HasDescription`, above.
- Controlled-vocabulary membership was **deliberately left to SHACL** rather than unified here:
  the F-22 check traverses `skos:inScheme` against operator-supplied vocabulary data that OCL has
  no access to, so restating it would mean hand-mirroring the authority tables.

### SHACL joined it here on 2026-08-18

On-write SHACL enforcement used to live in the admin REST resources, so it covered HTTP
callers and nobody else. It now runs in the same place, immediately after the model
constraints — see the development guide's entry for that date. This section's instruction to
hook at the persistence boundary "not the REST layer — the admin services are OSGi services
with other possible callers" turned out to apply to both layers, not only to OCL.

### Why this layer exists at all, given SHACL

**No shapes ship with the repository** — `config.local` defaults `shapesDirectory` to
`/tmp/dcat-shapes-unset` — and an empty shapes set conforms to everything. So a deployment that
has not been given shapes validates nothing at all. These constraints travel in the model and
need no operator setup, which makes them the floor; SHACL is the profile check above it.

### The converter also guards its own IRIs (done 2026-08-13)

OCL is the primary defence, but it is not the only caller's problem: the admin services are OSGi
services that other code can reach. So `EObjectToJena.iri` validates every IRI it builds from
model data — the subject from `about`, `AnyURI` attributes, and non-containment link targets —
and throws `IllegalArgumentException` naming the feature (`Catalog.about`, `Dataset.theme`).

This closed a real hole. Jena's `createResource` validates *nothing*, so before the guard an
`about` of `http://x/catalogs/bro ken` produced a resource that projected into the SPARQL graph,
answered queries, and served through three of the four writers:

| output | behaviour with a space in the IRI |
|---|---|
| Turtle | writes it, escaped as `<http://x/catalogs/bro ken>` — and **re-reads it fine** |
| N-Triples | writes it, same escape |
| JSON-LD | writes it **raw and unescaped**: `"@id": "http://x/catalogs/bro ken"` |
| RDF/XML | throws `IRIException` — at *response* time, i.e. a 500 on a `GET` |

Note the check is `IRIx.create` for syntax plus `!isRelative()` for the scheme, **not**
`isAbsolute()`: RFC 3986's `absolute-URI` production excludes a fragment, so `isAbsolute()` would
reject a legitimate identifier like `<https://govdata.de#catalog>`. `anIriWithAFragmentIsAccepted`
exists to stop someone "tightening" it back.

`typeOf`/`predicateOf` are deliberately *not* guarded: those IRIs come from `ExtendedMetaData`
annotations, so a bad one is a model defect belonging to the predicate-completeness test in §6,
not to a per-write runtime check.

**What neither can cover.** Input validation sees the EMF object, not the emitted graph, so it
cannot catch the converter emitting `foaf:mbox "mailto:x@y.de"` as a *literal* instead of
`<mailto:x@y.de>`. That is a converter defect, not bad data. The two email failures look alike
and have different homes:

| failure | caught by |
|---|---|
| `mbox = "ilenia@example.com"` — no scheme, invalid data | OCL at the write boundary |
| `foaf:mbox "mailto:…"` emitted as a literal, not an IRI | converter test (`assertObjectIri`), or SHACL `sh:nodeKind sh:IRI` on the output graph |

Keep both. Dropping the converter test because validation exists would leave the silent one
uncovered.

## 6. Tests

`PointerModelRdfComplianceTest` (23 tests) mostly retargets at the converter unchanged, because
it asserts **emitted triples**, not EMF shapes. `publisherKeepsItsIri`, `licenseKeepsItsIri`,
`contributorIdKeepsItsIri`, `distributionKeepsItsOwnRdfAbout` and
`identityBearingFeaturesSurviveTheRoundTrip` all still state exactly the right contract.

Obsolete: `aLinkIsAnEmptyElementCarryingRdfResource`, which asserts RDF/XML text.

New, and worth having:

- **Predicate completeness.** Walk every `EStructuralFeature` and assert it maps to a predicate.
  Without it, adding a feature silently produces no triples — the new silent-failure mode this
  design introduces, and cheap to close.
- **Reference-document round-trip.** Read the five committed MDO documents through Jena → EMF →
  Jena and compare with `isIsomorphicWith`. These were unreadable under the old model; they
  become the real conformance corpus.
- **Rebasing.** Owned IRIs rebased in both directions, foreign IRIs untouched, and the
  `example.org/dcat` vs `example.org/dcatalog` boundary case.

Discipline that carries over unchanged: compare graphs with `Model.isIsomorphicWith`, never text,
and assert the parsed triple's **object**, never just that it parsed.

## 7. Order

1. ~~Converter EMF → Jena.~~ **Done** — `EObjectToJena.toModel(Object)`, no `ResourceSet` needed.
2. ~~Converter Jena → EMF.~~ **Done** — `JenaToEObject.over(EPackage…).parse(Model)`, pulled forward
   from step 6 because it is what proves the forward direction. 19 tests green in `msg.body.writer`.
3. ~~Drop `RdfXmlMessageBodyReader`.~~ **Done**, along with `EObjectRDFModelBuilder`. Every writer
   including RDF/XML now goes through Jena and no longer needs a `ResourceSetFactory`.
4. ~~Spec completeness pass.~~ **Done** — see Closed below. The structural parts landed before any
   client integrated, which was the constraint.
5. ~~Storage rework.~~ **Done 2026-08-17.** XMI, shared `ResourceSet`, logical URIs + URIMap,
   distributions folded into datasets. `RdfPointers` and the `RDFRoot`/`DCATAP_ROOT__*` machinery
   are gone; `DcatHelper.Store` is the single write choke point, with `StoreLayout` and
   `StoreResourceSets` beside it. `impl` is green (98 tests).
6. ~~Identity/rebasing.~~ **Done 2026-08-17.** `PublicIrisConfig` carries `publicBaseUrl` and
   `additionalOwnedBases`; `PublicView.render`/`fold` are the outbound and inbound halves and
   `PublicIriFilter` runs both, so XMI `href`s a client was served fold back to logical on the
   way in and never write the public host into stored data.
7. ~~Repoint `DcatGraphServiceImpl` at `EObjectToJena`.~~ **Done 2026-08-13**, pulled forward
   because the deleted `EObjectRDFModelBuilder` left `sparql` and `validation` red.
   ~~**Still open:** making `skipped` an error signal.~~ **Done 2026-08-17** — the `sparql`
   health check reports WARN with the count of resources that could not be projected.
   Deliberately WARN and not CRITICAL: REST still serves them, so taking the instance out of
   rotation would be the wrong trade, but SPARQL under-reporting must be visible.
8. ~~**OCL validation at the write boundary via `emf.m2x` (§5).**~~ **Done 2026-08-18** — see
   §5, including the deviation from this plan's "not through the Diagnostician" instruction.
   44 constraints, enforced in `DcatHelper.Store.put`, `422` over REST, 12 unit + 4 OSGi tests.
   The neighbours that cover part of the same ground remain: `DcatIds.idForWrite` refuses an
   identity that is not ours *at the service*, and the DCAT-AP.de SHACL shapes run on write
   (FR-4) when an operator has configured them.
9. **Read external documents through `JenaToEObject` as a conformance corpus — reopened, needs a
   source.** The five committed MDO documents turned out to be **stale and have been deleted**:
   four carried `<!-- Version 1.1, 13.08.2020 -->`, i.e. DCAT-AP.de 1.1, and the fifth was a
   European Data Portal harvest using the `edp:trans*` vocabulary retired with the 2021
   data.europa.eu rebrand. Across all five, *zero* occurrences of eleven DCAT-3 / DCAT-AP-2
   discriminators (`DatasetSeries`, `inSeries`, `dcat:version`, `hasVersion`, `versionInfo`,
   `temporalResolution`, `spatialResolutionInMeters`, `availability`, `accessRights`,
   `applicableLegislation`, `previousVersion`). Passing them would have proved nothing about
   v3, and failing them would often have been correct — they carry vocabulary v3 deprecated.

   **What the corpus is actually for.** Not "is our output valid RDF" — Jena answers that, and
   it said nothing about `dcat:endpointURL` being emitted as a literal. Validating our own
   output is a closed loop: it cannot contain what the model cannot hold. The corpus answers
   *does the model match reality*, which is the one question nothing else asks. A predicate/class
   coverage audit against the ecore's `ExtendedMetaData` table is the concrete form.

   Already demonstrated on one real document (a `dcat:Catalog` from the Jena city piveau
   portal): it validates **clean** against the DCAT-AP.de 3.0 shapes while carrying
   `dct:type "ckan"` as a plain literal — legal, because the shapes constrain `dcterms:type` per
   class and leave it free on `dcat:Catalog`. Our `type` sits on the shared `DcatResource`
   supertype as `AnyURI`, so we were stricter than the profile; the serializer now degrades a
   non-IRI `AnyURI` value to a literal rather than throwing (which had made every RDF read of
   such a resource a 500 while XMI reads hid it).

   Blocked on a source: the portal's per-resource download emits `<rdf:RDF>` with **no namespace
   declarations** (unparseable as-is) and a catalog with no `dcat:dataset`/`dcat:service` links,
   so it cannot exercise references. Options are the DCAT-AP.de 3.0 reference implementation, or
   fetching complete documents from a live piveau instance.

## Closed (2026-08-12)

- **Storage format: XMI.** Request bodies and stored files become the same shape, so a stored file
  can be POSTed back, and non-containment `href`s are first-class. Turtle was rejected despite
  diffing better in a git working tree, because it would put the Jena→EMF converter on the
  critical path for *persistence* — inverting the dependency order for no proportionate gain.
- **Spec completeness — it was one property, not six.** The earlier list counted spec *mentions*
  without checking status. Actual state: `politicalGeocodingURI` (§4.3.6), `geocodingDescription`
  (§4.3.14), `legalBasis` (§4.3.23) and `qualityProcessURI` (§4.3.32) are all **DEPRECATED**;
  `plannedAvailability` was **removed** in 3.0 and replaced by `dcatap:availability`, which the
  model already has. Only **`politicalGeocodingLevelURI`** (§4.3.5, `rdfs:Resource`, Empfohlen,
  `[*]`) was a real gap — added as an `AnyURI` attribute on `DcatResource`. The four deprecated
  ones were deliberately skipped: they would only matter for reading older DCAT-AP.de documents,
  and with harvesting out of scope and writes XMI-only, nothing will hand us one.
  Two further genuine gaps were found the same way and added: **`adms:identifier`** (§4.3.16, as
  `DcatResource.admsIdentifier` to avoid colliding with the `dcterms:identifier` literal) and
  **`dcterms:provenance`** (§4.3.30).
- **`originator` / `custodian` placement.** Moved to `DcatResource` alongside `contributorID`, so
  `DataService` can carry them.

## Open

- **Six ecore files are empty or near-empty and still on disk.** `locn`, `odrl`, `owl`, `prov` and
  `schema` have **zero** classifiers; `rdfs` has only the now-orphaned `RDFClass`. Deleting the
  `DocumentRoot`s emptied them. Each still costs a genmodel entry, a generated package, and
  `ResourceFactory`/`EPackageConfigurator` DS components for nothing.
- **…but check before deleting.** `odrl`, `owl`, `prov` and `schema` being empty means those
  vocabularies are not modelled *at all*. `odrl:hasPolicy` is a live spec property and may be a
  genuine gap of the same kind `adms:identifier` turned out to be; `owl:versionInfo` is
  deprecated. Not yet checked against the spec.
- **The XMI `href` contract.** Do clients send **public** URLs and the server rebases inward (what
  §3 assumes, and what keeps the internal logical base invisible), or are they expected to know
  the logical base? A client-contract decision.
- **The converter's literal carriers are hardcoded.** `EObjectToJena`/`JenaToEObject` recognise
  `PlainLiteral`, `TypedLiteral` and `DateOrDateTimeLiteral` by type — the only place either
  converter carries model knowledge. An ecore annotation marking "this class is a literal, not a
  node" would make both fully model-driven. Not worth reopening the model for on its own.

## Resolved: it was the content type, not the dialect (2026-08-13)

The store writes through `XMIResourceImpl` — `xmi:version="2.0"`, and `PlainLiteral` as
`<title value="…" lang="en"/>`. The fennec codec's message-body reader
(`org.eclipse.fennec.codec.rest`, `EObjectMessageBodyHandler`) rejects both:

- `Feature 'version' not found` for `xmi:version`
- `Feature 'value' not found` for the attribute form of a literal

so it is reading as plain XML with `ExtendedMetaData`, not as XMI. **This breaks the round-trip
property that XMI was chosen for**: a stored file cannot currently be POSTed back.

**The cause was the media type, not a dialect mismatch.** `EObjectMessageBodyHandler` is
`@Consumes(WILDCARD)`/`@Produces(WILDCARD)` and selects its *codec* by media type, so
`application/xml` was picking a plain-XML codec that understands neither `xmi:version` nor a
literal in attribute form. **`application/xmi` is the write and round-trip format**; it is now the
only thing the admin endpoints `@Consumes`, and is added to `@Produces` everywhere so a stored file
can be fetched and sent back unchanged. Store and codec agreed all along — no `ResourceSetProvider`
override is needed.

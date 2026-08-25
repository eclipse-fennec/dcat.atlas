# Publishing model.atlas EPackages as DCAT

**Status:** proposal, 2026-08-21. Nothing implemented.
**Belongs to:** [`client-library-implementation-plan.md`](client-library-implementation-plan.md)
(issue #27) — this is the worked example that plan's P1 should be built against.

The question this answers: *when model.atlas publishes its EPackages to a DCAT portal,
what DCAT entity is an EPackage?* MDO answered it one way, in code that runs. We propose a
different answer, and the rest of the mapping follows from it.

---

## 1. What MDO did

`de.jena.mdo.piveau/src/de/jena/mdo/piveau/MDOPiveauProvider.java` implements piveau's
`DatasetProvider` and `DistributionProvider`. Measured from that file:

- **The Dataset is configuration, not data.** `createDataset()` returns
  `RDFHelper.createDataset(datasetConfig)` — a `DatasetConfig` from ConfigAdmin, carrying
  `id()` and `catalogueId()`. One dataset per deployment, decided by an operator.
- **Distributions are derived, one per endpoint × media type.** For every registered
  JAX-RS resource or GraphQL service, `createDistributions` emits one Distribution per
  supported media type, with `accessURL = {endpoint}/rest/{modelName}`. A model with a root
  class gets a second set for its instances (`?limit=1000`), and GraphQL gets one at
  `application/json`.
- **The model identity lives in distribution metadata**: `distribution.model.name`,
  `distribution.model.ns`, and `distribution.model.description` from
  `EcoreUtil.getDocumentation(ePackage)`.
- **Annotation-driven metadata was started and abandoned.** A commented-out block reads
  `EcoreUtil.getAnnotation(ePackage, "Piveau", "keywords" | "root" | "theme")`. The
  intention was clearly to let a model carry its own DCAT metadata.

So in MDO, **an EPackage is a Distribution** of a fixed, configured Dataset.

### What that costs

- A harvester sees *one* dataset with many distributions, not N discoverable models. The
  models are not first-class in the catalogue.
- A model cannot carry its own publisher, licence, theme or keywords — those belong to a
  Dataset, and there is only one Dataset for all of them.
- There is nowhere to express that a model has **versions**: a Distribution is not
  versioned, and DCAT's mechanism for that lives at Dataset level.

None of this makes MDO wrong for MDO. It is the shortest path from "we have endpoints" to
"the portal lists us", and its Dataset is genuinely a deployment rather than a model.

---

## 2. What we propose instead

**An EPackage is a Dataset, and a serialisation of it is a Distribution.** This follows the
vocabulary's own definitions rather than convenience:

> **dcat:Dataset** — "A collection of data, published or curated by a single agent, and
> available for access or download in one or more representations."
>
> **dcat:Distribution** — "A specific representation of a dataset. A dataset might be
> available in multiple serializations."

An EPackage *is* a curated collection available in several representations; XMI and JSON of
the same package *are* two serialisations of one thing. Reading it the other way round —
the model as the representation — inverts the relationship the vocabulary describes.

---

## 3. The mapping

| model.atlas | DCAT | why |
|---|---|---|
| **Scope** | `dcat:Catalog` | A scope is a curated, governed collection of packages, which is what a catalogue is. One per scope, optionally as a sub-catalogue of one instance-level Catalog (`dcat:catalog`). |
| **EPackage** (by `nsUri`) | `dcat:DatasetSeries` | The model as a thing that persists across versions. The series is the stable identity a consumer links to. |
| **EPackage in a Stage** | `dcat:Dataset` | The concrete, addressable published thing: `/{scope}/schema/stages/{stage}` is a URL that exists, and a Dataset needs one. Joined to its series with `dcat:inSeries`. |
| **The model.atlas REST API** | `dcat:DataService` | "A collection of operations that provides access to one or more datasets." `dcat:endpointURL` is the API base; `dcat:servesDataset` points at the package datasets. |
| **Dataset × content type** | `dcat:Distribution` | One per media type the endpoint will return for that package, each with `dcat:mediaType` and `dcat:accessService` pointing at the DataService. |

### Where the metadata comes from

`PackageDescriptor(nsUri, scope, stage, version, fingerprint)` is what model.atlas already
hands out, and it covers more than MDO used:

| source | DCAT |
|---|---|
| `EPackage.getName()` | `dct:title` (fallback) |
| `EPackage.getNsURI()` | the series identity; `dct:identifier` |
| `EcoreUtil.getDocumentation(ePackage)` | `dct:description` (fallback) |
| `PackageDescriptor.version` | `owl:versionInfo` / `dct:hasVersion` on the Dataset |
| `PackageDescriptor.fingerprint` | a content checksum on the Distribution, and the natural thing to compare before re-registering |
| `PackageDescriptor.stage` | which Dataset of the series this is |

**The gap, and it is the interesting one.** DCAT-AP.de requires title, description,
publisher, licence and theme, and dcat.atlas enforces that with SHACL on write — so a
registration that carries only what an EPackage knows about itself will be refused. Name
and documentation cover two of the five; publisher, licence and theme have to come from
somewhere else. Two layers, and both are needed:

1. **Client configuration** supplies the defaults, so a deployment can publish at all.
2. **EAnnotations on the EPackage** override them per model — exactly the mechanism MDO
   started (`EcoreUtil.getAnnotation(ePackage, "Piveau", …)`) and left commented out.
   Without it every published model carries an identical publisher and licence, which is
   tolerable to start and wrong as soon as two teams publish into one portal.

> **Decided (Ilenia, 2026-08-24): layer 1 only, for now.** A single default publisher is
> acceptable to get publishing working; the per-model EAnnotation override comes later.
>
> **Sharper than this section assumed, measured in P3 of the client:** `publisher` is not
> merely a SHACL expectation. It is a **declared multiplicity `[1]` on `DcatResource`**, and
> `description` is the OCL invariant `HasDescription` — both enforced by
> `StoreConfig.validateOnWrite()`, which **defaults to on**, entirely independently of whether
> any SHACL shapes are mounted. So `title` + `description` + `publisher` is the hard floor for
> *every* write: without a publisher, not one registration succeeds even against a portal with
> no shapes at all. A `foaf:Agent` needs its own `name` too — an empty Agent does not satisfy
> the reference. Licence and theme, by contrast, really are SHACL-level and only bite where
> the shapes are mounted.

## 3a. Where this gets built

**Decided (Ilenia, 2026-08-24): on the model.atlas side.** The publisher component lives in
model.atlas and consumes the dcat client as jars, rather than dcat.atlas driving model.atlas
over HTTP. dcat.atlas publishes nothing to Maven today (`snapshot.yml` ships only the
container), so for now the four jars are hand-placed in model.atlas's `cnf/local` — the
`fennec.jgit` arrangement, accepted as non-reproducible until publishing is switched on.

The four:

| jar | why |
|---|---|
| `org.eclipse.fennec.dcat.atlas.client.api` | `DcatAtlasClient`, `Registration`, the exception hierarchy |
| `org.eclipse.fennec.dcat.atlas.client.impl` | the implementation + the XMI codec; also the `…impl.spi` seam |
| `org.eclipse.fennec.dcat.atlas.client.osgi` | the DS factory component and `AsyncDcatAtlasClient` — only needed for the OSGi front-end |
| `org.eclipse.fennec.dcat.atlas.dcatap.de.model` | the `dcat`, `rdf`, `foaf`, `terms`, `spdx`, `vcard`, `adms` EMF packages the client signatures are written in |

Runtime notes for that side: the OSGi front-end needs **SCR**, a **Jakarta RS whiteboard** to
source its `ClientBuilder`, and — since 2026-08-25 — **SPI-Fly**. `client.impl` declares its
factory with bnd's `@ServiceProvider`, so `DcatAtlasClientFactory` reaches the registry through
the SPI-Fly bridge rather than through SCR, and the requirement is `effective:=resolve` so the
resolver enforces it. model.atlas already has all three (`org.apache.aries.spifly.dynamic.bundle`
is in `modelatlas.runtime_base.bndrun` for Jena's sake). And do **not** set
`-resolve.effective: active` in a bndrun that includes `client.osgi`, for the reason recorded in
the client plan's §9.

Off a framework — a plain-Java publisher rather than a component — use
`DcatAtlasClient.builder()`, which finds the same factory through the `META-INF/services`
entry in the impl jar.

---

## 4. Two things to decide — both decided 2026-08-24

**Distributions that differ only by media type.** `SchemaPackagesResource` declares a bare
`@Produces` and negotiates on `Accept`; there is no `?format=` or `.json` variant. So every
Distribution for a package would share one `dcat:accessURL` and differ only in
`dcat:mediaType`. That is legal DCAT and weak in practice: a harvester cannot fetch a
specific representation without knowing to set a header, and `dcat:downloadURL` — the
property that means "this URL returns this representation" — has nothing to point at.
Either model.atlas grows a format selector, or the distributions stay negotiation-only and
we accept that a crawler gets whichever format the server prefers.

> **Decided (Ilenia, 2026-08-24): one Distribution per media type, negotiation-only.** The
> shared `accessURL` and the empty `dcat:downloadURL` are accepted for now. A format selector
> in model.atlas would turn this into a widening later — each Distribution gains its own
> `downloadURL` — and nothing has to be re-modelled to get there.

**Dataset per stage, or per version?** Both exist in `PackageDescriptor`, and only the
stage is addressable:

- *Per stage* (proposed): matches the URL space, so `accessURL` is real. The dataset's
  content changes when a new version reaches the stage — normal for DCAT, `dct:modified`
  moves, and `fingerprint` says when.
- *Per version*: immutable members, which is the purer reading of a DatasetSeries, but
  needs version-pinned URLs that do not exist today.

Per stage now; per version becomes available if model.atlas ever serves a version-pinned
URL, and the series makes that a widening rather than a migration.

> **Decided (Ilenia, 2026-08-24): a Dataset per stage**, as proposed.

---

## 5. Not every package gets published

Publication is **selective**: a model.atlas instance holds packages that have no business
in an open-data portal — internal, experimental, or simply not the kind of thing a
catalogue is for. So something has to say which packages are publishable, and the mechanism
is undecided: a configured list of namespace URIs, an EAnnotation on the package that opts
it in, or both with configuration winning.

Whichever it is, it is the same mechanism as the metadata in §3 — a package that opts itself
in is also the natural place to carry its own title, licence and theme — so the two should
be decided together rather than separately.

It changes nothing structural in the mapping, and one consequence is worth writing down
before it surprises somebody: **selective publication implies unpublication.** When a
package drops off the list, or is deprecated, its Dataset is already in the portal. Deleting
it means a cascade, because the Catalog references it and so may a DatasetSeries — which is
exactly what `DELETE …?cascade=true` and the unlinked-resource report exist for. Leaving it
means the portal advertises a model that model.atlas no longer serves. That is a policy
decision for whoever runs the portal, not something the client should choose, so the client
has to be able to do either.

## 6. Worth noting

This mapping is the first consumer of three things dcat.atlas has built and nothing uses:
`dcat:accessService` on a Distribution (FR-10), `dcat:inSeries` membership (FR-11), and
sub-catalogues. If the mapping is right, it exercises them; if any of them is awkward here,
that is worth knowing before a second consumer arrives.

One open point deliberately left open: model.atlas **scopes inherit** — an inherited
package reports the parent's scope and stage. DCAT sub-catalogues express containment, not
inheritance, so mapping scope hierarchy onto `dcat:catalog` is an approximation. Whether an
inherited package should appear in the child scope's catalogue at all is a question about
what the portal is for, not about the vocabulary.

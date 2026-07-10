# org.eclipse.fennec.shacl.model

An EMF model of the **SHACL validation-results vocabulary** (`http://www.w3.org/ns/shacl#`)
— `sh:ValidationReport`, `sh:ValidationResult` and the result properties. Generated from
`model/shacl.ecore` (+ `shacl.genmodel`) the same way as the DCAT-AP model bundle, reusing
`rdf:Resource` / `rdf:PlainLiteral` from `org.eclipse.fennec.dcat.atlas.dcatap.de.model`.

The source vocabulary `model/shacl.ttl` was taken from
<https://github.com/w3c/data-shapes/blob/gh-pages/shacl/shacl.ttl>.

## Why this bundle exists (and why it is currently unused)

The REST layer returns validation reports as the **native Jena `ValidationReport`**, which
serialises losslessly to every RDF syntax (Turtle, RDF/XML, N-Triples, N3, JSON-LD) — see
`org.eclipse.fennec.dcat.atlas.rest.ValidationReportMessageBodyWriter`. That path is
fully spec-compliant and needs no EMF model.

This EMF model is kept for the one thing Jena cannot emit: a **plain, non-RDF
`application/json`** projection of the report via the fennec codec (like the DCAT
entities). It is **not wired into the service or REST layer yet** — enable it only if/when
plain-JSON reports are actually required. Until then it is a standalone, generated model.

## Scope: results vocabulary only

Only the *results* half of SHACL is modelled. The *constraint* vocabulary (`sh:Shape`,
`sh:NodeShape`, `sh:PropertyShape`, the constraint components, `sh:path`, `sh:minCount`, …)
is deliberately **not** modelled — this bundle represents the report a validation *returns*,
not the shapes used to author it.

## Fidelity limitations (important)

A SHACL report can contain arbitrary RDF, so a *typed* EMF model is inherently a
**lossy projection** of it. Only Jena's native report (which the REST layer serves for RDF)
is byte-for-byte faithful. Specifically, in this model:

- **URI-only object fields.** `focusNode`, `resultPath`, `resultSeverity`, `value`,
  `sourceShape`, `sourceConstraintComponent`, `sourceConstraint` are typed as
  `rdf:Resource`, i.e. they hold an **IRI** (`rdf:resource="…"`). Consequences:
  - `sh:value` that is a **literal** (a bad literal value) is **not** representable and is
    dropped. (Could be enriched to `rdf:ObjectType`, which carries value+datatype, if needed.)
  - `sh:sourceShape` is frequently a **blank node** (the GovData property shapes are
    anonymous `[ sh:path … ; sh:hasValue … ]`); a blank node cannot be represented as an
    `rdf:resource` reference, and representing it faithfully would require modelling the
    SHACL *constraint* vocabulary — out of scope. Such sources are dropped.
  - `focusNode` is assumed to be an IRI (true for our entities); a blank-node focus is dropped.
- **`resultPath`** is a Jena `Path`. Only a **simple predicate path** (a single IRI) is
  captured; **complex** paths (sequence / inverse / alternative / zero-or-more, which are
  blank-node RDF structures) are not expanded.
- **`resultMessage`** → `rdf:PlainLiteral`, so string and language-tagged (`@de`) messages
  are preserved.
- **`resultSeverity`** holds the severity IRI (`sh:Violation` / `sh:Warning` / `sh:Info`).
  Enforcement (FR-4) blocks a write only on `sh:Violation` (DCAT-AP.de "MUSS").

When populating this model from a Jena report, fields whose node kind is not representable
(literal `value`, blank `sourceShape`, complex `resultPath`) are simply skipped — the
projection still carries the essentials (focus node, severity, message, simple path).

## Regenerating

Edit `model/shacl.ecore` / `model/shacl.genmodel` and run the bnd `fennecEMF` generator
(`-generate` in `bnd.bnd`); the cross-bundle `rdf` references resolve via the relative path
to `../../org.eclipse.fennec.dcat.atlas.dcatap.de.model/model/dcatap.genmodel#//rdf`.

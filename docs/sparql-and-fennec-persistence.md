# SPARQL and the Fennec persistence layer — feasibility findings

> **Status:** findings document, 2026-08-10. Investigates the proposal to persist DCAT
> EObjects through Eclipse Fennec Persistence and to serve SPARQL by translating it into
> that layer's backend-neutral query IR — "the way `emf.odata` translates OData".
> Internal document, not published to the docs site.
>
> Feeds decision **C1** (persistence: JPA/PostgreSQL vs Jena TDB2) and work packages
> **WP-DCAT-3** (persistence, issue #32) and **WP-DCAT-5** (SPARQL).
>
> **Outcome (2026-08-10):** §§1–5 stand as the analysis. **§6 and §7 are superseded** —
> Fennec Persistence was *not* adopted; the store of record stays file-based and later moves
> to git, with SPARQL served by the in-memory graph of §5.2. The agreed plan is
> [`persistence-and-sparql-implementation-plan.md`](plans/persistence-and-sparql-implementation-plan.md).

---

## 1. Summary

The proposal has three parts. Two are feasible and well supported; the third does not work
as described.

| # | Part | Verdict |
|---|------|---------|
| A | Persist DCAT EObjects via Fennec Persistence, backend configurable | **Feasible.** This is the framework's purpose. One model prerequisite (identity). |
| B | Persistence returns EObjects → existing writers emit Jena formats | **Already works.** No change required. |
| C | Translate SPARQL into the persistence query IR, as OData does | **Not feasible as a general translation.** Structural, not a matter of effort. Only a narrow subset maps. |

The reason C fails is not that the IR is immature — it is that SPARQL and the IR describe
different things. OData maps onto the IR because both are *rooted, entity-and-property*
languages. SPARQL is a graph-pattern language over triples with free variables in any
position and no root. §4 sets this out in detail.

C failing does **not** block SPARQL support. §5 gives four ways to serve SPARQL without any
SPARQL→IR translator, three of which need no second database. The recommendation is §6.

---

## 2. Evidence base

Examined on 2026-08-10:

| Source | What was read |
|---|---|
| `references/code/emf.persistence-jpa` | `README.md`, module table, `docs/unified-persistence/{concept,query-ir-redesign,query-processor-spi,search-access,composite-identity}.md`, `query.ecore`, `expression.ecore`, `command.ecore`, `QueryProcessor`, `QueryableResource`, module listing |
| `references/code/emf.odata` | bundle layout (`odata.persistence.api`, `odata.query`, `odata.ocl.evaluator`, …) |
| `references/specs/specification.pdf` | DCAT-AP.de Spezifikation 3.0, §4.6.24 and the `dcat:DataService` class section |
| This repository | `EObjectRDFModelBuilder`, `DcatHelper`, `CatalogAdminServiceImpl`, `DistributionAdminServiceImpl`, all `model/*.ecore` |

**Not examined**, and therefore not claimed on: the W3C DCAT and DCAT-AP specifications that
DCAT-AP.de cross-references; the exact Jena 6.1.0 class signatures named in §5.3; runtime
behaviour of either reference project (no build or execution was attempted).

---

## 3. What the intermediate layer actually is

Confirmed present and mature in `emf.persistence-jpa`:

- **`org.eclipse.fennec.expression.model`** — backend-neutral predicate IR:
  `And`/`Or`/`Not`, `Comparison`, `IsNull`, `Between`, `In`, `StringMatch`,
  `Quantifier` (`Exists`/`ForAll`), `PropertyPath`, `Variable`/`VariableRef`/`AliasRef`,
  `Arithmetic`, `Concat`, `StringFunction`, typed literals, `Score`, and geo predicates
  (`GeoWithin`, `GeoDistance`, `GeoBox`, `GeoPolygon`).
- **`org.eclipse.fennec.query.model`** — the `Query` envelope: `from`, `predicate`,
  `orderBy`, `select`, `apply` (a `Pipeline` of `FilterStage`/`GroupByStage` with
  `Aggregate`s), `expand`, `top`, `skip`, `distinct`, `countOnly`, `parameters`, `saveQuery`.
- **`org.eclipse.fennec.command.model`** — writes are deliberately *not* query vocabulary
  (decision **R8**): Insert = payload command, Delete = selector, Update = selector +
  ChangeSet template. The query language is read-only by design.
- **The SPI**:

  ```java
  public interface QueryProcessor {
      String            backend();
      QueryCapabilities capabilities();
      Diagnostic        validate(Query query, EClass rootEClass);
      QueryPlan         translate(Query query, QueryContext context) throws QueryException;
  }

  public interface QueryableResource {
      QueryResult query(Query query) throws IOException;
      QueryResult query(Query query, Map<String, Object> parameters, Map<?, ?> options);
      QueryResult query(String name, Map<String, Object> parameters, Map<?, ?> options);
  }
  ```

  Backends declare capabilities and **refuse with Diagnostics** rather than silently
  post-filtering in memory.

- **Backends available today:** EclipseLink/JPA (relational) and MongoDB (document).
  Plus `persistence.ecore` (reverse-engineers Ecore from a database schema).

Two design decisions from `query-ir-redesign.md` matter for everything below:

- **R3 — "Curated subset, not a copy."** The IR is ~15–20 classifiers, structurally informed
  by Essential OCL. `LetExp`, `IterateExp`, tuple literals and similar *deliberately do not
  exist*, so that "illegal queries become unbuildable instead of capability-refused
  everywhere".
- **R10 — the OData migration is staged and unfinished.** `emf.odata` still emits an OCL AST
  which an `OCL → Expr` bridge feeds into the IR; `ODataToExprBuilder` is phase 2.

---

## 4. Why OData maps and SPARQL does not

### 4.1 OData: shape isomorphism

OData is a **rooted** query language. A request selects one entity set and then constrains
and shapes it: `$filter`, `$select`, `$orderby`, `$expand`, `$top`, `$skip`, `$count`,
`$apply`. Navigation follows *named* properties from that root.

The IR has the same shape, to the point that `Query`'s features are the OData query options
one-for-one: `from`, `predicate`, `select`, `orderBy`, `expand`, `top`, `skip`, `countOnly`,
`apply`. The translation is therefore largely structural renaming plus expression-tree
rewriting. `QueryProcessor.validate(Query, EClass rootEClass)` makes the rooted assumption
explicit in the contract.

### 4.2 SPARQL: a different kind of language

SPARQL is a **graph-pattern** language. A query is a set of triple patterns
`(subject, predicate, object)` in which *any* position may be a variable, joined by shared
variables, and composed with relational operators. There is no root entity and no
distinguished navigation direction.

Concretely inexpressible in the IR:

| SPARQL construct | Why the IR cannot carry it |
|---|---|
| **Variable predicate** (`?s ?p ?o`) | `PropertyPath` navigates *named* `EStructuralFeature`s. A variable in the predicate position has no representation at all. |
| **`OPTIONAL`** (left join) | `Junction` is boolean logic over predicates, not relational algebra over pattern groups. There is no left-join construct. |
| **`UNION`** | Same reason. `Or` combines *predicates* on one root, not alternative result-producing patterns. |
| **Transitive property paths** (`elem*`, `elem+`) | `PropertyPath` is a fixed-length chain of named features. No closure operator. |
| **Multi-subject joins** | Every `Query` is anchored at a single `from`/`rootEClass`. Joining two unrelated subjects by a shared variable has no expression. |
| **`CONSTRUCT` / `DESCRIBE` / `ASK`** | These yield graphs or booleans. `QueryResult` yields rows/EObjects over a root type. |
| **Named graphs / quads** (`GRAPH ?g`) | The IR has no notion of a graph dimension. Relevant directly to **FR-14** (named-graph replace). |

### 4.3 Why this is not a gap to be filled

It is tempting to read the table as a to-do list. It is not, for two reasons.

**It contradicts the IR's stated design.** R3 curated the IR *down* precisely so that
unrepresentable queries cannot be built. Adding left joins, unions, closure and unrooted
multi-pattern joins means re-introducing general relational algebra — the thing R3 rejected,
and a far larger surface than "add a few classifiers".

**Every backend would have to serve it.** The SPI's contract is that a processor either
translates natively or refuses with Diagnostics. A SPARQL-complete IR would be refused by
both existing backends for most queries, so the capability negotiation would report "cannot"
in the common case. That is not a translation layer; it is a fiction with a fallback.

**Corollary — the analogy does not transfer.** "OData compiles into the IR, so SPARQL can
too" does not follow, because the premise that makes OData easy (it is already rooted and
property-oriented, like the IR) is exactly the premise SPARQL violates.

### 4.4 What *is* mappable

A useful subset survives, worth remembering for §5.4:

- a basic graph pattern anchored on one subject type (`?s a dcat:Dataset`),
- with fixed-length navigation along known predicates,
- `FILTER` over comparisons, `IN`, string matching, ranges, null checks,
- `ORDER BY`, `LIMIT`/`OFFSET`, `COUNT`,
- `EXISTS`/`NOT EXISTS` over a multi-valued reference, via `Quantifier`.

That is "find datasets whose publisher is X and whose distribution format is CSV" — the
faceted-search case. It is not general SPARQL.

---

## 5. Ways to serve SPARQL without translating it

The common insight: **let Jena's ARQ execute the SPARQL.** It already implements the entire
language. The only question is where it gets its triples from. Note that none of these
requires the persistence layer to understand SPARQL.

`EObjectRDFModelBuilder.toModel(Object entity, ResourceSet)` already returns a Jena `Model`,
and its package is exported. That is the bridge all four options build on.

### 5.1 Materialize per request

Load entities from persistence with an ordinary read (all, or all of a type — no query
analysis), build a `Model`, run ARQ, discard.

*Correct for any SPARQL. Cost:* loads more than the query needs, on every request.

### 5.2 Cached in-memory model — **recommended starting point**

Build the `Model` once and keep it. On each write, replace that one resource's statements in
it. Rebuild at startup by scanning persistence.

*Correct for any SPARQL. No second database — this is a cache, not a store.* Writes in a
data catalog are editorial and infrequent while reads are the hot path, so maintenance is
cheap and queries are fast. Bounded by catalog size in memory: a few thousand datasets at
~50 triples each is a few hundred thousand triples, which Jena holds comfortably (it scales
into the millions). *That is a rule of thumb, not a measurement.*

### 5.3 Custom Jena `Graph` over persistence

Implement Jena's graph SPI — subclass `GraphBase` and implement `graphBaseFind(Triple)`
(plus `DatasetGraph` for named graphs). ARQ then executes the query by calling
`find(s, p, o)` per triple pattern and performing all joins itself.

This is the standard adapter for SPARQL over a non-RDF store, and it is tractable precisely
because you never translate a *query* — only individual patterns:

| pattern | persistence lookup |
|---|---|
| bound subject | get by id |
| bound predicate + object | filter on that feature — within the IR |
| bound predicate, free object | project that feature |
| all free | scan |

ARQ retains ownership of everything in the §4.2 table. Half the required mapping already
exists: `EObjectRDFModelBuilder` derives predicate IRIs from `ExtendedMetaData`
namespace/name annotations in the write direction, so the inverse (predicate IRI →
`EStructuralFeature`) is derivable from the same annotations.

*Cost:* chattiness. A naive `find`-per-pattern against a remote Mongo is many small round
trips; batching and caching are needed before it beats §5.2.

*Caveat:* the Jena class names above should be verified against the bundled Jena 6.1.0
before being relied on.

### 5.4 Optional later: pushdown of the rooted subset

Compile only the §4.4 subset into the IR for pushdown, and refuse anything else with
Diagnostics — the capability discipline the framework already enforces. This is an
optimization on top of 5.2 or 5.3, never a way to serve SPARQL on its own.

### 5.5 For completeness: the two single-store alternatives

- **TDB2 as the source of truth.** SPARQL native, no duplication — but it abandons the
  configurable-backend goal and needs RDF→EObject on every read. Today only RDF/XML→EObject
  exists (`RdfXmlMessageBodyReader`, via the EMF XMI reader); there is no Turtle→EObject.
- **Defer SPARQL.** Serve faceted search through the IR, where §4.4 fits well. Legitimate:
  SPARQL is WP-DCAT-5, not current scope.

---

## 6. Recommendation

**Do the persistence swap now; do not introduce a second database.**

1. Persist EObjects through Fennec Persistence, **Mongo backend** (§7).
2. Writes through `command.model`; reads for REST/faceted search through the query IR.
3. When WP-DCAT-5 starts, serve SPARQL with the **cached in-memory model** (§5.2).
4. Escalate to a custom `Graph` (§5.3) or an on-disk TDB2 projection only if measurement
   demands it.

**Deferring the second store costs nothing**, which is what makes this safe: the write path
is *identical* in §5.1–§5.3 and in a TDB2 projection — EObjects into persistence via
`command.model`. A projection or a custom graph added later changes nothing about how data
is written; it only adds a consumer. So the decision can wait for real query patterns and a
real catalog size.

If a TDB2 projection is ever added, it must be a **projection** in the strict sense — a
derived, rebuildable copy with a one-way arrow from persistence, per the persistence
concept's principle P1 ("losing a projection means re-indexing, never data loss"). One named
graph per resource makes replace idempotent and therefore self-healing. Two *authoritative*
stores would be a genuine distributed-consistency problem and should not be built.

---

## 7. Prerequisites for the persistence swap (part A)

**Favourable finding — the storage boundary is already the right shape.** `DcatHelper`
works through `ResourceSet`/`Resource`, creating `URI.createFileURI(...)`. The persistence
layer exposes backends as EMF resources under `jpa://` and `mongodb://` schemes with
proxy-based lazy loading. Changing the URI scheme is the bulk of the work, not a rewrite.

**Mongo rather than JPA.** The model is XSD-derived and containment-heavy, with wrapper
classes (`DatasetContainer`, `rdf.ecore#//Resource`). Documents nest naturally; the
relational path would explode those wrappers into their own tables.

**Identity must be declared — open work item.** There is no `iD="true"` anywhere in any of
this repository's ecores (checked, all files). `about` is a plain `AnyURI` `EAttribute`,
redeclared on several classes, and `DcatHelper.idOf()` currently derives an id by taking the
last path segment of the `rdf:about` URI. Fennec Persistence requires identity per EClass:
either `iD="true"` on `about`, or the explicit `idFeatures` annotation (source
`http://eclipse.org/fennec/persistence/1.0`, per `composite-identity.md`). This is a model
decision and should be taken together with the `accessService` containment question (N7).

**Dependency weight.** This pulls EclipseLink or the Mongo driver plus the
expression/query/command/orm bundles into the runtime, on top of Jena. Worth sizing against
the currently lean `base.bndrun`.

---

## 8. Expectation corrections

**Lucene is not a selectable backend.** Only EclipseLink/JPA and Mongo exist.
`docs/unified-persistence/search-access.md` is a *blueprint for a future `emf.search`
repository* (status: working blueprint, 2026-08-05). The `Score` and geo vocabulary landed
in the IR as prerequisites for it (#99, #100), which is why those constructs are visible —
but "switch to Lucene by configuration" is not available today.

**The OData→IR path is itself unfinished.** Per R10, `emf.odata` still emits OCL fed through
an `OCL → Expr` bridge, with `ODataToExprBuilder` as phase 2; the OclEvaluator differential
tests are deactivated pending fennec-odata publishing its query bundle. So the reference
implementation of "language → IR" is a work in progress, not a finished pattern to copy.

**No scale requirement is documented.** Nothing in `opendata-portal-anforderungen.en.md` or
`opendata-portal-planung.md` states catalog volume, load or response-time targets. Several
choices above (§5.2 vs §5.3, whether a projection is ever needed) hinge on that number. It
is worth writing down.

---

## 9. Consequences for open decisions

- **C1 (JPA/PostgreSQL vs TDB2)** is not either/or once SPARQL is decoupled from storage.
  The honest resolution is "Fennec Persistence is the store of record; Jena serves SPARQL
  over a derived view", with the derived view starting as an in-memory cache.
- **WP-DCAT-3 / #32** can proceed on part A without resolving SPARQL at all.
- **FR-14 (named-graph replace)** has no representation in the query IR (§4.2) and must be
  served on the Jena side.
- **FR-6 (transactions)** is served by the persistence layer, not by the RDF view.
- **N7 (`accessService`)** interacts with §7: the containment-vs-pointer choice changes what
  is persisted as its own entity, so it should be settled alongside the identity decision.

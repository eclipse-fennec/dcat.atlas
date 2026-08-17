### Can a `Dataset` exist without a `Catalog`?

**Technically yes, but operationally no.**

1. **In RDF/Ontological Terms (Semantic Level):**
   - A `dcat:Dataset` is an independent entity in RDF. It does not require a `dcat:Catalog` to be syntactically valid on its own.
   - If you publish a standalone RDF node representing a `dcat:Dataset`, it is a valid RDF graph.
2. **In DCAT-AP.de / DCAT-AP Portal Terms (Exchange & Compliance Level):**
   - The purpose of **DCAT-AP.de** is harvesting and metadata exchange between data portals (e.g., GovData, regional open data portals).
   - In a portal metadata harvest or export, the **entry point is always a `dcat:Catalog`**.
   - A valid DCAT-AP.de feed or file must contain at least one `dcat:Catalog` that links to the datasets via `dcat:dataset` (or `dcat:record`).
   - Therefore, for a dataset to be compliant with harvesting workflows, **it must be contained in (or referenced by) a Catalog**.

### How can a Catalog link to a DatasetSeries?

**A `dcat:Catalog` can—and should—reference a `dcat:DatasetSeries` directly.**

Here is how the underlying ontology and DCAT-AP.de 3.0 specification actually handle this:

### 1. Object Hierarchy: `dcat:DatasetSeries` IS a `dcat:Dataset`

In the underlying W3C DCAT 3 vocabulary (which DCAT-AP.de 3.0 adopts), `dcat:DatasetSeries` is defined as a **subclass** of `dcat:Dataset`.

$$dcat:DatasetSeries \sqsubseteq dcat:Dataset$$

Because every `dcat:DatasetSeries` is semantically a special type of `dcat:Dataset`, any property expecting a `dcat:Dataset` automatically accepts a `dcat:DatasetSeries`.

### Can a DataService serve more than one Dataset?

**Yes, absolutely.** A `dcat:DataService` can serve **one, many, or even zero** datasets.  

In **DCAT-AP.de 3.0** (and underlying DCAT 3 / DCAT-AP specifications), the relationship between a `DataService` and a `Dataset` is a **many-to-many ($N:M$) relationship**.

### How the Link Works: `dcat:servesDataset`

The property **`dcat:servesDataset`** connects a `dcat:DataService` to the `dcat:Dataset` resources it delivers.  

- **Cardinality:** `0..*` (zero to many)  
- **Direction:** The predicate goes **from** the `DataService` **to** the `Dataset`.  

### Other Inconsistencies

+ endpointURL/endpointDescription typed as literal in our ecore, not rdfs:Resource
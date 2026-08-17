# Open-Data-Portal — Admin-Schnittstelle (DCAT-AP 3)

> Stand: 2026-06-27 · Status: Entwurf zum Review
> Gehört zu `docs/opendata-portal-planung.md` → **AP3**.
> Zweck: bau­fähige Spezifikation für **OSGi-Service-API** und **REST-Adapter** zum Pflegen des Katalogbestands.

## 1. Zweck & Geltungsbereich

Diese Schnittstelle pflegt (anlegen/ändern/löschen/lesen-zurück) den DCAT-AP-3-Katalogbestand des Portals. Sie ist die **Schreibseite**; die maschinen-/menschenlesbare **Leseseite** (Content-Negotiation, SPARQL, UI) ist AP4/AP9 und hier nur referenziert.

**Zwei austauschbare Fassaden über derselben Logik (§7 der Planung):**
- **OSGi-Service-API** — primärer Kontrakt; für eingebettete Aufrufer (Model-/Data-Atlas im selben Prozess), EMF-typisiert.
- **REST-Adapter** — dünner Aufsatz über der OSGi-API; nimmt/liefert RDF (DCAT-XML u. a.), für den verteilten Microservice-Betrieb.

Beide Fassaden bilden **dieselben Operationen** ab. Wer eine kann, kann beide.

## 2. Verwaltete Entitäten (DCAT-AP 3)

Abgeleitet aus `org.eclipse.fennec.data.atlas.dcat.model`. Gemeinsame Basis ist `DcatResource` (identifier, title, description, theme, keyword, creator, publisher, issued, modified, license, …).

| Entität | EClass | Wesentliche Beziehungen (Ecore) | REST-Collection |
|---|---|---|---|
| **Katalog** | `Catalog` | `dataset`, `service`, `catalog` (Sub-Kataloge), `record`, `themeTaxonomy`, `homepage` | `/catalogs` |
| **Dataset** | `Dataset` | `distribution`, `spatial`, `temporal`, `accrualPeriodicity`; **`inSeries` → DatasetSeries** (AP1) | `/datasets` |
| **DatasetSeries** | `DatasetSeries` *(neu in AP1)* | `seriesMember` → Dataset | `/dataset-series` |
| **DataService** | `DataService` | `servesDataset`, `endpointURL`, `endpointDescription` | `/data-services` |
| **Distribution** | `Distribution` | `accessURL`, `downloadURL`, `accessService` → DataService, `format`, `license` | `/distributions` |
| **CatalogRecord** | `CatalogRecord` | `primaryTopic`, `conformsTo` | `/catalog-records` *(meist abgeleitet)* |

> **Agent** (`foaf:Agent`: publisher/creator/contactPoint), `Concept`/Vokabulare (`skos`), `Standard` etc. werden **nicht** als eigene Top-Level-Ressourcen verwaltet, sondern als Bestandteil bzw. Referenz innerhalb der obigen Entitäten gepflegt (siehe FR-12).

> **DatasetSeries** hängt an **AP1** (DCAT-AP-3-Upgrade). Bis dahin sind die zugehörigen Endpunkte/Methoden Teil des Vertrags, aber noch nicht implementierbar.

## 3. Design-Grundsätze (Vorschlag, im Review bestätigen)

- **D1 — Linked-Data-konforme URIs:** Die REST-URL einer Ressource **ist** ihre dereferenzierbare DCAT-URI: `{publicBaseUrl}/{collection}/{id}`. Der `{id}` ist das letzte Pfadsegment. → Endpunkt-Awareness aus AP7 liefert `publicBaseUrl`.
- **D2 — ID-Strategie:** Client darf `id`/URI mitgeben (`PUT …/{id}` oder `dct:identifier`/`@id` im Payload). Fehlt sie, **mintet** der Server eine stabile ID unter `publicBaseUrl`.
- **D3 — Idempotenz/Upsert:** `PUT …/{id}` ist *create-or-replace* und wiederholbar (Kernanforderung: Data-Atlas „spielt aus", evtl. mehrfach).
- **D4 — RDF als Wire-Format:** Inhalt wird als RDF ausgetauscht; Content-Negotiation über `application/rdf+xml` (= „DCAT-XML", Default-Eingang), `text/turtle`, `application/ld+json`, `application/n-triples`.
- **D5 — Transaktional & validiert:** Jede schreibende Operation ist atomar (Jena-TDB2-Transaktion) und wird vor dem Commit gegen DCAT-AP-3-SHACL geprüft.
- **D6 — Mehrkatalog/Named Graphs:** Jeder `Catalog` = ein Named Graph; katalogweites Löschen/Ersetzen ist eine Graph-Operation.
- **D7 — Optimistische Nebenläufigkeit:** Jede Ressource trägt ein `ETag`; ändernde Operationen akzeptieren `If-Match` (verbindlich, siehe FR-7).
- **D8 — Replace-only (kein PATCH):** Aktualisiert wird ausschließlich durch **vollständiges Ersetzen** (`PUT`-Upsert). Es gibt **kein** partielles Ändern (PATCH). Wer ändert, sendet die komplette Ressource bzw. den kompletten (Sub-)Graphen.

## 4. Funktionale Requirements

Zerlegbar in Issues; je FR eindeutig referenzierbar.

### Allgemein
- **FR-1 (CRUD je Entität):** Anlegen, Lesen, Ersetzen (Upsert) und Löschen für Catalog, Dataset, DatasetSeries, DataService, Distribution.
- **FR-2 (Upsert/Idempotenz):** Wiederholtes Anlegen/Ersetzen mit gleicher ID erzeugt denselben Zustand, keine Duplikate (D3).
- **FR-2b (Replace-only, kein PATCH):** Aktualisierung erfolgt **ausschließlich** durch vollständiges Ersetzen der Ressource bzw. des (Sub-)Graphen (`PUT`-Upsert). Partielle Updates (PATCH) sind **nicht** Teil der Schnittstelle (D8). Vorteil: einfache, eindeutige Semantik und konfliktfreies Re-Ausspielen aus dem Data-Atlas.
- **FR-3 (URI-Vergabe):** Server mintet fehlende IDs unter `publicBaseUrl`; mitgelieferte IDs werden übernommen, sofern kollisionsfrei und im erlaubten Namensraum (D1/D2).
- **FR-4 (Validierung):** Schreibende Operationen validieren gegen DCAT-AP-3-Constraints (SHACL); bei Verstoß **kein** Commit, strukturierter Validierungsbericht zurück.
- **FR-5 (Reiner Validierungslauf):** Eingang lässt sich validieren **ohne** zu schreiben (Dry-Run).
- **FR-6 (Transaktionalität):** Jede Operation ist atomar; Teilzustände sind nie sichtbar.
- **FR-7 (ETag-Nebenläufigkeit, verbindlich):** Jede Ressource liefert bei `GET` und nach jedem Schreiben ein `ETag`. Ändernde Operationen (`PUT`/`DELETE`) **müssen** `If-Match` auswerten und bei Nichtübereinstimmung mit `412 Precondition Failed` abweisen; so werden verlorene Updates verhindert (D7).
- **FR-8 (Format-Agnostik):** Eingang/Ausgang in RDF/XML, Turtle, JSON-LD, N-Triples (D4).

### Beziehungen & Komposition
- **FR-9 (Katalog-Mitgliedschaft):** Dataset, DataService und Sub-Katalog können einem Katalog zugeordnet/entzogen werden, **ohne** die Zielressource erneut vollständig zu senden.
- **FR-10 (Distribution-Komposition):** Distributions werden im Kontext ihres Datasets angelegt/gelöscht; eine Distribution ohne Dataset ist unzulässig. `accessService` verweist optional auf einen DataService.
- **FR-11 (Serien-Mitgliedschaft):** Datasets können einer DatasetSeries zugeordnet/entzogen werden (`inSeries`/`seriesMember`) — *abhängig von AP1*.
- **FR-12 (Eingebettete Referenzen):** Agents (publisher/creator/contactPoint), Concepts/Themes, Lizenzen, Standards werden als Teil der Entität mitgepflegt; gleiche Agents/Concepts werden dedupliziert (by-URI).

### Massenpflege (Data-/Model-Atlas-Anbindung)
- **FR-13 (Graph-Ingest):** Ein komplettes DCAT-Dokument (ganzer/teilweiser Katalog) lässt sich in **einer** transaktionalen Operation upserten (Kern für „Ausspielen" aus dem Data-Atlas, AP5).
- **FR-14 (Graph-Ersetzen/Löschen):** Ein Katalog-Graph lässt sich vollständig ersetzen oder löschen (Re-Sync eines Mandanten/Katalogs, D6).
- **FR-15 (Differenz-Upsert):** Wiederholter Ingest aktualisiert nur Geändertes; entfernte Ressourcen sind optional per „replace"-Semantik tilgbar.

### Endpunkt-/Microservice-Bezug
- **FR-16 (Öffentliche URLs):** `accessURL`/`downloadURL` von Distributions werden als **öffentliche** URLs übernommen und nicht verfälscht (AP7).
- **FR-17 (Erreichbarkeitsprüfung, optional):** Auf Wunsch prüft die Operation die Erreichbarkeit der Distribution-URLs und meldet das Ergebnis (nicht-blockierend konfigurierbar).
- **FR-18 (Self-Awareness):** Geminteter URI-Namensraum folgt der konfigurierten öffentlichen Basis-URL, auch eingebettet betrieben.

### Querschnitt
- **FR-19 (Fehlertaxonomie):** Einheitliches Fehlermodell (Validierung 422, Konflikt 409, nicht gefunden 404, Vorbedingung 412 …) in beiden Fassaden.
- **FR-20 (Audit/Provenienz, optional):** Änderungen werden mit Zeitstempel/Urheber protokolliert (`CatalogRecord.modified`/Audit-Log).
- **FR-21 (Sicherheit, §11.7 Planung):** Schreibzugriff ist absicherbar (z. B. Keycloak-Token wie MDO); Hook vorgesehen, Ausgestaltung offene Entscheidung.

## 5. REST-Schnittstelle

Basis: `{publicBaseUrl}/admin/api/v1`. Alle Bodies sind RDF (D4); `Accept`/`Content-Type` steuern die Serialisierung.

### 5.1 Standard-Operationen je Collection

Gilt einheitlich für `catalogs`, `datasets`, `dataset-series`, `data-services`, `distributions`:

| Methode & Pfad | Zweck | Erfolg | Wichtige Fehler |
|---|---|---|---|
| `GET /{coll}` | Liste (paginiert: `?page`, `?size`, Filter `?q`, `?theme`, `?publisher`) | 200 | — |
| `POST /{coll}` | Anlegen; ID aus dem `about` des Payloads, falls dieses eine unserer Ressourcen benennt, gemintet nur bei fehlendem `about` | 201 + `Location` | 400 (fremdes/unbrauchbares `about`), 409 (ID bereits vergeben), 422 Validierung |
| `GET /{coll}/{id}` | Einzelne Ressource (Content-Negotiation) | 200 + `ETag` | 404 |
| `PUT /{coll}/{id}` | **Upsert** (create-or-replace, idempotent; einziger Änderungsweg, FR-2b). ID kommt aus dem Pfad; `about` im Body muss fehlen oder genau diese Ressource benennen | 200/201 + `ETag` | 400 (`about` ≠ Pfad-Ressource), 409, 412, 422 |
| `DELETE /{coll}/{id}` | Löschen (`?cascade=true` löscht Komposita) | 204 | 404, 409 (referenziert) |

Es gibt **kein** `PATCH` — Änderungen erfolgen ausschließlich per vollständigem `PUT`-Replace (D8/FR-2b). `PUT`/`DELETE` werten `If-Match: <etag>` aus (FR-7, verbindlich). `?validate=only` führt FR-5 (Dry-Run) aus → 200 mit Bericht, kein Schreiben.

Ein `409` beim Anlegen trägt ebenfalls einen **`Location`**-Header — die Lese-URL der
Ressource, die diese Identität bereits belegt, also genau die URL, die das `201` geliefert
hätte. Ein Client, dessen Wiederholung im Konflikt endet, hat damit trotzdem die URL und die
darin enthaltene ID, die er braucht, um anschließend Member hinzuzufügen; ohne
zwischengeschaltetes `GET` und ohne die Meldung zu parsen. Die übrigen Ablehnungen tragen
keinen `Location`: Ein `400` benennt keine Ressource von uns, auf die er zeigen könnte.

### 5.2 Beziehungs-/Mitgliedschafts-Endpunkte (FR-9/10/11)

| Methode & Pfad | Zweck |
|---|---|
| `GET  /catalogs/{id}/datasets` | Datasets des Katalogs |
| `PUT  /catalogs/{id}/datasets/{datasetId}` | Dataset in Katalog aufnehmen (Link, ohne Re-Upload) |
| `DELETE /catalogs/{id}/datasets/{datasetId}` | Zuordnung lösen |
| `PUT/DELETE /catalogs/{id}/services/{serviceId}` | DataService-Zuordnung |
| `PUT/DELETE /catalogs/{id}/catalogs/{subId}` | Sub-Katalog-Zuordnung |
| `GET  /datasets/{id}/distributions` | Distributions eines Datasets |
| `POST /datasets/{id}/distributions` | Distribution **im Kontext** anlegen (FR-10) |
| `PUT/DELETE /dataset-series/{id}/members/{datasetId}` | Serien-Mitgliedschaft (FR-11, AP1) |
| `PUT/DELETE /distributions/{id}/access-service/{serviceId}` | `accessService`-Verknüpfung |

Zusätzlich gibt es je Mitgliedschaft ein `POST /catalogs/{id}/datasets` (bzw.
`/services`, `/catalogs`) mit dem Member im Body: es **legt den Member an** und
verknüpft ihn anschließend. Es gelten daher dieselben Identitätsregeln wie für
`POST /datasets` — 400 bei fremdem `about`, **409 wenn der Member bereits existiert**.
Ein vorhandener Member wird also nicht überschrieben: zum Verknüpfen dient das `PUT`
oben, zum Ändern `PUT /datasets/{datasetId}`. Das ist Absicht — ein Dataset kann in
mehreren Katalogen, Serien und Services referenziert sein, und eine Änderung über einen
einzelnen Mitgliedschaftspfad würde diese unangekündigt für alle mit ändern.

**Wie eine Verknüpfung je Format geschrieben wird.** XMI serialisiert eine Mitgliedschaft
als `<dataset href="{base}/datasets/{id}#/"/>`, RDF als
`<dcat:dataset rdf:resource="{base}/datasets/{id}"/>`. Das angehängte `#/` ist
XMI-Zeigersyntax — Dokument-URL plus Fragment, das das Objekt darin benennt, wobei `/`
die Wurzel dieses Dokuments meint — und gehört **nicht** zur Identität; die Identität ist
die URL ohne Fragment. Ein Fragment wird nie an einen Server übertragen, beide Formen
adressieren über HTTP also dieselbe Ressource. Beim Schreiben wird ein `href` in beiden
Formen akzeptiert.

### 5.3 Massen-Ingest (FR-13/14/15)

| Methode & Pfad | Zweck |
|---|---|
| `POST /ingest?catalog={catalogId}&mode=merge\|replace` | Ganzes DCAT-Dokument transaktional upserten; `replace` = Graph-Ersatz (FR-14) |
| `POST /ingest?validate=only` | Dry-Run-Validierung eines ganzen Dokuments (FR-5) |
| `DELETE /catalogs/{id}?purge=true` | Katalog-Graph vollständig entfernen (FR-14) |

### 5.4 Beispiel — Distribution upserten (DCAT-XML)

```http
PUT /admin/api/v1/distributions/baum-kataster-csv HTTP/1.1
Content-Type: application/rdf+xml
If-Match: "a1b2c3"

<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:dcat="http://www.w3.org/ns/dcat#"
         xmlns:dct="http://purl.org/dc/terms/">
  <dcat:Distribution rdf:about="https://portal.example/admin/api/v1/distributions/baum-kataster-csv">
    <dct:title xml:lang="de">Baumkataster (CSV)</dct:title>
    <dcat:accessURL rdf:resource="https://data.example/baeume.csv"/>
    <dcat:downloadURL rdf:resource="https://data.example/baeume.csv"/>
    <dcat:mediaType>text/csv</dcat:mediaType>
  </dcat:Distribution>
</rdf:RDF>
```

```http
HTTP/1.1 200 OK
ETag: "d4e5f6"
Location: https://portal.example/admin/api/v1/distributions/baum-kataster-csv
```

### 5.5 Fehlermodell (FR-19)

| HTTP | Bedeutung | Body |
|---|---|---|
| 400 | Syntaktisch ungültiges RDF | Problem+JSON / RDF |
| 404 | Ressource unbekannt | — |
| 409 | Konflikt (z. B. Löschen referenzierter Ressource) | Verweisliste |
| 412 | `If-Match` passt nicht (FR-7) | aktuelles ETag |
| 422 | DCAT-AP-3-Validierung fehlgeschlagen (FR-4) | **SHACL-Report** (RDF/JSON) |

## 6. OSGi-Service-API

Primärer Kontrakt (D-Grundsatz „OSGi zuerst"). EMF-typisiert gegen `dcat.model`; der REST-Adapter aus §5 ruft ausschließlich diese Services. Aufgeteilt nach Verantwortung.

```java
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.data.atlas.dcat.dcat.Catalog;
import org.eclipse.fennec.data.atlas.dcat.dcat.Dataset;
import org.eclipse.fennec.data.atlas.dcat.dcat.DatasetSeries;   // ab AP1
import org.eclipse.fennec.data.atlas.dcat.dcat.DataService;
import org.eclipse.fennec.data.atlas.dcat.dcat.Distribution;

/**
 * CRUD + Upsert je DCAT-AP-3-Entität (FR-1..FR-8).
 * Implementierung: transaktional gegen Jena-TDB2, SHACL-validiert vor Commit.
 */
public interface CatalogAdminService {

    // --- Katalog -----------------------------------------------------------
    Catalog upsertCatalog(Catalog catalog);                 // FR-1/2/3/4
    Optional<Catalog> getCatalog(String id);
    List<Catalog> listCatalogs(Query query);
    void deleteCatalog(String id, boolean cascade);         // FR-1, 409 wenn referenziert

    // --- Dataset -----------------------------------------------------------
    Dataset upsertDataset(Dataset dataset);
    Optional<Dataset> getDataset(String id);
    List<Dataset> listDatasets(Query query);
    void deleteDataset(String id, boolean cascade);

    // --- DatasetSeries (ab AP1) -------------------------------------------
    DatasetSeries upsertDatasetSeries(DatasetSeries series);
    Optional<DatasetSeries> getDatasetSeries(String id);
    void deleteDatasetSeries(String id, boolean cascade);

    // --- DataService -------------------------------------------------------
    DataService upsertDataService(DataService service);
    Optional<DataService> getDataService(String id);
    List<DataService> listDataServices(Query query);
    void deleteDataService(String id, boolean cascade);

    // --- Distribution (immer im Kontext eines Datasets, FR-10) -------------
    Distribution upsertDistribution(String datasetId, Distribution distribution);
    Optional<Distribution> getDistribution(String id);
    void deleteDistribution(String id);
}
```

```java
/**
 * Beziehungs-/Mitgliedschaftspflege ohne Re-Upload der Zielressource (FR-9/10/11).
 */
public interface CatalogRelationService {
    void linkDatasetToCatalog(String catalogId, String datasetId);
    void unlinkDatasetFromCatalog(String catalogId, String datasetId);
    void linkServiceToCatalog(String catalogId, String serviceId);
    void unlinkServiceFromCatalog(String catalogId, String serviceId);
    void linkSubCatalog(String catalogId, String subCatalogId);
    void unlinkSubCatalog(String catalogId, String subCatalogId);
    void addSeriesMember(String seriesId, String datasetId);     // ab AP1
    void removeSeriesMember(String seriesId, String datasetId);
    void setAccessService(String distributionId, String serviceId);
}
```

```java
import java.io.InputStream;
import org.eclipse.emf.ecore.resource.Resource;

/**
 * Massen-Ingest ganzer DCAT-Dokumente (FR-13/14/15) — Anbindung Data-/Model-Atlas.
 * Eingebettet: EMF-Resource übergeben. Über REST: RDF-Stream + Mediatype.
 */
public interface CatalogIngestService {

    enum Mode { MERGE, REPLACE }

    IngestResult ingest(String catalogId, Resource dcatDocument, Mode mode);     // FR-13/14
    IngestResult ingest(String catalogId, InputStream rdf, String mediaType, Mode mode);
    void purgeCatalog(String catalogId);                                          // FR-14
}
```

```java
/**
 * Validierung als eigenständiger Dienst (FR-4/5) — vom Admin- und Ingest-Pfad genutzt,
 * auch standalone als Dry-Run aufrufbar.
 */
public interface DcatValidationService {
    ValidationReport validate(org.eclipse.emf.ecore.EObject resource);   // SHACL DCAT-AP 3
    ValidationReport validate(org.eclipse.emf.ecore.resource.Resource document);
    boolean isValid(org.eclipse.emf.ecore.EObject resource);
}
```

**Begleittypen:** `Query` (Filter/Pagination zu §5.1), `IngestResult` (angelegt/aktualisiert/abgelehnt + ETags), `ValidationReport` (SHACL-konformer Befund). **Fehler** als Runtime-Exceptions, die der REST-Adapter auf §5.5 abbildet:

```java
public class ValidationException extends RuntimeException { /* trägt ValidationReport -> 422 */ }
public class ConflictException   extends RuntimeException { /* Referenz/ETag -> 409/412     */ }
public class NotFoundException    extends RuntimeException { /* -> 404                       */ }
```

## 7. Mapping REST ↔ OSGi

| REST (§5) | OSGi (§6) |
|---|---|
| `PUT /{coll}/{id}` | `…AdminService.upsertX(...)` (ID aus Pfad/Payload, D2) |
| `GET /{coll}/{id}` | `getX(id)` + RIOT-Serialisierung |
| `DELETE /{coll}/{id}?cascade` | `deleteX(id, cascade)` |
| `PUT /catalogs/{id}/datasets/{dsId}` | `CatalogRelationService.linkDatasetToCatalog` |
| `POST /ingest` | `CatalogIngestService.ingest(...)` |
| `?validate=only` | `DcatValidationService.validate(...)` |
| `If-Match` / `ETag` | optimistische Sperre in der Impl (FR-7, verbindlich) |
| *(kein `PATCH`)* | Änderung nur via `upsertX(...)`-Replace (FR-2b) |
| RDF-Body ⇄ EMF | RIOT ⇄ EMF-DCAT-Brücke (AP2) |

Der REST-Adapter enthält **keine Fachlogik** — nur Serialisierung (RDF⇄EMF), Statuscode-Mapping und Auth-Hook (FR-21). Damit verhält sich eingebetteter und verteilter Betrieb identisch.

Das ist eine Vorgabe, wo Regeln liegen, und nicht bloß eine Beschreibung. Die **Identitäts­regel**
gehört dazu: `DcatIds.idForWrite` übernimmt ein `about`, das eine unserer Ressourcen benennt,
mintet nur bei fehlendem `about` und wirft andernfalls `ForeignIdentityException`. Durchgesetzt
wird sie im *Service*, damit ein Importer oder ein anderes Bundle mit direktem Aufruf von
`upsertDataset(...)` dieselbe Antwort erhält wie `POST /admin/datasets`; der Adapter macht aus
der Ablehnung lediglich ein `400`. Das war nicht immer so — die Regel lag allein im Adapter,
während der Service darunter für ein fremdes `about` stillschweigend eine neue ID mintete:
genau die Divergenz, die dieses Prinzip verhindern soll.

## 8. Offene Punkte (Review)

1. ~~**PATCH-Form**~~ — **entschieden (2026-06-27): kein PATCH, ausschließlich Replace per `PUT`-Upsert** (D8/FR-2b). ETag/`If-Match` verbindlich (FR-7).
2. **CatalogRecord:** automatisch vom Portal gepflegt (Provenienz, FR-20) oder explizit über die API?
3. **`replace`-Ingest-Semantik:** Tilgt fehlende Ressourcen pro Katalog-Graph — gewünschtes Default-Verhalten?
4. **Erreichbarkeitsprüfung (FR-17):** synchron blockierend, asynchron meldend oder rein optional?
5. **Auth (FR-21 / Planung §11.7):** Keycloak wie MDO? Welche Rollen (read/write/admin)?
6. **ID-Schema:** Slugs (lesbar) vs. opake UUIDs für geminteten Teil (D2/FR-3).
7. **Versionierung der API:** `…/v1`-Pfad bestätigt?
```

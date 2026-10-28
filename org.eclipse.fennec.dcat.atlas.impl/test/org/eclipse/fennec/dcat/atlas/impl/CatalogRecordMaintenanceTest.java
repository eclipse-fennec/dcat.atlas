/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.dcat.atlas.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;
import dcat.CatalogRecord;
import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;

/**
 * The catalogue records the server maintains (#56).
 *
 * <h2>What a record is</h2>
 *
 * One per <em>member</em> of a catalog, not one per catalog: it describes the entry, and its
 * {@code dcterms:modified} answers a question the member's own cannot — when the catalogue
 * entry's metadata last changed, as opposed to when the data did. That is what a harvester
 * asking "what moved since Tuesday" reads, so it has to move when the member's metadata
 * moves, not only when membership does.
 */
public class CatalogRecordMaintenanceTest {

	private static final String CATALOGS = StoreLayout.LOGICAL_BASE + "catalogs/";
	private static final String DATASETS = StoreLayout.LOGICAL_BASE + "datasets/";
	private static final String SERVICES = StoreLayout.LOGICAL_BASE + "data-services/";

	@TempDir
	Path storage;

	private DistributionAdminServiceImpl distributions() {
		return TestNotifiers.announcing(new DistributionAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH, datasets()));
	}

	private CatalogAdminServiceImpl catalogs() {
		return TestNotifiers.announcing(new CatalogAdminServiceImpl(TestResourceSets.factory(),
				TestGitStore.at(storage), TestGitStore.BASE_PATH));
	}

	private DatasetAdminServiceImpl datasets() {
		return TestNotifiers.announcing(new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH));
	}

	private DataServiceAdminServiceImpl dataServices() {
		return TestNotifiers.announcing(new DataServiceAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH));
	}

	// --- a member entering the catalog --------------------------------------

	@Test
	void addingADatasetToACatalogCreatesItsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));

		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));

		CatalogRecord record = onlyRecord("gov");
		assertEquals(DATASETS + "air", topicIri(record), "the record describes the member");
		assertNotNull(record.getModified(), "dcterms:modified is mandatory on a record");
		assertNotNull(record.getIssued(), "when the entry was created is worth keeping");
	}

	@Test
	void linkingAnExistingDataServiceCreatesItsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		dataServices().upsertDataService(dataService("sparql", "SPARQL"));

		catalogs().linkDataServiceToCatalog("gov", "sparql");

		assertEquals(SERVICES + "sparql", topicIri(onlyRecord("gov")));
	}

	@Test
	void aRepeatedLinkDoesNotAddASecondRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));

		catalogs().linkDatasetToCatalog("gov", "air");

		assertEquals(1, records("gov").size(), "one entry, one record");
	}

	// --- a member leaving ----------------------------------------------------

	@Test
	void unlinkingAMemberRemovesItsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));

		catalogs().deleteDatasetFromCatalog("gov", "air");

		assertTrue(records("gov").isEmpty(), "a record describes an entry that no longer exists");
	}

	@Test
	void deletingTheMemberRemovesItsRecordFromEveryCatalog() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().upsertCatalog(catalog("eu", "EU Portal"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));
		catalogs().linkDatasetToCatalog("eu", "air");

		datasets().deleteDataset("air", true);

		assertTrue(records("gov").isEmpty(), "gov still holds a record for a dataset that is gone");
		assertTrue(records("eu").isEmpty(), "eu still holds a record for a dataset that is gone");
	}

	// --- the member's own metadata changing ----------------------------------

	/**
	 * The decision this feature rests on: a plain {@code PUT} of the member, which never
	 * touches the catalog, still moves the record's date. Without this the record reports
	 * membership changes while looking like provenance a harvester can trust.
	 */
	@Test
	void writingTheMemberRestampsItsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));
		String before = modified(onlyRecord("gov"));

		datasets().upsertDataset(dataset("air", "Air quality (revised)"));

		assertNotEquals(before, modified(onlyRecord("gov")), "the entry's metadata changed");
		assertEquals(DATASETS + "air", topicIri(onlyRecord("gov")), "and it still describes the member");
	}

	@Test
	void writingAMemberOfTwoCatalogsRestampsBoth() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().upsertCatalog(catalog("eu", "EU Portal"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));
		catalogs().linkDatasetToCatalog("eu", "air");
		String gov = modified(onlyRecord("gov"));
		String eu = modified(onlyRecord("eu"));

		datasets().upsertDataset(dataset("air", "Air quality (revised)"));

		assertNotEquals(gov, modified(onlyRecord("gov")));
		assertNotEquals(eu, modified(onlyRecord("eu")));
	}

	/**
	 * A distribution is contained in its dataset, so writing one rewrites the dataset — and
	 * what a catalogue entry says about that dataset has changed.
	 */
	@Test
	void writingADistributionRestampsItsDatasetsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));
		String before = modified(onlyRecord("gov"));

		distributions().upsertDistributionToDataset("air", distribution("csv"));

		assertNotEquals(before, modified(onlyRecord("gov")));
	}

	@Test
	void writingADatasetThatIsInNoCatalogChangesNothing() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));

		datasets().upsertDataset(dataset("loose", "Not catalogued"));

		assertTrue(records("gov").isEmpty());
	}

	// --- a client cannot write records ---------------------------------------

	/**
	 * A catalog body carries its records, because they are contained in it. A client that
	 * sends one back — or sends none — must not be able to rewrite the server's bookkeeping,
	 * the same reason {@code about} is re-stamped rather than believed.
	 */
	@Test
	void aPlainCatalogWriteKeepsTheRecordsTheEntriesHad() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));
		String before = modified(onlyRecord("gov"));

		// What a read-modify-write client sends back: the membership, no records.
		Catalog edited = catalog("gov", "GovData (renamed)");
		edited.getDataset().add(datasets().getDataset("air").orElseThrow());
		catalogs().upsertCatalog(edited);

		assertEquals(DATASETS + "air", topicIri(onlyRecord("gov")), "the entry still has its record");
		assertEquals(before, modified(onlyRecord("gov")),
				"renaming the catalog did not change the dataset's entry metadata");
	}

	@Test
	void aClientSuppliedRecordIsDropped() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));

		Catalog edited = catalog("gov", "GovData");
		edited.getDataset().add(datasets().getDataset("air").orElseThrow());
		CatalogRecord forged = DcatFactory.eINSTANCE.createCatalogRecord();
		forged.setPrimaryTopic(datasets().getDataset("air").orElseThrow());
		forged.setModified(dateTime("1999-01-01T00:00:00Z"));
		edited.getRecord().add(forged);
		catalogs().upsertCatalog(edited);

		assertEquals(1, records("gov").size(), "one entry, one record");
		assertNotEquals("1999-01-01T00:00:00Z", modified(onlyRecord("gov")),
				"a client does not get to say when a catalogue entry was last touched");
	}

	@Test
	void aMemberDroppedFromACatalogWriteLosesItsRecord() {
		catalogs().upsertCatalog(catalog("gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"));

		// The same body without the membership: the entry is gone, so its record is too.
		catalogs().upsertCatalog(catalog("gov", "GovData"));

		assertTrue(records("gov").isEmpty());
	}

	// --- helpers -------------------------------------------------------------

	private List<CatalogRecord> records(String catalogId) {
		return catalogs().getCatalog(catalogId).orElseThrow().getRecord();
	}

	private CatalogRecord onlyRecord(String catalogId) {
		List<CatalogRecord> records = records(catalogId);
		assertEquals(1, records.size(), "expected exactly one record in catalog " + catalogId);
		return records.get(0);
	}

	private static String topicIri(CatalogRecord record) {
		EObject topic = (EObject) record.eGet(record.eClass().getEStructuralFeature("primaryTopic"), false);
		return topic == null ? null : Members.targetIri(topic);
	}

	private static rdf.DateOrDateTimeLiteral dateTime(String lexical) {
		rdf.DateOrDateTimeLiteral literal = rdf.RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		literal.setValue(javax.xml.datatype.DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(lexical));
		return literal;
	}

	private static String modified(CatalogRecord record) {
		return record.getModified() == null ? null : String.valueOf(record.getModified().getValue());
	}

	private static Catalog catalog(String id, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(CATALOGS + id);
		return TestEntities.mandatoryDataset(catalog, title);
	}

	private static Dataset dataset(String id, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASETS + id);
		return TestEntities.mandatoryDataset(dataset, title);
	}

	private static dcat.Distribution distribution(String id) {
		dcat.Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(StoreLayout.distributionIri("air", id));
		return TestEntities.mandatoryDistribution(distribution);
	}

	private static DataService dataService(String id, String title) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(SERVICES + id);
		return TestEntities.mandatoryDataService(service, title);
	}
}

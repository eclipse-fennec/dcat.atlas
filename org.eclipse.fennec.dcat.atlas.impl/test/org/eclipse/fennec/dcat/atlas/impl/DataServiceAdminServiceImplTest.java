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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.ResourceInUseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Round-trips the file-backed data-service store (admin impl, which also covers
 * the inherited read operations).
 */
public class DataServiceAdminServiceImplTest {

	private static final String BASE = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE + "data-services/";
	private static final String DATASETS = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE + "datasets/";

	@TempDir
	Path storage;

	private DataServiceAdminServiceImpl service() {
		return new DataServiceAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentDataService() {
		service().upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));

		Optional<DataService> loaded = service().getDataService("sparql");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "sparql", loaded.get().getAbout());
		assertEquals("SPARQL endpoint", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredDataService() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		service.upsertDataService(dataService(BASE + "wfs", "WFS endpoint"));
		assertEquals(2, service.listDataServices().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDataService("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDataService() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		service.deleteDataService("sparql", false);
		assertTrue(service.getDataService("sparql").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(null, "Untitled about"));
		assertEquals(1, service.listDataServices().size());
	}

	// --- dcat:servesDataset membership -------------------------------------

	@Test
	void addStoresTheDatasetAndLinksIt() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));

		DataService stored = service.addDatasetToDataService("sparql", dataset(DATASETS + "air", "Air quality"));

		assertEquals(1, stored.getServesDataset().size());
		assertEquals(DATASETS + "air", stored.getServesDataset().get(0).getAbout());
		// The dataset is a store entity in its own right, not a copy inside the service.
		assertTrue(datasets().getDataset("air").isPresent());
	}

	@Test
	void linkRequiresTheDatasetToExistAlready() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));

		assertThrows(NoSuchElementException.class, () -> service.linkDatasetToDataService("sparql", "air"));

		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		DataService stored = service.linkDatasetToDataService("sparql", "air");
		assertEquals(1, stored.getServesDataset().size());
	}

	@Test
	void linkLeavesTheDatasetItsOwnContent() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));

		service.linkDatasetToDataService("sparql", "air");

		// Unlike add, link writes nothing: the dataset keeps its title.
		assertEquals("Air quality", datasets().getDataset("air").get().getTitle().get(0).getValue());
	}

	@Test
	void linkIsIdempotentAndLeavesTheEtagAlone() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));

		service.linkDatasetToDataService("sparql", "air");
		String first = service.etag("sparql").orElseThrow();
		service.linkDatasetToDataService("sparql", "air");

		assertEquals(1, service.getDataService("sparql").get().getServesDataset().size());
		assertEquals(first, service.etag("sparql").orElseThrow());
	}

	@Test
	void removeUnlinksButKeepsTheDataset() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		service.linkDatasetToDataService("sparql", "air");

		service.deleteDatasetFromDataService("sparql", "air");

		assertTrue(service.getDataService("sparql").get().getServesDataset().isEmpty());
		assertTrue(datasets().getDataset("air").isPresent());
		// Idempotent: unlinking again is a no-op, not a failure.
		service.deleteDatasetFromDataService("sparql", "air");
	}

	@Test
	void membershipOnAnUnknownDataServiceFails() {
		DataServiceAdminServiceImpl service = service();
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		assertThrows(NoSuchElementException.class, () -> service.linkDatasetToDataService("missing", "air"));
		assertThrows(NoSuchElementException.class,
				() -> service.addDatasetToDataService("missing", dataset(DATASETS + "no2", "NO2")));
	}

	@Test
	void servedDatasetCannotBeDeletedWhileStillReferenced() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		service.linkDatasetToDataService("sparql", "air");

		// FR-1 walks every non-containment EReference reflectively, so servesDataset is
		// covered without naming it anywhere — this pins that it really is.
		assertThrows(ResourceInUseException.class, () -> datasets().deleteDataset("air", false));

		// ...and a cascade detaches the reference instead of leaving a dangling one.
		datasets().deleteDataset("air", true);
		assertTrue(service.getDataService("sparql").get().getServesDataset().isEmpty());
	}

	private DatasetAdminServiceImpl datasets() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		return dataset;
	}

	private static DataService dataService(String about, String title) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		if (about != null) {
			service.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		service.getTitle().add(literal);
		return service;
	}
}

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

import org.eclipse.fennec.dcat.atlas.impl.handler.DataServiceAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.integrity.ResourceInUseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips the file-backed data-service store (admin impl, which also covers
 * the inherited read operations).
 */
public class DataServiceAdminHandlerImplTest {

	private static final String BASE = org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.LOGICAL_BASE + "data-services/";
	private static final String DATASETS = org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout.LOGICAL_BASE + "datasets/";

	@TempDir
	Path storage;

	private DataServiceAdminHandlerImpl service() {
		return new DataServiceAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage));
	}

	@Test
	void upsertThenGetReturnsEquivalentDataService() {
		service().upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);

		Optional<DataService> loaded = service().getDataService("sparql");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "sparql", loaded.get().getAbout());
		assertEquals("SPARQL endpoint", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredDataService() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		service.upsertDataService(dataService(BASE + "wfs", "WFS endpoint"), Caller.ANONYMOUS);
		assertEquals(2, service.listDataServices().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDataService("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDataService() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		service.deleteDataService("sparql", false, Caller.ANONYMOUS);
		assertTrue(service.getDataService("sparql").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(null, "Untitled about"), Caller.ANONYMOUS);
		assertEquals(1, service.listDataServices().size());
	}

	// --- dcat:servesDataset membership -------------------------------------

	@Test
	void addStoresTheDatasetAndLinksIt() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);

		DataService stored = service.addDatasetToDataService("sparql", dataset(DATASETS + "air", "Air quality"),
				Caller.ANONYMOUS);

		assertEquals(1, stored.getServesDataset().size());
		assertEquals(DATASETS + "air", stored.getServesDataset().get(0).getAbout());
		// The dataset is a store entity in its own right, not a copy inside the service.
		assertTrue(datasets().getDataset("air").isPresent());
	}

	@Test
	void linkRequiresTheDatasetToExistAlready() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);

		assertThrows(NoSuchElementException.class, () -> service.linkDatasetToDataService("sparql", "air",
				Caller.ANONYMOUS));

		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);
		DataService stored = service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);
		assertEquals(1, stored.getServesDataset().size());
	}

	@Test
	void linkLeavesTheDatasetItsOwnContent() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);

		service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);

		// Unlike add, link writes nothing: the dataset keeps its title.
		assertEquals("Air quality", datasets().getDataset("air").get().getTitle().get(0).getValue());
	}

	@Test
	void linkIsIdempotentAndLeavesTheEtagAlone() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);

		service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);
		String first = service.etag("sparql").orElseThrow();
		service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);

		assertEquals(1, service.getDataService("sparql").get().getServesDataset().size());
		assertEquals(first, service.etag("sparql").orElseThrow());
	}

	@Test
	void removeUnlinksButKeepsTheDataset() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);
		service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);

		service.deleteDatasetFromDataService("sparql", "air", Caller.ANONYMOUS);

		assertTrue(service.getDataService("sparql").get().getServesDataset().isEmpty());
		assertTrue(datasets().getDataset("air").isPresent());
		// Idempotent: unlinking again is a no-op, not a failure.
		service.deleteDatasetFromDataService("sparql", "air", Caller.ANONYMOUS);
	}

	@Test
	void membershipOnAnUnknownDataServiceFails() {
		DataServiceAdminHandlerImpl service = service();
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);
		assertThrows(NoSuchElementException.class, () -> service.linkDatasetToDataService("missing", "air",
				Caller.ANONYMOUS));
		assertThrows(NoSuchElementException.class,
				() -> service.addDatasetToDataService("missing", dataset(DATASETS + "no2", "NO2"), Caller.ANONYMOUS));
	}

	@Test
	void servedDatasetCannotBeDeletedWhileStillReferenced() {
		DataServiceAdminHandlerImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"), Caller.ANONYMOUS);
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);
		service.linkDatasetToDataService("sparql", "air", Caller.ANONYMOUS);

		// FR-1 walks every non-containment EReference reflectively, so servesDataset is
		// covered without naming it anywhere — this pins that it really is.
		assertThrows(ResourceInUseException.class, () -> datasets().deleteDataset("air", false, Caller.ANONYMOUS));

		// ...and a cascade detaches the reference instead of leaving a dangling one.
		datasets().deleteDataset("air", true, Caller.ANONYMOUS);
		assertTrue(service.getDataService("sparql").get().getServesDataset().isEmpty());
	}

	private DatasetAdminHandlerImpl datasets() {
		return new DatasetAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage));
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		return TestEntities.mandatoryDataset(dataset, title);
	}

	/** No description: §4.4 does not make it Pflicht for a DataService,
			and Dataset::HasDescription does not reach here. */
	private static DataService dataService(String about, String title) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		if (about != null) {
			service.setAbout(about);
		}
		return TestEntities.mandatoryDataService(service, title);
	}
}

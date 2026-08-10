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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * FR-10: a Distribution is created/read/deleted only in the context of its
 * Dataset. The Distribution itself is kept in the distribution store; the
 * dataset->distribution link is a {@code dcat:distribution} URI reference the
 * service maintains on the owning Dataset.
 */
public class DistributionAdminServiceImplTest {

	private static final String DATASET_BASE = "https://portal.example/admin/api/v1/datasets/";
	/** Distributions dereference under their dataset, e.g. {@code .../datasets/air/distributions/csv}. */
	private static final String DIST_BASE = DATASET_BASE + "air/distributions/";
	/** DataServices are catalog entities of their own, dereferencing under /data-services/. */
	private static final String SERVICE_BASE = "https://portal.example/admin/api/v1/data-services/";

	@TempDir
	Path storage;

	@TempDir
	Path datasetStorage;

	private DatasetAdminServiceImpl datasetService;

	private DistributionAdminServiceImpl service() {
		datasetService = new DatasetAdminServiceImpl(TestResourceSets.factory(), datasetStorage);
		return new DistributionAdminServiceImpl(TestResourceSets.factory(), storage, datasetService);
	}

	/** Seeds an (empty) owning dataset "air" and returns the ready distribution service. */
	private DistributionAdminServiceImpl serviceWithDataset() {
		DistributionAdminServiceImpl service = service();
		datasetService.upsertDataset(dataset(DATASET_BASE + "air", "Air quality"));
		return service;
	}

	@Test
	void upsertStoresDistributionAndLinksItToDataset() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		Optional<Distribution> loaded = service.getDistributionForDataset("air", "csv");
		assertTrue(loaded.isPresent());
		assertEquals(DIST_BASE + "csv", loaded.get().getAbout());
		assertEquals("CSV download", loaded.get().getTitle().getValue());

		// The owning dataset now references the distribution.
		Dataset dataset = datasetService.getDataset("air").get();
		assertEquals(1, dataset.getDistribution().size());
		assertEquals(DIST_BASE + "csv", dataset.getDistribution().get(0).getResource());
	}

	@Test
	void upsertWithoutDatasetIsRejected() {
		DistributionAdminServiceImpl service = service();
		// No dataset "air" seeded: a Distribution cannot exist without a Dataset.
		assertThrows(NoSuchElementException.class,
				() -> service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download")));
	}

	@Test
	void listReturnsOnlyTheDatasetsDistributions() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "json", "JSON download"));

		assertEquals(2, service.listDistributionsForDataset("air").size());
	}

	@Test
	void getForWrongDatasetIsEmpty() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));
		datasetService.upsertDataset(dataset(DATASET_BASE + "water", "Water quality"));

		// "csv" belongs to "air", not "water".
		assertTrue(service.getDistributionForDataset("water", "csv").isEmpty());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(serviceWithDataset().getDistributionForDataset("air", "does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesDistributionAndUnlinksItFromDataset() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		service.deleteDistributionFromDataset("air", "csv");

		assertTrue(service.getDistributionForDataset("air", "csv").isEmpty());
		assertTrue(datasetService.getDataset("air").get().getDistribution().isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		Distribution distribution = distribution(null, "Untitled about");
		service.upsertDistributionToDataset("air", distribution);
		// Stored under a minted id; with no about there is no dataset link to add.
		assertTrue(datasetService.getDataset("air").get().getDistribution().isEmpty());
	}

	// --- FR-10 accessService link ------------------------------------------

	@Test
	void addAccessServiceStoresAReferenceNotACopy() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "wfs", "Air WFS"));

		Distribution loaded = service.getDistributionForDataset("air", "csv").get();
		assertEquals(1, loaded.getAccessService().size());
		// A dcat:accessService rdf:resource pointer at the service's about — the service
		// itself is not embedded, so it stays a single catalog entity (DCAT-AP.de §4.6.24).
		assertEquals(SERVICE_BASE + "wfs", loaded.getAccessService().get(0).getResource());
	}

	@Test
	void addAccessServiceIsIdempotent() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "wfs", "Air WFS"));
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "wfs", "Air WFS"));

		assertEquals(1, service.getDistributionForDataset("air", "csv").get().getAccessService().size());
	}

	@Test
	void addAccessServiceKeepsSeveralServices() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		// Multiplicity is [*]: the same data may be served by more than one service.
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "wfs", "Air WFS"));
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "ogcapi", "Air OGC API"));

		assertEquals(2, service.getDistributionForDataset("air", "csv").get().getAccessService().size());
	}

	@Test
	void addAccessServiceWithoutAboutIsRejected() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		assertThrows(IllegalArgumentException.class, () -> service.addAccessServiceToDistribution("air", "csv",
				dataService(null, "Nameless service")));
	}

	@Test
	void addAccessServiceForUnknownDistributionIsRejected() {
		DistributionAdminServiceImpl service = serviceWithDataset();

		assertThrows(NoSuchElementException.class, () -> service.addAccessServiceToDistribution("air", "nope",
				dataService(SERVICE_BASE + "wfs", "Air WFS")));
	}

	@Test
	void deleteAccessServiceRemovesOnlyTheReference() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "wfs", "Air WFS"));
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICE_BASE + "ogcapi", "Air OGC API"));

		service.deleteAccessServiceFromDistribution("air", "csv", "wfs");

		Distribution loaded = service.getDistributionForDataset("air", "csv").get();
		assertEquals(1, loaded.getAccessService().size());
		assertEquals(SERVICE_BASE + "ogcapi", loaded.getAccessService().get(0).getResource());
	}

	@Test
	void deleteAccessServiceThatIsNotLinkedIsANoOp() {
		DistributionAdminServiceImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"));

		service.deleteAccessServiceFromDistribution("air", "csv", "wfs");

		assertTrue(service.getDistributionForDataset("air", "csv").get().getAccessService().isEmpty());
	}

	private static DataService dataService(String about, String title) {
		DataService dataService = DcatFactory.eINSTANCE.createDataService();
		if (about != null) {
			dataService.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataService.getTitle().add(literal);
		return dataService;
	}

	private static Distribution distribution(String about, String title) {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		if (about != null) {
			distribution.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		distribution.setTitle(literal);
		return distribution;
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		if (about != null) {
			dataset.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		return dataset;
	}
}

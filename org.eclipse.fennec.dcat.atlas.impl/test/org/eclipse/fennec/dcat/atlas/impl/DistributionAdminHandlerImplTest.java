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
import org.eclipse.fennec.dcat.atlas.impl.handler.DistributionAdminHandlerImpl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.identity.ForeignIdentityException;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;

/**
 * FR-10: a Distribution exists only in the context of its Dataset.
 * <p>
 * Since the storage rework that is structural rather than enforced: {@code
 * dcat:distribution} is containment, so a Distribution is stored <em>inside</em>
 * its Dataset's file and cannot outlive it. Its identity nests to match, at
 * {@code …/datasets/air/distributions/csv}.
 */
public class DistributionAdminHandlerImplTest {

	private static final String DATASETS = StoreLayout.LOGICAL_BASE + "datasets/";
	private static final String SERVICES = StoreLayout.LOGICAL_BASE + "data-services/";
	private static final String DIST_BASE = DATASETS + "air/distributions/";

	@TempDir
	Path storage;

	private DatasetAdminHandlerImpl datasetService;

	private DistributionAdminHandlerImpl service() {

		datasetService = new DatasetAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage));
		return new DistributionAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage), datasetService);
	}

	/** Seeds an (empty) owning dataset "air" and returns the ready distribution service. */
	private DistributionAdminHandlerImpl serviceWithDataset() {
		DistributionAdminHandlerImpl service = service();
		datasetService.upsertDataset(dataset(DATASETS + "air", "Air quality"), Caller.ANONYMOUS);
		return service;
	}

	// --- storage ------------------------------------------------------------

	@Test
	void upsertStoresDistributionInsideItsDataset() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		Optional<Distribution> loaded = service.getDistributionForDataset("air", "csv");
		assertTrue(loaded.isPresent());
		assertEquals(DIST_BASE + "csv", loaded.get().getAbout());
		assertEquals("CSV download", loaded.get().getTitle().getValue());

		// Contained, so it is part of the dataset — not a link to a separate file.
		assertEquals(1, datasetService.getDataset("air").get().getDistribution().size());
	}

	@Test
	void theDistributionLivesInTheDatasetFile() throws Exception {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		String stored = TestGitStore.stored(storage, StoreLayout.DATASETS, "air");
		assertTrue(stored.contains("CSV download"), stored);
		assertFalse(TestGitStore.paths(storage).stream().anyMatch(path -> path.contains("/distributions/")),
				"there is no distribution store any more: " + TestGitStore.paths(storage));
	}

	@Test
	void upsertWithoutDatasetIsRejected() {
		DistributionAdminHandlerImpl service = service();
		// No dataset "air" seeded: a Distribution cannot exist without a Dataset.
		assertThrows(NoSuchElementException.class,
				() -> service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"),
						Caller.ANONYMOUS));
	}

	@Test
	void listReturnsOnlyTheDatasetsDistributions() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "json", "JSON download"), Caller.ANONYMOUS);

		assertEquals(2, service.listDistributionsForDataset("air").size());
	}

	@Test
	void upsertReplacesRatherThanAccumulates() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download v2"),
				Caller.ANONYMOUS);

		assertEquals(1, service.listDistributionsForDataset("air").size());
		assertEquals("CSV download v2", service.getDistributionForDataset("air", "csv").get().getTitle().getValue());
	}

	@Test
	void getForWrongDatasetIsEmpty() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);
		datasetService.upsertDataset(dataset(DATASETS + "water", "Water quality"), Caller.ANONYMOUS);

		// "csv" belongs to "air", not "water".
		assertTrue(service.getDistributionForDataset("water", "csv").isEmpty());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(serviceWithDataset().getDistributionForDataset("air", "does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDistributionFromItsDataset() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		service.deleteDistributionFromDataset("air", "csv", Caller.ANONYMOUS);

		assertTrue(service.getDistributionForDataset("air", "csv").isEmpty());
		assertTrue(datasetService.getDataset("air").get().getDistribution().isEmpty());
	}

	@Test
	void deletingTheDatasetTakesItsDistributionsWithIt() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		datasetService.deleteDataset("air", false, Caller.ANONYMOUS);

		assertTrue(service.listDistributionsForDataset("air").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		Distribution stored = service.upsertDistributionToDataset("air", distribution(null, "Untitled about"),
				Caller.ANONYMOUS);

		assertTrue(stored.getAbout().startsWith(DIST_BASE), stored.getAbout());
		assertEquals(1, service.listDistributionsForDataset("air").size());
	}

	/**
	 * The nested identity follows the same rule, through its own derivation
	 * ({@code DcatIds.distributionIdForWrite}): mint only when nothing was asked for,
	 * refuse anything we cannot file here. "Here" is the point — a Distribution's identity
	 * nests inside <em>this</em> dataset (FR-10), so an about under a *different* dataset
	 * is as foreign as somebody else's URL, and silently minting would have stored it under
	 * this dataset while the caller believed it belonged to the other one.
	 */
	@Test
	void anAboutFromAnotherDatasetIsRefused() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		datasetService.upsertDataset(dataset(DATASETS + "water", "Water quality"), Caller.ANONYMOUS);

		assertThrows(ForeignIdentityException.class, () -> service.upsertDistributionToDataset("air",
				distribution(DATASETS + "water/distributions/csv", "Wrong dataset"), Caller.ANONYMOUS));

		assertTrue(service.listDistributionsForDataset("air").isEmpty(), "a refused write must store nothing");
	}

	@Test
	void aForeignDistributionAboutIsRefused() {
		DistributionAdminHandlerImpl service = serviceWithDataset();

		assertThrows(ForeignIdentityException.class, () -> service.upsertDistributionToDataset("air",
				distribution("https://someone-else.example/files/data.csv", "Foreign"), Caller.ANONYMOUS));

		assertTrue(service.listDistributionsForDataset("air").isEmpty());
	}

	@Test
	void theEtagIsTheOwningDatasets() {
		// A distribution has no stored bytes of its own, so its version is the dataset's.
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		assertEquals(datasetService.etag("air").orElseThrow(), service.etag("air", "csv").orElseThrow());
		assertTrue(service.etag("air", "does-not-exist").isEmpty());
	}

	// --- FR-10 accessService link ------------------------------------------

	@Test
	void addAccessServiceStoresAReferenceNotACopy() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "wfs", "Air WFS"),
				Caller.ANONYMOUS);

		Distribution loaded = service.getDistributionForDataset("air", "csv").get();
		assertEquals(1, loaded.getAccessService().size());
		// A cross-resource reference at the service's own identity — the DataService
		// stays a single catalog entity (DCAT-AP.de §4.6.24).
		assertEquals(SERVICES + "wfs", loaded.getAccessService().get(0).getAbout());
	}

	@Test
	void addAccessServiceIsIdempotent() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "wfs", "Air WFS"),
				Caller.ANONYMOUS);
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "wfs", "Air WFS"),
				Caller.ANONYMOUS);

		assertEquals(1, service.getDistributionForDataset("air", "csv").get().getAccessService().size());
	}

	@Test
	void linkAccessServiceRequiresTheServiceToExist() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		assertThrows(NoSuchElementException.class,
				() -> service.linkAccessServiceToDistribution("air", "csv", "wfs", Caller.ANONYMOUS));

		new DataServiceAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage))
				.upsertDataService(dataService(SERVICES + "wfs", "Air WFS"), Caller.ANONYMOUS);
		Distribution linked = service.linkAccessServiceToDistribution("air", "csv", "wfs", Caller.ANONYMOUS);
		assertEquals(1, linked.getAccessService().size());
	}

	@Test
	void addAccessServiceKeepsSeveralServices() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		// Multiplicity is [*]: the same data may be served by more than one service.
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "wfs", "Air WFS"),
				Caller.ANONYMOUS);
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "ogcapi", "Air OGC API"),
				Caller.ANONYMOUS);

		assertEquals(2, service.getDistributionForDataset("air", "csv").get().getAccessService().size());
	}

	@Test
	void addAccessServiceForUnknownDistributionIsRejected() {
		DistributionAdminHandlerImpl service = serviceWithDataset();

		assertThrows(NoSuchElementException.class, () -> service.addAccessServiceToDistribution("air", "nope",
				dataService(SERVICES + "wfs", "Air WFS"), Caller.ANONYMOUS));
	}

	@Test
	void deleteAccessServiceRemovesOnlyTheReference() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "wfs", "Air WFS"),
				Caller.ANONYMOUS);
		service.addAccessServiceToDistribution("air", "csv", dataService(SERVICES + "ogcapi", "Air OGC API"),
				Caller.ANONYMOUS);

		service.deleteAccessServiceFromDistribution("air", "csv", "wfs", Caller.ANONYMOUS);

		Distribution loaded = service.getDistributionForDataset("air", "csv").get();
		assertEquals(1, loaded.getAccessService().size());
		assertEquals(SERVICES + "ogcapi", loaded.getAccessService().get(0).getAbout());
		// The DataService itself survives — only the link went.

		assertTrue(new DataServiceAdminHandlerImpl(TestNotifiers.discarding(), TestStores.at(storage)).getDataService("wfs")
				.isPresent());
	}

	@Test
	void deleteAccessServiceThatIsNotLinkedIsANoOp() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		service.upsertDistributionToDataset("air", distribution(DIST_BASE + "csv", "CSV download"), Caller.ANONYMOUS);

		service.deleteAccessServiceFromDistribution("air", "csv", "wfs", Caller.ANONYMOUS);

		assertTrue(service.getDistributionForDataset("air", "csv").get().getAccessService().isEmpty());
	}

	// --- fixtures -----------------------------------------------------------

	private static DataService dataService(String about, String title) {
		DataService dataService = DcatFactory.eINSTANCE.createDataService();
		if (about != null) {
			dataService.setAbout(about);
		}
		return TestEntities.mandatoryDataService(dataService, title);
	}

	private static Distribution distribution(String about, String title) {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		if (about != null) {
			distribution.setAbout(about);
		}
		distribution.setTitle(TestEntities.literal(title));
		return TestEntities.mandatoryDistribution(distribution);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		if (about != null) {
			dataset.setAbout(about);
		}
		return TestEntities.mandatoryDataset(dataset, title);
	}

	@Test
	void aMissingFormatIsCompletedFromTheMediaTypeOnWrite() {
		// Issue #68: a writer that knows only the IANA media type must still be found by
		// clients querying dct:format, the field German portals actually fill.
		DistributionAdminHandlerImpl service = serviceWithDataset();
		Distribution sent = distribution(DIST_BASE + "geo", "OGC Features");
		sent.setMediaType("http://www.iana.org/assignments/media-types/application/geo+json");
		service.upsertDistributionToDataset("air", sent, Caller.ANONYMOUS);

		Distribution stored = service.getDistributionForDataset("air", "geo").orElseThrow();
		assertEquals("http://publications.europa.eu/resource/authority/file-type/GEOJSON", stored.getFormat());
		assertEquals("http://www.iana.org/assignments/media-types/application/geo+json", stored.getMediaType());
	}

	@Test
	void anExplicitFormatSurvivesTheWrite() {
		DistributionAdminHandlerImpl service = serviceWithDataset();
		Distribution sent = distribution(DIST_BASE + "geo", "OGC Features");
		sent.setMediaType("http://www.iana.org/assignments/media-types/application/geo+json");
		sent.setFormat("http://publications.europa.eu/resource/authority/file-type/JSON");
		service.upsertDistributionToDataset("air", sent, Caller.ANONYMOUS);

		Distribution stored = service.getDistributionForDataset("air", "geo").orElseThrow();
		assertEquals("http://publications.europa.eu/resource/authority/file-type/JSON", stored.getFormat());
	}
}

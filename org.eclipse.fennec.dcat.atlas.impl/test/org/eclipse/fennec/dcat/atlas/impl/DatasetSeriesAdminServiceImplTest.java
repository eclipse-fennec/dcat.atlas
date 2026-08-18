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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;

/**
 * Round-trips the file-backed dataset-series store (admin impl, which also covers
 * the inherited read operations), plus FR-11 series membership (which is stored
 * as {@code dcat:inSeries} on the owning Dataset).
 */
public class DatasetSeriesAdminServiceImplTest {

	private static final String BASE = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE + "dataset-series/";
	private static final String DATASET_BASE = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE + "datasets/";

	@TempDir
	Path storage;

	private DatasetAdminServiceImpl datasetService() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	private DatasetSeriesAdminServiceImpl service() {
		return new DatasetSeriesAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentSeries() {
		service().upsertDatasetSeries(series(BASE + "air", "Air quality series"));

		Optional<DatasetSeries> loaded = service().getDatasetSeries("air");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "air", loaded.get().getAbout());
		assertEquals("Air quality series", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredSeries() {
		DatasetSeriesAdminServiceImpl service = service();
		service.upsertDatasetSeries(series(BASE + "air", "Air quality series"));
		service.upsertDatasetSeries(series(BASE + "water", "Water quality series"));
		assertEquals(2, service.listDatasetSeries().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDatasetSeries("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheSeries() {
		DatasetSeriesAdminServiceImpl service = service();
		service.upsertDatasetSeries(series(BASE + "air", "Air quality series"));
		service.deleteDatasetSeries("air", false);
		assertTrue(service.getDatasetSeries("air").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DatasetSeriesAdminServiceImpl service = service();
		service.upsertDatasetSeries(series(null, "Untitled about"));
		assertEquals(1, service.listDatasetSeries().size());
	}

	// --- FR-11 series membership -------------------------------------------

	@Test
	void addDatasetToSeriesRecordsInSeriesOnDataset() {
		DatasetAdminServiceImpl datasets = datasetService();
		DatasetSeriesAdminServiceImpl series = new DatasetSeriesAdminServiceImpl(TestResourceSets.factory(), storage);
		series.upsertDatasetSeries(series(BASE + "air", "Air quality series"));

		series.addDatasetToDatasetSeries("air", dataset(DATASET_BASE + "no2", "NO2"));

		Optional<Dataset> stored = datasets.getDataset("no2");
		assertTrue(stored.isPresent());
		assertEquals(1, stored.get().getInSeries().size());
		assertEquals(BASE + "air", stored.get().getInSeries().get(0).getAbout());
	}

	@Test
	void addDatasetToSeriesIsIdempotent() {
		DatasetAdminServiceImpl datasets = datasetService();
		DatasetSeriesAdminServiceImpl series = new DatasetSeriesAdminServiceImpl(TestResourceSets.factory(), storage);
		series.upsertDatasetSeries(series(BASE + "air", "Air quality series"));

		series.addDatasetToDatasetSeries("air", dataset(DATASET_BASE + "no2", "NO2"));
		// Re-add the same membership: the dataset already carries the series.
		series.addDatasetToDatasetSeries("air", datasets.getDataset("no2").get());

		assertEquals(1, datasets.getDataset("no2").get().getInSeries().size());
	}

	@Test
	void addDatasetToUnknownSeriesThrows() {
		DatasetSeriesAdminServiceImpl series = service();
		assertThrows(NoSuchElementException.class,
				() -> series.addDatasetToDatasetSeries("missing", dataset(DATASET_BASE + "no2", "NO2")));
	}

	@Test
	void deleteDatasetFromSeriesRemovesInSeries() {
		DatasetAdminServiceImpl datasets = datasetService();
		DatasetSeriesAdminServiceImpl series = new DatasetSeriesAdminServiceImpl(TestResourceSets.factory(), storage);
		series.upsertDatasetSeries(series(BASE + "air", "Air quality series"));
		series.addDatasetToDatasetSeries("air", dataset(DATASET_BASE + "no2", "NO2"));

		series.deleteDatasetFromDatasetSeries("air", "no2");

		assertTrue(datasets.getDataset("no2").get().getInSeries().isEmpty());
	}

	@Test
	void deleteDatasetFromSeriesIgnoresUnknownDataset() {
		DatasetSeriesAdminServiceImpl series = service();
		series.upsertDatasetSeries(series(BASE + "air", "Air quality series"));
		// No dataset "no2" exists: the call is a no-op, not an error.
		series.deleteDatasetFromDatasetSeries("air", "no2");
		assertFalse(datasetService().getDataset("no2").isPresent());
	}

	private static DatasetSeries series(String about, String title) {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		if (about != null) {
			series.setAbout(about);
		}
		return TestEntities.mandatoryDataset(series, title);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		if (about != null) {
			dataset.setAbout(about);
		}
		return TestEntities.mandatoryDataset(dataset, title);
	}
}

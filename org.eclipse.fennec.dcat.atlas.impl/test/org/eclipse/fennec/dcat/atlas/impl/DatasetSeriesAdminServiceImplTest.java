package org.eclipse.fennec.dcat.atlas.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DatasetSeries;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Round-trips the file-backed dataset-series store (admin impl, which also covers
 * the inherited read operations).
 */
public class DatasetSeriesAdminServiceImplTest {

	private static final String BASE = "https://portal.example/admin/api/v1/dataset-series/";

	@TempDir
	Path storage;

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

	private static DatasetSeries series(String about, String title) {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		if (about != null) {
			series.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		series.getTitle().add(literal);
		return series;
	}
}

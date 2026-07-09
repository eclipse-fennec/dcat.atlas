package org.eclipse.fennec.dcat.atlas.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Round-trips the file-backed dataset store (admin impl, which also covers the
 * inherited read operations).
 */
public class DatasetAdminServiceImplTest {

	private static final String BASE = "https://portal.example/admin/api/v1/datasets/";

	@TempDir
	Path storage;

	private DatasetAdminServiceImpl service() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentDataset() {
		service().upsertDataset(dataset(BASE + "air", "Air quality"));

		Optional<Dataset> loaded = service().getDataset("air");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "air", loaded.get().getAbout());
		assertEquals("Air quality", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredDataset() {
		DatasetAdminServiceImpl service = service();
		service.upsertDataset(dataset(BASE + "air", "Air quality"));
		service.upsertDataset(dataset(BASE + "water", "Water quality"));
		assertEquals(2, service.listDatasets().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDataset("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDataset() {
		DatasetAdminServiceImpl service = service();
		service.upsertDataset(dataset(BASE + "air", "Air quality"));
		service.deleteDataset("air", false);
		assertTrue(service.getDataset("air").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DatasetAdminServiceImpl service = service();
		service.upsertDataset(dataset(null, "Untitled about"));
		assertEquals(1, service.listDatasets().size());
	}

	@Test
	void etagIsEmptyWhenMissing() {
		assertTrue(service().etag("does-not-exist").isEmpty());
	}

	@Test
	void etagIsStableAcrossReadsAndChangesWithContent() {
		DatasetAdminServiceImpl service = service();
		service.upsertDataset(dataset(BASE + "air", "Air quality"));
		String first = service.etag("air").orElseThrow();
		// Same stored bytes -> same validator.
		assertEquals(first, service.etag("air").orElseThrow());

		// Changed content -> different validator.
		service.upsertDataset(dataset(BASE + "air", "Air quality (v2)"));
		assertNotEquals(first, service.etag("air").orElseThrow());
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

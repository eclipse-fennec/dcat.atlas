package org.eclipse.fennec.dcat.atlas.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DcatFactory;
import dcat.Distribution;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Round-trips the file-backed distribution store (admin impl, which also covers
 * the inherited read operations). Distribution is a root-level resource keyed by
 * its own {@code rdf:about}, like the other entities.
 */
public class DistributionAdminServiceImplTest {

	private static final String BASE = "https://portal.example/admin/api/v1/distributions/";

	@TempDir
	Path storage;

	private DistributionAdminServiceImpl service() {
		return new DistributionAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentDistribution() {
		service().upsertDistribution(distribution(BASE + "csv", "CSV download"));

		Optional<Distribution> loaded = service().getDistribution("csv");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "csv", loaded.get().getAbout());
		assertEquals("CSV download", loaded.get().getTitle().getValue());
	}

	@Test
	void listReturnsEveryStoredDistribution() {
		DistributionAdminServiceImpl service = service();
		service.upsertDistribution(distribution(BASE + "csv", "CSV download"));
		service.upsertDistribution(distribution(BASE + "json", "JSON download"));
		assertEquals(2, service.listDistributions().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDistribution("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDistribution() {
		DistributionAdminServiceImpl service = service();
		service.upsertDistribution(distribution(BASE + "csv", "CSV download"));
		service.deleteDistribution("csv", false);
		assertTrue(service.getDistribution("csv").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DistributionAdminServiceImpl service = service();
		service.upsertDistribution(distribution(null, "Untitled about"));
		assertEquals(1, service.listDistributions().size());
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
}

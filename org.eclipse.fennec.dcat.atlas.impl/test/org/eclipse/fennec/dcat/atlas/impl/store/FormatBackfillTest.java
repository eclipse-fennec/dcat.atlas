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
package org.eclipse.fennec.dcat.atlas.impl.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.impl.TestGitStore;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;

import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsFactory;

/** The once-per-start completion of issue #68 over what is already stored. */
class FormatBackfillTest {

	private static final String BASE = StoreLayout.LOGICAL_BASE;
	private static final String GEOJSON = FormatDefaults.EU_FILE_TYPES + "GEOJSON";

	@TempDir
	Path storage;

	/**
	 * validateOnWrite off, as this fixture has always run: it stages content that is
	 * deliberately incomplete, which is the whole premise of a backfill.
	 */
	private DcatStoreService store() {
		return org.eclipse.fennec.dcat.atlas.impl.TestStores.at(storage, false);
	}

	private FormatBackfill backfill() {
		return new FormatBackfill(store());
	}

	@Test
	void aStoredDistributionWithoutFormatIsCompletedOnce() {
		storeWithoutFormat("air", "http://www.iana.org/assignments/media-types/application/geo+json");
		assertFalse(TestGitStore.stored(storage, StoreLayout.DATASETS, "air").contains("file-type"),
				"fixture must start without a dct:format");

		assertEquals(1, backfill().run());

		String stored = TestGitStore.stored(storage, StoreLayout.DATASETS, "air");
		assertTrue(stored.contains(GEOJSON), stored);
	}

	@Test
	void aSecondPassCommitsNothing() {
		storeWithoutFormat("air", "http://www.iana.org/assignments/media-types/application/geo+json");
		backfill().run();
		Optional<String> before = store().etag(StoreLayout.DATASETS, "air");

		assertEquals(0, backfill().run());

		// Idempotent on the history too: no commit, so the ETag does not move.
		assertEquals(before, store().etag(StoreLayout.DATASETS, "air"));
	}

	@Test
	void anUnknownMediaTypeLeavesTheStoreUntouched() {
		storeWithoutFormat("air", "http://www.iana.org/assignments/media-types/application/x-made-up");
		Optional<String> before = store().etag(StoreLayout.DATASETS, "air");

		assertEquals(0, backfill().run());

		assertEquals(before, store().etag(StoreLayout.DATASETS, "air"));
		assertFalse(TestGitStore.stored(storage, StoreLayout.DATASETS, "air").contains("file-type"));
	}

	@Test
	void anEmptyStoreIsFine() {
		assertEquals(0, backfill().run());
	}

	// --- fixture: what the store looked like before FormatDefaults existed --------

	/**
	 * Writes a dataset whose distribution carries a media type but no format, bypassing
	 * the write floor the way nothing can from outside: {@code put} would complete it, so
	 * the media type is added afterwards and staged as bookkeeping.
	 */
	private void storeWithoutFormat(String id, String mediaType) {
		DcatStoreSession session = store().open(Caller.ANONYMOUS);
		Dataset dataset = session.put(StoreLayout.DATASETS, id, dataset(id));
		dataset.getDistribution().get(0).setMediaType(mediaType);
		session.saveBookkeeping(dataset);
		session.commit("fixture: " + id + " without dct:format");
	}

	private static Dataset dataset(String id) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(BASE + StoreLayout.DATASETS + "/" + id);
		dataset.getTitle().add(literal("Air quality"));
		dataset.getDescription().add(literal("Fixture"));
		Agent publisher = FoafFactory.eINSTANCE.createAgent();
		publisher.setAbout("https://example.de/organisation/uba");
		publisher.getName().add(literal("Umweltbundesamt"));
		dataset.setPublisher(publisher);
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(dataset.getAbout() + "/distributions/geo");
		distribution.getAccessURL().add("https://example.de/geo");
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
		distribution.setLicense(license);
		dataset.getDistribution().add(distribution);
		return dataset;
	}

	private static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang("en");
		return literal;
	}
}

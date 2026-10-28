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

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.impl.TestGitStore;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
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

	private ResourceSetFactory resourceSets() {
		return org.eclipse.fennec.dcat.atlas.impl.TestResourceSets.factory();
	}

	private GitService git() {
		return TestGitStore.at(storage);
	}

	private FormatBackfill backfill() {
		return new FormatBackfill(resourceSets(), git(), TestGitStore.BASE_PATH);
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
		Optional<String> before = DcatHelper.etag(git(), TestGitStore.BASE_PATH, StoreLayout.DATASETS, "air");

		assertEquals(0, backfill().run());

		// Idempotent on the history too: no commit, so the ETag does not move.
		assertEquals(before, DcatHelper.etag(git(), TestGitStore.BASE_PATH, StoreLayout.DATASETS, "air"));
	}

	@Test
	void anUnknownMediaTypeLeavesTheStoreUntouched() {
		storeWithoutFormat("air", "http://www.iana.org/assignments/media-types/application/x-made-up");
		Optional<String> before = DcatHelper.etag(git(), TestGitStore.BASE_PATH, StoreLayout.DATASETS, "air");

		assertEquals(0, backfill().run());

		assertEquals(before, DcatHelper.etag(git(), TestGitStore.BASE_PATH, StoreLayout.DATASETS, "air"));
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
		Store store = DcatHelper.open(resourceSets(), git(), TestGitStore.BASE_PATH, false, null, Caller.ANONYMOUS);
		Dataset dataset = store.put(StoreLayout.DATASETS, id, dataset(id));
		dataset.getDistribution().get(0).setMediaType(mediaType);
		store.saveBookkeeping(dataset);
		store.commit("fixture: " + id + " without dct:format");
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

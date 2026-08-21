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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * That one API operation is one commit (persistence plan Phase 2, D5).
 * <p>
 * This is the property the whole session design exists for, and nothing else in the suite
 * asserts it: the round-trip tests would pass just as well if every resource write were its
 * own commit. What that would cost is visible here — a cascade delete would publish a state
 * in which the catalog is gone but its referrers still point at it, and the SPARQL
 * projection reads the same store.
 */
public class GitCommitBoundaryTest {

	private static final String CATALOGS = StoreLayout.logicalBase(StoreLayout.CATALOGS);
	private static final String DATASETS = StoreLayout.logicalBase(StoreLayout.DATASETS);

	@TempDir
	Path storage;

	private CatalogAdminServiceImpl catalogs() {
		return new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH);
	}

	private DatasetAdminServiceImpl datasets() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH);
	}

	@Test
	void oneUpsertIsOneCommit() {
		catalogs().upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		assertEquals(1, commits().size(), messages());
		assertTrue(commits().get(0).getFullMessage().contains("gov"), messages());
	}

	@Test
	void addingAMemberStoresBothInOneCommit() {
		catalogs().upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		// The dataset and the catalog that now links to it, together: committing them
		// separately would publish a catalog pointing at a dataset that is not there yet.
		assertEquals(2, commits().size(), messages());
		assertEquals(TestGitStore.stored(storage, StoreLayout.DATASETS, "air").isEmpty(), false);
	}

	@Test
	void aCascadeDeleteIsOneCommit() {
		catalogs().upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		catalogs().addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));
		int before = commits().size();

		datasets().deleteDataset("air", true);

		// The unlink of the catalog and the removal of the dataset are one commit, not two.
		assertEquals(before + 1, commits().size(), messages());
		assertTrue(commits().get(0).getFullMessage().contains("air"), messages());
	}

	@Test
	void aRepeatedLinkCommitsNothing() {
		catalogs().upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		catalogs().linkDatasetToCatalog("gov", "air");
		int before = commits().size();
		String etag = catalogs().etag("gov").orElseThrow();

		catalogs().linkDatasetToCatalog("gov", "air");

		// An idempotent no-op must leave no trace in the history and must not move the ETag:
		// staging nothing means there is nothing to commit, and the blob is untouched.
		assertEquals(before, commits().size(), messages());
		assertEquals(etag, catalogs().etag("gov").orElseThrow());
	}

	@Test
	void blobsLandUnderTheConfiguredBasePath() {
		catalogs().upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		assertEquals(List.of(TestGitStore.BASE_PATH + "/catalogs/gov.xmi"), TestGitStore.paths(storage));
	}

	// --- helpers ------------------------------------------------------------

	/** The branch's commits, newest first. */
	private List<RevCommit> commits() {
		GitService git = TestGitStore.at(storage);
		if (git.getFiles().getCommitId() == null) {
			return List.of();
		}
		try {
			List<RevCommit> log = new ArrayList<>();
			git.getLog().forEach(log::add);
			return log;
		} catch (Exception e) {
			throw new IllegalStateException("Could not read the log", e);
		}
	}

	/** The history as a message, so a count assertion says what actually happened. */
	private String messages() {
		return commits().stream().map(RevCommit::getShortMessage).toList().toString();
	}

	private static Catalog catalog(String about, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(about);
		// A Catalog is a Dataset in the model, so it takes the same mandatory properties.
		return TestEntities.mandatoryDataset(catalog, title);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = TestEntities.mandatoryDataset(DcatFactory.eINSTANCE.createDataset(), title);
		dataset.setAbout(about);
		return dataset;
	}
}

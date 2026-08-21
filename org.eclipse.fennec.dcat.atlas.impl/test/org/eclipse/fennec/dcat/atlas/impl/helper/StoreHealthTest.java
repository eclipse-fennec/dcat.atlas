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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.eclipse.fennec.dcat.atlas.impl.TestGitStore;
import org.eclipse.fennec.jgit.api.GitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * F-25 store readiness, over a git-backed store.
 * <p>
 * The case that matters operationally is the one that is deliberately <em>not</em> a
 * failure: a repository with no commits in it at all. That is what a brand-new deployment
 * looks like — git has no empty directories, so the collection folders do not exist until
 * the first write — and reporting it CRITICAL would mean the portal never started serving.
 * It is the direct successor of the old "store directory does not exist yet" case.
 * <p>
 * The unreadable-repository branch is not covered here. Producing one means either a stub
 * {@code GitService} — implementing an eighteen-method {@code @ProviderType} to throw from
 * one of them — or corrupting a repository on disk and hoping JGit fails the way the test
 * assumed. Neither would be testing this class; both would be testing JGit.
 */
public class StoreHealthTest {

	@TempDir
	Path root;

	@Test
	void noGitServiceIsNotReady() {
		assertFalse(StoreHealth.ready(null));
		assertTrue(StoreHealth.detail(null, TestGitStore.BASE_PATH, StoreLayout.CATALOGS).contains("no git service"),
				StoreHealth.detail(null, TestGitStore.BASE_PATH, StoreLayout.CATALOGS));
	}

	@Test
	void aRepositoryWithNoCommitsIsReady() {
		GitService git = TestGitStore.at(root);

		assertTrue(StoreHealth.ready(git));
		String detail = StoreHealth.detail(git, TestGitStore.BASE_PATH, StoreLayout.CATALOGS);
		assertTrue(detail.contains("no commits yet"), detail);
		// Names where the first write will put things, so an operator can see the layout
		// before anything exists.
		assertTrue(detail.contains(StoreLayout.collectionPrefix(TestGitStore.BASE_PATH, StoreLayout.CATALOGS)),
				detail);
	}

	@Test
	void aRepositoryWithAStoredResourceIsReadyAndCountsIt() {
		GitService git = TestGitStore.at(root);
		git.writeFile(StoreLayout.repoPath(TestGitStore.BASE_PATH, StoreLayout.CATALOGS, "gov"),
				"<catalog/>".getBytes(StandardCharsets.UTF_8), "Store catalog gov");

		assertTrue(StoreHealth.ready(git));
		String detail = StoreHealth.detail(git, TestGitStore.BASE_PATH, StoreLayout.CATALOGS);
		assertTrue(detail.contains("store readable"), detail);
		assertTrue(detail.contains("1 resource(s)"), detail);
	}

	@Test
	void aCollectionWithNothingInItIsStillReady() {
		GitService git = TestGitStore.at(root);
		git.writeFile(StoreLayout.repoPath(TestGitStore.BASE_PATH, StoreLayout.CATALOGS, "gov"),
				"<catalog/>".getBytes(StandardCharsets.UTF_8), "Store catalog gov");

		// Nothing has ever been written to the datasets collection, so its folder does not
		// exist. That is not a fault - there is nothing to create ahead of time.
		assertTrue(StoreHealth.ready(git));
		String detail = StoreHealth.detail(git, TestGitStore.BASE_PATH, StoreLayout.DATASETS);
		assertTrue(detail.contains("0 resource(s)"), detail);
	}
}

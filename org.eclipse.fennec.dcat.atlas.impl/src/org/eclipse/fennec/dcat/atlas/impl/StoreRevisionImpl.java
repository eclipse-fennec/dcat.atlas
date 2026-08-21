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

import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.store.StoreRevision;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The store's version, as the commit its branch currently points at.
 * <p>
 * A commit id is exactly the property {@link StoreRevision} asks for: it changes whenever
 * anything in the repository changes, and not otherwise. It is the one place in the codebase
 * where a commit id is the *right* answer — per resource it would be wrong, because it moves
 * for every resource whenever any one of them is written (see
 * {@code DcatHelper#etag}, which uses the blob id for that reason).
 * <p>
 * Lives here rather than in the SPARQL bundle so the projection does not have to know the
 * store is a git repository.
 */
@Component(name = "StoreRevision", service = StoreRevision.class)
public class StoreRevisionImpl implements StoreRevision {

	private final GitService gitService;

	@Activate
	public StoreRevisionImpl(@Reference(name = "gitService") GitService gitService) {
		this.gitService = gitService;
	}

	@Override
	public Optional<String> current() {
		// getFiles() also walks the tree, which is more than is needed - but it is the only
		// way the service exposes the tip, and it is a single in-memory walk of a small
		// repository against a poll measured in minutes. Revisit if that stops being true.
		return Optional.ofNullable(gitService.getFiles().getCommitId());
	}
}

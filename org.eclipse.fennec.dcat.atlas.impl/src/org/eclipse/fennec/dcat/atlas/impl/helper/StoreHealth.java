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

import org.eclipse.fennec.jgit.api.GitService;

/**
 * Readiness of one git-backed store (F-25).
 * <p>
 * Each entity type reports its own readiness, so an operator reading the response can see
 * which collection is in trouble — even though all of them are served from one repository
 * now, and so in practice succeed or fail together.
 * <p>
 * Three deliberate decisions about what counts as ready:
 * <ul>
 * <li><b>An empty repository is ready.</b> A freshly created repository has no commits and
 * therefore no collection folders — git has no empty directories, so a folder starts
 * existing when its first blob is committed. Failing readiness there would mean a new
 * deployment never starts serving.</li>
 * <li><b>A collection with nothing in it is ready.</b> Same reason: absence of content is
 * not a fault, and there is nothing to create ahead of time.</li>
 * <li><b>A repository that cannot be read at all is not ready.</b> No {@code GitService}
 * bound, or a branch that cannot be resolved, is a misconfiguration that no amount of
 * waiting fixes.</li>
 * </ul>
 * <p>
 * Writability is deliberately not probed. Finding out whether a commit would be accepted
 * means attempting one, and a health check must not write to the store it is checking; a
 * read-only replica is a legitimate deployment anyway.
 */
public final class StoreHealth {

	private StoreHealth() {
	}

	/** Whether the repository behind this store can be read. */
	public static boolean ready(GitService gitService) {
		if (gitService == null) {
			return false;
		}
		try {
			// Listing the tip is the cheapest thing that proves the repository is open and its
			// branch resolvable. An empty repository answers with an empty listing rather than
			// failing, which is exactly the "new deployment" case that must stay ready.
			gitService.getFiles();
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	/**
	 * Whether a remote's copy of the branch agrees with ours, appended to the detail text.
	 * <p>
	 * Reported rather than failed. For a remote the repository is held in the heap, so a
	 * commit that has not reached the remote survives only until the next restart — an
	 * operator needs to see that, but the portal is still serving correctly and taking it
	 * out of rotation would not help. Empty for a repository on disk, which has no remote
	 * and where a commit is durable the moment it is written.
	 */
	private static String remoteState(GitService gitService, String commitId) {
		String remoteHead = gitService.getRemoteHead();
		if (remoteHead == null || remoteHead.equals(commitId)) {
			return "";
		}
		return "; WARNING: the branch differs from the remote's copy (%s) as of the last fetch - unpushed commits are lost on restart"
				.formatted(remoteHead);
	}

	/** What was checked and what was found, for an operator reading the readiness response. */
	public static String detail(GitService gitService, String basePath, String collection) {
		if (gitService == null) {
			return "no git service bound for the store";
		}
		String where = StoreLayout.collectionPrefix(basePath, collection);
		try {
			String commitId = gitService.getFiles().getCommitId();
			if (commitId == null) {
				return "repository %s has no commits yet on %s; %s will be created by the first write"
						.formatted(gitService.getGitUrl(), gitService.getBranch(), where);
			}
			int count = gitService.getFiles(where).getFiles().size();
			return "store readable: %s at %s on %s, %d resource(s) in %s%s"
					.formatted(gitService.getGitUrl(), commitId, gitService.getBranch(), count, where,
							remoteState(gitService, commitId));
		} catch (RuntimeException e) {
			return "store not readable: %s on %s - %s"
					.formatted(gitService.getGitUrl(), gitService.getBranch(), e.getMessage());
		}
	}
}

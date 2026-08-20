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
	 * Reported rather than failed: the portal is serving correctly either way, and taking it
	 * out of rotation would not get a commit to the remote. Empty when the two agree, and
	 * empty for a repository that has no remote at all.
	 * <p>
	 * The consequence depends on which repository this is, and saying the wrong one is worse
	 * than saying nothing. A repository given as a URL is held in memory, so a commit that
	 * has not reached the remote is genuinely gone at the next restart. A repository on disk
	 * that mirrors to a remote has its commits in the volume's object database already — a
	 * divergence there means the mirror is behind, not that anything is at risk. Both were
	 * measured: with the remote stopped, a write returned 503 and the commit was still on the
	 * volume, survived a restart, and was pushed by the next successful write.
	 */
	private static String remoteState(GitService gitService, String commitId) {
		String remoteHead = gitService.getRemoteHead();
		if (remoteHead == null || remoteHead.equals(commitId)) {
			return "";
		}
		if (heldInMemory(gitService.getGitUrl())) {
			return ("; WARNING: the branch differs from the remote's copy (%s): this repository is held in memory,"
					+ " so a commit that has not reached the remote is lost on restart").formatted(remoteHead);
		}
		return ("; WARNING: the branch differs from the remote's copy (%s): pushes are not landing, so the mirror is"
				+ " behind. The commits are durable in this repository and a later push completes them")
						.formatted(remoteHead);
	}

	/**
	 * Whether the store is the in-memory mirror of a remote rather than a repository on
	 * disk — decided by the same rule the git service itself uses, that a repository named
	 * by a URL <em>is</em> its remote.
	 */
	private static boolean heldInMemory(String repo) {
		return repo != null && (repo.startsWith("git://") || repo.startsWith("git@") || repo.startsWith("ssh://")
				|| repo.startsWith("http://") || repo.startsWith("https://"));
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

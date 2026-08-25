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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.impl.URIHandlerImpl;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.impl.store.PendingChanges.State;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.jgit.exceptions.GitFileNotFoundException;

/**
 * Reads and writes stored resources as git blobs, as an ordinary EMF {@code URIHandler}.
 *
 * <h2>Why this is the seam</h2>
 *
 * The store used to map each collection's logical base onto a directory in the
 * {@code URIConverter}'s URI map, which welded a resource's identity to its location: the
 * IRI segment <em>was</em> the folder name, and the file name <em>was</em> the id. A handler
 * separates them. Cross-resource references keep working exactly as before — EMF still
 * writes {@code href="http://dcat.atlas/datasets/air#/"} — while where those bytes actually
 * live becomes this class's business, and configurable ({@link StoreLayout}).
 *
 * <h2>Why writes do not commit</h2>
 *
 * {@code createOutputStream} stages the bytes in the session's {@link PendingChanges} and
 * commits nothing. One {@code close()} must not be one commit: EMF closes a stream per
 * resource, and an operation is frequently several resources that have to land together.
 * The session decides when to commit; see {@code DcatHelper.Store#commit}.
 * <p>
 * This is the first writable git handler in the ecosystem — model.atlas's
 * {@code GitURIHandler} and its ancestor in jena-MDO both refuse to write. The
 * buffer-and-flush shape follows the non-git persistence backends instead, whose output
 * streams persist on {@code close()}.
 *
 * <h2>Reads see this session's own writes</h2>
 *
 * A staged blob is not in the repository yet, so a read that went straight to git would
 * miss a resource this very operation had just written. Every method therefore consults
 * {@link PendingChanges} first. Within one session the store behaves as though the writes
 * had already happened, which is what the rest of the code assumes.
 */
public final class DcatGitUriHandler extends URIHandlerImpl {

	private final GitService gitService;
	private final String basePath;
	private final PendingChanges pending;

	public DcatGitUriHandler(GitService gitService, String basePath, PendingChanges pending) {
		this.gitService = gitService;
		this.basePath = basePath;
		this.pending = pending;
	}

	/**
	 * Claims every URI under the logical base, including one that does not name a resource
	 * we can file.
	 * <p>
	 * Refusing a malformed one would be worse than failing on it: EMF would fall through to
	 * its default handler, which would try to fetch {@code http://dcat.atlas/...} over the
	 * network and report a connection problem for what is really a bad identity.
	 */
	@Override
	public boolean canHandle(URI uri) {
		return uri != null && uri.toString().startsWith(DcatIds.LOGICAL_BASE);
	}

	@Override
	public InputStream createInputStream(URI uri, Map<?, ?> options) throws IOException {
		String path = repoPath(uri);
		if (pending.state(path) == State.WRITTEN) {
			return new ByteArrayInputStream(pending.staged(path));
		}
		if (pending.state(path) == State.REMOVED) {
			throw new FileNotFoundException(uri.toString());
		}
		try {
			return gitService.readLatestFile(path);
		} catch (GitFileNotFoundException e) {
			// EMF distinguishes "not there" from "could not be read" by the exception type,
			// and Resource.load's own demand-creation depends on it. A git-specific
			// exception would surface as an unreadable resource instead of an absent one.
			throw (FileNotFoundException) new FileNotFoundException(uri.toString()).initCause(e);
		}
	}

	/**
	 * A stream that stages what is written to it, on {@code close()}.
	 * <p>
	 * Nothing reaches the repository here — see the class comment.
	 */
	@Override
	public OutputStream createOutputStream(URI uri, Map<?, ?> options) throws IOException {
		String path = repoPath(uri);
		return new ByteArrayOutputStream() {

			private boolean staged;

			@Override
			public void close() throws IOException {
				super.close();
				// EMF closes a save stream once, but a resource that is saved twice in one
				// session gets two streams, and some EMF paths close defensively. Staging
				// once per stream keeps a redundant close from re-staging stale bytes.
				if (!staged) {
					staged = true;
					pending.write(path, toByteArray());
				}
			}
		};
	}

	@Override
	public boolean exists(URI uri, Map<?, ?> options) {
		String path;
		try {
			path = repoPath(uri);
		} catch (IOException e) {
			// A URI under our base that names no storable resource does not exist, which is
			// the honest answer here. It only becomes an error when something tries to read
			// or write it, where the diagnosis can actually be reported.
			return false;
		}
		switch (pending.state(path)) {
		case WRITTEN:
			return true;
		case REMOVED:
			return false;
		default:
			return gitService.exists(null, path);
		}
	}

	@Override
	public void delete(URI uri, Map<?, ?> options) throws IOException {
		pending.remove(repoPath(uri));
	}

	/**
	 * The repository path backing {@code uri}.
	 *
	 * @throws IOException if the URI is under our base but does not name a resource in one
	 *                     of our collections
	 */
	String repoPath(URI uri) throws IOException {
		String tail = uri.toString().substring(DcatIds.LOGICAL_BASE.length());
		int slash = tail.indexOf('/');
		if (slash > 0) {
			String collection = tail.substring(0, slash);
			String id = tail.substring(slash + 1);
			if (StoreLayout.COLLECTIONS.contains(collection) && DcatIds.isSafeId(id)) {
				return StoreLayout.repoPath(basePath, collection, id);
			}
		}
		throw new IOException(uri + " does not name a stored resource");
	}
}

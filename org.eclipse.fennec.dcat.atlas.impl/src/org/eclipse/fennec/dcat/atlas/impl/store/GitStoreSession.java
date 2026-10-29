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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.dcat.atlas.api.store.StoreConflictException;
import org.eclipse.fennec.dcat.atlas.api.store.StoreUnavailableException;
import org.eclipse.fennec.dcat.atlas.impl.store.PendingChanges.State;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.jgit.api.TreeResult;
import org.eclipse.fennec.jgit.exceptions.GitConflictException;
import org.eclipse.fennec.jgit.exceptions.GitPushException;


/**
 * The git-backed storage: one XMI blob per resource, one commit per operation.
 * <p>
 * Persistence only. The portal's write rules live in {@link GuardedStoreSession}, which
 * wraps this — see {@link StorageSession} for what that division is for.
 *
 * @author ilenia
 * @since Oct 28, 2026
 */
public class GitStoreSession implements StorageSession {
	
	private final ResourceSet resourceSet;
	private final PendingChanges pending;
	private final GitService gitService;
	private final StoreConfig config;
	
	public GitStoreSession(ResourceSetFactory resourceSetFactory, GitService gitService, StoreConfig config) {
		this.config = config;
		this.gitService = gitService;
		pending = new PendingChanges();
		resourceSet = StoreResourceSets.create(resourceSetFactory, gitService, config.basePath(), pending);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#get(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends EObject> Optional<T> get(String collection, String id) {
		URI uri = StoreResourceSets.resourceUri(collection, id);
		if (resourceSet.getResource(uri, false) == null && !resourceSet.getURIConverter().exists(uri, null)) {
			return Optional.empty();
		}
		Resource resource = resourceSet.getResource(uri, true);
		return resource.getContents().isEmpty() ? Optional.empty()
				: Optional.of((T) resource.getContents().get(0));
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#list(java.lang.String)
	 */
	@Override
	public <T extends EObject> List<T> list(String collection) {
		List<String> ids = ids(collection);
		List<T> result = new ArrayList<>(ids.size());
		for (String id : ids) {
			this.<T>get(collection, id).ifPresent(result::add);
		}
		return result;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#ids(java.lang.String)
	 */
	@Override
	public List<String> ids(String collection) {
		String prefix = StoreLayout.collectionPrefix(config.basePath(), collection);
		Set<String> ids = new LinkedHashSet<>();
		TreeResult tree = gitService.getFiles(prefix);
		for (String path : tree.getFiles()) {
			String id = StoreLayout.idOfPath(config.basePath(), collection, path);
			// A listing is recursive and the prefix may be shared with unrelated content,
			// so anything that is not one of our blobs is not ours to read.
			if (id != null && pending.state(StoreLayout.repoPath(config.basePath(), collection, id)) != State.REMOVED) {
				ids.add(id);
			}
		}
		for (String id : stagedIds(collection)) {
			ids.add(id);
		}
		List<String> sorted = new ArrayList<>(ids);
		sorted.sort(null);
		return sorted;
	}

	@Override
	public <T extends EObject> T store(String collection, String id, T object) {
		URI uri = StoreResourceSets.resourceUri(collection, id);
		Resource resource = resourceSet.getResource(uri, false);
		if (resource == null) {
			resource = resourceSet.createResource(uri);
		}
		resource.getContents().clear();
		resource.getContents().add(object);
		save(resource);
		return object;
	}

	@Override
	public boolean exists(String collection, String id) {
		return resourceSet.getURIConverter().exists(StoreResourceSets.resourceUri(collection, id), null);
	}

	@Override
	public void stage(EObject object) {
		Resource resource = object.eResource();
		if (resource == null) {
			throw new IllegalArgumentException(
					"Cannot save a " + object.eClass().getName() + " that is not in the store");
		}
		save(resource);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#delete(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean delete(String collection, String id) {
		URI uri = StoreResourceSets.resourceUri(collection, id);
		boolean existed = resourceSet.getURIConverter().exists(uri, null);
		Resource loaded = resourceSet.getResource(uri, false);
		if (loaded != null) {
			resourceSet.getResources().remove(loaded);
		}
		pending.remove(StoreLayout.repoPath(config.basePath(), collection, id));
		return existed;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#commit(java.lang.String)
	 */
	@Override
	public Optional<String> commit(String message) {
		if (pending.isEmpty()) {
			return Optional.empty();
		}
		String commitId;
		try {
			commitId = gitService.commit(pending.toCommitRequest(message));
		} catch (GitConflictException e) {
			// Translated here rather than let out: the REST adapter renders refusals and
			// does not know the store is a git repository, the same boundary
			// ReferentialIntegrityException already draws.
			throw new StoreConflictException(
					"Could not store the change: the store moved underneath this operation (" + message + ")", e);
		} catch (GitPushException e) {
			// The commit is written and on the branch; only sending it to the remote
			// failed. Retryable, and the retry is cheap because the work is already done -
			// so it is a 503 and not a lost write. Caught after GitConflictException,
			// which is the more specific "the remote moved on" case.
			throw new StoreUnavailableException(
					"The change is stored locally but could not be sent to the remote (" + message + ")", e);
		}
		// A plain GitWriteException - the object database write failed and nothing was
		// recorded - is deliberately NOT caught. It is a server fault rather than a
		// refusal, so it should surface as an unmapped 500 with its stack trace in the
		// log; wrapping it in a mapped exception would render a tidy message and lose the
		// diagnosis, and there is nothing a client could do with the detail anyway.
		pending.clear();
		// After the commit, never before: a listener is told about writes that happened,
		// and one that throws cannot fail an operation that is already durable.
		StoreChanges.committed();
		return Optional.ofNullable(commitId);
	}

	/** The ids of {@code collection} this session has staged but not yet committed. */
	private List<String> stagedIds(String collection) {
		List<String> staged = new ArrayList<>();
		for (String path : pending.writtenPaths()) {
			String id = StoreLayout.idOfPath(config.basePath(), collection, path);
			if (id != null) {
				staged.add(id);
			}
		}
		return staged;
	}

	

	private void save(Resource resource) {
		try {
			resource.save(StoreResourceSets.saveOptions());
		} catch (IOException e) {
			throw new UncheckedIOException("Could not store " + resource.getURI(), e);
		}
	}
	
	
}

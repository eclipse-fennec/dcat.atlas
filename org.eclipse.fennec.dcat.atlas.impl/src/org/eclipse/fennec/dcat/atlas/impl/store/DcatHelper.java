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
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.store.PendingChanges.State;
import org.eclipse.fennec.dcat.atlas.impl.validation.ModelValidation;
import org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.jgit.api.TreeResult;
import org.eclipse.fennec.jgit.exceptions.GitConflictException;
import org.eclipse.fennec.jgit.exceptions.GitPushException;

import rdf.IdentifiedResource;

/**
 * Git-backed persistence for the DCAT-AP stores: one XMI blob per resource, the entity
 * itself as the blob's sole root object.
 *
 * <h2>Sessions, and why they exist</h2>
 *
 * Links between entities are EMF cross-resource references, so two entities can only be
 * linked while they are loaded in the <em>same</em> {@link ResourceSet} — that is what lets
 * EMF write {@code href="http://dcat.atlas/datasets/air#/"} rather than inlining a copy.
 * {@link #open} hands out a {@link Store} holding one such resource set; everything read
 * through it resolves against everything else.
 * <p>
 * A session is also the unit of change. Writes are staged, not committed, until
 * {@link Store#commit} is called, so one API operation becomes one commit however many
 * resources it touches — and an operation that fails part way through commits nothing at
 * all. See {@link PendingChanges}.
 * <p>
 * A store is scoped to one operation and is not thread-safe, matching EMF's own guarantees
 * — see {@code StoreResourceSets} for why that is preferred over one shared set.
 */
public final class DcatHelper {

	private DcatHelper() {
	}

	/** Opens a store session, without model validation on write. */
	public static Store open(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		return open(resourceSetFactory, gitService, basePath, false);
	}

	/**
	 * Opens a store session.
	 *
	 * @param validateOnWrite whether {@link Store#put} checks the entity against the
	 *                        model's constraints first — see {@link ModelValidation}
	 */
	public static Store open(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		return open(resourceSetFactory, gitService, basePath, validateOnWrite, null);
	}

	/**
	 * Opens a store session.
	 *
	 * @param validateOnWrite whether {@link Store#put} checks the entity against the
	 *                        model's constraints first — see {@link ModelValidation}
	 * @param validation      the SHACL validation service, or {@code null} for none; see
	 *                        {@link ShaclValidation} for why absence means no enforcement
	 */
	public static Store open(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite, DcatValidationService validation) {
		PendingChanges pending = new PendingChanges();
		ResourceSet resourceSet = StoreResourceSets.create(resourceSetFactory, gitService, basePath, pending);
		return new Store(resourceSet, gitService, basePath, pending, validateOnWrite, validation);
	}

	/** One operation's view of the store, and the one commit it will become. */
	public static final class Store {

		private final ResourceSet resourceSet;
		private final GitService gitService;
		private final String basePath;
		private final PendingChanges pending;
		private final boolean validateOnWrite;
		/** SHACL enforcement, or {@code null} when no validation service is bound. */
		private final DcatValidationService validation;

		private Store(ResourceSet resourceSet, GitService gitService, String basePath, PendingChanges pending,
				boolean validateOnWrite, DcatValidationService validation) {
			this.resourceSet = resourceSet;
			this.gitService = gitService;
			this.basePath = basePath;
			this.pending = pending;
			this.validateOnWrite = validateOnWrite;
			this.validation = validation;
		}

		/** The object stored under {@code id}, or empty if there is no such resource. */
		@SuppressWarnings("unchecked")
		public <T extends EObject> Optional<T> get(String collection, String id) {
			URI uri = StoreResourceSets.resourceUri(collection, id);
			if (resourceSet.getResource(uri, false) == null && !resourceSet.getURIConverter().exists(uri, null)) {
				return Optional.empty();
			}
			Resource resource = resourceSet.getResource(uri, true);
			return resource.getContents().isEmpty() ? Optional.empty()
					: Optional.of((T) resource.getContents().get(0));
		}

		/** Every object in {@code collection}, ordered by id. */
		public <T extends EObject> List<T> list(String collection) {
			List<String> ids = ids(collection);
			List<T> result = new ArrayList<>(ids.size());
			for (String id : ids) {
				this.<T>get(collection, id).ifPresent(result::add);
			}
			return result;
		}

		/**
		 * The ids in {@code collection}, ordered, as this session sees them — committed
		 * resources plus anything it has staged, minus anything it has removed.
		 * <p>
		 * Session-aware because the alternative is a scan that contradicts the very
		 * operation running it: {@code References} looks for referrers while a cascade is
		 * part way through unlinking them.
		 */
		public List<String> ids(String collection) {
			String prefix = StoreLayout.collectionPrefix(basePath, collection);
			Set<String> ids = new LinkedHashSet<>();
			TreeResult tree = gitService.getFiles(prefix);
			for (String path : tree.getFiles()) {
				String id = StoreLayout.idOfPath(basePath, collection, path);
				// A listing is recursive and the prefix may be shared with unrelated content,
				// so anything that is not one of our blobs is not ours to read.
				if (id != null && pending.state(StoreLayout.repoPath(basePath, collection, id)) != State.REMOVED) {
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

		/** The ids of {@code collection} this session has staged but not yet committed. */
		private List<String> stagedIds(String collection) {
			List<String> staged = new ArrayList<>();
			for (String path : pending.writtenPaths()) {
				String id = StoreLayout.idOfPath(basePath, collection, path);
				if (id != null) {
					staged.add(id);
				}
			}
			return staged;
		}

		/**
		 * Stores {@code object} under {@code id}, creating or replacing, and stamps it
		 * with its logical identity.
		 * <p>
		 * The identity is minted here rather than taken from the caller, because it is
		 * the store that decides what a resource is called. Accepting an {@code about}
		 * from the request is how the writer's hostname used to end up frozen into the
		 * file.
		 */
		public <T extends EObject> T put(String collection, String id, T object) {
			StoreLayout.requireSafeId(id);
			if (object instanceof IdentifiedResource identified) {
				identified.setAbout(StoreLayout.logicalIri(collection, id));
			}
			// After the identity is stamped — "a stored entity has an about" is one of the
			// constraints, and a SHACL report has to name the node that will actually be
			// stored — and before anything is staged, because the point is that an invalid
			// entity never reaches a commit.
			validate(object);
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

		/**
		 * Re-stages an object previously {@link #get}, checked exactly as {@link #put} checks
		 * what it stores.
		 *
		 * <h2>Why this validates, and why that was issue #34</h2>
		 *
		 * A Distribution has no file of its own — it lives in its Dataset (FR-10) — and a
		 * membership link lives on its container, so both are written by mutating an object
		 * from {@link #get} and re-staging it here rather than through {@link #put}. While
		 * this method did not validate, those two whole families of write were unchecked:
		 * {@code /health/ready} reported {@code enforceOnWrite=true}, a Distribution with no
		 * {@code accessURL} was answered {@code 201}, and {@code POST /admin/validate/…} then
		 * called the very same stored entity non-conformant. Measured, not theorised.
		 * <p>
		 * So the validating method is the one with the short name, and the caller who wants
		 * the unchecked path has to say so and be right about why — see {@link #saveRemoval}.
		 * That way round, forgetting produces a refused write rather than a silent one.
		 */
		public void save(EObject object) {
			validate(object);
			stage(object);
		}

		/**
		 * Re-stages an object whose change only <b>removed</b> content, without validating it.
		 *
		 * <h2>Removal must always be possible</h2>
		 *
		 * Validating here would build a data jail: an operator who has invalid entities in the
		 * store — written before #34 was fixed, or while {@code validateOnWrite} was off —
		 * could not delete their way out of them, because the delete would be refused by the
		 * very content it was deleting. A cascade would be worse still: {@code References}
		 * unlinks referrers to make a delete possible, and refusing that leaves the dangling
		 * reference the cascade exists to prevent.
		 * <p>
		 * It is also the honest reading of what on-write validation is for. A write that adds
		 * or replaces content has to clear the model's floor; a write that only takes content
		 * away cannot introduce a violation that was not already there, and is not the place
		 * to discover one.
		 * <p>
		 * Use it only where the change is a removal. Everything else uses {@link #save}.
		 */
		public void saveRemoval(EObject object) {
			stage(object);
		}

		/**
		 * The write-side floor, shared by {@link #put} and {@link #save} so the two cannot
		 * drift — which is how #34 happened in the first place.
		 */
		private void validate(EObject object) {
			// Refuse a link to an identity of ours that is not there, before anything is
			// written — the write-side half of the rule References.detach enforces on delete.
			//
			// It runs before validation on purpose. EMF's own validate_EveryProxyResolves
			// would also reject a dangling link, but as a generic "a proxy did not resolve"
			// — losing which member was missing, and answering 422 where the API has always
			// answered 409. The more specific diagnosis wins.
			References.requireResolvable(this, object);
			// Ungated, like requireResolvable above and unlike ModelValidation.check below.
			// Distinct identities are not a question of conformance an operator may switch off:
			// the store addresses a Distribution by its about, so two of them under one about
			// makes the second unreachable and a DELETE of that id answer 204 while leaving it
			// in place. validateOnWrite=false must still not be able to store that.
			ModelValidation.checkDistinctDistributions(object);
			// Model constraints first: they are cheap and structural, where SHACL serializes
			// the entity to RDF and unions the vocabulary graph. An entity missing its
			// publisher should not pay for a shapes run to be told so.
			if (validateOnWrite) {
				ModelValidation.check(object);
			}
			// The referenced resources come along as SHACL context — only their rdf:type —
			// so a reference-typing constraint can be answered. requireResolvable above has
			// already established that they all exist.
			ShaclValidation.check(validation, object, References.referenced(this, object));
		}

		private void stage(EObject object) {
			Resource resource = object.eResource();
			if (resource == null) {
				throw new IllegalArgumentException(
						"Cannot save a " + object.eClass().getName() + " that is not in the store");
			}
			save(resource);
		}

		/** Stages the removal of the object stored under {@code id}; returns whether it existed. */
		public boolean delete(String collection, String id) {
			URI uri = StoreResourceSets.resourceUri(collection, id);
			boolean existed = resourceSet.getURIConverter().exists(uri, null);
			Resource loaded = resourceSet.getResource(uri, false);
			if (loaded != null) {
				resourceSet.getResources().remove(loaded);
			}
			pending.remove(StoreLayout.repoPath(basePath, collection, id));
			return existed;
		}

		/**
		 * Commits everything this session has staged, as one commit.
		 * <p>
		 * A session that staged nothing commits nothing and returns empty: an idempotent
		 * no-op — re-linking a member that is already linked — must leave no trace in the
		 * history, and must not move an ETag.
		 *
		 * @param message the commit message; it is the audit trail, so it should name the
		 *                operation and the resource
		 * @return the id of the new commit, or empty if there was nothing to commit
		 */
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
			return Optional.ofNullable(commitId);
		}

		private void save(Resource resource) {
			try {
				resource.save(StoreResourceSets.saveOptions());
			} catch (IOException e) {
				throw new UncheckedIOException("Could not store " + resource.getURI(), e);
			}
		}
	}

	// --- ETag ---------------------------------------------------------------

	/**
	 * A strong entity-tag validator for the object stored under {@code id}: the id of the
	 * git blob holding it, or empty if there is no such resource.
	 * <p>
	 * The blob id is git's own content hash of exactly that resource, so it changes iff the
	 * stored state changes — which is what conditional requests need (F-16). A commit id
	 * would not do: it changes whenever <em>any</em> resource changes, so every cached
	 * representation in the estate would be invalidated by an unrelated write.
	 * <p>
	 * Note it digests the <em>stored</em> (logical) bytes, not what a client is served. Two
	 * deployments rendering different public IRIs therefore agree on the ETag, which is
	 * correct: they are serving the same resource at the same version.
	 */
	public static Optional<String> etag(GitService gitService, String basePath, String collection, String id) {
		return gitService.blobId(null, StoreLayout.repoPath(basePath, collection, id));
	}
}

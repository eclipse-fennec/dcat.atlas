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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreHealth;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;

import rdf.IdentifiedResource;

/**
 * What every entity store does the same way: read one, read all, report an ETag,
 * and report readiness.
 * <p>
 * The five stores differ only in which collection they hold and which type comes
 * back, so the shared behaviour lives here and each service supplies its
 * collection. Keeping it in one place is what lets a change to the storage
 * contract — the move to XMI, logical identities — land once rather than five
 * times in near-identical copies.
 *
 * @param <T> the entity type this store holds
 */
abstract class AbstractEntityStore<T extends EObject> implements HealthCheck {

	protected final ResourceSetFactory resourceSetFactory;
	protected final GitService gitService;
	/** The folder inside the repository the collection folders sit under; see {@link StoreConfig}. */
	protected final String basePath;
	/** Which collection this store holds; see {@link StoreLayout}. */
	protected final String collection;
	/**
	 * Whether writes through this store are checked against the model's constraints
	 * ({@link StoreConfig#validateOnWrite()}). Held here rather than in the admin
	 * subclasses because {@link #store()} is what opens the session, and a read-only
	 * service never reaches the write path where it is consulted.
	 */
	protected final boolean validateOnWrite;

	protected AbstractEntityStore(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			String collection, boolean validateOnWrite) {
		this.resourceSetFactory = resourceSetFactory;
		this.gitService = gitService;
		this.basePath = StoreLayout.requireSafeBasePath(basePath);
		this.collection = collection;
		this.validateOnWrite = validateOnWrite;
		// Nothing to prepare: git has no empty directories, so a collection folder starts
		// existing when its first blob is committed and needs no creating before that.
	}

	/** A store session; everything read through one resolves against everything else. */
	protected Store store() {
		return DcatHelper.open(resourceSetFactory, gitService, basePath, validateOnWrite, writeValidation());
	}

	/**
	 * The SHACL validation service this store enforces with, or {@code null} for none.
	 * <p>
	 * Overridden by the admin subclasses, which hold the DS reference; a read-only service
	 * never reaches the write path, so the base answers {@code null}. The reference is
	 * declared per subclass rather than here because DS binds fields on the component
	 * class, matching how {@code graphService} is already declared.
	 */
	protected DcatValidationService writeValidation() {
		return null;
	}

	protected Optional<T> getEntity(String id) {
		return store().get(collection, id);
	}

	protected List<T> listEntities() {
		return store().list(collection);
	}

	/**
	 * One page of the collection, and the total it was taken from.
	 * <p>
	 * The two halves cost very different things, which is the whole point of paging here:
	 * {@code ids} is a git tree listing and reads no blob at all, so the total is free and
	 * only the entries on this page are materialised. Listing the ids in full on every
	 * request and slicing afterwards is deliberate — a collection's ids are one tree
	 * object, while its entries are one blob each.
	 * <p>
	 * Both halves are read through <em>one</em> session, so the ids and the entities cannot
	 * disagree: a resource that is in the id list is one this session can also read.
	 */
	protected Page<T> listEntities(PageRequest page) {
		Store store = store();
		List<String> ids = store.ids(collection);
		int total = ids.size();
		int from = firstIndexAfter(ids, page.after());
		int to = Math.min(from + page.limit(), total);
		List<T> items = new ArrayList<>(to - from);
		for (String id : ids.subList(from, to)) {
			store.<T>get(collection, id).ifPresent(items::add);
		}
		String nextAfter = to < total ? ids.get(to - 1) : null;
		return new Page<>(items, nextAfter, total);
	}

	/**
	 * Where a page resuming at {@code after} starts: the first id strictly greater than it.
	 * <p>
	 * "Greater than", not "the one after the one that equals it", so a cursor whose resource
	 * has since been deleted still resumes in the right place instead of restarting the
	 * collection. {@code ids} is sorted, so this is a binary search — {@code insertionPoint}
	 * when absent, and one past the match when present.
	 */
	private static int firstIndexAfter(List<String> ids, String after) {
		if (after == null) {
			return 0;
		}
		int found = Collections.binarySearch(ids, after);
		return found >= 0 ? found + 1 : -(found + 1);
	}

	public Optional<String> etag(String id) {
		return DcatHelper.etag(gitService, basePath, collection, id);
	}

	/**
	 * The id {@code about} names in this collection, or a fresh one when it names
	 * nothing at all (D2/FR-3).
	 * <p>
	 * An {@code about} that is not ours is <em>refused</em>
	 * ({@link org.eclipse.fennec.dcat.atlas.api.identity.ForeignIdentityException}) rather than
	 * quietly replaced by a minted id: filing an entity under a segment carved out of
	 * somebody else's URL would claim a resource we do not own, and minting instead told
	 * the caller nothing. The rule is {@link DcatIds#idForWrite}, shared with the REST
	 * adapter so both doors answer the same way.
	 */
	protected String idOrMint(T entity) {
		String about = entity instanceof IdentifiedResource identified ? identified.getAbout() : null;
		return DcatIds.idForWrite(collection, about);
	}

	// --- F-25 readiness -----------------------------------------------------

	/**
	 * Reports whether this store can serve (F-25). CRITICAL rather than WARN: an
	 * unreachable repository is a misconfiguration that no retry fixes, and the
	 * portal should be taken out of rotation.
	 */
	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (StoreHealth.ready(gitService)) {
			log.info("{}", StoreHealth.detail(gitService, basePath, collection));
		} else {
			log.critical("{}", StoreHealth.detail(gitService, basePath, collection));
		}
		return new Result(log);
	}
}

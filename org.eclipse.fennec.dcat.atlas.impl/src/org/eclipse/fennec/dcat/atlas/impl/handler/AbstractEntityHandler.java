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
package org.eclipse.fennec.dcat.atlas.impl.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import rdf.IdentifiedResource;

/**
 * What every entity handler does the same way: read one, read all, and report an ETag.
 * <p>
 * Readiness is not among them any more. All five asked the same {@link DcatStoreService}
 * the same question and could not disagree, so the F-25 check lives on the store itself.
 * <p>
 * The five handlers differ only in which collection they hold and which type comes back, so
 * the shared behaviour lives here and each supplies its collection. Keeping it in one place
 * is what lets a change to the storage contract — the move to XMI, logical identities —
 * land once rather than five times in near-identical copies.
 *
 * @param <T> the entity type this handler reads
 */
abstract class AbstractEntityHandler<T extends EObject> {


	/** Which collection this store holds; see {@link StoreLayout}. */
	protected final String collection;
	private final DcatStoreService storeService;


	protected AbstractEntityHandler(DcatStoreService storeService, String collection) {
		this.storeService = storeService;
		this.collection = collection;
		// Nothing to prepare: git has no empty directories, so a collection folder starts
		// existing when its first blob is committed and needs no creating before that.
	}

	/**
	 * The ids this store holds, in the collection's own order.
	 * <p>
	 * A tree listing and nothing more: no entity is loaded, however many ids come back. That
	 * is what lets the runtime service name every resource in a large collection — the one
	 * thing it does that would otherwise be proportional to the size of the portal.
	 * <p>
	 * A store that cannot be read answers empty rather than throwing. The caller is taking an
	 * inventory, and a collection it could not see is reported as holding nothing while the
	 * health check reports why.
	 */
	public List<String> ids() {
		try {
			return store().ids(collection);
		} catch (RuntimeException unreadable) {
			return List.of();
		}
	}

	/** 
	 * A read-only store session; everything read through one resolves against everything
	 * else.
	 * <p>
	 * Reads are not restricted by who is asking, so this session is anonymous. A write
	 * opens its own with the caller that asked for it: {@link #store(Caller)}.
	 */
	protected DcatStoreSession store() {
		return storeService.open(Caller.ANONYMOUS);
	}

	/**
	 * A store session for a write, on behalf of {@code caller}.
	 *
	 * @param caller who the gateway authenticated the request as; it decides what this
	 *               session may change, and what a resource it creates is stamped with
	 */
	protected DcatStoreSession store(Caller caller) {
		return storeService.open(caller);
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
		DcatStoreSession store = store();
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

	/**
	 * The version token of the stored entity, or empty when there is none.
	 * <p>
	 * Delegated rather than read here: what a version token <em>is</em> belongs to the
	 * store that produces it — a blob id today — and a service holding the collection has
	 * nothing to add to it.
	 */
	public Optional<String> etag(String id) {
		return storeService.etag(collection, id);
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
}

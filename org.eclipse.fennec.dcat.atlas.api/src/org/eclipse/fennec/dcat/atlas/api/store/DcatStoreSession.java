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
package org.eclipse.fennec.dcat.atlas.api.store;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;

/**
 * 
 * @author ilenia
 * @since Oct 28, 2026
 */
public interface DcatStoreSession {


	/** The object stored under {@code id}, or empty if there is no such resource. */
	public <T extends EObject> Optional<T> get(String collection, String id);
	
	/** Every object in {@code collection}, ordered by id. */
	public <T extends EObject> List<T> list(String collection);
	
	/**
	 * The ids in {@code collection}, ordered, as this session sees them — committed
	 * resources plus anything it has staged, minus anything it has removed.
	 * <p>
	 * Session-aware because the alternative is a scan that contradicts the very
	 * operation running it: {@code References} looks for referrers while a cascade is
	 * part way through unlinking them.
	 */
	public List<String> ids(String collection);
	
	/**
	 * Stores {@code object} under {@code id}, creating or replacing, and stamps it
	 * with its logical identity.
	 * <p>
	 * The identity is minted here rather than taken from the caller, because it is
	 * the store that decides what a resource is called. Accepting an {@code about}
	 * from the request is how the writer's hostname used to end up frozen into the
	 * file.
	 * <p>
	 * {@code managedBy} is likewise read from what is <em>stored</em> and written back,
	 * never taken from {@code object}: believing the body would let a stranger clear the
	 * flag and then delete, or stamp a system's name on a resource to fence it off. A
	 * resource nobody manages yet is stamped with the caller — see {@link Caller#stamp()}.
	 */
	public <T extends EObject> T put(String collection, String id, T object);
	
	
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
	public void save(EObject object);
	
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
	 * <p>
	 * Skipping validation is not skipping the access check. This is how a cascade
	 * unlinks referrers, and a referrer may be a catalog some system manages: without
	 * the check, deleting one's own dataset would rewrite that catalog's membership
	 * without ever naming it.
	 */
	public void saveRemoval(EObject object);
	
	/**
	 * Re-stages a resource the server itself changed as a side effect of writing another
	 * one, without validating it — today, a catalog whose catalogue records were
	 * re-stamped because one of its members was written
	 * ({@code CatalogRecords#memberWritten}).
	 *
	 * <h2>Why it is not validated</h2>
	 *
	 * The caller wrote a dataset. Refusing that write because some <em>catalog</em> that
	 * happens to list it is missing a publisher would fail an operation over content the
	 * caller did not send and may not be able to fix — the same data jail
	 * {@link #saveRemoval} avoids, reached from the other side. What this method stages is
	 * in any case not the caller's content but a timestamp on a record the server built
	 * complete; the records' own shape is pinned by tests rather than re-checked on every
	 * write.
	 * <p>
	 * Use it only for a change the server makes on its own behalf. Everything a caller
	 * sent goes through {@link #save}.
	 * <p>
	 * It does not ask who is writing either, for the same reason: a record's timestamp
	 * mirrors a change to a member the caller was allowed to make. Refusing it would
	 * stop someone editing their own dataset merely because a managed catalog lists it.
	 */
	public void saveBookkeeping(EObject object);
	
	/**
	 * Stages the removal of the object stored under {@code id}; returns whether it
	 * existed.
	 * <p>
	 * Refused when the resource belongs to another system, checked only when there is
	 * something to delete — so deleting what is not there stays the plain {@code false}
	 * callers rely on, rather than becoming a way to probe for managed resources.
	 */
	public boolean delete(String collection, String id);
	
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
	public Optional<String> commit(String message);

}

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

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;

/**
 * What a storage backend has to do, and nothing more: read, stage, remove, commit.
 *
 * <h2>Why this is not DcatStoreSession</h2>
 *
 * The portal's write rules — who may write, what a stored resource is called, the catalogue
 * records the server maintains, and the two validation floors — are DCAT policy, not
 * storage. They used to live inside the git session, which meant a second backend would
 * have had to re-implement all four or silently skip them.
 * <p>
 * They are in {@link GuardedStoreSession} now, which wraps one of these and is what callers
 * actually get from {@code DcatStoreService#open}. A new backend implements this interface
 * and gets the rules for free.
 *
 * <h2>What an implementation still owes</h2>
 *
 * <ul>
 * <li>{@link #ids} must be session-aware: committed resources, plus what this session has
 * staged, minus what it has removed. {@code References} walks it while a cascade is part
 * way through unlinking, so a listing that contradicts the operation running it would
 * break the cascade.</li>
 * <li>{@link #commit} must be all-or-nothing and must announce itself through
 * {@code StoreChanges.committed()} once the write is durable — after the commit, never
 * before, so a listener that throws cannot fail an operation that already happened.</li>
 * <li>A session that staged nothing must commit nothing and return empty, so an idempotent
 * no-op leaves no trace in the history and does not move an ETag.</li>
 * </ul>
 */
public interface StorageSession {

	/** The object stored under {@code id}, or empty if there is no such resource. */
	<T extends EObject> Optional<T> get(String collection, String id);

	/** Every object in {@code collection}, ordered by id. */
	<T extends EObject> List<T> list(String collection);

	/** The ids in {@code collection}, ordered, as this session sees them. */
	List<String> ids(String collection);

	/** Whether something is stored under {@code id}. */
	boolean exists(String collection, String id);

	/**
	 * Stores {@code object} under {@code id}, creating or replacing, exactly as given.
	 * <p>
	 * The identity has already been stamped and the content already checked by the time
	 * this is called; an implementation persists what it is handed.
	 */
	<T extends EObject> T store(String collection, String id, T object);

	/** Re-stages an object previously {@link #get}, whose content has been mutated. */
	void stage(EObject object);

	/** Stages the removal of {@code id}; returns whether it was there to begin with. */
	boolean delete(String collection, String id);

	/**
	 * Commits everything staged, as one commit; empty when nothing was staged.
	 *
	 * @param message the commit message, which is the audit trail
	 */
	Optional<String> commit(String message);
}

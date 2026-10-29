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

import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;

/**
 * 
 * @author ilenia
 * @since Oct 28, 2026
 */
public interface DcatStoreService {
	
	/**
	 * Opens a store session.
	 * <p>
	 * There is deliberately no overload without a caller: one that defaulted to
	 * {@link Caller#ANONYMOUS} would make forgetting it produce an unmanaged resource and
	 * let a stranger's write through, silently.
	 *
	 * @param caller          who the write is from; {@link Caller#ANONYMOUS} for a
	 *                        read-only session, where it is never consulted
	 */
	DcatStoreSession open(Caller caller);

	/**
	 * The version token of the resource stored under {@code id}, or empty when there is
	 * none stored.
	 * <p>
	 * Not on a session, and not because it could not be: it is a single lookup that reads
	 * no entity, while opening a session builds a resource set and a staging area. A
	 * conditional request asks for this and nothing else, and a write asks two or three
	 * times — see the admin resources — so paying for a session each time would be most of
	 * what those requests cost.
	 */
	Optional<String> etag(String collection, String id);

	/**
	 * The version of the store as a whole: one opaque token that changes if, and only if,
	 * something stored has changed. Empty when the store has no content yet.
	 *
	 * <h2>What it is for</h2>
	 *
	 * Answering "has anything changed since I last looked?" without reading everything. The
	 * RDF projection is rebuilt on a timer as a safety net against drift; comparing this
	 * token first turns that timer from "re-read every resource every interval" into "do
	 * nothing unless something actually moved". It is also half of a collection's ETag — the
	 * other half being which page it is.
	 *
	 * <h2>Opaque on purpose</h2>
	 *
	 * Callers may only compare tokens for equality. It is not a version number, it does not
	 * order, and it is not the version of any individual resource — that is {@link #etag}, and
	 * the two must not be confused: this one changes when <em>anything</em> changes, so using
	 * it per resource would invalidate every cached representation on every unrelated write.
	 * <p>
	 * It says nothing about how it is derived. The git store answers with its current commit,
	 * which is exactly the property asked for, but a caller that knew that would be coupled to
	 * the storage technology for no benefit.
	 * <p>
	 * An empty result means "unknown", and must never be treated as equal to another empty
	 * result.
	 */
	Optional<String> revision();

	/**
	 * Whether the backing store can be read at all (F-25).
	 * <p>
	 * The question is about the store, not about one collection: a repository that cannot
	 * be opened takes every collection with it, and one that can is readable for all of
	 * them whether or not they hold anything yet.
	 */
	boolean isReady();

	/**
	 * One line on what this collection looks like in the backing store, for a health
	 * report to log.
	 * <p>
	 * A sentence rather than a structure, because the only consumer renders it. Kept
	 * free of the health-check framework's types so implementing a store does not mean
	 * depending on them.
	 */
	String describe(String collection);
}

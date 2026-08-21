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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.jgit.api.CommitRequest;

/**
 * The blob writes and deletes one store session has accumulated but not yet committed.
 *
 * <h2>Why the buffer exists</h2>
 *
 * A single API operation is often several resource writes — deleting a catalog with
 * {@code cascade} unlinks every referrer and then removes the catalog, and
 * {@code addDatasetToCatalog} stores the dataset and then links it. Committing each of
 * those separately would publish a state in which the store contradicts itself: a catalog
 * pointing at a dataset that is not there yet, or referrers still pointing at something
 * already gone. Both are externally visible, because the SPARQL projection reads the same
 * store.
 * <p>
 * So writes accumulate here and become one commit, which is also what makes the history a
 * usable audit trail: one entry per thing a caller actually asked for.
 *
 * <h2>Not thread-safe, deliberately</h2>
 *
 * One of these belongs to one session, which belongs to one operation on one thread —
 * matching the {@code ResourceSet} it shares a lifetime with, and EMF's own guarantees.
 */
public final class PendingChanges {

	/** What this session has staged for a path. */
	public enum State {
		/** New content is staged. */
		WRITTEN,
		/** A removal is staged. */
		REMOVED,
		/** Nothing is staged; the repository still decides. */
		UNTOUCHED
	}

	/**
	 * Staged changes in the order they were first made, content {@code null} for a removal.
	 * <p>
	 * Keyed by path so a later change to the same path replaces the earlier one rather than
	 * queueing behind it — writing a resource twice in one operation must commit one blob,
	 * and writing then deleting it must commit the delete.
	 */
	private final Map<String, byte[]> staged = new LinkedHashMap<>();

	/** Stages the content of the blob at {@code path}, replacing anything staged for it. */
	public void write(String path, byte[] content) {
		staged.put(path, content);
	}

	/** Stages the removal of the blob at {@code path}, replacing anything staged for it. */
	public void remove(String path) {
		staged.put(path, null);
	}

	/** What this session has staged for {@code path}. */
	public State state(String path) {
		if (!staged.containsKey(path)) {
			return State.UNTOUCHED;
		}
		return staged.get(path) == null ? State.REMOVED : State.WRITTEN;
	}

	/** The content staged for {@code path}, or {@code null} if none is. */
	public byte[] staged(String path) {
		return staged.get(path);
	}

	/**
	 * The paths this session has staged content for, in the order they were first staged.
	 * Removals are not included — they are not there to be listed.
	 */
	public Set<String> writtenPaths() {
		Set<String> paths = new LinkedHashSet<>();
		for (Map.Entry<String, byte[]> change : staged.entrySet()) {
			if (change.getValue() != null) {
				paths.add(change.getKey());
			}
		}
		return paths;
	}

	public boolean isEmpty() {
		return staged.isEmpty();
	}

	/** Forgets everything staged, so a session can be reused after it has been committed. */
	public void clear() {
		staged.clear();
	}

	/**
	 * Everything staged, as one commit.
	 *
	 * @throws IllegalStateException if nothing is staged — an empty commit is never what a
	 *                               caller meant, and {@code CommitRequest} rejects one
	 *                               anyway; callers check {@link #isEmpty()} first
	 */
	public CommitRequest toCommitRequest(String message) {
		if (staged.isEmpty()) {
			throw new IllegalStateException("Nothing to commit");
		}
		CommitRequest.Builder builder = CommitRequest.builder(message);
		for (Map.Entry<String, byte[]> change : staged.entrySet()) {
			if (change.getValue() == null) {
				builder.delete(change.getKey());
			} else {
				builder.put(change.getKey(), change.getValue());
			}
		}
		return builder.build();
	}
}

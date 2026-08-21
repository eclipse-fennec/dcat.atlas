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
package org.eclipse.fennec.dcat.atlas.api.read;

import java.util.List;

/**
 * One page of a collection, and what a caller needs to ask for the next one.
 *
 * @param <T>       the entity type
 * @param items     the entries, in the collection's id order
 * @param nextAfter the cursor for the following page, or {@code null} when this page
 *                  is the last one. Callers should test this rather than compare
 *                  {@code items.size()} against the requested limit: a full page that
 *                  happens to end at the last entry is indistinguishable that way.
 * @param total     how many entries the whole collection holds. Cheap to know — the
 *                  ids come from a tree listing, so counting them costs no blob reads
 *                  — and it is what lets a client show progress rather than only
 *                  "there is more".
 */
public record Page<T>(List<T> items, String nextAfter, int total) {

	public Page {
		items = List.copyOf(items);
	}

	/** Whether a following page exists. */
	public boolean hasMore() {
		return nextAfter != null;
	}

	/** An empty page of an empty collection. */
	public static <T> Page<T> empty() {
		return new Page<>(List.of(), null, 0);
	}
}

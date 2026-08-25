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

/**
 * One page's worth of request: where to resume, and how many entries to return.
 *
 * <h2>Keyset, not offset</h2>
 *
 * {@code after} is the last id of the previous page, not a numeric offset. Both cost
 * the same here — a collection is a sorted list of ids read from a git tree, so
 * resuming means a binary search rather than a skip — but they behave differently
 * when the collection changes between two pages: an offset shifts, so a resource
 * inserted or deleted <em>before</em> the cursor makes the next page skip an entry or
 * serve one twice. Resuming from an id cannot do that, because the id does not move
 * when its neighbours change.
 *
 * <p>
 * The id in {@code after} need not exist. It is a position in a sorted list, so a
 * cursor whose resource has since been deleted still resumes at the right place, which
 * is what a client walking pages while another writes needs.
 *
 * @param after the id the previous page ended at, or {@code null} for the first page
 * @param limit how many entries at most, always between 1 and {@link #MAX_LIMIT}
 */
public record PageRequest(String after, int limit) {

	/** Applied when a caller asks for a collection without saying how much of it. */
	public static final int DEFAULT_LIMIT = 50;

	/**
	 * The most a single response will carry, whatever was asked for. A cap rather than a
	 * rejection: the point of this ceiling is that no request can make the server
	 * serialise the whole store, and refusing the request would not serve the caller any
	 * better than serving them the first {@value #MAX_LIMIT}.
	 */
	public static final int MAX_LIMIT = 500;

	public PageRequest {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be at least 1, but was " + limit);
		}
		if (limit > MAX_LIMIT) {
			throw new IllegalArgumentException("limit must be at most " + MAX_LIMIT + ", but was " + limit);
		}
	}

	/** The first page, at the default size. */
	public static PageRequest first() {
		return new PageRequest(null, DEFAULT_LIMIT);
	}

	/**
	 * A request from whatever a client sent, clamped into range. A blank {@code after} is
	 * the first page, an absent {@code limit} is the default, and one outside the allowed
	 * range is corrected rather than refused — a caller asking for 100000 entries wants as
	 * many as they can have, and refusing would serve them no better than
	 * {@value #MAX_LIMIT}. A {@code limit} that is not a number at all never arrives here:
	 * the conversion fails first, and {@code QueryParamExceptionMapper} renders that as a
	 * 400.
	 */
	public static PageRequest of(String after, Integer limit) {
		String cursor = after == null || after.isBlank() ? null : after;
		int size = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
		return new PageRequest(cursor, size);
	}

}

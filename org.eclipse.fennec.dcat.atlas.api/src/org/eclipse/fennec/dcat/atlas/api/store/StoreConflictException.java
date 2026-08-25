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

import org.eclipse.fennec.dcat.atlas.api.integrity.ReferentialIntegrityException;

/**
 * Thrown when a write could not be stored because the underlying store moved underneath it.
 * The REST layer renders this as {@code 409 Conflict}.
 *
 * <h2>What it means</h2>
 *
 * Something else wrote to the same branch between the point this operation read what it was
 * changing and the point it tried to store the result. Nothing was written: the store
 * refuses rather than overwriting, so the caller's view of the world is stale but the
 * stored state is intact.
 *
 * <h2>Why it is not retried automatically</h2>
 *
 * The portal is a single writer by design (persistence plan Phase 2, D8). A conflict
 * therefore does not mean "two requests raced" — it means something outside the portal is
 * committing to its branch. Re-applying this operation on top of that unknown other change
 * would be the clobbering the plan lists as a non-goal, not a recovery. An operator needs
 * to look.
 *
 * <h2>Why the store's own vocabulary, not git's</h2>
 *
 * The REST layer maps refusals to status codes; it does not know, and should not learn,
 * that the store is a git repository. This is the same reason
 * {@link ReferentialIntegrityException} exists rather than an EMF exception reaching the
 * adapter.
 */
public class StoreConflictException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StoreConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}

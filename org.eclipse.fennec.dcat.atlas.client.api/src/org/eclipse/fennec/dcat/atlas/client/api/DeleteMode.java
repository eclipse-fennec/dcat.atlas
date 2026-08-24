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
package org.eclipse.fennec.dcat.atlas.client.api;

/**
 * Whether a delete may rewrite other resources to make itself possible.
 *
 * <h2>Why a mode and not a boolean</h2>
 *
 * {@code delete(collection, id, true)} does not say at the call site what the
 * {@code true} means, and the operation is destructive enough that it should. A mode
 * also leaves room for a future dry-run — "tell me what a cascade would unlink" —
 * without changing the signature again.
 */
public enum DeleteMode {

	/**
	 * Delete only if nothing references the resource; otherwise the portal answers
	 * {@code 409} and nothing is removed.
	 */
	SINGLE,

	/**
	 * Unlink every referrer first, then delete. The portal does this as a single
	 * commit, and reports which resources it rewrote — see
	 * {@code DcatAtlasClient#delete}.
	 */
	CASCADE
}

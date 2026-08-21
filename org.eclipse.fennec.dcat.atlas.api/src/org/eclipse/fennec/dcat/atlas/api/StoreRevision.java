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
package org.eclipse.fennec.dcat.atlas.api;

import java.util.Optional;

/**
 * The version of the store as a whole: one opaque token that changes if, and only if,
 * something stored has changed.
 *
 * <h2>What it is for</h2>
 *
 * Answering "has anything changed since I last looked?" without reading everything. The RDF
 * projection is rebuilt from the stores on a timer as a safety net against drift; comparing
 * this token first turns that timer from "re-read every resource every interval" into "do
 * nothing unless something actually moved".
 *
 * <h2>Opaque on purpose</h2>
 *
 * Callers may only compare tokens for equality. It is not a version number, it does not
 * order, and it is not the version of any individual resource — that is what an
 * {@code etag(id)} is for, and the two must not be confused: this one changes when
 * <em>anything</em> changes, so using it per resource would invalidate every cached
 * representation on every unrelated write.
 * <p>
 * Deliberately says nothing about how it is derived. It happens to be the store's current
 * commit, but a consumer that knew that would be coupled to the storage technology for no
 * benefit — the same reason the REST layer maps {@link StoreConflictException} rather than a
 * git exception.
 */
public interface StoreRevision {

	/**
	 * The current token, or empty when the store has no content yet — a repository that has
	 * never been written to has no version to report, and an empty result must therefore be
	 * treated as "unknown", never as equal to another empty result.
	 */
	Optional<String> current();
}

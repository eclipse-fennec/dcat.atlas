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
package org.eclipse.fennec.dcat.atlas.api.integrity;

/**
 * A write refused because it would leave a stored link resolving to nothing.
 * <p>
 * The invariant has two sides and one consequence, so they share a type: deleting a
 * resource something points at ({@link ResourceInUseException}) and storing a pointer
 * to a resource that is not there ({@link DanglingReferenceException}) both end in an
 * unresolved proxy whose {@code about} is {@code null}, which makes serializing the
 * <em>referrer</em> to RDF throw — one bad write surfacing later as a 500 on an
 * unrelated resource.
 * <p>
 * The REST layer renders the whole family as {@code 409 Conflict}: the request is
 * well-formed and the caller is understood, it just conflicts with what is stored.
 */
public abstract class ReferentialIntegrityException extends IllegalStateException {

	private static final long serialVersionUID = 1L;

	protected ReferentialIntegrityException(String message) {
		super(message);
	}
}

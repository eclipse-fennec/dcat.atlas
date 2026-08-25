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

import java.util.List;

/**
 * Thrown when a write would store a reference to an identity of ours that does not
 * exist. The REST layer renders this as {@code 409 Conflict}.
 * <p>
 * This is {@link ResourceInUseException}'s mirror image: that one refuses to
 * <em>delete</em> a resource something still points at, this one refuses to
 * <em>create</em> a pointer to something that is not there. Both protect the same
 * invariant — no stored link resolves to nothing — and both would otherwise surface
 * as the same delayed failure, an unresolved proxy whose {@code about} is
 * {@code null}, which makes serializing the <em>referrer</em> to RDF throw.
 * <p>
 * Only identities under our own logical base are checked. A reference to somebody
 * else's IRI (a publisher, a licence, a vocabulary concept) is not ours to resolve
 * or to refuse.
 */
public class DanglingReferenceException extends ReferentialIntegrityException {

	private static final long serialVersionUID = 1L;

	private final transient List<String> missing;

	/**
	 * @param missing the identities the write referenced that are not in the store;
	 *                reported so a caller can act rather than guess
	 */
	public DanglingReferenceException(String message, List<String> missing) {
		super(message);
		this.missing = List.copyOf(missing);
	}

	/** The referenced identities that do not exist. */
	public List<String> getMissing() {
		return missing;
	}
}

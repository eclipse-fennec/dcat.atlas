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
 * Thrown when a resource cannot be deleted because something still references it
 * (FR-1). The REST layer renders this as {@code 409 Conflict}.
 * <p>
 * Deleting anyway would leave the referrer pointing at nothing. EMF tolerates that
 * quietly — the link loads as an unresolved proxy — but the resource then has no
 * {@code rdf:about}, and serializing the <em>referrer</em> to RDF fails. So a
 * delete that would strand a reference is refused unless the caller asks for a
 * cascade, which unlinks first.
 */
public class ResourceInUseException extends ReferentialIntegrityException {

	private static final long serialVersionUID = 1L;

	private final transient List<String> referencedBy;

	/**
	 * @param referencedBy identities of the resources still pointing at the target;
	 *                     reported so a caller can act rather than guess
	 */
	public ResourceInUseException(String message, List<String> referencedBy) {
		super(message);
		this.referencedBy = List.copyOf(referencedBy);
	}

	/** The resources still referencing the one that could not be deleted. */
	public List<String> getReferencedBy() {
		return referencedBy;
	}
}

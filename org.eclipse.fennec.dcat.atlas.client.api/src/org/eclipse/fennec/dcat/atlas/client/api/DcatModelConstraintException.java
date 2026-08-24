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

import java.util.List;

/**
 * {@code 422} — the model's own OCL constraints refused the write.
 * <p>
 * Distinct from {@link DcatShaclException} because the two say different things: OCL
 * runs at the write boundary on the EMF object graph and catches what the model
 * itself forbids, while SHACL checks the resulting RDF against the DCAT-AP.de
 * profile. A caller fixing one is not fixing the other.
 *
 * <h2>How the two are told apart</h2>
 *
 * Both are {@code 422}. The discriminator is the {@code X-SHACL-Conforms} response
 * header, which only the SHACL branch sets — not the body's media type, since the
 * SHACL branch can also answer {@code text/plain}. Sniffing the body would get this
 * wrong in exactly that case.
 * <p>
 * The violations arrive as plain text, one per line, so unlike a SHACL report there
 * is nothing to parse and {@link #getViolations()} can hand back real strings.
 */
public class DcatModelConstraintException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	private final List<String> violations;

	/**
	 * @param message    a short description naming the operation that was refused
	 * @param violations the violation lines as received; may be empty, never {@code null}
	 */
	public DcatModelConstraintException(String message, List<String> violations) {
		super(message);
		this.violations = violations == null ? List.of() : List.copyOf(violations);
	}

	/**
	 * @return the constraint violations, one per line as the portal reported them
	 */
	public List<String> getViolations() {
		return violations;
	}
}

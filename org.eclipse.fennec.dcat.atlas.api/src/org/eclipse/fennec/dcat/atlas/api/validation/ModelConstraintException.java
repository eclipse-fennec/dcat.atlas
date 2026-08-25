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
package org.eclipse.fennec.dcat.atlas.api.validation;

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.identity.ForeignIdentityException;

/**
 * A write supplied an entity that violates the model's own constraints — the OCL
 * invariants annotated on the ecore, or a multiplicity the ecore declares.
 *
 * <h2>Why this exists next to SHACL</h2>
 *
 * SHACL enforcement (FR-4) is <em>operator-configured</em>: no shapes ship with this
 * repository, and an empty shapes set conforms to everything, so a deployment that has
 * not been given shapes validates nothing. These constraints travel <em>in the model</em>
 * and are therefore always on. They are the floor; SHACL is the profile check layered
 * above it, and the two overlap deliberately.
 *
 * <h2>Thrown by the store, not only by REST</h2>
 *
 * Raised at the persistence boundary ({@code DcatHelper.Store.put}) so it holds for every
 * caller — an importer, a migration script, another bundle — and not merely for requests
 * that arrive over HTTP. Same rule as {@link ForeignIdentityException}, and the same
 * reason: the REST adapter is one door among several.
 * <p>
 * An {@link IllegalArgumentException} because it is exactly that: the argument is not a
 * storable entity. The REST layer maps it to {@code 422 Unprocessable Content} through
 * {@code ModelConstraintExceptionMapper} — the same status on-write SHACL enforcement
 * already answers with, because the failure is the same kind: a well-formed request
 * carrying metadata that does not conform.
 */
public class ModelConstraintException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	private final transient List<String> violations;

	public ModelConstraintException(String message, List<String> violations) {
		super(message);
		this.violations = violations == null ? List.of() : List.copyOf(violations);
	}

	/**
	 * One line per violated constraint, in the order the EMF {@code Diagnostician}
	 * reported them. Never {@code null}.
	 */
	public List<String> getViolations() {
		return violations;
	}
}

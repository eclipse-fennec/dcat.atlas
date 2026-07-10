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

import java.util.List;

/**
 * The outcome of validating an entity against the DCAT-AP.de SHACL shapes (FR-4/FR-5).
 * <p>
 * {@code violations} is the structured, JSON-friendly view; {@code reportTurtle} is
 * the native {@code sh:ValidationReport} serialized as Turtle, so a REST layer can
 * return the report as RDF (FR-19) without the API exposing any Jena type.
 *
 * @deprecated Superseded by returning the native Jena {@code ValidationReport} from
 *             {@link DcatValidationService#validate}, which serializes to every RDF
 *             syntax without this lossy projection. Retained only as a fallback.
 */
@Deprecated
public record ValidationResult(boolean conforms, List<Violation> violations, String reportTurtle) {

	public ValidationResult {
		violations = violations == null ? List.of() : List.copyOf(violations);
	}

	/**
	 * Whether the entity has at least one hard violation ({@code sh:Violation}, a
	 * DCAT-AP.de "MUSS"). This — not {@link #conforms()} — is what gates a write (FR-4):
	 * {@code conforms()} is {@code false} for any report entry, including mere
	 * {@code sh:Warning} recommendations ("SOLL"), which must not block a write.
	 */
	public boolean hasBlockingViolations() {
		return violations.stream().anyMatch(Violation::isViolation);
	}
}

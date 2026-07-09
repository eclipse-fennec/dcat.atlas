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
 */
public record ValidationResult(boolean conforms, List<Violation> violations, String reportTurtle) {

	public ValidationResult {
		violations = violations == null ? List.of() : List.copyOf(violations);
	}
}

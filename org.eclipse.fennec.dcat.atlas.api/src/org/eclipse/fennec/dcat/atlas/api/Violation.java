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

/**
 * A single SHACL constraint violation, flattened to strings so the API stays
 * free of any SHACL/Jena types (F-16/FR-4). Mirrors a {@code sh:ValidationResult}:
 * the focus node that failed, the property path (if any), the human-readable
 * message, the severity IRI, and the shape that raised it.
 *
 * @deprecated Part of the deprecated {@link ValidationResult} projection; the native
 *             Jena {@code ValidationReport} now carries the results directly.
 */
@Deprecated
public record Violation(String focusNode, String path, String message, String severity, String sourceShape) {

	/** The SHACL severity IRI for a hard violation (DCAT-AP.de "MUSS"). */
	public static final String SH_VIOLATION = "http://www.w3.org/ns/shacl#Violation";

	/**
	 * Whether this is a hard violation ({@code sh:Violation}, i.e. a DCAT-AP.de "MUSS"),
	 * as opposed to a {@code sh:Warning}/{@code sh:Info} ("SOLL"/recommendation). Only
	 * hard violations block a write (FR-4); recommendations are reported but do not reject.
	 */
	public boolean isViolation() {
		return SH_VIOLATION.equals(severity);
	}
}

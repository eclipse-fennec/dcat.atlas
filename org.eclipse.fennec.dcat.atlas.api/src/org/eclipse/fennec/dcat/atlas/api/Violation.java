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
 */
public record Violation(String focusNode, String path, String message, String severity, String sourceShape) {
}

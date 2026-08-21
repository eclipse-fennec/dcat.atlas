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

import org.apache.jena.shacl.ValidationReport;
import org.eclipse.emf.ecore.EObject;

/**
 * Validates a DCAT-AP.de entity against the SHACL shapes (FR-4/FR-5, product F-21).
 * <p>
 * The entity is serialized to RDF and checked against the configured shape graph; the
 * native Jena {@link ValidationReport} is returned so the REST layer can serialize the
 * full, spec-compliant {@code sh:ValidationReport} in any RDF syntax (FR-19) with no
 * loss of fidelity. This deliberately couples the API to Jena, and that is the trade
 * that was made: the earlier Jena-free {@code ValidationResult}/{@code Violation} wrappers
 * kept the API clean and could only carry a lossy projection of the report, so they were
 * removed rather than carried. The shape files themselves are loaded from an external,
 * operator-configured location — they are not bundled with the app.
 */
public interface DcatValidationService {

	/** Validates {@code entity} against the loaded shapes. Never {@code null}. */
	ValidationReport validate(EObject entity);

	/**
	 * Whether the admin write path must enforce validation (FR-4): reject a
	 * non-conformant entity with {@code 422} before persisting it. When {@code false},
	 * validation is offered only as the explicit dry run (FR-5) and writes are never
	 * blocked. This is operator policy, co-located with the shapes configuration; the
	 * decision to act on it belongs to the caller (the REST write path), not this
	 * service.
	 */
	boolean isWriteEnforced();
}

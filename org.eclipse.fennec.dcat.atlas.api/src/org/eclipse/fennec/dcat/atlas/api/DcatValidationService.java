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

import org.eclipse.emf.ecore.EObject;

/**
 * Validates a DCAT-AP.de entity against the SHACL shapes (FR-4/FR-5, product F-21).
 * <p>
 * The entity is serialized to RDF and checked against the configured shape graph;
 * the {@link ValidationResult} reports conformance plus the violations. The shape
 * files themselves are loaded from an external, operator-configured location — they
 * are not bundled with the application.
 */
public interface DcatValidationService {

	/** Validates {@code entity} against the loaded shapes. Never {@code null}. */
	ValidationResult validate(EObject entity);
}

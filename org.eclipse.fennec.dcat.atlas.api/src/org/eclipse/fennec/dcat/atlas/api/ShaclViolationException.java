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

/**
 * A write was refused because the entity has a hard SHACL violation (FR-4).
 *
 * <h2>Why it carries the native report</h2>
 *
 * The report is the whole diagnostic: which node failed, on which property path, against
 * which shape, at which severity. Flattening it to strings here would repeat the mistake
 * {@link ValidationResult} was deprecated for. Carrying Jena's own type costs nothing —
 * {@link DcatValidationService#validate(org.eclipse.emf.ecore.EObject)} already returns it,
 * so this API is coupled to Jena either way — and lets the REST layer serialize the full
 * {@code sh:ValidationReport} in any RDF syntax the client negotiates (FR-19), which is the
 * one part of on-write enforcement that genuinely belongs to the REST adapter.
 *
 * <h2>Thrown by the store, not only by REST</h2>
 *
 * On-write enforcement used to live entirely in the admin REST resources, which meant a
 * caller reaching {@code upsertDataset} directly — an importer, a migration script, another
 * bundle — wrote unvalidated while the same body over HTTP was refused. That is the
 * asymmetry {@link ForeignIdentityException} documents for identity, with the same fix:
 * the rule belongs to the persistence boundary, so it holds for every caller.
 * <p>
 * Whether enforcement happens at all remains operator policy
 * ({@link DcatValidationService#isWriteEnforced()}); what changed is only <em>who</em> is
 * covered once it is switched on.
 */
public class ShaclViolationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final transient ValidationReport report;

	public ShaclViolationException(String message, ValidationReport report) {
		super(message);
		this.report = report;
	}

	/** The full {@code sh:ValidationReport} for the refused entity. May be {@code null} after deserialization. */
	public ValidationReport getReport() {
		return report;
	}
}

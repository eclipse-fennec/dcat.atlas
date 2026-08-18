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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.Severity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ShaclViolationException;

import rdf.IdentifiedResource;

/**
 * On-write SHACL enforcement (FR-4), at the persistence boundary.
 *
 * <h2>Why here rather than in the REST adapter</h2>
 *
 * This check used to live in the five admin REST resources, so it covered HTTP callers and
 * nobody else: the same entity handed straight to {@code upsertDataset} was stored
 * unvalidated. The admin services are OSGi services with other possible callers, so the
 * rule belongs beside the write — the same conclusion {@code DcatIds.idForWrite} reached
 * for identity, and what both planning documents already specified.
 * <p>
 * What stays in REST is turning the refusal into a response: content negotiation and
 * serializing the {@code sh:ValidationReport} are genuinely the adapter's job
 * ({@code ShaclViolationExceptionMapper}).
 *
 * <h2>What blocks</h2>
 *
 * Only {@code sh:Violation} severity — DCAT-AP.de <em>MUSS</em>. A {@code sh:Warning}
 * (<em>SOLL</em>, including most F-22 controlled-vocabulary findings) is reported by the
 * dry run but must never reject a write. A missing severity defaults to
 * {@code sh:Violation}, matching SHACL's own default.
 *
 * <h2>Absent service means no enforcement — deliberately</h2>
 *
 * Unlike {@link ModelValidation}, this fails <em>open</em>: with no validation service
 * bound, or with enforcement switched off, the write proceeds. That is because SHACL
 * enforcement is operator policy over operator-supplied shapes, not a property of the
 * model. An operator who wants the strict reading raises the DS reference's minimum
 * cardinality — {@code validationService.cardinality.minimum=1} — which makes the admin
 * service itself unsatisfiable without a validation service, so no write can slip past
 * while the shapes are unavailable. {@code DcatAdminWriteHealthCheck} is what explains the
 * resulting {@code 404}s.
 */
public final class ShaclValidation {

	private ShaclValidation() {
	}

	/**
	 * Throws {@link ShaclViolationException} if {@code entity} has a hard violation.
	 * <p>
	 * Must run on the entity in the exact form it will be stored — i.e. after its
	 * {@code about} has been stamped — because the report's {@code sh:focusNode} is that
	 * IRI, and a report naming a node that is not the stored one is not actionable.
	 */
	public static void check(DcatValidationService validation, EObject entity) {
		if (validation == null || !validation.isWriteEnforced()) {
			return;
		}
		ValidationReport report = validation.validate(entity);
		if (report.getEntries().stream().noneMatch(ShaclValidation::isViolation)) {
			return;
		}
		throw new ShaclViolationException(summary(entity, report), report);
	}

	/** A hard violation (DCAT-AP.de "MUSS"); a null severity defaults to {@code sh:Violation}. */
	private static boolean isViolation(ReportEntry entry) {
		return entry.severity() == null || Severity.Violation.equals(entry.severity());
	}

	private static String summary(EObject entity, ValidationReport report) {
		long violations = report.getEntries().stream().filter(ShaclValidation::isViolation).count();
		String subject = entity instanceof IdentifiedResource identified && identified.getAbout() != null
				? entity.eClass().getName() + " " + identified.getAbout()
				: entity.eClass().getName();
		return subject + " has " + violations + (violations == 1 ? " SHACL violation" : " SHACL violations");
	}
}

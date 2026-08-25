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
package org.eclipse.fennec.dcat.atlas.rest.helper;

import java.util.List;

import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.Severity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

/**
 * On-write SHACL enforcement for the admin write path (FR-4).
 * <p>
 * When enforcement is configured ({@link DcatValidationService#isWriteEnforced()}),
 * a create/replace validates the submitted entity against the DCAT-AP.de shapes
 * <em>before</em> it is persisted and rejects it with {@code 422 Unprocessable Entity}
 * when the report contains a hard violation, carrying the native
 * {@code sh:ValidationReport} (serialized by {@link ValidationReportMessageBodyWriter} in
 * whichever RDF syntax the client accepts, FR-19) plus {@code X-SHACL-Conforms: false}.
 * <p>
 * Only {@code sh:Violation}-severity results (DCAT-AP.de "MUSS") block; {@code sh:Warning}
 * recommendations (F-22 "SOLL" checks such as the license vocabulary) are reported by the
 * dry run but never reject a write.
 * <p>
 * When enforcement is off — or no shapes are configured, or the validation service is
 * momentarily unbound (e.g. mid-reconfigure) or absent from the runtime — this is a no-op
 * and writes proceed unchecked. Validate the entity in the exact form it will be stored
 * (i.e. after the admin resource has stamped its public {@code about} URI).
 */
public class WriteValidation {

	/** 422 has no constant in {@link Response.Status}. */
	public static final int UNPROCESSABLE_ENTITY = 422;

	/** RDF syntaxes the report can be returned in (no plain JSON); Turtle is the default. */
	public static final MediaType[] REPORT_TYPES = { MediaType.valueOf("text/turtle"),
			MediaType.valueOf("application/ld+json"), MediaType.valueOf("application/rdf+xml"),
			MediaType.valueOf("text/n3"), MediaType.valueOf("application/n-triples") };


	/**
	 * @param acceptable the request's {@code Accept} media types, used to pick the report
	 *                   syntax for the 422 body (defaults to Turtle)
	 * @return a response builder to return as-is (422 with the SHACL report) when
	 *         enforcement is on and {@code entity} has a hard violation; {@code null} when
	 *         the caller should proceed with the write.
	 */
	public static ResponseBuilder enforce(DcatValidationService validation, EObject entity, List<MediaType> acceptable) {
		if (validation == null || !validation.isWriteEnforced()) {
			return null;
		}
		ValidationReport report = validation.validate(entity);
		boolean blocking = report.getEntries().stream().anyMatch(WriteValidation::isViolation);
		if (!blocking) {
			return null;
		}
		return Response.status(UNPROCESSABLE_ENTITY) //
				.entity(report) //
				.type(reportType(acceptable)) //
				.header("X-SHACL-Conforms", "false");
	}

	/** A hard violation (DCAT-AP.de "MUSS"); a null severity defaults to {@code sh:Violation}. */
	public static boolean isViolation(ReportEntry entry) {
		return entry.severity() == null || Severity.Violation.equals(entry.severity());
	}

	/** First accepted RDF report syntax, or Turtle when the client accepts none of them / any. */
	public static MediaType reportType(List<MediaType> acceptable) {
		if (acceptable != null) {
			for (MediaType accept : acceptable) {
				for (MediaType candidate : REPORT_TYPES) {
					if (accept.isCompatible(candidate)) {
						return candidate;
					}
				}
			}
		}
		return REPORT_TYPES[0];
	}
}

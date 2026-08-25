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
package org.eclipse.fennec.dcat.atlas.rest.filter;

import org.eclipse.fennec.dcat.atlas.api.validation.ShaclViolationException;
import org.eclipse.fennec.dcat.atlas.rest.helper.WriteValidation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders {@link ShaclViolationException} as {@code 422 Unprocessable Content} carrying the
 * native {@code sh:ValidationReport} (FR-4/FR-19).
 *
 * <h2>Why the enforcement moved but the rendering did not</h2>
 *
 * Deciding to refuse a write is the persistence boundary's job — it holds for every caller
 * of the admin services, not only for HTTP requests, which is why
 * {@code ShaclValidation.check} lives beside the write. Turning that refusal into a
 * response is the adapter's: negotiating the RDF syntax from {@code Accept} and streaming
 * the report through {@code ValidationReportMessageBodyWriter}. This mapper is the seam
 * between the two, and it produces the same response the admin resources used to build
 * themselves — same status, same negotiated report, same {@code X-SHACL-Conforms} header.
 * <p>
 * Mapped on the specific type rather than a supertype, following
 * {@code ForeignIdentityExceptionMapper}: a blanket mapping would relabel unrelated faults
 * as the caller's problem.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatShaclViolationExceptionMapper")
public class ShaclViolationExceptionMapper implements ExceptionMapper<ShaclViolationException> {

	@Context
	HttpHeaders headers;

	@Override
	public Response toResponse(ShaclViolationException exception) {
		if (exception.getReport() == null) {
			return Response.status(WriteValidation.UNPROCESSABLE_ENTITY) //
					.entity(exception.getMessage()) //
					.type(MediaType.TEXT_PLAIN) //
					.header("X-SHACL-Conforms", "false") //
					.build();
		}
		return Response.status(WriteValidation.UNPROCESSABLE_ENTITY) //
				.entity(exception.getReport()) //
				.type(WriteValidation.reportType(headers == null ? null : headers.getAcceptableMediaTypes())) //
				.header("X-SHACL-Conforms", "false") //
				.build();
	}
}

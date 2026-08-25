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

import org.eclipse.fennec.dcat.atlas.api.validation.ModelConstraintException;
import org.eclipse.fennec.dcat.atlas.rest.helper.WriteValidation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders {@link ModelConstraintException} as {@code 422 Unprocessable Content}.
 * <p>
 * The same status on-write SHACL enforcement answers with ({@link WriteValidation}), whose
 * constant this reuses — 422 has none in {@link jakarta.ws.rs.core.Response.Status},
 * and for the same reason: the request itself is well-formed — it parsed, its media type
 * was right, its identity was ours — but the metadata it carries is not storable. A
 * {@code 400} would tell the client its request was malformed, which is the wrong repair
 * instruction.
 * <p>
 * Mapped on the specific type rather than on {@link IllegalArgumentException}, following
 * {@code ForeignIdentityExceptionMapper}: a blanket mapping would relabel every argument
 * bug in the stack as the caller's fault and hide real faults behind a 4xx.
 * <p>
 * The body is the violation list, one per line, because that is what the caller has to act
 * on — "the required feature 'publisher' must be set" names the field to fix, where the
 * exception's own summary only counts them.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatModelConstraintExceptionMapper")
public class ModelConstraintExceptionMapper implements ExceptionMapper<ModelConstraintException> {

	@Override
	public Response toResponse(ModelConstraintException exception) {
		String body = exception.getViolations().isEmpty() //
				? exception.getMessage() //
				: String.join(System.lineSeparator(), exception.getViolations());
		return Response.status(WriteValidation.UNPROCESSABLE_ENTITY) //
				.entity(body) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

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

import org.eclipse.fennec.dcat.atlas.api.ForeignIdentityException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders {@link ForeignIdentityException} as {@code 400 Bad Request} (FR-19).
 * <p>
 * Mostly a backstop. The admin resources resolve identity through
 * {@code CreateIdentity}/{@code ReplaceIdentity} before touching a service, and those
 * already return a {@code 400} of their own, so a request that reaches here is one taking
 * a path that does not — a service method called from somewhere the identity was not
 * pre-resolved. Without this it would surface as a {@code 500}, blaming the server for
 * what is the caller's malformed {@code about}.
 * <p>
 * Mapped on the specific type rather than on {@link IllegalArgumentException}, for the
 * same reason {@code ReferentialIntegrityExceptionMapper} is mapped on its own supertype:
 * a blanket mapping would turn every argument bug in the stack into a {@code 400} and hide
 * real faults behind a status code that tells the client to go and fix its request.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatForeignIdentityExceptionMapper")
public class ForeignIdentityExceptionMapper implements ExceptionMapper<ForeignIdentityException> {

	@Override
	public Response toResponse(ForeignIdentityException exception) {
		return Response.status(Status.BAD_REQUEST) //
				.entity(exception.getMessage()) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

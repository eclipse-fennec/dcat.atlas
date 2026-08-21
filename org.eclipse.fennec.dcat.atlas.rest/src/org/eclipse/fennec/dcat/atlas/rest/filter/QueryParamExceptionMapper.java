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

import org.glassfish.jersey.server.ParamException.QueryParamException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders a query parameter that will not convert as {@code 400 Bad Request}.
 *
 * <h2>What it is fixing</h2>
 *
 * A {@code @QueryParam} whose declared type cannot be parsed from what arrived — say
 * {@code ?limit=abc} for an {@code Integer} — raises this before the resource method is
 * ever entered, and JAX-RS prescribes <b>404 Not Found</b> for that (§3.2). The
 * collection then reports that it does not exist, which sends a client looking for a
 * missing resource when the resource is there and the query string is what is wrong.
 * Measured on {@code GET /catalogs?limit=abc}, which answered 404 before this existed.
 *
 * <p>
 * A path parameter that will not convert is a different case and is deliberately left
 * alone: there, "not found" is the right answer, because the unparseable text was the
 * identity of the thing being asked for. Hence the mapping is on
 * {@link QueryParamException} and not on its {@code ParamException} supertype.
 *
 * <h2>The one implementation dependency in this bundle</h2>
 *
 * {@code ParamException} is Jersey's, not the specification's: JAX-RS fixes the status
 * for this failure but not the exception type, so there is nothing portable to map. The
 * alternatives were worse — a pre-matching {@code ContainerRequestFilter} validating
 * numeric parameters by name, which puts the check nowhere near the parameter, or
 * declaring such parameters as {@code String} and parsing them by hand in every resource,
 * which is the same coupling in more places and loses the type. If the whiteboard is ever
 * something other than Jersey, this class is the single thing to replace.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatQueryParamExceptionMapper")
public class QueryParamExceptionMapper implements ExceptionMapper<QueryParamException> {

	@Override
	public Response toResponse(QueryParamException exception) {
		// The parameter name and the value that would not convert; the cause carries the
		// conversion failure itself, which is the part that says what was expected.
		String detail = exception.getCause() == null ? "" : ": " + exception.getCause().getMessage();
		return Response.status(Status.BAD_REQUEST) //
				.entity("query parameter '" + exception.getParameterName() + "' is not valid" + detail) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

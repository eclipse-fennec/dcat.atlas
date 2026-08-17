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

import org.eclipse.fennec.dcat.atlas.api.ReferentialIntegrityException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders a refused write as {@code 409 Conflict} rather than letting it escape as a
 * {@code 500}.
 * <p>
 * Both halves of the referential-integrity rule reach here: a delete that something
 * still points at, and a write that would point at something absent. The distinction a
 * client needs is refusal versus crash — a 500 says "we broke", a 409 says "your request
 * conflicts with what is stored, and here is what is in the way" — so the message, which
 * names the identities involved, is returned as the body.
 * <p>
 * Mapped on the shared supertype rather than on {@code IllegalStateException}: the latter
 * would quietly turn every unrelated illegal-state bug into a 409 and hide real faults.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatReferentialIntegrityExceptionMapper")
public class ReferentialIntegrityExceptionMapper implements ExceptionMapper<ReferentialIntegrityException> {

	@Override
	public Response toResponse(ReferentialIntegrityException exception) {
		return Response.status(Status.CONFLICT) //
				.entity(exception.getMessage()) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

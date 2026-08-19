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

import org.eclipse.fennec.dcat.atlas.api.StoreUnavailableException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders a write the store could not accept as {@code 503 Service Unavailable} rather than letting it
 * escape as a {@code 500}.
 * <p>
 * The store decides; this only renders, which is why it maps a store exception and not a
 * git one — see {@code StoreUnavailableException} for what it means and what a caller should do.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatStoreUnavailableExceptionMapper")
public class StoreUnavailableExceptionMapper implements ExceptionMapper<StoreUnavailableException> {

	@Override
	public Response toResponse(StoreUnavailableException exception) {
		return Response.status(Status.SERVICE_UNAVAILABLE) //
				.entity(exception.getMessage()) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

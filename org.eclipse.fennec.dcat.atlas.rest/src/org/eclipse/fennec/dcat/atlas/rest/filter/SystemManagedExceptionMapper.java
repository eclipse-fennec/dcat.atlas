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

import org.eclipse.fennec.dcat.atlas.api.access.SystemManagedException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Renders a write to another system's resource as {@code 403 Forbidden}.
 * <p>
 * Not {@code 401}: the gateway has already authenticated the caller and no credential
 * would change the answer. Not the {@code 409} the integrity refusals use either: a
 * conflict could succeed once the stored state changed, and this cannot. The message names
 * the managing system, which is the one thing a client can act on.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatSystemManagedExceptionMapper")
public class SystemManagedExceptionMapper implements ExceptionMapper<SystemManagedException> {

	@Override
	public Response toResponse(SystemManagedException exception) {
		return Response.status(Status.FORBIDDEN) //
				.entity(exception.getMessage()) //
				.type(MediaType.TEXT_PLAIN) //
				.build();
	}
}

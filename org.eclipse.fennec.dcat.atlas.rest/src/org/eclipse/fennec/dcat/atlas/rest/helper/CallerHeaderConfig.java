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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/** Which request header the gateway puts the authenticated client into. */
@ObjectClassDefinition(name = "DCAT.Atlas caller header", description = "The request header the upstream "
		+ "policy enforcement point (APISix/Keycloak) writes the authenticated client id into. It decides "
		+ "which generated resources a request may change, so the gateway must also strip any copy of it "
		+ "the client sent.")
public @interface CallerHeaderConfig {

	/**
	 * The header name. Configurable because it is the gateway's choice, and the two have
	 * to agree: a name that does not match what APISix sets makes every request anonymous.
	 */
	@AttributeDefinition(name = "Caller header", description = "Request header carrying the authenticated "
			+ "client id, as the gateway writes it.")
	String caller_header() default "X-Atlas-Client";
}

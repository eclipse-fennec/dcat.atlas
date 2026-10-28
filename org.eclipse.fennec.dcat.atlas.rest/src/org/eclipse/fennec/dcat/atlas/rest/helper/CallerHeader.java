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

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

import jakarta.ws.rs.core.HttpHeaders;

/**
 * Reads the caller off the request, from the header the gateway sets. A component rather
 * than a constant because the name belongs to the deployment, and one copy of it keeps
 * the five admin resources in step.
 * <p>
 * The configuration is required, not defaulted into existence: the header name has to
 * match what the gateway writes, so a deployment states it. Without it the component is
 * absent and the admin resources do not register at all, which is louder than a portal
 * that serves and quietly calls everybody anonymous.
 * <p>
 * It trusts that header completely — the portal authenticates nobody — so the gateway has
 * to overwrite it on every request it forwards and the portal must not be reachable around
 * it. A header that is absent or blank is {@link Caller#ANONYMOUS}, so a misconfigured
 * gateway does not open the portal up; it makes everyone anonymous, which locks the
 * managing systems out of their own resources and surfaces on the first sync.
 */
@Component(name = "DcatCallerHeader", service = CallerHeader.class,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = CallerHeaderConfig.class)
public class CallerHeader {

	private final String header;

	@Activate
	public CallerHeader(CallerHeaderConfig config) {
		this.header = config.caller_header();
	}

	/** The caller this request carries. */
	public Caller of(HttpHeaders headers) {
		return headers == null ? Caller.ANONYMOUS : Caller.of(headers.getHeaderString(header));
	}

	/** The configured header name, for diagnostics. */
	public String name() {
		return header;
	}
}

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
package org.eclipse.fennec.dcat.atlas.http;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "DCAT.Atlas HTTP connector",
		description = "An additional Jetty connector whose URI rules differ from the runtime's")
public @interface HttpConnectorConfig {

	/**
	 * The port the extra connector listens on. It must not be the port the Felix HTTP
	 * runtime already binds: this connector is added beside that one, not instead of it.
	 */
	@AttributeDefinition(name = "Port", description = "TCP port for this connector")
	int port();

	/**
	 * Defaults to loopback rather than every interface. The whole point of this connector
	 * is that it accepts URLs the main one refuses, so reaching it should be a deliberate
	 * act of deployment — a gateway or a container network — and not the default.
	 */
	@AttributeDefinition(name = "Host", description = "Interface to bind; loopback unless a deployment widens it")
	String host() default "localhost";

	/**
	 * A Jetty {@code UriCompliance} specification: a base mode, then the violations to
	 * permit. Parsed with {@code UriCompliance.from}, which accepts this comma form —
	 * unlike the {@code org.eclipse.jetty.UriComplianceMode} property of the Felix HTTP
	 * runtime, which goes through {@code UriCompliance.valueOf} and so takes only the six
	 * mode names, the narrowest of which permits eight violations rather than one.
	 * <p>
	 * The default allows exactly the one an OData entity key needs: its key is the
	 * resource IRI, so the canonical single-entity URL carries a percent-encoded
	 * {@code '/'} inside a path segment.
	 */
	@AttributeDefinition(name = "URI compliance",
			description = "Jetty UriCompliance spec: a base mode followed by the violations to allow")
	String uriCompliance() default "DEFAULT,AMBIGUOUS_PATH_SEPARATOR";

	/**
	 * Names the connector in Jetty's logs and dumps, so an operator reading them can tell
	 * which connector accepted a request that the other would have refused.
	 */
	@AttributeDefinition(name = "Connector name", description = "Name for this connector in Jetty logs and dumps")
	String name() default "dcatLenient";

	/**
	 * Mirrors what the Felix HTTP runtime does with its own connector: the server version
	 * is not something a deployment should have to remember to turn off.
	 */
	@AttributeDefinition(name = "Send server version", description = "Whether to send the Server response header")
	boolean sendServerVersion() default false;
}

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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.apache.felix.http.jetty.ConnectorFactory;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;

/**
 * A second Jetty connector whose URI rules are looser than the runtime's, by exactly one
 * violation.
 *
 * <h2>Why this exists</h2>
 *
 * An OData entity key is the resource IRI, so the canonical URL for a single entity is
 * {@code Dataset('http%3A%2F%2F…%2Fdatasets%2Fair')} — a percent-encoded {@code '/'}
 * inside a path segment. Jetty rejects that at the connector, before any servlet sees it,
 * with {@code 400 Ambiguous URI path separator}. Everything downstream copes: the OData
 * resource-path grammar treats a quoted key as one token, and nothing re-decodes the path,
 * so the entity resolves once the request is let through.
 *
 * <p>
 * The Felix HTTP runtime can be told to relax this, but only through
 * {@code org.eclipse.jetty.UriComplianceMode}, which it resolves with
 * {@code UriCompliance.valueOf} — that accepts the six mode names and returns {@code null}
 * for anything else, silently. The narrowest name permitting an encoded separator is
 * {@code LEGACY}, which permits seven others besides, for every context on the runtime.
 * Measured on this stack, that turns requests the connector used to refuse cleanly into
 * {@code 500}s inside the JAX-RS layer.
 *
 * <p>
 * So this takes the other route Felix offers. A {@link ConnectorFactory} service is asked
 * for a connector and the runtime adds it to its {@link Server}; the connector carries its
 * own {@link HttpConfiguration}, and the compliance spec goes through
 * {@link UriCompliance#from}, which does accept "a base mode plus these violations". The
 * result is one violation allowed instead of eight, on one port.
 *
 * <h2>The second gate</h2>
 *
 * Letting the URI past the connector is not enough. The ee10 servlet layer checks it again:
 * a request whose URI carries violations is wrapped in {@code ServletApiRequest.AmbiguousURI},
 * which throws from {@code getPathInfo()} and {@code getServletPath()} — the two methods the
 * OData servlet routes on — unless {@link ServletHandler#setDecodeAmbiguousURIs(boolean)} is
 * set. The Felix runtime sets it for the three named modes {@code LEGACY}, {@code UNSAFE} and
 * {@code UNAMBIGUOUS} and for nothing else, which is why {@code LEGACY} works end to end and a
 * connector configured by hand does not. So this does the same thing Felix does, on the same
 * beans.
 *
 * <p>
 * That switch is per servlet context and reaches every context on the server, so unlike the
 * compliance above it cannot be confined to one port. It does not need to be: it is consulted
 * only for a URI that already carries violations, and the runtime's own connector rejects
 * those before any servlet is reached. The connector is the gate; this only stops the servlet
 * layer refusing a second time what a connector was configured to allow.
 *
 * <h2>What this does NOT isolate</h2>
 *
 * Jetty serves every context on every connector. A request arriving here reaches
 * {@code /rest} and {@code /rest/sparql} as readily as {@code /odata}, under these looser
 * rules. The isolation is therefore the port, not the path: whatever fronts this runtime
 * must route only the OData paths here, and the host defaults to loopback so that reaching
 * it at all is a deliberate act of deployment.
 */
@Component(name = "DcatHttpConnector", service = ConnectorFactory.class,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = HttpConnectorConfig.class, factory = true)
public class LenientUriConnectorFactory implements ConnectorFactory {

	private final int port;
	private final String host;
	private final String name;
	private final boolean sendServerVersion;
	private final UriCompliance compliance;

	/**
	 * The spec is resolved here rather than per connector so that a typo fails the
	 * component instead of producing a connector that quietly enforces something else —
	 * which is the failure mode of the Felix property this works around.
	 */
	@Activate
	public LenientUriConnectorFactory(HttpConnectorConfig config) {
		this.port = config.port();
		this.host = config.host();
		this.name = config.name();
		this.sendServerVersion = config.sendServerVersion();
		this.compliance = UriCompliance.from(config.uriCompliance());
		if (compliance == null) {
			throw new IllegalArgumentException(
					"not a UriCompliance specification: '" + config.uriCompliance() + "'");
		}
	}

	@Override
	public Connector createConnector(Server server) {
		allowAmbiguousUris(server);
		HttpConfiguration configuration = new HttpConfiguration();
		configuration.setUriCompliance(compliance);
		configuration.setSendServerVersion(sendServerVersion);
		ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(configuration));
		connector.setPort(port);
		connector.setHost(host);
		connector.setName(name);
		return connector;
	}

	/**
	 * Lets the servlet layer serve a URI this connector accepted, for every context the
	 * server holds — see "The second gate". Contexts registered after this runs are not
	 * covered, which is the same limitation the runtime's own handling of {@code LEGACY}
	 * has; it is logged rather than hidden, because the symptom otherwise is a {@code 400}
	 * that looks exactly like the one this component exists to remove.
	 */
	private void allowAmbiguousUris(Server server) {
		var handlers = server.getContainedBeans(ServletHandler.class);
		handlers.forEach(handler -> handler.setDecodeAmbiguousURIs(true));
		LOGGER.log(Level.INFO, () -> "connector '" + name + "' allows " + compliance.getAllowed()
				+ "; ambiguous URIs enabled on " + handlers.size()
				+ " servlet handler(s) - a context registered later will refuse them");
	}

	private static final Logger LOGGER = System.getLogger(LenientUriConnectorFactory.class.getName());
}

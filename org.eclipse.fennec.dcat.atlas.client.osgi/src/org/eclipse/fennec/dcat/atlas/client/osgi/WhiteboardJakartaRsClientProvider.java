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
package org.eclipse.fennec.dcat.atlas.client.osgi;

import java.util.Objects;

import org.eclipse.fennec.dcat.atlas.client.impl.spi.DefaultJakartaRsClientProvider;

import jakarta.ws.rs.client.ClientBuilder;

/**
 * Builds the client from the Whiteboard's {@link ClientBuilder} rather than from
 * {@code ClientBuilder.newBuilder()}, so the runtime's own HTTP client, registered providers
 * and framework configuration apply.
 * <p>
 * Only the builder-creation seam is overridden; the timeout and authentication wiring is
 * inherited. That is the whole reason the plain-Java bundle exports this one class.
 */
final class WhiteboardJakartaRsClientProvider extends DefaultJakartaRsClientProvider {

	private final ClientBuilder clientBuilder;

	WhiteboardJakartaRsClientProvider(ClientBuilder clientBuilder) {
		this.clientBuilder = Objects.requireNonNull(clientBuilder, "clientBuilder");
	}

	@Override
	protected ClientBuilder newClientBuilder() {
		return clientBuilder;
	}
}

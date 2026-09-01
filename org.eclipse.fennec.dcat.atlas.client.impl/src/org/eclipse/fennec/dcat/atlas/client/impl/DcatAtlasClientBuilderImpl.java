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
package org.eclipse.fennec.dcat.atlas.client.impl;

import java.net.URI;
import java.util.Objects;

import org.eclipse.fennec.dcat.atlas.client.api.ClientConfiguration;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.JakartaRsClientProvider;
import org.eclipse.fennec.dcat.atlas.client.impl.spi.DefaultJakartaRsClientProvider;

/**
 * Assembles a plain-Java client.
 * <p>
 * The convenience setters refine a {@link ClientConfiguration} rather than holding their
 * own fields, so there is one place where a default lives and no way for the two to
 * disagree.
 */
final class DcatAtlasClientBuilderImpl implements DcatAtlasClient.Builder {

	private ClientConfiguration.Builder configuration = ClientConfiguration.builder();
	private JakartaRsClientProvider clientProvider;

	@Override
	public DcatAtlasClient.Builder configuration(ClientConfiguration configuration) {
		this.configuration = ClientConfiguration.builder(Objects.requireNonNull(configuration, "configuration"));
		return this;
	}

	@Override
	public DcatAtlasClient.Builder baseUri(URI baseUri) {
		configuration.baseUri(Objects.requireNonNull(baseUri, "baseUri"));
		return this;
	}

	@Override
	public DcatAtlasClient.Builder publicBaseUri(URI publicBaseUri) {
		configuration.publicBaseUri(publicBaseUri);
		return this;
	}

	@Override
	public DcatAtlasClient.Builder connectTimeoutMs(int connectTimeoutMs) {
		configuration.connectTimeoutMs(connectTimeoutMs);
		return this;
	}

	@Override
	public DcatAtlasClient.Builder readTimeoutMs(int readTimeoutMs) {
		configuration.readTimeoutMs(readTimeoutMs);
		return this;
	}

	@Override
	public DcatAtlasClient.Builder clientProvider(JakartaRsClientProvider clientProvider) {
		this.clientProvider = clientProvider;
		return this;
	}

	@Override
	public DcatAtlasClient build() {
		ClientConfiguration effective = configuration.build();
		JakartaRsClientProvider provider = clientProvider == null ? new DefaultJakartaRsClientProvider()
				: clientProvider;
		return new DcatAtlasClientImpl(effective, provider.newClient(effective));
	}
}

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
package org.eclipse.fennec.dcat.atlas.client.impl.spi;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.client.api.ClientConfiguration;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientException;
import org.eclipse.fennec.dcat.atlas.client.api.JakartaRsClientProvider;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * The default, plain-Java {@link JakartaRsClientProvider}: a client from
 * {@code ClientBuilder.newBuilder()} with the configured timeouts and authentication.
 * <p>
 * This is the <em>only</em> place in the client that calls
 * {@link ClientBuilder#newBuilder()}. The OSGi front-end subclasses it and overrides
 * {@link #newClientBuilder()} to take the Whiteboard's builder instead, which is why this
 * one class is the bundle's only export.
 *
 * <h2>Authentication the portal does not check yet</h2>
 *
 * A credential is attached if configured, even though the portal has no authentication
 * today and will ignore it. That is deliberate: a consumer configured for bearer tokens
 * from the start needs no change when a policy enforcement point appears in front of the
 * portal. What this class will not do is fail because a credential is missing — it logs
 * and proceeds unauthenticated, since against today's portal that works and a hard failure
 * would be a self-inflicted outage.
 */
public class DefaultJakartaRsClientProvider implements JakartaRsClientProvider {

	private static final Logger logger = Logger.getLogger(DefaultJakartaRsClientProvider.class.getName());

	@Override
	public Client newClient(ClientConfiguration configuration) {
		Objects.requireNonNull(configuration, "configuration");
		ClientBuilder builder = newClientBuilder();
		builder.connectTimeout(configuration.getConnectTimeoutMs(), TimeUnit.MILLISECONDS);
		builder.readTimeout(configuration.getReadTimeoutMs(), TimeUnit.MILLISECONDS);
		applyAuth(builder, configuration);
		return builder.build();
	}

	/**
	 * The builder to configure. Overridden by the OSGi front-end to return the
	 * Whiteboard's.
	 *
	 * @return a fresh {@link ClientBuilder}
	 */
	protected ClientBuilder newClientBuilder() {
		return ClientBuilder.newBuilder();
	}

	/** Applies whatever {@code auth.type} asks for. */
	protected void applyAuth(ClientBuilder builder, ClientConfiguration configuration) {
		switch (configuration.getAuthType()) {
		case BEARER -> applyHeader(builder, configuration, HttpHeaders.AUTHORIZATION, "Bearer ",
				configuration.getAuthTokenEnv());
		case API_KEY -> applyHeader(builder, configuration, configuration.getApiKeyHeader(), "",
				configuration.getApiKeyEnv());
		case MTLS -> applyMutualTls(builder, configuration);
		case NONE -> {
			// nothing to do — and the only mode the portal understands today
		}
		}
	}

	private void applyHeader(ClientBuilder builder, ClientConfiguration configuration, String header, String prefix,
			String environmentVariable) {
		String credential = environmentVariable == null ? null : System.getenv(environmentVariable);
		if (credential == null || credential.isBlank()) {
			logger.log(Level.WARNING,
					() -> "auth.type=" + configuration.getAuthType() + " but no credential resolved from env var '"
							+ environmentVariable + "'; requests will be unauthenticated");
			return;
		}
		builder.register(new StaticHeaderFilter(header, prefix + credential));
	}

	private void applyMutualTls(ClientBuilder builder, ClientConfiguration configuration) {
		KeyStore keyStore = load(configuration.getKeystorePath(), configuration.getKeystorePassword(),
				configuration.getKeystoreType(), "keystore");
		KeyStore trustStore = load(configuration.getTruststorePath(), configuration.getTruststorePassword(),
				configuration.getTruststoreType(), "truststore");
		if (keyStore != null) {
			builder.keyStore(keyStore,
					configuration.getKeystorePassword() == null ? "" : configuration.getKeystorePassword());
		}
		if (trustStore != null) {
			builder.trustStore(trustStore);
		}
	}

	private KeyStore load(String path, String password, String type, String what) {
		if (path == null || path.isBlank()) {
			return null;
		}
		try (InputStream in = Files.newInputStream(Path.of(path))) {
			KeyStore store = KeyStore.getInstance(type);
			store.load(in, password == null ? null : password.toCharArray());
			return store;
		} catch (Exception e) {
			// Unlike a missing bearer token this cannot be shrugged off: mTLS was asked for
			// explicitly, and proceeding without it would connect in a way the operator did
			// not configure.
			throw new DcatAtlasClientException("Could not load the " + what + " at " + path, e);
		}
	}

	/** Adds one fixed header to every request. */
	private static final class StaticHeaderFilter implements ClientRequestFilter {

		private final String header;
		private final String value;

		StaticHeaderFilter(String header, String value) {
			this.header = header;
			this.value = value;
		}

		@Override
		public void filter(ClientRequestContext requestContext) {
			requestContext.getHeaders().putSingle(header, value);
		}
	}
}

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
package org.eclipse.fennec.dcat.atlas.client.api;

import java.net.URI;
import java.util.Objects;

/**
 * Immutable configuration shared by the plain-Java and the OSGi client.
 * <p>
 * Assembled through {@link #builder()} in plain Java; the OSGi front-end will populate
 * the same fields from ConfigAdmin (PID
 * {@code org.eclipse.fennec.dcat.atlas.client}), one configuration per portal. The
 * dotted property name each field will carry is given in its accessor javadoc, so the
 * two never drift.
 * <p>
 * Kept to what this client actually uses. {@code model.atlas}'s configuration of the
 * same name is much larger because that client caches EPackages, detects drift and
 * resolves lazily; a registration client has no state to cache, so those fields would
 * be dead here.
 */
public final class ClientConfiguration {

	/** Default key/trust store type when not configured. */
	public static final String DEFAULT_STORE_TYPE = "PKCS12";

	/**
	 * The media type this client reads and writes DCAT entities in.
	 *
	 * <h2>Never {@code application/xml}</h2>
	 *
	 * The portal selects its codec by media type, and {@code application/xml} is a
	 * <em>different</em> codec — the plain-XML encoding of the same object graph. Sending
	 * it to an admin endpoint is a {@code 415}. This one constant is why that mistake
	 * cannot be made per call site.
	 */
	public static final String XMI = "application/xmi";

	private final URI baseUri;
	private final URI publicBaseUri;
	private final int connectTimeoutMs;
	private final int readTimeoutMs;
	private final String readAcceptMediaType;
	private final AuthType authType;
	private final String authTokenEnv;
	private final String apiKeyHeader;
	private final String apiKeyEnv;
	private final String keystorePath;
	private final String keystorePassword;
	private final String keystoreType;
	private final String truststorePath;
	private final String truststorePassword;
	private final String truststoreType;

	private ClientConfiguration(Builder builder) {
		this.baseUri = builder.baseUri;
		this.publicBaseUri = builder.publicBaseUri;
		this.connectTimeoutMs = builder.connectTimeoutMs;
		this.readTimeoutMs = builder.readTimeoutMs;
		this.readAcceptMediaType = builder.readAcceptMediaType;
		this.authType = builder.authType;
		this.authTokenEnv = builder.authTokenEnv;
		this.apiKeyHeader = builder.apiKeyHeader;
		this.apiKeyEnv = builder.apiKeyEnv;
		this.keystorePath = builder.keystorePath;
		this.keystorePassword = builder.keystorePassword;
		this.keystoreType = builder.keystoreType;
		this.truststorePath = builder.truststorePath;
		this.truststorePassword = builder.truststorePassword;
		this.truststoreType = builder.truststoreType;
	}

	/** A new builder with every property at its default; {@code base.uri} is required. */
	public static Builder builder() {
		return new Builder();
	}

	/** A builder pre-populated from an existing configuration. */
	public static Builder builder(ClientConfiguration from) {
		return new Builder(from);
	}

	/**
	 * {@code base.uri} — required. The portal's REST base, the URL this client actually
	 * reaches the portal on, e.g. {@code http://localhost:8085/dcat/rest/}. A trailing
	 * slash is optional; the client normalises it.
	 * <p>
	 * Purely a <b>transport address</b>: every request is targeted at it and
	 * {@code /health/ready} is probed relative to it, so behind a reverse proxy this is the
	 * internal one. It says nothing about the identities the portal serves — those come from
	 * {@link #getPublicBaseUri()}.
	 */
	public URI getBaseUri() {
		return baseUri;
	}

	/**
	 * {@code public.base.uri} — the base the portal serves its identities under, and what
	 * {@link DcatAtlasClient#aboutFor(DcatCollection, String)} computes from. Optional;
	 * {@code null} means "the same as {@code base.uri}".
	 *
	 * <h2>Why this is a second setting rather than derived from the first</h2>
	 *
	 * {@code base.uri} has to be an address this runtime can connect to, and the portal
	 * stamps identities from its own {@code PUBLIC_BASE_URL}. In a direct deployment those
	 * are one URL and this can be left unset. Behind a reverse proxy they are two, and one
	 * value cannot be both — deriving the identity base from the connect base produced IRIs
	 * under an internal hostname, which the portal refuses {@code 400} on a write and, worse,
	 * which a publisher that merely <em>recorded</em> what it published stored with no error
	 * at all (issue #42).
	 *
	 * <h2>After a write there is a better source, needing no configuration</h2>
	 *
	 * A registration response carries the stored entity with its {@code about} already
	 * rendered under the portal's public base, so {@code registration.entity().getAbout()} is
	 * right whatever this is set to. This setting is for the window before the first write,
	 * and for a caller that wants to send {@code about} itself.
	 *
	 * @return the configured public base, or {@code null} when it is {@code base.uri}
	 */
	public URI getPublicBaseUri() {
		return publicBaseUri;
	}

	/** {@code connect.timeout.ms} — default {@code 5000}. */
	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	/** {@code read.timeout.ms} — default {@code 30000}. */
	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	/**
	 * {@code read.accept} — what a read asks for; default {@link #XMI}.
	 * <p>
	 * Only XMI comes back as EMF objects, so the typed read methods require it. A caller
	 * that wants Turtle or JSON-LD is asking for bytes rather than objects, which is a
	 * different operation and not what this setting changes — it exists so a deployment
	 * can pin the negotiation rather than rely on the server's preference.
	 */
	public String getReadAcceptMediaType() {
		return readAcceptMediaType;
	}

	/** {@code auth.type} — default {@link AuthType#NONE}, which is all the portal understands today. */
	public AuthType getAuthType() {
		return authType;
	}

	/** {@code auth.token.env} — environment variable holding the bearer token; may be {@code null}. */
	public String getAuthTokenEnv() {
		return authTokenEnv;
	}

	/** {@code auth.apikey.header} — header name for {@link AuthType#API_KEY}; default {@code X-API-Key}. */
	public String getApiKeyHeader() {
		return apiKeyHeader;
	}

	/** {@code auth.apikey.env} — environment variable holding the API key; may be {@code null}. */
	public String getApiKeyEnv() {
		return apiKeyEnv;
	}

	/** {@code auth.keystore.path} — client keystore; used only when {@code auth.type = MTLS}. */
	public String getKeystorePath() {
		return keystorePath;
	}

	/** {@code auth.keystore.password} — used only when {@code auth.type = MTLS}. */
	public String getKeystorePassword() {
		return keystorePassword;
	}

	/** {@code auth.keystore.type} — default {@link #DEFAULT_STORE_TYPE}. */
	public String getKeystoreType() {
		return keystoreType;
	}

	/** {@code auth.truststore.path} — used only when {@code auth.type = MTLS}. */
	public String getTruststorePath() {
		return truststorePath;
	}

	/** {@code auth.truststore.password} — used only when {@code auth.type = MTLS}. */
	public String getTruststorePassword() {
		return truststorePassword;
	}

	/** {@code auth.truststore.type} — default {@link #DEFAULT_STORE_TYPE}. */
	public String getTruststoreType() {
		return truststoreType;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ClientConfiguration that)) {
			return false;
		}
		return connectTimeoutMs == that.connectTimeoutMs //
				&& readTimeoutMs == that.readTimeoutMs //
				&& Objects.equals(baseUri, that.baseUri) //
				&& Objects.equals(publicBaseUri, that.publicBaseUri) //
				&& Objects.equals(readAcceptMediaType, that.readAcceptMediaType) //
				&& authType == that.authType //
				&& Objects.equals(authTokenEnv, that.authTokenEnv) //
				&& Objects.equals(apiKeyHeader, that.apiKeyHeader) //
				&& Objects.equals(apiKeyEnv, that.apiKeyEnv) //
				&& Objects.equals(keystorePath, that.keystorePath) //
				&& Objects.equals(keystorePassword, that.keystorePassword) //
				&& Objects.equals(keystoreType, that.keystoreType) //
				&& Objects.equals(truststorePath, that.truststorePath) //
				&& Objects.equals(truststorePassword, that.truststorePassword) //
				&& Objects.equals(truststoreType, that.truststoreType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(baseUri, publicBaseUri, connectTimeoutMs, readTimeoutMs, readAcceptMediaType, authType,
				authTokenEnv,
				apiKeyHeader, apiKeyEnv, keystorePath, keystorePassword, keystoreType, truststorePath,
				truststorePassword, truststoreType);
	}

	/** Deliberately omits every credential field. */
	@Override
	public String toString() {
		return "ClientConfiguration[baseUri=" + baseUri + ", publicBaseUri=" + publicBaseUri + ", authType="
				+ authType + ", connectTimeoutMs=" + connectTimeoutMs + ", readTimeoutMs=" + readTimeoutMs + "]";
	}

	/**
	 * Fluent builder. Not thread-safe; build one per configuration. {@code base.uri} must
	 * be set before {@link #build()}.
	 */
	public static final class Builder {

		private URI baseUri;
		private URI publicBaseUri;
		private int connectTimeoutMs = 5_000;
		private int readTimeoutMs = 30_000;
		private String readAcceptMediaType = XMI;
		private AuthType authType = AuthType.NONE;
		private String authTokenEnv;
		private String apiKeyHeader = "X-API-Key";
		private String apiKeyEnv;
		private String keystorePath;
		private String keystorePassword;
		private String keystoreType = DEFAULT_STORE_TYPE;
		private String truststorePath;
		private String truststorePassword;
		private String truststoreType = DEFAULT_STORE_TYPE;

		private Builder() {
			// use ClientConfiguration.builder()
		}

		private Builder(ClientConfiguration from) {
			this.baseUri = from.baseUri;
			this.publicBaseUri = from.publicBaseUri;
			this.connectTimeoutMs = from.connectTimeoutMs;
			this.readTimeoutMs = from.readTimeoutMs;
			this.readAcceptMediaType = from.readAcceptMediaType;
			this.authType = from.authType;
			this.authTokenEnv = from.authTokenEnv;
			this.apiKeyHeader = from.apiKeyHeader;
			this.apiKeyEnv = from.apiKeyEnv;
			this.keystorePath = from.keystorePath;
			this.keystorePassword = from.keystorePassword;
			this.keystoreType = from.keystoreType;
			this.truststorePath = from.truststorePath;
			this.truststorePassword = from.truststorePassword;
			this.truststoreType = from.truststoreType;
		}

		public Builder baseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}

		/**
		 * The base the portal serves its identities under; {@code null} to mean
		 * {@code base.uri}.
		 *
		 * @see ClientConfiguration#getPublicBaseUri()
		 */
		public Builder publicBaseUri(URI publicBaseUri) {
			this.publicBaseUri = publicBaseUri;
			return this;
		}

		public Builder connectTimeoutMs(int connectTimeoutMs) {
			this.connectTimeoutMs = connectTimeoutMs;
			return this;
		}

		public Builder readTimeoutMs(int readTimeoutMs) {
			this.readTimeoutMs = readTimeoutMs;
			return this;
		}

		public Builder readAcceptMediaType(String readAcceptMediaType) {
			this.readAcceptMediaType = Objects.requireNonNull(readAcceptMediaType, "readAcceptMediaType");
			return this;
		}

		public Builder authType(AuthType authType) {
			this.authType = Objects.requireNonNull(authType, "authType");
			return this;
		}

		public Builder authTokenEnv(String authTokenEnv) {
			this.authTokenEnv = authTokenEnv;
			return this;
		}

		public Builder apiKeyHeader(String apiKeyHeader) {
			this.apiKeyHeader = Objects.requireNonNull(apiKeyHeader, "apiKeyHeader");
			return this;
		}

		public Builder apiKeyEnv(String apiKeyEnv) {
			this.apiKeyEnv = apiKeyEnv;
			return this;
		}

		public Builder keystorePath(String keystorePath) {
			this.keystorePath = keystorePath;
			return this;
		}

		public Builder keystorePassword(String keystorePassword) {
			this.keystorePassword = keystorePassword;
			return this;
		}

		public Builder keystoreType(String keystoreType) {
			this.keystoreType = Objects.requireNonNull(keystoreType, "keystoreType");
			return this;
		}

		public Builder truststorePath(String truststorePath) {
			this.truststorePath = truststorePath;
			return this;
		}

		public Builder truststorePassword(String truststorePassword) {
			this.truststorePassword = truststorePassword;
			return this;
		}

		public Builder truststoreType(String truststoreType) {
			this.truststoreType = Objects.requireNonNull(truststoreType, "truststoreType");
			return this;
		}

		/**
		 * @return the built, immutable configuration
		 * @throws IllegalStateException if {@code base.uri} was never set
		 */
		public ClientConfiguration build() {
			if (baseUri == null) {
				throw new IllegalStateException("base.uri is required");
			}
			return new ClientConfiguration(this);
		}
	}
}

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

import java.net.URI;

import org.eclipse.fennec.dcat.atlas.api.PublicIris;

import jakarta.ws.rs.core.UriBuilder;

/**
 * Builds the dereferenceable read URL of a resource from the configured public base.
 *
 * <h2>Why not from the request</h2>
 *
 * These URLs used to come from {@code UriInfo.getBaseUriBuilder()}, i.e. from the
 * request as the container received it. That makes a {@code Location} header — and the
 * {@code Location} on a {@code 409} — disagree with the {@code about} in the very same
 * response, because {@code about} is rendered from {@code publicBaseUrl} while
 * {@code UriInfo} reflects whatever reached the container. Measured behind the two
 * common proxy styles:
 * <ul>
 * <li>a proxy that preserves {@code Host} produced
 * {@code Location: http://opendata.example.de/rest/catalogs/{id}} next to
 * {@code about="http://localhost:8090/rest/catalogs/{id}"} — one response naming one
 * resource twice, differently;</li>
 * <li>a proxy that forwards only {@code X-Forwarded-Host}/{@code -Proto}/{@code -Prefix}
 * produced the container's own address, because Jetty's
 * {@code ForwardedRequestCustomizer} is not enabled — those headers are ignored.</li>
 * </ul>
 * Two divergences survive even a {@code Host}-preserving proxy: the scheme, since TLS
 * terminated upstream means the request arrives as plain {@code http}; and the path
 * prefix, when the portal is published under one the container does not see.
 * <p>
 * Deriving both from {@link PublicIris} makes them the same string by construction
 * rather than by coincidence, and leaves one configured value governing every IRI the
 * portal emits.
 */
public final class PublicUri {

	private PublicUri() {
	}

	/**
	 * The read URL of {@code segments} under the public base, e.g.
	 * {@code {publicBase}/catalogs/{id}}.
	 * <p>
	 * A segment may itself carry several path segments (a Distribution's membership path
	 * does); {@link UriBuilder#path(String)} keeps its slashes as delimiters and encodes
	 * the rest.
	 *
	 * @param publicIris the configured mapping, or {@code null}
	 * @param segments   path segments to append to the public base
	 * @return the URL, or {@code null} when {@code publicIris} is {@code null} — the
	 *         callers that pass one treat a missing location as "no header", and the
	 *         resources cannot register without the service anyway
	 */
	public static URI of(PublicIris publicIris, String... segments) {
		if (publicIris == null) {
			return null;
		}
		UriBuilder builder = UriBuilder.fromUri(publicIris.publicBase());
		for (String segment : segments) {
			builder.path(segment);
		}
		return builder.build();
	}
}

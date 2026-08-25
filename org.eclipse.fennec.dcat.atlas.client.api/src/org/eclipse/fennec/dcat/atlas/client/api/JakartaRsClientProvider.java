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

import org.osgi.annotation.versioning.ConsumerType;

import jakarta.ws.rs.client.Client;

/**
 * The single seam between client construction and Jakarta RS.
 * <p>
 * Everything above this seam — path building, XMI serialisation, ETag handling,
 * status mapping — is identical whether the client runs plain or in OSGi. The two
 * differ only in where the {@link Client} comes from: the default implementation
 * calls {@code ClientBuilder.newBuilder()}, and the OSGi front-end takes the
 * Whiteboard's {@code ClientBuilder} through DS. Nothing outside an implementation of
 * this interface should touch {@code ClientBuilder}.
 * <p>
 * An implementation owns applying the configured timeouts and whatever
 * {@link AuthType} asks for; the client itself never sets a credential header.
 * <p>
 * Mirrors {@code model.atlas}'s provider of the same name, so a consumer that already
 * supplies one there recognises this.
 */
@ConsumerType
public interface JakartaRsClientProvider {

	/**
	 * Build a configured Jakarta RS client. The caller owns it and closes it.
	 *
	 * @param configuration the effective configuration; never {@code null}
	 * @return a ready-to-use client
	 */
	Client newClient(ClientConfiguration configuration);
}

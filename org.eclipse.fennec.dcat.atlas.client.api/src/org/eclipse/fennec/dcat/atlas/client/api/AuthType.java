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

/**
 * How the client authenticates against the portal.
 *
 * <h2>Nothing is behind this yet</h2>
 *
 * The portal has <b>no authentication at all</b> today: no security filter, no
 * credentials, and the admin API is open to anything that can reach the port. This
 * enum exists so that consumers are configured through it from the start and are not
 * rewritten when the portal grows a policy enforcement point in front of it — closing
 * that gap is separate work on the portal, not something the client can do.
 */
public enum AuthType {

	/** No credentials. The only mode the portal actually understands today. */
	NONE,

	/** {@code Authorization: Bearer <token>}, the token read from the configured source. */
	BEARER,

	/** A fixed header, as MDO used against piveau ({@code X-API-Key}). */
	API_KEY,

	/** Mutual TLS, using the configured key and trust stores. */
	MTLS
}

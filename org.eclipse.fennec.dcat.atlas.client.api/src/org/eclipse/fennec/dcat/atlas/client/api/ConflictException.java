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
 * {@code 409} — the portal refused the write because of the state it already holds.
 *
 * <h2>Why this is one type and not two</h2>
 *
 * The portal distinguishes two causes internally: a repeat {@code POST} of an
 * identity it already has, and a reference that would dangle. Both are rendered by
 * their own {@code ExceptionMapper} as {@code 409} with a {@code text/plain} body
 * and <em>no</em> discriminating header, so from the wire the two are
 * indistinguishable without parsing prose. Splitting the type here would mean
 * sniffing a human-readable message, which breaks the moment somebody rewords it.
 * The message is carried through verbatim instead.
 */
public class ConflictException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public ConflictException(String message) {
		super(message);
	}

	public ConflictException(String message, Throwable cause) {
		super(message, cause);
	}
}

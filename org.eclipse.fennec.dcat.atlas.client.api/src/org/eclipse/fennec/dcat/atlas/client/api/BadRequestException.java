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
 * {@code 400} — the request was malformed, as opposed to the metadata being invalid
 * (which is {@code 422}).
 * <p>
 * Like {@link ConflictException} this covers more than one server-side cause with
 * nothing on the wire to separate them: an unconvertible query parameter and an
 * {@code about} belonging to somebody else both render as {@code 400} with a
 * {@code text/plain} body.
 */
public class BadRequestException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}
}

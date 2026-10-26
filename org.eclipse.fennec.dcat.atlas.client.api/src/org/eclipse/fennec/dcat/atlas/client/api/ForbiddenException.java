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
 * {@code 403} — the caller may not write this resource, and trying again will not
 * change that.
 *
 * <h2>Why it is its own type</h2>
 *
 * The portal refuses a write to a resource another system manages: {@code managedBy}
 * is stamped from the caller's identity and only that system may write it again.
 * Without a type of its own a {@code 403} falls to {@link TransportException}, which a
 * publishing loop reads as transient and retries forever — the refusal never surfaces
 * as a failure anybody can act on. The answer is permanent for these credentials, so
 * the caller should stop and report, the way it does for a validation refusal.
 *
 * <h2>What it merges</h2>
 *
 * A gateway refusing the request outright arrives as the same status with no
 * discriminating header, so, as with {@link ConflictException}, this is one type and
 * the portal's own text is carried through in the message.
 */
public class ForbiddenException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public ForbiddenException(String message) {
		super(message);
	}

	public ForbiddenException(String message, Throwable cause) {
		super(message, cause);
	}
}

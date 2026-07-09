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
package org.eclipse.fennec.dcat.atlas.rest;

import java.util.Optional;

import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response.ResponseBuilder;

/**
 * Helpers for HTTP conditional requests / optimistic locking (F-16).
 * <p>
 * ETags come from the service layer as a strong validator over the stored
 * representation (see {@code DcatHelper.etag}). The heavy lifting of comparing
 * {@code If-Match}/{@code If-None-Match} is delegated to
 * {@link Request#evaluatePreconditions}, which returns a pre-built response
 * (304 for a matched {@code If-None-Match} on a read, 412 for a failed
 * {@code If-Match}) when a precondition is not met, or {@code null} to proceed.
 */
final class ConditionalRequests {

	private ConditionalRequests() {
	}

	/** Wraps a raw validator string as a strong {@link EntityTag}, or {@code null} if empty. */
	static EntityTag tag(Optional<String> etag) {
		return etag.map(EntityTag::new).orElse(null);
	}

	/**
	 * Evaluates {@code If-Match}/{@code If-None-Match} against the resource's current
	 * ETag ({@code currentEtag}, empty when the resource does not exist).
	 *
	 * @return a response builder to return as-is when a precondition fails (412) or a
	 *         read is fresh (304); {@code null} when the caller should proceed.
	 */
	static ResponseBuilder evaluate(Request request, Optional<String> currentEtag) {
		return currentEtag.isPresent() //
				? request.evaluatePreconditions(new EntityTag(currentEtag.get())) //
				: request.evaluatePreconditions(); // no representation yet: supports If-None-Match: * create-guard
	}
}

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

import java.util.Objects;

/**
 * The outcome of a registration: what was stored, and the validator to guard the next
 * write with.
 *
 * <h2>Why a result and not just the entity</h2>
 *
 * A registration returns the stored entity <em>and</em> its {@code ETag}, so a caller can
 * hold on to the validator and make its next registration conditional without reading the
 * resource back — reading it back is exactly what does not work here (see
 * {@link DcatAtlasClient}). The portal supplies the ETag on the write response, so this
 * costs no extra request.
 *
 * <h2>Why {@code applied} can be false</h2>
 *
 * A conditional registration whose {@code If-Match} no longer matches is answered
 * {@code 412} and <b>nothing is written</b>. That is not an error in this client: it is the
 * whole point of sending the validator, and it means somebody else changed the resource
 * since it was last registered. So it comes back as {@code applied() == false} — logged by
 * the client — rather than as an exception, and a registration loop can carry on with the
 * next resource. Every other failure still throws.
 * <p>
 * An unconditional registration cannot come back unapplied: with no {@code If-Match} there
 * is no precondition to fail.
 *
 * <h2>A refusal carries nothing else</h2>
 *
 * The portal's {@code 412} has no body and no {@code ETag}, so a refused registration
 * cannot tell you what the current validator is. To get back in step, either read the
 * resource or register unconditionally next time — which of those is right depends on
 * whether the caller or the portal owns the truth.
 *
 * @param <T> the registered entity type
 */
public final class Registration<T> {

	private final T entity;
	private final String etag;

	private Registration(T entity, String etag) {
		this.entity = entity;
		this.etag = etag;
	}

	/**
	 * The write happened.
	 *
	 * @param entity the stored entity as the portal returned it
	 * @param etag   the new validator; may be {@code null} if the portal sent none
	 */
	public static <T> Registration<T> applied(T entity, String etag) {
		return new Registration<>(Objects.requireNonNull(entity, "entity"), etag);
	}

	/** The {@code If-Match} did not match, so nothing was written. */
	public static <T> Registration<T> notApplied() {
		return new Registration<>(null, null);
	}

	/**
	 * @return {@code true} if the entity was stored, {@code false} if a conditional
	 *         registration was refused because the resource had moved on
	 */
	public boolean applied() {
		return entity != null;
	}

	/**
	 * @return the stored entity
	 * @throws IllegalStateException if nothing was stored — asking for the result of a
	 *                               write that did not happen is a programming error, and
	 *                               a {@code null} here would be found much later
	 */
	public T entity() {
		if (entity == null) {
			throw new IllegalStateException(
					"This registration was not applied: the If-Match did not match, so there is no stored entity. "
							+ "Check applied() first.");
		}
		return entity;
	}

	/**
	 * The validator of the stored entity — pass it as {@code ifMatch} on the next
	 * registration to avoid overwriting somebody else's change.
	 *
	 * @return the {@code ETag}, or {@code null} if the portal sent none
	 * @throws IllegalStateException if nothing was stored
	 */
	public String etag() {
		if (entity == null) {
			throw new IllegalStateException(
					"This registration was not applied, and the portal's 412 carries no ETag, so there is no "
							+ "validator to report. Check applied() first.");
		}
		return etag;
	}

	@Override
	public String toString() {
		return applied() ? "Registration[applied, etag=" + etag + "]" : "Registration[not applied]";
	}
}

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
package org.eclipse.fennec.dcat.atlas.api.store;

/**
 * Thrown when a change was recorded in the store but could not be copied to its remote. The
 * REST layer renders this as {@code 503 Service Unavailable}.
 *
 * <h2>What a caller should do</h2>
 *
 * Retry. Unlike {@link StoreConflictException}, nothing about the request is wrong and
 * nothing about the stored state contradicts it — and unlike an outright store failure, the
 * work is already done: the change is committed, only its copy to the remote is outstanding.
 * That makes the retry cheap and likely to succeed once the remote is reachable.
 * <p>
 * ⚠️ For a store configured against a <em>remote</em>, the repository is held in memory, so
 * "recorded" means recorded until the process restarts. The readiness check reports the
 * divergence from the remote for exactly this reason.
 *
 * <h2>What it is not</h2>
 *
 * A failure to record the change at all. That is a server fault rather than a transient
 * condition, and it surfaces as an unmapped {@code 500} with its stack trace in the log,
 * because there is nothing a client can do with the detail and the diagnosis matters more
 * than a tidy body.
 * <p>
 * The two used to be indistinguishable — committing and pushing were reported through one
 * exception type — and this exception covered both, erring towards {@code 503}. Fixed
 * upstream 2026-08-19 by {@code GitPushException}.
 */
public class StoreUnavailableException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public StoreUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}

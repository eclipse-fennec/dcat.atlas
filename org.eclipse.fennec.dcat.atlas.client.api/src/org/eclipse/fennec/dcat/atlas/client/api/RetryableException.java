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
 * {@code 503} — the write is durable but not yet mirrored: the portal committed it
 * locally and the push to its git remote failed.
 *
 * <h2>Do not report this as data loss</h2>
 *
 * The easy mistake is to treat a 503 on a write as "the registration did not
 * happen". It did — the commit is in the portal store — and the portal retries the
 * push, so a later write completes the backlog. Retrying is safe because
 * registration is idempotent ({@code PUT} by path), and giving up and reporting
 * failure upstream would be wrong.
 */
public class RetryableException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public RetryableException(String message) {
		super(message);
	}

	public RetryableException(String message, Throwable cause) {
		super(message, cause);
	}
}

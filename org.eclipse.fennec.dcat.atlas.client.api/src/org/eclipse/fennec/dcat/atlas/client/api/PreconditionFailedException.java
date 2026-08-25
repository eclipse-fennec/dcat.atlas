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
 * {@code 412} — the {@code If-Match} validator no longer matches: somebody else
 * wrote to the resource since it was read.
 * <p>
 * Expected rather than exceptional in a read-modify-write loop; re-read and retry.
 */
public class PreconditionFailedException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public PreconditionFailedException(String message) {
		super(message);
	}

	public PreconditionFailedException(String message, Throwable cause) {
		super(message, cause);
	}
}

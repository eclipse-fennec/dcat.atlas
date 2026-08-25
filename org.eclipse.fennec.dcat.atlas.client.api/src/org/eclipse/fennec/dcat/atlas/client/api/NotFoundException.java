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
 * {@code 404} — the resource, or its parent, does not exist.
 * <p>
 * On a membership call this is the more interesting outcome: linking to a container
 * that is not there, or naming a member that is not, both answer 404, so the
 * message carries the path that produced it.
 */
public class NotFoundException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}

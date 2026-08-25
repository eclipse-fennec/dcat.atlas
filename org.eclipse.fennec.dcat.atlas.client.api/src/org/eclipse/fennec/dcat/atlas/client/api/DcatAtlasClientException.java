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
 * Base of everything this client throws.
 * <p>
 * Unchecked on purpose: a registration failure is not something a caller can
 * usefully recover from at every call site, and the useful distinctions are
 * between the subtypes rather than between "checked" and "not". Catch this type to
 * treat any portal failure alike; catch a subtype to act on one.
 * <p>
 * The subtypes correspond to what the portal actually puts on the wire, which is
 * not the same as the set of failures the portal's own service layer distinguishes
 * — see {@link ConflictException} and {@link BadRequestException} for the two
 * places where several server-side causes share one status with nothing to tell
 * them apart.
 */
public class DcatAtlasClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DcatAtlasClientException(String message) {
		super(message);
	}

	public DcatAtlasClientException(String message, Throwable cause) {
		super(message, cause);
	}
}

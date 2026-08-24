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
 * The request never produced a usable answer: a connect or read timeout, a refused
 * connection, an unreadable body — or a status that means the client itself is
 * malformed rather than the metadata.
 * <p>
 * {@code 415 Unsupported Media Type} lands here, and in this API it means almost
 * exactly one thing: {@code application/xml} was sent where
 * {@code application/xmi} was meant. The portal picks its codec by media type and
 * refuses the other.
 */
public class TransportException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	public TransportException(String message) {
		super(message);
	}

	public TransportException(String message, Throwable cause) {
		super(message, cause);
	}
}

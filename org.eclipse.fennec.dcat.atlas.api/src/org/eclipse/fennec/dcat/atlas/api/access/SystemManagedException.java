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
package org.eclipse.fennec.dcat.atlas.api.access;

/**
 * Thrown when a write or delete would change a resource that belongs to another system.
 * The REST layer renders this as {@code 403 Forbidden}.
 * <p>
 * There is deliberately no administrator override. A hand edit of a generated resource is
 * not reliably repaired by the system that generated it — model.atlas skips a rewrite
 * whose fingerprint still matches, so the edit survives, while one that moves the
 * fingerprint is wiped on the next sync and a hand delete is never recreated. Refusing is
 * the only answer that is the same every time. It is not a dead end either: the store is a
 * git repository, so an operator retiring a generating system can remove what it left
 * behind out of band.
 */
public class SystemManagedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String managedBy;
	private final String resource;

	/**
	 * @param managedBy the system the resource belongs to; reported so a client can say
	 *                  which one to go to rather than only that it may not proceed
	 * @param resource  the identity of the resource that was refused
	 */
	public SystemManagedException(String message, String managedBy, String resource) {
		super(message);
		this.managedBy = managedBy;
		this.resource = resource;
	}

	/** The system the refused resource is managed by. */
	public String getManagedBy() {
		return managedBy;
	}

	/**
	 * The refused resource, by its stored (logical) identity; {@code PublicIriFilter}
	 * rewrites it to the public one as the body goes out, as it does for every refusal.
	 */
	public String getResource() {
		return resource;
	}
}

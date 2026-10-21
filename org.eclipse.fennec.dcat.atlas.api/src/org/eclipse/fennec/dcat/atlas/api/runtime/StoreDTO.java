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
package org.eclipse.fennec.dcat.atlas.api.runtime;

import org.osgi.dto.DTO;

/**
 * Where what this portal holds is kept.
 *
 * <p>
 * <strong>Described without naming a backend.</strong> Git is one deployment's answer and a
 * database is an equally good one, so there is no branch here, no base path and no commit id:
 * a consumer that learned to read those would have to be rewritten the day the backend
 * changes, and a commit id is not something to make public in any case.
 * </p>
 *
 * <p>
 * <strong>And never a credential.</strong> The store configuration can carry a password, a
 * private key and its passphrase; {@link #location} is the location with any userinfo
 * stripped, and nothing else from that configuration appears here at all.
 * </p>
 *
 * @since 1.0
 */
public class StoreDTO extends DTO {

	/** What kind of store this is, in one lower-case word, e.g. {@code git}. */
	public String kind;

	/** Where it is, with any userinfo removed; {@code null} when the backend has no location. */
	public String location;

	/**
	 * Whether what is written survives a restart. False is one of the six silent failures:
	 * an in-memory store accepts every write and loses them all.
	 */
	public boolean persistent;

	/** Whether the store could be read when this snapshot was built. */
	public boolean readable;
}

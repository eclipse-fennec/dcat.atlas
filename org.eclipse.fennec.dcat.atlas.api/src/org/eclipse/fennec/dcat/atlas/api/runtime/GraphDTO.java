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
 * The SPARQL projection over what this portal holds.
 *
 * @since 1.0
 */
public class GraphDTO extends DTO {

	/** Whether the projection is configured at all. */
	public boolean enabled;

	/**
	 * Whether it can answer. Enabled but not ready is one of the six silent failures: queries
	 * succeed and return too few rows, which no caller can distinguish from an empty portal.
	 */
	public boolean ready;

	/** The configured maximum number of rows a query may return; {@code 0} when unlimited. */
	public int maxResults;

	/** The configured query timeout in milliseconds; {@code 0} when there is none. */
	public long timeoutMillis;
}

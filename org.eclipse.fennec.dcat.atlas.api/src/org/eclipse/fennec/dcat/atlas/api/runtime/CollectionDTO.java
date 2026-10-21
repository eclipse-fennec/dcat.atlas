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

import java.util.List;

import org.osgi.dto.DTO;

/**
 * One stored collection: how much is in it, and what it is called.
 *
 * @since 1.0
 */
public class CollectionDTO extends DTO {

	/** The collection's path segment, e.g. {@code datasets}. */
	public String name;

	/** How many resources it holds. Counted from the store's listing; nothing is loaded. */
	public int count;

	/** The ids it holds, in the store's order. */
	public List<String> ids;

	/**
	 * What this collection's store was configured to do on a write.
	 * <p>
	 * Reported per collection because it is configured per collection — ten times over — and
	 * two of them disagreeing is a state nothing else makes visible.
	 */
	public boolean validateOnWrite;
}

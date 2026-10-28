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
 * One stored collection: how much is in it, and what each entry is called.
 *
 * @since 1.0
 */
public class CollectionDTO extends DTO {

	/** How many resources it holds. */
	public int count;

	/** What it holds, in the store's id order. */
	public List<ResourceDTO> resources;
}

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
 * The catalogues, which carry more than an id.
 *
 * <p>
 * Its own type rather than a {@link CollectionDTO} of {@link CatalogDTO}, because a reader
 * that had to cast every entry to reach a catalogue's title would be reading an inventory
 * that does not quite know what it holds.
 * </p>
 *
 * @since 1.0
 */
public class CatalogCollectionDTO extends DTO {

	/** How many catalogues there are. */
	public int count;

	/** The catalogues, in the store's id order. */
	public List<CatalogDTO> resources;
}

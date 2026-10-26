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
 * What this portal holds, at one moment.
 *
 * <p>
 * A DTO and not the model, for the reason the OSGi DTOs exist: a reader gets a snapshot it
 * cannot reach through, and does not have to carry EMF to read a number. Everything here is a
 * copy of state that went on living the moment it was built.
 * </p>
 *
 * <p>
 * One field per stored DCAT collection, and no field for anything that is not stored content.
 * {@code dcat:Distribution} has none of its own because it is contained: a Distribution lives
 * inside its Dataset rather than in a collection of its own.
 * </p>
 *
 * <p>
 * {@link #changeCount} is the number that also travels as the service's
 * {@code service.changecount} property, and it is the version a consumer compares: there is
 * no revision and no commit id here, because the storage layer's own is not public and would
 * not survive a change of backend. It counts changes rather than describing them — "has
 * anything happened" is a comparison, and "what" is another snapshot.
 * </p>
 *
 * @since 1.0
 */
public class DcatAtlasRuntimeDTO extends DTO {

	/** How often this snapshot has changed since the portal started. */
	public long changeCount;

	/** When this snapshot was built, in epoch milliseconds. */
	public long takenAt;

	/** The catalogues, each with what it lists. */
	public CatalogCollectionDTO catalogs;

	/** The datasets. */
	public CollectionDTO datasets;

	/** The data services. */
	public CollectionDTO dataServices;

	/** The dataset series. */
	public CollectionDTO datasetSeries;
}

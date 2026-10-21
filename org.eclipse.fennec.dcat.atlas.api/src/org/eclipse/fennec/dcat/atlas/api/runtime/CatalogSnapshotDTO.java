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
 * One catalog, with what it holds.
 *
 * <p>
 * Catalogs are the one kind of resource this snapshot reads rather than counts, because there
 * are tens of them and the question "what is in this portal" is unanswerable without them.
 * Their members are counted from the catalog itself and never loaded.
 * </p>
 *
 * @since 1.0
 */
public class CatalogSnapshotDTO extends DTO {

	/** The id it is stored under. */
	public String id;

	/** The IRI it is served as — the public one, which is what a caller can dereference. */
	public String iri;

	/** Its title in whatever language came first, or {@code null} when it has none. */
	public String title;

	/** How many datasets it lists. */
	public int datasets;

	/** How many data services it lists. */
	public int dataServices;

	/** How many sub-catalogs it lists. */
	public int catalogs;

	/** How many catalogue records it carries — one per member entry the portal maintains. */
	public int records;
}

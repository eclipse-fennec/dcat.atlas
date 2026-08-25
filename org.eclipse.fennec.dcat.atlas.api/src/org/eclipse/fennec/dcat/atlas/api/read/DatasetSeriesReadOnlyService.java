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
package org.eclipse.fennec.dcat.atlas.api.read;

import java.util.List;
import java.util.Optional;

import dcat.DatasetSeries;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
public interface DatasetSeriesReadOnlyService {
	
	Optional<DatasetSeries> getDatasetSeries(String id);

	/** Every entry, materialised. See {@link #listDatasetSeries(PageRequest)} first. */
	List<DatasetSeries> listDatasetSeries();

	/**
	 * One page of the collection, resuming after {@code page.after()}.
	 * <p>
	 * The paged read is what a client should use: listDatasetSeries() loads and
	 * materialises every stored entity, which is affordable for the graph projection
	 * that needs all of them anyway and not for an HTTP response.
	 */
	Page<DatasetSeries> listDatasetSeries(PageRequest page);

	/** Strong ETag validator for the stored dataset series {@code id}, or empty if absent (F-16). */
	Optional<String> etag(String id);

}

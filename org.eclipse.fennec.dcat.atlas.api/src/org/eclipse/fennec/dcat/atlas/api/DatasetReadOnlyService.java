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
package org.eclipse.fennec.dcat.atlas.api;

import java.util.List;
import java.util.Optional;

import dcat.Dataset;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
public interface DatasetReadOnlyService {
	
	Optional<Dataset> getDataset(String id);

    /** Every entry, materialised. See {@link #listDatasets(PageRequest)} first. */
    List<Dataset> listDatasets();

    /**
     * One page of the collection, resuming after {@code page.after()}.
     * <p>
     * The paged read is what a client should use: listDatasets() loads and
     * materialises every stored entity, which is affordable for the graph projection
     * that needs all of them anyway and not for an HTTP response.
     */
    Page<Dataset> listDatasets(PageRequest page);

    /** Strong ETag validator for the stored dataset {@code id}, or empty if absent (F-16). */
    Optional<String> etag(String id);

}

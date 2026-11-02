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

import dcat.DataService;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
public interface DataServiceReadOnlyHandler {
	
	Optional<DataService> getDataService(String id);

    /** Every entry, materialised. See {@link #listDataServices(PageRequest)} first. */
    List<DataService> listDataServices();

    /**
     * One page of the collection, resuming after {@code page.after()}.
     * <p>
     * The paged read is what a client should use: listDataServices() loads and
     * materialises every stored entity, which is affordable for the graph projection
     * that needs all of them anyway and not for an HTTP response.
     */
    Page<DataService> listDataServices(PageRequest page);

    /** Strong ETag validator for the stored data service {@code id}, or empty if absent (F-16). */
    Optional<String> etag(String id);


    /**
     * The ids this collection holds, in the store's order, without loading anything.
     * <p>
     * A tree listing and no blob read at all, which is what makes an inventory of a large
     * collection affordable: {@link #listDataServices()} materialises every entity and a page
     * materialises its own entries, while this reads one tree object however many ids come
     * back. A collection that cannot be listed answers empty rather than throwing, because
     * a caller counting data services is not the caller who should be told the store is down.
     */
    List<String> ids();
}

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
package org.eclipse.fennec.dcat.atlas.impl.handler;

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Catalog;

/**
 * File-backed, read-only view of the catalog store.
 * <p>
 * {@code CatalogAdminHandlerImpl} extends this class to inherit the read
 * operations and add the write operations, so the two services share one
 * implementation of {@code getCatalog}/{@code listCatalogs}.
 */
public class CatalogReadOnlyHandlerImpl extends AbstractEntityHandler<Catalog> implements CatalogReadOnlyHandler {

	public CatalogReadOnlyHandlerImpl(DcatStoreService storeService) {
		super(storeService, StoreLayout.CATALOGS);
	}

	@Override
	public Optional<Catalog> getCatalog(String id) {
		return getEntity(id);
	}

	@Override
	public List<Catalog> listCatalogs() {
		return listEntities();
	}

	@Override
	public Page<Catalog> listCatalogs(PageRequest page) {
		return listEntities(page);
	}
}

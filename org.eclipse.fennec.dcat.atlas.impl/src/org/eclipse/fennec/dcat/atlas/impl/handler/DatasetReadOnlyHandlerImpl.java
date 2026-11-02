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

import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Dataset;

/**
 * File-backed, read-only view of the dataset store. A Dataset's Distributions are
 * contained in its own file (FR-10), so they come back with it and need no store
 * of their own.
 */
public class DatasetReadOnlyHandlerImpl extends AbstractEntityHandler<Dataset> implements DatasetReadOnlyHandler {

	public DatasetReadOnlyHandlerImpl(DcatStoreService storeService) {
		super(storeService, StoreLayout.DATASETS);
	}

	@Override
	public Optional<Dataset> getDataset(String id) {
		return getEntity(id);
	}

	@Override
	public List<Dataset> listDatasets() {
		return listEntities();
	}

	@Override
	public Page<Dataset> listDatasets(PageRequest page) {
		return listEntities(page);
	}
}

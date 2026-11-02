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

import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.DatasetSeries;

/** File-backed, read-only view of the dataset-series store. */
public class DatasetSeriesReadOnlyHandlerImpl extends AbstractEntityHandler<DatasetSeries>
		implements DatasetSeriesReadOnlyHandler {

	public DatasetSeriesReadOnlyHandlerImpl(DcatStoreService storeService) {
		super(storeService, StoreLayout.DATASET_SERIES);
	}

	@Override
	public Optional<DatasetSeries> getDatasetSeries(String id) {
		return getEntity(id);
	}

	@Override
	public List<DatasetSeries> listDatasetSeries() {
		return listEntities();
	}

	@Override
	public Page<DatasetSeries> listDatasetSeries(PageRequest page) {
		return listEntities(page);
	}
}

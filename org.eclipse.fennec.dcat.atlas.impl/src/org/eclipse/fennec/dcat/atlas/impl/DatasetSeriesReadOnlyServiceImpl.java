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
package org.eclipse.fennec.dcat.atlas.impl;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.felix.hc.api.HealthCheck;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DatasetSeries;

/** File-backed, read-only view of the dataset-series store. */
@Component(name = "DatasetSeriesReadOnlyService", service = { DatasetSeriesReadOnlyService.class,
		HealthCheck.class }, property = { HealthCheck.NAME + "=store:dataset-series", HealthCheck.TAGS + "=ready" })
@Designate(ocd = StoreConfig.class)
public class DatasetSeriesReadOnlyServiceImpl extends AbstractEntityStore<DatasetSeries>
		implements DatasetSeriesReadOnlyService {

	@Activate
	public DatasetSeriesReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		this(resourceSetFactory, Path.of(config.root()));
	}

	/** Package-visible for the admin subclass and tests. */
	DatasetSeriesReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path root) {
		super(resourceSetFactory, root, StoreLayout.DATASET_SERIES);
	}

	@Override
	public Optional<DatasetSeries> getDatasetSeries(String id) {
		return getEntity(id);
	}

	@Override
	public List<DatasetSeries> listDatasetSeries() {
		return listEntities();
	}
}

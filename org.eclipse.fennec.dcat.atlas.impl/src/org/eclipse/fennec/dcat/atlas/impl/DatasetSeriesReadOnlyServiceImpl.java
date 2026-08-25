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

import java.util.List;
import java.util.Optional;

import org.apache.felix.hc.api.HealthCheck;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
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
	public DatasetSeriesReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		this(resourceSetFactory, gitService, config.basePath(), config.validateOnWrite());
	}

	/** Package-visible for the admin subclass and tests; validates as the shipped configurations do. */
	DatasetSeriesReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		this(resourceSetFactory, gitService, basePath, true);
	}

	/** Package-visible for the admin subclass and tests. */
	DatasetSeriesReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, StoreLayout.DATASET_SERIES, validateOnWrite);
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

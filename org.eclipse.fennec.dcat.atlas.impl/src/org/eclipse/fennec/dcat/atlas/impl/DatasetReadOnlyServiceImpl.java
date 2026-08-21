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
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.Page;
import org.eclipse.fennec.dcat.atlas.api.PageRequest;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;

/**
 * File-backed, read-only view of the dataset store. A Dataset's Distributions are
 * contained in its own file (FR-10), so they come back with it and need no store
 * of their own.
 */
@Component(name = "DatasetReadOnlyService", service = { DatasetReadOnlyService.class, HealthCheck.class }, property = {
		HealthCheck.NAME + "=store:datasets", HealthCheck.TAGS + "=ready" })
@Designate(ocd = StoreConfig.class)
public class DatasetReadOnlyServiceImpl extends AbstractEntityStore<Dataset> implements DatasetReadOnlyService {

	@Activate
	public DatasetReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		this(resourceSetFactory, gitService, config.basePath(), config.validateOnWrite());
	}

	/** Package-visible for the admin subclass and tests; validates as the shipped configurations do. */
	DatasetReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		this(resourceSetFactory, gitService, basePath, true);
	}

	/** Package-visible for the admin subclass and tests. */
	DatasetReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, StoreLayout.DATASETS, validateOnWrite);
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

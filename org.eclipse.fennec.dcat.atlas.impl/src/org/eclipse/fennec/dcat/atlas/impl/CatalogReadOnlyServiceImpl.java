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
import org.eclipse.fennec.dcat.atlas.api.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Catalog;

/**
 * File-backed, read-only view of the catalog store.
 * <p>
 * {@code CatalogAdminServiceImpl} extends this class to inherit the read
 * operations and add the write operations, so the two services share one
 * implementation of {@code getCatalog}/{@code listCatalogs}.
 */
@Component(name = "CatalogReadOnlyService", service = { CatalogReadOnlyService.class, HealthCheck.class }, property = {
		HealthCheck.NAME + "=store:catalogs", HealthCheck.TAGS + "=ready" })
@Designate(ocd = StoreConfig.class)
public class CatalogReadOnlyServiceImpl extends AbstractEntityStore<Catalog> implements CatalogReadOnlyService {

	@Activate
	public CatalogReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		this(resourceSetFactory, gitService, config.basePath(), config.validateOnWrite());
	}

	/** Package-visible for the admin subclass and tests; validates as the shipped configurations do. */
	CatalogReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		this(resourceSetFactory, gitService, basePath, true);
	}

	/** Package-visible for the admin subclass and tests. */
	CatalogReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, StoreLayout.CATALOGS, validateOnWrite);
	}

	@Override
	public Optional<Catalog> getCatalog(String id) {
		return getEntity(id);
	}

	@Override
	public List<Catalog> listCatalogs() {
		return listEntities();
	}
}

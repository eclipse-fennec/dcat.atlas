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

import org.eclipse.fennec.dcat.atlas.impl.handler.CatalogReadOnlyHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DataServiceReadOnlyHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetReadOnlyHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetSeriesReadOnlyHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DistributionReadOnlyHandlerImpl;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DcatReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DistributionReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

/**
 * The read side of the portal.
 *
 * <h2>Always deployed, and deployable alone</h2>
 *
 * A portal that only publishes configures this and nothing else; one that also writes
 * configures a {@code DcatAdminService} beside it, naming the same store. The write side
 * does not provide this service, although an admin handler is already a read handler: the
 * RDF projection behind SPARQL reads through this interface, and a writing portal requires
 * the projection, so one component providing both would close a cycle and nothing would
 * start.
 */
@Component(name = "DcatReadOnlyService", configurationPid = "DcatReadOnlyService",
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DcatReadOnlyConfig.class)
public class DcatReadOnlyServiceImpl implements DcatReadOnlyService {

	private final DcatStoreService storeService;
	private final CatalogReadOnlyHandler catalogReadOnlyHandler;
	private final DataServiceReadOnlyHandler dataServiceReadOnlyHandler;
	private final DatasetReadOnlyHandler datasetReadOnlyHandler;
	private final DistributionReadOnlyHandler distributionReadOnlyHandler;
	private final DatasetSeriesReadOnlyHandler dataSeriesReadOnlyService;

	/**
	 * The handlers are built here, not bound: each is a plain object over the one store
	 * this service was configured against, so they cannot disagree about which store they
	 * read.
	 */
	@Activate
	public DcatReadOnlyServiceImpl(@Reference(name = "storeService") DcatStoreService storeService) {
		this.storeService = storeService;
		this.catalogReadOnlyHandler = new CatalogReadOnlyHandlerImpl(storeService);
		this.dataServiceReadOnlyHandler = new DataServiceReadOnlyHandlerImpl(storeService);
		this.datasetReadOnlyHandler = new DatasetReadOnlyHandlerImpl(storeService);
		this.dataSeriesReadOnlyService = new DatasetSeriesReadOnlyHandlerImpl(storeService);
		// A Distribution has no file of its own and lives in its Dataset (FR-10), so its
		// handler reads through the dataset handler built just above rather than binding a
		// DatasetReadOnlyHandler of its own.
		this.distributionReadOnlyHandler = new DistributionReadOnlyHandlerImpl(storeService,
				datasetReadOnlyHandler);
	}

	@Override
	public CatalogReadOnlyHandler getCatalogReadOnlyHandler() {
		return catalogReadOnlyHandler;
	}

	@Override
	public DataServiceReadOnlyHandler getDataServiceReadOnlyHandler() {
		return dataServiceReadOnlyHandler;
	}

	@Override
	public DatasetReadOnlyHandler getDatasetReadOnlyHandler() {
		return datasetReadOnlyHandler;
	}

	@Override
	public DistributionReadOnlyHandler getDistributionReadOnlyHandler() {
		return distributionReadOnlyHandler;
	}

	@Override
	public DatasetSeriesReadOnlyHandler getDataSeriesReadOnlyHandler() {
		return dataSeriesReadOnlyService;
	}

	@Override
	public Optional<String> revision() {
		return storeService.revision();
	}
}

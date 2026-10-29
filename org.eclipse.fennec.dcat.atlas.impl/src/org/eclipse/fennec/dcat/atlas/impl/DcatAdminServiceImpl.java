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

import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DistributionAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DistributionReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.handler.CatalogAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DataServiceAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DatasetSeriesAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.handler.DistributionAdminHandlerImpl;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.metatype.annotations.Designate;

/**
 * 
 * @author ilenia
 * @since Oct 28, 2026
 */
// service is pinned to DcatAdminService alone: this class is a DcatReadOnlyService too, but
// publishing that from here closes a cycle through the SPARQL projection - see DcatAdminService.
@Component(name = "DcatAdminService", service = DcatAdminService.class,
		configurationPid = "DcatAdminService", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DcatAdminConfig.class)
public class DcatAdminServiceImpl implements DcatAdminService{
	
	private final DcatStoreService storeService;
	private final CatalogAdminHandler catalogAdminHandler;
	private final DataServiceAdminHandler dataServiceAdminHandler;
	private final DatasetAdminHandler datasetAdminHandler;
	private final DistributionAdminHandler distributionAdminHandler;
	private final DatasetSeriesAdminHandler dataSeriesAdminService;
	
	
	/**
	 * The handlers are built here, not bound: each is a plain object over the one store
	 * this service was configured against. That is the point of the facade - the store is
	 * selected in one place, so the handlers cannot disagree about which store they write
	 * to.
	 * <p>
	 * Everything a handler needs is a constructor parameter, because the handlers are
	 * built here: SCR sets an injected field only after the constructor has returned, so a
	 * field would hand every handler a null.
	 */
	@Activate
	public DcatAdminServiceImpl(
			@Reference(name = "changeNotifier") DcatChangeNotifier changeNotifier,
			@Reference(name = "storeService") DcatStoreService storeService,
			@Reference(name = "graphService", cardinality = ReferenceCardinality.OPTIONAL) DcatGraphService graphService,
			DcatAdminConfig config) {

		requireGraphWhenSparqlSupported(config, graphService);
		this.storeService = storeService;
		this.catalogAdminHandler = new CatalogAdminHandlerImpl(changeNotifier, storeService, graphService);
		this.dataServiceAdminHandler = new DataServiceAdminHandlerImpl(changeNotifier, storeService, graphService);
		this.datasetAdminHandler = new DatasetAdminHandlerImpl(changeNotifier, storeService, graphService);
		this.dataSeriesAdminService = new DatasetSeriesAdminHandlerImpl(changeNotifier, storeService, graphService);
		// The distribution handler needs to read datasets - a Distribution has no file of its
		// own and lives in its Dataset (FR-10). It is given the dataset handler built just
		// above, not a DatasetReadOnlyHandler of its own: an admin handler is already a
		// read-only one, and taking the service would reintroduce the question this facade
		// exists to settle, namely whether the two are looking at the same store.
		this.distributionAdminHandler = new DistributionAdminHandlerImpl(changeNotifier, storeService,
				datasetAdminHandler, graphService);
	}

	/**
	 * A portal that serves SPARQL needs the projection a write invalidates. See
	 * {@link DcatAdminConfig#sparqlSupported()} for why this is checked here as well as
	 * through the reference's cardinality.
	 */
	private static void requireGraphWhenSparqlSupported(DcatAdminConfig config, DcatGraphService graphService) {
		if (config.sparqlSupported() && graphService == null) {
			throw new IllegalStateException(
					"This portal supports SPARQL, but no DcatGraphService is bound");
		}
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService#getCatalogAdminHandler()
	 */
	@Override
	public CatalogAdminHandler getCatalogAdminHandler() {
		return catalogAdminHandler;
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService#getDataServiceAdminHandler()
	 */
	@Override
	public DataServiceAdminHandler getDataServiceAdminHandler() {
		return dataServiceAdminHandler;
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService#getDatasetAdminHandler()
	 */
	@Override
	public DatasetAdminHandler getDatasetAdminHandler() {
		return datasetAdminHandler;
	}


	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService#getDistributionAdminHandler()
	 */
	@Override
	public DistributionAdminHandler getDistributionAdminHandler() {
		return distributionAdminHandler;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService#getDataSeriesAdminHandler()
	 */
	@Override
	public DatasetSeriesAdminHandler getDataSeriesAdminHandler() {
		return dataSeriesAdminService;
	}

	// --- the read half -------------------------------------------------------
	//
	// An admin handler is already a read handler, so these are the same five objects under
	// their read-only interfaces. Holding a DcatAdminService is therefore enough to read as
	// well as write; what this component must not do is *register* as a
	// DcatReadOnlyService, which is what the SPARQL projection binds.

	@Override
	public CatalogReadOnlyHandler getCatalogReadOnlyHandler() {
		return catalogAdminHandler;
	}

	@Override
	public DataServiceReadOnlyHandler getDataServiceReadOnlyHandler() {
		return dataServiceAdminHandler;
	}

	@Override
	public DatasetReadOnlyHandler getDatasetReadOnlyHandler() {
		return datasetAdminHandler;
	}

	@Override
	public DistributionReadOnlyHandler getDistributionReadOnlyHandler() {
		return distributionAdminHandler;
	}

	@Override
	public DatasetSeriesReadOnlyHandler getDataSeriesReadOnlyHandler() {
		return dataSeriesAdminService;
	}

	@Override
	public Optional<String> revision() {
		return storeService.revision();
	}
}

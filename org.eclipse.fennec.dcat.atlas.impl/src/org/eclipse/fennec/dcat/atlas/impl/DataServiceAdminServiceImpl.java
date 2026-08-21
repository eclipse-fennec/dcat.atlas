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
import java.util.NoSuchElementException;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreConfig;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import dcat.DataService;
import dcat.Dataset;

/** File-backed {@link DataServiceAdminService} (write side). */
@Component(name = "DataServiceAdminService", service = DataServiceAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DataServiceAdminServiceImpl extends DataServiceReadOnlyServiceImpl implements DataServiceAdminService {

	@Activate
	public DataServiceAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		super(resourceSetFactory, gitService, config);
	}

	/** Package-visible for tests. */
	DataServiceAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		super(resourceSetFactory, gitService, basePath);
	}

	/** Package-visible for tests that need the model constraints enforced. */
	DataServiceAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, validateOnWrite);
	}

	/**
	 * The RDF projection behind the SPARQL endpoint, maintained here rather than in
	 * the REST layer: this service is the persistence boundary, and REST is only one
	 * of its callers (persistence plan constraint G2). Optional and dynamic — absent
	 * simply means no SPARQL.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graphService;

	/**
	 * On-write SHACL enforcement (FR-4), maintained here rather than in the REST layer:
	 * this service is the persistence boundary, and REST is only one of its callers.
	 * <p>
	 * Optional and dynamic, so absence simply means no enforcement — see
	 * {@link org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation}. An operator who
	 * needs the strict reading raises this reference's minimum cardinality in
	 * configuration ({@code validationService.cardinality.minimum=1}), which makes this
	 * component unsatisfiable without a validation service instead of letting writes
	 * through unchecked.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@Override
	protected DcatValidationService writeValidation() {
		return validationService;
	}

	@Override
	public DataService upsertDataService(DataService service) {
		String id = idOrMint(service);
		Store store = store();
		store.put(collection, id, service);
		store.commit("Store data service " + id);
		reproject(id);
		return service;
	}

	@Override
	public List<String> deleteDataService(String id, boolean cascade) {
		Store store = store();
		List<String> unlinked = References.detach(store, collection, id, cascade);
		store.delete(collection, id);
		// One commit for the delete and every unlink it caused; see CatalogAdminServiceImpl.
		store.commit(cascade ? "Delete data service " + id + " and unlink its referrers"
				: "Delete data service " + id);
		reproject(id);
		return unlinked;
	}

	// --- dcat:servesDataset membership -------------------------------------
	//
	// The reference lives on the DataService, so the DataService is what is edited,
	// saved and returned — the Dataset is untouched. Mirrors the catalog membership
	// shapes (add stores then links, link requires the target to exist, both idempotent).

	@Override
	public DataService addDatasetToDataService(String dataServiceId, Dataset dataset) {
		if (dataset == null) {
			throw new IllegalArgumentException("Cannot add nothing to data service " + dataServiceId);
		}
		Store store = store();
		requireDataService(store, dataServiceId);
		// Written first: a link to something that is not there yet is exactly the dangling
		// reference this design refuses to create.
		String datasetId = datasetIdOrMint(dataset);
		store.put(StoreLayout.DATASETS, datasetId, dataset);
		return connect(store, dataServiceId, datasetId,
				"Add dataset %s to data service %s".formatted(datasetId, dataServiceId));
	}

	@Override
	public DataService linkDatasetToDataService(String dataServiceId, String datasetId) {
		Store store = store();
		requireDataService(store, dataServiceId);
		if (store.get(StoreLayout.DATASETS, datasetId).isEmpty()) {
			throw new NoSuchElementException("Unknown dataset: " + datasetId);
		}
		return connect(store, dataServiceId, datasetId,
				"Link dataset %s to data service %s".formatted(datasetId, dataServiceId));
	}

	@Override
	public void deleteDatasetFromDataService(String dataServiceId, String datasetId) {
		Store store = store();
		DataService dataService = requireDataService(store, dataServiceId);
		if (Members.remove(dataService.getServesDataset(), StoreLayout.DATASETS, datasetId)) {
			store.save(dataService);
			store.commit("Unlink dataset %s from data service %s".formatted(datasetId, dataServiceId));
			reproject(dataServiceId);
		}
	}

	private DataService connect(Store store, String dataServiceId, String datasetId, String message) {
		DataService dataService = requireDataService(store, dataServiceId);
		if (!Members.contains(dataService.getServesDataset(), StoreLayout.DATASETS, datasetId)) {
			dataService.getServesDataset().add(store.<Dataset>get(StoreLayout.DATASETS, datasetId)
					.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId)));
			store.save(dataService);
		}
		// Commits whatever the operation staged, as one commit; see CatalogAdminServiceImpl.
		store.commit(message);
		reproject(dataServiceId);
		return dataService;
	}

	private static DataService requireDataService(Store store, String dataServiceId) {
		return store.<DataService>get(StoreLayout.DATA_SERVICES, dataServiceId)
				.orElseThrow(() -> new NoSuchElementException("Unknown data service: " + dataServiceId));
	}

	/** As {@code idOrMint}, but for a Dataset — a member of a collection other than this store's. */
	private static String datasetIdOrMint(Dataset dataset) {
		return DcatIds.idForWrite(StoreLayout.DATASETS, dataset.getAbout());
	}

	/** Re-projects one data service into the RDF graph, if a projection is present. */
	private void reproject(String id) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.DATA_SERVICE, id);
		}
	}
}

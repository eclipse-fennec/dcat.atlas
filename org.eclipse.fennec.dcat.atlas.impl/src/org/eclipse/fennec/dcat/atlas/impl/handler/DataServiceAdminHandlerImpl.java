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
import java.util.NoSuchElementException;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.records.CatalogRecords;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.DataService;
import dcat.Dataset;

/** File-backed {@link DataServiceAdminHandler} (write side). */
public class DataServiceAdminHandlerImpl extends DataServiceReadOnlyHandlerImpl implements DataServiceAdminHandler {

	/** How this service says it changed the portal; see DcatChangeNotifier. */
	private final DcatChangeNotifier changeNotifier;

	public DataServiceAdminHandlerImpl(DcatChangeNotifier changeNotifier,
			DcatStoreService store,
			DcatGraphService graphService) {
		super(store);
		this.changeNotifier = changeNotifier;
		this.graphService = graphService;
	}

	/** For callers with no projection to reconcile - the tests, and a portal without SPARQL. */
	public DataServiceAdminHandlerImpl(DcatChangeNotifier changeNotifier, DcatStoreService store) {
		this(changeNotifier, store, null);
	}

	/**
	 * The RDF projection behind the SPARQL endpoint, maintained here rather than in
	 * the REST layer: this handler is the persistence boundary, and REST is only one
	 * of its callers (persistence plan constraint G2).
	 * <p>
	 * Null when the deployment serves no SPARQL. Whether it is allowed to be null is
	 * settled at the other end, by {@code DcatAdminConfig.sparqlSupported}.
	 */
	private final DcatGraphService graphService;


	@Override
	public DataService upsertDataService(DataService service, Caller caller) {
		String id = idOrMint(service);
		DcatStoreSession storeSession = store(caller);
		storeSession.put(collection, id, service);
		// The entry's metadata changed, so its catalogue records move with it (#56).
		CatalogRecords.memberWritten(storeSession, StoreLayout.logicalIri(collection, id));
		storeSession.commit("Store data service " + id);
		reproject(id);
		// After the commit: delivery is asynchronous, so a consumer can read the portal
		// before the operation that announced the change has finished it.
		changeNotifier.entityChanged(collection, id);
		return service;
	}

	@Override
	public List<String> deleteDataService(String id, boolean cascade, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		if (cascade) {
			// See CatalogRecords.memberDeleted: detach would otherwise leave records without a topic.
			CatalogRecords.memberDeleted(storeSession, StoreLayout.logicalIri(collection, id));
		}
		List<String> unlinked = References.detach(storeSession, collection, id, cascade);
		storeSession.delete(collection, id);
		// One commit for the delete and every unlink it caused; see CatalogAdminHandlerImpl.
		storeSession.commit(cascade ? "Delete data service " + id + " and unlink its referrers"
				: "Delete data service " + id);
		reproject(id);
		changeNotifier.entityDeleted(collection, id);
		return unlinked;
	}

	// --- dcat:servesDataset membership -------------------------------------
	//
	// The reference lives on the DataService, so the DataService is what is edited,
	// saved and returned — the Dataset is untouched. Mirrors the catalog membership
	// shapes (add stores then links, link requires the target to exist, both idempotent).

	@Override
	public DataService addDatasetToDataService(String dataServiceId, Dataset dataset, Caller caller) {
		if (dataset == null) {
			throw new IllegalArgumentException("Cannot add nothing to data service " + dataServiceId);
		}
		DcatStoreSession storeSession = store(caller);
		requireDataService(storeSession, dataServiceId);
		// Written first: a link to something that is not there yet is exactly the dangling
		// reference this design refuses to create.
		String datasetId = datasetIdOrMint(dataset);
		storeSession.put(StoreLayout.DATASETS, datasetId, dataset);
		return connect(storeSession, dataServiceId, datasetId,
				"Add dataset %s to data service %s".formatted(datasetId, dataServiceId));
	}

	@Override
	public DataService linkDatasetToDataService(String dataServiceId, String datasetId, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		requireDataService(storeSession, dataServiceId);
		if (storeSession.get(StoreLayout.DATASETS, datasetId).isEmpty()) {
			throw new NoSuchElementException("Unknown dataset: " + datasetId);
		}
		return connect(storeSession, dataServiceId, datasetId,
				"Link dataset %s to data service %s".formatted(datasetId, dataServiceId));
	}

	@Override
	public void deleteDatasetFromDataService(String dataServiceId, String datasetId, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		DataService dataService = requireDataService(storeSession, dataServiceId);
		if (Members.remove(dataService.getServesDataset(), StoreLayout.DATASETS, datasetId)) {
			// A removal. See DcatStoreSession.saveRemoval.
			storeSession.saveRemoval(dataService);
			storeSession.commit("Unlink dataset %s from data service %s".formatted(datasetId, dataServiceId));
			reproject(dataServiceId);
			changeNotifier.entityUnlinked(StoreLayout.DATASETS, datasetId);
		}
	}

	private DataService connect(DcatStoreSession storeSession, String dataServiceId, String datasetId, String message) {
		DataService dataService = requireDataService(storeSession, dataServiceId);
		boolean linked = false;
		if (!Members.contains(dataService.getServesDataset(), StoreLayout.DATASETS, datasetId)) {
			dataService.getServesDataset().add(storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId)
					.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId)));
			storeSession.save(dataService);
			linked = true;
		}
		// Commits whatever the operation staged, as one commit; see CatalogAdminHandlerImpl.
		storeSession.commit(message);
		reproject(dataServiceId);
		// Only for a link that happened, and only once it is committed; see
		// CatalogAdminHandlerImpl.connect.
		if (linked) {
			changeNotifier.entityLinked(StoreLayout.DATASETS, datasetId);
		}
		return dataService;
	}

	private static DataService requireDataService(DcatStoreSession storeSession, String dataServiceId) {
		return storeSession.<DataService>get(StoreLayout.DATA_SERVICES, dataServiceId)
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

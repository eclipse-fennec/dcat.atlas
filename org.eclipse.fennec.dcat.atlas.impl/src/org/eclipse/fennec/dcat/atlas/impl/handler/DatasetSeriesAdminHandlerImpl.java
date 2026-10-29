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
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminHandler;
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

import dcat.Dataset;
import dcat.DatasetSeries;

/**
 * File-backed {@link DatasetSeriesAdminHandler} (write side).
 *
 * <h2>Series membership (FR-11)</h2>
 *
 * The link is {@code dcat:inSeries}, which lives on the <em>Dataset</em> and points
 * at the series — so joining a series changes the dataset's file, not the series'.
 * That is why these operations re-project the dataset.
 */
public class DatasetSeriesAdminHandlerImpl extends DatasetSeriesReadOnlyHandlerImpl
		implements DatasetSeriesAdminHandler {

	/** How this service says it changed the portal; see DcatChangeNotifier. */
	private final DcatChangeNotifier changeNotifier;

	public DatasetSeriesAdminHandlerImpl(DcatChangeNotifier changeNotifier,
			DcatStoreService store,
			DcatGraphService graphService) {
		super(store);
		this.changeNotifier = changeNotifier;
		this.graphService = graphService;
	}

	/** For callers with no projection to reconcile - the tests, and a portal without SPARQL. */
	public DatasetSeriesAdminHandlerImpl(DcatChangeNotifier changeNotifier, DcatStoreService store) {
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
	public DatasetSeries upsertDatasetSeries(DatasetSeries series, Caller caller) {
		String id = idOrMint(series);
		DcatStoreSession storeSession = store(caller);
		storeSession.put(collection, id, series);
		// The entry's metadata changed, so its catalogue records move with it (#56).
		CatalogRecords.memberWritten(storeSession, StoreLayout.logicalIri(collection, id));
		storeSession.commit("Store dataset series " + id);
		reproject(DcatEntity.DATASET_SERIES, id);
		// After the commit: delivery is asynchronous, so a consumer can read the portal
		// before the operation that announced the change has finished it.
		changeNotifier.entityChanged(collection, id);
		return series;
	}

	@Override
	public List<String> deleteDatasetSeries(String id, boolean cascade, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		if (cascade) {
			// See CatalogRecords.memberDeleted: detach would otherwise leave records without a topic.
			CatalogRecords.memberDeleted(storeSession, StoreLayout.logicalIri(collection, id));
		}
		List<String> unlinked = References.detach(storeSession, collection, id, cascade);
		storeSession.delete(collection, id);
		// One commit for the delete and every unlink it caused; see CatalogAdminHandlerImpl.
		storeSession.commit(cascade ? "Delete dataset series " + id + " and unlink its referrers"
				: "Delete dataset series " + id);
		reproject(DcatEntity.DATASET_SERIES, id);
		changeNotifier.entityDeleted(collection, id);
		return unlinked;
	}

	@Override
	public DatasetSeries addDatasetToDatasetSeries(String datasetSeriesId, Dataset dataset, Caller caller) {
		if (dataset == null) {
			throw new IllegalArgumentException("Cannot add nothing to dataset series " + datasetSeriesId);
		}
		DcatStoreSession storeSession = store(caller);
		requireSeries(storeSession, datasetSeriesId);
		String datasetId = DcatIds.idForWrite(StoreLayout.DATASETS, dataset.getAbout());
		storeSession.put(StoreLayout.DATASETS, datasetId, dataset);
		return connect(storeSession, datasetSeriesId, datasetId,
				"Add dataset %s to dataset series %s".formatted(datasetId, datasetSeriesId));
	}

	@Override
	public DatasetSeries linkDatasetToDatasetSeries(String datasetSeriesId, String datasetId, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		requireSeries(storeSession, datasetSeriesId);
		if (storeSession.get(StoreLayout.DATASETS, datasetId).isEmpty()) {
			throw new NoSuchElementException("Unknown dataset: " + datasetId);
		}
		return connect(storeSession, datasetSeriesId, datasetId,
				"Link dataset %s to dataset series %s".formatted(datasetId, datasetSeriesId));
	}

	@Override
	public void deleteDatasetFromDatasetSeries(String datasetSeriesId, String datasetId, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> {
			if (Members.remove(dataset.getInSeries(), collection, datasetSeriesId)) {
				// A removal. See DcatStoreSession.saveRemoval.
				storeSession.saveRemoval(dataset);
				storeSession.commit("Unlink dataset %s from dataset series %s".formatted(datasetId, datasetSeriesId));
				reproject(DcatEntity.DATASET, datasetId);
				changeNotifier.entityUnlinked(StoreLayout.DATASETS, datasetId);
			}
		});
	}

	private DatasetSeries connect(DcatStoreSession storeSession, String datasetSeriesId, String datasetId, String message) {
		DatasetSeries series = requireSeries(storeSession, datasetSeriesId);
		Dataset dataset = storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId));
		boolean linked = false;
		if (!Members.contains(dataset.getInSeries(), collection, datasetSeriesId)) {
			dataset.getInSeries().add(series);
			storeSession.save(dataset);
			linked = true;
		}
		// Commits whatever the operation staged, as one commit; see CatalogAdminHandlerImpl.
		storeSession.commit(message);
		reproject(DcatEntity.DATASET, datasetId);
		// Only for a link that happened, and only once it is committed; see
		// CatalogAdminHandlerImpl.connect.
		if (linked) {
			changeNotifier.entityLinked(StoreLayout.DATASETS, datasetId);
		}
		return series;
	}

	private DatasetSeries requireSeries(DcatStoreSession storeSession, String datasetSeriesId) {
		return storeSession.<DatasetSeries>get(collection, datasetSeriesId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset series: " + datasetSeriesId));
	}

	private void reproject(DcatEntity entity, String id) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(entity, id);
		}
	}
}

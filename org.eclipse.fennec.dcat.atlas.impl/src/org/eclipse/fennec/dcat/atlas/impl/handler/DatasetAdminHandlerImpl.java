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

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.records.CatalogRecords;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Dataset;

/**
 * File-backed {@link DatasetAdminHandler} (write side).
 * <p>
 * A Dataset's Distributions are contained, so storing a Dataset stores them with
 * it — there is no separate distribution write to keep in step (FR-10).
 */
public class DatasetAdminHandlerImpl extends DatasetReadOnlyHandlerImpl implements DatasetAdminHandler {

	/** How this service says it changed the portal; see DcatChangeNotifier. */
	private final DcatChangeNotifier changeNotifier;

	public DatasetAdminHandlerImpl(DcatChangeNotifier changeNotifier,
			DcatStoreService store,
			DcatGraphService graphService) {
		super(store);
		this.changeNotifier = changeNotifier;
		this.graphService = graphService;
	}

	/** For callers with no projection to reconcile - the tests, and a portal without SPARQL. */
	public DatasetAdminHandlerImpl(DcatChangeNotifier changeNotifier, DcatStoreService store) {
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
	public Dataset upsertDataset(Dataset dataset, Caller caller) {
		String id = idOrMint(dataset);
		DcatStoreSession storeSession = store(caller);
		storeSession.put(collection, id, dataset);
		// The entry's metadata changed, so its catalogue records move with it (#56).
		CatalogRecords.memberWritten(storeSession, StoreLayout.logicalIri(collection, id));
		storeSession.commit("Store dataset " + id);
		reproject(id);
		// After the commit: delivery is asynchronous, so a consumer can read the portal
		// before the operation that announced the change has finished it.
		changeNotifier.entityChanged(collection, id);
		return dataset;
	}

	@Override
	public List<String> deleteDataset(String id, boolean cascade, Caller caller) {
		DcatStoreSession storeSession = store(caller);
		if (cascade) {
			// See CatalogRecords.memberDeleted: detach would otherwise leave records without a topic.
			CatalogRecords.memberDeleted(storeSession, StoreLayout.logicalIri(collection, id));
		}
		List<String> unlinked = References.detach(storeSession, collection, id, cascade);
		storeSession.delete(collection, id);
		// One commit for the delete and every unlink it caused; see CatalogAdminHandlerImpl.
		storeSession.commit(cascade ? "Delete dataset " + id + " and unlink its referrers" : "Delete dataset " + id);
		reproject(id);
		changeNotifier.entityDeleted(collection, id);
		return unlinked;
	}

	/** Re-projects one dataset into the RDF graph, if a projection is present. */
	private void reproject(String id) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.DATASET, id);
		}
	}
}

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

import java.util.NoSuchElementException;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.admin.DistributionAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.records.CatalogRecords;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.DataService;
import dcat.Dataset;
import dcat.Distribution;

/**
 * File-backed {@link DistributionAdminHandler} (write side).
 *
 * <h2>Every write is a dataset write (FR-10)</h2>
 *
 * Distributions are contained in their Dataset, so creating, changing or deleting
 * one rewrites the Dataset's file — there is no second store to keep in step, and
 * no way for the two to disagree about what belongs to what. A Distribution
 * therefore cannot outlive its Dataset, which is what FR-10 asks for.
 * <p>
 * {@code dcat:accessService} is the exception: it points at a DataService, which is
 * a catalog entity in its own right, so that stays a cross-resource reference.
 */
public class DistributionAdminHandlerImpl extends DistributionReadOnlyHandlerImpl implements DistributionAdminHandler {

	/** How this service says it changed the portal; see DcatChangeNotifier. */
	private final DcatChangeNotifier changeNotifier;

	public DistributionAdminHandlerImpl(DcatChangeNotifier changeNotifier,
			DcatStoreService store,
			DatasetReadOnlyHandler datasetService,
			DcatGraphService graphService) {
		super(store, datasetService);
		this.changeNotifier = changeNotifier;
		this.graphService = graphService;
	}

	/** For callers with no projection to reconcile - the tests, and a portal without SPARQL. */
	public DistributionAdminHandlerImpl(DcatChangeNotifier changeNotifier, DcatStoreService store,
			DatasetReadOnlyHandler datasetService) {
		this(changeNotifier, store, datasetService, null);
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
	public Distribution upsertDistributionToDataset(String datasetId, Distribution distribution, Caller caller) {
		if (distribution == null) {
			throw new IllegalArgumentException("Cannot store nothing in dataset " + datasetId);
		}
		DcatStoreSession storeSession = store.open(caller);
		Dataset dataset = requireDataset(storeSession, datasetId);

		// Mint only when the caller supplied no identity at all (D2/FR-3); one that is
		// not ours is refused rather than silently replaced — see DcatIds.idForWrite.
		String distributionId = DcatIds.distributionIdForWrite(datasetId, distribution.getAbout());
		distribution.setAbout(StoreLayout.distributionIri(datasetId, distributionId));

		// Replace in place so a repeated PUT does not accumulate copies.
		find(dataset, datasetId, distributionId).ifPresent(dataset.getDistribution()::remove);
		dataset.getDistribution().add(distribution);
		storeSession.save(dataset);
		// A distribution lives in its dataset's blob, so this changed what a catalogue entry
		// says about that dataset (#56).
		CatalogRecords.memberWritten(storeSession, StoreLayout.logicalIri(StoreLayout.DATASETS, datasetId));
		storeSession.commit("Store distribution %s of dataset %s".formatted(distributionId, datasetId));
		reproject(datasetId, distributionId);
		// A distribution has no collection of its own: it lives in its dataset's blob, so
		// what changed, as far as anyone outside this service can see, is the dataset.
		changeNotifier.entityChanged(StoreLayout.DATASETS, datasetId);
		return distribution;
	}

	@Override
	public void deleteDistributionFromDataset(String datasetId, String distributionId, Caller caller) {
		DcatStoreSession storeSession = store.open(caller);
		storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> {
			if (find(dataset, datasetId, distributionId).map(dataset.getDistribution()::remove).orElse(false)) {
				// A removal: never refused by what it is removing. See DcatStoreSession.saveRemoval.
				storeSession.saveRemoval(dataset);
				CatalogRecords.memberWritten(storeSession, StoreLayout.logicalIri(StoreLayout.DATASETS, datasetId));
				storeSession.commit("Delete distribution %s of dataset %s".formatted(distributionId, datasetId));
				reproject(datasetId, distributionId);
				changeNotifier.entityChanged(StoreLayout.DATASETS, datasetId);
			}
		});
	}

	// --- FR-10 accessService link ------------------------------------------

	@Override
	public Distribution addAccessServiceToDistribution(String datasetId, String distributionId,
			DataService dataService, Caller caller) {
		if (dataService == null) {
			throw new IllegalArgumentException("Cannot link nothing as an access service");
		}
		DcatStoreSession storeSession = store.open(caller);
		requireDistribution(storeSession, datasetId, distributionId);
		String serviceId = DcatIds.idForWrite(StoreLayout.DATA_SERVICES, dataService.getAbout());
		storeSession.put(StoreLayout.DATA_SERVICES, serviceId, dataService);
		return connect(storeSession, datasetId, distributionId, serviceId,
				"Add access service %s to distribution %s of dataset %s".formatted(serviceId, distributionId,
						datasetId));
	}

	@Override
	public Distribution linkAccessServiceToDistribution(String datasetId, String distributionId,
			String dataServiceId, Caller caller) {
		DcatStoreSession storeSession = store.open(caller);
		requireDistribution(storeSession, datasetId, distributionId);
		if (storeSession.get(StoreLayout.DATA_SERVICES, dataServiceId).isEmpty()) {
			throw new NoSuchElementException("Unknown data service: " + dataServiceId);
		}
		return connect(storeSession, datasetId, distributionId, dataServiceId,
				"Link access service %s to distribution %s of dataset %s".formatted(dataServiceId, distributionId,
						datasetId));
	}

	@Override
	public void deleteAccessServiceFromDistribution(String datasetId, String distributionId, String dataServiceId,
			Caller caller) {
		DcatStoreSession storeSession = store.open(caller);
		storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> find(dataset, datasetId,
				distributionId).ifPresent(distribution -> {
					if (Members.remove(distribution.getAccessService(), StoreLayout.DATA_SERVICES, dataServiceId)) {
						// A removal. See DcatStoreSession.saveRemoval.
						storeSession.saveRemoval(dataset);
						storeSession.commit("Unlink access service %s from distribution %s of dataset %s"
								.formatted(dataServiceId, distributionId, datasetId));
						reproject(datasetId, distributionId);
						changeNotifier.entityUnlinked(StoreLayout.DATA_SERVICES, dataServiceId);
					}
				}));
	}

	// --- helpers ------------------------------------------------------------

	private Distribution connect(DcatStoreSession storeSession, String datasetId, String distributionId, String dataServiceId,
			String message) {
		Dataset dataset = requireDataset(storeSession, datasetId);
		Distribution distribution = requireDistribution(storeSession, datasetId, distributionId);
		boolean linked = false;
		if (!Members.contains(distribution.getAccessService(), StoreLayout.DATA_SERVICES, dataServiceId)) {
			distribution.getAccessService().add(storeSession.<DataService>get(StoreLayout.DATA_SERVICES, dataServiceId)
					.orElseThrow(() -> new NoSuchElementException("Unknown data service: " + dataServiceId)));
			storeSession.save(dataset);
			linked = true;
		}
		// Commits whatever the operation staged, as one commit; a repeat stages nothing and
		// so leaves the dataset - and its ETag - untouched. See CatalogAdminHandlerImpl.
		storeSession.commit(message);
		reproject(datasetId, distributionId);
		// Only for a link that happened, and only once it is committed; see
		// CatalogAdminHandlerImpl.connect.
		if (linked) {
			changeNotifier.entityLinked(StoreLayout.DATA_SERVICES, dataServiceId);
		}
		return distribution;
	}

	private static Dataset requireDataset(DcatStoreSession storeSession, String datasetId) {
		return storeSession.<Dataset>get(StoreLayout.DATASETS, datasetId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId));
	}

	private static Distribution requireDistribution(DcatStoreSession storeSession, String datasetId, String distributionId) {
		return find(requireDataset(storeSession, datasetId), datasetId, distributionId)
				.orElseThrow(() -> new NoSuchElementException(
						"Unknown distribution: " + distributionId + " in dataset: " + datasetId));
	}

	/**
	 * Re-projects one distribution into the RDF graph, if a projection is present.
	 * The owning dataset is required: a distribution is only resolvable through it.
	 */
	private void reproject(String datasetId, String distributionId) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.DISTRIBUTION, datasetId, distributionId);
		}
	}
}

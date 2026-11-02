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
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DistributionReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Dataset;
import dcat.Distribution;

/**
 * Read-only view of the Distributions of a Dataset.
 *
 * <h2>No store of its own (FR-10)</h2>
 *
 * {@code dcat:distribution} is containment, so a Distribution is part of its
 * Dataset's file and is read by reading the Dataset. That is also why there is no
 * {@code store:distributions} readiness check and no distribution directory: there
 * is nothing separate that could be unavailable.
 * <p>
 * The previous design kept Distributions in their own store and reconstructed the
 * relationship from a URI reference, which let the model and the store disagree
 * about who owns what — a Distribution could outlive its Dataset, or be reachable
 * from two.
 */
public class DistributionReadOnlyHandlerImpl implements DistributionReadOnlyHandler {

	/**
	 * Two collaborators rather than one: a Distribution is read through its Dataset,
	 * which is what {@code datasetService} answers, while the ETag and the admin
	 * subclass's writes go to the store directly.
	 */
	protected final DcatStoreService store;
	protected final DatasetReadOnlyHandler datasetService;

	public DistributionReadOnlyHandlerImpl(DcatStoreService store,
			DatasetReadOnlyHandler datasetService) {
		this.store = store;
		this.datasetService = datasetService;
	}

	@Override
	public Optional<Distribution> getDistributionForDataset(String datasetId, String distributionId) {
		return datasetService.getDataset(datasetId).flatMap(dataset -> find(dataset, datasetId, distributionId));
	}

	@Override
	public List<Distribution> listDistributionsForDataset(String datasetId) {
		return datasetService.getDataset(datasetId) //
				.map(dataset -> List.copyOf(dataset.getDistribution())) //
				.orElseGet(List::of);
	}

	/**
	 * The owning dataset's ETag: a distribution has no stored bytes of its own, so a
	 * change to it <em>is</em> a change to the dataset.
	 */
	@Override
	public Optional<String> etag(String datasetId, String distributionId) {
		if (getDistributionForDataset(datasetId, distributionId).isEmpty()) {
			return Optional.empty();
		}
		return store.etag(StoreLayout.DATASETS, datasetId);
	}

	/** The Distribution of {@code dataset} whose identity ends in {@code distributionId}. */
	protected static Optional<Distribution> find(Dataset dataset, String datasetId, String distributionId) {
		if (distributionId == null) {
			return Optional.empty();
		}
		return dataset.getDistribution().stream() //
				.filter(distribution -> distributionId
						.equals(StoreLayout.distributionIdOf(datasetId, distribution.getAbout()))) //
				.findFirst();
	}
}

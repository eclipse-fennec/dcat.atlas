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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

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
@Component(name = "DistributionReadOnlyService", service = DistributionReadOnlyService.class)
@Designate(ocd = StoreConfig.class)
public class DistributionReadOnlyServiceImpl implements DistributionReadOnlyService {

	protected final ResourceSetFactory resourceSetFactory;
	protected final Path root;
	protected final DatasetReadOnlyService datasetService;
	/**
	 * Whether writes through this store are checked against the model's constraints
	 * ({@link StoreConfig#validateOnWrite()}). This service does not extend
	 * {@code AbstractEntityStore} — a Distribution has no store of its own — so it
	 * carries the flag itself, for {@code DistributionAdminServiceImpl.store()}.
	 */
	protected final boolean validateOnWrite;

	@Activate
	public DistributionReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference DatasetReadOnlyService datasetService, StoreConfig config) {
		this(resourceSetFactory, Path.of(config.root()), datasetService, config.validateOnWrite());
	}

	/** Package-visible for the admin subclass and tests; writes are not validated. */
	DistributionReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path root,
			DatasetReadOnlyService datasetService) {
		this(resourceSetFactory, root, datasetService, false);
	}

	/** Package-visible for the admin subclass and tests. */
	DistributionReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path root,
			DatasetReadOnlyService datasetService, boolean validateOnWrite) {
		this.resourceSetFactory = resourceSetFactory;
		this.root = root;
		this.datasetService = datasetService;
		this.validateOnWrite = validateOnWrite;
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
		return DcatHelper.etag(root, StoreLayout.DATASETS, datasetId);
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

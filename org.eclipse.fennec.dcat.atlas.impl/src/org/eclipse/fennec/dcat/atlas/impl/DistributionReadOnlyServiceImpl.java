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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreHealth;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;
import dcat.DcatPackage;
import dcat.Distribution;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@Component(name = "DistributionReadOnlyService", service = { DistributionReadOnlyService.class, HealthCheck.class }, property = {
		HealthCheck.NAME + "=store:distributions", HealthCheck.TAGS + "=ready" })
@Designate(ocd = StoreConfig.class)
public class DistributionReadOnlyServiceImpl implements DistributionReadOnlyService, HealthCheck {
	
	protected final ResourceSetFactory resourceSetFactory;
	protected final Path directory;

	/**
	 * A Distribution only exists in the context of a Dataset (FR-10). The
	 * dataset->distribution link is modelled as a {@code dcat:distribution} URI
	 * reference on the Dataset, so resolving a dataset's distributions means
	 * reading the dataset and looking the referenced ids up in this store.
	 */
	protected final DatasetReadOnlyService datasetService;

	@Activate
	public DistributionReadOnlyServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference DatasetReadOnlyService datasetService, StoreConfig config) {
		this(resourceSetFactory, Path.of(config.directory()), datasetService);
	}

	/** Package-visible for the admin subclass and tests. */
	DistributionReadOnlyServiceImpl(ResourceSetFactory resourceSetFactory, Path directory,
			DatasetReadOnlyService datasetService) {
		this.resourceSetFactory = resourceSetFactory;
		this.directory = directory;
		this.datasetService = datasetService;
		try {
			Files.createDirectories(directory);
		} catch (IOException e) {
			throw new UncheckedIOException("Could not create catalog storage directory " + directory, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService#getDistributionForDataset(java.lang.String, java.lang.String)
	 */
	@Override
	public Optional<Distribution> getDistributionForDataset(String datasetId, String distributionId) {
		return datasetService.getDataset(datasetId) //
				.filter(dataset -> referencesDistribution(dataset, distributionId)) //
				.flatMap(dataset -> DcatHelper.get(resourceSetFactory, directory, distributionId,
						DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService#listDistributionsForDataset(java.lang.String)
	 */
	@Override
	public List<Distribution> listDistributionsForDataset(String datasetId) {
		return datasetService.getDataset(datasetId) //
				.map(dataset -> dataset.getDistribution().stream() //
						.map(ref -> DcatHelper.idOf(ref.getResource())) //
						.filter(Objects::nonNull) //
						.map(id -> DcatHelper.<Distribution>get(resourceSetFactory, directory, id,
								DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION)) //
						.filter(Optional::isPresent).map(Optional::get) //
						.collect(Collectors.toList())) //
				.orElseGet(List::of);
	}

	@Override
	public Optional<String> etag(String id) {
		return DcatHelper.etag(directory, id);
	}

	/** Whether {@code dataset} carries a {@code dcat:distribution} reference to {@code distributionId}. */
	protected static boolean referencesDistribution(Dataset dataset, String distributionId) {
		return dataset.getDistribution().stream()
				.anyMatch(ref -> distributionId.equals(DcatHelper.idOf(ref.getResource())));
	}

	// --- F-25 readiness -----------------------------------------------------

	/**
	 * Reports whether this store can serve (F-25). CRITICAL rather than WARN: an
	 * unusable store directory is a misconfiguration that no retry fixes, and the
	 * portal should be taken out of rotation.
	 */
	@Override
	public Result execute() {
		FormattingResultLog log = new FormattingResultLog();
		if (StoreHealth.ready(directory)) {
			log.info("{}", StoreHealth.detail(directory));
		} else {
			log.critical("{}", StoreHealth.detail(directory));
		}
		return new Result(log);
	}

}

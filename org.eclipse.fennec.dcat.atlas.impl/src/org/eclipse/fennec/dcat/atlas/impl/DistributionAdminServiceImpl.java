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
import java.util.NoSuchElementException;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;
import dcat.DcatPackage;
import dcat.Distribution;
import rdf.RdfFactory;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@Component(name = "DistributionAdminService", service = DistributionAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DistributionAdminServiceImpl extends DistributionReadOnlyServiceImpl
		implements DistributionAdminService {

	/**
	 * The write-side Dataset service: the distribution store keeps the
	 * {@code Distribution} resource, but the dataset->distribution link lives on
	 * the Dataset, so create/delete also updates the owning dataset (FR-10). It
	 * is a {@link DatasetAdminService}, which is a {@code DatasetReadOnlyService},
	 * so it also satisfies the read-side dependency of the superclass.
	 */
	private final DatasetAdminService datasetAdminService;

	@Activate
	public DistributionAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference DatasetAdminService datasetAdminService, StoreConfig config) {
		super(resourceSetFactory, datasetAdminService, config);
		this.datasetAdminService = datasetAdminService;
	}

	/** Package-visible for tests. */
	DistributionAdminServiceImpl(ResourceSetFactory resourceSetFactory, Path directory,
			DatasetAdminService datasetAdminService) {
		super(resourceSetFactory, directory, datasetAdminService);
		this.datasetAdminService = datasetAdminService;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionAdminService#upsertDistributionToDataset(java.lang.String, dcat.Distribution)
	 */
	@Override
	public Distribution upsertDistributionToDataset(String datasetId, Distribution distribution) {
		// FR-10: a Distribution cannot exist without a Dataset.
		Dataset dataset = requireDataset(datasetId);
		String id = DcatHelper.idOf(distribution.getAbout());
		if (id == null) {
			// Mint an id when the client supplied no about (D2/FR-3).
			id = UUID.randomUUID().toString();
		}
		DcatHelper.write(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__DISTRIBUTION, distribution);
		linkToDataset(dataset, distribution.getAbout());
		return distribution;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DistributionAdminService#deleteDistributionFromDataset(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteDistributionFromDataset(String datasetId, String distributionId) {
		datasetAdminService.getDataset(datasetId).ifPresent(dataset -> {
			if (dataset.getDistribution()
					.removeIf(ref -> distributionId.equals(DcatHelper.idOf(ref.getResource())))) {
				datasetAdminService.upsertDataset(dataset);
			}
		});
		DcatHelper.delete(directory, distributionId);
	}

	private Dataset requireDataset(String datasetId) {
		return datasetAdminService.getDataset(datasetId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId));
	}

	/** Adds a {@code dcat:distribution} reference to the dataset if it is not already present. */
	private void linkToDataset(Dataset dataset, String distributionAbout) {
		if (distributionAbout == null) {
			return;
		}
		boolean linked = dataset.getDistribution().stream()
				.anyMatch(ref -> distributionAbout.equals(ref.getResource()));
		if (!linked) {
			rdf.Resource ref = RdfFactory.eINSTANCE.createResource();
			ref.setResource(distributionAbout);
			dataset.getDistribution().add(ref);
			datasetAdminService.upsertDataset(dataset);
		}
	}

}

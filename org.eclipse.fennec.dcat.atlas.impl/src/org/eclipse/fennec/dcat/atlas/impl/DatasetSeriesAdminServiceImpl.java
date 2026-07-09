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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatPackage;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@Component(name = "DataSeriesAdminService", service = DatasetSeriesAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DatasetSeriesAdminServiceImpl extends DatasetSeriesReadOnlyServiceImpl implements DatasetSeriesAdminService{

	/**
	 * Series membership is modelled as {@code dcat:inSeries} on the Dataset
	 * (there is no {@code seriesMember} back-reference on DatasetSeries in this
	 * model), so assigning/removing a dataset to/from a series edits the Dataset
	 * and stores it through the write-side Dataset service (FR-11).
	 */
	private final DatasetAdminService datasetAdminService;

	@Activate
	public DatasetSeriesAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference DatasetAdminService datasetAdminService, StoreConfig config) {
		super(resourceSetFactory, config);
		this.datasetAdminService = datasetAdminService;
	}

	/** Package-visible for tests. */
	DatasetSeriesAdminServiceImpl(ResourceSetFactory resourceSetFactory, Path directory,
			DatasetAdminService datasetAdminService) {
		super(resourceSetFactory, directory);
		this.datasetAdminService = datasetAdminService;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DataSeriesAdminService#upsertDatasetSeries(dcat.DatasetSeries)
	 */
	@Override
	public DatasetSeries upsertDatasetSeries(DatasetSeries series) {
		String id = DcatHelper.idOf(series.getAbout());
		if (id == null) {
			// Mint an id when the client supplied no about (D2/FR-3).
			id = UUID.randomUUID().toString();
		}
		DcatHelper.write(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__DATASET_SERIES, series);
		return series;
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DataSeriesAdminService#deleteDatasetSeries(java.lang.String, boolean)
	 */
	@Override
	public void deleteDatasetSeries(String id, boolean cascade) {
		// TODO FR-1: 409 when the catalog is still referenced; cascade currently ignored.
		DcatHelper.delete(directory, id);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService#addDatasetToDatasetSeries(java.lang.String, dcat.Dataset)
	 */
	@Override
	public DatasetSeries addDatasetToDatasetSeries(String datasetSeriesId, Dataset dataset) {
		DatasetSeries series = getDatasetSeries(datasetSeriesId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset series: " + datasetSeriesId));
		boolean present = dataset.getInSeries().stream()
				.anyMatch(s -> datasetSeriesId.equals(DcatHelper.idOf(s.getAbout())));
		if (!present) {
			dataset.getInSeries().add(EcoreUtil.copy(series));
		}
		datasetAdminService.upsertDataset(dataset);
		return series;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService#deleteDatasetFromDatasetSeries(java.lang.String, java.lang.String)
	 */
	@Override
	public void deleteDatasetFromDatasetSeries(String datasetSeriesId, String datasetId) {
		datasetAdminService.getDataset(datasetId).ifPresent(dataset -> {
			if (dataset.getInSeries().removeIf(s -> datasetSeriesId.equals(DcatHelper.idOf(s.getAbout())))) {
				datasetAdminService.upsertDataset(dataset);
			}
		});
	}

}

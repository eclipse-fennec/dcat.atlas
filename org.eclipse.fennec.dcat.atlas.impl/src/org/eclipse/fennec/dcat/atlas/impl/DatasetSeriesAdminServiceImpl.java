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

import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService;
import org.eclipse.fennec.dcat.atlas.api.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.helper.Members;
import org.eclipse.fennec.dcat.atlas.impl.helper.References;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Dataset;
import dcat.DatasetSeries;

/**
 * File-backed {@link DatasetSeriesAdminService} (write side).
 *
 * <h2>Series membership (FR-11)</h2>
 *
 * The link is {@code dcat:inSeries}, which lives on the <em>Dataset</em> and points
 * at the series — so joining a series changes the dataset's file, not the series'.
 * That is why these operations re-project the dataset.
 */
@Component(name = "DatasetSeriesAdminService", service = DatasetSeriesAdminService.class)
@Designate(ocd = StoreConfig.class)
public class DatasetSeriesAdminServiceImpl extends DatasetSeriesReadOnlyServiceImpl
		implements DatasetSeriesAdminService {

	@Activate
	public DatasetSeriesAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		super(resourceSetFactory, config);
	}

	/** Package-visible for tests. */
	DatasetSeriesAdminServiceImpl(ResourceSetFactory resourceSetFactory, Path root) {
		super(resourceSetFactory, root);
	}

	/**
	 * The RDF projection behind the SPARQL endpoint, maintained here rather than in
	 * the REST layer: this service is the persistence boundary, and REST is only one
	 * of its callers (persistence plan constraint G2). Optional and dynamic — absent
	 * simply means no SPARQL.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graphService;

	@Override
	public DatasetSeries upsertDatasetSeries(DatasetSeries series) {
		String id = idOrMint(series);
		store().put(collection, id, series);
		reproject(DcatEntity.DATASET_SERIES, id);
		return series;
	}

	@Override
	public void deleteDatasetSeries(String id, boolean cascade) {
		Store store = store();
		References.detach(store, root, collection, id, cascade);
		store.delete(collection, id);
		reproject(DcatEntity.DATASET_SERIES, id);
	}

	@Override
	public DatasetSeries addDatasetToDatasetSeries(String datasetSeriesId, Dataset dataset) {
		if (dataset == null) {
			throw new IllegalArgumentException("Cannot add nothing to dataset series " + datasetSeriesId);
		}
		Store store = store();
		requireSeries(store, datasetSeriesId);
		String datasetId = DcatIds.idForWrite(StoreLayout.DATASETS, dataset.getAbout());
		store.put(StoreLayout.DATASETS, datasetId, dataset);
		return connect(store, datasetSeriesId, datasetId);
	}

	@Override
	public DatasetSeries linkDatasetToDatasetSeries(String datasetSeriesId, String datasetId) {
		Store store = store();
		requireSeries(store, datasetSeriesId);
		if (store.get(StoreLayout.DATASETS, datasetId).isEmpty()) {
			throw new NoSuchElementException("Unknown dataset: " + datasetId);
		}
		return connect(store, datasetSeriesId, datasetId);
	}

	@Override
	public void deleteDatasetFromDatasetSeries(String datasetSeriesId, String datasetId) {
		Store store = store();
		store.<Dataset>get(StoreLayout.DATASETS, datasetId).ifPresent(dataset -> {
			if (Members.remove(dataset.getInSeries(), collection, datasetSeriesId)) {
				store.save(dataset);
				reproject(DcatEntity.DATASET, datasetId);
			}
		});
	}

	private DatasetSeries connect(Store store, String datasetSeriesId, String datasetId) {
		DatasetSeries series = requireSeries(store, datasetSeriesId);
		Dataset dataset = store.<Dataset>get(StoreLayout.DATASETS, datasetId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset: " + datasetId));
		if (!Members.contains(dataset.getInSeries(), collection, datasetSeriesId)) {
			dataset.getInSeries().add(series);
			store.save(dataset);
			reproject(DcatEntity.DATASET, datasetId);
		}
		return series;
	}

	private DatasetSeries requireSeries(Store store, String datasetSeriesId) {
		return store.<DatasetSeries>get(collection, datasetSeriesId)
				.orElseThrow(() -> new NoSuchElementException("Unknown dataset series: " + datasetSeriesId));
	}

	private void reproject(DcatEntity entity, String id) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(entity, id);
		}
	}
}

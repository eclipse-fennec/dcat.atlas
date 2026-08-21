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
package org.eclipse.fennec.dcat.atlas.sparql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.Page;
import org.eclipse.fennec.dcat.atlas.api.PageRequest;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;

/**
 * In-memory stand-ins for the five read services, so the projection can be tested
 * against a store whose contents the test controls outright. Deliberately not
 * mocks: the projection's whole job is to mirror what these return, and a map is
 * both clearer and harder to get subtly wrong than recorded interactions.
 */
final class FakeStores {

	final Map<String, Catalog> catalogs = new LinkedHashMap<>();
	final Map<String, Dataset> datasets = new LinkedHashMap<>();
	final Map<String, DatasetSeries> series = new LinkedHashMap<>();
	final Map<String, DataService> dataServices = new LinkedHashMap<>();

	/** distributions, keyed by dataset id then distribution id (FR-10 scoping). */
	final Map<String, Map<String, Distribution>> distributions = new LinkedHashMap<>();

	void putDistribution(String datasetId, String distributionId, Distribution distribution) {
		distributions.computeIfAbsent(datasetId, k -> new LinkedHashMap<>()).put(distributionId, distribution);
	}

	void removeDistribution(String datasetId, String distributionId) {
		Map<String, Distribution> forDataset = distributions.get(datasetId);
		if (forDataset != null) {
			forDataset.remove(distributionId);
		}
	}

	/**
	 * How many times the catalog store has been listed. A projection pass lists every
	 * store exactly once, so this counts passes — which is what lets a test assert that a
	 * reconcile tick did no work.
	 */
	int catalogReads;

	CatalogReadOnlyService catalogService() {
		return new CatalogReadOnlyService() {
			@Override
			public Optional<Catalog> getCatalog(String id) {
				return Optional.ofNullable(catalogs.get(id));
			}

			@Override
			public List<Catalog> listCatalogs() {
				catalogReads++;
				return new ArrayList<>(catalogs.values());
			}

			@Override
			public Page<Catalog> listCatalogs(PageRequest page) {
				// The projection reads whole collections; nothing here should ever page.
				throw new UnsupportedOperationException("the graph projection does not page");
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}
		};
	}

	DatasetReadOnlyService datasetService() {
		return new DatasetReadOnlyService() {
			@Override
			public Optional<Dataset> getDataset(String id) {
				return Optional.ofNullable(datasets.get(id));
			}

			@Override
			public List<Dataset> listDatasets() {
				return new ArrayList<>(datasets.values());
			}

			@Override
			public Page<Dataset> listDatasets(PageRequest page) {
				// The projection reads whole collections; nothing here should ever page.
				throw new UnsupportedOperationException("the graph projection does not page");
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}
		};
	}

	DatasetSeriesReadOnlyService seriesService() {
		return new DatasetSeriesReadOnlyService() {
			@Override
			public Optional<DatasetSeries> getDatasetSeries(String id) {
				return Optional.ofNullable(series.get(id));
			}

			@Override
			public List<DatasetSeries> listDatasetSeries() {
				return new ArrayList<>(series.values());
			}

			@Override
			public Page<DatasetSeries> listDatasetSeries(PageRequest page) {
				// The projection reads whole collections; nothing here should ever page.
				throw new UnsupportedOperationException("the graph projection does not page");
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}
		};
	}

	DataServiceReadOnlyService dataServiceService() {
		return new DataServiceReadOnlyService() {
			@Override
			public Optional<DataService> getDataService(String id) {
				return Optional.ofNullable(dataServices.get(id));
			}

			@Override
			public List<DataService> listDataServices() {
				return new ArrayList<>(dataServices.values());
			}

			@Override
			public Page<DataService> listDataServices(PageRequest page) {
				// The projection reads whole collections; nothing here should ever page.
				throw new UnsupportedOperationException("the graph projection does not page");
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}
		};
	}

	DistributionReadOnlyService distributionService() {
		return new DistributionReadOnlyService() {
			@Override
			public Optional<Distribution> getDistributionForDataset(String datasetId, String distributionId) {
				return Optional.ofNullable(distributions.getOrDefault(datasetId, Map.of()).get(distributionId));
			}

			@Override
			public List<Distribution> listDistributionsForDataset(String datasetId) {
				return new ArrayList<>(distributions.getOrDefault(datasetId, Map.of()).values());
			}

			@Override
			public Optional<String> etag(String datasetId, String distributionId) {
				return Optional.empty();
			}
		};
	}
}

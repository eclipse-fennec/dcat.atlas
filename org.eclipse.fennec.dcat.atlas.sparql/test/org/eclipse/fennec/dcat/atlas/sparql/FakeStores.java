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

import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DistributionReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.DcatReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;

/**
 * An in-memory read side, so the projection can be tested against a store whose contents
 * the test controls outright. Deliberately not mocks: the projection's whole job is to
 * mirror what these return, and a map is both clearer and harder to get subtly wrong than
 * recorded interactions.
 * <p>
 * It is the facade the projection actually binds, which is also how a test controls the
 * store revision the reconcile poll compares — see {@link #revision}.
 */
final class FakeStores implements DcatReadOnlyService {

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

	CatalogReadOnlyHandler catalogService() {
		return new CatalogReadOnlyHandler() {
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

			@Override
			public List<String> ids() {
				// Deliberately not counted: the projection reads whole collections, so a
				// listing here would not be one of the passes catalogReads counts.
				return new ArrayList<>(catalogs.keySet());
			}
		};
	}

	DatasetReadOnlyHandler datasetService() {
		return new DatasetReadOnlyHandler() {
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

			@Override
			public List<String> ids() {
				// Deliberately not counted: the projection reads whole collections, so a
				// listing here would not be one of the passes the read counters counts.
				return new ArrayList<>(datasets.keySet());
			}
		};
	}

	DatasetSeriesReadOnlyHandler seriesService() {
		return new DatasetSeriesReadOnlyHandler() {
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

			@Override
			public List<String> ids() {
				// Deliberately not counted: the projection reads whole collections, so a
				// listing here would not be one of the passes the read counters counts.
				return new ArrayList<>(series.keySet());
			}
		};
	}

	DataServiceReadOnlyHandler dataServiceService() {
		return new DataServiceReadOnlyHandler() {
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

			@Override
			public List<String> ids() {
				// Deliberately not counted: the projection reads whole collections, so a
				// listing here would not be one of the passes the read counters counts.
				return new ArrayList<>(dataServices.keySet());
			}
		};
	}

	DistributionReadOnlyHandler distributionService() {
		return new DistributionReadOnlyHandler() {
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

	// --- the facade --------------------------------------------------------

	/**
	 * What {@link #revision()} answers. Null means the store cannot say, which a test uses
	 * to check that the reconcile optimisation is optional.
	 */
	String revision;

	/** Set to make {@link #revision()} throw, for the "fingerprint unreadable" path. */
	RuntimeException revisionFailure;

	@Override
	public Optional<String> revision() {
		if (revisionFailure != null) {
			throw revisionFailure;
		}
		return Optional.ofNullable(revision);
	}

	@Override
	public CatalogReadOnlyHandler getCatalogReadOnlyHandler() {
		return catalogService();
	}

	@Override
	public DatasetReadOnlyHandler getDatasetReadOnlyHandler() {
		return datasetService();
	}

	@Override
	public DatasetSeriesReadOnlyHandler getDataSeriesReadOnlyHandler() {
		return seriesService();
	}

	@Override
	public DataServiceReadOnlyHandler getDataServiceReadOnlyHandler() {
		return dataServiceService();
	}

	@Override
	public DistributionReadOnlyHandler getDistributionReadOnlyHandler() {
		return distributionService();
	}
}

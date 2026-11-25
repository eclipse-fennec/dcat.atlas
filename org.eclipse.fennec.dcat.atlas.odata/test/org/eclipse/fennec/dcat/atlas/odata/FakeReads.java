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
package org.eclipse.fennec.dcat.atlas.odata;

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;

/**
 * In-memory stand-ins for the four read services, holding whatever a test hands them.
 * <p>
 * Deliberately plain lists rather than a store: what is under test is the snapshot the
 * repository derives, not how the entities were read.
 */
final class FakeReads {

	/** The public base a test's expectations are written against. */
	static final String PUBLIC_BASE = "https://portal.example/rest/";

	private FakeReads() {
	}

	/** Maps the logical base onto {@link #PUBLIC_BASE} and leaves anything else alone. */
	static PublicIris publicIris() {
		return new PublicIris() {

			@Override
			public String toPublic(String iri) {
				return iri != null && iri.startsWith(DcatIds.LOGICAL_BASE)
						? PUBLIC_BASE + iri.substring(DcatIds.LOGICAL_BASE.length())
						: iri;
			}

			@Override
			public String toLogical(String iri) {
				return iri != null && iri.startsWith(PUBLIC_BASE)
						? DcatIds.LOGICAL_BASE + iri.substring(PUBLIC_BASE.length())
						: iri;
			}

			@Override
			public boolean isOwned(String iri) {
				return iri != null && (iri.startsWith(DcatIds.LOGICAL_BASE) || iri.startsWith(PUBLIC_BASE));
			}

			@Override
			public String publicBase() {
				return PUBLIC_BASE;
			}
		};
	}

	static CatalogReadOnlyService catalogs(List<Catalog> entries) {
		return new CatalogReadOnlyService() {

			@Override
			public Optional<Catalog> getCatalog(String id) {
				return entries.stream().filter(c -> c.getAbout().endsWith("/" + id)).findFirst();
			}

			@Override
			public List<Catalog> listCatalogs() {
				return entries;
			}

			@Override
			public Page<Catalog> listCatalogs(PageRequest page) {
				return new Page<>(entries, null, entries.size());
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}

			@Override
			public List<String> ids() {
				return entries.stream().map(c -> DcatIds.idOf(DcatIds.CATALOGS, c.getAbout())).toList();
			}
		};
	}

	static DatasetReadOnlyService datasets(List<Dataset> entries) {
		return new DatasetReadOnlyService() {

			@Override
			public Optional<Dataset> getDataset(String id) {
				return entries.stream().filter(d -> d.getAbout().endsWith("/" + id)).findFirst();
			}

			@Override
			public List<Dataset> listDatasets() {
				return entries;
			}

			@Override
			public Page<Dataset> listDatasets(PageRequest page) {
				return new Page<>(entries, null, entries.size());
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}

			@Override
			public List<String> ids() {
				return entries.stream().map(d -> DcatIds.idOf(DcatIds.DATASETS, d.getAbout())).toList();
			}
		};
	}

	static DatasetSeriesReadOnlyService series(List<DatasetSeries> entries) {
		return new DatasetSeriesReadOnlyService() {

			@Override
			public Optional<DatasetSeries> getDatasetSeries(String id) {
				return entries.stream().filter(s -> s.getAbout().endsWith("/" + id)).findFirst();
			}

			@Override
			public List<DatasetSeries> listDatasetSeries() {
				return entries;
			}

			@Override
			public Page<DatasetSeries> listDatasetSeries(PageRequest page) {
				return new Page<>(entries, null, entries.size());
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}

			@Override
			public List<String> ids() {
				return entries.stream().map(s -> DcatIds.idOf(DcatIds.DATASET_SERIES, s.getAbout())).toList();
			}
		};
	}

	static DataServiceReadOnlyService dataServices(List<DataService> entries) {
		return new DataServiceReadOnlyService() {

			@Override
			public Optional<DataService> getDataService(String id) {
				return entries.stream().filter(s -> s.getAbout().endsWith("/" + id)).findFirst();
			}

			@Override
			public List<DataService> listDataServices() {
				return entries;
			}

			@Override
			public Page<DataService> listDataServices(PageRequest page) {
				return new Page<>(entries, null, entries.size());
			}

			@Override
			public Optional<String> etag(String id) {
				return Optional.empty();
			}

			@Override
			public List<String> ids() {
				return entries.stream().map(s -> DcatIds.idOf(DcatIds.DATA_SERVICES, s.getAbout())).toList();
			}
		};
	}
}

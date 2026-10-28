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
package org.eclipse.fennec.dcat.atlas.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogCollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

/**
 * What a snapshot becomes, as metric points.
 *
 * <p>
 * The point computation is tested rather than the instruments: an OpenTelemetry gauge is a
 * callback the SDK invokes, so the part worth checking is what the callback would record.
 * </p>
 */
class DcatAtlasTelemetryTest {

	private static final AttributeKey<String> COLLECTION = AttributeKey.stringKey("fennec.collection");
	private static final AttributeKey<String> CATALOG = AttributeKey.stringKey("fennec.catalog");

	@Test
	void eachCollectionIsItsOwnSeries() {
		DcatAtlasRuntimeDTO snapshot = snapshot(3, 12, 2, 0);

		Map<Attributes, Long> points = DcatAtlasTelemetry.resourcesPerCollection(snapshot);

		assertEquals(4, points.size(), "one series per stored DCAT collection");
		assertEquals(3L, points.get(Attributes.of(COLLECTION, "catalogs")));
		assertEquals(12L, points.get(Attributes.of(COLLECTION, "datasets")));
		assertEquals(2L, points.get(Attributes.of(COLLECTION, "data-services")));
	}

	/**
	 * An empty collection reports its zero.
	 *
	 * <p>
	 * A zero and a missing series are different things to every backend: if a collection
	 * were published only once it held something, "no datasets" and "the exporter died"
	 * would be the same observation.
	 * </p>
	 */
	@Test
	void anEmptyCollectionStillReportsItsZero() {
		Map<Attributes, Long> points = DcatAtlasTelemetry.resourcesPerCollection(snapshot(0, 0, 0, 0));

		assertEquals(4, points.size());
		assertEquals(0L, points.get(Attributes.of(COLLECTION, "dataset-series")));
	}

	/**
	 * A snapshot whose parts are absent produces points rather than an exception.
	 * <p>
	 * The exporter calls these on a timer, and a portal that has not finished coming up is
	 * exactly when someone is watching the metrics.
	 */
	@Test
	void aSnapshotWithNothingInItDoesNotThrow() {
		DcatAtlasRuntimeDTO empty = new DcatAtlasRuntimeDTO();

		Map<Attributes, Long> points = DcatAtlasTelemetry.resourcesPerCollection(empty);

		assertEquals(4, points.size(), "every collection still reports, all of them zero");
		assertEquals(0L, points.get(Attributes.of(COLLECTION, "datasets")));
		assertEquals(Map.of(), DcatAtlasTelemetry.datasetsPerCatalog(empty));
	}

	@Test
	void perCatalogSeriesCarryTheCatalogId() {
		DcatAtlasRuntimeDTO snapshot = snapshot(1, 0, 0, 0);
		snapshot.catalogs.resources = List.of(catalog("gov", 7), catalog("geo", 2));
		snapshot.catalogs.count = 2;

		Map<Attributes, Long> points = DcatAtlasTelemetry.datasetsPerCatalog(snapshot);

		assertEquals(7L, points.get(Attributes.of(CATALOG, "gov")));
		assertEquals(2L, points.get(Attributes.of(CATALOG, "geo")));
	}

	private static DcatAtlasRuntimeDTO snapshot(int catalogs, int datasets, int dataServices, int series) {
		DcatAtlasRuntimeDTO dto = new DcatAtlasRuntimeDTO();
		dto.catalogs = new CatalogCollectionDTO();
		dto.catalogs.count = catalogs;
		dto.catalogs.resources = List.of();
		dto.datasets = collection(datasets);
		dto.dataServices = collection(dataServices);
		dto.datasetSeries = collection(series);
		return dto;
	}

	private static CollectionDTO collection(int count) {
		CollectionDTO dto = new CollectionDTO();
		dto.count = count;
		dto.resources = List.of();
		return dto;
	}

	private static CatalogDTO catalog(String id, int datasets) {
		CatalogDTO dto = new CatalogDTO();
		dto.id = id;
		dto.datasets = datasets;
		return dto;
	}
}

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogSnapshotDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureReasons;
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

	private static final AttributeKey<String> REASON = AttributeKey.stringKey("fennec.reason");
	private static final AttributeKey<String> COLLECTION = AttributeKey.stringKey("fennec.collection");

	/**
	 * A reason nothing hit reports zero rather than no series at all.
	 *
	 * <p>
	 * The two are different to every backend: if a reason were published only while it
	 * occurred, fixing it would make the series stop instead of fall to zero, and an alert
	 * on {@code > 0} could not tell that from the exporter having died.
	 * </p>
	 */
	@Test
	void everyReasonReportsAValueEvenWhenNothingIsWrong() {
		DcatAtlasRuntimeDTO snapshot = snapshot();
		snapshot.failures = List.of(failure(FailureReasons.SHAPES_NOT_LOADED));

		Map<Attributes, Long> points = DcatAtlasTelemetry.failuresPerReason(snapshot);

		assertEquals(FailureReasons.ALL.size(), points.size(), "one point per reason, not per occurrence");
		assertEquals(1L, points.get(Attributes.of(REASON, FailureReasons.SHAPES_NOT_LOADED)));
		assertEquals(0L, points.get(Attributes.of(REASON, FailureReasons.GRAPH_NOT_READY)),
				"a reason nothing hit is a zero, not a missing series");
	}

	@Test
	void repeatedFailuresOfOneReasonAreCounted() {
		DcatAtlasRuntimeDTO snapshot = snapshot();
		snapshot.failures = List.of(failure(FailureReasons.DANGLING_REFERENCE),
				failure(FailureReasons.DANGLING_REFERENCE), failure(FailureReasons.STORE_UNREADABLE));

		Map<Attributes, Long> points = DcatAtlasTelemetry.failuresPerReason(snapshot);

		assertEquals(2L, points.get(Attributes.of(REASON, FailureReasons.DANGLING_REFERENCE)));
		assertEquals(1L, points.get(Attributes.of(REASON, FailureReasons.STORE_UNREADABLE)));
	}

	@Test
	void eachCollectionIsItsOwnSeries() {
		DcatAtlasRuntimeDTO snapshot = snapshot();
		snapshot.collections = List.of(collection("datasets", 12), collection("catalogs", 3));

		Map<Attributes, Long> points = DcatAtlasTelemetry.resourcesPerCollection(snapshot);

		assertEquals(2, points.size());
		assertEquals(12L, points.get(Attributes.of(COLLECTION, "datasets")));
		assertEquals(3L, points.get(Attributes.of(COLLECTION, "catalogs")));
	}

	/** An empty portal is a portal reporting zeros, not a portal reporting nothing. */
	@Test
	void anEmptyCollectionStillReportsItsZero() {
		DcatAtlasRuntimeDTO snapshot = snapshot();
		snapshot.collections = List.of(collection("datasets", 0));

		assertEquals(0L, DcatAtlasTelemetry.resourcesPerCollection(snapshot).get(Attributes.of(COLLECTION, "datasets")));
	}

	/**
	 * A snapshot whose optional parts are absent produces points rather than an exception.
	 * <p>
	 * The exporter calls these on a timer, and a portal that has not finished coming up is
	 * exactly when someone is watching the metrics.
	 */
	@Test
	void aSnapshotWithNothingInItDoesNotThrow() {
		DcatAtlasRuntimeDTO empty = new DcatAtlasRuntimeDTO();

		assertEquals(Map.of(), DcatAtlasTelemetry.resourcesPerCollection(empty));
		assertEquals(Map.of(), DcatAtlasTelemetry.datasetsPerCatalog(empty));
		assertTrue(DcatAtlasTelemetry.failuresPerReason(empty).values().stream().allMatch(value -> value == 0L),
				"every reason is still reported, all of them zero");
	}

	@Test
	void perCatalogSeriesCarryTheCatalogId() {
		DcatAtlasRuntimeDTO snapshot = snapshot();
		CatalogSnapshotDTO catalog = new CatalogSnapshotDTO();
		catalog.id = "example";
		catalog.datasets = 7;
		snapshot.catalogs = List.of(catalog);

		Map<Attributes, Long> points = DcatAtlasTelemetry.datasetsPerCatalog(snapshot);

		assertEquals(7L, points.get(Attributes.of(AttributeKey.stringKey("fennec.catalog"), "example")));
	}

	private static DcatAtlasRuntimeDTO snapshot() {
		DcatAtlasRuntimeDTO dto = new DcatAtlasRuntimeDTO();
		dto.collections = new ArrayList<>();
		dto.catalogs = new ArrayList<>();
		dto.failures = new ArrayList<>();
		return dto;
	}

	private static CollectionDTO collection(String name, int count) {
		CollectionDTO dto = new CollectionDTO();
		dto.name = name;
		dto.count = count;
		return dto;
	}

	private static FailureDTO failure(String reason) {
		FailureDTO dto = new FailureDTO();
		dto.reason = reason;
		return dto;
	}
}

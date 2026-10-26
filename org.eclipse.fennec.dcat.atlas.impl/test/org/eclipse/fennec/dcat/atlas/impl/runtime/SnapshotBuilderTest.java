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
package org.eclipse.fennec.dcat.atlas.impl.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.ResourceDTO;
import org.junit.jupiter.api.Test;

/**
 * The mapping from what was read to what is handed out.
 */
class SnapshotBuilderTest {

	private static final List<ResourceReading> NONE = List.of();

	@Test
	void everyResourceIsNamedByIdAndByTheIriACallerCanDereference() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0, List.of(),
				List.of(new ResourceReading("luftqualitaet-2026",
						"https://daten.example.de/dcat/rest/datasets/luftqualitaet-2026")),
				NONE, NONE);

		ResourceDTO dataset = dto.datasets.resources.get(0);
		assertEquals("luftqualitaet-2026", dataset.id, "the id a store operation takes");
		assertEquals("https://daten.example.de/dcat/rest/datasets/luftqualitaet-2026", dataset.iri,
				"and the IRI a caller can dereference; neither alone is enough");
	}

	@Test
	void aCollectionsCountIsWhatItLists() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0, List.of(),
				List.of(new ResourceReading("air", "urn:air"), new ResourceReading("traffic", "urn:traffic")),
				List.of(new ResourceReading("api", "urn:api")), NONE);

		assertEquals(2, dto.datasets.count);
		assertEquals(2, dto.datasets.resources.size());
		assertEquals(1, dto.dataServices.count);
		assertEquals(0, dto.datasetSeries.count);
		assertEquals(List.of(), dto.datasetSeries.resources);
	}

	@Test
	void aCatalogCarriesItsTitleAndWhatItLists() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0,
				List.of(new CatalogReading("gov", "https://daten.example.de/dcat/rest/catalogs/gov", "Offene Daten", 2,
						1, 3, 4)),
				NONE, NONE, NONE);

		assertEquals(1, dto.catalogs.count);
		CatalogDTO catalog = dto.catalogs.resources.get(0);
		assertEquals("gov", catalog.id);
		assertEquals("https://daten.example.de/dcat/rest/catalogs/gov", catalog.iri);
		assertEquals("Offene Daten", catalog.title);
		assertEquals(2, catalog.datasets);
		assertEquals(1, catalog.dataServices);
		assertEquals(3, catalog.catalogs);
		assertEquals(4, catalog.records);
	}

	/** A catalogue need not be titled, and an untitled one is not a defect to report. */
	@Test
	void aCatalogWithoutATitleIsCarriedWithNone() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0,
				List.of(new CatalogReading("gov", "urn:gov", null, 0, 0, 0, 0)), NONE, NONE, NONE);

		assertNull(dto.catalogs.resources.get(0).title);
	}

	@Test
	void theChangeCountAndTheTimeAreCarriedThrough() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(42, 1761148800000L, List.of(), NONE, NONE, NONE);

		assertEquals(42, dto.changeCount);
		assertEquals(1761148800000L, dto.takenAt);
	}

	/** Nothing stored is a valid portal, not a missing section. */
	@Test
	void anEmptyPortalIsAnEmptySnapshotAndNotANullOne() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0, List.of(), NONE, NONE, NONE);

		assertEquals(0, dto.catalogs.count);
		assertEquals(0, dto.datasets.count);
		assertEquals(0, dto.dataServices.count);
		assertEquals(0, dto.datasetSeries.count);
	}

	/**
	 * Every empty list is its own object.
	 *
	 * <p>
	 * {@code List.of()} returns one shared singleton, so a snapshot whose empty collections
	 * all used it serialised with JSON-pointer back-references — {@code "resources":{"$ref":
	 * "#/datasets/resources"}} on a field that points at nothing of the sort — and a
	 * converter round-trip to {@code Map} threw on them outright.
	 * </p>
	 */
	@Test
	void twoEmptyCollectionsDoNotShareOneList() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0, List.of(), NONE, NONE, NONE);

		assertNotSame(dto.datasets.resources, dto.dataServices.resources);
		assertNotSame(dto.dataServices.resources, dto.datasetSeries.resources);
		assertNotSame(dto.datasets.resources, dto.catalogs.resources);
	}

	/** A reader gets a copy, and a copy is not a handle on what the component maintains. */
	@Test
	void whatComesBackCannotBeWrittenThrough() {
		DcatAtlasRuntimeDTO dto = SnapshotBuilder.build(0, 0, List.of(),
				List.of(new ResourceReading("air", "urn:air")), NONE, NONE);

		assertThrows(UnsupportedOperationException.class, () -> dto.datasets.resources.add(new ResourceDTO()));
	}
}

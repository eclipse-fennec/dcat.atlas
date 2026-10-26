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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogCollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.ResourceDTO;

/**
 * Turns what was read into the snapshot.
 *
 * <p>
 * Pure: it holds nothing, reads nothing and takes no services, which is what lets the shape
 * of the answer be tested without a store behind it.
 * </p>
 */
final class SnapshotBuilder {

	private SnapshotBuilder() {
	}

	static DcatAtlasRuntimeDTO build(long changeCount, long takenAt, List<CatalogReading> catalogs,
			List<ResourceReading> datasets, List<ResourceReading> dataServices,
			List<ResourceReading> datasetSeries) {
		DcatAtlasRuntimeDTO dto = new DcatAtlasRuntimeDTO();
		dto.changeCount = changeCount;
		dto.takenAt = takenAt;
		dto.catalogs = catalogs(catalogs);
		dto.datasets = collection(datasets);
		dto.dataServices = collection(dataServices);
		dto.datasetSeries = collection(datasetSeries);
		return dto;
	}

	private static CollectionDTO collection(List<ResourceReading> readings) {
		List<ResourceDTO> resources = new ArrayList<>(readings.size());
		for (ResourceReading reading : readings) {
			ResourceDTO dto = new ResourceDTO();
			dto.id = reading.id();
			dto.iri = reading.iri();
			resources.add(dto);
		}
		CollectionDTO dto = new CollectionDTO();
		dto.resources = immutable(resources);
		dto.count = dto.resources.size();
		return dto;
	}

	private static CatalogCollectionDTO catalogs(List<CatalogReading> readings) {
		List<CatalogDTO> resources = new ArrayList<>(readings.size());
		for (CatalogReading reading : readings) {
			CatalogDTO dto = new CatalogDTO();
			dto.id = reading.id();
			dto.iri = reading.iri();
			dto.title = reading.title();
			dto.datasets = reading.datasets();
			dto.dataServices = reading.dataServices();
			dto.catalogs = reading.catalogs();
			dto.records = reading.records();
			resources.add(dto);
		}
		CatalogCollectionDTO dto = new CatalogCollectionDTO();
		dto.resources = immutable(resources);
		dto.count = dto.resources.size();
		return dto;
	}

	/**
	 * An unmodifiable copy that is never shared with another field.
	 *
	 * <p>
	 * {@code List.of()} and {@code List.copyOf} of an empty collection both return the same
	 * singleton, so a snapshot whose empty collections all used it serialised with
	 * JSON-pointer back-references — a {@code resources} field rendered as
	 * {@code {"$ref":"#/datasets/resources"}} — and a converter round-trip to {@code Map}
	 * threw on them outright. A distinct instance per field costs an object and keeps the
	 * document honest.
	 * </p>
	 */
	private static <T> List<T> immutable(List<T> items) {
		return items.isEmpty() ? Collections.unmodifiableList(new ArrayList<T>()) : List.copyOf(items);
	}
}

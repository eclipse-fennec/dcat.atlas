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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.dcat.atlas.api.runtime.CatalogSnapshotDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.CollectionDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.FailureReasons;
import org.eclipse.fennec.dcat.atlas.api.runtime.GraphDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.IdentityDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.StoreDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.ValidationDTO;

/**
 * Turns what was read into the snapshot, and decides what counts as a failure.
 *
 * <p>
 * Pure: it holds nothing, reads nothing and takes no services. Every rule here is one of the
 * states this portal can be in that serves {@code 200} while being wrong, and each is a
 * comparison between two facts that are individually fine — enforcement on against no shapes
 * loaded, a graph enabled against a graph not ready, one collection's write policy against
 * another's.
 * </p>
 */
final class SnapshotBuilder {

	private SnapshotBuilder() {
	}

	static DcatAtlasRuntimeDTO build(long changeCount, long takenAt, IdentityReading identity, List<String> endpoints,
			StoreReading store, ValidationReading validation, GraphReading graph, List<CollectionReading> collections,
			List<CatalogReading> catalogs, List<FailureDTO> inherited) {
		DcatAtlasRuntimeDTO dto = new DcatAtlasRuntimeDTO();
		dto.changeCount = changeCount;
		dto.takenAt = takenAt;
		dto.endpoints = immutable(endpoints);
		dto.identity = identity(identity);
		dto.store = store(store);
		dto.validation = validation(validation);
		dto.graph = graph(graph);
		dto.collections = collections(collections);
		dto.catalogs = catalogs(catalogs);
		dto.failures = failures(store, validation, graph, collections, inherited);
		return dto;
	}

	private static IdentityDTO identity(IdentityReading reading) {
		IdentityDTO dto = new IdentityDTO();
		dto.logicalBase = reading.logicalBase();
		dto.publicBase = reading.publicBase();
		return dto;
	}

	private static StoreDTO store(StoreReading reading) {
		StoreDTO dto = new StoreDTO();
		dto.kind = reading.kind();
		dto.location = withoutCredentials(reading.location());
		dto.persistent = reading.persistent();
		dto.readable = reading.readable();
		return dto;
	}

	/**
	 * The location with any userinfo removed.
	 *
	 * <p>
	 * Done by hand rather than with {@code URI}, because the form that actually carries a
	 * secret here is often not a URI at all: an scp-style git remote
	 * ({@code git@host:path}) hides its user in front of the host, where no URI parser
	 * looks, and {@code URI.create} on it throws or yields an opaque URI with the whole
	 * thing in the scheme-specific part. Everything before the last {@code @} of the
	 * authority goes, whichever form it is in.
	 * </p>
	 */
	private static String withoutCredentials(String location) {
		if (location == null) {
			return null;
		}
		int schemeEnd = location.indexOf("://");
		int authorityStart = schemeEnd < 0 ? 0 : schemeEnd + 3;
		int pathStart = location.indexOf('/', authorityStart);
		int authorityEnd = pathStart < 0 ? location.length() : pathStart;
		int at = location.lastIndexOf('@', authorityEnd);
		if (at < authorityStart) {
			return location;
		}
		return location.substring(0, authorityStart) + location.substring(at + 1);
	}

	private static ValidationDTO validation(ValidationReading reading) {
		ValidationDTO dto = new ValidationDTO();
		dto.shapesDirectory = reading.shapesDirectory();
		dto.shapeFiles = reading.shapeFiles();
		dto.vocabularyDirectory = reading.vocabularyDirectory();
		dto.vocabularyFiles = reading.vocabularyFiles();
		dto.enforceOnWrite = reading.enforceOnWrite();
		dto.effectivelyEnforcing = reading.enforceOnWrite() && reading.shapeFiles() > 0;
		return dto;
	}

	private static GraphDTO graph(GraphReading reading) {
		GraphDTO dto = new GraphDTO();
		dto.enabled = reading.enabled();
		dto.ready = reading.ready();
		dto.maxResults = (int) Math.min(Integer.MAX_VALUE, reading.maxResultRows());
		dto.timeoutMillis = reading.queryTimeoutMillis();
		return dto;
	}

	/**
	 * One entry per collection, however many stores answer for it.
	 *
	 * <p>
	 * Every collection has two: a read-only component and an admin component, each
	 * registering itself for introspection. Both readings are kept for
	 * {@link #disagreementOverValidateOnWrite}, because the setting is configured on each and
	 * the two can differ — but a reader asking what the portal holds is told once.
	 * </p>
	 */
	private static List<CollectionDTO> collections(List<CollectionReading> readings) {
		Map<String, CollectionDTO> byName = new LinkedHashMap<>();
		for (CollectionReading reading : readings) {
			CollectionDTO dto = new CollectionDTO();
			dto.name = reading.name();
			dto.ids = immutable(reading.ids());
			dto.count = dto.ids.size();
			dto.validateOnWrite = reading.validateOnWrite();
			byName.putIfAbsent(reading.name(), dto);
		}
		return immutable(byName.values());
	}

	/**
	 * An unmodifiable copy that is never shared with another field.
	 *
	 * <p>
	 * {@code List.of()} and {@code List.copyOf} of an empty collection both return the same
	 * singleton, so a snapshot whose empty fields all used it serialised with JSON-pointer
	 * back-references — {@code "failures":{"$ref":"#/endpoints"}}, which reads as though
	 * failures pointed at endpoints — and a converter round-trip to {@code Map} threw on
	 * them outright. A distinct instance per field costs an object and keeps the document
	 * honest.
	 * </p>
	 */
	private static <T> List<T> immutable(java.util.Collection<T> items) {
		return items.isEmpty() ? java.util.Collections.unmodifiableList(new ArrayList<T>())
				: List.copyOf(items);
	}

	private static List<CatalogSnapshotDTO> catalogs(List<CatalogReading> readings) {
		List<CatalogSnapshotDTO> dtos = new ArrayList<>();
		for (CatalogReading reading : readings) {
			CatalogSnapshotDTO dto = new CatalogSnapshotDTO();
			dto.id = reading.id();
			dto.iri = reading.iri();
			dto.title = reading.title();
			dto.datasets = reading.datasets();
			dto.dataServices = reading.dataServices();
			dto.catalogs = reading.catalogs();
			dto.records = reading.records();
			dtos.add(dto);
		}
		return immutable(dtos);
	}

	private static List<FailureDTO> failures(StoreReading store, ValidationReading validation, GraphReading graph,
			List<CollectionReading> collections, List<FailureDTO> inherited) {
		List<FailureDTO> failures = new ArrayList<>(inherited);

		if (!store.readable()) {
			failures.add(failure(FailureReasons.STORE_UNREADABLE, withoutCredentials(store.location()),
					"the store could not be read when this snapshot was built"));
		}
		if (!store.persistent()) {
			failures.add(failure(FailureReasons.STORE_NOT_PERSISTING, withoutCredentials(store.location()),
					"writes are accepted and will not survive a restart"));
		}

		if (validation.shapeFiles() == 0) {
			failures.add(failure(FailureReasons.SHAPES_NOT_LOADED, validation.shapesDirectory(),
					"no shape file was parsed, so every entity conforms"));
			if (validation.enforceOnWrite()) {
				failures.add(failure(FailureReasons.ENFORCEMENT_INEFFECTIVE, validation.shapesDirectory(),
						"enforceOnWrite is on and there is nothing to enforce: no write can be refused"));
			}
		} else if (validation.vocabularyFiles() == 0) {
			// Reported from the shapes rather than from the configuration: the CV shapes are
			// part of the DCAT-AP.de set the portal loads, so shapes without vocabulary data
			// behind them is the F-22 state whatever the vocabulary directory says.
			failures.add(failure(FailureReasons.VOCABULARY_MISSING, validation.vocabularyDirectory(),
					"shapes are loaded with no controlled-vocabulary data behind them; every coded "
							+ "value will report a false violation"));
		}

		if (graph.enabled() && !graph.ready()) {
			failures.add(failure(FailureReasons.GRAPH_NOT_READY, null,
					"the projection is enabled and cannot answer completely: queries succeed and return too few rows"));
		}

		disagreementOverValidateOnWrite(collections).ifPresent(failures::add);

		return immutable(failures);
	}

	/**
	 * {@code validateOnWrite} is configured once per store — ten times over — and nothing
	 * else makes two of them disagreeing visible.
	 */
	private static java.util.Optional<FailureDTO> disagreementOverValidateOnWrite(List<CollectionReading> collections) {
		if (collections.isEmpty()) {
			return java.util.Optional.empty();
		}
		boolean first = collections.get(0).validateOnWrite();
		List<String> differing = new ArrayList<>();
		for (CollectionReading collection : collections) {
			if (collection.validateOnWrite() != first) {
				differing.add(collection.name());
			}
		}
		if (differing.isEmpty()) {
			return java.util.Optional.empty();
		}
		return java.util.Optional.of(failure(FailureReasons.CONFIG_INCONSISTENT, String.join(", ", differing),
				"these collections disagree with the others about validateOnWrite; the same write is "
						+ "checked in one collection and not in another"));
	}

	private static FailureDTO failure(String reason, String subject, String message) {
		FailureDTO failure = new FailureDTO();
		failure.reason = reason;
		failure.subject = subject;
		failure.message = message;
		return failure;
	}
}

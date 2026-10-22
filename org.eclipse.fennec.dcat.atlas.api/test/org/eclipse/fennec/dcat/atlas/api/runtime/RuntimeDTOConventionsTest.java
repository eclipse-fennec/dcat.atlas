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
package org.eclipse.fennec.dcat.atlas.api.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.osgi.util.converter.Converters;

/**
 * The two properties the runtime DTOs have to keep, both of which are easy to lose by
 * accident and neither of which a compiler checks.
 */
class RuntimeDTOConventionsTest {

	/**
	 * A populated snapshot survives the OSGi converter with no custom rules.
	 * <p>
	 * That is the whole claim of "plain DTO": public fields, no behaviour, nothing that needs
	 * telling how to copy it. A field of a type the converter cannot handle fails here rather
	 * than in whatever consumer meets it first.
	 */
	@Test
	void aPopulatedSnapshotRoundTripsThroughTheConverter() {
		DcatAtlasRuntimeDTO original = populated();

		@SuppressWarnings("unchecked")
		Map<String, Object> asMap = Converters.standardConverter().convert(original).to(Map.class);
		DcatAtlasRuntimeDTO copy = Converters.standardConverter().convert(asMap).to(DcatAtlasRuntimeDTO.class);

		assertEquals(original.changeCount, copy.changeCount);
		assertEquals(original.identity.publicBase, copy.identity.publicBase);
		assertEquals(original.store.kind, copy.store.kind);
		assertEquals(original.validation.effectivelyEnforcing, copy.validation.effectivelyEnforcing);
		assertEquals(original.collections.get(0).ids, copy.collections.get(0).ids);
		assertEquals(original.catalogs.get(0).datasets, copy.catalogs.get(0).datasets);
		assertEquals(original.failures.get(0).reason, copy.failures.get(0).reason);
	}

	/**
	 * Nothing in the package names the DCAT-AP model, the codec or EMF.
	 * <p>
	 * This is the decision rather than a tidiness rule: the consumer the service was built for
	 * is a telemetry bundle, and requiring it to carry EMF and the profile to read an integer
	 * would be the whole cost of the feature. It also keeps the snapshot structurally unable to
	 * reference a content type — a catalog with a {@code String iri} cannot become a
	 * {@code dcat:Catalog}.
	 * <p>
	 * Asserted over the sources, because an import that is only ever a type in a signature
	 * leaves nothing in the bytecode of this bundle's own tests to find.
	 */
	@Test
	void nothingInThePackageImportsEmfOrTheDcatModel() throws IOException {
		Path sources = Path.of("src/org/eclipse/fennec/dcat/atlas/api/runtime");
		assertTrue(Files.isDirectory(sources), "the runtime API sources moved: " + sources.toAbsolutePath());

		try (Stream<Path> files = Files.list(sources)) {
			for (Path file : files.filter(f -> f.toString().endsWith(".java")).toList()) {
				for (String line : Files.readAllLines(file)) {
					if (!line.startsWith("import ")) {
						continue;
					}
					assertTrue(permitted(line),
							file.getFileName() + " imports something the DTOs may not depend on: " + line.trim());
				}
			}
		}
	}

	/** {@code java.*}, the OSGi DTO and annotation packages, and nothing else. */
	private static boolean permitted(String importLine) {
		return Stream.of("java.", "org.osgi.dto", "org.osgi.framework", "org.osgi.annotation")
				.anyMatch(allowed -> importLine.startsWith("import " + allowed));
	}

	private static DcatAtlasRuntimeDTO populated() {
		DcatAtlasRuntimeDTO dto = new DcatAtlasRuntimeDTO();
		dto.changeCount = 7;
		dto.takenAt = 1_760_000_000_000L;
		dto.endpoints = List.of("http://localhost:8085/dcat/rest");

		dto.identity = new IdentityDTO();
		dto.identity.logicalBase = "http://dcat.atlas/";
		dto.identity.publicBase = "http://localhost:8085/dcat/rest/";

		dto.store = new StoreDTO();
		dto.store.kind = "git";
		dto.store.location = "https://example.org/store.git";
		dto.store.persistent = true;
		dto.store.readable = true;

		dto.validation = new ValidationDTO();
		dto.validation.shapesDirectory = "/opt/dcat/shapes";
		dto.validation.shapeFiles = 12;
		dto.validation.vocabularyDirectory = "/opt/dcat/vocabularies";
		dto.validation.vocabularyFiles = 5;
		dto.validation.enforceOnWrite = true;
		dto.validation.effectivelyEnforcing = true;

		dto.graph = new GraphDTO();
		dto.graph.enabled = true;
		dto.graph.ready = true;
		dto.graph.maxResults = 1000;
		dto.graph.timeoutMillis = 30_000;

		CollectionDTO datasets = new CollectionDTO();
		datasets.name = "datasets";
		datasets.count = 2;
		datasets.ids = List.of("air-quality", "traffic");
		datasets.validateOnWrite = true;
		dto.collections = List.of(datasets);

		CatalogSnapshotDTO catalog = new CatalogSnapshotDTO();
		catalog.id = "example";
		catalog.iri = "http://localhost:8085/dcat/rest/catalogs/example";
		catalog.title = "Example Catalog";
		catalog.datasets = 2;
		catalog.dataServices = 0;
		catalog.catalogs = 0;
		catalog.records = 2;
		dto.catalogs = List.of(catalog);

		FailureDTO failure = new FailureDTO();
		failure.reason = FailureReasons.VOCABULARY_MISSING;
		failure.subject = "/opt/dcat/vocabularies";
		failure.message = "CV shapes are loaded with no vocabulary data behind them";
		dto.failures = List.of(failure);

		return dto;
	}
}

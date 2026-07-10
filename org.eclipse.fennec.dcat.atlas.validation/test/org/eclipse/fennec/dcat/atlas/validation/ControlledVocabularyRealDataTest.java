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
package org.eclipse.fennec.dcat.atlas.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.Severity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DcatFactory;
import skos.Concept;
import skos.SkosFactory;

/**
 * Opt-in regression test that validates against the <em>real</em> DCAT-AP.de
 * controlled-vocabulary shape + the real authority-table reference data (F-22) — the
 * one thing the deterministic {@link DcatValidationServiceImplTest} (tiny self-authored
 * shape + vocab) cannot cover. It guards the shape/vocab loading + graph-union logic
 * against real inputs if we ever refactor it.
 * <p>
 * <b>It is disabled unless a developer provides the files locally.</b> It runs only when
 * both {@code SHACL_SHAPES_DIR} and {@code SHACL_VOCAB_DIR} are set (the same env vars the
 * runtime {@code config.json} uses) and the expected files are present; otherwise it is
 * skipped, so it never runs on CI/GitHub. To run it locally:
 * <pre>
 *   SHACL_SHAPES_DIR=/path/to/dcat-ap.de shapes \
 *   SHACL_VOCAB_DIR=/path/to/downloaded owl:imports data \
 *   ./gradlew :org.eclipse.fennec.dcat.atlas.validation:test --tests '*ControlledVocabularyRealDataTest*'
 * </pre>
 * The shapes are the GovData set (AGPLv3, not vendored); the vocabulary dir holds each
 * (uncommented) {@code owl:imports} target from {@code dcat-ap-de-imports.ttl} downloaded
 * as a local RDF file — see the development guide (F-22 entry).
 */
@EnabledIfEnvironmentVariable(named = "SHACL_SHAPES_DIR", matches = ".+")
@EnabledIfEnvironmentVariable(named = "SHACL_VOCAB_DIR", matches = ".+")
public class ControlledVocabularyRealDataTest {

	/** The controlled-vocabulary shape file within the GovData shapes set. */
	private static final String CV_SHAPE_FILE = "dcat-ap-de-controlledvocabularies.ttl";
	/** A real EU authority frequency (dcterms:accrualPeriodicity is a MUSS controlled-vocabulary check). */
	private static final String VALID_FREQUENCY = "http://publications.europa.eu/resource/authority/frequency/ANNUAL";
	private static final String BOGUS_FREQUENCY = "http://example/frequency/made-up";

	@TempDir
	Path cvShapeOnlyDir;

	@Test
	void realFrequencyVocabularyConformsForValidAndBlocksBogus() throws IOException {
		Path shapesDir = Path.of(System.getenv("SHACL_SHAPES_DIR"));
		Path vocabDir = Path.of(System.getenv("SHACL_VOCAB_DIR"));
		Path cvShape = shapesDir.resolve(CV_SHAPE_FILE);
		assumeTrue(Files.isRegularFile(cvShape),
				() -> CV_SHAPE_FILE + " not found under SHACL_SHAPES_DIR=" + shapesDir);
		assumeTrue(Files.isDirectory(vocabDir), () -> "SHACL_VOCAB_DIR is not a directory: " + vocabDir);

		// Load ONLY the controlled-vocabulary shape so the result isolates CV findings;
		// the full structural shape set would add unrelated MUSS violations to a minimal
		// dataset. The real vocabulary data is loaded (and unioned in) as configured.
		Files.copy(cvShape, cvShapeOnlyDir.resolve(CV_SHAPE_FILE));
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				cvShapeOnlyDir, vocabDir);

		ValidationReport valid = service.validate(datasetWithFrequency(VALID_FREQUENCY));
		assertTrue(valid.conforms(), "a real EU frequency URI should satisfy the CV check");

		ValidationReport bogus = service.validate(datasetWithFrequency(BOGUS_FREQUENCY));
		assertFalse(bogus.conforms(), "a bogus frequency URI should violate the CV check");
		assertTrue(bogus.getEntries().stream().anyMatch(e -> Severity.Violation.equals(e.severity())),
				"dcterms:accrualPeriodicity is a MUSS check -> must block a write");
		assertTrue(bogus.getEntries().stream().anyMatch(e -> e.message() != null && e.message().contains("kv-frequency")),
				"expected the frequency CV violation");
	}

	private static Dataset datasetWithFrequency(String frequencyUri) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout("https://portal.example/datasets/cv-real-data");
		Concept frequency = SkosFactory.eINSTANCE.createConcept();
		frequency.setResource(frequencyUri);
		dataset.setAccrualPeriodicity(frequency);
		return dataset;
	}
}

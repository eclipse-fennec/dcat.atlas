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
import terms.LicenseDocument;
import terms.TermsFactory;

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
	/** A real DCAT-AP.de licence — the one the live 422 was traced to. */
	private static final String DL_BY_DE = "http://dcat-ap.de/def/licenses/dl-by-de/2.0";
	/** The plain literal every entry of the real licence table carries as its {@code dct:type}. */
	private static final String LICENCE_TABLE_TYPE = "Freie Nutzung";

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
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(cvShapeOnlyDir, vocabDir);

		ValidationReport valid = service.validate(datasetWithFrequency(VALID_FREQUENCY));
		assertTrue(valid.conforms(), "a real EU frequency URI should satisfy the CV check");

		ValidationReport bogus = service.validate(datasetWithFrequency(BOGUS_FREQUENCY));
		assertFalse(bogus.conforms(), "a bogus frequency URI should violate the CV check");
		assertTrue(bogus.getEntries().stream().anyMatch(e -> Severity.Violation.equals(e.severity())),
				"dcterms:accrualPeriodicity is a MUSS check -> must block a write");
		assertTrue(bogus.getEntries().stream().anyMatch(e -> e.message() != null && e.message().contains("kv-frequency")),
				"expected the frequency CV violation");
	}

	/**
	 * The bug this guards against, on the inputs that exposed it: every entry in the real
	 * licence table carries {@code dct:type "Freie Nutzung"} as a plain literal, which the
	 * SEMIC shapes reject as not an IRI. Unioned into the data graph it landed on the
	 * caller's licence node and made {@code enforceOnWrite} reject every write that
	 * referenced any licence at all.
	 */
	@Test
	void realLicenceTableDefectsAreNotReportedAsTheEntitys() {
		Path shapesDir = Path.of(System.getenv("SHACL_SHAPES_DIR"));
		Path vocabDir = Path.of(System.getenv("SHACL_VOCAB_DIR"));
		assumeTrue(Files.isDirectory(shapesDir), () -> "SHACL_SHAPES_DIR is not a directory: " + shapesDir);
		assumeTrue(Files.isDirectory(vocabDir), () -> "SHACL_VOCAB_DIR is not a directory: " + vocabDir);

		// The full shape set here, not just the CV shape: the licence rule lives in the
		// SEMIC shapes. A minimal dataset trips plenty of unrelated MUSS rules, so this
		// asserts on the one result rather than on conformance.
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		ValidationReport report = service.validate(datasetWithLicence(DL_BY_DE));

		assertTrue(report.getEntries().stream().noneMatch(e -> isLicenceTableType(e.value())),
				() -> "the licence table's own dct:type defect was reported as the caller's: " + report.getEntries());
	}

	private static boolean isLicenceTableType(org.apache.jena.graph.Node value) {
		return value != null && value.isLiteral() && LICENCE_TABLE_TYPE.equals(value.getLiteralLexicalForm());
	}

	private static Dataset datasetWithLicence(String licenceUri) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout("https://portal.example/datasets/cv-real-data");
		// Exactly what an <license about="..."/> in a submitted entity produces: the licence
		// node exists in the entity graph (carrying its rdf:type) but the caller wrote none
		// of the triples the table holds about it.
		LicenseDocument licence = TermsFactory.eINSTANCE.createLicenseDocument();
		licence.setAbout(licenceUri);
		dataset.setLicense(licence);
		return dataset;
	}

	private static Dataset datasetWithFrequency(String frequencyUri) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout("https://portal.example/datasets/cv-real-data");
		// Controlled-vocabulary references are AnyURI attributes now, not skos:Concept
		// nodes, so the converter emits the IRI directly as the object of the triple —
		// which is what the CV shape's sh:class/skos:inScheme check needs to resolve.
		dataset.setAccrualPeriodicity(frequencyUri);
		return dataset;
	}
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.Severity;
import org.eclipse.fennec.dcat.atlas.api.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Proves the SHACL pipeline end-to-end (EMF entity → RDF → Jena SHACL → report)
 * against a tiny, self-authored shape (so the test is deterministic and carries no
 * upstream-licensed shapes). The real DCAT-AP.de shapes are pointed at via config
 * at runtime, not bundled here. Assertions are against the native Jena
 * {@link ValidationReport} that {@code validate} now returns; the deprecated
 * {@code validateLegacy} projection is covered by a single test.
 */
public class DcatValidationServiceImplTest {

	private static final String BASE = "https://portal.example/datasets/";

	/** Minimal shape: a dcat:Dataset must carry at least one dct:title. */
	private static final String TITLE_SHAPE = """
			@prefix sh:   <http://www.w3.org/ns/shacl#> .
			@prefix dcat: <http://www.w3.org/ns/dcat#> .
			@prefix dct:  <http://purl.org/dc/terms/> .
			@prefix ex:   <http://example/shapes#> .

			ex:DatasetShape a sh:NodeShape ;
			    sh:targetClass dcat:Dataset ;
			    sh:property [
			        sh:path dct:title ;
			        sh:minCount 1 ;
			        sh:message "A Dataset must have a dct:title."
			    ] .
			""";

	/** A dcat:Dataset value must be in this controlled vocabulary (mirrors the DCAT-AP.de kv-* checks). */
	private static final String SCHEME = "http://example/scheme/frequency";

	/**
	 * Mirrors the DCAT-AP.de controlled-vocabulary pattern: the focus node must carry a
	 * {@code skos:inScheme} pointing at the authority scheme. The tested entity never
	 * serialises that triple itself — it can only be satisfied from the reference
	 * vocabulary graph (F-22).
	 */
	private static final String IN_SCHEME_SHAPE = """
			@prefix sh:   <http://www.w3.org/ns/shacl#> .
			@prefix dcat: <http://www.w3.org/ns/dcat#> .
			@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
			@prefix ex:   <http://example/shapes#> .

			ex:DatasetSchemeShape a sh:NodeShape ;
			    sh:targetClass dcat:Dataset ;
			    sh:property [
			        sh:path skos:inScheme ;
			        sh:hasValue <%s> ;
			        sh:minCount 1 ;
			        sh:severity sh:Violation ;
			        sh:message "value must be in the controlled vocabulary"
			    ] .
			""".formatted(SCHEME);

	/** Same title rule as {@link #TITLE_SHAPE} but only a recommendation (DCAT-AP.de "SOLL"). */
	private static final String TITLE_WARNING_SHAPE = """
			@prefix sh:   <http://www.w3.org/ns/shacl#> .
			@prefix dcat: <http://www.w3.org/ns/dcat#> .
			@prefix dct:  <http://purl.org/dc/terms/> .
			@prefix ex:   <http://example/shapes#> .

			ex:DatasetShape a sh:NodeShape ;
			    sh:targetClass dcat:Dataset ;
			    sh:property [
			        sh:path dct:title ;
			        sh:minCount 1 ;
			        sh:severity sh:Warning ;
			        sh:message "A Dataset should have a dct:title."
			    ] .
			""";

	@TempDir
	Path shapesDir;

	@TempDir
	Path vocabDir;

	private DcatValidationServiceImpl serviceWithTitleShape() throws IOException {
		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		return new DcatValidationServiceImpl(ValidationTestResourceSets.factory(), shapesDir);
	}

	@Test
	void conformsWhenMandatoryFieldPresent() throws IOException {
		ValidationReport report = serviceWithTitleShape().validate(dataset(BASE + "air", "Air quality"));
		assertTrue(report.conforms());
		assertTrue(report.getEntries().isEmpty());
	}

	@Test
	void reportsViolationWhenMandatoryFieldMissing() throws IOException {
		ValidationReport report = serviceWithTitleShape().validate(dataset(BASE + "air", null));
		assertFalse(report.conforms());
		assertEquals(1, report.getEntries().size());
		assertTrue(firstEntry(report).message().contains("dct:title"), firstEntry(report).message());
		// Default severity is sh:Violation (MUSS) -> blocks a write.
		assertTrue(blocks(report));
	}

	@Test
	void controlledVocabularyConformsWhenReferenceDataProvidesMembership() throws IOException {
		Files.writeString(shapesDir.resolve("in-scheme.ttl"), IN_SCHEME_SHAPE);
		Files.writeString(vocabDir.resolve("scheme.ttl"), """
				@prefix skos: <http://www.w3.org/2004/02/skos/core#> .
				<%s> skos:inScheme <%s> .
				""".formatted(BASE + "air", SCHEME));
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				shapesDir, vocabDir);

		ValidationReport report = service.validate(dataset(BASE + "air", "Air quality"));

		assertTrue(report.conforms());
	}

	@Test
	void controlledVocabularyViolatesWhenReferenceDataMissing() throws IOException {
		Files.writeString(shapesDir.resolve("in-scheme.ttl"), IN_SCHEME_SHAPE);
		// vocabDir left empty: the skos:inScheme triple is nowhere in the graph.
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				shapesDir, vocabDir);

		ValidationReport report = service.validate(dataset(BASE + "air", "Air quality"));

		assertFalse(report.conforms());
		assertTrue(blocks(report));
	}

	@Test
	void warningSeverityIsReportedButDoesNotBlock() throws IOException {
		Files.writeString(shapesDir.resolve("title-warning.ttl"), TITLE_WARNING_SHAPE);
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				shapesDir);

		ValidationReport report = service.validate(dataset(BASE + "air", null));

		// Jena reports any entry, so it does not "conform"...
		assertFalse(report.conforms());
		assertEquals(1, report.getEntries().size());
		// ...but a sh:Warning ("SOLL") is not a blocking violation, so a write is not rejected.
		assertEquals(Severity.Warning, firstEntry(report).severity());
		assertFalse(blocks(report));
	}

	@Test
	void writeEnforcementReflectsConfig() throws IOException {
		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		assertFalse(new DcatValidationServiceImpl(ValidationTestResourceSets.factory(), shapesDir, false).isWriteEnforced());
		assertTrue(new DcatValidationServiceImpl(ValidationTestResourceSets.factory(), shapesDir, true).isWriteEnforced());
		// Default (2-arg) constructor leaves enforcement off.
		assertFalse(serviceWithTitleShape().isWriteEnforced());
	}

	@Test
	void validationConformsWhenNoShapesConfigured() {
		// Empty directory -> empty shapes -> everything conforms (enforcement is the
		// caller's concern; the service just reports).
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				shapesDir);
		ValidationReport report = service.validate(dataset(BASE + "air", null));
		assertTrue(report.conforms());
		assertTrue(report.getEntries().isEmpty());
	}

	@Test
	@SuppressWarnings("deprecation")
	void legacyProjectionMirrorsTheReport() throws IOException {
		ValidationResult legacy = serviceWithTitleShape().validateLegacy(dataset(BASE + "air", null));
		assertFalse(legacy.conforms());
		assertEquals(1, legacy.violations().size());
		assertTrue(legacy.hasBlockingViolations());
		assertTrue(legacy.violations().get(0).message().contains("dct:title"));
		assertTrue(legacy.reportTurtle().contains("ValidationReport"), legacy.reportTurtle());
	}

	/** True when the report has a hard ({@code sh:Violation}) entry — a null severity defaults to it. */
	private static boolean blocks(ValidationReport report) {
		return report.getEntries().stream().anyMatch(e -> e.severity() == null || Severity.Violation.equals(e.severity()));
	}

	private static ReportEntry firstEntry(ValidationReport report) {
		return List.copyOf(report.getEntries()).get(0);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		if (title != null) {
			PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
			literal.setLang("en");
			literal.setValue(title);
			dataset.getTitle().add(literal);
		}
		return dataset;
	}
}

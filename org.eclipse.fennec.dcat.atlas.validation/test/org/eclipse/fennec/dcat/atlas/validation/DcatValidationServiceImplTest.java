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
import org.apache.felix.hc.api.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsFactory;

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

	/** A licence the entity only references — the shape of the real DCAT-AP.de licence table. */
	private static final String LICENCE = "http://example/licences/dl-by-de";
	private static final String ALLOWED_LICENCE_TYPE = "http://example/licences/type/free";
	private static final String BOGUS_LICENCE_TYPE = "http://example/licences/type/bogus";

	/**
	 * Mirrors the SEMIC licence rule that exposed the vocabulary-union bug: {@code dct:type}
	 * must be one of the authority's IRIs. The real table violates it on every entry.
	 */
	private static final String LICENCE_TYPE_SHAPE = """
			@prefix sh:  <http://www.w3.org/ns/shacl#> .
			@prefix dct: <http://purl.org/dc/terms/> .
			@prefix ex:  <http://example/shapes#> .

			ex:LicenceShape a sh:NodeShape ;
			    sh:targetClass dct:LicenseDocument ;
			    sh:property [
			        sh:path dct:type ;
			        sh:nodeKind sh:IRI ;
			        sh:in ( <%s> ) ;
			        sh:message "dct:type must be an allowed licence-type IRI"
			    ] .
			""".formatted(ALLOWED_LICENCE_TYPE);

	/**
	 * The authority table as it really is: {@code dct:type} written as a plain literal, which
	 * the shape above rejects. Unioned in, it is a defect the caller neither wrote nor can fix.
	 */
	private static final String LICENCE_VOCABULARY = """
			@prefix dct: <http://purl.org/dc/terms/> .
			<%s> a dct:LicenseDocument ;
			    dct:type "Freie Nutzung" .
			""".formatted(LICENCE);

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
		return new DcatValidationServiceImpl(shapesDir);
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
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		ValidationReport report = service.validate(dataset(BASE + "air", "Air quality"));

		assertTrue(report.conforms());
	}

	@Test
	void controlledVocabularyViolatesWhenReferenceDataMissing() throws IOException {
		Files.writeString(shapesDir.resolve("in-scheme.ttl"), IN_SCHEME_SHAPE);
		// vocabDir left empty: the skos:inScheme triple is nowhere in the graph.
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		ValidationReport report = service.validate(dataset(BASE + "air", "Air quality"));

		assertFalse(report.conforms());
		assertTrue(blocks(report));
	}

	@Test
	void vocabularyOwnDefectsAreNotReportedAsTheEntitys() throws IOException {
		licenceShapeAndVocabulary();
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		// The entity says nothing about the licence beyond referencing it; the offending
		// dct:type is purely the authority table's.
		ValidationReport report = service.validate(datasetWithLicence(null));

		assertTrue(report.conforms(), () -> report.getEntries().toString());
	}

	@Test
	void entityDefectSurvivesEvenOnAVocabularyNode() throws IOException {
		licenceShapeAndVocabulary();
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		ValidationReport report = service.validate(datasetWithLicence(BOGUS_LICENCE_TYPE));

		// Same focus node and same sh:path as the suppressed vocabulary result: only the
		// value distinguishes them, which is why attribution has to be per triple.
		assertFalse(report.conforms());
		assertEquals(1, report.getEntries().size(), () -> report.getEntries().toString());
		assertEquals(BOGUS_LICENCE_TYPE, firstEntry(report).value().getURI());
	}

	@Test
	void valuelessResultsSurviveTheVocabularyFilter() throws IOException {
		// sh:minCount reports no sh:value, so there is no triple to attribute; the caller's
		// own missing title must still come through with a vocabulary unioned in.
		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		Files.writeString(vocabDir.resolve("licences.ttl"), LICENCE_VOCABULARY);
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir, vocabDir);

		ValidationReport report = service.validate(dataset(BASE + "air", null));

		assertFalse(report.conforms());
		assertEquals(1, report.getEntries().size());
		assertTrue(blocks(report));
	}

	@Test
	void warningSeverityIsReportedButDoesNotBlock() throws IOException {
		Files.writeString(shapesDir.resolve("title-warning.ttl"), TITLE_WARNING_SHAPE);
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir);

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
		assertFalse(new DcatValidationServiceImpl(shapesDir, false).isWriteEnforced());
		assertTrue(new DcatValidationServiceImpl(shapesDir, true).isWriteEnforced());
		// Default (2-arg) constructor leaves enforcement off.
		assertFalse(serviceWithTitleShape().isWriteEnforced());
	}

	@Test
	void validationConformsWhenNoShapesConfigured() {
		// Empty directory -> empty shapes -> everything conforms (enforcement is the
		// caller's concern; the service just reports).
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir);
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

	private void licenceShapeAndVocabulary() throws IOException {
		Files.writeString(shapesDir.resolve("licence-type.ttl"), LICENCE_TYPE_SHAPE);
		Files.writeString(vocabDir.resolve("licences.ttl"), LICENCE_VOCABULARY);
	}

	/**
	 * A dataset referencing {@link #LICENCE}. The licence node is in the entity graph too
	 * (it carries its {@code rdf:type}), so focus-node attribution alone cannot tell the
	 * entity's triples from the authority table's.
	 */
	private static Dataset datasetWithLicence(String licenceType) {
		Dataset dataset = dataset(BASE + "air", "Air quality");
		LicenseDocument licence = TermsFactory.eINSTANCE.createLicenseDocument();
		licence.setAbout(LICENCE);
		if (licenceType != null) {
			licence.getType().add(licenceType);
		}
		dataset.setLicense(licence);
		return dataset;
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
	// --- F-25 readiness (split shapes status) -------------------------------

	@Test
	void readinessNoShapesConfiguredIsWarnNotCritical() {
		// Shapes are operator-supplied deployment input; running without them is a
		// documented no-op, so the portal stays fit to serve. WARN maps to HTTP 200.
		DcatValidationServiceImpl service = new DcatValidationServiceImpl((Path) null);
		// WARN, not CRITICAL. Felix's Result.isOk() is strictly OK, so "still serving" is
		// not expressed here but by the servlet's httpStatus.warn=200 mapping — asserted in
		// HealthEndpointIntegrationTest.
		assertEquals(Result.Status.WARN, service.execute().getStatus());
	}

	@Test
	void readinessShapesConfiguredButEmptyIsCritical() {
		// The dangerous case: an operator believes the portal validates, and it does not.
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir);
		assertEquals(Result.Status.CRITICAL, service.execute().getStatus());
	}

	@Test
	void readinessShapesConfiguredButDirectoryMissingIsCritical() {
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(shapesDir.resolve("does-not-exist"));
		assertEquals(Result.Status.CRITICAL, service.execute().getStatus());
	}

	@Test
	void readinessShapesLoadedIsOk() throws IOException {
		DcatValidationServiceImpl service = serviceWithTitleShape();
		Result result = service.execute();
		assertEquals(Result.Status.OK, result.getStatus());
		assertTrue(result.isOk());
		assertTrue(result.toString().contains("1 shape file(s) loaded"), result.toString());
	}

}

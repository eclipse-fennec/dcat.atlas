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
 * at runtime, not bundled here.
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

	@TempDir
	Path shapesDir;

	private DcatValidationServiceImpl serviceWithTitleShape() throws IOException {
		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		return new DcatValidationServiceImpl(ValidationTestResourceSets.factory(), shapesDir);
	}

	@Test
	void conformsWhenMandatoryFieldPresent() throws IOException {
		ValidationResult result = serviceWithTitleShape().validate(dataset(BASE + "air", "Air quality"));
		assertTrue(result.conforms(), result.reportTurtle());
		assertTrue(result.violations().isEmpty());
	}

	@Test
	void reportsViolationWhenMandatoryFieldMissing() throws IOException {
		ValidationResult result = serviceWithTitleShape().validate(dataset(BASE + "air", null));
		assertFalse(result.conforms());
		assertEquals(1, result.violations().size());
		assertTrue(result.violations().get(0).message().contains("dct:title"),
				result.violations().get(0).message());
		// The native SHACL report is carried as Turtle for the RDF (422) body.
		assertTrue(result.reportTurtle().contains("ValidationReport"), result.reportTurtle());
	}

	@Test
	void validationDisabledWhenNoShapesConfigured() {
		// Empty directory -> no shapes loaded -> everything "conforms" (enforcement is
		// the caller's concern; the service just reports).
		DcatValidationServiceImpl service = new DcatValidationServiceImpl(ValidationTestResourceSets.factory(),
				shapesDir);
		ValidationResult result = service.validate(dataset(BASE + "air", null));
		assertTrue(result.conforms());
		assertTrue(result.violations().isEmpty());
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

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
package org.eclipse.fennec.dcat.atlas.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.validation.ReportEntry;
import org.apache.jena.shacl.validation.Severity;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ShaclViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Dataset;
import dcat.DcatFactory;

/**
 * On-write SHACL enforcement at the <em>service</em>, not the REST resource.
 *
 * <h2>The gap these tests close</h2>
 *
 * Enforcement used to live in the five admin REST resources, so a caller that reached
 * {@code upsertDataset} directly — an importer, a migration script, another bundle — wrote
 * unvalidated while the identical entity over HTTP was refused. Every test here calls the
 * service directly, with no REST layer in sight; before the check moved they would all have
 * stored the entity.
 * <p>
 * The reports are hand-built rather than produced by running shapes: what is under test is
 * the <em>decision</em> — which severities block, what happens when the service is absent or
 * enforcement is off — not Jena's SHACL engine, which has its own tests and is exercised
 * end to end by {@code WriteValidationIntegrationTest}.
 */
public class ShaclWriteEnforcementTest {

	private static final String BASE = org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.LOGICAL_BASE
			+ org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout.DATASETS + "/";

	private static final String SHACL_MIN_COUNT = "http://www.w3.org/ns/shacl#MinCountConstraintComponent";

	@TempDir
	Path storage;

	@Test
	void aHardViolationRefusesTheWrite() {
		DatasetAdminServiceImpl service = service(enforcing(report(Severity.Violation)));

		ShaclViolationException refused = assertThrows(ShaclViolationException.class,
				() -> service.upsertDataset(dataset()));

		assertNotNull(refused.getReport(), "the native report must survive the throw — it is the whole diagnostic");
		assertTrue(refused.getMessage().contains(BASE + "air"),
				"the message should name the entity: " + refused.getMessage());
	}

	@Test
	void aRefusedWriteLeavesNothingOnDisk() {
		DatasetAdminServiceImpl service = service(enforcing(report(Severity.Violation)));

		assertThrows(ShaclViolationException.class, () -> service.upsertDataset(dataset()));

		assertTrue(service(null).getDataset("air").isEmpty(),
				"validation runs before the file is written, so a refused entity must not exist");
	}

	/**
	 * DCAT-AP.de "SOLL". A recommendation is reported by the dry run but must never block —
	 * most F-22 controlled-vocabulary findings are warnings, and blocking on them would make
	 * a conformant-but-unfashionable licence unpublishable.
	 */
	@Test
	void aWarningDoesNotBlock() {
		DatasetAdminServiceImpl service = service(enforcing(report(Severity.Warning)));

		service.upsertDataset(dataset());

		assertTrue(service(null).getDataset("air").isPresent());
	}

	@Test
	void enforcementOffStoresTheEntityAnyway() {
		DatasetAdminServiceImpl service = service(new FakeValidation(report(Severity.Violation), false));

		service.upsertDataset(dataset());

		assertTrue(service(null).getDataset("air").isPresent(),
				"isWriteEnforced() is operator policy; a violating entity is reported, not refused");
	}

	@Test
	void noValidationServiceMeansNoEnforcement() {
		service(null).upsertDataset(dataset());

		assertTrue(service(null).getDataset("air").isPresent(),
				"absence fails open by design — the strict reading is validationService.cardinality.minimum=1, "
						+ "which removes the admin service rather than letting writes through");
	}

	// --- fixtures -----------------------------------------------------------

	private DatasetAdminServiceImpl service(DcatValidationService validation) {
		DatasetAdminServiceImpl service = new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		service.validationService = validation;
		return service;
	}

	private static DcatValidationService enforcing(ValidationReport report) {
		return new FakeValidation(report, true);
	}

	private static ValidationReport report(Severity severity) {
		ValidationReport.Builder builder = ValidationReport.create();
		builder.addReportEntry(ReportEntry.create() //
				.severity(severity) //
				.focusNode(NodeFactory.createURI(BASE + "air")) //
				// Required: ValidationReport materializes each entry into a graph on build,
				// and sh:sourceConstraintComponent is not optional there.
				.sourceConstraintComponent(NodeFactory.createURI(SHACL_MIN_COUNT)) //
				.message("test entry"));
		return builder.build();
	}

	/**
	 * Conformant to the <em>model</em>, so that only SHACL can refuse it — otherwise these
	 * tests would pass on a ModelConstraintException and prove nothing about SHACL.
	 */
	private static Dataset dataset() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(BASE + "air");
		return TestEntities.mandatoryDataset(dataset, "Air quality");
	}

	/** Returns a fixed report, so the test pins the decision rather than Jena's engine. */
	private record FakeValidation(ValidationReport report, boolean enforced) implements DcatValidationService {

		@Override
		public ValidationReport validate(EObject entity) {
			return report;
		}


		@Override
		public boolean isWriteEnforced() {
			return enforced;
		}
	}
}

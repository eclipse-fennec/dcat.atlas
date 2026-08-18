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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.ResultLog;
import org.apache.felix.hc.api.Result.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.apache.jena.shacl.ValidationReport;

/**
 * The check that explains a write path which has vanished (F-25).
 * <p>
 * Raising {@code validationService.cardinality.minimum=1} is the operator's way to require
 * SHACL enforcement, but its symptom is a bare {@code 404} on the admin endpoints — the
 * JAX-RS whiteboard unregisters a resource whose service reference is unsatisfied. These
 * tests pin the wording that turns that into something actionable.
 */
public class AdminWriteHealthCheckTest {

	@TempDir
	Path storage;

	@Test
	void allServicesPresentIsOk() {
		AdminWriteHealthCheck check = check(true);

		Result result = check.execute();

		assertEquals(Status.OK, result.getStatus());
		assertTrue(messages(result).contains("all five admin write services are available"));
	}

	@Test
	void aMissingAdminServiceIsCriticalAndNamesTheCollection() {
		AdminWriteHealthCheck check = check(true);
		check.datasets = null;

		Result result = check.execute();

		assertEquals(Status.CRITICAL, result.getStatus());
		assertTrue(messages(result).contains("datasets"), messages(result));
	}

	/** The whole point: say why the endpoint 404s instead of leaving it to be guessed. */
	@Test
	void theCriticalMessageExplainsThe404() {
		AdminWriteHealthCheck check = check(true);
		check.datasets = null;

		String messages = messages(check.execute());

		assertTrue(messages.contains("404"), messages);
	}

	@Test
	void aMissingValidationServiceIsNamedAsTheLikelyCause() {
		AdminWriteHealthCheck check = check(false);
		check.datasets = null;

		String messages = messages(check.execute());

		assertTrue(messages.contains("cardinality.minimum=1"), messages);
	}

	/** With the validation service present, the cardinality explanation would be a red herring. */
	@Test
	void withValidationPresentTheCauseIsNotBlamedOnIt() {
		AdminWriteHealthCheck check = check(true);
		check.datasets = null;

		String messages = messages(check.execute());

		assertTrue(messages.contains("cause is elsewhere"), messages);
	}

	@Test
	void theHealthyReportStatesWhetherEnforcementIsOn() {
		assertTrue(messages(check(true).execute()).contains("SHACL enforcement: on"));
	}

	// --- fixtures -----------------------------------------------------------

	private AdminWriteHealthCheck check(boolean withValidation) {
		AdminWriteHealthCheck check = new AdminWriteHealthCheck();
		check.catalogs = new CatalogAdminServiceImpl(TestResourceSets.factory(), storage);
		check.datasets = new DatasetAdminServiceImpl(TestResourceSets.factory(), storage);
		check.datasetSeries = new DatasetSeriesAdminServiceImpl(TestResourceSets.factory(), storage);
		check.dataServices = new DataServiceAdminServiceImpl(TestResourceSets.factory(), storage);
		check.distributions = new DistributionAdminServiceImpl(TestResourceSets.factory(), storage,
				new DatasetReadOnlyServiceImpl(TestResourceSets.factory(), storage));
		check.validation = withValidation ? new AlwaysEnforcing() : null;
		return check;
	}

	/** {@link Result} is {@code Iterable}, not streamable. */
	private static String messages(Result result) {
		StringBuilder all = new StringBuilder();
		for (ResultLog.Entry entry : result) {
			all.append(entry.getMessage()).append(" | ");
		}
		return all.toString();
	}

	private record AlwaysEnforcing() implements DcatValidationService {

		@Override
		public ValidationReport validate(EObject entity) {
			return ValidationReport.reportConformsTrue();
		}

		@Override
		public ValidationResult validateLegacy(EObject entity) {
			throw new UnsupportedOperationException("not used by the health check");
		}

		@Override
		public boolean isWriteEnforced() {
			return true;
		}
	}
}

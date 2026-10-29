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

import java.nio.file.Path;

import org.apache.felix.hc.api.Result.Status;
import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.ResultLog;
import org.apache.jena.shacl.ValidationReport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The check that explains a write path which has vanished (F-25).
 * <p>
 * Configuring the store with {@code validateOnWrite} is the operator's way to require
 * SHACL enforcement, but its symptom is a bare {@code 404} on the admin endpoints — the
 * JAX-RS whiteboard unregisters a resource whose service reference is unsatisfied. These
 * tests pin the wording that turns that into something actionable.
 * <p>
 * There is no per-collection case left to test: the handlers are plain objects built by
 * one DcatAdminService, so they are all there or none of them is.
 */
public class AdminWriteHealthCheckTest {

	@TempDir
	Path storage;

	@Test
	void allServicesPresentIsOk() {
		AdminWriteHealthCheck check = check(true);

		Result result = check.execute();

		assertEquals(Status.OK, result.getStatus());
		assertTrue(messages(result).contains("the admin write services are available"));
	}

	@Test
	void aMissingAdminServiceIsCritical() {
		AdminWriteHealthCheck check = check(true);
		withoutAdmin(check);

		Result result = check.execute();

		assertEquals(Status.CRITICAL, result.getStatus());
		assertTrue(messages(result).contains("no DcatAdminService is registered"), messages(result));
	}

	/** The first thing to check, and the one an operator is most likely to have missed. */
	@Test
	void theCriticalMessagePointsAtTheConfiguration() {
		AdminWriteHealthCheck check = check(true);
		withoutAdmin(check);

		String messages = messages(check.execute());

		assertTrue(messages.contains("ConfigurationPolicy.REQUIRE"), messages);
	}

	/** The whole point: say why the endpoint 404s instead of leaving it to be guessed. */
	@Test
	void theCriticalMessageExplainsThe404() {
		AdminWriteHealthCheck check = check(true);
		withoutAdmin(check);

		String messages = messages(check.execute());

		assertTrue(messages.contains("404"), messages);
	}

	@Test
	void aMissingValidationServiceIsNamedAsTheLikelyCause() {
		AdminWriteHealthCheck check = check(false);
		withoutAdmin(check);

		String messages = messages(check.execute());

		assertTrue(messages.contains("cardinality.minimum=1"), messages);
	}

	/** With the validation service present, the cardinality explanation would be a red herring. */
	@Test
	void withValidationPresentTheCauseIsNotBlamedOnIt() {
		AdminWriteHealthCheck check = check(true);
		withoutAdmin(check);

		String messages = messages(check.execute());

		assertTrue(messages.contains("cause is elsewhere"), messages);
	}

	@Test
	void theHealthyReportStatesWhetherEnforcementIsOn() {
		assertTrue(messages(check(true).execute()).contains("SHACL enforcement: on"));
	}

	// --- fixtures -----------------------------------------------------------

	/** The facade the fixture bound, so a test can unbind exactly that one. */
	private DcatAdminService boundDcatAdminService;

	private AdminWriteHealthCheck check(boolean withValidation) {
		AdminWriteHealthCheck check = new AdminWriteHealthCheck();
		boundDcatAdminService = TestAdminServices.facade(storage);
		check.setDcatAdminService(boundDcatAdminService);
		if (withValidation) {
			check.setValidation(new AlwaysEnforcing());
		}
		return check;
	}

	/** Unbinds the facade the way DS would, rather than clearing the field. */
	private void withoutAdmin(AdminWriteHealthCheck check) {
		check.unsetDcatAdminService(boundDcatAdminService);
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
		public boolean isWriteEnforced() {
			return true;
		}
	}
}

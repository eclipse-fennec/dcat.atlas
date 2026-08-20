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

import org.apache.felix.hc.api.Result;
import org.apache.felix.hc.api.Result.Status;
import org.apache.felix.hc.api.ResultLog;
import org.junit.jupiter.api.Test;

/**
 * The check that explains a portal which cannot render identities (F-25).
 * <p>
 * {@code publicBaseUrl} has no default, so an unconfigured deployment loses the
 * {@code PublicIris} service and every REST resource with it. Before this check the
 * readiness endpoint blamed SPARQL. These tests pin the wording that points at the
 * setting instead.
 */
public class PublicIrisHealthCheckTest {

	private static final String PUBLIC = "https://opendata.example.de/dcat/rest/";

	@Test
	void aBoundServiceReportsTheBaseItRendersUnder() {
		PublicIrisHealthCheck check = new PublicIrisHealthCheck();
		check.publicIris = new PublicIrisImpl(PUBLIC);

		Result result = check.execute();

		assertEquals(Status.OK, result.getStatus());
		// The base itself is the useful part: it is what every published 'about' will
		// carry, so an operator can tell a proxy misconfiguration from a working one.
		assertTrue(messages(result).contains(PUBLIC), messages(result));
	}

	@Test
	void aMissingServiceIsCriticalAndNamesTheSetting() {
		PublicIrisHealthCheck check = new PublicIrisHealthCheck();
		check.publicIris = null;

		Result result = check.execute();

		assertEquals(Status.CRITICAL, result.getStatus());
		String messages = messages(result);
		assertTrue(messages.contains("PUBLIC_BASE_URL"), messages);
		assertTrue(messages.contains("publicBaseUrl"), messages);
	}

	@Test
	void aMissingServiceExplainsThe404() {
		// The symptom an operator actually sees is a 404 from every /rest/... path,
		// because the resources hold PublicIris as a mandatory reference. A check that
		// did not say so would leave them hunting a routing problem.
		PublicIrisHealthCheck check = new PublicIrisHealthCheck();
		check.publicIris = null;

		assertTrue(messages(check.execute()).contains("404"));
	}

	private static String messages(Result result) {
		StringBuilder all = new StringBuilder();
		for (ResultLog.Entry entry : result) {
			all.append(entry.getMessage()).append(" | ");
		}
		return all.toString();
	}
}

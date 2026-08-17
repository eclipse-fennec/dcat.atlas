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
package org.eclipse.fennec.dcat.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * End-to-end HTTP test for the F-25 health and readiness endpoints, served by the Apache
 * Felix Health Check executor servlet.
 * <p>
 * Note the URLs are <em>not</em> under {@code /rest}: the servlet is registered on the
 * HTTP whiteboard directly, while the DCAT resources live behind the Jersey JAX-RS
 * whiteboard at {@code /rest}.
 * <p>
 * The test runtime configures the store directories under {@code /tmp/dcat-atlas-test-store}
 * (see {@code configs/config.json} — deliberately not the live runtime's store) and points the
 * SHACL shapes directory at {@code /tmp/dcat-shapes-unset}, which does not exist. That
 * exercises the interesting readiness case on purpose: stores are ready (creatable even
 * before the first write) while the shapes check is CRITICAL, so readiness must be 503 —
 * the misconfiguration a silently unvalidating portal would otherwise hide.
 * <p>
 * <b>CRITICAL here is the expected steady state, not a broken fixture.</b> The DCAT-AP.de
 * shapes are AGPL-3.0 and are deliberately not vendored (see {@code NOTICE.md}), so no test
 * can point this runtime at a real shapes directory. Readiness is therefore always 503 in
 * this suite, and {@link #configuredButUnloadedShapesMakeReadinessCritical()} asserts
 * exactly that. Assert on the individual checks rather than on {@code overallResult}.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class HealthEndpointIntegrationTest {

	private static final String HOST = "http://localhost:8185";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@Test
	void livenessIsUpAndIndependentOfEverythingElse() throws Exception {
		HttpResponse<String> live = get("/health/live");
		// Liveness must stay 200 even though readiness is failing on the shapes check:
		// a restart would not fix missing shapes, so it must not be requested.
		assertEquals(200, live.statusCode(), live.body());
		assertTrue(live.body().contains("DCAT.Atlas is running"), live.body());
	}

	@Test
	void readinessAggregatesEveryStoreAndTheShapesCheck() throws Exception {
		String body = get("/health/ready").body();
		assertTrue(body.contains("store:catalogs"), body);
		assertTrue(body.contains("store:datasets"), body);
		assertTrue(body.contains("store:data-services"), body);
		assertTrue(body.contains("store:dataset-series"), body);
		// Four stores, not five: a Distribution is contained in its Dataset and has no
		// store of its own, so there is no store:distributions check to report.
		assertFalse(body.contains("store:distributions"), body);
		assertTrue(body.contains("shacl"), body);
	}

	@Test
	void everyStoreReportsOk() throws Exception {
		String body = get("/health/ready").body();
		// One OK per store. The "absent but creatable" case that keeps a fresh install
		// serving cannot be forced here (the test store's dirs may already exist from an
		// earlier run), so that semantic is pinned by StoreHealthTest instead.
		// Four: catalogs, datasets, data-services, dataset-series. Distributions are
		// contained in their Dataset rather than stored on their own.
		assertEquals(4, countOf(body, "\"name\":\"store:"), body);
		// No store may be in a state that would take the portal out of rotation.
		assertTrue(!body.contains("cannot be created"), body);
		assertTrue(!body.contains("is not a directory"), body);
		assertTrue(!body.contains("is not readable"), body);
	}

	@Test
	void configuredButUnloadedShapesMakeReadinessCritical() throws Exception {
		HttpResponse<String> ready = get("/health/ready");
		assertEquals(503, ready.statusCode(), ready.body());
		assertTrue(ready.body().contains("CRITICAL"), ready.body());
		assertTrue(ready.body().contains("would silently pass everything"), ready.body());
	}

	@Test
	void servicesCheckCoversAnAbsentValidationService() throws Exception {
		// The gap a self-registering contributor could not close: ServicesCheck names the
		// service that must be present, so a component that failed to activate is caught
		// rather than silently invisible. Here it is present, so the check passes.
		String body = get("/health/ready").body();
		assertTrue(body.contains("\"name\":\"services\""), body);
		// Felix reports the count, not the filter, so assert on its verdict.
		assertTrue(body.contains("All 1 required services are available"), body);
	}

	private static int countOf(String haystack, String needle) {
		int n = 0;
		for (int i = haystack.indexOf(needle); i >= 0; i = haystack.indexOf(needle, i + 1)) {
			n++;
		}
		return n;
	}

	private HttpResponse<String> get(String path) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(HOST + path)).GET().header("Accept", "application/json")
				.timeout(Duration.ofSeconds(10)).build(), BodyHandlers.ofString());
	}

}

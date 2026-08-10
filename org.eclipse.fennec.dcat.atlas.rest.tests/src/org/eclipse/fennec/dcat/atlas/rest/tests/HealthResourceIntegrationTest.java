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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.eclipse.fennec.dcat.atlas.rest.tests.helper.RestReady;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * End-to-end HTTP test for the F-25 health and readiness endpoints.
 * <p>
 * The test runtime configures the five store directories under {@code /tmp/rdf} and
 * points the SHACL shapes directory at {@code /tmp/dcat-shapes-unset}, which does not
 * exist. That combination exercises the interesting readiness case on purpose: the
 * stores are ready (they are creatable even before the first write), while the shapes
 * are configured-but-not-loaded, so the aggregate must be NOT_READY with 503 — the
 * misconfiguration a silently unvalidating portal would otherwise hide.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class HealthResourceIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@BeforeEach
	void ensureResources(@InjectBundleContext BundleContext context) throws InterruptedException {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
	}

	@Test
	void healthIsUpAndChecksNoDependencies() throws Exception {
		HttpResponse<String> health = get("/health");
		assertEquals(200, health.statusCode(), health.body());
		assertTrue(health.body().contains("\"status\":\"UP\""), health.body());
	}

	@Test
	void readyAggregatesEveryContributor() throws Exception {
		HttpResponse<String> ready = get("/ready");
		String body = ready.body();

		// One contributor per store, plus the SHACL one.
		assertTrue(body.contains("\"store:catalogs\""), body);
		assertTrue(body.contains("\"store:datasets\""), body);
		assertTrue(body.contains("\"store:data-services\""), body);
		assertTrue(body.contains("\"store:dataset-series\""), body);
		assertTrue(body.contains("\"store:distributions\""), body);
		assertTrue(body.contains("\"shacl\""), body);
	}

	@Test
	void storesReportReadyEvenBeforeAnythingIsWritten() throws Exception {
		String body = get("/ready").body();
		// Store readiness must not depend on the directory already existing, or a fresh
		// install would never start serving.
		assertTrue(body.contains("\"store:catalogs\",\"ready\":true"), body);
	}

	@Test
	void configuredButUnloadedShapesMakeThePortalNotReady() throws Exception {
		HttpResponse<String> ready = get("/ready");
		assertEquals(503, ready.statusCode(), ready.body());
		assertTrue(ready.body().contains("\"status\":\"NOT_READY\""), ready.body());
		assertTrue(ready.body().contains("\"shacl\",\"ready\":false"), ready.body());
		assertTrue(ready.body().contains("would silently pass everything"), ready.body());
	}

	@Test
	void readinessBodyIsWellFormedJson() throws Exception {
		String body = get("/ready").body();
		// Hand-built JSON: guard the shape so a stray quote in a path cannot corrupt it.
		assertTrue(body.startsWith("{\"status\":\""), body);
		assertTrue(body.endsWith("]}"), body);
		assertEquals(countOf(body, '['), countOf(body, ']'), body);
	}

	private static long countOf(String value, char c) {
		return value.chars().filter(ch -> ch == c).count();
	}

	private HttpResponse<String> get(String path) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(BASE + path)).GET().header("Accept", "application/json")
				.timeout(Duration.ofSeconds(10)).build(), BodyHandlers.ofString());
	}

}

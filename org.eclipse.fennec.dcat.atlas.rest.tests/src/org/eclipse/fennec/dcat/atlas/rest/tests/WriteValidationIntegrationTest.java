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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.dcat.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.dcat.atlas.rest.tests.helper.RestReady;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Exercises on-write SHACL enforcement (FR-4) end-to-end over HTTP: with enforcement
 * configured on the {@code DcatValidationService}, a create whose payload violates the
 * shapes is rejected with {@code 422} and the {@code sh:ValidationReport}, while a
 * conformant payload is created (201).
 * <p>
 * As in {@link ValidationResourceIntegrationTest}, the shape is written to a temp dir
 * and the service is reconfigured via {@link ConfigurationAdmin} (here also flipping
 * {@code enforceOnWrite=true}) so the test provisions everything itself and depends on
 * no external (GovData/AGPL) shapes. Both config keys are set in one update, so once
 * the dry-run reports the shape as live, enforcement is live too.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class WriteValidationIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String ADMIN_DATASETS = BASE + "/admin/datasets";
	private static final String RDF_XML = "application/rdf+xml";
	private static final String PID = "DcatValidationService";
	private static final String DEFAULT_SHAPES_DIR = "/tmp/dcat-shapes-unset";
	/** Fixed id for the idempotent readiness probe so it never mints throw-away resources. */
	private static final String PROBE_ID = "write-validation-readiness-probe";

	/** Minimal shape: a dcat:Dataset must carry at least one dct:title. */
	private static final String TITLE_SHAPE = """
			@prefix sh:   <http://www.w3.org/ns/shacl#> .
			@prefix dcat: <http://www.w3.org/ns/dcat#> .
			@prefix dct:  <http://purl.org/dc/terms/> .
			@prefix ex:   <http://example/shapes#> .

			ex:DatasetShape a sh:NodeShape ;
			    sh:targetClass dcat:Dataset ;
			    sh:property [ sh:path dct:title ; sh:minCount 1 ;
			                  sh:message "A Dataset must have a dct:title." ] .
			""";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	ConfigurationAdmin configAdmin;

	@TempDir
	Path shapesDir;

	@BeforeEach
	void enableEnforcement(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "DatasetAdminResource").waitForResource(15, TimeUnit.SECONDS),
				"DatasetAdminResource should be registered within 15 seconds.");

		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		updateValidationConfig(shapesDir.toString(), true);

		// The reconfigured validation service rebinds into the (dynamic) admin resource
		// asynchronously, so poll the admin write path itself until enforcement is live:
		// an idempotent PUT of a title-less dataset flips from 2xx to 422. Using PUT to a
		// fixed id keeps the probe from minting a new resource per attempt.
		HttpResponse<String> ready = awaitEnforcementActive(30_000);
		assertEquals(422, ready.statusCode(),
				"enforcement did not become active after configuring shapes; last probe: " + ready.statusCode() + " "
						+ ready.body());
	}

	@AfterEach
	void disableEnforcement() throws Exception {
		// Leave the runtime as later test classes expect it: no shapes, no enforcement.
		updateValidationConfig(DEFAULT_SHAPES_DIR, false);
		// Best-effort cleanup of the title-less dataset the readiness probe may have stored
		// before enforcement kicked in.
		http.send(delete(ADMIN_DATASETS + "/" + PROBE_ID), BodyHandlers.discarding());
	}

	@Test
	void nonConformantCreateIsRejectedAndConformantSucceeds() throws Exception {
		HttpResponse<String> rejected = post(ADMIN_DATASETS, datasetBody("no-title", null));
		assertEquals(422, rejected.statusCode(), rejected.body());
		assertEquals("false", conformsHeader(rejected));
		assertTrue(rejected.body().contains("ValidationReport"), rejected.body());

		HttpResponse<String> created = post(ADMIN_DATASETS, datasetBody("with-title", "Air quality"));
		assertEquals(201, created.statusCode(), created.body());
	}

	// --- helpers -----------------------------------------------------------

	private void updateValidationConfig(String directory, boolean enforceOnWrite) throws IOException {
		Configuration configuration = configAdmin.getConfiguration(PID, "?");
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("shapesDirectory", directory);
		properties.put("enforceOnWrite", enforceOnWrite);
		configuration.update(properties);
	}

	/**
	 * Polls the admin PUT (idempotent, fixed id) with a title-less dataset until it is
	 * rejected with 422 — i.e. the reconfigured validation service has rebound and
	 * enforcement is live. Returns the last response (its status is asserted by the caller).
	 */
	private HttpResponse<String> awaitEnforcementActive(long timeoutMillis) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		HttpResponse<String> last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				last = http.send(put(ADMIN_DATASETS + "/" + PROBE_ID, datasetBody(PROBE_ID, null)),
						BodyHandlers.ofString());
				if (last.statusCode() == 422) {
					return last;
				}
			} catch (IOException connecting) {
				// endpoint momentarily down during a whiteboard reload; retry
			}
			Thread.sleep(250);
		}
		return last == null ? http.send(put(ADMIN_DATASETS + "/" + PROBE_ID, datasetBody(PROBE_ID, null)),
				BodyHandlers.ofString()) : last;
	}

	private HttpResponse<String> post(String url, String body) throws IOException, InterruptedException {
		return http.send(withRdfBody(HttpRequest.newBuilder(URI.create(url))).POST(BodyPublishers.ofString(body))
				.build(), BodyHandlers.ofString());
	}

	private HttpRequest put(String url, String body) {
		return withRdfBody(HttpRequest.newBuilder(URI.create(url))).PUT(BodyPublishers.ofString(body)).build();
	}

	private static HttpRequest delete(String url) {
		return HttpRequest.newBuilder(URI.create(url)).DELETE().timeout(Duration.ofSeconds(10)).build();
	}

	private static HttpRequest.Builder withRdfBody(HttpRequest.Builder builder) {
		return builder.header("Content-Type", RDF_XML).header("Accept", RDF_XML).timeout(Duration.ofSeconds(10));
	}

	private static String conformsHeader(HttpResponse<String> response) {
		return response.headers().firstValue("X-SHACL-Conforms").orElse(null);
	}

	private static String datasetBody(String id, String title) {
		String titleElement = title == null ? "" : "<dct:title xml:lang=\"en\">" + title + "</dct:title>";
		return """
				<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				         xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmlns:dct="http://purl.org/dc/terms/">
				  <dcat:Dataset rdf:about="%s/datasets/%s">%s</dcat:Dataset>
				</rdf:RDF>""".formatted(BASE, id, titleElement);
	}
}

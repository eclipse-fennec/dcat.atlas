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
 * Exercises the FR-5 dry-run endpoint against a <em>real</em> (tiny, self-authored)
 * SHACL shape, end-to-end over HTTP. The shape is written to a temp dir and the
 * {@code DcatValidationService} is reconfigured (via {@link ConfigurationAdmin}) to
 * load it — so the test provisions everything itself and depends on no external
 * (GovData/AGPL) shapes. Reconfiguring reactivates the service and re-registers
 * {@code ValidationResource}, so we poll the endpoint until validation is live.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ValidationResourceIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String VALIDATE_DATASETS = BASE + "/admin/validate/datasets";
	/** The only write format; see {@link #validate}. */
	private static final String XMI = "application/xmi";
	private static final String TURTLE = "text/turtle";
	private static final String PID = "DcatValidationService";
	private static final String DEFAULT_SHAPES_DIR = "/tmp/dcat-shapes-unset";

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
	void configureShapes(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "ValidationResource").waitForResource(15, TimeUnit.SECONDS),
				"ValidationResource should be registered within 15 seconds.");

		Files.writeString(shapesDir.resolve("title-shape.ttl"), TITLE_SHAPE);
		updateShapesDirectory(shapesDir.toString());

		// Reconfiguring reactivates the service + resource asynchronously; wait until
		// validation is actually live (a title-less dataset now fails to conform).
		assertTrue(awaitValidationActive(20_000), "validation did not become active after configuring shapes");
	}

	@AfterEach
	void resetShapes() throws Exception {
		// Point back at the (non-existent) default so later test classes see no shapes.
		updateShapesDirectory(DEFAULT_SHAPES_DIR);
	}

	@Test
	void dryRunReportsViolationForMissingTitleAndConformsForValid() throws Exception {
		HttpResponse<String> missing = validate(datasetBody("no-title", null));
		assertEquals(200, missing.statusCode(), missing.body());
		assertEquals("false", conformsHeader(missing));
		assertTrue(missing.body().contains("ValidationReport"), missing.body());

		HttpResponse<String> valid = validate(datasetBody("with-title", "Air quality"));
		assertEquals(200, valid.statusCode(), valid.body());
		assertEquals("true", conformsHeader(valid));
	}

	// --- helpers -----------------------------------------------------------

	private void updateShapesDirectory(String directory) throws IOException {
		Configuration configuration = configAdmin.getConfiguration(PID, "?");
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("shapesDirectory", directory);
		configuration.update(properties);
	}

	/** Polls the dry-run endpoint until a title-less dataset is reported non-conformant. */
	private boolean awaitValidationActive(long timeoutMillis) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		while (System.currentTimeMillis() < deadline) {
			try {
				HttpResponse<String> response = validate(datasetBody("probe", null));
				if (response.statusCode() == 200 && "false".equals(conformsHeader(response))) {
					return true;
				}
			} catch (IOException connecting) {
				// endpoint momentarily down during the whiteboard reload; retry
			}
			Thread.sleep(200);
		}
		return false;
	}

	/**
	 * The body is XMI and only the report comes back as RDF. An RDF/XML request body stopped
	 * being readable when the reader was dropped for the Jena converter — it now earns a 415,
	 * which the readiness poll above sees as "validation never became active".
	 */
	private HttpResponse<String> validate(String body) throws IOException, InterruptedException {
		return http.send(HttpRequest.newBuilder(URI.create(VALIDATE_DATASETS)).header("Content-Type", XMI)
				.header("Accept", TURTLE).POST(BodyPublishers.ofString(body)).timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
	}

	private static String conformsHeader(HttpResponse<String> response) {
		return response.headers().firstValue("X-SHACL-Conforms").orElse(null);
	}

	private static String datasetBody(String id, String title) {
		String titleElement = title == null ? "" : "\n  <title lang=\"en\" value=\"%s\"/>".formatted(title);
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="http://dcat.atlas/datasets/%s">%s
				</dcat:Dataset>""".formatted(id, titleElement);
	}
}

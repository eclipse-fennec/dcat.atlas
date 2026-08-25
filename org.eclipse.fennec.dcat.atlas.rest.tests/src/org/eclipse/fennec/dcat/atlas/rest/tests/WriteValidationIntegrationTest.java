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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.validation.ShaclViolationException;
import org.eclipse.fennec.dcat.atlas.rest.tests.helper.ResourceAware;
import dcat.Dataset;
import dcat.DcatFactory;
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
	/** The only write format: the codec picks its codec by media type (application/xml selects a plain-XML one). */
	private static final String XMI = "application/xmi";
	private static final String PID = "DcatValidationService";
	private static final String DEFAULT_SHAPES_DIR = "/tmp/dcat-shapes-unset";
	/** Fixed id for the idempotent readiness probe so it never mints throw-away resources. */
	private static final String PROBE_ID = "write-validation-readiness-probe";
	/** Fixed id for the direct-service tests, cleaned up with the probe. */
	private static final String DIRECT_ID = "write-validation-direct-call";

	/**
	 * Minimal shape: a dcat:Dataset must carry at least one dcat:keyword.
	 * <p>
	 * It used to require {@code dct:title}, which stopped working as a test once the model
	 * required a title too: the model check runs first, so the entity was refused before
	 * SHACL ever saw it and this suite would have been asserting on the wrong layer.
	 * {@code dcat:keyword} is optional throughout the model, so a violation here can only
	 * come from the shapes.
	 */
	private static final String KEYWORD_SHAPE = """
			@prefix sh:   <http://www.w3.org/ns/shacl#> .
			@prefix dcat: <http://www.w3.org/ns/dcat#> .
			@prefix ex:   <http://example/shapes#> .

			ex:DatasetShape a sh:NodeShape ;
			    sh:targetClass dcat:Dataset ;
			    sh:property [ sh:path dcat:keyword ; sh:minCount 1 ;
			                  sh:message "A Dataset must have a dcat:keyword." ] .
			""";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	ConfigurationAdmin configAdmin;

	/** The service itself, to prove enforcement no longer depends on going through REST. */
	@InjectService
	DatasetAdminService datasetAdminService;

	@TempDir
	Path shapesDir;

	@BeforeEach
	void enableEnforcement(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "DatasetAdminResource").waitForResource(15, TimeUnit.SECONDS),
				"DatasetAdminResource should be registered within 15 seconds.");

		Files.writeString(shapesDir.resolve("keyword-shape.ttl"), KEYWORD_SHAPE);
		updateValidationConfig(shapesDir.toString(), true);

		// The reconfigured validation service rebinds into the (dynamic) admin resource
		// asynchronously, so poll the admin write path itself until enforcement is live:
		// an idempotent PUT of a keyword-less dataset flips from 2xx to 422. Using PUT to a
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
		// Best-effort cleanup of the keyword-less dataset the readiness probe may have stored
		// before enforcement kicked in.
		http.send(delete(ADMIN_DATASETS + "/" + PROBE_ID), BodyHandlers.discarding());
		http.send(delete(ADMIN_DATASETS + "/" + DIRECT_ID), BodyHandlers.discarding());
	}

	@Test
	void aShapeViolationIsRejectedAndAConformantBodySucceeds() throws Exception {
		HttpResponse<String> rejected = post(ADMIN_DATASETS, datasetBody("no-keyword", null));
		assertEquals(422, rejected.statusCode(), rejected.body());
		assertEquals("false", conformsHeader(rejected));
		assertTrue(rejected.body().contains("ValidationReport"), rejected.body());

		try {
			HttpResponse<String> created = post(ADMIN_DATASETS, datasetBody("with-keyword", "air"));
			assertEquals(201, created.statusCode(), created.body());
		} finally {
			// The body names its own id, and a create now honours it — so leaving this behind
			// would make the next run of this test a 409 rather than the 201 it asserts. The
			// store outlives the run; a test that creates has to remove what it created.
			http.send(delete(ADMIN_DATASETS + "/with-keyword"), BodyHandlers.discarding());
		}
	}

	/**
	 * The add-member endpoint stores the Dataset before linking it, so enforcement has to
	 * hold there too — otherwise a payload rejected at {@code /admin/datasets} could be
	 * persisted by choosing a different path.
	 */
	@Test
	void aShapeViolatingMemberIsRejectedOnTheCatalogPath() throws Exception {
		String catalogId = "write-validation-catalog";
		String datasets = BASE + "/admin/catalogs/" + catalogId + "/datasets";
		assertEquals(201,
				http.send(put(BASE + "/admin/catalogs/" + catalogId,
						entityBody("Catalog", DcatIds.logicalIri(DcatIds.CATALOGS, catalogId), null)),
						BodyHandlers.ofString()).statusCode());
		try {
			HttpResponse<String> rejected = post(datasets, datasetBody("member-no-keyword", null));

			assertEquals(422, rejected.statusCode(), rejected.body());
			assertEquals("false", conformsHeader(rejected));
			assertTrue(rejected.body().contains("ValidationReport"), rejected.body());
			// Rejected before the write: no dataset was stored under that id.
			assertEquals(404, http.send(HttpRequest.newBuilder(URI.create(ADMIN_DATASETS + "/member-no-keyword"))
					.DELETE().timeout(Duration.ofSeconds(10)).build(), BodyHandlers.ofString()).statusCode());

			// ...and a conformant member still goes through.
			HttpResponse<String> accepted = post(datasets, datasetBody("member-with-keyword", "air"));
			assertEquals(200, accepted.statusCode(), accepted.body());
		} finally {
			http.send(delete(datasets + "/member-with-keyword"), BodyHandlers.discarding());
			http.send(delete(ADMIN_DATASETS + "/member-with-keyword"), BodyHandlers.discarding());
			http.send(delete(BASE + "/admin/catalogs/" + catalogId), BodyHandlers.discarding());
		}
	}

	// --- helpers -----------------------------------------------------------

	/**
	 * The reason on-write enforcement moved out of the REST resources.
	 * <p>
	 * This calls the OSGi service directly — no HTTP, no JAX-RS, nothing that could apply a
	 * check on the way past. Before the move it stored the entity happily while the same
	 * body over {@code POST /admin/datasets} was refused, which is precisely the asymmetry
	 * {@code ForeignIdentityException} was created to end for identity.
	 */
	@Test
	void aDirectServiceCallIsEnforcedToo() {
		// Model-conformant on purpose, and missing only the dcat:keyword the shape wants —
		// otherwise ModelConstraintException would be thrown first and this would prove
		// nothing about SHACL reaching a direct caller.
		Dataset keywordless = RestEntities.mandatoryDataset(DcatFactory.eINSTANCE.createDataset(), "Air quality");
		keywordless.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, DIRECT_ID));

		ShaclViolationException refused = assertThrows(ShaclViolationException.class,
				() -> datasetAdminService.upsertDataset(keywordless));

		assertNotNull(refused.getReport(), "the caller needs the report, not just a failure");
		assertFalse(refused.getReport().conforms(), "a conforming report would not have been thrown");
		assertTrue(datasetAdminService.getDataset(DIRECT_ID).isEmpty(),
				"nothing may reach the store when the service itself refuses");
	}

	/** The same call with a conformant entity still stores, so the check is not simply refusing everything. */
	@Test
	void aDirectServiceCallWithAConformantEntityStores() {
		Dataset dataset = RestEntities.mandatoryDataset(DcatFactory.eINSTANCE.createDataset(), "Directly written");
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, DIRECT_ID));
		dataset.getKeyword().add(RestEntities.literal("air"));

		datasetAdminService.upsertDataset(dataset);

		assertTrue(datasetAdminService.getDataset(DIRECT_ID).isPresent());
	}

	private void updateValidationConfig(String directory, boolean enforceOnWrite) throws IOException {
		Configuration configuration = configAdmin.getConfiguration(PID, "?");
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("shapesDirectory", directory);
		properties.put("enforceOnWrite", enforceOnWrite);
		configuration.update(properties);
	}

	/**
	 * Polls the admin PUT (idempotent, fixed id) with a keyword-less dataset until it is
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
		return http.send(withXmiBody(HttpRequest.newBuilder(URI.create(url))).POST(BodyPublishers.ofString(body))
				.build(), BodyHandlers.ofString());
	}

	private HttpRequest put(String url, String body) {
		return withXmiBody(HttpRequest.newBuilder(URI.create(url))).PUT(BodyPublishers.ofString(body)).build();
	}

	private static HttpRequest delete(String url) {
		return HttpRequest.newBuilder(URI.create(url)).DELETE().timeout(Duration.ofSeconds(10)).build();
	}

	/**
	 * Writes are XMI; only the <em>response</em> is RDF. RDF/XML request bodies stopped
	 * being readable when the reader was dropped for the Jena converter (they now earn a
	 * 415), while {@code Accept: application/rdf+xml} still serves both the created entity
	 * and the {@code sh:ValidationReport} of a 422.
	 */
	private static HttpRequest.Builder withXmiBody(HttpRequest.Builder builder) {
		return builder.header("Content-Type", XMI).header("Accept", RDF_XML).timeout(Duration.ofSeconds(10));
	}

	private static String conformsHeader(HttpResponse<String> response) {
		return response.headers().firstValue("X-SHACL-Conforms").orElse(null);
	}

	private static String datasetBody(String id, String keyword) {
		return entityBody("Dataset", DcatIds.logicalIri(DcatIds.DATASETS, id), keyword);
	}

	/**
	 * An XMI entity that always satisfies the <em>model</em> — title, description and
	 * publisher — and satisfies the <em>shape</em> only when {@code keyword} is given.
	 * That separation is the point: it isolates which layer refused the write.
	 */
	private static String entityBody(String type, String about, String keyword) {
		String keywordElement = keyword == null ? "" : "  <keyword lang=\"en\" value=\"%s\"/>%n".formatted(keyword);
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:%s xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s">
				  <title lang="en" value="Air quality"/>
				  <description lang="en" value="Integration-test fixture"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				%s</dcat:%s>""".formatted(type, about, keywordElement, type);
	}
}

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
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * On-write enforcement of the model's own constraints, end to end over HTTP.
 *
 * <h2>What this proves that the unit tests cannot</h2>
 *
 * {@code ModelConstraintValidationTest} installs the OCL delegates by hand, because a plain
 * JUnit test has no whiteboard. This one installs nothing: it proves the delegate really
 * does reach EMF's global registry from the {@code m2x} engine's OSGi service, that
 * {@code @RequireOCL} pulled the engine into the runtime, and that a violation surfaces as
 * {@code 422} rather than a {@code 500}.
 * <p>
 * Enforcement is off in the test runtime's configuration (as SHACL's is), so the test flips
 * {@code validateOnWrite} on the {@code DatasetAdminService} through
 * {@link ConfigurationAdmin} and restores it afterwards — the arrangement
 * {@link WriteValidationIntegrationTest} established. The store is shared with the live
 * runtime, so everything written here is cleaned up.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelConstraintWriteIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String ADMIN_DATASETS = BASE + "/admin/datasets";
	private static final String XMI = "application/xmi";
	private static final String PID = "DatasetAdminService";
	/** Fixed id for the idempotent readiness probe so it never mints throw-away resources. */
	private static final String PROBE_ID = "model-constraint-readiness-probe";
	private static final String CONFORMANT_ID = "model-constraint-conformant";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	ConfigurationAdmin configAdmin;

	@BeforeEach
	void enableEnforcement(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "DatasetAdminResource").waitForResource(15, TimeUnit.SECONDS),
				"DatasetAdminResource should be registered within 15 seconds.");

		setValidateOnWrite(true);

		// The reconfigured service rebinds asynchronously, so poll the write path itself
		// until enforcement is live: an idempotent PUT of a title-only dataset flips from
		// 2xx to 422. A fixed id keeps the probe from minting a resource per attempt.
		HttpResponse<String> ready = awaitEnforcementActive(30_000);
		assertEquals(422, ready.statusCode(), "enforcement did not become active; last probe: " + ready.statusCode()
				+ " " + ready.body());
	}

	@AfterEach
	void disableEnforcement() throws Exception {
		setValidateOnWrite(false);
		delete(ADMIN_DATASETS + "/" + PROBE_ID);
		delete(ADMIN_DATASETS + "/" + CONFORMANT_ID);
	}

	@Test
	void aDatasetMissingAMandatoryFeatureIsRejected() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));

		assertEquals(422, response.statusCode(), "a title-only Dataset violates publisher [1] and HasDescription");
		assertTrue(response.body().contains("publisher"),
				"the body should name the feature to fix, but was: " + response.body());
	}

	@Test
	void theRejectionNamesTheViolatedOclInvariant() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));

		assertEquals(422, response.statusCode());
		assertTrue(response.body().contains("HasDescription"),
				"the OCL invariant should be named, not just the multiplicities: " + response.body());
	}

	@Test
	void aConformantDatasetIsAccepted() throws Exception {
		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + CONFORMANT_ID, conformant(CONFORMANT_ID));

		assertTrue(response.statusCode() / 100 == 2,
				"a Dataset carrying title, description and publisher should be stored, but was: "
						+ response.statusCode() + " " + response.body());
	}

	@Test
	void anAnyUriThatIsNotAnIriIsRejected() throws Exception {
		String body = conformant(CONFORMANT_ID).replace("<title lang=\"en\"",
				"<theme>not-an-iri</theme>\n  <title lang=\"en\"");

		HttpResponse<String> response = put(ADMIN_DATASETS + "/" + CONFORMANT_ID, body);

		assertEquals(422, response.statusCode(),
				"a bare token in an AnyURI attribute must not reach the store: " + response.body());
		assertTrue(response.body().contains("ThemeIsIri"), "expected ThemeIsIri, but was: " + response.body());
	}

	// --- payloads -----------------------------------------------------------

	/** Everything DCAT-AP.de makes Pflicht for a Dataset: title, description, publisher. */
	private static String conformant(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Air quality"/>
				  <description lang="en" value="Hourly air quality measurements"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				</dcat:Dataset>""".formatted(logicalIri(id));
	}

	/** Title only — what most of this suite writes, and what enforcement now refuses. */
	private static String titleOnly(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Probe"/>
				</dcat:Dataset>""".formatted(logicalIri(id));
	}

	private static String logicalIri(String id) {
		return BASE + "/datasets/" + id;
	}

	// --- plumbing -----------------------------------------------------------

	private void setValidateOnWrite(boolean enabled) throws IOException {
		Configuration configuration = configAdmin.getConfiguration(PID, "?");
		Dictionary<String, Object> properties = configuration.getProperties();
		Dictionary<String, Object> updated = properties == null ? new Hashtable<>() : properties;
		updated.put("validateOnWrite", enabled);
		configuration.update(updated);
	}

	private HttpResponse<String> awaitEnforcementActive(long timeoutMillis) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		HttpResponse<String> last = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));
		while (last.statusCode() != 422 && System.currentTimeMillis() < deadline) {
			Thread.sleep(250);
			last = put(ADMIN_DATASETS + "/" + PROBE_ID, titleOnly(PROBE_ID));
		}
		return last;
	}

	private HttpResponse<String> put(String url, String body) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)) //
				.header("Content-Type", XMI) //
				.header("Accept", XMI) //
				.PUT(BodyPublishers.ofString(body)) //
				.build(), BodyHandlers.ofString());
	}

	private void delete(String url) throws Exception {
		http.send(HttpRequest.newBuilder(URI.create(url)).DELETE().build(), BodyHandlers.ofString());
	}
}

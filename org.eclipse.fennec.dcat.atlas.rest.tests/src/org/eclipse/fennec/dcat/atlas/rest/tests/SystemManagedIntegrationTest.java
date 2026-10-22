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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.dcat.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.dcat.atlas.rest.tests.helper.RestReady;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * What a system generates stays the system's, over HTTP.
 * <p>
 * {@code SystemManagedResourceTest} calls the services directly and hands them a
 * {@code Caller}; this one sends a header and nothing else, so what is under test is the
 * REST half — that {@code CallerHeader} is bound into the admin resources, reads the name
 * it defaults to, and that the refusal surfaces as {@code 403} rather than an unmapped
 * {@code 500}.
 * <p>
 * There is no gateway in the test runtime, so every other class in this suite writes
 * anonymously and is unaffected. The store is shared with the live runtime, so everything
 * written here is cleaned up — and the cleanup has to present the managing system's header
 * or it would be refused by the rule under test.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class SystemManagedIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String ADMIN_DATASETS = BASE + "/admin/datasets";
	private static final String XMI = "application/xmi";

	/** The default in {@code CallerHeaderConfig}; a deployment may rename it. */
	private static final String CALLER_HEADER = "X-Atlas-Client";
	private static final String MODEL_ATLAS = "model.atlas";
	private static final String DATA_ATLAS = "data.atlas";

	private static final String MANAGED_ID = "system-managed-generated";
	private static final String UNMANAGED_ID = "system-managed-by-hand";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@BeforeEach
	void ready(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		assertTrue(ResourceAware.create(context, "DatasetAdminResource").waitForResource(15, TimeUnit.SECONDS),
				"DatasetAdminResource should be registered within 15 seconds.");
	}

	@AfterEach
	void cleanUp() throws Exception {
		delete(ADMIN_DATASETS + "/" + MANAGED_ID, MODEL_ATLAS);
		delete(ADMIN_DATASETS + "/" + UNMANAGED_ID, null);
	}

	@Test
	void aGeneratedResourceCannotBeDeletedByHand() throws Exception {
		assertTrue(put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), MODEL_ATLAS).statusCode() / 100 == 2);

		HttpResponse<String> refused = delete(ADMIN_DATASETS + "/" + MANAGED_ID, null);

		assertEquals(403, refused.statusCode(), "a hand delete of a generated dataset: " + refused.body());
		assertTrue(refused.body().contains(MODEL_ATLAS),
				"the refusal should name the managing system, but was: " + refused.body());
		assertEquals(200, get(BASE + "/datasets/" + MANAGED_ID).statusCode(), "and it should still be there");
	}

	@Test
	void aGeneratedResourceCannotBeEditedByHand() throws Exception {
		assertTrue(put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), MODEL_ATLAS).statusCode() / 100 == 2);

		HttpResponse<String> refused = put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), null);

		assertEquals(403, refused.statusCode(), "a hand edit of a generated dataset: " + refused.body());
	}

	@Test
	void anotherSystemIsRefusedToo() throws Exception {
		assertTrue(put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), MODEL_ATLAS).statusCode() / 100 == 2);

		assertEquals(403, put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), DATA_ATLAS).statusCode());
		assertEquals(403, delete(ADMIN_DATASETS + "/" + MANAGED_ID, DATA_ATLAS).statusCode());
	}

	@Test
	void theManagingSystemEditsAndDeletesItsOwn() throws Exception {
		assertTrue(put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), MODEL_ATLAS).statusCode() / 100 == 2);

		assertTrue(put(ADMIN_DATASETS + "/" + MANAGED_ID, dataset(MANAGED_ID), MODEL_ATLAS).statusCode() / 100 == 2);
		assertEquals(204, delete(ADMIN_DATASETS + "/" + MANAGED_ID, MODEL_ATLAS).statusCode());
	}

	@Test
	void aResourceNobodyGeneratedIsUnaffected() throws Exception {
		// The shape every other test class in this suite takes: no header, no management.
		assertTrue(put(ADMIN_DATASETS + "/" + UNMANAGED_ID, dataset(UNMANAGED_ID), null).statusCode() / 100 == 2);

		assertTrue(put(ADMIN_DATASETS + "/" + UNMANAGED_ID, dataset(UNMANAGED_ID), null).statusCode() / 100 == 2);
		assertEquals(204, delete(ADMIN_DATASETS + "/" + UNMANAGED_ID, null).statusCode());
	}

	// --- fixtures -----------------------------------------------------------

	private static String dataset(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="Air quality"/>
				  <description lang="en" value="Hourly air quality measurements"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				</dcat:Dataset>""".formatted(BASE + "/datasets/" + id);
	}

	private HttpResponse<String> put(String url, String body, String caller) throws Exception {
		return http.send(as(HttpRequest.newBuilder(URI.create(url)) //
				.header("Content-Type", XMI) //
				.header("Accept", XMI) //
				.PUT(BodyPublishers.ofString(body)), caller).build(), BodyHandlers.ofString());
	}

	private HttpResponse<String> delete(String url, String caller) throws Exception {
		return http.send(as(HttpRequest.newBuilder(URI.create(url)).DELETE(), caller).build(),
				BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String url) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)).header("Accept", XMI).GET().build(),
				BodyHandlers.ofString());
	}

	/** Sends as {@code caller}, or as nobody at all when it is {@code null}. */
	private static HttpRequest.Builder as(HttpRequest.Builder request, String caller) {
		return caller == null ? request : request.header(CALLER_HEADER, caller);
	}
}

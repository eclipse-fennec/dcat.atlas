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
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.rest.tests.helper.RestReady;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * {@code ?cascade=true} on the admin delete endpoints (dcat.atlas#20).
 * <p>
 * The cascade itself was implemented and tested at the service layer long before it was
 * reachable over HTTP; what these tests cover is the REST contract that was missing —
 * the query parameter being read at all, and the three-way response that distinguishes
 * "unlinked things" from "nothing to unlink" from "plain delete".
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class CascadeDeleteIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String PUBLIC_BASE = "http://localhost:8185/rest";
	private static final String XMI = "application/xmi";
	private static final String DATASET_ID = "cascade-ds";
	private static final String CATALOG_ID = "cascade-cat";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	CatalogAdminService catalogs;

	@InjectService
	DatasetAdminService datasets;

	@BeforeEach
	void ready(@InjectBundleContext BundleContext context) throws InterruptedException {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
	}

	@AfterEach
	void cleanup() {
		catalogs.getCatalog(CATALOG_ID).ifPresent(unused -> catalogs.deleteDatasetFromCatalog(CATALOG_ID, DATASET_ID));
		catalogs.deleteCatalog(CATALOG_ID, false);
		datasets.deleteDataset(DATASET_ID, false);
	}

	/**
	 * The whole point of the issue: before this, the parameter was ignored and a client
	 * holding a referenced dataset could not delete it through the API at all.
	 */
	@Test
	void cascadeDeletesAReferencedDatasetAndReportsWhatItUnlinked() throws Exception {
		seedDataset();
		linkDatasetIntoCatalog();

		HttpResponse<String> delete = delete("/admin/datasets/" + DATASET_ID + "?cascade=true");

		assertEquals(200, delete.statusCode(), delete.body());
		assertTrue(delete.headers().firstValue("Content-Type").orElse("").startsWith("text/plain"),
				delete.headers().firstValue("Content-Type").orElse("(none)"));
		// The identity of the catalog that was unlinked, rendered public: a client's cache is
		// keyed by the URL it was served, so a logical http://dcat.atlas/… IRI here would be
		// unusable for the invalidation this report exists to enable.
		assertEquals(PUBLIC_BASE + "/catalogs/" + CATALOG_ID, delete.body().strip(), delete.body());

		assertTrue(datasets.getDataset(DATASET_ID).isEmpty(), "the dataset should be gone");
		assertTrue(catalogs.getCatalog(CATALOG_ID).orElseThrow().getDataset().isEmpty(),
				"the catalog should no longer reference it");
	}

	/**
	 * A cascade with nothing to unlink is a plain delete and says so — an empty body with
	 * {@code 200} would imply a report was being made when nothing else changed.
	 */
	@Test
	void cascadeWithNothingToUnlinkAnswers204() throws Exception {
		seedDataset();

		HttpResponse<String> delete = delete("/admin/datasets/" + DATASET_ID + "?cascade=true");

		assertEquals(204, delete.statusCode(), delete.body());
		assertTrue(delete.body().isEmpty(), "204 carries no body: " + delete.body());
		assertTrue(datasets.getDataset(DATASET_ID).isEmpty());
	}

	/** Unchanged for every existing caller: absent parameter means no cascade. */
	@Test
	void aPlainDeleteStillAnswers204() throws Exception {
		seedDataset();

		HttpResponse<String> delete = delete("/admin/datasets/" + DATASET_ID);

		assertEquals(204, delete.statusCode(), delete.body());
		assertTrue(datasets.getDataset(DATASET_ID).isEmpty());
	}

	/**
	 * {@code cascade=false} is the default, so it must behave exactly as its absence does —
	 * including still refusing a referenced resource with {@code 409}.
	 */
	@Test
	void cascadeFalseStillRefusesAReferencedResource() throws Exception {
		seedDataset();
		linkDatasetIntoCatalog();

		HttpResponse<String> delete = delete("/admin/datasets/" + DATASET_ID + "?cascade=false");

		assertEquals(409, delete.statusCode(), delete.body());
		assertFalse(datasets.getDataset(DATASET_ID).isEmpty(), "the dataset must survive a refused delete");
	}

	/**
	 * {@code If-Match} is evaluated against the target, and a cascade proceeds on a correct
	 * one even though the referrers' ETags were never checked — the documented narrowing of
	 * F-16. A stale one still fails, so the precondition is genuinely being evaluated
	 * rather than skipped on the cascade path.
	 */
	@Test
	void ifMatchIsEvaluatedAgainstTheTargetOnly() throws Exception {
		seedDataset();
		linkDatasetIntoCatalog();

		HttpResponse<String> stale = http.send(
				HttpRequest.newBuilder(URI.create(BASE + "/admin/datasets/" + DATASET_ID + "?cascade=true"))
						.header("If-Match", "\"not-the-current-etag\"").DELETE()
						.timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
		assertEquals(412, stale.statusCode(), stale.body());
		assertFalse(datasets.getDataset(DATASET_ID).isEmpty(), "a failed precondition must not delete");

		String etag = http
				.send(HttpRequest.newBuilder(URI.create(BASE + "/datasets/" + DATASET_ID)).header("Accept", XMI)
						.timeout(Duration.ofSeconds(10)).build(), BodyHandlers.ofString())
				.headers().firstValue("ETag").orElseThrow();

		HttpResponse<String> current = http.send(
				HttpRequest.newBuilder(URI.create(BASE + "/admin/datasets/" + DATASET_ID + "?cascade=true"))
						.header("If-Match", etag).DELETE().timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
		assertEquals(200, current.statusCode(), current.body());
		assertTrue(datasets.getDataset(DATASET_ID).isEmpty());
	}

	// --- helpers -----------------------------------------------------------

	private HttpResponse<String> delete(String path) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(BASE + path)).DELETE()
				.timeout(Duration.ofSeconds(10)).build(), BodyHandlers.ofString());
	}

	private void seedDataset() throws Exception {
		HttpResponse<String> put = put(BASE + "/admin/datasets/" + DATASET_ID, """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="http://dcat.atlas/datasets/%s">
				  <title lang="en" value="Cascade fixture"/>
				  <description lang="en" value="Cascade delete fixture"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				</dcat:Dataset>""".formatted(DATASET_ID));
		assertTrue(put.statusCode() < 300, put.body());
	}

	private void linkDatasetIntoCatalog() throws Exception {
		HttpResponse<String> put = put(BASE + "/admin/catalogs/" + CATALOG_ID, """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="http://dcat.atlas/catalogs/%s">
				  <title lang="en" value="Cascade catalog"/>
				  <description lang="en" value="Cascade delete fixture"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				  <dataset href="http://dcat.atlas/datasets/%s#/"/>
				</dcat:Catalog>""".formatted(CATALOG_ID, DATASET_ID));
		assertTrue(put.statusCode() < 300, put.body());
		assertEquals(1, catalogs.getCatalog(CATALOG_ID).orElseThrow().getDataset().size(),
				"fixture should have linked the dataset");
	}

	private HttpResponse<String> put(String url, String body) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XMI).header("Accept", XMI)
				.timeout(Duration.ofSeconds(10)).PUT(BodyPublishers.ofString(body)).build(), BodyHandlers.ofString());
	}
}

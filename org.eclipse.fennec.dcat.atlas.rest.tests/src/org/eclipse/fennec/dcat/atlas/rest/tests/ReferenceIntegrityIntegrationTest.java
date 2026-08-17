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

import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
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
 * How a write that names another resource is treated (the write-side mirror of FR-1).
 * <p>
 * Three ways a client can point at a Dataset from a Catalog body, and what each must do:
 * an {@code href} to something stored links it; the same member written out <em>inline</em>
 * means the identity its {@code about} names, not a new object; and a reference to an
 * identity of ours that does not exist is refused rather than stored as a link that can
 * never resolve.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ReferenceIntegrityIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String XMI = "application/xmi";
	private static final String DATASET_ID = "refint-ds";
	private static final String CATALOG_ID = "refint-cat";

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
		// Unlink before deleting: FR-1 refuses to delete a still-referenced dataset.
		catalogs.getCatalog(CATALOG_ID).ifPresent(unused -> catalogs.deleteDatasetFromCatalog(CATALOG_ID, DATASET_ID));
		catalogs.deleteCatalog(CATALOG_ID, false);
		datasets.deleteDataset(DATASET_ID, false);
	}

	@Test
	void hrefToAStoredDatasetLinksIt() throws Exception {
		seedDataset();

		HttpResponse<String> put = putCatalog("""
				  <dataset href="http://dcat.atlas/datasets/%s#/"/>""".formatted(DATASET_ID));

		assertTrue(put.statusCode() < 300, put.statusCode() + " " + put.body());
		assertEquals(1, catalogs.getCatalog(CATALOG_ID).orElseThrow().getDataset().size());
		assertEquals("Original title", storedMemberTitle());
	}

	@Test
	void inlineMemberWithAnIdentityOfOursIsStoredAsAReference() throws Exception {
		seedDataset();

		// The member written out in full, carrying the identity of the dataset that already
		// exists. XMI cannot express an inline object under a non-containment reference, so
		// left alone EMF writes a same-document IDREF that resolves to nothing on reload and
		// the membership silently disappears.
		HttpResponse<String> put = putCatalog("""
				  <dataset about="http://dcat.atlas/datasets/%s">
				    <title lang="en" value="Inline title that must not be stored"/>
				  </dataset>""".formatted(DATASET_ID));

		assertTrue(put.statusCode() < 300, put.statusCode() + " " + put.body());
		assertEquals(1, catalogs.getCatalog(CATALOG_ID).orElseThrow().getDataset().size(),
				"the inline member must be linked, not dropped");
		// It is a reference, so the stored dataset keeps its own content: an inline body is
		// not a back-door write to another resource.
		assertEquals("Original title", storedMemberTitle());
		assertEquals("Original title", datasets.getDataset(DATASET_ID).orElseThrow().getTitle().get(0).getValue());
	}

	@Test
	void inlineMemberIsNotStoredTwice() throws Exception {
		seedDataset();
		int before = datasets.listDatasets().size();

		putCatalog("""
				  <dataset about="http://dcat.atlas/datasets/%s">
				    <title lang="en" value="Inline title"/>
				  </dataset>""".formatted(DATASET_ID));

		assertEquals(before, datasets.listDatasets().size(), "linking must not mint a second dataset");
	}

	@Test
	void referenceToAnIdentityOfOursThatDoesNotExistIsRejected() throws Exception {
		// No seedDataset() — the target was never stored.
		HttpResponse<String> put = putCatalog("""
				  <dataset href="http://dcat.atlas/datasets/%s#/"/>""".formatted(DATASET_ID));

		// 409, the same status FR-1 uses when a delete would break referential integrity;
		// this is that rule on the write side. 404 would be wrong — the request target (the
		// catalog) is fine, it is the body that names something missing.
		assertEquals(409, put.statusCode(), put.body());
		assertTrue(catalogs.getCatalog(CATALOG_ID).isEmpty(), "nothing should have been stored");
	}

	@Test
	void foreignReferenceIsAccepted() throws Exception {
		// Not an identity we own, so it is not ours to refuse — vocabularies, publishers and
		// licences all point outward like this.
		HttpResponse<String> put = putCatalog("""
				  <dataset href="https://someone-else.example/datasets/air#/"/>""");

		assertTrue(put.statusCode() < 300, put.statusCode() + " " + put.body());
		assertTrue(catalogs.getCatalog(CATALOG_ID).isPresent());
	}

	@Test
	void deletingAReferencedDatasetIsAConflictNotAServerError() throws Exception {
		seedDataset();
		putCatalog("""
				  <dataset href="http://dcat.atlas/datasets/%s#/"/>""".formatted(DATASET_ID));

		HttpResponse<String> delete = http.send(
				HttpRequest.newBuilder(URI.create(BASE + "/admin/datasets/" + DATASET_ID)).DELETE()
						.timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());

		// FR-1 is documented as 409; without a mapper the ResourceInUseException escapes as
		// a 500 and the client cannot tell a refusal from a crash.
		assertEquals(409, delete.statusCode(), delete.body());
		assertFalse(datasets.getDataset(DATASET_ID).isEmpty(), "the dataset must survive a refused delete");
	}

	// --- helpers -----------------------------------------------------------

	private String storedMemberTitle() {
		return catalogs.getCatalog(CATALOG_ID).orElseThrow().getDataset().get(0).getTitle().get(0).getValue();
	}

	private void seedDataset() throws Exception {
		HttpResponse<String> put = put(BASE + "/admin/datasets/" + DATASET_ID, """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="http://dcat.atlas/datasets/%s">
				  <title lang="en" value="Original title"/>
				</dcat:Dataset>""".formatted(DATASET_ID));
		assertTrue(put.statusCode() < 300, put.body());
	}

	private HttpResponse<String> putCatalog(String memberElement) throws Exception {
		return put(BASE + "/admin/catalogs/" + CATALOG_ID, """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Catalog xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="http://dcat.atlas/catalogs/%s">
				  <title lang="en" value="Reference integrity catalog"/>
				%s
				</dcat:Catalog>""".formatted(CATALOG_ID, memberElement));
	}

	private HttpResponse<String> put(String url, String body) throws Exception {
		return http.send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XMI).header("Accept", XMI)
				.timeout(Duration.ofSeconds(10)).PUT(BodyPublishers.ofString(body)).build(), BodyHandlers.ofString());
	}
}

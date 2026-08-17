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

import org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
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

import dcat.DataService;
import dcat.DcatFactory;
import dcat.Dataset;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * End-to-end HTTP test for the FR-10 Distribution composition endpoints. Unlike
 * the other entities a Distribution is not a root collection: it is created,
 * read and deleted only in the context of its owning Dataset, under
 * {@code /datasets/{datasetId}/distributions} (read) and
 * {@code /admin/datasets/{datasetId}/distributions} (write). So this test drives
 * the nested resources directly rather than extending
 * {@link AbstractEntityResourceIntegrationTest}.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class DistributionResourceIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String RDF_XML = "application/rdf+xml";
	/** The only format the admin endpoints accept: our EMF model's own XMI. */
	private static final String XML = "application/xmi";
	private static final String TURTLE = "text/turtle";

	private static final String DATASET_ID = "dist-e2e-ds";
	/** The id {@link #distributionBody} names, and therefore the one a create lands under. */
	private static final String DISTRIBUTION_ID = "placeholder";
	/**
	 * The id the {@code PUT} tests address in the path. Their bodies must name it too: a
	 * {@code PUT} whose {@code about} points at a different distribution is refused now
	 * rather than silently rewritten, and these used to send {@link #DISTRIBUTION_ID}.
	 */
	private static final String PUT_DISTRIBUTION_ID = "csv";
	/** The DataService that the FR-10 accessService tests link to. */
	private static final String SERVICE_ID = "dist-e2e-svc";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	/** Seeds/inspects the owning dataset that distributions attach to. */
	@InjectService
	DatasetAdminService datasetService;

	/** Seeds/inspects the DataService referenced by {@code dcat:accessService}. */
	@InjectService
	DataServiceAdminService dataServiceService;

	/** Reads back the stored distribution to assert on the accessService references. */
	@InjectService
	DistributionAdminService distributionService;

	@BeforeEach
	void ensureResourcesAndDataset(@InjectBundleContext BundleContext context) throws InterruptedException {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
		// Seeded the way the store mints identities: logical, not the request URL. A public
		// URL here is not an identity the store owns, so DcatIds.idOf refuses to carve an id
		// out of it and upsert files the dataset under a fresh UUID instead — leaving every
		// /datasets/{DATASET_ID}/... path below a 404.
		datasetService.upsertDataset(dataset(DcatIds.logicalIri(DcatIds.DATASETS, DATASET_ID), "Air quality"));
	}

	@AfterEach
	void cleanup() {
		datasetService.deleteDataset(DATASET_ID, false);
		dataServiceService.deleteDataService(SERVICE_ID, false);
	}

	private String distributions() {
		return BASE + "/datasets/" + DATASET_ID + "/distributions";
	}

	private String adminDistributions() {
		return BASE + "/admin/datasets/" + DATASET_ID + "/distributions";
	}

	@Test
	void postCreatesDistributionUnderDatasetAndLinksIt() throws Exception {
		HttpResponse<String> post = send(HttpRequest.newBuilder(URI.create(adminDistributions()))
				.header("Content-Type", XML).POST(BodyPublishers.ofString(distributionBody("CSV download"))),
				XML);
		assertEquals(201, post.statusCode(), post.body());

		// The created Location is a dereferenceable, dataset-scoped read URL.
		String location = post.headers().firstValue("Location").orElseThrow();
		assertTrue(location.contains("/datasets/" + DATASET_ID + "/distributions/"), location);

		HttpResponse<String> get = get(location, TURTLE);
		assertEquals(200, get.statusCode());
		assertTrue(get.body().contains("CSV download"), get.body());

		// The owning dataset now carries the dcat:distribution link.
		assertEquals(1, datasetService.getDataset(DATASET_ID).get().getDistribution().size());
	}

	/**
	 * The conflict rule on a nested identity: the first POST creates under the id the body
	 * names, a repeat is refused. Distributions are the one create whose identity is not a
	 * collection member — it nests inside the dataset (FR-10) — so it resolves through its own
	 * path in {@code CreateIdentity} and needs its own test.
	 */
	@Test
	void postingTheSameDistributionTwiceCreatesOnceAndThenConflicts() throws Exception {
		HttpResponse<String> first = send(HttpRequest.newBuilder(URI.create(adminDistributions()))
				.header("Content-Type", XML).POST(BodyPublishers.ofString(distributionBody("CSV download"))), XML);
		assertEquals(201, first.statusCode(), first.body());
		assertEquals(distributions() + "/" + DISTRIBUTION_ID, first.headers().firstValue("Location").orElseThrow());

		HttpResponse<String> second = send(HttpRequest.newBuilder(URI.create(adminDistributions()))
				.header("Content-Type", XML).POST(BodyPublishers.ofString(distributionBody("CSV again"))), XML);

		assertEquals(409, second.statusCode(), second.body());
		assertTrue(second.body().contains("distributions/" + DISTRIBUTION_ID),
				"the 409 should name what is in the way: " + second.body());
		// The nested read URL, which is the multi-segment case of the conflict's Location.
		assertEquals(distributions() + "/" + DISTRIBUTION_ID, second.headers().firstValue("Location").orElse(null),
				"the conflict should point at the distribution it collided with");
		assertEquals(1, datasetService.getDataset(DATASET_ID).get().getDistribution().size(),
				"the refused create must not have added a second distribution");
	}

	/**
	 * And the foreign-{@code about} rule on that same nested path. A Distribution's identity
	 * is scoped to its Dataset, so "not ours" here includes another dataset's distribution —
	 * the body would otherwise have been filed under this one with its identity rewritten.
	 */
	@Test
	void postingADistributionWithAForeignAboutIsRefused() throws Exception {
		String foreign = "https://www.govdata.de/datasets/other/distributions/csv";
		String body = """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Distribution xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s">
				  <title lang="en" value="Foreign"/>
				</dcat:Distribution>""".formatted(foreign);

		HttpResponse<String> refused = send(HttpRequest.newBuilder(URI.create(adminDistributions()))
				.header("Content-Type", XML).POST(BodyPublishers.ofString(body)), XML);

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains(foreign), "the 400 should quote back the about: " + refused.body());
		assertTrue(datasetService.getDataset(DATASET_ID).get().getDistribution().isEmpty(),
				"the refused create must not have added a distribution");
	}

	/**
	 * The nested form of the {@code PUT} identity rule: the body may not name another
	 * distribution of this dataset, nor one of another dataset's.
	 */
	@Test
	void puttingADistributionWhoseAboutNamesAnotherIsRefused() throws Exception {
		String url = adminDistributions() + "/" + PUT_DISTRIBUTION_ID;

		HttpResponse<String> refused = send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody("some-other-distribution", "Hijack"))), XML);

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains("some-other-distribution")
				&& refused.body().contains("distributions/" + PUT_DISTRIBUTION_ID), refused.body());
		assertTrue(distributionService.getDistributionForDataset(DATASET_ID, PUT_DISTRIBUTION_ID).isEmpty(),
				"the refused PUT must not have stored anything");
	}

	@Test
	void putThenGetThenDelete() throws Exception {
		String url = adminDistributions() + "/csv";
		HttpResponse<String> put = send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		assertTrue(put.statusCode() == 200 || put.statusCode() == 201, "status=" + put.statusCode());

		HttpResponse<String> get = get(distributions() + "/csv", TURTLE);
		assertEquals(200, get.statusCode());
		assertTrue(get.body().contains("CSV download"), get.body());

		HttpResponse<String> delete = send(HttpRequest.newBuilder(URI.create(url)).DELETE(), TURTLE);
		assertEquals(204, delete.statusCode());
		assertEquals(404, get(distributions() + "/csv", TURTLE).statusCode());
		assertTrue(datasetService.getDataset(DATASET_ID).get().getDistribution().isEmpty());
	}

	@Test
	void getUnknownIsNotFound() throws Exception {
		assertEquals(404, get(distributions() + "/does-not-exist", TURTLE).statusCode());
	}

	@Test
	void getReturnsEtagAndHonoursIfNoneMatch() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);

		HttpResponse<String> read = get(distributions() + "/csv", TURTLE);
		assertEquals(200, read.statusCode());
		String etag = read.headers().firstValue("ETag").orElseThrow();

		HttpResponse<String> conditional = send(
				HttpRequest.newBuilder(URI.create(distributions() + "/csv")).GET().header("If-None-Match", etag),
				TURTLE);
		assertEquals(304, conditional.statusCode());
	}

	@Test
	void reAddingSameDistributionLeavesDatasetLinkUnchanged() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		String datasetEtagAfterFirst = datasetService.etag(DATASET_ID).orElseThrow();

		// PUT the identical distribution again: the dcat:distribution link is already
		// present, so the owning dataset is untouched and its ETag does not change.
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);

		assertEquals(datasetEtagAfterFirst, datasetService.etag(DATASET_ID).orElseThrow());
		assertEquals(1, datasetService.getDataset(DATASET_ID).get().getDistribution().size());
	}

	@Test
	void putHonoursIfMatch() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);

		HttpResponse<String> stale = send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.header("If-Match", "\"stale-value\"").PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "v2"))), RDF_XML);
		assertEquals(412, stale.statusCode());
	}

	@Test
	void createUnderUnknownDatasetIsNotFound() throws Exception {
		String url = BASE + "/admin/datasets/no-such-dataset/distributions";
		HttpResponse<String> post = send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.POST(BodyPublishers.ofString(distributionBody("CSV download"))), RDF_XML);
		assertEquals(404, post.statusCode());
	}

	// --- FR-10 accessService link ------------------------------------------

	@Test
	void putLinksAccessServiceAndRdfCarriesTheReference() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		seedDataService();

		HttpResponse<String> link = send(
				HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID)).PUT(BodyPublishers.noBody()),
				RDF_XML);
		assertEquals(200, link.statusCode(), link.body());
		// The mutated resource is the Distribution, so its ETag comes back.
		assertTrue(link.headers().firstValue("ETag").isPresent(), "link response should carry the distribution ETag");

		// dcat:accessService is emitted as a URI reference to the service, not a copy of it.
		HttpResponse<String> get = get(distributions() + "/csv", RDF_XML);
		assertEquals(200, get.statusCode());
		assertTrue(get.body().contains(serviceAbout()), get.body());
		assertTrue(get.body().contains("accessService"), get.body());
	}

	@Test
	void reLinkingSameAccessServiceLeavesDistributionUnchanged() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		seedDataService();

		send(HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID)).PUT(BodyPublishers.noBody()),
				RDF_XML);
		String etagAfterFirst = distributionService.etag(DATASET_ID, "csv").orElseThrow();

		// Idempotent: the link is already recorded, so nothing is written and the ETag holds.
		HttpResponse<String> again = send(
				HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID)).PUT(BodyPublishers.noBody()),
				RDF_XML);
		assertEquals(200, again.statusCode());
		assertEquals(etagAfterFirst, distributionService.etag(DATASET_ID, "csv").orElseThrow());
		assertEquals(1, distributionService.getDistributionForDataset(DATASET_ID, "csv").get().getAccessService().size());
	}

	@Test
	void deleteUnlinksAccessServiceButKeepsTheService() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		seedDataService();
		send(HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID)).PUT(BodyPublishers.noBody()),
				RDF_XML);

		HttpResponse<String> unlink = send(
				HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID)).DELETE(), RDF_XML);
		assertEquals(204, unlink.statusCode());

		assertTrue(distributionService.getDistributionForDataset(DATASET_ID, "csv").get().getAccessService().isEmpty());
		// The DataService is a catalog entity of its own and must survive the unlink.
		assertTrue(dataServiceService.getDataService(SERVICE_ID).isPresent());
	}

	@Test
	void unlinkingAnAccessServiceThatIsNotLinkedIsNoContent() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);

		HttpResponse<String> unlink = send(
				HttpRequest.newBuilder(URI.create(url + "/access-service/never-linked")).DELETE(), RDF_XML);
		assertEquals(204, unlink.statusCode());
	}

	@Test
	void linkingUnknownAccessServiceIsNotFound() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);

		HttpResponse<String> link = send(
				HttpRequest.newBuilder(URI.create(url + "/access-service/no-such-service")).PUT(BodyPublishers.noBody()),
				RDF_XML);
		assertEquals(404, link.statusCode());
	}

	@Test
	void linkingOnUnknownDistributionIsNotFound() throws Exception {
		seedDataService();
		HttpResponse<String> link = send(HttpRequest
				.newBuilder(URI.create(adminDistributions() + "/no-such-dist/access-service/" + SERVICE_ID))
				.PUT(BodyPublishers.noBody()), RDF_XML);
		assertEquals(404, link.statusCode());
	}

	@Test
	void linkHonoursIfMatch() throws Exception {
		String url = adminDistributions() + "/csv";
		send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(distributionBody(PUT_DISTRIBUTION_ID, "CSV download"))), XML);
		seedDataService();

		HttpResponse<String> stale = send(HttpRequest.newBuilder(URI.create(url + "/access-service/" + SERVICE_ID))
				.header("If-Match", "\"stale-value\"").PUT(BodyPublishers.noBody()), RDF_XML);
		assertEquals(412, stale.statusCode());
	}

	// --- helpers -----------------------------------------------------------

	/** The service's public IRI — what a client sees in the RDF, not what it is stored under. */
	private String serviceAbout() {
		return BASE + "/data-services/" + SERVICE_ID;
	}

	/** Catalogues the DataService that {@code accessService} will reference. */
	private void seedDataService() {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		// Logical, for the same reason as the dataset seed above.
		service.setAbout(DcatIds.logicalIri(DcatIds.DATA_SERVICES, SERVICE_ID));
		PlainLiteral title = RdfFactory.eINSTANCE.createPlainLiteral();
		title.setLang("en");
		title.setValue("Air quality WFS");
		service.getTitle().add(title);
		dataServiceService.upsertDataService(service);
	}

	/**
	 * An XMI Distribution; admin endpoints accept XML/XMI only.
	 * <p>
	 * The {@code about} is the public, dataset-scoped IRI of {@link #DISTRIBUTION_ID}, and a
	 * create now honours it rather than minting over it — so this body names the id it lands
	 * under, on {@code POST} as well as on the {@code PUT}s that spell it in the path.
	 */
	private String distributionBody(String title) {
		return distributionBody(DISTRIBUTION_ID, title);
	}

	/** The same, for the {@code PUT}s whose path names an id other than the default. */
	private String distributionBody(String id, String title) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Distribution xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s/%s">
				  <title lang="en" value="%s"/>
				</dcat:Distribution>""".formatted(distributions(), id, title);
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		return dataset;
	}

	private HttpResponse<String> get(String url, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).GET(), accept);
	}

	private HttpResponse<String> send(HttpRequest.Builder request, String accept) throws Exception {
		return http.send(request.header("Accept", accept).timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
	}
}

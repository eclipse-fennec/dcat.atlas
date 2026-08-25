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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
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

import dcat.Dataset;
import dcat.DcatFactory;

/**
 * End-to-end tests for the SPARQL endpoint over the in-memory RDF projection
 * (persistence plan P1-8).
 * <p>
 * Every assertion is scoped to an IRI this test minted, because the test runtime
 * shares one store directory across the whole suite — counting triples globally
 * would make these tests depend on what else has run.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class SparqlEndpointIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";
	private static final String SPARQL = BASE + "/sparql";
	private static final String TITLE = "http://purl.org/dc/terms/title";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	DatasetAdminService datasetService;

	private String id;

	@BeforeEach
	void waitForTheWhiteboard(@InjectBundleContext BundleContext context) throws InterruptedException {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"the Jakarta REST whiteboard did not reach a stable state; registered = "
						+ RestReady.registeredResources(context));
		id = UUID.randomUUID().toString();
	}

	@AfterEach
	void cleanUp() {
		if (id != null) {
			datasetService.deleteDataset(id, false);
		}
	}

	// --- the projection tracks the store -----------------------------------

	@Test
	void aRestWriteIsImmediatelyVisibleToSparql() throws Exception {
		createDatasetOverRest(id, "Air quality " + id);

		// Synchronous on purpose: a client that just wrote and then queries must not
		// have to know about an indexing delay.
		assertTrue(sparqlAsksForTitle("Air quality " + id));
	}

	@Test
	void aRestDeleteRemovesItFromSparql() throws Exception {
		createDatasetOverRest(id, "Water quality " + id);
		assertTrue(sparqlAsksForTitle("Water quality " + id));

		HttpResponse<String> deleted = send(
				HttpRequest.newBuilder(URI.create(BASE + "/admin/datasets/" + id)).DELETE().build());
		assertEquals(204, deleted.statusCode(), deleted.body());

		assertFalse(sparqlAsksForTitle("Water quality " + id));
	}

	@Test
	void aMutationThroughTheOsgiServiceWithoutRestIsProjected() throws Exception {
		// Constraint G2: the graph is maintained at the persistence boundary, so a
		// caller that never touches REST must not be able to leave it stale. A hook in
		// the JAX-RS resource would fail exactly this test.
		//
		// The identity is the *logical* one, which is what the store holds. Going
		// straight to the service is the point of this test, but it also bypasses
		// PublicIriFilter's public->logical fold, so a public IRI here is an about the
		// store does not recognise as ours — it silently mints a fresh id instead, the
		// record lands somewhere cleanUp() never looks, and the suite leaked one dataset
		// per run (seven of them, before this was noticed).
		String title = "Direct OSGi write " + id;
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		RestEntities.mandatoryDataset(dataset, title);

		datasetService.upsertDataset(dataset);

		assertTrue(sparqlAsksForTitle(title));
		// Turns a silent leak into a failure: if the write lands under any other id,
		// cleanUp() cannot remove it and the store grows by one on every run.
		assertTrue(datasetService.getDataset(id).isPresent(),
				"the write must be stored under the id cleanUp() deletes, or it leaks");
	}

	@Test
	void anUpdateReplacesRatherThanAccumulates() throws Exception {
		createDatasetOverRest(id, "First title " + id);
		createDatasetOverRest(id, "Second title " + id);

		assertTrue(sparqlAsksForTitle("Second title " + id));
		assertFalse(sparqlAsksForTitle("First title " + id), "the resource's named graph must be replaced");
	}

	// --- endpoint behaviour ------------------------------------------------

	@Test
	void selectReturnsSparqlResultsJson() throws Exception {
		createDatasetOverRest(id, "Air quality " + id);

		HttpResponse<String> response = get("?query=" + encode(
				"SELECT ?t WHERE { GRAPH <" + BASE + "/datasets/" + id + "> { ?s <" + TITLE + "> ?t } }"),
				"application/sparql-results+json");

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("Air quality " + id), response.body());
	}

	@Test
	void constructReturnsRdfInTheNegotiatedSyntax() throws Exception {
		createDatasetOverRest(id, "Air quality " + id);

		HttpResponse<String> response = get(
				"?query=" + encode("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH <" + BASE + "/datasets/" + id
						+ "> { ?s ?p ?o } }"),
				"text/turtle");

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("Air quality " + id), response.body());
	}

	@Test
	void theQueryMayBePostedAsSparqlQuery() throws Exception {
		createDatasetOverRest(id, "Air quality " + id);

		HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(SPARQL))
				.header("Content-Type", "application/sparql-query")
				.header("Accept", "application/sparql-results+json")
				.POST(BodyPublishers.ofString(
						"SELECT ?t WHERE { GRAPH <" + BASE + "/datasets/" + id + "> { ?s <" + TITLE + "> ?t } }"))
				.build());

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("Air quality " + id), response.body());
	}

	@Test
	void aSparqlUpdateIsRejected() throws Exception {
		// The projection is derived, not a store of record: it must not be writable.
		HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(SPARQL))
				.header("Content-Type", "application/sparql-query")
				.POST(BodyPublishers.ofString("INSERT DATA { <urn:a> <urn:b> <urn:c> }")).build());

		assertEquals(400, response.statusCode(), response.body());
	}

	@Test
	void aMalformedQueryIsRejected() throws Exception {
		HttpResponse<String> response = get("?query=" + encode("SELECT ?s WHERE {"), "*/*");
		assertEquals(400, response.statusCode(), response.body());
	}

	@Test
	void aMissingQueryIsRejected() throws Exception {
		assertEquals(400, get("", "*/*").statusCode());
	}

	// --- readiness and reindex ---------------------------------------------

	@Test
	void readinessReportsTheProjection() throws Exception {
		HttpResponse<String> ready = send(
				HttpRequest.newBuilder(URI.create("http://localhost:8185/health/ready")).GET().build());
		// The check must be part of readiness, or an unbuilt projection would let an
		// instance take traffic and answer queries with too few results.
		assertTrue(ready.body().contains("\"name\":\"sparql\""), ready.body());
	}

	@Test
	void reindexIsAcceptedAndKeepsAnsweringMeanwhile() throws Exception {
		createDatasetOverRest(id, "Air quality " + id);

		HttpResponse<String> response = send(
				HttpRequest.newBuilder(URI.create(BASE + "/admin/sparql/reindex")).POST(BodyPublishers.noBody())
						.build());
		assertEquals(202, response.statusCode(), response.body());

		// The rebuild runs asynchronously and deliberately does not clear the dataset
		// first, so results stay available throughout rather than briefly vanishing.
		assertTrue(sparqlAsksForTitle("Air quality " + id));
	}

	// --- helpers -----------------------------------------------------------

	private void createDatasetOverRest(String datasetId, String title) throws Exception {
		// XMI: RDF is an output format only, so an RDF/XML body earns a 415.
		String body = """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s/datasets/%s">
				  <title lang="en" value="%s"/>
				  <description lang="en" value="SPARQL integration fixture"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				</dcat:Dataset>""".formatted(BASE, datasetId, title);
		HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(BASE + "/admin/datasets/" + datasetId))
				.header("Content-Type", "application/xmi").PUT(BodyPublishers.ofString(body)).build());
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
				"write failed: " + response.statusCode() + " " + response.body());
	}

	private boolean sparqlAsksForTitle(String title) throws Exception {
		String query = "ASK { GRAPH ?g { ?s <" + TITLE + "> \"" + title + "\"@en } }";
		HttpResponse<String> response = get("?query=" + encode(query), "application/sparql-results+json");
		assertEquals(200, response.statusCode(), response.body());
		return response.body().contains("true");
	}

	private HttpResponse<String> get(String queryString, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(SPARQL + queryString)).header("Accept", accept).GET().build());
	}

	private HttpResponse<String> send(HttpRequest request) throws Exception {
		return http.send(request, BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}

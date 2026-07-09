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
import java.util.ArrayList;
import java.util.List;

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
 * Shared end-to-end HTTP test for a DCAT-AP entity's read/admin resource pair:
 * request → Jersey whiteboard → resource → service (file store) → RDF body
 * writer/reader. Runtime coordinates come from {@code configs/config.json} (Felix
 * HTTP on 8185, Jersey context {@code /rest}, per-entity store directory).
 * <p>
 * Concrete subclasses supply the entity specifics (collection path, RDF type
 * name, resource names, and how to seed/inspect the backing store through the
 * injected admin service).
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public abstract class AbstractEntityResourceIntegrationTest {

	protected static final String BASE = "http://localhost:8185/rest";

	protected static final String RDF_XML = "application/rdf+xml";
	protected static final String TURTLE = "text/turtle";
	protected static final String N_TRIPLES = "application/n-triples";
	protected static final String JSON_LD = "application/ld+json";
	protected static final String N3 = "text/n3";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	/** Ids created during a test, removed afterwards to keep the shared store clean. */
	private final List<String> created = new ArrayList<>();

	// --- per-entity hooks --------------------------------------------------

	/** REST collection segment, e.g. {@code "datasets"}. */
	protected abstract String collection();

	/** RDF type local name in the {@code dcat} namespace, e.g. {@code "Dataset"}. */
	protected abstract String typeName();

	protected abstract String readResourceName();

	protected abstract String adminResourceName();

	/** Store an entity with the given about-id and title through the admin service. */
	protected abstract void seed(String id, String title);

	protected abstract boolean storedPresent(String id);

	protected abstract String storedTitle(String id);

	protected abstract void removeFromStore(String id);

	// --- lifecycle ---------------------------------------------------------

	@BeforeEach
	void ensureResourcesAvailable(@InjectBundleContext BundleContext context) throws InterruptedException {
		// Wait for the WHOLE whiteboard to reach steady state, not just this entity's
		// two resources: a late-registering resource elsewhere reloads the shared
		// Jersey application and transiently 404s every endpoint (see RestReady).
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"REST whiteboard should reach a stable state within 20 seconds.");
	}

	@AfterEach
	void cleanup() {
		created.forEach(this::removeFromStore);
		created.clear();
	}

	// --- read (public) tests ----------------------------------------------

	@Test
	void getServesTurtle() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads() + "/e1", TURTLE);
		assertEquals(200, response.statusCode());
		assertEquals(TURTLE, mediaType(response));
		assertTrue(response.body().contains("Hello"), response.body());
		assertTrue(response.body().contains("dcat"), response.body());
	}

	@Test
	void getServesJsonLd() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads() + "/e1", JSON_LD);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("Hello"), response.body());
		assertTrue(response.body().contains("@"), response.body());
	}

	@Test
	void getServesN3() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads() + "/e1", N3);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("Hello"), response.body());
	}

	@Test
	void getServesNTriples() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads() + "/e1", N_TRIPLES);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("Hello"), response.body());
	}

	@Test
	void getServesRdfXml() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads() + "/e1", RDF_XML);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains(typeName()), response.body());
		assertTrue(response.body().contains("Hello"), response.body());
	}

	@Test
	void getUnknownIsNotFound() throws Exception {
		assertEquals(404, get(reads() + "/does-not-exist", TURTLE).statusCode());
	}

	@Test
	void listContainsSeededEntity() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> response = get(reads(), TURTLE);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("Hello"), response.body());
	}

	// --- write (admin) tests ----------------------------------------------

	@Test
	void putStoresEntityFromRdfXml() throws Exception {
		String id = "put1";
		track(id);
		HttpResponse<String> put = send(HttpRequest.newBuilder(URI.create(writes() + "/" + id))
				.header("Content-Type", RDF_XML).PUT(BodyPublishers.ofString(rdfXmlBody(id, "Uploaded"))), RDF_XML);
		assertTrue(put.statusCode() == 200 || put.statusCode() == 201, "status=" + put.statusCode());

		assertTrue(storedPresent(id));
		assertEquals("Uploaded", storedTitle(id));
	}

	@Test
	void deleteRemovesEntity() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> delete = send(HttpRequest.newBuilder(URI.create(writes() + "/e1")).DELETE(), TURTLE);
		assertEquals(204, delete.statusCode());
		assertEquals(404, get(reads() + "/e1", TURTLE).statusCode());
	}

	// --- conditional requests (F-16) --------------------------------------

	@Test
	void getReturnsEtagAndHonoursIfNoneMatch() throws Exception {
		track("e1");
		seed("e1", "Hello");
		HttpResponse<String> first = get(reads() + "/e1", TURTLE);
		assertEquals(200, first.statusCode());
		String etag = first.headers().firstValue("ETag").orElseThrow();

		// If-None-Match with the current ETag -> 304 Not Modified.
		HttpResponse<String> conditional = send(
				HttpRequest.newBuilder(URI.create(reads() + "/e1")).GET().header("If-None-Match", etag), TURTLE);
		assertEquals(304, conditional.statusCode());
	}

	@Test
	void putHonoursIfMatch() throws Exception {
		track("e1");
		seed("e1", "Hello");
		String etag = get(reads() + "/e1", RDF_XML).headers().firstValue("ETag").orElseThrow();

		// A stale/wrong validator is rejected with 412.
		HttpResponse<String> stale = send(HttpRequest.newBuilder(URI.create(writes() + "/e1"))
				.header("Content-Type", RDF_XML).header("If-Match", "\"stale-value\"")
				.PUT(BodyPublishers.ofString(rdfXmlBody("e1", "Updated"))), RDF_XML);
		assertEquals(412, stale.statusCode());

		// The current validator is accepted.
		HttpResponse<String> ok = send(HttpRequest.newBuilder(URI.create(writes() + "/e1"))
				.header("Content-Type", RDF_XML).header("If-Match", etag)
				.PUT(BodyPublishers.ofString(rdfXmlBody("e1", "Updated"))), RDF_XML);
		assertEquals(200, ok.statusCode());
		assertEquals("Updated", storedTitle("e1"));
	}

	@Test
	void deleteHonoursIfMatch() throws Exception {
		track("e1");
		seed("e1", "Hello");

		HttpResponse<String> stale = send(HttpRequest.newBuilder(URI.create(writes() + "/e1"))
				.header("If-Match", "\"stale-value\"").DELETE(), TURTLE);
		assertEquals(412, stale.statusCode());
		assertTrue(storedPresent("e1"));
	}

	// --- helpers -----------------------------------------------------------

	protected String reads() {
		return BASE + "/" + collection();
	}

	protected String writes() {
		return BASE + "/admin/" + collection();
	}

	protected void track(String id) {
		created.add(id);
	}

	/** A minimal {@code <rdf:RDF>} document with one resource of this entity's type and a title. */
	private String rdfXmlBody(String id, String title) {
		return rdfXmlBody(typeName(), reads() + "/" + id, title);
	}

	/** A minimal {@code <rdf:RDF>} document with one typed resource ({@code about}) and a title. */
	protected static String rdfXmlBody(String type, String about, String title) {
		return """
				<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				         xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmlns:dct="http://purl.org/dc/terms/">
				  <dcat:%s rdf:about="%s">
				    <dct:title xml:lang="en">%s</dct:title>
				  </dcat:%s>
				</rdf:RDF>""".formatted(type, about, title, type);
	}

	protected HttpResponse<String> get(String url, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).GET(), accept);
	}

	/** POST an RDF/XML body to {@code url}. */
	protected HttpResponse<String> postRdfXml(String url, String body) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", RDF_XML)
				.POST(BodyPublishers.ofString(body)), RDF_XML);
	}

	/** DELETE {@code url}. */
	protected HttpResponse<String> delete(String url) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).DELETE(), RDF_XML);
	}

	protected HttpResponse<String> send(HttpRequest.Builder request, String accept)
			throws IOException, InterruptedException {
		return http.send(request.header("Accept", accept).timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
	}

	protected static String mediaType(HttpResponse<String> response) {
		return response.headers().firstValue("Content-Type").map(v -> v.split(";")[0].trim()).orElse(null);
	}
}

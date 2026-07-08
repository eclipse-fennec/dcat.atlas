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
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.dcat.atlas.rest.tests.helper.ResourceAware;
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
		assertTrue(ResourceAware.create(context, readResourceName()).waitForResource(15, TimeUnit.SECONDS),
				readResourceName() + " should be registered within 15 seconds.");
		assertTrue(ResourceAware.create(context, adminResourceName()).waitForResource(15, TimeUnit.SECONDS),
				adminResourceName() + " should be registered within 15 seconds.");
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

	/** A minimal {@code <rdf:RDF>} document with one typed resource and a title. */
	private String rdfXmlBody(String id, String title) {
		return """
				<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				         xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmlns:dct="http://purl.org/dc/terms/">
				  <dcat:%s rdf:about="%s/%s">
				    <dct:title xml:lang="en">%s</dct:title>
				  </dcat:%s>
				</rdf:RDF>""".formatted(typeName(), reads(), id, title, typeName());
	}

	private HttpResponse<String> get(String url, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).GET(), accept);
	}

	private HttpResponse<String> send(HttpRequest.Builder request, String accept)
			throws IOException, InterruptedException {
		return http.send(request.header("Accept", accept).timeout(Duration.ofSeconds(10)).build(),
				BodyHandlers.ofString());
	}

	private static String mediaType(HttpResponse<String> response) {
		return response.headers().firstValue("Content-Type").map(v -> v.split(";")[0].trim()).orElse(null);
	}
}

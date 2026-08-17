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
	protected static final String JSON = "application/json";
	/** The only format the admin endpoints accept: our EMF model's own XMI. */
	protected static final String XML = "application/xmi";
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

	/**
	 * Every wired format, because a collection is where the two writer families disagree:
	 * the RDF ones take it as it is but select on the <em>generic</em> type, while the codec
	 * needs a single root object and never accepts a list. Getting either wrong turns the
	 * whole endpoint into a 500, and the single-entity tests cannot catch it — a rendered
	 * EObject keeps its class either way.
	 * <p>
	 * {@code application/json} is in the list because the resources advertise it, and it
	 * only works if {@code org.eclipse.fennec.codec} is in {@code -runbundles}: that bundle
	 * carries the factory registered for the content type, and without it JSON is
	 * unserializable for single entities too. Advertising a format nothing can write is a
	 * 500, so this keeps the two in step.
	 */
	@Test
	void listIsServedInEveryFormatWithPublicIris() throws Exception {
		track("e1");
		seed("e1", "Hello");
		for (String accept : List.of(TURTLE, XML, RDF_XML, N_TRIPLES, JSON_LD, N3, JSON)) {
			HttpResponse<String> response = get(reads(), accept);
			assertEquals(200, response.statusCode(), accept + " -> " + response.body());
			// ...and the identities are the public ones, which is the filter's whole job.
			assertTrue(response.body().contains(reads() + "/e1"),
					accept + " should carry the public IRI: " + response.body());
		}
	}

	// --- write (admin) tests ----------------------------------------------

	@Test
	void putStoresEntityFromXmi() throws Exception {
		String id = "put1";
		track(id);
		HttpResponse<String> put = send(HttpRequest.newBuilder(URI.create(writes() + "/" + id))
				.header("Content-Type", XML).PUT(BodyPublishers.ofString(xmiBody(id, "Uploaded"))), XML);
		assertTrue(put.statusCode() == 200 || put.statusCode() == 201, "status=" + put.statusCode());

		assertTrue(storedPresent(id));
		assertEquals("Uploaded", storedTitle(id));
	}

	/**
	 * A create names its own identity when it has one, and a second create naming the same
	 * identity is refused with {@code 409} instead of silently producing a second resource
	 * (the retry/double-click case). Here for every entity type, because the mint-first
	 * create was copied into all of them and a fix that reached only some would be worse than
	 * none: a client could not tell which collections honour it.
	 * <p>
	 * The refusal carries the same {@code Location} as the create it collided with, so a
	 * client that loses the race still ends up holding the URL — and the id in it — that it
	 * needs to go on and add members.
	 */
	@Test
	void postingAnIdentityTwiceCreatesOnceAndThenConflicts() throws Exception {
		String id = "dup-post";
		track(id);
		// The public form, which is what a client has: folded back to the logical one before
		// the resource sees it.
		HttpResponse<String> first = postXmi(writes(), xmiBody(id, "First"));
		assertEquals(201, first.statusCode(), first.body());
		assertEquals(reads() + "/" + id, first.headers().firstValue("Location").orElse(null),
				"the identity the client named should be the one created");

		HttpResponse<String> second = postXmi(writes(), xmiBody(id, "Second"));

		assertEquals(409, second.statusCode(), second.body());
		assertEquals(reads() + "/" + id, second.headers().firstValue("Location").orElse(null),
				"the conflict should point at the resource it collided with");
		assertEquals("First", storedTitle(id), "the refused create must not have replaced the stored entity");
	}

	/**
	 * A create refused for a reason other than a collision points at nothing: there is no
	 * resource of ours at the identity the client sent, so a {@code Location} would name a
	 * URL that 404s.
	 */
	@Test
	void aRefusedAboutCarriesNoLocation() throws Exception {
		HttpResponse<String> refused = postXmi(writes(),
				xmiBody(typeName(), "https://www.govdata.de/" + collection() + "/no-location-probe", "Foreign"));

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.headers().firstValue("Location").isEmpty(),
				"a 400 names no resource to point at: " + refused.headers().firstValue("Location").orElse(""));
	}

	/**
	 * An {@code about} that names no identity of ours is refused rather than quietly
	 * replaced by a minted one — again for every entity type, since a client picks the door,
	 * and a rule that held in only some collections would be no rule at all. Only the entity
	 * being stored is subject to it; what it contains keeps whatever identity it arrived
	 * with (see {@code CatalogResourceIntegrationTest#containedResourcesKeepTheirForeignAbout}).
	 */
	@Test
	void postingAForeignAboutIsRefused() throws Exception {
		String foreign = "https://www.govdata.de/" + collection() + "/foreign-about-probe";

		HttpResponse<String> refused = postXmi(writes(), xmiBody(typeName(), foreign, "Foreign"));

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains(foreign),
				"the 400 should quote back the about it refused: " + refused.body());
		// The last segment would be a usable id, so the risk the old minting behaviour ran
		// was filing the entity under it — nothing may be stored there.
		assertFalse(storedPresent("foreign-about-probe"), "no entity may be created for a foreign about");
	}

	/**
	 * A {@code PUT} takes its identity from the path, and now says so instead of quietly
	 * dropping a body that claims a different one. Nothing becomes unreachable: the resource
	 * is addressed by the path, so an {@code about} is never needed to reach it — which is
	 * what the next test shows.
	 */
	@Test
	void puttingAForeignAboutIsRefused() throws Exception {
		String id = "put-foreign";
		String foreign = "https://www.govdata.de/" + collection() + "/" + id;

		HttpResponse<String> refused = send(HttpRequest.newBuilder(URI.create(writes() + "/" + id))
				.header("Content-Type", XML).PUT(BodyPublishers.ofString(xmiBody(typeName(), foreign, "Foreign"))),
				XML);

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains(foreign), refused.body());
		assertFalse(storedPresent(id), "the refused PUT must not have stored anything");
	}

	/** The escape hatch the refusal points at, and the way a harvested body is stored. */
	@Test
	void puttingWithoutAnAboutStoresUnderThePathId() throws Exception {
		String id = "put-no-about";
		track(id);
		String body = """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:%s xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0">
				  <title lang="en" value="No about"/>
				</dcat:%s>""".formatted(typeName(), typeName());

		HttpResponse<String> put = send(HttpRequest.newBuilder(URI.create(writes() + "/" + id))
				.header("Content-Type", XML).PUT(BodyPublishers.ofString(body)), XML);

		assertEquals(201, put.statusCode(), put.body());
		assertEquals("No about", storedTitle(id));
	}

	/**
	 * The sharper half of the same rule: an {@code about} that <em>is</em> ours but names a
	 * different resource. This one used to be written to the path's id under the other
	 * resource's name, which is a worse outcome than the foreign case — the client named one
	 * of our resources explicitly and got another one modified.
	 */
	@Test
	void puttingAnAboutForAnotherResourceIsRefused() throws Exception {
		track("put-target");
		track("put-other");
		seed("put-other", "Untouched");

		HttpResponse<String> refused = send(HttpRequest.newBuilder(URI.create(writes() + "/put-target"))
				.header("Content-Type", XML)
				.PUT(BodyPublishers.ofString(xmiBody(typeName(), reads() + "/put-other", "Hijack"))), XML);

		assertEquals(400, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains(collection() + "/put-other")
				&& refused.body().contains(collection() + "/put-target"),
				"the 400 should name both ends of the disagreement: " + refused.body());
		assertFalse(storedPresent("put-target"), "nothing may be stored under the path id");
		assertEquals("Untouched", storedTitle("put-other"), "nor may the resource it named be touched");
	}

	/**
	 * Writes take XMI and nothing else, which the user guide now states outright. RDF is an
	 * output format only — {@code RdfXmlMessageBodyReader} went with the move to the Jena
	 * converter — so the syntax a client just read back is not one it can send.
	 */
	@Test
	void anRdfXmlBodyIsUnsupportedOnAWrite() throws Exception {
		String rdfXml = """
				<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
				         xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmlns:dct="http://purl.org/dc/terms/">
				  <dcat:%s rdf:about="%s/rdf-body">
				    <dct:title xml:lang="en">Sent as RDF/XML</dct:title>
				  </dcat:%s>
				</rdf:RDF>""".formatted(typeName(), reads(), typeName());

		HttpResponse<String> refused = send(HttpRequest.newBuilder(URI.create(writes()))
				.header("Content-Type", RDF_XML).POST(BodyPublishers.ofString(rdfXml)), XML);

		assertEquals(415, refused.statusCode(), refused.body());
		assertFalse(storedPresent("rdf-body"), "nothing may be stored from a body we cannot read");
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
				.header("Content-Type", XML).header("If-Match", "\"stale-value\"")
				.PUT(BodyPublishers.ofString(xmiBody("e1", "Updated"))), XML);
		assertEquals(412, stale.statusCode());

		// The current validator is accepted.
		HttpResponse<String> ok = send(HttpRequest.newBuilder(URI.create(writes() + "/e1"))
				.header("Content-Type", XML).header("If-Match", etag)
				.PUT(BodyPublishers.ofString(xmiBody("e1", "Updated"))), XML);
		assertEquals(200, ok.statusCode());
		assertEquals("Updated", storedTitle("e1"));
	}

	/**
	 * The plain update: a {@code PUT} over a resource that exists, carrying no conditional
	 * header at all. {@code If-Match} is how a client <em>opts in</em> to optimistic locking
	 * (F-16); a client that does not send one is not asking to be locked, and must not be
	 * answered {@code 412}. Only {@code putStoresEntityFromXmi} covered a bare {@code PUT},
	 * and it writes to an id that does not exist yet — so the create path was tested and the
	 * replace path was not.
	 */
	@Test
	void putWithoutAConditionalHeaderReplaces() throws Exception {
		track("e1");
		seed("e1", "Hello");

		HttpResponse<String> put = send(HttpRequest.newBuilder(URI.create(writes() + "/e1"))
				.header("Content-Type", XML).PUT(BodyPublishers.ofString(xmiBody("e1", "Updated"))), XML);

		assertEquals(200, put.statusCode(), put.body());
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

	/** A minimal XMI document for this entity's type. */
	private String xmiBody(String id, String title) {
		return xmiBody(typeName(), reads() + "/" + id, title);
	}

	/**
	 * A minimal XMI document with one typed resource and a title.
	 * <p>
	 * XMI, not RDF/XML: writes go through the EMF model, so a request body is the same
	 * shape as a stored file and a stored file can be sent straight back. RDF is an
	 * output format only — {@code RdfXmlMessageBodyReader} was deleted with the move
	 * to the Jena converter, which is why an RDF/XML body now earns a 415.
	 */
	protected static String xmiBody(String type, String about, String title) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:%s xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				         xmi:version="2.0" about="%s">
				  <title lang="en" value="%s"/>
				</dcat:%s>""".formatted(type, about, title, type);
	}

	protected HttpResponse<String> get(String url, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).GET(), accept);
	}

	/** POST an XMI body to {@code url}; admin endpoints accept XML/XMI only. */
	protected HttpResponse<String> postXmi(String url, String body) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", XML)
				.POST(BodyPublishers.ofString(body)), XML);
	}

	/** DELETE {@code url}. */
	protected HttpResponse<String> delete(String url) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).DELETE(), RDF_XML);
	}

	/**
	 * PUT {@code url} with no body — the link endpoints, which name both ends in the path
	 * and so carry nothing to send.
	 */
	protected HttpResponse<String> putEmpty(String url) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).PUT(BodyPublishers.noBody()), XML);
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

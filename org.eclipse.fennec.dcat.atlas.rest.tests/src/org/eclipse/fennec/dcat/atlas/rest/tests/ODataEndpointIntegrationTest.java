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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
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
 * End-to-end tests for the OData endpoints over the snapshot repository.
 *
 * <h2>Why every query is scoped</h2>
 *
 * The test runtime shares one store directory with the rest of the suite and the store
 * outlives the run, so a global {@code $count} would depend on what else has run. Every
 * query here carries {@code startswith(about, …)} over a per-test prefix, which makes the
 * result set exactly the fixtures this test wrote.
 *
 * <h2>Why the waiting</h2>
 *
 * Unlike the SPARQL projection, which is maintained at the persistence boundary, the OData
 * snapshot is rebuilt asynchronously off an EventAdmin announcement. A write therefore
 * returns before the snapshot knows about it, and the wait is for the <em>effect</em> on
 * the endpoint, which is the thing under test.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ODataEndpointIntegrationTest {

	private static final String REST = "http://localhost:8185/rest";
	private static final String ODATA = "http://localhost:8185/odata";

	private static final String ENVI = "http://publications.europa.eu/resource/authority/data-theme/ENVI";
	private static final String EDUC = "http://publications.europa.eu/resource/authority/data-theme/EDUC";
	private static final String GEOJSON = "http://publications.europa.eu/resource/authority/file-type/GEOJSON";
	private static final String CSV = "http://publications.europa.eu/resource/authority/file-type/CSV";
	private static final String LICENCE = "http://dcat-ap.de/def/licenses/dl-by-de/2.0";

	/** How long the snapshot may take to catch up with an announced write. */
	private static final Duration REFRESH = Duration.ofSeconds(20);

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@InjectService
	DatasetAdminService datasets;

	/** The run-scoped id prefix every fixture shares, and every query filters on. */
	private String run;
	private String matching;
	private String wrongFormat;
	private String wrongTheme;

	@BeforeEach
	void seedTheFixtures(@InjectBundleContext BundleContext context) throws Exception {
		assertTrue(RestReady.awaitStable(context, RestReady.ALL_RESOURCES, 20_000, 750),
				"the Jakarta REST whiteboard did not reach a stable state; registered = "
						+ RestReady.registeredResources(context));

		run = "odata-" + UUID.randomUUID();
		matching = run + "-match";
		wrongFormat = run + "-format";
		wrongTheme = run + "-theme";

		// Three datasets, so each half of the acceptance filter has something to exclude:
		// a decoy that satisfies only the theme and one that satisfies only the format. A
		// filter that silently ignored one conjunct would pass against a single fixture.
		put(matching, ENVI, distribution(matching, "geo", GEOJSON), distribution(matching, "csv", CSV));
		put(wrongFormat, ENVI, distribution(wrongFormat, "csv", CSV));
		put(wrongTheme, EDUC, distribution(wrongTheme, "geo", GEOJSON));

		awaitSnapshot(() -> count(scoped("")) == 3, "all three fixtures have to reach the snapshot");
	}

	@AfterEach
	void cleanUp() {
		for (String id : List.of(matching, wrongFormat, wrongTheme)) {
			datasets.deleteDataset(id, false, Caller.ANONYMOUS);
		}
	}

	// --- the service's shape -----------------------------------------------

	@Test
	void theServiceDocumentListsTheFourSetsAndNoOther() throws Exception {
		HttpResponse<String> response = get(ODATA + "/");

		assertEquals(200, response.statusCode(), response.body());
		assertEquals("4.01", response.headers().firstValue("OData-Version").orElse(null));
		for (String set : List.of("Catalog", "Dataset", "DatasetSeries", "DataService")) {
			assertTrue(response.body().contains("\"" + set + "\""), set + " missing from: " + response.body());
		}
		// A Distribution resolves through its dataset only, so a root set would have
		// nothing to list; the allowlist is what keeps it out.
		assertFalse(response.body().contains("\"Distribution\""), response.body());
	}

	@Test
	void metadataDescribesTheNarrowedModel() throws Exception {
		HttpResponse<String> response = get(ODATA + "/$metadata", "application/xml");

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains("edmx:Edmx"), response.body());
		assertTrue(response.body().contains("Namespace=\"dcat\""), response.body());
		// One <edm:Key> for the whole document: `about` is declared on rdf.IdentifiedResource
		// and every entity type inherits it.
		assertTrue(response.body().contains("<edm:Key>"), response.body());
	}

	@Test
	void theContainerLivesInTheDcatSchema() throws Exception {
		// Not cosmetic: the container's schema namespace and name appear in $metadata and
		// in the @odata.context of every response, so moving it later breaks clients. It
		// is dcat only because dcat is first in odata.model.packages - unconfigured, the
		// packages sort by nsURI and the container would be published from `terms`.
		String metadata = get(ODATA + "/$metadata", "application/xml").body();

		String dcat = schemaOf(metadata, "dcat");
		assertTrue(dcat.contains("<edm:EntityContainer"),
				"the merged container belongs to the dcat schema, not: " + metadata);
	}

	// --- the acceptance query ----------------------------------------------

	@Test
	void theAcceptanceFilterSelectsByThemeAndDistributionFormat() throws Exception {
		// The portal's acceptance criterion as one request: a lambda over a primitive
		// collection (theme is a multi-valued AnyURI) and one over a containment
		// navigation (distribution), conjoined.
		HttpResponse<String> response = get(scopedAnd(
				"theme/any(t: t eq '" + ENVI + "') and distribution/any(d: d/format eq '" + GEOJSON + "')",
				"&$count=true"));

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains(iri(matching)), response.body());
		assertFalse(response.body().contains(iri(wrongFormat)),
				"its only distribution is CSV: " + response.body());
		assertFalse(response.body().contains(iri(wrongTheme)),
				"its theme is EDUC: " + response.body());
		assertTrue(response.body().contains("\"@odata.count\":1"), response.body());
	}

	@Test
	void eachHalfOfTheFilterExcludesOnItsOwn() throws Exception {
		// Guards the test above: if either conjunct were being dropped the combined
		// filter would still pass, because the matching fixture satisfies both.
		String byTheme = get(scopedAnd("theme/any(t: t eq '" + ENVI + "')", "")).body();
		assertTrue(byTheme.contains(iri(matching)) && byTheme.contains(iri(wrongFormat)), byTheme);
		assertFalse(byTheme.contains(iri(wrongTheme)), byTheme);

		String byFormat = get(scopedAnd("distribution/any(d: d/format eq '" + GEOJSON + "')", "")).body();
		assertTrue(byFormat.contains(iri(matching)) && byFormat.contains(iri(wrongTheme)), byFormat);
		assertFalse(byFormat.contains(iri(wrongFormat)), byFormat);
	}

	// --- the query options the plan names -----------------------------------

	@Test
	void selectNarrowsTheProperties() throws Exception {
		HttpResponse<String> response = get(scoped("&$select=about"));

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.body().contains(iri(matching)), response.body());
		assertFalse(response.body().contains("\"description\""),
				"$select must drop everything it did not name: " + response.body());
	}

	@Test
	void aDistributionIsReachableThroughItsDataset() throws Exception {
		// Measured, not assumed: this serializer inlines containment, so the dataset's
		// distributions arrive without $expand. The decision that matters is that they
		// arrive *through the dataset* and nowhere else.
		HttpResponse<String> inline = get(scoped(""));
		assertEquals(200, inline.statusCode(), inline.body());
		assertTrue(inline.body().contains(GEOJSON) && inline.body().contains(CSV), inline.body());

		HttpResponse<String> expanded = get(scoped("&$expand=distribution"));
		assertEquals(200, expanded.statusCode(), expanded.body());
		assertTrue(expanded.body().contains(GEOJSON), expanded.body());
	}

	@Test
	void distributionsAreNotServedAsASetOfTheirOwn() throws Exception {
		// The other half of the decision: reachable through its dataset, never at the root.
		assertEquals(404, get(ODATA + "/Distribution").statusCode());
	}

	@Test
	void orderbyTopAndSkipPageTheResult() throws Exception {
		String ascending = get(scoped("&$orderby=" + encode("about asc"))).body();
		String descending = get(scoped("&$orderby=" + encode("about desc"))).body();
		assertNotEquals(ascending.indexOf(iri(matching)) < ascending.indexOf(iri(wrongTheme)),
				descending.indexOf(iri(matching)) < descending.indexOf(iri(wrongTheme)),
				"desc has to reverse what asc produced");

		// $select=about strips the inlined containment, so the entities in the page can be
		// counted by their key.
		HttpResponse<String> firstPage = get(scoped(
				"&$orderby=" + encode("about asc") + "&$top=1&$count=true&$select=about"));
		assertEquals(200, firstPage.statusCode(), firstPage.body());
		// $count is the size of the filtered set, not of the page - the distinction a
		// client pages on.
		assertTrue(firstPage.body().contains("\"@odata.count\":3"), firstPage.body());
		assertEquals(1, occurrences(firstPage.body(), "\"about\""), firstPage.body());

		String second = get(scoped(
				"&$orderby=" + encode("about asc") + "&$top=1&$skip=1&$select=about")).body();
		assertNotEquals(aboutIn(firstPage.body()), aboutIn(second), "$skip has to advance the window");
	}

	// --- what the endpoint refuses ------------------------------------------

	@Test
	void aWriteIsRefused() throws Exception {
		// OData is a read surface; the REST API is the write boundary that validates.
		//
		// Nothing in the deployed set provides a WriteService, so the 405 is an absence
		// and not a configuration. What this guards is that no bundle added later
		// quietly supplies one: a bound WriteService would accept the POST below into
		// its own store, bypassing SHACL, the identity rules and the git repository.
		HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(ODATA + "/Dataset"))
				.header("Content-Type", "application/json")
				.POST(BodyPublishers.ofString("{\"about\":\"https://example.org/injected\"}")).build());

		assertEquals(405, response.statusCode(), response.body());
	}

	@Test
	void anUnlistedSetIsNotFound() throws Exception {
		// The allowlist decides routing as well as $metadata: a type of a published
		// package that is not a configured set is simply not part of the service.
		assertEquals(404, get(ODATA + "/Agent").statusCode());
		assertEquals(404, get(ODATA + "/NoSuchSet").statusCode());
	}

	@Test
	void injectionStyleFiltersDieAtTheDoorWithoutLeaking() throws Exception {
		for (String attack : List.of(
				"about eq 'a' or 1=1 --",
				"about eq 'a'; DROP TABLE datasets",
				"nosuchproperty eq 1",
				"frobnicate(about) eq 1")) {
			HttpResponse<String> response = get(ODATA + "/Dataset?$filter=" + encode(attack));

			assertEquals(400, response.statusCode(), attack + " -> " + response.body());
			assertFalse(response.body().contains("Exception"),
					"an error body must not carry internals: " + response.body());
		}
	}

	// --- the snapshot tracks the store ---------------------------------------

	@Test
	void aWriteAndADeleteBothReachTheSnapshot() throws Exception {
		String late = run + "-late";
		try {
			put(late, ENVI, distribution(late, "csv", CSV));
			awaitSnapshot(() -> count(scoped("")) == 4, "a write has to reach the snapshot");
		} finally {
			datasets.deleteDataset(late, false, Caller.ANONYMOUS);
		}
		awaitSnapshot(() -> count(scoped("")) == 3, "and so has a delete");
	}

	@Test
	void readinessReportsTheSnapshot() throws Exception {
		HttpResponse<String> ready = get("http://localhost:8185/health/ready");

		// An endpoint answering 200 from a half-built snapshot is worse than one that is
		// not answering yet, so the check gates readiness.
		assertTrue(ready.body().contains("\"name\":\"odata\""), ready.body());
	}

	// --- fixtures -------------------------------------------------------------

	/** A Dataset with the given theme and distributions, written over the REST write boundary. */
	private void put(String id, String theme, String... distributions) throws Exception {
		String body = """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Dataset xmlns:xmi="http://www.omg.org/XMI" xmlns:dcat="http://www.w3.org/ns/dcat#"
				              xmi:version="2.0" about="%s">
				  <title lang="en" value="OData fixture %s"/>
				  <description lang="en" value="OData integration fixture"/>
				  <publisher about="https://example.de/organisation/uba">
				    <name lang="en" value="Umweltbundesamt"/>
				  </publisher>
				  <theme>%s</theme>
				%s
				</dcat:Dataset>""".formatted(iri(id), id, theme, String.join("\n", distributions));

		HttpResponse<String> response = send(HttpRequest.newBuilder(URI.create(REST + "/admin/datasets/" + id))
				.header("Content-Type", "application/xmi").PUT(BodyPublishers.ofString(body)).build());
		assertTrue(response.statusCode() == 200 || response.statusCode() == 201,
				"fixture write failed: " + response.statusCode() + " " + response.body());
	}

	private static String distribution(String datasetId, String name, String format) {
		return """
				  <distribution about="%s/datasets/%s/distributions/%s">
				    <title lang="en" value="%s"/>
				    <accessURL>https://example.de/%s-%s.data</accessURL>
				    <format>%s</format>
				    <license about="%s"/>
				  </distribution>""".formatted(REST, datasetId, name, name, datasetId, name, format, LICENCE);
	}

	private static String iri(String id) {
		return REST + "/datasets/" + id;
	}

	// --- helpers ---------------------------------------------------------------

	/**
	 * A {@code Dataset} query restricted to this test's own fixtures, so an assertion never
	 * depends on what the shared store already held.
	 */
	private String scoped(String suffix) {
		return scopedAnd(null, suffix);
	}

	/**
	 * {@link #scoped} with a further condition conjoined into the SAME {@code $filter}.
	 * <p>
	 * It has to be one option: a second {@code $filter} in the query string is not an
	 * error here, it is silently ignored — the first one wins — so a test written that way
	 * passes its scoping filter alone and asserts nothing about the condition it meant to
	 * test.
	 */
	private String scopedAnd(String condition, String suffix) {
		String scope = "startswith(about, '" + iri(run) + "')";
		String filter = condition == null ? scope : scope + " and (" + condition + ")";
		return ODATA + "/Dataset?$filter=" + encode(filter) + suffix;
	}

	/**
	 * How many entities a query returns, or {@code -1} when it did not answer 200.
	 * <p>
	 * Read off {@code @odata.count} rather than counted in the payload: this serializer
	 * inlines containment, so a dataset's {@code publisher} and {@code distribution} each
	 * carry an {@code about} of their own and counting those would overcount severalfold.
	 */
	private int count(String url) {
		try {
			HttpResponse<String> response = get(url + "&$count=true");
			if (response.statusCode() != 200) {
				return -1;
			}
			String marker = "\"@odata.count\":";
			int at = response.body().indexOf(marker);
			if (at < 0) {
				return -1;
			}
			int start = at + marker.length();
			int end = start;
			while (end < response.body().length() && Character.isDigit(response.body().charAt(end))) {
				end++;
			}
			return Integer.parseInt(response.body().substring(start, end));
		} catch (RuntimeException e) {
			return -1;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Polls until the snapshot has caught up. Polling rather than listening, for the same
	 * reason as {@code RuntimeServiceIntegrationTest}: what is being waited for is the
	 * effect on the endpoint, and a latch on the event would fire while the repository was
	 * still rebuilding.
	 */
	private static void awaitSnapshot(BooleanSupplier caughtUp, String what) {
		long deadline = System.nanoTime() + REFRESH.toNanos();
		while (System.nanoTime() < deadline) {
			if (caughtUp.getAsBoolean()) {
				return;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
				throw new AssertionError("interrupted while waiting for " + what, interrupted);
			}
		}
		assertTrue(caughtUp.getAsBoolean(), () -> what + ", within " + REFRESH);
	}

	/** The {@code <Schema>} element of the given namespace, as raw CSDL XML. */
	private static String schemaOf(String metadata, String namespace) {
		for (String fragment : metadata.split("<edm:Schema")) {
			if (fragment.contains("Namespace=\"" + namespace + "\"")) {
				return fragment;
			}
		}
		throw new AssertionError("no schema for '" + namespace + "' in: " + metadata);
	}

	private static String aboutIn(String body) {
		int key = body.indexOf("\"about\"");
		assertTrue(key >= 0, "no entity in: " + body);
		int start = body.indexOf('"', body.indexOf(':', key)) + 1;
		return body.substring(start, body.indexOf('"', start));
	}

	private static int occurrences(String body, String token) {
		return body.split(token, -1).length - 1;
	}

	private HttpResponse<String> get(String url) throws Exception {
		return get(url, "application/json");
	}

	private HttpResponse<String> get(String url, String accept) throws Exception {
		return send(HttpRequest.newBuilder(URI.create(url)).header("Accept", accept).GET().build());
	}

	private HttpResponse<String> send(HttpRequest request) throws Exception {
		return http.send(request, BodyHandlers.ofString());
	}

	private static String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}

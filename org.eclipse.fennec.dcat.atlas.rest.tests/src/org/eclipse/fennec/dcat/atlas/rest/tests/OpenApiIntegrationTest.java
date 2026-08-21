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
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The OpenAPI descriptor (dcat.atlas#21), served at {@code /openapi.json} and
 * {@code /openapi.yaml}.
 * <p>
 * The descriptor is generated from the JAX-RS annotations rather than written, so what
 * needs guarding is not its wording but the three ways that generation can silently
 * produce something useless: an empty document, media types that misdescribe the
 * XMI-only write side, and mutation of the cached model across requests.
 * <p>
 * Assertions are deliberately loose about the exact route count — endpoints get added —
 * and strict about the properties that would make the descriptor wrong rather than
 * merely out of date.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class OpenApiIntegrationTest {

	private static final String BASE = "http://localhost:8185/rest";

	private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

	@Test
	void theDescriptorIsServedAsJsonAndDescribesTheWholeApi() throws Exception {
		HttpResponse<String> response = get("/openapi.json");

		assertEquals(200, response.statusCode(), response.body());
		assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"),
				response.headers().firstValue("Content-Type").orElse("(none)"));

		String body = response.body();
		assertTrue(body.contains("\"openapi\""), "not an OpenAPI document: " + head(body));
		assertTrue(body.contains("\"title\" : \"DCAT.Atlas\""), "missing the info block: " + head(body));

		// The failure this guards: if app.getClasses() came back empty — which is exactly
		// what happens if the whiteboard registers resources as instances rather than
		// classes — the document would still be a valid 200 with an info block and no
		// paths at all. Silent and useless.
		int operations = countOperations(body);
		assertTrue(operations > 40, "expected the whole API to be described, found " + operations + " operations");

		// A few routes from across the resources, so a resource dropping out of the scan
		// is noticed rather than absorbed by the count.
		for (String path : Set.of("/catalogs/{id}", "/admin/catalogs", "/admin/datasets/{id}",
				"/datasets/{datasetId}/distributions", "/admin/validate/catalogs", "/sparql")) {
			assertTrue(body.contains("\"" + path + "\""), "missing path " + path);
		}
	}

	@Test
	void theDescriptorIsAlsoServedAsYaml() throws Exception {
		// Exercises snakeyaml-engine through jackson-dataformat-yaml, which is a separate
		// resolution path from the JSON one and was the last bundle needed to make the
		// runtime resolve at all.
		HttpResponse<String> response = get("/openapi.yaml");

		assertEquals(200, response.statusCode(), response.body());
		assertEquals("application/yaml", response.headers().firstValue("Content-Type").orElse(null));
		assertTrue(response.body().startsWith("openapi:"), head(response.body()));
		assertTrue(response.body().contains("title: DCAT.Atlas"), head(response.body()));
	}

	/**
	 * Writes accept XMI alone while reads negotiate eight formats, and the descriptor has
	 * to say so per operation. This is what would break if the media types were injected
	 * across every operation the way {@code model.atlas} does it for its dynamic set: the
	 * write endpoints would advertise Turtle they reject with a 415.
	 */
	@Test
	void writesAdvertiseXmiOnlyWhileReadsAdvertiseEveryFormat() throws Exception {
		String body = get("/openapi.json").body();

		String create = operation(body, "/admin/catalogs", "post");
		assertTrue(create.contains("application/xmi"), "the create should consume XMI: " + head(create));
		for (String rdf : Set.of("text/turtle", "application/ld+json", "application/n-triples", "text/n3")) {
			assertFalse(requestBodyOf(create).contains(rdf),
					"a write must not advertise " + rdf + ": " + head(create));
		}

		String read = operation(body, "/catalogs/{id}", "get");
		for (String rdf : Set.of("text/turtle", "application/ld+json", "application/n-triples", "text/n3",
				"application/rdf+xml", "application/xmi")) {
			assertTrue(read.contains(rdf), "the read should offer " + rdf + ": " + head(read));
		}
	}

	/**
	 * Swagger caches the {@code OpenAPI} model per context id, so the resource decorates
	 * an object that survives between requests. Appending to it instead of assigning grew
	 * {@code servers} by one entry per call — measured at 7 after seven requests before
	 * this was fixed.
	 */
	@Test
	void repeatedRequestsDoNotAccumulateServers() throws Exception {
		int first = countServers(get("/openapi.json").body());
		get("/openapi.json");
		get("/openapi.json");
		int fourth = countServers(get("/openapi.json").body());

		assertEquals(1, first, "expected exactly one servers entry");
		assertEquals(first, fourth, "the servers list grew across requests: the descriptor is being mutated");
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create(BASE + path)).GET()
				.timeout(Duration.ofSeconds(20)).build();
		return http.send(request, BodyHandlers.ofString());
	}

	/** Counts HTTP methods under `paths`, which is the number of described operations. */
	private static int countOperations(String json) {
		int count = 0;
		for (String verb : Set.of("get", "post", "put", "delete")) {
			Matcher m = Pattern.compile("\"" + verb + "\"\\s*:\\s*\\{").matcher(json);
			while (m.find()) {
				count++;
			}
		}
		return count;
	}

	private static int countServers(String json) {
		Matcher m = Pattern.compile("\"servers\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
		if (!m.find()) {
			return 0;
		}
		return m.group(1).split("\"url\"", -1).length - 1;
	}

	/**
	 * The slice of the document describing one operation: from the path key to the next
	 * top-level path key. Crude on purpose — parsing JSON would mean a dependency in the
	 * test bundle for the sake of three assertions.
	 */
	private static String operation(String json, String path, String verb) {
		int start = json.indexOf("\"" + path + "\"");
		assertTrue(start >= 0, "path not described: " + path);
		int next = json.indexOf("\n    \"/", start + 1);
		String slice = next < 0 ? json.substring(start) : json.substring(start, next);
		int v = slice.indexOf("\"" + verb + "\"");
		assertTrue(v >= 0, verb + " not described for " + path);
		return slice.substring(v);
	}

	/** The requestBody section of an operation slice, or empty when it has no body. */
	private static String requestBodyOf(String operationSlice) {
		int i = operationSlice.indexOf("\"requestBody\"");
		if (i < 0) {
			return "";
		}
		int end = operationSlice.indexOf("\"responses\"", i);
		return end < 0 ? operationSlice.substring(i) : operationSlice.substring(i, end);
	}

	private static String head(String s) {
		return s.length() <= 400 ? s : s.substring(0, 400) + "…";
	}
}

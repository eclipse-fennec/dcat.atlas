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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The {@code Location} header of a create comes from the configured {@code publicBaseUrl},
 * not from the request.
 *
 * <h2>Why this needs a raw socket</h2>
 *
 * The distinguishing case is a request whose {@code Host} is not the host the container is
 * reached on — which is what a reverse proxy in front of the portal produces. Before the
 * fix, {@code Location} was built from {@code UriInfo.getBaseUriBuilder()} and so followed
 * that header, while the {@code about} in the very same response body was rendered from
 * {@code publicBaseUrl}: one response naming one resource with two different URLs. Two
 * further divergences did not even need a proxy — TLS terminated upstream makes the request
 * arrive as plain {@code http}, and a published path prefix the container never sees is
 * simply absent.
 * <p>
 * {@code Host} is on {@link java.net.http.HttpClient}'s restricted list and cannot be set
 * without a JVM-wide system property, so the request is written to a socket directly. That
 * also keeps the assertion honest: nothing between the test and Jetty can normalise the
 * header away.
 *
 * @see org.eclipse.fennec.dcat.atlas.rest.helper.PublicUri
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class PublicLocationIntegrationTest {

	private static final String HOST = "localhost";
	private static final int PORT = 8185;
	/** What {@code configs/config.json} configures as {@code publicBaseUrl}. */
	private static final String PUBLIC_BASE = "http://localhost:8185/rest";
	/** Stands in for a reverse proxy that forwards its own {@code Host}. */
	private static final String PROXY_HOST = "opendata.example.de";

	@Test
	void aCreateBehindAProxyLocatesTheResourceUnderThePublicBase() throws Exception {
		String id = "public-location-probe";
		String body = xmi(id);

		String response = post("/rest/admin/catalogs", PROXY_HOST, body);

		assertTrue(status(response) == 201 || status(response) == 409,
				"expected the create to be accepted or to collide with a previous run: " + response);
		String location = header(response, "location");
		assertNotNull(location, "a create must say where the resource is: " + response);

		// The point of the test: the proxy's Host must not leak into the header.
		assertEquals(PUBLIC_BASE + "/catalogs/" + id, location,
				"Location must come from publicBaseUrl, not from the request's Host");
		assertTrue(!location.contains(PROXY_HOST), "Location followed the request Host: " + location);

		delete("/rest/admin/catalogs/" + id);
	}

	/**
	 * The header and the body have to agree. They are produced by different code — the
	 * resource builds the header, the response filter rebases the entity — so this is the
	 * assertion that keeps them from drifting apart again.
	 */
	@Test
	void theLocationHeaderMatchesTheAboutInTheSameResponse() throws Exception {
		String id = "public-location-agreement";
		delete("/rest/admin/catalogs/" + id);

		String response = post("/rest/admin/catalogs", PROXY_HOST, xmi(id));

		assertEquals(201, status(response), response);
		String location = header(response, "location");
		String about = about(response);
		assertNotNull(about, "the created entity should be echoed with its about: " + response);
		assertEquals(about, location, "the Location header and the entity's about must be the same URL");

		delete("/rest/admin/catalogs/" + id);
	}

	private static String xmi(String id) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<dcat:Catalog xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" \
				xmlns:dcat="http://www.w3.org/ns/dcat#" about="%s/catalogs/%s" \
				homepage="https://www.govdata.de/">
				  <title value="Location probe" lang="de"/>
				  <description value="Location probe" lang="de"/>
				  <publisher about="https://www.umweltbundesamt.de/">
				    <name value="Umweltbundesamt" lang="de"/>
				  </publisher>
				  <license about="http://dcat-ap.de/def/licenses/dl-by-de/2.0"/>
				  <themeTaxonomy>http://publications.europa.eu/resource/authority/data-theme</themeTaxonomy>
				</dcat:Catalog>
				""".formatted(PUBLIC_BASE, id);
	}

	/** Writes one request with an arbitrary {@code Host} and returns the whole response. */
	private static String post(String path, String host, String body) throws Exception {
		byte[] payload = body.getBytes(StandardCharsets.UTF_8);
		String request = "POST " + path + " HTTP/1.1\r\n" //
				+ "Host: " + host + "\r\n" //
				+ "Content-Type: application/xmi\r\n" //
				+ "Accept: application/xmi\r\n" //
				+ "Content-Length: " + payload.length + "\r\n" //
				+ "Connection: close\r\n\r\n";
		return exchange(request.getBytes(StandardCharsets.UTF_8), payload);
	}

	/** Best-effort cleanup, so a rerun is not a 409 against its own leftovers. */
	private static void delete(String path) throws Exception {
		String request = "DELETE " + path + " HTTP/1.1\r\n" //
				+ "Host: " + HOST + ":" + PORT + "\r\n" //
				+ "Connection: close\r\n\r\n";
		exchange(request.getBytes(StandardCharsets.UTF_8), new byte[0]);
	}

	private static String exchange(byte[] head, byte[] body) throws Exception {
		try (Socket socket = new Socket(HOST, PORT)) {
			socket.setSoTimeout(10_000);
			OutputStream out = socket.getOutputStream();
			out.write(head);
			if (body.length > 0) {
				out.write(body);
			}
			out.flush();
			try (InputStream in = socket.getInputStream()) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] chunk = new byte[4096];
				int read;
				while ((read = in.read(chunk)) != -1) {
					buffer.write(chunk, 0, read);
				}
				return buffer.toString(StandardCharsets.UTF_8);
			}
		}
	}

	private static int status(String response) {
		String[] parts = response.split(" ", 3);
		return parts.length < 2 ? -1 : Integer.parseInt(parts[1].trim());
	}

	private static String header(String response, String name) {
		for (String line : response.split("\r\n")) {
			if (line.isEmpty()) {
				break;
			}
			int colon = line.indexOf(':');
			if (colon > 0 && line.substring(0, colon).trim().toLowerCase(Locale.ROOT).equals(name)) {
				return line.substring(colon + 1).trim();
			}
		}
		return null;
	}

	/** The {@code about} of the response's root element. */
	private static String about(String response) {
		int root = response.indexOf("<dcat:Catalog");
		if (root < 0) {
			return null;
		}
		int attribute = response.indexOf("about=\"", root);
		if (attribute < 0) {
			return null;
		}
		int start = attribute + "about=\"".length();
		return response.substring(start, response.indexOf('"', start));
	}
}

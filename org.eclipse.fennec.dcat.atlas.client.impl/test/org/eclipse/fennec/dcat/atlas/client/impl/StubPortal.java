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
package org.eclipse.fennec.dcat.atlas.client.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * A portal that answers whatever the test tells it to, and records what it was asked.
 *
 * <h2>Why a real HTTP server and not a mocked {@code Response}</h2>
 *
 * The things most likely to be wrong in this client are on the wire: which media type a
 * write asks for, whether a membership link sends a body, whether the ETag goes out as
 * {@code If-Match}, whether the XMI the portal will actually receive parses. A stubbed
 * {@code Response} object cannot catch any of those, because it skips the JAX-RS client
 * that produces them. So the suite runs the real Jersey client against a real socket.
 * <p>
 * Replies are a queue rather than a map from path: several tests care about a
 * <em>sequence</em> (a 412 then a success), and a queue makes the expected order explicit
 * in the test. An exhausted queue answers {@code 500}, so a missing {@code enqueue} shows
 * up as a failure rather than as an accidental pass.
 */
final class StubPortal implements AutoCloseable {

	/** What the client sent. */
	record Received(String method, String path, String query, Map<String, String> headers, byte[] body) {

		String header(String name) {
			return headers.get(name.toLowerCase());
		}

		String bodyAsString() {
			return new String(body, StandardCharsets.UTF_8);
		}
	}

	/** What the portal should answer. */
	record Reply(int status, String contentType, byte[] body, Map<String, String> headers) {

		static Reply of(int status) {
			return new Reply(status, null, new byte[0], Map.of());
		}

		static Reply of(int status, String contentType, String body) {
			return new Reply(status, contentType, body.getBytes(StandardCharsets.UTF_8), Map.of());
		}

		static Reply of(int status, String contentType, byte[] body) {
			return new Reply(status, contentType, body, Map.of());
		}

		Reply withHeader(String name, String value) {
			Map<String, String> merged = new HashMap<>(headers);
			merged.put(name, value);
			return new Reply(status, contentType, body, merged);
		}
	}

	private final HttpServer server;
	private final List<Received> received = new CopyOnWriteArrayList<>();
	private final Deque<Reply> replies = new ArrayDeque<>();

	StubPortal() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", this::handle);
		server.start();
	}

	/** Queue one reply. Replies are consumed in the order they were queued. */
	StubPortal enqueue(Reply reply) {
		synchronized (replies) {
			replies.addLast(reply);
		}
		return this;
	}

	/** Every request the client made, in order. */
	List<Received> received() {
		return List.copyOf(received);
	}

	Received lastRequest() {
		List<Received> all = received();
		if (all.isEmpty()) {
			throw new IllegalStateException("the client made no request");
		}
		return all.get(all.size() - 1);
	}

	/**
	 * The REST base, with a trailing slash and a {@code /rest} segment — so the suite also
	 * covers that {@code ready()} looks for {@code /health/ready} beside it rather than
	 * under it.
	 */
	URI baseUri() {
		return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/rest/");
	}

	@Override
	public void close() {
		server.stop(0);
	}

	private void handle(HttpExchange exchange) throws IOException {
		byte[] body = exchange.getRequestBody().readAllBytes();
		Map<String, String> headers = new HashMap<>();
		exchange.getRequestHeaders()
				.forEach((name, values) -> headers.put(name.toLowerCase(), String.join(",", values)));
		received.add(new Received(exchange.getRequestMethod(), exchange.getRequestURI().getPath(),
				exchange.getRequestURI().getQuery(), headers, body));

		Reply reply;
		synchronized (replies) {
			reply = replies.pollFirst();
		}
		if (reply == null) {
			respond(exchange, new Reply(500, "text/plain",
					"the stub had no reply queued for this request".getBytes(StandardCharsets.UTF_8), Map.of()));
			return;
		}
		respond(exchange, reply);
	}

	private void respond(HttpExchange exchange, Reply reply) throws IOException {
		if (reply.contentType() != null) {
			exchange.getResponseHeaders().set("Content-Type", reply.contentType());
		}
		reply.headers().forEach((name, value) -> exchange.getResponseHeaders().set(name, value));
		boolean hasBody = reply.body() != null && reply.body().length > 0;
		// -1 means "no body"; 204 must not carry one at all.
		exchange.sendResponseHeaders(reply.status(), hasBody ? reply.body().length : -1);
		if (hasBody) {
			try (OutputStream out = exchange.getResponseBody()) {
				out.write(reply.body());
			}
		}
		exchange.close();
	}
}

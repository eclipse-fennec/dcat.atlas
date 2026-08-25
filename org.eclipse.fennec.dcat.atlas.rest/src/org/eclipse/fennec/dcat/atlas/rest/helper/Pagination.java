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
package org.eclipse.fennec.dcat.atlas.rest.helper;

import java.net.URI;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.rest.filter.DcatConditionalFilter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.UriBuilder;

/**
 * Turns a {@link Page} into a paginated collection response: the entries as the body,
 * and everything a client needs to walk the collection in headers.
 *
 * <h2>Why the metadata is in headers and not in the body</h2>
 *
 * A paginated collection normally answers with an envelope carrying the entries plus
 * {@code total} and {@code next}. Not here, for two reasons that both come from this
 * API serving eight representations of the same collection:
 * <ul>
 * <li><b>RDF must stay envelope-free.</b> A wrapper would appear in a harvester's graph
 * as a subject of its own, i.e. this portal's paging would become part of the DCAT it
 * publishes. {@code hydra:PartialCollectionView} is the RDF-native way to say this in
 * band, and adopting it is a decision about the published vocabulary rather than about
 * pagination.</li>
 * <li><b>The codec envelope has nowhere to put it.</b> XMI/JSON/XML collections already
 * travel in the fennec {@code utilities.Response} wrapper (see {@code PublicIriFilter}),
 * whose fields are {@code resultSize}, {@code responseCode}, {@code responseMessage} and
 * {@code timestamp} — no total and no cursor. Adding them would mean changing a model
 * shared with the rest of fennec, for one endpoint's benefit.</li>
 * </ul>
 * {@code Link} and {@code X-Total-Count} are the same in every representation, and a
 * client reads one thing regardless of the format it asked for.
 *
 * <h2>The ETag covers the page, not the collection</h2>
 *
 * The validator combines the store's revision with the page's own coordinates. The
 * revision alone would be wrong in a way that is easy to miss: two different pages of an
 * unchanged collection would share an ETag, so a client that walked to page 2 with the
 * page 1 validator in {@code If-None-Match} would be told 304 and would keep page 1
 * forever.
 */
public final class Pagination {

	/** Query parameter naming the id the previous page ended at. */
	public static final String PARAM_AFTER = "after";

	/** Query parameter naming how many entries to return. */
	public static final String PARAM_LIMIT = "limit";

	/** How many entries the whole collection holds, page aside. */
	public static final String HEADER_TOTAL = "X-Total-Count";

	private Pagination() {
	}

	/**
	 * The response for {@code page}.
	 * <p>
	 * An empty page keeps the {@code 204 No Content} the collections have always answered
	 * with when they hold nothing, rather than an empty document — for a collection that
	 * is genuinely empty, and equally for a cursor that has walked past the end.
	 *
	 * @param page           the page that was read
	 * @param entity         the body, which the caller builds so that the element type
	 *                       survives erasure — see {@code PublicIriFilter}
	 * @param request        the page request this answers, needed to build the links
	 * @param publicIris     the configured public base; links, like every other URL this
	 *                       API emits, are built from it and never from the request
	 * @param collection     the collection's path segment, e.g. {@code catalogs}
	 * @param revision       the store's current version, for the validator
	 * @param requestContext the request, for conditional-GET handling
	 */
	public static Response respond(Page<?> page, Object entity, PageRequest request, PublicIris publicIris,
			String collection, Optional<String> revision, ContainerRequestContext requestContext) {
		ResponseBuilder response = page.items().isEmpty() ? Response.noContent() : Response.ok(entity);
		response.header(HEADER_TOTAL, page.total());

		URI collectionUri = PublicUri.of(publicIris, collection);
		if (collectionUri != null) {
			response.links(link(collectionUri, null, request.limit(), "first"));
			if (page.hasMore()) {
				response.links(link(collectionUri, page.nextAfter(), request.limit(), "next"));
			}
		}

		// Revalidation rather than expiry: the ETag makes a conditional GET cheap, and a
		// collection has no lifetime over which a stale copy would be acceptable.
		response.header("Cache-Control", "no-cache");
		DcatConditionalFilter.attach(requestContext, revision.map(value -> validator(value, request)));
		return response.build();
	}

	/** A {@code rel} link to one page of {@code collectionUri}. */
	private static Link link(URI collectionUri, String after, int limit, String rel) {
		UriBuilder builder = UriBuilder.fromUri(collectionUri).queryParam(PARAM_LIMIT, limit);
		if (after != null) {
			builder.queryParam(PARAM_AFTER, after);
		}
		return Link.fromUri(builder.build()).rel(rel).build();
	}

	/**
	 * The validator for one page: the store revision plus the coordinates that decide
	 * which slice of it this is. Opaque to clients, and it has to stay that way — the
	 * separator is not part of any contract.
	 */
	private static String validator(String revision, PageRequest request) {
		return revision + ":" + request.limit() + ":" + (request.after() == null ? "" : request.after());
	}
}

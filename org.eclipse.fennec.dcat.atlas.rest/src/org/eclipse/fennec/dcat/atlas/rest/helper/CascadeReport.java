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

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Renders the outcome of a delete: what a cascade changed besides the resource itself.
 *
 * <h2>Why a cascade does not answer 204</h2>
 *
 * A cascade unlinks an arbitrary number of other resources, and every one of their ETags
 * moves. {@code 204} would tell the client nothing, so a client holding any of those
 * resources would go on using a stale ETag until something answered {@code 412} at it.
 * Reporting the identities makes the side effects of an operation the client explicitly
 * asked for visible to it, in the same round trip.
 * <p>
 * <b>Identities, not ETags.</b> Worth stating plainly because dcat.atlas#20's own wording
 * — "every one of their ETags moves" — reads as though the new validators were the payload.
 * They are not, deliberately: an ETag identifies a <em>representation</em>, so a client given the
 * new one for content it does not hold would record it, send it as {@code If-None-Match},
 * collect a {@code 304} and go on serving the old body believing it current — turning
 * detectable staleness into silent staleness. A URL says "this changed, drop it", which is
 * what invalidation needs; the new ETag arrives with the content on the next {@code GET},
 * where it is usable. Nor do the listed resources get deleted — only the target does; they
 * are rewritten.
 * <p>
 * Three cases, and the distinction between the last two is deliberate:
 * <ul>
 * <li>a cascade that unlinked something → {@code 200} and the identities;</li>
 * <li>a cascade that had nothing to unlink → {@code 204}: nothing else changed, and the
 * response stays honest about that rather than returning an empty list as though a report
 * were being made;</li>
 * <li>a plain delete → {@code 204}, unchanged for every existing caller.</li>
 * </ul>
 *
 * <h2>Why {@code text/plain}</h2>
 *
 * A list of IRIs is not a DCAT entity, so the collection pattern — a
 * {@code GenericEntity<List<…>>} through the RDF body writers — does not apply, and the
 * delete endpoints carry no {@code @Produces} to negotiate over anyway. One IRI per line
 * matches how the {@code 409} on the refusing branch already reports the same list. A
 * machine-readable form would want a deliberate model type rather than an ad-hoc JSON
 * shape, so it is not invented here.
 *
 * <h2>Public, not logical</h2>
 *
 * The store computes these as logical identities ({@code http://dcat.atlas/…}). They are
 * rebased before they are written, because the whole point of reporting them is that the
 * client can re-fetch or invalidate those resources — and its cache is keyed by the public
 * URLs it was served, which a logical IRI would not match. Note this differs from the
 * {@code 409} body on the refusing branch, which still names logical IRIs.
 */
public final class CascadeReport {

	private CascadeReport() {
	}

	/**
	 * The response for a delete that has already happened.
	 *
	 * @param unlinked   logical IRIs of the resources a cascade rewrote, as the service
	 *                   reported them; empty for a plain delete or a cascade with nothing
	 *                   to unlink
	 * @param publicIris the mapping used to render them; when {@code null} the identities
	 *                   cannot be made safe to publish, so the report is dropped in favour
	 *                   of {@code 204} rather than leaking internal IRIs
	 * @return {@code 200} with one IRI per line, or {@code 204}
	 */
	public static Response respond(List<String> unlinked, PublicIris publicIris) {
		if (unlinked == null || unlinked.isEmpty() || publicIris == null) {
			return Response.noContent().build();
		}
		String body = unlinked.stream().map(publicIris::toPublic).reduce(new StringBuilder(),
				(sb, iri) -> sb.append(iri).append('\n'), StringBuilder::append).toString();
		return Response.ok(body, MediaType.TEXT_PLAIN_TYPE).build();
	}
}

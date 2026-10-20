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
package org.eclipse.fennec.dcat.atlas.rest.filter;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import dcat.DcatResource;
import dcat.Distribution;
import rdf.DateOrDateTimeLiteral;

/**
 * Centralises the read side of HTTP conditional requests / caching (F-16).
 * <p>
 * Read resources do not build ETag headers or evaluate {@code If-None-Match}
 * themselves. Instead a {@code GET} attaches the resource's current strong
 * validator via {@link #attach(ContainerRequestContext, Optional)}; this filter
 * reads it back and:
 * <ul>
 *   <li>stamps the {@code ETag} header (only when the resource did not already set
 *       one) and {@code Vary: Accept} — the ETag is state-based and therefore shared
 *       across representations, so caches must key on {@code Accept};</li>
 *   <li>on a safe ({@code GET}/{@code HEAD}) {@code 200} whose {@code If-None-Match}
 *       matches, rewrites the response to {@code 304 Not Modified} with no body,
 *       before the message-body writer runs.</li>
 * </ul>
 * The write side ({@code If-Match} / create-guard) stays in the admin resources via
 * {@link ConditionalRequests}: it must run <em>before</em> the mutation and the
 * membership endpoints lock on a resource that is not the path resource, so it
 * cannot be centralised here.
 * <p>
 * It also stamps {@code Last-Modified} from the entity itself — see
 * {@link #stampLastModified(ContainerResponseContext)}.
 */
@Component
@JakartarsExtension
@JakartarsName("DcatConditionalFilter")
public class DcatConditionalFilter implements ContainerResponseFilter {

	/** Request-property key under which a read resource stashes its current ETag validator. */
	static final String PROP_ETAG = "org.eclipse.fennec.dcat.atlas.rest.etag";

	/**
	 * Opt the current response into ETag stamping and conditional-GET handling. No-op
	 * when {@code requestContext} is {@code null} or {@code etag} is empty (e.g. the
	 * resource is not stored / has no validator).
	 */
	public static void attach(ContainerRequestContext requestContext, Optional<String> etag) {
		if (requestContext != null && etag != null) {
			etag.ifPresent(value -> requestContext.setProperty(PROP_ETAG, value));
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		// Before the ETag block, because it reads the entity and the 304 branch below
		// discards it - and a 304 is allowed, and expected, to carry its validators.
		stampLastModified(responseContext);

		if (!(requestContext.getProperty(PROP_ETAG) instanceof String etagValue)) {
			return;
		}
		if (responseContext.getEntityTag() == null) {
			responseContext.getHeaders().putSingle(HttpHeaders.ETAG, new EntityTag(etagValue));
		}
		addVaryAccept(responseContext);

		if (isSafe(requestContext) && responseContext.getStatus() == Response.Status.OK.getStatusCode()
				&& ifNoneMatchMatches(requestContext.getHeaderString(HttpHeaders.IF_NONE_MATCH), etagValue)) {
			// Validators are already on the response; drop the body and switch to 304.
			responseContext.setStatus(Response.Status.NOT_MODIFIED.getStatusCode());
			responseContext.setEntity(null);
		}
	}

	/**
	 * Stamps {@code Last-Modified} from the entity's own {@code dcterms:modified}.
	 *
	 * <h2>Why from the entity, and why only there</h2>
	 *
	 * There is no cheaper honest source. The store's timestamps are per <em>commit</em> and
	 * a commit covers the whole repository, so the tip's time would move for every resource
	 * whenever any one of them was written - the same reason the ETag is the blob id rather
	 * than the commit id ({@code DcatHelper#etag}). A per-resource commit time would mean a
	 * path-filtered log, which {@code GitService} does not offer.
	 *
	 * <h2>Why it is not used as a validator</h2>
	 *
	 * {@code dcterms:modified} is content: whoever wrote the entity chose it, and an edit
	 * that forgets to move it leaves the date behind while the representation changes. So
	 * the header is reported and {@code If-Modified-Since} is deliberately <em>not</em>
	 * honoured - answering {@code 304} on a date that can lie would turn a stale cache into
	 * a silent one, where {@code If-None-Match} against the blob id cannot. A resource with
	 * no {@code dcterms:modified} gets no header rather than a guess.
	 */
	private static void stampLastModified(ContainerResponseContext responseContext) {
		if (responseContext.getHeaders().containsKey(HttpHeaders.LAST_MODIFIED)) {
			return;
		}
		Date modified = modifiedOf(responseContext.getEntity());
		if (modified != null) {
			responseContext.getHeaders().putSingle(HttpHeaders.LAST_MODIFIED, modified);
		}
	}

	/**
	 * The entity's {@code dcterms:modified} as an instant, or {@code null} when it has none
	 * - which includes every collection response, whose entity is a list.
	 * <p>
	 * Two branches because {@code Distribution} is an {@code IdentifiedResource} rather than
	 * a {@code DcatResource}: it carries its own {@code modified} and shares no supertype
	 * that declares one.
	 * <p>
	 * The default time zone is UTC, not the server's: a date-only value ({@code 2026-10-19},
	 * which the datatype allows) has no zone of its own, and reading it as local midnight
	 * would make the same stored resource report different instants in two deployments.
	 */
	private static Date modifiedOf(Object entity) {
		DateOrDateTimeLiteral modified = entity instanceof DcatResource resource ? resource.getModified()
				: entity instanceof Distribution distribution ? distribution.getModified() : null;
		if (modified == null || modified.getValue() == null) {
			return null;
		}
		return modified.getValue().toGregorianCalendar(TimeZone.getTimeZone("UTC"), null, null).getTime();
	}

	private static boolean isSafe(ContainerRequestContext requestContext) {
		String method = requestContext.getMethod();
		return HttpMethod.GET.equals(method) || HttpMethod.HEAD.equals(method);
	}

	private static void addVaryAccept(ContainerResponseContext responseContext) {
		List<Object> vary = responseContext.getHeaders().get(HttpHeaders.VARY);
		if (vary != null) {
			for (Object value : vary) {
				if (value != null && value.toString().toLowerCase().contains("accept")) {
					return;
				}
			}
		}
		responseContext.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	}

	/**
	 * Matches an {@code If-None-Match} header value (which may be {@code *}, a single
	 * tag, or a comma-separated list of strong/weak tags) against the current ETag.
	 */
	private static boolean ifNoneMatchMatches(String headerValue, String etagValue) {
		if (headerValue == null) {
			return false;
		}
		String header = headerValue.trim();
		if ("*".equals(header)) {
			return true;
		}
		for (String token : header.split(",")) {
			String tag = token.trim();
			if (tag.startsWith("W/")) {
				tag = tag.substring(2).trim();
			}
			tag = tag.replace("\"", "");
			if (tag.equals(etagValue)) {
				return true;
			}
		}
		return false;
	}
}

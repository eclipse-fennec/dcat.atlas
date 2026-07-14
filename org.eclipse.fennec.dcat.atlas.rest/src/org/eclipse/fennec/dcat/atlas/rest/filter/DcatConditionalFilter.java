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

import java.util.List;
import java.util.Optional;

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

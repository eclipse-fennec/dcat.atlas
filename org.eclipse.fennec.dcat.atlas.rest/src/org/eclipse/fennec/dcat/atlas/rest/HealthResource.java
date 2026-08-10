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
package org.eclipse.fennec.dcat.atlas.rest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.DcatHealthContributor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Health and readiness endpoints for operational monitoring (F-25).
 * <p>
 * The two are deliberately different signals, because an orchestrator reacts to them
 * differently:
 * <ul>
 * <li>{@code GET /health} — <em>liveness</em>. The process is up and serving HTTP. It
 * checks no dependencies on purpose: a failing liveness probe means "restart me", and a
 * missing store or missing shapes are not fixed by a restart.</li>
 * <li>{@code GET /ready} — <em>readiness</em>. Every registered
 * {@link DcatHealthContributor} is consulted; 200 when all report ready, 503 otherwise.
 * A failing readiness probe means "stop routing traffic to me", which is the correct
 * response to a store that is not mounted or shapes that failed to load.</li>
 * </ul>
 * Contributors are bound dynamically, so a subsystem that appears later (the in-memory
 * SPARQL graph, for instance) shows up here by registering the service — this resource
 * needs no change.
 * <p>
 * The response body is hand-built JSON rather than a model: it must stay serialisable
 * with no dependency on the EMF/codec message-body writers, so that a readiness probe
 * still answers when those are exactly what is broken.
 */
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("HealthResource")
@Component(name = "HealthResource", service = HealthResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/")
public class HealthResource {

	/**
	 * Every registered readiness contributor. Greedy and dynamic so contributors that
	 * come and go with their configuration are picked up without recycling this resource
	 * and reloading the whole JAX-RS whiteboard.
	 */
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile List<DcatHealthContributor> contributors = new ArrayList<>();

	@GET
	@Path("health")
	@Produces(MediaType.APPLICATION_JSON)
	public Response health() {
		// Liveness: reaching this method is the answer. No dependency is consulted.
		return Response.ok("{\"status\":\"UP\"}").build();
	}

	@GET
	@Path("ready")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ready() {
		// Snapshot the volatile field once: contributors may be rebound concurrently, and
		// the reported detail must match the status that was aggregated.
		List<DcatHealthContributor> current = new ArrayList<>(contributors);
		current.sort(Comparator.comparing(DcatHealthContributor::name));

		boolean allReady = true;
		StringBuilder checks = new StringBuilder();
		for (DcatHealthContributor contributor : current) {
			boolean ready = safeReady(contributor);
			allReady &= ready;
			if (checks.length() > 0) {
				checks.append(',');
			}
			checks.append("{\"name\":\"").append(escape(contributor.name())) //
					.append("\",\"ready\":").append(ready) //
					.append(",\"detail\":\"").append(escape(safeDetail(contributor))).append("\"}");
		}

		String body = "{\"status\":\"" + (allReady ? "READY" : "NOT_READY") + "\",\"checks\":[" + checks + "]}";
		return Response.status(allReady ? Status.OK : Status.SERVICE_UNAVAILABLE).entity(body).build();
	}

	/** A contributor that throws is not ready — it must never take the probe down with it. */
	private static boolean safeReady(DcatHealthContributor contributor) {
		try {
			return contributor.ready();
		} catch (RuntimeException e) {
			return false;
		}
	}

	private static String safeDetail(DcatHealthContributor contributor) {
		try {
			String detail = contributor.detail();
			return detail == null ? "" : detail;
		} catch (RuntimeException e) {
			return "contributor threw " + e.getClass().getSimpleName();
		}
	}

	/** Minimal JSON string escaping — details carry filesystem paths, which may contain backslashes or quotes. */
	private static String escape(String value) {
		StringBuilder out = new StringBuilder(value.length());
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"' -> out.append("\\\"");
			case '\\' -> out.append("\\\\");
			case '\n' -> out.append("\\n");
			case '\r' -> out.append("\\r");
			case '\t' -> out.append("\\t");
			default -> {
				if (c < 0x20) {
					out.append(String.format("\\u%04x", (int) c));
				} else {
					out.append(c);
				}
			}
			}
		}
		return out.toString();
	}

}

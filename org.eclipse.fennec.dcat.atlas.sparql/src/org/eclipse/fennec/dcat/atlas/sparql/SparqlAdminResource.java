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
package org.eclipse.fennec.dcat.atlas.sparql;

import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Operator control over the RDF projection (persistence plan P1-6).
 * <p>
 * Kept on the {@code /admin} path, and in a resource of its own, so the upstream
 * PEP can protect it the same way as the other write paths (F-6/F-12) — rebuilding
 * is expensive and must not be reachable anonymously.
 * <p>
 * The projection repairs itself on restart and on its reconciliation interval, so
 * this exists to make recovery <em>immediate</em> rather than possible: an operator
 * who knows the graph is wrong should not have to restart the runtime or wait out
 * an interval.
 */
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("SparqlAdminResource")
@Component(name = "SparqlAdminResource", service = SparqlAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/sparql")
public class SparqlAdminResource {

	@Reference
	DcatGraphService graphService;

	@Reference
	SparqlEngine engine;

	/**
	 * Rebuilds the projection from the store.
	 * <p>
	 * Answers {@code 202 Accepted}: the rebuild runs asynchronously because it is
	 * O(store size) and a synchronous call would tie up a request thread for as long
	 * as startup takes. Queries keep being answered from the previous projection
	 * meanwhile — the {@code sparql} check on {@code /health/ready} reports WARN for
	 * the duration rather than going CRITICAL, since a possibly-stale answer is not a
	 * reason to take the instance out of rotation.
	 */
	@POST
	@Path("/reindex")
	@Produces(MediaType.TEXT_PLAIN)
	public Response reindex() {
		if (!engine.isEnabled()) {
			return Response.status(Status.NOT_FOUND).entity("SPARQL is not enabled on this deployment\n").build();
		}
		graphService.rebuild();
		return Response.accepted().entity("Reindex started; watch the 'sparql' check on /health/ready\n").build();
	}
}

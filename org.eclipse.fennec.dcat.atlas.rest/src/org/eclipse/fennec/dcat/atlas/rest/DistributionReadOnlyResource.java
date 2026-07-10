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

import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DistributionReadOnlyService;
import org.eclipse.fennec.dcat.atlas.rest.filter.DcatConditionalFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Distribution;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * 
 * @author ilenia
 * @since Jul 8, 2026
 */
@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("DistributionReadOnlyResource")
@Component(name = "DistributionReadOnlyResource", service = DistributionReadOnlyResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/datasets/{datasetId}/distributions")
public class DistributionReadOnlyResource {
	
	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";
	static final String TURTLE = "text/turtle";
	static final String N_TRIPLES = "application/n-triples";
	static final String JSON_LD = "application/ld+json";
	static final String N3 = "text/n3";

	@Reference
	DistributionReadOnlyService distributionReadOnlyService;

	@GET
	@Produces({ JSON, XML, RDF_XML, TURTLE, N_TRIPLES, JSON_LD, N3 })
	public Response listDistributions(@PathParam("datasetId") String datasetId) {
		List<Distribution> distributions = distributionReadOnlyService.listDistributionsForDataset(datasetId);
		if (distributions.isEmpty()) {
			return Response.noContent().build();
		}
		// GenericEntity preserves List<Distribution> so the RDF body writers see the element type.
		return Response.ok(new GenericEntity<List<Distribution>>(distributions) {
		}).build();
	}

	@GET
	@Path("/{id}")
	@Produces({ JSON, XML, RDF_XML, TURTLE, N_TRIPLES, JSON_LD, N3 })
	public Response getDistribution(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			@Context ContainerRequestContext requestContext) {
		Optional<Distribution> distribution = distributionReadOnlyService.getDistributionForDataset(datasetId, id);
		if (distribution.isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Attach the ETag; DcatConditionalFilter stamps it, adds Vary, and does the
		// If-None-Match -> 304 handling (F-16).
		DcatConditionalFilter.attach(requestContext, distributionReadOnlyService.etag(id));
		return Response.ok(distribution.get()).build();
	}

}

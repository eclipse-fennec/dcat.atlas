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
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.store.StoreRevision;
import org.eclipse.fennec.dcat.atlas.rest.filter.PublicIriFilter;
import org.eclipse.fennec.dcat.atlas.rest.helper.Pagination;
import org.eclipse.fennec.dcat.atlas.rest.filter.DcatConditionalFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Catalog;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Public, read-only REST adapter for the {@code Catalog} collection (visitor /
 * machine-consumer read side, F-1/F-11/F-18/F-19).
 * <p>
 * Only {@code GET} is exposed, content-negotiated across the DCAT-AP RDF formats
 * (RDF/XML, Turtle, N-Triples, JSON-LD) plus JSON/XML. It is kept on a separate
 * path from the admin resource so the upstream PEP (APISix) can grant anonymous
 * access here while protecting the write paths (F-6/F-12).
 */
@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("CatalogReadOnlyResource")
@Component(name = "CatalogReadOnlyResource", service = CatalogReadOnlyResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/catalogs")
public class CatalogReadOnlyResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	/** Our EMF model's own XMI: the write format, and the read format that round-trips. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";
	static final String TURTLE = "text/turtle";
	static final String N_TRIPLES = "application/n-triples";
	static final String JSON_LD = "application/ld+json";
	static final String N3 = "text/n3";

	/**
	 * Held only to gate registration — see {@link PublicIriFilter} for why every
	 * collection resource has to require it.
	 */
	@Reference
	PublicIris identityRendering;

	@Reference
	CatalogReadOnlyService catalogReadOnlyService;

	/**
	 * The store's version, which is half of a collection response's validator — the
	 * other half is which page it is. Read here rather than through the read service
	 * because it is one token for the whole store, not a per-collection property.
	 */
	@Reference
	StoreRevision storeRevision;

	@GET
	@Produces({ XMI, JSON, XML, RDF_XML, TURTLE, N_TRIPLES, JSON_LD, N3 })
	public Response listCatalogs(@QueryParam(Pagination.PARAM_AFTER) String after,
			@QueryParam(Pagination.PARAM_LIMIT) Integer limit, @Context ContainerRequestContext requestContext) {
		// An out-of-range limit is clamped rather than refused; one that is not a number at
		// all never reaches here — see QueryParamExceptionMapper for why that is a 400.
		PageRequest request = PageRequest.of(after, limit);
		Page<Catalog> page = catalogReadOnlyService.listCatalogs(request);
		// GenericEntity preserves List<Catalog> so the RDF body writers see the element type.
		Object entity = new GenericEntity<List<Catalog>>(page.items()) {
		};
		return Pagination.respond(page, entity, request, identityRendering, DcatIds.CATALOGS,
				storeRevision.current(), requestContext);
	}

	@GET
	@Path("/{id}")
	@Produces({ XMI, JSON, XML, RDF_XML, TURTLE, N_TRIPLES, JSON_LD, N3 })
	public Response getCatalog(@PathParam("id") String id, @Context ContainerRequestContext requestContext) {
		Optional<Catalog> catalog = catalogReadOnlyService.getCatalog(id);
		if (catalog.isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Attach the ETag; DcatConditionalFilter stamps it, adds Vary, and does the
		// If-None-Match -> 304 handling (F-16).
		DcatConditionalFilter.attach(requestContext, catalogReadOnlyService.etag(id));
		return Response.ok(catalog.get()).build();
	}
}

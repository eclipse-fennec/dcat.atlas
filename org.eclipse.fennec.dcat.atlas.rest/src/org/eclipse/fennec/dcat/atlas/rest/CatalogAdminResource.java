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

import java.net.URI;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.rest.helper.ConditionalRequests;
import org.eclipse.fennec.dcat.atlas.rest.helper.WriteValidation;
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

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

/**
 * Admin (write) REST adapter for the {@code Catalog} collection (F-4/F-11).
 * <p>
 * Exposes only mutating operations (create/replace/delete) in JSON/XML (F-13,
 * replace-only per F-17). It lives on a separate {@code /admin} path from the
 * public {@link CatalogReadOnlyResource} so the upstream PEP (APISix/Keycloak)
 * can require authentication/authorization here while leaving reads open
 * (F-6/F-12). The adapter holds no business logic: it maps the request onto the
 * resource's public {@code rdf:about} URI (the read URL) and delegates to
 * {@link CatalogAdminService}.
 */
@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("CatalogAdminResource")
@Component(name = "CatalogAdminResource", service = CatalogAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/catalogs")
public class CatalogAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "catalogs";

	@Reference
	CatalogAdminService catalogAdminService;

	/**
	 * On-write SHACL enforcement (FR-4); gated by the validation service's config.
	 * Dynamic/optional so a validation reconfigure (shapes or enforce-flag change)
	 * rebinds here without recycling this resource and reloading the whole JAX-RS
	 * whiteboard; absent/unbound simply means no enforcement (see {@link WriteValidation}).
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@POST
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createCatalog(Catalog catalog, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, id);
		catalog.setAbout(about.toString());
		// Validate the exact form to be stored (about already stamped); 422 if enforced.
		ResponseBuilder invalid = WriteValidation.enforce(validationService, catalog, headers.getAcceptableMediaTypes());
		if (invalid != null) {
			return invalid.build();
		}
		catalogAdminService.upsertCatalog(catalog);
		ResponseBuilder created = Response.created(about).entity(catalog);
		catalogAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertCatalog(@PathParam("id") String id, Catalog catalog, @Context UriInfo uriInfo,
			@Context Request request, @Context HttpHeaders headers) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		catalog.setAbout(readUri(uriInfo, id).toString());
		ResponseBuilder invalid = WriteValidation.enforce(validationService, catalog, headers.getAcceptableMediaTypes());
		if (invalid != null) {
			return invalid.build();
		}
		boolean existed = catalogAdminService.getCatalog(id).isPresent();
		catalogAdminService.upsertCatalog(catalog);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(catalog);
		catalogAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteCatalog(@PathParam("id") String id, @Context Request request) {
		if (catalogAdminService.getCatalog(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		catalogAdminService.deleteCatalog(id, false);
		return Response.noContent().build();
	}

	// --- FR-9 catalog membership -------------------------------------------
	//
	// Assign/remove a member (Dataset, DataService or sub-catalog) to/from a
	// catalog without re-sending the catalog itself. The member is contained in
	// the catalog, so these operate purely on the catalog identified by {id}.

	@POST
	@Path("/{id}/datasets")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response addDataset(@PathParam("id") String id, Dataset dataset, @Context Request request) {
		return addMember(id, request, () -> catalogAdminService.addDatasetToCatalog(id, dataset));
	}

	@DELETE
	@Path("/{id}/datasets/{datasetId}")
	public Response removeDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteDatasetFromCatalog(id, datasetId));
	}

	@POST
	@Path("/{id}/services")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response addService(@PathParam("id") String id, DataService service, @Context Request request) {
		return addMember(id, request, () -> catalogAdminService.addDataServiceToCatalog(id, service));
	}

	@DELETE
	@Path("/{id}/services/{serviceId}")
	public Response removeService(@PathParam("id") String id, @PathParam("serviceId") String serviceId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteDataServiceFromCatalog(id, serviceId));
	}

	@POST
	@Path("/{id}/catalogs")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response addSubCatalog(@PathParam("id") String id, Catalog subCatalog, @Context Request request) {
		return addMember(id, request, () -> catalogAdminService.addSubCatalogToCatalog(id, subCatalog));
	}

	@DELETE
	@Path("/{id}/catalogs/{subCatalogId}")
	public Response removeSubCatalog(@PathParam("id") String id, @PathParam("subCatalogId") String subCatalogId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteSubCatalogFromCatalog(id, subCatalogId));
	}

	/**
	 * Shared flow for the add-member endpoints: 404 if the catalog is unknown,
	 * optimistic-lock check against the catalog's ETag (F-16), then run {@code add}
	 * (idempotent in the service) and return 200 with the catalog's new ETag. A
	 * no-op add leaves the catalog — and therefore the ETag — unchanged.
	 */
	private Response addMember(String id, Request request, java.util.function.Supplier<Catalog> add) {
		if (catalogAdminService.getCatalog(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		ResponseBuilder ok = Response.ok(add.get());
		catalogAdminService.etag(id).ifPresent(ok::tag);
		return ok.build();
	}

	/** Shared flow for the remove-member endpoints (see {@link #addMember}); returns 204. */
	private Response removeMember(String id, Request request, Runnable remove) {
		if (catalogAdminService.getCatalog(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		remove.run();
		ResponseBuilder noContent = Response.noContent();
		catalogAdminService.etag(id).ifPresent(noContent::tag);
		return noContent.build();
	}

	/** The public (read-side) URI of the catalog, e.g. {@code {base}/catalogs/{id}}. */
	private static URI readUri(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_COLLECTION).path(id).build();
	}
}

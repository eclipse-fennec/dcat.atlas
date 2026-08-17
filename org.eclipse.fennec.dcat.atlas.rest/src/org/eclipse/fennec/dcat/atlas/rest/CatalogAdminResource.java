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
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.rest.helper.ConditionalRequests;
import org.eclipse.fennec.dcat.atlas.rest.helper.CreateIdentity;
import org.eclipse.fennec.dcat.atlas.rest.helper.ReplaceIdentity;
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
import rdf.IdentifiedResource;
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
	/** Our EMF model's own XMI — the only write format. The codec picks its codec by
	 * media type, so "application/xml" would select a plain-XML one that does not
	 * understand xmi:version or a literal in attribute form. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "catalogs";

	@Reference
	CatalogAdminService catalogAdminService;

	/**
	 * Read-only, because the membership endpoints only ask these whether a member id is
	 * already taken — the write itself goes through {@link CatalogAdminService}, which owns
	 * the reference as well as the member.
	 */
	@Reference
	DatasetReadOnlyService datasetReadOnlyService;

	@Reference
	DataServiceReadOnlyService dataServiceReadOnlyService;

	/**
	 * On-write SHACL enforcement (FR-4); gated by the validation service's config.
	 * Dynamic/optional so a validation reconfigure (shapes or enforce-flag change)
	 * rebinds here without recycling this resource and reloading the whole JAX-RS
	 * whiteboard; absent/unbound simply means no enforcement (see {@link WriteValidation}).
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@POST
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response createCatalog(Catalog catalog, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
		// The identity is logical, and taken from the body when it names one of ours so that
		// this same request sent twice conflicts instead of creating a second catalog; only
		// then is one minted (see CreateIdentity). The request URL supplies nothing but the
		// Location header — stamping it here is what used to freeze the writing host into
		// the stored file (and, behind a proxy, an internal address).
		CreateIdentity identity = CreateIdentity.resolve(DcatIds.CATALOGS, catalog, this::catalogExists, uriInfo);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String id = identity.id();
		URI about = readUri(uriInfo, id);
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
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response upsertCatalog(@PathParam("id") String id, Catalog catalog, @Context UriInfo uriInfo,
			@Context Request request, @Context HttpHeaders headers) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// The path says which catalog this is; the body may agree or say nothing, but it may
		// not name a different one — nor one of somebody else's (D1/D2, replace-only F-17).
		ResponseBuilder mismatch = ReplaceIdentity.stamp(DcatIds.CATALOGS, id, catalog);
		if (mismatch != null) {
			return mismatch.build();
		}
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

	/**
	 * Stores the submitted Dataset and makes it a member. The Dataset must be a new one:
	 * one whose identity is already taken is refused with 409 rather than replaced, because
	 * this endpoint writes the whole body and the Dataset it would overwrite may be a member
	 * of other catalogs, series and services too — see {@link CreateIdentity#resolveMember}.
	 * Use {@link #linkDataset} to attach one that exists, or {@code PUT /admin/datasets/{id}}
	 * to change it.
	 */
	@POST
	@Path("/{id}/datasets")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response addDataset(@PathParam("id") String id, Dataset dataset, @Context UriInfo uriInfo,
			@Context Request request, @Context HttpHeaders headers) {
		return addMember(id, request,
				() -> refuseMember(DcatIds.DATASETS, dataset, this::datasetExists, membership(id, "datasets"), uriInfo,
						headers),
				() -> catalogAdminService.addDatasetToCatalog(id, dataset));
	}

	/**
	 * Links a Dataset that already exists — the counterpart of the {@code DELETE} on this
	 * same path, and the request the {@code POST} above points at when it refuses. This one
	 * carries no body, so it attaches the Dataset without touching its content. 404 if
	 * either the catalog or the dataset is unknown.
	 */
	@PUT
	@Path("/{id}/datasets/{datasetId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		return addMember(id, request, () -> catalogAdminService.linkDatasetToCatalog(id, datasetId));
	}

	@DELETE
	@Path("/{id}/datasets/{datasetId}")
	public Response removeDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteDatasetFromCatalog(id, datasetId));
	}

	@POST
	@Path("/{id}/services")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response addService(@PathParam("id") String id, DataService service, @Context UriInfo uriInfo,
			@Context Request request, @Context HttpHeaders headers) {
		return addMember(id, request,
				() -> refuseMember(DcatIds.DATA_SERVICES, service, this::dataServiceExists, membership(id, "services"),
						uriInfo, headers),
				() -> catalogAdminService.addDataServiceToCatalog(id, service));
	}

	/** Links a DataService that already exists; see {@link #linkDataset}. */
	@PUT
	@Path("/{id}/services/{serviceId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkService(@PathParam("id") String id, @PathParam("serviceId") String serviceId,
			@Context Request request) {
		return addMember(id, request, () -> catalogAdminService.linkDataServiceToCatalog(id, serviceId));
	}

	@DELETE
	@Path("/{id}/services/{serviceId}")
	public Response removeService(@PathParam("id") String id, @PathParam("serviceId") String serviceId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteDataServiceFromCatalog(id, serviceId));
	}

	@POST
	@Path("/{id}/catalogs")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response addSubCatalog(@PathParam("id") String id, Catalog subCatalog, @Context UriInfo uriInfo,
			@Context Request request, @Context HttpHeaders headers) {
		return addMember(id, request,
				() -> refuseMember(DcatIds.CATALOGS, subCatalog, this::catalogExists, membership(id, "catalogs"),
						uriInfo, headers),
				() -> catalogAdminService.addSubCatalogToCatalog(id, subCatalog));
	}

	/** Links a sub-catalog that already exists; see {@link #linkDataset}. */
	@PUT
	@Path("/{id}/catalogs/{subCatalogId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkSubCatalog(@PathParam("id") String id, @PathParam("subCatalogId") String subCatalogId,
			@Context Request request) {
		return addMember(id, request, () -> catalogAdminService.linkSubCatalogToCatalog(id, subCatalogId));
	}

	@DELETE
	@Path("/{id}/catalogs/{subCatalogId}")
	public Response removeSubCatalog(@PathParam("id") String id, @PathParam("subCatalogId") String subCatalogId,
			@Context Request request) {
		return removeMember(id, request, () -> catalogAdminService.deleteSubCatalogFromCatalog(id, subCatalogId));
	}

	/**
	 * Shared flow for the add- and link-member endpoints: 404 if the catalog is unknown,
	 * optimistic-lock check against the catalog's ETag (F-16), then run {@code add}
	 * (idempotent in the service) and return 200 with the catalog's new ETag. A
	 * no-op add leaves the catalog — and therefore the ETag — unchanged.
	 * <p>
	 * The link endpoints require their member to exist already, and the service signals a
	 * missing one with {@link NoSuchElementException}. That is a 404 about the member, not
	 * a server fault — without this it would surface as a 500, since the bundle registers
	 * no {@code ExceptionMapper}.
	 */
	private Response addMember(String id, Request request, java.util.function.Supplier<Catalog> add) {
		// The link endpoints persist no member of their own, so there is nothing to validate:
		// whatever they point at was validated when it was written.
		return addMember(id, request, () -> null, add);
	}

	private Response addMember(String id, Request request, java.util.function.Supplier<ResponseBuilder> validate,
			java.util.function.Supplier<Catalog> add) {
		if (catalogAdminService.getCatalog(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// After the 404 (an unknown catalog cannot take a member, conformant or not) and
		// before the write, per FR-4.
		ResponseBuilder invalid = validate.get();
		if (invalid != null) {
			return invalid.build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, catalogAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		Catalog catalog;
		try {
			catalog = add.get();
		} catch (NoSuchElementException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder ok = Response.ok(catalog);
		catalogAdminService.etag(id).ifPresent(ok::tag);
		return ok.build();
	}

	/**
	 * Everything that can refuse a member submitted in the body, in the order a create in
	 * the member's own collection applies it. {@code add} <em>stores</em> the member before
	 * linking it, so these endpoints must be gated exactly as {@code POST}/{@code PUT} on
	 * that collection is — otherwise both FR-4 enforcement and the identity rules are
	 * bypassable by choosing a different path. Resolving the identity first also stamps it,
	 * so the shapes see the exact form that will be stored.
	 *
	 * @return the response refusing the member, or {@code null} when it may be stored
	 */
	private ResponseBuilder refuseMember(String collection, IdentifiedResource member, Predicate<String> exists,
			String membershipPath, UriInfo uriInfo, HttpHeaders headers) {
		CreateIdentity identity = CreateIdentity.resolveMember(collection, member, exists, membershipPath, uriInfo);
		if (identity.refused()) {
			return identity.refusal();
		}
		return WriteValidation.enforce(validationService, member, headers.getAcceptableMediaTypes());
	}

	/** The path a membership lives under, as the refusal above names it to the client. */
	private static String membership(String id, String segment) {
		return READ_COLLECTION + "/" + id + "/" + segment;
	}

	private boolean datasetExists(String id) {
		return datasetReadOnlyService.getDataset(id).isPresent();
	}

	private boolean dataServiceExists(String id) {
		return dataServiceReadOnlyService.getDataService(id).isPresent();
	}

	private boolean catalogExists(String id) {
		return catalogAdminService.getCatalog(id).isPresent();
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

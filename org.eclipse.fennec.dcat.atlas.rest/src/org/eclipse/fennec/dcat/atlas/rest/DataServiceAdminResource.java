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

import java.net.URI;
import java.util.NoSuchElementException;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.admin.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.rest.filter.PublicIriFilter;
import org.eclipse.fennec.dcat.atlas.rest.helper.ConditionalRequests;
import org.eclipse.fennec.dcat.atlas.rest.helper.CreateIdentity;
import org.eclipse.fennec.dcat.atlas.rest.helper.ReplaceIdentity;
import org.eclipse.fennec.dcat.atlas.rest.helper.CascadeReport;
import org.eclipse.fennec.dcat.atlas.rest.helper.PublicUri;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.DataService;
import dcat.Dataset;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;


@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("DataServiceAdminResource")
@Component(name = "DataServiceAdminResource", service = DataServiceAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/data-services")
public class DataServiceAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	/** Our EMF model's own XMI — the only write format. The codec picks its codec by
	 * media type, so "application/xml" would select a plain-XML one that does not
	 * understand xmi:version or a literal in attribute form. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "data-services";

	/**
	 * The configured public base, used here to build the {@code Location} of a create
	 * and of the {@code 409} that refuses one, so those headers carry the same URL as
	 * the {@code about} the resource is served with. Mandatory also to gate
	 * registration — see {@link PublicIriFilter}.
	 */
	@Reference
	PublicIris identityRendering;

	@Reference
	DataServiceAdminService dataServiceAdminService;

	/**
	 * Read-only, because {@code servesDataset} membership only asks whether the Dataset id
	 * in the body is already taken; the write goes through {@link DataServiceAdminService},
	 * which owns the reference.
	 */
	@Reference
	DatasetReadOnlyService datasetReadOnlyService;

	@POST
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response createDataService(DataService dataService) {
		// The identity is logical, and taken from the body when it names one of ours so that
		// this same request sent twice conflicts instead of creating a second service; only
		// then is one minted (see CreateIdentity). The request URL supplies nothing but the
		// Location header — stamping it here is what used to freeze the writing host into
		// the stored file (and, behind a proxy, an internal address).
		CreateIdentity identity = CreateIdentity.resolve(DcatIds.DATA_SERVICES, dataService,
				candidate -> dataServiceAdminService.getDataService(candidate).isPresent(), identityRendering);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String id = identity.id();
		URI about = readUri(id);
		dataServiceAdminService.upsertDataService(dataService);
		ResponseBuilder created = Response.created(about).entity(dataService);
		dataServiceAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response upsertDataService(@PathParam("id") String id, DataService dataService,
			@Context Request request) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, dataServiceAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// The path says which service this is; the body may agree or say nothing, but it may
		// not name a different one — nor one of somebody else's (D1/D2, replace-only F-17).
		ResponseBuilder mismatch = ReplaceIdentity.stamp(DcatIds.DATA_SERVICES, id, dataService);
		if (mismatch != null) {
			return mismatch.build();
		}
		boolean existed = dataServiceAdminService.getDataService(id).isPresent();
		dataServiceAdminService.upsertDataService(dataService);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(dataService);
		dataServiceAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataService(@PathParam("id") String id,
			@QueryParam("cascade") @DefaultValue("false") boolean cascade, @Context Request request) {
		if (dataServiceAdminService.getDataService(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, dataServiceAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// If-Match was evaluated against this data service's ETag only. A cascade also rewrites
		// the referrers, whose ETags the caller never saw — see the service javadoc for why
		// that narrowing of F-16 is deliberate.
		List<String> unlinked = dataServiceAdminService.deleteDataService(id, cascade);
		return CascadeReport.respond(unlinked, identityRendering);
	}

	// --- dcat:servesDataset membership --------------------------------------
	//
	// Records which Datasets this service serves. The reference is declared on the
	// DataService, so the DataService is what is edited, what If-Match keys on and what
	// these endpoints return; the Dataset is untouched. Not the inverse of
	// dcat:accessService — that says which service gives access to a Distribution.

	/**
	 * Stores the submitted Dataset and links it. Like the other add-member endpoints this
	 * <em>writes</em> the body, so FR-4 gates it and a Dataset that already exists is
	 * refused with 409 rather than replaced — it is very likely served by other services and
	 * listed in catalogs, and none of them asked for it to change. Use the {@code PUT} below
	 * to attach one that exists, or {@code PUT /admin/datasets/{id}} to change it.
	 */
	@POST
	@Path("/{id}/datasets")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response addDataset(@PathParam("id") String id, Dataset dataset,
			@Context Request request) {
		if (dataServiceAdminService.getDataService(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Resolve the identity the store will file it under — refusing one that is not ours
		// and one that is already taken, exactly as POST /admin/datasets does — which also
		// stamps it, so the shapes see the exact form to be stored; then validate, then write.
		CreateIdentity identity = CreateIdentity.resolveMember(DcatIds.DATASETS, dataset,
				candidate -> datasetReadOnlyService.getDataset(candidate).isPresent(),
				READ_COLLECTION + "/" + id + "/" + DcatIds.DATASETS, identityRendering);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, dataServiceAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		ResponseBuilder ok = Response.ok(dataServiceAdminService.addDatasetToDataService(id, dataset));
		dataServiceAdminService.etag(id).ifPresent(ok::tag);
		return ok.build();
	}

	/** Links a Dataset that already exists — the counterpart of the {@code DELETE} below. */
	@PUT
	@Path("/{id}/datasets/{datasetId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		if (dataServiceAdminService.getDataService(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, dataServiceAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Nothing is written, so nothing to validate; an unknown dataset arrives as
		// NoSuchElementException, which is a 404 about the dataset and not a 500.
		DataService dataService;
		try {
			dataService = dataServiceAdminService.linkDatasetToDataService(id, datasetId);
		} catch (NoSuchElementException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder ok = Response.ok(dataService);
		dataServiceAdminService.etag(id).ifPresent(ok::tag);
		return ok.build();
	}

	@DELETE
	@Path("/{id}/datasets/{datasetId}")
	public Response removeDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		if (dataServiceAdminService.getDataService(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, dataServiceAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Idempotent: unlinking a dataset that is not served is a no-op, not a 404 —
		// consistent with the catalog and series membership removals.
		dataServiceAdminService.deleteDatasetFromDataService(id, datasetId);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the dataService, e.g. {@code {base}/dataServices/{id}}. */
	private URI readUri(String id) {
		return PublicUri.of(identityRendering, READ_COLLECTION, id);
	}
}

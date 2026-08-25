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

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
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
@JakartarsName("DatasetAdminResource")
@Component(name = "DatasetAdminResource", service = DatasetAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/datasets")
public class DatasetAdminResource {
	
	static final String JSON = "application/json";
	static final String XML = "application/xml";
	/** Our EMF model's own XMI — the only write format. The codec picks its codec by
	 * media type, so "application/xml" would select a plain-XML one that does not
	 * understand xmi:version or a literal in attribute form. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "datasets";

	/**
	 * The configured public base, used here to build the {@code Location} of a create
	 * and of the {@code 409} that refuses one, so those headers carry the same URL as
	 * the {@code about} the resource is served with. Mandatory also to gate
	 * registration — see {@link PublicIriFilter}.
	 */
	@Reference
	PublicIris identityRendering;

	@Reference
	DatasetAdminService datasetAdminService;

	@POST
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response createDataset(Dataset dataset) {
		// The identity is logical, and taken from the body when it names one of ours so that
		// this same request sent twice conflicts instead of creating a second dataset; only
		// then is one minted (see CreateIdentity). The request URL supplies nothing but the
		// Location header — stamping it here is what used to freeze the writing host into
		// the stored file (and, behind a proxy, an internal address).
		CreateIdentity identity = CreateIdentity.resolve(DcatIds.DATASETS, dataset,
				candidate -> datasetAdminService.getDataset(candidate).isPresent(), identityRendering);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String id = identity.id();
		URI about = readUri(id);
		datasetAdminService.upsertDataset(dataset);
		ResponseBuilder created = Response.created(about).entity(dataset);
		datasetAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response upsertDataset(@PathParam("id") String id, Dataset dataset,
			@Context Request request) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// The path says which dataset this is; the body may agree or say nothing, but it may
		// not name a different one — nor one of somebody else's (D1/D2, replace-only F-17).
		ResponseBuilder mismatch = ReplaceIdentity.stamp(DcatIds.DATASETS, id, dataset);
		if (mismatch != null) {
			return mismatch.build();
		}
		boolean existed = datasetAdminService.getDataset(id).isPresent();
		datasetAdminService.upsertDataset(dataset);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(dataset);
		datasetAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataset(@PathParam("id") String id,
			@QueryParam("cascade") @DefaultValue("false") boolean cascade, @Context Request request) {
		if (datasetAdminService.getDataset(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// If-Match was evaluated against this dataset's ETag only. A cascade also rewrites
		// the referrers, whose ETags the caller never saw — see the service javadoc for why
		// that narrowing of F-16 is deliberate.
		List<String> unlinked = datasetAdminService.deleteDataset(id, cascade);
		return CascadeReport.respond(unlinked, identityRendering);
	}

	/** The public (read-side) URI of the dataset, e.g. {@code {base}/datasets/{id}}. */
	private URI readUri(String id) {
		return PublicUri.of(identityRendering, READ_COLLECTION, id);
	}
}

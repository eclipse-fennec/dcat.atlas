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
import java.util.Optional;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
import org.eclipse.fennec.dcat.atlas.rest.helper.ConditionalRequests;
import org.eclipse.fennec.dcat.atlas.rest.helper.CreateIdentity;
import org.eclipse.fennec.dcat.atlas.rest.helper.ReplaceIdentity;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.DataService;
import dcat.Distribution;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;


@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("DistributionAdminResource")
@Component(name = "DistributionAdminResource", service = DistributionAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/datasets/{datasetId}/distributions")
public class DistributionAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	/** Our EMF model's own XMI — the only write format. The codec picks its codec by
	 * media type, so "application/xml" would select a plain-XML one that does not
	 * understand xmi:version or a literal in attribute form. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";

	/** Public read collection segments the dereferenceable {@code about} URI points at. */
	private static final String READ_DATASETS = "datasets";
	private static final String READ_DISTRIBUTIONS = "distributions";

	@Reference
	DistributionAdminService distributionAdminService;

	/** Used only to answer 404 (rather than 500) when the parent dataset is unknown (FR-10). */
	@Reference
	DatasetReadOnlyService datasetReadOnlyService;

	/** Resolves the {@code accessService} target: the DataService must already be catalogued. */
	@Reference
	DataServiceReadOnlyService dataServiceReadOnlyService;

	/**
	 * FR-10: a Distribution is always created in the context of its Dataset (the
	 * {@code datasetId} path segment), so there is no dataset-less create.
	 */
	@POST
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response createDistribution(@PathParam("datasetId") String datasetId, Distribution distribution,
			@Context UriInfo uriInfo) {
		if (datasetReadOnlyService.getDataset(datasetId).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Logical identity, nested under the dataset, and taken from the body when it names
		// one of this dataset's distributions so that the same request sent twice conflicts
		// instead of creating a second one; only then is an id minted (see CreateIdentity).
		// The request URL supplies nothing but Location.
		CreateIdentity identity = CreateIdentity.resolveDistribution(datasetId, distribution,
				candidate -> distributionAdminService.getDistributionForDataset(datasetId, candidate).isPresent(),
				uriInfo);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String id = identity.id();
		URI about = readUri(uriInfo, datasetId, id);
		distributionAdminService.upsertDistributionToDataset(datasetId, distribution);
		ResponseBuilder created = Response.created(about).entity(distribution);
		distributionAdminService.etag(datasetId, id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response upsertDistribution(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			Distribution distribution, @Context UriInfo uriInfo, @Context Request request) {
		if (datasetReadOnlyService.getDataset(datasetId).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(datasetId, id));
		if (precondition != null) {
			return precondition.build();
		}
		// The path says which distribution of which dataset this is; the body may agree or
		// say nothing, but it may not name a different one — including another dataset's
		// distribution (D1/D2, replace-only F-17).
		ResponseBuilder mismatch = ReplaceIdentity.stampDistribution(datasetId, id, distribution);
		if (mismatch != null) {
			return mismatch.build();
		}
		boolean existed = distributionAdminService.getDistributionForDataset(datasetId, id).isPresent();
		distributionAdminService.upsertDistributionToDataset(datasetId, distribution);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(distribution);
		distributionAdminService.etag(datasetId, id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDistribution(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			@Context Request request) {
		if (distributionAdminService.getDistributionForDataset(datasetId, id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(datasetId, id));
		if (precondition != null) {
			return precondition.build();
		}
		distributionAdminService.deleteDistributionFromDataset(datasetId, id);
		return Response.noContent().build();
	}

	// --- FR-10 accessService link ------------------------------------------
	//
	// Records which already catalogued DataService gives access to this distribution
	// (DCAT-AP.de 3.0 §4.6.24). The link is stored as dcat:accessService on the
	// Distribution, so the Distribution is what is edited, what If-Match keys on and
	// what these endpoints return. The DataService itself is never modified: it is a
	// catalog entity in its own right, referenced by URI rather than copied.

	@PUT
	@Path("/{id}/access-service/{serviceId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkAccessService(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			@PathParam("serviceId") String serviceId, @Context Request request) {
		if (distributionAdminService.getDistributionForDataset(datasetId, id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// The service must already exist in the catalog — accessService references it, so
		// there is nothing to point at otherwise.
		Optional<DataService> dataService = dataServiceReadOnlyService.getDataService(serviceId);
		if (dataService.isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(datasetId, id));
		if (precondition != null) {
			return precondition.build();
		}
		// link, not add: the DataService is named by id and already stored, so there is
		// nothing to write. The add* variant takes the entity and store.put()s it, which
		// here would mean reading the service only to write it straight back.
		ResponseBuilder ok = Response
				.ok(distributionAdminService.linkAccessServiceToDistribution(datasetId, id, serviceId));
		distributionAdminService.etag(datasetId, id).ifPresent(ok::tag);
		return ok.build();
	}

	@DELETE
	@Path("/{id}/access-service/{serviceId}")
	public Response removeAccessService(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			@PathParam("serviceId") String serviceId, @Context Request request) {
		if (distributionAdminService.getDistributionForDataset(datasetId, id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(datasetId, id));
		if (precondition != null) {
			return precondition.build();
		}
		// Idempotent: unlinking a service that is not referenced is a no-op, not a 404 —
		// consistent with the FR-11 series membership removal.
		distributionAdminService.deleteAccessServiceFromDistribution(datasetId, id, serviceId);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the distribution, e.g. {@code {base}/datasets/{datasetId}/distributions/{id}}. */
	private static URI readUri(UriInfo uriInfo, String datasetId, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_DATASETS).path(datasetId).path(READ_DISTRIBUTIONS).path(id)
				.build();
	}

}

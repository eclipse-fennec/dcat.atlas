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
import java.util.Optional;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminService;
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

import dcat.Dataset;
import dcat.DatasetSeries;
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
@JakartarsName("DatasetSeriesAdminResource")
@Component(name = "DatasetSeriesAdminResource", service = DatasetSeriesAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/dataset-series")
public class DatasetSeriesAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	/** Our EMF model's own XMI — the only write format. The codec picks its codec by
	 * media type, so "application/xml" would select a plain-XML one that does not
	 * understand xmi:version or a literal in attribute form. */
	static final String XMI = "application/xmi";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "dataset-series";

	/**
	 * The configured public base, used here to build the {@code Location} of a create
	 * and of the {@code 409} that refuses one, so those headers carry the same URL as
	 * the {@code about} the resource is served with. Mandatory also to gate
	 * registration — see {@link PublicIriFilter}.
	 */
	@Reference
	PublicIris identityRendering;

	@Reference
	DatasetSeriesAdminService datasetSeriesAdminService;

	/**
	 * FR-11 membership lives as {@code dcat:inSeries} on the <em>Dataset</em>, so the
	 * dataset is what actually changes — its ETag is the optimistic-lock target and
	 * the validator returned by the membership endpoints (F-16).
	 */
	@Reference
	DatasetReadOnlyService datasetReadOnlyService;

	@POST
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response createDatasetSeries(DatasetSeries datasetSeries) {
		// The identity is logical, and taken from the body when it names one of ours so that
		// this same request sent twice conflicts instead of creating a second series; only
		// then is one minted (see CreateIdentity). The request URL supplies nothing but the
		// Location header — stamping it here is what used to freeze the writing host into
		// the stored file (and, behind a proxy, an internal address).
		CreateIdentity identity = CreateIdentity.resolve(DcatIds.DATASET_SERIES, datasetSeries,
				candidate -> datasetSeriesAdminService.getDatasetSeries(candidate).isPresent(), identityRendering);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String id = identity.id();
		URI about = readUri(id);
		datasetSeriesAdminService.upsertDatasetSeries(datasetSeries);
		ResponseBuilder created = Response.created(about).entity(datasetSeries);
		datasetSeriesAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response upsertDatasetSeries(@PathParam("id") String id, DatasetSeries datasetSeries,
			@Context Request request) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetSeriesAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// The path says which series this is; the body may agree or say nothing, but it may
		// not name a different one — nor one of somebody else's (D1/D2, replace-only F-17).
		ResponseBuilder mismatch = ReplaceIdentity.stamp(DcatIds.DATASET_SERIES, id, datasetSeries);
		if (mismatch != null) {
			return mismatch.build();
		}
		boolean existed = datasetSeriesAdminService.getDatasetSeries(id).isPresent();
		datasetSeriesAdminService.upsertDatasetSeries(datasetSeries);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(datasetSeries);
		datasetSeriesAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDatasetSeries(@PathParam("id") String id,
			@QueryParam("cascade") @DefaultValue("false") boolean cascade, @Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetSeriesAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// If-Match was evaluated against this dataset series's ETag only. A cascade also rewrites
		// the referrers, whose ETags the caller never saw — see the service javadoc for why
		// that narrowing of F-16 is deliberate.
		List<String> unlinked = datasetSeriesAdminService.deleteDatasetSeries(id, cascade);
		return CascadeReport.respond(unlinked, identityRendering);
	}

	// --- FR-11 series membership -------------------------------------------
	//
	// Assign/remove a Dataset to/from a series without re-sending the series.
	// Membership is stored as dcat:inSeries on the Dataset, so the service edits
	// and stores the owning Dataset — and the Dataset's ETag is what If-Match keys
	// on and what these endpoints return.

	@POST
	@Path("/{id}/datasets")
	@Consumes({ XMI })
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response addDataset(@PathParam("id") String id, Dataset dataset,
			@Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// This stores the Dataset before linking it, so it is gated exactly as POST
		// /admin/datasets is: an identity of ours or none at all, and a Dataset that already
		// exists is refused rather than replaced — it may be in other series, catalogs and
		// services, none of which asked for it to change. Resolving stamps the identity, so
		// FR-4 below sees the form that will be stored.
		CreateIdentity identity = CreateIdentity.resolveMember(DcatIds.DATASETS, dataset,
				candidate -> datasetReadOnlyService.getDataset(candidate).isPresent(),
				READ_COLLECTION + "/" + id + "/" + DcatIds.DATASETS, identityRendering);
		if (identity.refused()) {
			return identity.refusal().build();
		}
		String datasetId = identity.id();
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetEtag(datasetId));
		if (precondition != null) {
			return precondition.build();
		}
		ResponseBuilder ok = Response.ok(datasetSeriesAdminService.addDatasetToDatasetSeries(id, dataset));
		datasetEtag(datasetId).ifPresent(ok::tag);
		return ok.build();
	}

	/**
	 * Links a Dataset that already exists — the counterpart of the {@code DELETE} on this
	 * same path, and the request the {@code POST} above points at when it refuses one. This
	 * one carries no body, so it attaches the Dataset without touching its content. 404 if
	 * either the series or the dataset is unknown.
	 */
	@PUT
	@Path("/{id}/datasets/{datasetId}")
	@Produces({ XMI, JSON, XML, RDF_XML })
	public Response linkDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetEtag(datasetId));
		if (precondition != null) {
			return precondition.build();
		}
		// Nothing is written, so nothing to validate; the service signals an unknown dataset
		// with NoSuchElementException, which is a 404 about the dataset and not a 500 (the
		// bundle registers no ExceptionMapper).
		DatasetSeries series;
		try {
			series = datasetSeriesAdminService.linkDatasetToDatasetSeries(id, datasetId);
		} catch (NoSuchElementException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder ok = Response.ok(series);
		datasetEtag(datasetId).ifPresent(ok::tag);
		return ok.build();
	}

	@DELETE
	@Path("/{id}/datasets/{datasetId}")
	public Response removeDataset(@PathParam("id") String id, @PathParam("datasetId") String datasetId,
			@Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetEtag(datasetId));
		if (precondition != null) {
			return precondition.build();
		}
		datasetSeriesAdminService.deleteDatasetFromDatasetSeries(id, datasetId);
		return Response.noContent().build();
	}

	/** Current ETag of the dataset {@code id}, or empty when the id is missing/absent. */
	private Optional<String> datasetEtag(String datasetId) {
		return datasetId == null || datasetId.isBlank() ? Optional.empty() : datasetReadOnlyService.etag(datasetId);
	}

	/** The public (read-side) URI of the datasetSeries, e.g. {@code {base}/datasetSeriess/{id}}. */
	private URI readUri(String id) {
		return PublicUri.of(identityRendering, READ_COLLECTION, id);
	}

}

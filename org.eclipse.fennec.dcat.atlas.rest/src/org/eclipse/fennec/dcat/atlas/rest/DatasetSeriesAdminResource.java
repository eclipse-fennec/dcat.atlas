package org.eclipse.fennec.dcat.atlas.rest;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Dataset;
import dcat.DatasetSeries;
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
@JakartarsName("DatasetSeriesAdminResource")
@Component(name = "DatasetSeriesAdminResource", service = DatasetSeriesAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/dataset-series")
public class DatasetSeriesAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "dataset-series";

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
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createDatasetSeries(DatasetSeries datasetSeries, @Context UriInfo uriInfo) {
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, id);
		datasetSeries.setAbout(about.toString());
		datasetSeriesAdminService.upsertDatasetSeries(datasetSeries);
		ResponseBuilder created = Response.created(about).entity(datasetSeries);
		datasetSeriesAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertDatasetSeries(@PathParam("id") String id, DatasetSeries datasetSeries,
			@Context UriInfo uriInfo, @Context Request request) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetSeriesAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		datasetSeries.setAbout(readUri(uriInfo, id).toString());
		boolean existed = datasetSeriesAdminService.getDatasetSeries(id).isPresent();
		datasetSeriesAdminService.upsertDatasetSeries(datasetSeries);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(datasetSeries);
		datasetSeriesAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDatasetSeries(@PathParam("id") String id, @Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetSeriesAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		datasetSeriesAdminService.deleteDatasetSeries(id, false);
		return Response.noContent().build();
	}

	// --- FR-11 series membership -------------------------------------------
	//
	// Assign/remove a Dataset to/from a series without re-sending the series.
	// Membership is stored as dcat:inSeries on the Dataset, so the service edits
	// and stores the owning Dataset — and the Dataset's ETag is what If-Match keys
	// on and what these endpoints return.

	@POST
	@Path("/{id}/datasets")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response addDataset(@PathParam("id") String id, Dataset dataset, @Context Request request) {
		if (datasetSeriesAdminService.getDatasetSeries(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		String datasetId = idOfAbout(dataset.getAbout());
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetEtag(datasetId));
		if (precondition != null) {
			return precondition.build();
		}
		ResponseBuilder ok = Response.ok(datasetSeriesAdminService.addDatasetToDatasetSeries(id, dataset));
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

	/** Last path segment of a resource {@code about} URI (its storage id), or {@code null}. */
	private static String idOfAbout(String about) {
		if (about == null || about.isBlank()) {
			return null;
		}
		int slash = about.lastIndexOf('/');
		String candidate = slash >= 0 ? about.substring(slash + 1) : about;
		return candidate.isBlank() ? null : candidate;
	}

	/** The public (read-side) URI of the datasetSeries, e.g. {@code {base}/datasetSeriess/{id}}. */
	private static URI readUri(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_COLLECTION).path(id).build();
	}

}

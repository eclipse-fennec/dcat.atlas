package org.eclipse.fennec.dcat.atlas.rest;

import java.net.URI;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Dataset;
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
@JakartarsName("DatasetAdminResource")
@Component(name = "DatasetAdminResource", service = DatasetAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/datasets")
public class DatasetAdminResource {
	
	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "datasets";

	@Reference
	DatasetAdminService datasetAdminService;

	@POST
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createDataset(Dataset dataset, @Context UriInfo uriInfo) {
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, id);
		dataset.setAbout(about.toString());
		datasetAdminService.upsertDataset(dataset);
		ResponseBuilder created = Response.created(about).entity(dataset);
		datasetAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertDataset(@PathParam("id") String id, Dataset dataset, @Context UriInfo uriInfo,
			@Context Request request) {
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		dataset.setAbout(readUri(uriInfo, id).toString());
		boolean existed = datasetAdminService.getDataset(id).isPresent();
		datasetAdminService.upsertDataset(dataset);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(dataset);
		datasetAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDataset(@PathParam("id") String id, @Context Request request) {
		if (datasetAdminService.getDataset(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, datasetAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		datasetAdminService.deleteDataset(id, false);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the dataset, e.g. {@code {base}/datasets/{id}}. */
	private static URI readUri(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_COLLECTION).path(id).build();
	}
}

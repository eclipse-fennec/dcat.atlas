package org.eclipse.fennec.dcat.atlas.rest;

import java.net.URI;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Distribution;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;


@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("DistributionAdminResource")
@Component(name = "DistributionAdminResource", service = DistributionAdminResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/distributions")
public class DistributionAdminResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";

	/** Public collection segment the dereferenceable {@code about} URI points at. */
	private static final String READ_COLLECTION = "distributions";

	@Reference
	DistributionAdminService distributionAdminService;

	@POST
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createDistribution(Distribution distribution, @Context UriInfo uriInfo) {
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, id);
		distribution.setAbout(about.toString());
		Distribution stored = distributionAdminService.upsertDistribution(distribution);
		return Response.created(about).entity(stored).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertDistribution(@PathParam("id") String id, Distribution distribution, @Context UriInfo uriInfo) {
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		distribution.setAbout(readUri(uriInfo, id).toString());
		boolean existed = distributionAdminService.getDistribution(id).isPresent();
		Distribution stored = distributionAdminService.upsertDistribution(distribution);
		Status status = existed ? Status.OK : Status.CREATED;
		return Response.status(status).entity(stored).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDistribution(@PathParam("id") String id) {
		if (distributionAdminService.getDistribution(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		distributionAdminService.deleteDistribution(id, false);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the distribution, e.g. {@code {base}/distributions/{id}}. */
	private static URI readUri(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_COLLECTION).path(id).build();
	}

}

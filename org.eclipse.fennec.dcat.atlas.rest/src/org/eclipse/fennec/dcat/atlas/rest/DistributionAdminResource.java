package org.eclipse.fennec.dcat.atlas.rest;

import java.net.URI;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
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

import dcat.Distribution;
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
	static final String RDF_XML = "application/rdf+xml";

	/** Public read collection segments the dereferenceable {@code about} URI points at. */
	private static final String READ_DATASETS = "datasets";
	private static final String READ_DISTRIBUTIONS = "distributions";

	@Reference
	DistributionAdminService distributionAdminService;

	/** Used only to answer 404 (rather than 500) when the parent dataset is unknown (FR-10). */
	@Reference
	DatasetReadOnlyService datasetReadOnlyService;

	/**
	 * On-write SHACL enforcement (FR-4); gated by the validation service's config.
	 * Dynamic/optional so a validation reconfigure (shapes or enforce-flag change)
	 * rebinds here without recycling this resource and reloading the whole JAX-RS
	 * whiteboard; absent/unbound simply means no enforcement (see {@link WriteValidation}).
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	/**
	 * FR-10: a Distribution is always created in the context of its Dataset (the
	 * {@code datasetId} path segment), so there is no dataset-less create.
	 */
	@POST
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createDistribution(@PathParam("datasetId") String datasetId, Distribution distribution,
			@Context UriInfo uriInfo, @Context HttpHeaders headers) {
		if (datasetReadOnlyService.getDataset(datasetId).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, datasetId, id);
		distribution.setAbout(about.toString());
		// Validate the exact form to be stored (about already stamped); 422 if enforced.
		ResponseBuilder invalid = WriteValidation.enforce(validationService, distribution, headers.getAcceptableMediaTypes());
		if (invalid != null) {
			return invalid.build();
		}
		distributionAdminService.upsertDistributionToDataset(datasetId, distribution);
		ResponseBuilder created = Response.created(about).entity(distribution);
		distributionAdminService.etag(id).ifPresent(created::tag);
		return created.build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertDistribution(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			Distribution distribution, @Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders headers) {
		if (datasetReadOnlyService.getDataset(datasetId).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		// Optimistic locking (F-16): reject a stale If-Match; If-None-Match: * makes it create-only.
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		distribution.setAbout(readUri(uriInfo, datasetId, id).toString());
		ResponseBuilder invalid = WriteValidation.enforce(validationService, distribution, headers.getAcceptableMediaTypes());
		if (invalid != null) {
			return invalid.build();
		}
		boolean existed = distributionAdminService.getDistributionForDataset(datasetId, id).isPresent();
		distributionAdminService.upsertDistributionToDataset(datasetId, distribution);
		ResponseBuilder response = Response.status(existed ? Status.OK : Status.CREATED).entity(distribution);
		distributionAdminService.etag(id).ifPresent(response::tag);
		return response.build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteDistribution(@PathParam("datasetId") String datasetId, @PathParam("id") String id,
			@Context Request request) {
		if (distributionAdminService.getDistributionForDataset(datasetId, id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		ResponseBuilder precondition = ConditionalRequests.evaluate(request, distributionAdminService.etag(id));
		if (precondition != null) {
			return precondition.build();
		}
		distributionAdminService.deleteDistributionFromDataset(datasetId, id);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the distribution, e.g. {@code {base}/datasets/{datasetId}/distributions/{id}}. */
	private static URI readUri(UriInfo uriInfo, String datasetId, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_DATASETS).path(datasetId).path(READ_DISTRIBUTIONS).path(id)
				.build();
	}

}

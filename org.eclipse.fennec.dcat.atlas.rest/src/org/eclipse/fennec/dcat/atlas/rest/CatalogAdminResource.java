package org.eclipse.fennec.dcat.atlas.rest;

import java.net.URI;
import java.util.UUID;

import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import dcat.Catalog;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
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

	@POST
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response createCatalog(Catalog catalog, @Context UriInfo uriInfo) {
		// Mint an id and make the resource's public read URL its about (D1/D2).
		String id = UUID.randomUUID().toString();
		URI about = readUri(uriInfo, id);
		catalog.setAbout(about.toString());
		Catalog stored = catalogAdminService.upsertCatalog(catalog);
		return Response.created(about).entity(stored).build();
	}

	@PUT
	@Path("/{id}")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ JSON, XML, RDF_XML })
	public Response upsertCatalog(@PathParam("id") String id, Catalog catalog, @Context UriInfo uriInfo) {
		// Force the public read URL onto the payload so the service stores it under
		// {id} regardless of what the client sent (D1/D2, replace-only F-17).
		catalog.setAbout(readUri(uriInfo, id).toString());
		boolean existed = catalogAdminService.getCatalog(id).isPresent();
		Catalog stored = catalogAdminService.upsertCatalog(catalog);
		Status status = existed ? Status.OK : Status.CREATED;
		return Response.status(status).entity(stored).build();
	}

	@DELETE
	@Path("/{id}")
	public Response deleteCatalog(@PathParam("id") String id) {
		if (catalogAdminService.getCatalog(id).isEmpty()) {
			return Response.status(Status.NOT_FOUND).build();
		}
		catalogAdminService.deleteCatalog(id, false);
		return Response.noContent().build();
	}

	/** The public (read-side) URI of the catalog, e.g. {@code {base}/catalogs/{id}}. */
	private static URI readUri(UriInfo uriInfo, String id) {
		return uriInfo.getBaseUriBuilder().path(READ_COLLECTION).path(id).build();
	}
}

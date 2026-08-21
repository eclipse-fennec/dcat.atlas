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
package org.eclipse.fennec.dcat.atlas.rest.openapi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.dcat.atlas.api.PublicIris;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.BaseOpenApiResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.integration.api.OpenApiScanner;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Serves the OpenAPI descriptor of the read and admin API at
 * {@code /openapi.json} and {@code /openapi.yaml} (dcat.atlas#21).
 *
 * <h2>Where the content comes from</h2>
 *
 * Nothing here describes the API by hand. The descriptor is derived from the JAX-RS
 * annotations the resources already carry — {@code @Path}, the verbs, {@code @Consumes},
 * {@code @Produces}, {@code @PathParam} — so it cannot drift from the code the way a
 * hand-maintained endpoint list can. No resource carries a swagger annotation, and this
 * class is the only place swagger is referenced at all.
 * <p>
 * This mirrors {@code OpenApiResource} in {@code model.atlas}, deliberately: the two
 * portals should answer the same shape of request, so one generated client can target
 * both. Two differences, both intentional:
 * <ul>
 * <li><b>No media-type enhancement.</b> model.atlas injects its registered media types
 * into every operation, because there the set is dynamic. Here it would be actively
 * wrong: reads negotiate eight formats while writes accept {@code application/xmi}
 * alone, and a blanket injection would advertise that {@code POST /admin/catalogs}
 * takes Turtle. The per-method annotations already state the truth, so they are left
 * to speak for themselves.</li>
 * <li><b>The server URL comes from {@link PublicIris}</b> rather than from the request,
 * for the same reason the {@code Location} header does — behind a reverse proxy the
 * address the container sees is not the one clients use.</li>
 * </ul>
 *
 * <h2>What it does not describe</h2>
 *
 * Request and response bodies are XMI of an EMF model, which does not reduce to a
 * useful JSON Schema, so the descriptor carries no body schemas. Nor does it carry the
 * status-code semantics that matter most to a client — {@code 409} on a taken identity,
 * {@code 400} on a foreign {@code about}, {@code 412} on a stale {@code If-Match}.
 * Those live in the user guide, and the description below points at it rather than
 * pretending otherwise.
 */
@RequireJakartarsWhiteboard
@JakartarsResource
@JakartarsName("DcatOpenApiResource")
@Component(name = "DcatOpenApiResource", service = DcatOpenApiResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/openapi.{type:json|yaml}")
public class DcatOpenApiResource extends BaseOpenApiResource {

	@Context
	private Application app;

	@Context
	private ServletConfig config;

	/**
	 * The configured public base, used for the {@code servers} entry. Optional and
	 * dynamic on purpose: the descriptor is still worth serving without it, and this
	 * resource should not be the one endpoint that disappears when identity rendering
	 * is unconfigured — the collection resources already answer that case with a 404.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile PublicIris publicIris;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, YAML })
	@Operation(hidden = true)
	public Response getOpenApi(@Context HttpHeaders headers, @Context UriInfo uriInfo,
			@PathParam("type") String type) throws Exception {

		OpenAPI oas = context().read();
		if (oas == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		describe(oas);

		// Always pretty-printed: a descriptor is read by people at least as often as by
		// generators, and the compact form would only be worth the extra dependency on
		// Jackson's ObjectMapper that Json.mapper()/Yaml.mapper() would drag onto the
		// buildpath.
		if ("yaml".equalsIgnoreCase(type == null ? "" : type.trim())) {
			return Response.ok(Yaml.pretty(oas)).type(YAML).build();
		}
		return Response.ok(Json.pretty(oas)).type(MediaType.APPLICATION_JSON).build();
	}

	/** {@code application/yaml}, which {@link MediaType} has no constant for. */
	static final String YAML = "application/yaml";

	/**
	 * Builds the scanning context.
	 * <p>
	 * The context is cached per application instance by swagger, hence the identity in
	 * the id: two whiteboard applications in one framework must not share a descriptor.
	 */
	private OpenApiContext context() throws Exception {
		String ctxId = app.getClass().getCanonicalName() + "#" + System.identityHashCode(app);
		OpenApiContext ctx = new JaxrsOpenApiContextBuilder<>() //
				.servletConfig(config) //
				.application(app) //
				.openApiConfiguration(new SwaggerConfiguration().prettyPrint(true)) //
				.ctxId(ctxId) //
				.buildContext(true);
		// The default scanner looks for a configuration file or a package to scan; the
		// whiteboard already knows exactly which classes are in this application, so it
		// is handed the set directly.
		ctx.setOpenApiScanner(new OpenApiScanner() {

			@Override
			public void setConfiguration(io.swagger.v3.oas.integration.api.OpenAPIConfiguration cfg) {
				// Nothing to configure: classes() is the whole of this scanner.
			}

			@Override
			public Set<Class<?>> classes() {
				return app.getClasses();
			}

			@Override
			public Map<String, Object> resources() {
				return null;
			}
		});
		return ctx;
	}

	/**
	 * Adds what the JAX-RS annotations cannot say: what this API is, and the base
	 * clients should call it on.
	 * <p>
	 * <b>Must be idempotent.</b> Swagger caches the {@link OpenAPI} per context id, so
	 * {@code read()} returns the same instance on every request and this runs against an
	 * object that already carries the previous call's work. Assigning wholesale rather
	 * than appending is what keeps it so: an {@code addServersItem} here grew the
	 * {@code servers} list by one entry per request until it was caught.
	 */
	private void describe(OpenAPI oas) {
		oas.setInfo(new Info() //
				.title("DCAT.Atlas") //
				.version("1.0.0") //
				.description("""
						Read and admin API for a DCAT-AP.de catalog.

						Bodies are XMI of the DCAT-AP.de EMF model: writes accept `application/xmi` \
						only, while reads additionally negotiate RDF/XML, Turtle, N-Triples, JSON-LD \
						and N3. This descriptor is generated from the resources and so describes \
						routes and media types; identity rules, conditional requests and the error \
						taxonomy are in the user guide.""") //
				.license(new License().name("EPL-2.0").url("https://www.eclipse.org/legal/epl-2.0/")));

		PublicIris iris = publicIris;
		// The public base is where clients dereference resources, which is exactly what a
		// servers entry means — not the address this container happens to answer on. Set,
		// never added, for the reason in the javadoc; cleared when there is no public base
		// so a stale entry cannot outlive the service going away.
		oas.setServers(iris == null ? null
				: List.of(new Server().url(iris.publicBase()).description("Public base URL")));
	}
}

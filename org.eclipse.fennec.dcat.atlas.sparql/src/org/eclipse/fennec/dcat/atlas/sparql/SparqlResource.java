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
package org.eclipse.fennec.dcat.atlas.sparql;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.servlet.whiteboard.annotations.RequireHttpWhiteboard;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * SPARQL query endpoint over the in-memory projection (persistence plan P1-5,
 * WP-DCAT-5).
 * <p>
 * Read-only by construction: {@link QueryFactory} parses queries only, so a SPARQL
 * <em>Update</em> is rejected as a parse error rather than being able to mutate a
 * projection that is not the store of record.
 * <p>
 * Results are serialized here rather than through the DCAT message body writers:
 * those map <em>EMF model objects</em> to RDF, whereas a {@code CONSTRUCT} already
 * yields a Jena {@link Model}, and SPARQL result sets are not RDF at all.
 * {@code RDFDataMgr}/{@code ResultSetFormatter} cover every syntax directly.
 */
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("SparqlResource")
@Component(name = "SparqlResource", service = SparqlResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/sparql")
public class SparqlResource {

	static final String SPARQL_QUERY = "application/sparql-query";
	static final String FORM = MediaType.APPLICATION_FORM_URLENCODED;

	static final String RESULTS_JSON = "application/sparql-results+json";
	static final String RESULTS_XML = "application/sparql-results+xml";
	static final String CSV = "text/csv";
	static final String TSV = "text/tab-separated-values";

	static final String TURTLE = "text/turtle";
	static final String RDF_XML = "application/rdf+xml";
	static final String JSON_LD = "application/ld+json";
	static final String N_TRIPLES = "application/n-triples";
	static final String N3 = "text/n3";

	@Reference
	SparqlEngine engine;

	@GET
	@Produces({ RESULTS_JSON, RESULTS_XML, CSV, TSV, TURTLE, RDF_XML, JSON_LD, N_TRIPLES, N3 })
	public Response query(@QueryParam("query") String query, @Context HttpHeaders headers) {
		return run(query, headers);
	}

	/** The SPARQL 1.1 protocol's direct POST: the body <em>is</em> the query. */
	@POST
	@Consumes(SPARQL_QUERY)
	@Produces({ RESULTS_JSON, RESULTS_XML, CSV, TSV, TURTLE, RDF_XML, JSON_LD, N_TRIPLES, N3 })
	public Response queryByPost(String query, @Context HttpHeaders headers) {
		return run(query, headers);
	}

	/** The SPARQL 1.1 protocol's form-encoded POST, for long queries from browsers. */
	@POST
	@Consumes(FORM)
	@Produces({ RESULTS_JSON, RESULTS_XML, CSV, TSV, TURTLE, RDF_XML, JSON_LD, N_TRIPLES, N3 })
	public Response queryByForm(@FormParam("query") String query, @Context HttpHeaders headers) {
		return run(query, headers);
	}

	private Response run(String queryString, HttpHeaders headers) {
		if (!engine.isEnabled()) {
			return Response.status(Status.NOT_FOUND).type(MediaType.TEXT_PLAIN)
					.entity("SPARQL is not enabled on this deployment\n").build();
		}
		if (!engine.isReady()) {
			// 503, never an empty result set: an unbuilt projection answers successfully
			// with too few rows, which a client cannot tell from 'nothing matches'.
			return Response.status(Status.SERVICE_UNAVAILABLE).type(MediaType.TEXT_PLAIN)
					.entity("The RDF projection is still being built; retry shortly\n").build();
		}
		if (queryString == null || queryString.isBlank()) {
			return badRequest("No query given: use ?query=, or POST the query as " + SPARQL_QUERY);
		}

		Query query;
		try {
			query = QueryFactory.create(queryString);
		} catch (QueryException e) {
			// Also the path a SPARQL Update takes: this endpoint parses queries only.
			return badRequest("Not a valid SPARQL query (this endpoint is read-only): " + e.getMessage());
		}

		applyRowCap(query);
		try {
			if (query.isSelectType() || query.isAskType()) {
				return resultSet(query, headers);
			}
			return graph(query, headers);
		} catch (QueryException e) {
			// Includes the timeout: ARQ cancels the execution and throws.
			return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
					.entity("Query execution failed or timed out: " + e.getMessage() + "\n").build();
		}
	}

	/**
	 * Caps SELECT rows (G4/R4). A tighter limit already in the query wins, so the cap
	 * only ever narrows the result.
	 */
	private void applyRowCap(Query query) {
		long cap = engine.maxResultRows();
		if (cap <= 0 || !query.isSelectType()) {
			return;
		}
		if (!query.hasLimit() || query.getLimit() > cap) {
			query.setLimit(cap);
		}
	}

	private Response resultSet(Query query, HttpHeaders headers) {
		String type = negotiate(headers, List.of(RESULTS_JSON, RESULTS_XML, CSV, TSV), RESULTS_JSON);
		byte[] body = engine.execute(query, execution -> {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			if (query.isAskType()) {
				boolean answer = execution.execAsk();
				switch (type) {
				case RESULTS_XML -> ResultSetFormatter.outputAsXML(out, answer);
				case CSV -> ResultSetFormatter.outputAsCSV(out, answer);
				case TSV -> ResultSetFormatter.outputAsTSV(out, answer);
				default -> ResultSetFormatter.outputAsJSON(out, answer);
				}
			} else {
				switch (type) {
				case RESULTS_XML -> ResultSetFormatter.outputAsXML(out, execution.execSelect());
				case CSV -> ResultSetFormatter.outputAsCSV(out, execution.execSelect());
				case TSV -> ResultSetFormatter.outputAsTSV(out, execution.execSelect());
				default -> ResultSetFormatter.outputAsJSON(out, execution.execSelect());
				}
			}
			return out.toByteArray();
		});
		return Response.ok(body, type).build();
	}

	private Response graph(Query query, HttpHeaders headers) {
		String type = negotiate(headers, List.of(TURTLE, RDF_XML, JSON_LD, N_TRIPLES, N3), TURTLE);
		Model model = engine.execute(query,
				execution -> query.isDescribeType() ? execution.execDescribe() : execution.execConstruct());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFDataMgr.write(out, model, langFor(type));
		return Response.ok(out.toByteArray(), type).build();
	}

	/**
	 * Picks the first acceptable type this result kind can produce. JAX-RS has
	 * already matched {@code @Produces} across both kinds, so a client asking only
	 * for Turtle on a SELECT would otherwise get Turtle-labelled result rows.
	 */
	private static String negotiate(HttpHeaders headers, List<String> supported, String fallback) {
		for (MediaType accepted : headers.getAcceptableMediaTypes()) {
			for (String candidate : supported) {
				if (accepted.isCompatible(MediaType.valueOf(candidate)) && !accepted.isWildcardType()) {
					return candidate;
				}
			}
		}
		return fallback;
	}

	private static Lang langFor(String mediaType) {
		return switch (mediaType) {
		case RDF_XML -> Lang.RDFXML;
		case JSON_LD -> Lang.JSONLD;
		case N_TRIPLES -> Lang.NTRIPLES;
		case N3 -> Lang.N3;
		default -> Lang.TURTLE;
		};
	}

	private static Response badRequest(String message) {
		return Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity(message + "\n").build();
	}
}

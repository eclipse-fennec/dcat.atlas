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

import org.apache.jena.shacl.ValidationReport;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
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

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * Dry-run SHACL validation (FR-5): validate a submitted entity against the
 * DCAT-AP.de shapes <em>without</em> writing it, and return the SHACL report.
 * <p>
 * One POST per entity type so the existing per-type RDF/JSON/XML body readers
 * deserialize the payload. The response is always {@code 200} — a dry run reports
 * rather than rejects — carrying the native {@code sh:ValidationReport}, serialized in
 * whichever RDF syntax the client negotiates (Turtle/JSON-LD/RDF-XML/N3/N-Triples,
 * FR-19), with {@code X-SHACL-Conforms: true|false} for a quick programmatic check.
 * <p>
 * On-write enforcement (FR-4, {@code 422}) is a separate, config-gated concern on
 * the admin resources; this endpoint never blocks.
 */
@RequireCodecMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireHttpWhiteboard
@JakartarsResource
@JakartarsName("ValidationResource")
@Component(name = "ValidationResource", service = ValidationResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/admin/validate")
public class ValidationResource {

	static final String JSON = "application/json";
	static final String XML = "application/xml";
	static final String RDF_XML = "application/rdf+xml";
	static final String TURTLE = "text/turtle";
	static final String JSONLD = "application/ld+json";
	static final String N3 = "text/n3";
	static final String NTRIPLES = "application/n-triples";

	/**
	 * Dynamic/optional so a validation reconfigure (shapes or enforce-flag change) rebinds
	 * here without recycling this resource and reloading the whole JAX-RS whiteboard. The
	 * field can be momentarily {@code null} during a rebind (or if no validation service is
	 * installed), which {@link #report} answers with 503 rather than a 500/NPE.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@POST
	@Path("/catalogs")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ TURTLE, JSONLD, RDF_XML, N3, NTRIPLES })
	public Response validateCatalog(Catalog catalog) {
		return report(catalog);
	}

	@POST
	@Path("/datasets")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ TURTLE, JSONLD, RDF_XML, N3, NTRIPLES })
	public Response validateDataset(Dataset dataset) {
		return report(dataset);
	}

	@POST
	@Path("/dataset-series")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ TURTLE, JSONLD, RDF_XML, N3, NTRIPLES })
	public Response validateDatasetSeries(DatasetSeries series) {
		return report(series);
	}

	@POST
	@Path("/data-services")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ TURTLE, JSONLD, RDF_XML, N3, NTRIPLES })
	public Response validateDataService(DataService dataService) {
		return report(dataService);
	}

	@POST
	@Path("/distributions")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces({ TURTLE, JSONLD, RDF_XML, N3, NTRIPLES })
	public Response validateDistribution(Distribution distribution) {
		return report(distribution);
	}

	private Response report(EObject entity) {
		DcatValidationService validation = validationService;
		if (validation == null) {
			// Momentarily unbound (mid-reconfigure) or no validation service installed.
			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
		}
		ValidationReport result = validation.validate(entity);
		// The report entity is serialized by ValidationReportMessageBodyWriter in whichever
		// of the @Produces syntaxes the client negotiates; no explicit type here.
		return Response.ok(result) //
				.header("X-SHACL-Conforms", Boolean.toString(result.conforms())) //
				.build();
	}
}

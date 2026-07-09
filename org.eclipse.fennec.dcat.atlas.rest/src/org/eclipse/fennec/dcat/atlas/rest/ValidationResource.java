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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.codec.rest.annotations.RequireCodecMessageBodyReaderWriter;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.ValidationResult;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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
 * rather than rejects — carrying the native {@code sh:ValidationReport} as Turtle,
 * with {@code X-SHACL-Conforms: true|false} for a quick programmatic check.
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

	@Reference
	DcatValidationService validationService;

	@POST
	@Path("/catalogs")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces(TURTLE)
	public Response validateCatalog(Catalog catalog) {
		return report(catalog);
	}

	@POST
	@Path("/datasets")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces(TURTLE)
	public Response validateDataset(Dataset dataset) {
		return report(dataset);
	}

	@POST
	@Path("/dataset-series")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces(TURTLE)
	public Response validateDatasetSeries(DatasetSeries series) {
		return report(series);
	}

	@POST
	@Path("/data-services")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces(TURTLE)
	public Response validateDataService(DataService dataService) {
		return report(dataService);
	}

	@POST
	@Path("/distributions")
	@Consumes({ JSON, XML, RDF_XML })
	@Produces(TURTLE)
	public Response validateDistribution(Distribution distribution) {
		return report(distribution);
	}

	private Response report(EObject entity) {
		ValidationResult result = validationService.validate(entity);
		return Response.ok(result.reportTurtle()) //
				.type(TURTLE) //
				.header("X-SHACL-Conforms", Boolean.toString(result.conforms())) //
				.build();
	}
}

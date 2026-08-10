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
package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ValidationReport;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Serializes a Jena {@link ValidationReport} (the native, spec-compliant
 * {@code sh:ValidationReport}) in any of the supported RDF syntaxes (FR-19). Unlike the
 * DCAT-AP {@code EObject} writers this needs no EMF conversion — the report already is a
 * Jena {@link Model}, so we hand it straight to {@code RDFDataMgr} in the negotiated
 * syntax, preserving full fidelity (blank nodes, complex paths, literal values). It
 * therefore does not extend {@link AbstractRDFMessageBodyWriter} (which converts an
 * {@code EObject} to a model), but lives alongside it as the report's writer.
 * <p>
 * {@code application/json} (a plain, non-RDF JSON projection) is deliberately not offered
 * here; JSON-LD covers the JSON-shaped RDF case.
 */
@Provider
@JakartarsExtension
@Produces({ "text/turtle", "application/ld+json", "application/rdf+xml", "text/n3", "application/n-triples" })
@Component(name = "ValidationReportMessageBodyWriter", service = MessageBodyWriter.class)
public class ValidationReportMessageBodyWriter implements MessageBodyWriter<ValidationReport> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ValidationReport.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(ValidationReport report, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(ValidationReport report, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out) throws IOException {
		Model model = report.getModel();
		String syntax = mediaType.getType() + "/" + mediaType.getSubtype();
		switch (syntax) {
		case "application/ld+json" -> RDFDataMgr.write(out, model, RDFFormat.JSONLD11_PRETTY);
		case "application/rdf+xml" -> RDFDataMgr.write(out, model, RDFFormat.RDFXML_PRETTY);
		case "application/n-triples" -> RDFDataMgr.write(out, model, RDFFormat.NTRIPLES);
		case "text/n3" -> RDFDataMgr.write(out, model, Lang.N3);
		default -> RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
		}
		out.flush();
	}
}

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
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shacl.ValidationReport;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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

	/**
	 * Needed to render the report's identities public (#46), and mandatory for the same
	 * reason the read resources make it mandatory: without it this would serialize the
	 * store's internal base to a client, and a report nobody can correlate is worse than no
	 * report. The {@code public-iris} readiness check explains the absence.
	 */
	@Reference
	PublicIris publicIris;

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
		Model model = publicView(report.getModel());
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

	/**
	 * The report with every identity of ours rendered public (#46).
	 *
	 * <h2>Why this matters more here than in a text body</h2>
	 *
	 * The report is meant to be processed by machine. A consumer correlating
	 * {@code sh:focusNode} against its own records was handed
	 * {@code http://dcat.atlas/datasets/x} — an IRI matching nothing it sent and nothing it
	 * can fetch. Validation runs on the stored identity, which is correct; only the rendering
	 * was wrong.
	 *
	 * <h2>What is mapped</h2>
	 *
	 * URI nodes in any position, and the lexical form of a literal — a {@code sh:resultMessage}
	 * quotes the node it is about, so mapping only the nodes would leave the same IRI in the
	 * prose beside them. Blank nodes carry across untouched, which matters: a SHACL report is
	 * mostly blank nodes, and their identity is what ties a result to its report.
	 * <p>
	 * A new model rather than an edit in place: the report belongs to the exception that
	 * carries it, and may be logged or inspected after this.
	 */
	private Model publicView(Model model) {
		Model rendered = ModelFactory.createDefaultModel();
		rendered.setNsPrefixes(model.getNsPrefixMap());
		for (StmtIterator statements = model.listStatements(); statements.hasNext();) {
			Statement statement = statements.next();
			rendered.add(publicResource(rendered, statement.getSubject()), statement.getPredicate(),
					publicNode(rendered, statement.getObject()));
		}
		return rendered;
	}

	private Resource publicResource(Model into, Resource resource) {
		if (!resource.isURIResource()) {
			return resource;
		}
		String iri = resource.getURI();
		String mapped = publicIris.toPublic(iri);
		return iri.equals(mapped) ? resource : into.createResource(mapped);
	}

	private RDFNode publicNode(Model into, RDFNode node) {
		if (node.isResource()) {
			return publicResource(into, node.asResource());
		}
		if (node.isLiteral()) {
			String lexical = node.asLiteral().getLexicalForm();
			if (lexical.contains(DcatIds.LOGICAL_BASE)) {
				return into.createLiteral(lexical.replace(DcatIds.LOGICAL_BASE, publicIris.publicBase()),
						node.asLiteral().getLanguage());
			}
		}
		return node;
	}
}

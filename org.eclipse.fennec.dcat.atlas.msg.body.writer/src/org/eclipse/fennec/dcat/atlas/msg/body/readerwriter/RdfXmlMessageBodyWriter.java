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

import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Writes DCAT-AP model objects as RDF/XML ({@code application/rdf+xml}).
 * <p>
 * Now just another syntax on top of {@link EObjectToJena}: RDF/XML used to be
 * written straight through EMF's {@code XMLResource}, which is what forced the
 * model to carry RDF/XML syntax constructs. Jena writes it from the graph like
 * every other format, so no shape can be emitted that Jena would not accept.
 */
@Provider
@JakartarsExtension
@Produces("application/rdf+xml")
@Component(name = "RdfXmlMessageBodyWriter", service = MessageBodyWriter.class)
public class RdfXmlMessageBodyWriter extends AbstractRDFMessageBodyWriter {

	@Override
	protected void writeModel(Model model, OutputStream out) {
		RDFDataMgr.write(out, model, RDFFormat.RDFXML_PRETTY);
	}
}

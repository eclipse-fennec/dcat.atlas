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
 * Writes DCAT-AP model objects as RDF 1.1 Turtle ({@code text/turtle}).
 */
@Provider
@JakartarsExtension
@Produces("text/turtle")
@Component(name = "TurtleMessageBodyWriter", service = MessageBodyWriter.class)
public class TurtleMessageBodyWriter extends AbstractRDFMessageBodyWriter {

	@Override
	protected void writeModel(Model model, OutputStream out) {
		RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
	}
}

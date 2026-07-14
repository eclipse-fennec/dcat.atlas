package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import java.io.OutputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
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

	@Reference
	private ResourceSetFactory resourceSetFactory;

	@Override
	protected ResourceSetFactory resourceSetFactory() {
		return resourceSetFactory;
	}

	@Override
	protected void writeModel(Model model, OutputStream out) {
		RDFDataMgr.write(out, model, RDFFormat.TURTLE_PRETTY);
	}
}

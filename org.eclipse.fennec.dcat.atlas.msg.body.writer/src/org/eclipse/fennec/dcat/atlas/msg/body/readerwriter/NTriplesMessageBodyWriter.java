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
 * Writes DCAT-AP model objects as N-Triples ({@code application/n-triples}),
 * one of the RDF read-side formats mandated by DCAT-AP.de 3.0 (F-18).
 */
@Provider
@JakartarsExtension
@Produces("application/n-triples")
@Component(name = "NTriplesMessageBodyWriter", service = MessageBodyWriter.class)
public class NTriplesMessageBodyWriter extends AbstractRDFMessageBodyWriter {

	@Reference
	private ResourceSetFactory resourceSetFactory;

	@Override
	protected ResourceSetFactory resourceSetFactory() {
		return resourceSetFactory;
	}

	@Override
	protected void writeModel(Model model, OutputStream out) {
		RDFDataMgr.write(out, model, RDFFormat.NTRIPLES);
	}
}

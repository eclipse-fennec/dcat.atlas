package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Writes DCAT-AP model objects as RDF/XML ({@code application/rdf+xml}) straight
 * through EMF's {@code XMLResource} — the format the fennec codec does not cover.
 */
@Provider
@JakartarsExtension
@Produces("application/rdf+xml")
@Component(name = "RdfXmlMessageBodyWriter", service = MessageBodyWriter.class)
public class RdfXmlMessageBodyWriter implements MessageBodyWriter<Object> {

	@Reference
	private ResourceSetFactory resourceSetFactory;

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return AbstractRDFMessageBodyWriter.isSupported(type, genericType);
	}

	@Override
	public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		EObjectRDFModelBuilder.writeRdfXml(entity, resourceSetFactory.createResourceSet(), entityStream);
		entityStream.flush();
	}
}

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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

/**
 * Reads an {@code application/rdf+xml} body into a DCAT-AP model object via EMF's
 * {@code XMLResource}. Counterpart of {@link RdfXmlMessageBodyWriter}; together
 * they give the REST layer full RDF/XML support without the fennec codec.
 */
@Provider
@JakartarsExtension
@Consumes("application/rdf+xml")
@Component(name = "RdfXmlMessageBodyReader", service = MessageBodyReader.class)
public class RdfXmlMessageBodyReader implements MessageBodyReader<EObject> {

	@Reference
	private ResourceSetFactory resourceSetFactory;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EObject.class.isAssignableFrom(type);
	}

	@Override
	public EObject readFrom(Class<EObject> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		List<EObject> parsed = EObjectRDFModelBuilder.parse(entityStream, resourceSetFactory.createResourceSet());
		return parsed.stream() //
				.filter(type::isInstance) //
				.findFirst() //
				.orElseThrow(() -> new BadRequestException(
						"RDF/XML body contained no " + type.getSimpleName()));
	}
}

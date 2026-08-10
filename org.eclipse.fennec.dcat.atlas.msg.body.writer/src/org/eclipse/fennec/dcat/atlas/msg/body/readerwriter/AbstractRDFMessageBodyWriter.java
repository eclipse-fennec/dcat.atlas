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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Base for {@link MessageBodyWriter}s that render DCAT-AP EMF model objects as
 * RDF. The common part (accepting {@link EObject} / {@code Collection<EObject>}
 * entities and turning them into a Jena {@link Model}) lives here; concrete
 * subclasses only declare the media type they {@code @Produces} and how the
 * model is written in their syntax.
 *
 * @param <T> handled entity type ({@link EObject} or a collection thereof)
 */
public abstract class AbstractRDFMessageBodyWriter implements MessageBodyWriter<Object> {

	/**
	 * @return a resource set factory able to create resource sets that know the
	 *         DCAT-AP packages and resource factory. Supplied by the concrete
	 *         component so the OSGi {@code @Reference} is unambiguous.
	 */
	protected abstract ResourceSetFactory resourceSetFactory();

	/**
	 * Serializes the given graph in the concrete RDF syntax.
	 */
	protected abstract void writeModel(Model model, OutputStream out);

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return isSupported(type, genericType);
	}

	@Override
	public void writeTo(Object entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		// A fresh resource set per request: ResourceSet is not thread safe.
		Model model = EObjectRDFModelBuilder.toModel(entity, resourceSetFactory().createResourceSet());
		writeModel(model, entityStream);
		entityStream.flush();
	}

	/**
	 * Accepts a single {@link EObject} or a {@link Collection} whose element type
	 * is (statically) an {@link EObject}.
	 */
	static boolean isSupported(Class<?> type, Type genericType) {
		if (EObject.class.isAssignableFrom(type)) {
			return true;
		}
		if (Collection.class.isAssignableFrom(type) && genericType instanceof ParameterizedType parameterized) {
			Type[] arguments = parameterized.getActualTypeArguments();
			if (arguments.length == 1 && arguments[0] instanceof Class<?> elementType) {
				return EObject.class.isAssignableFrom(elementType);
			}
		}
		return false;
	}
}

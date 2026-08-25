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
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.emf.ecore.EObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import rdf.IdentifiedResource;

/**
 * Serves a DCAT-AP resource as {@code text/html} (N25), so that following a minted
 * {@code about} IRI in a browser yields a page instead of a {@code 406}, and a crawler
 * finds schema.org in it.
 * <p>
 * The page is the same URI as every other representation, negotiated by
 * {@code Accept}: no {@code 303} to a separate HTML URI, so harvesters see one URI
 * space. {@code DcatConditionalFilter} already stamps the state-based ETag and
 * {@code Vary: Accept}, and because that validator is shared across representations a
 * conditional {@code GET} for HTML behaves exactly as one for Turtle.
 *
 * <h2>Single entities only</h2>
 *
 * Only the per-resource {@code GET}s advertise {@code text/html}; a collection stays
 * {@code 406} for it, which is the scope agreed on the issue. Note what would happen if
 * that changed without more thought: {@code PublicIriFilter} treats HTML as a
 * non-RDF format and so wraps a collection in a {@code utilities.Response} before any
 * writer runs, and this writer would then dutifully render the *wrapper's* graph. A
 * collection index therefore needs work in that filter too, not just a wider
 * {@code @Produces}.
 *
 * @see RdfHtmlRenderer for what the page contains and why it is generated from the graph
 */
@Provider
@JakartarsExtension
@Produces("text/html")
@Component(name = "HtmlMessageBodyWriter", service = MessageBodyWriter.class)
public class HtmlMessageBodyWriter implements MessageBodyWriter<EObject> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return EObject.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(EObject entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
			throws IOException, WebApplicationException {
		Model model = EObjectToJena.toModel(entity);
		String html = RdfHtmlRenderer.render(model, subjectOf(model, entity));
		// @Produces cannot carry the charset without making it part of the negotiated type,
		// and DCAT-AP.de content is full of umlauts, so it is stated on the response instead.
		httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML + ";charset=UTF-8");
		entityStream.write(html.getBytes(StandardCharsets.UTF_8));
		entityStream.flush();
	}

	@Override
	public long getSize(EObject entity, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	/**
	 * The node the page is about.
	 * <p>
	 * Normally that is the entity's {@code rdf:about}, which every stored resource has.
	 * Falling back on "the node nothing points at" covers an entity built in memory
	 * without one — {@link EObjectToJena} then makes it a blank node, and the page still
	 * has to render rather than 500. Both fallbacks pick deterministically, so the same
	 * graph always produces the same page.
	 */
	private static Resource subjectOf(Model model, EObject entity) {
		if (entity instanceof IdentifiedResource identified) {
			String about = identified.getAbout();
			if (about != null && !about.isBlank()) {
				return model.getResource(about);
			}
		}
		Comparator<Resource> stable = Comparator.comparing(Resource::toString);
		List<Resource> subjects = model.listSubjects().toList();
		return subjects.stream() //
				.filter(subject -> !model.contains(null, null, subject)) //
				.min(stable) //
				.or(() -> subjects.stream().min(stable)) //
				.orElseGet(model::createResource);
	}
}

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
package org.eclipse.fennec.dcat.atlas.rest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicView;
import org.eclipse.fennec.model.utilities.UtilitiesFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.ReaderInterceptorContext;

/**
 * Renders stored identities under the public base on the way out (D1/D2).
 * <p>
 * Entities are stored under {@code http://dcat.atlas/…} so that nothing about the
 * writing host is recorded in them. Clients must still receive IRIs they can
 * dereference, so every response entity is swapped for a public-facing copy before
 * any message-body writer runs — which is what makes this work for the XMI, JSON
 * and RDF representations alike without any of them knowing about it.
 *
 * <h2>Why a filter rather than a call in each resource</h2>
 *
 * There are ten resources with many return points, and a single missed one would
 * publish an internal identity that a harvester might then treat as canonical.
 * Here it cannot be forgotten, including by endpoints added later.
 *
 * <h2>Why the resources require {@link PublicIris} too</h2>
 *
 * Each of the ten collection resources holds a mandatory {@code @Reference PublicIris}.
 * The five admin ones call it, to build the {@code Location} of a create; the five
 * read-only ones hold it purely to gate registration. That gate is not redundant with
 * this class: a filter is a JAX-RS <em>extension</em>, so when {@code PublicIris} is
 * absent the whiteboard simply unregisters the filter and leaves every resource
 * serving. Measured in a container started without {@code PUBLIC_BASE_URL}:
 * {@code POST /admin/catalogs} answered <b>201</b>, with no logical-to-public mapping
 * applied in either direction — responses would carry raw {@code http://dcat.atlas/…}
 * identities, and an inbound {@code about} under the public base would no longer fold
 * back and so read as foreign.
 * <p>
 * Requiring the service in the resources turns that into an unregistered endpoint: a
 * misconfigured portal answers 404 and publishes nothing, instead of quietly
 * publishing identities nobody can dereference. The 404 is explained by the
 * {@code public-iris} readiness check rather than left to be guessed at.
 *
 * @see PublicView
 */
@Component
@JakartarsExtension
@JakartarsName("DcatPublicIriFilter")
public class PublicIriFilter implements ContainerResponseFilter, ReaderInterceptor {

	/**
	 * The syntaxes served by {@code …msg.body.readerwriter}'s RDF writers, which take a
	 * collection as it is. Everything else the read resources negotiate goes through the
	 * codec, which needs a single root object.
	 */
	private static final Set<String> RDF_MEDIA_TYPES = Set.of("application/rdf+xml", "text/turtle",
			"application/n-triples", "application/ld+json", "text/n3");

	private final PublicIris publicIris;

	@org.osgi.service.component.annotations.Activate
	public PublicIriFilter(@Reference PublicIris publicIris) {
		this.publicIris = publicIris;
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		Object entity = responseContext.getEntity();
		Object rendered;
		if (entity instanceof EObject eObject) {
			// A rendered EObject keeps its class, so the writers still recognise it.
			rendered = PublicView.render(eObject, publicIris);
		} else if (entity instanceof Collection<?> collection) {
			rendered = renderCollection(renderAll(collection), responseContext);
		} else if (entity instanceof String text) {
			rendered = publicise(text);
		} else {
			return;
		}
		responseContext.setEntity(rendered, responseContext.getEntityAnnotations(), responseContext.getMediaType());
	}

	/**
	 * Rewrites the store's logical base out of a text body (#46).
	 *
	 * <h2>Why text bodies belong here too</h2>
	 *
	 * A refusal names the resource it is about, and until this existed it named it by the
	 * stored identity: a {@code 422} said {@code http://dcat.atlas/datasets/x}, which the
	 * client never sent, cannot fetch, and cannot match against its own records. Validation
	 * runs on the logical form and should — the stored identity is what is being validated —
	 * so the mapping belongs at rendering, which is here.
	 * <p>
	 * The reasoning is {@code CascadeReport}'s, which already maps its own {@code text/plain}
	 * body with {@code publicIris::toPublic}: a client's cache is keyed by the URL it was
	 * served. Doing it in the filter rather than in each of the seven
	 * {@code ExceptionMapper}s is the same argument this class's own javadoc makes about the
	 * ten resources — here it cannot be forgotten, including by a mapper added later.
	 *
	 * <h2>Why a base replacement is safe</h2>
	 *
	 * Only the logical base is rewritten, and an occurrence of it in an <em>outbound</em> body
	 * is internal by definition. In particular a foreign {@code about} quoted back by a
	 * {@code 400} is untouched: it is not under our base, which is exactly why it was refused.
	 */
	private String publicise(String text) {
		String logicalBase = DcatIds.LOGICAL_BASE;
		return text.contains(logicalBase) ? text.replace(logicalBase, publicIris.publicBase()) : text;
	}

	/**
	 * Folds a request body's identities back to the stored form, so a client that
	 * refers to our resources by the IRIs we served it does not write the public host
	 * into data we keep host-free.
	 */
	@Override
	public Object aroundReadFrom(ReaderInterceptorContext context) throws java.io.IOException {
		Object entity = context.proceed();
		return entity instanceof EObject eObject ? PublicView.fold(eObject, publicIris) : entity;
	}

	/**
	 * Shapes a rendered collection into something the negotiated format can actually
	 * write, because the two families of writer want opposite things.
	 * <p>
	 * <b>RDF</b> takes the collection as it is: a graph of many subjects is still one
	 * graph. The writers select on the <em>generic</em> type though
	 * ({@code AbstractRDFMessageBodyWriter.isSupported} wants a {@code ParameterizedType}
	 * whose element is an {@code EObject}), so the declared {@code List<Catalog>} has to
	 * be re-attached — handing over a bare {@code ArrayList} makes every one of them
	 * decline, which is a 500 on the endpoint rather than a missing writer.
	 * <p>
	 * <b>XMI/JSON/XML</b> cannot take a collection at all: the codec's writer wants a
	 * single {@code EObject} ({@code EObject.class.isAssignableFrom(type)}), and no list
	 * ever satisfies that. So the members go into a {@code utilities.Response}, the
	 * wrapper the fennec model provides for exactly this — the same pattern
	 * {@code model.atlas}'s {@code JpaDataResource} uses. Wrapping only here keeps the RDF
	 * representations byte-identical to what they have always been: no wrapper subject
	 * appears in a harvester's graph.
	 * <p>
	 * {@code Response.data} is a <em>containment</em> reference, so adding stored objects
	 * to it would pull them out of their store resources. Only the detached copies
	 * {@link PublicView#render} produces ever reach it.
	 */
	private Object renderCollection(List<Object> members, ContainerResponseContext responseContext) {
		if (isRdf(responseContext.getMediaType())) {
			return new GenericEntity<>(members, responseContext.getEntityType());
		}
		org.eclipse.fennec.model.utilities.Response wrapper = UtilitiesFactory.eINSTANCE.createResponse();
		for (Object member : members) {
			if (member instanceof EObject eObject) {
				wrapper.getData().add(eObject);
			}
		}
		wrapper.setResultSize(wrapper.getData().size());
		return wrapper;
	}

	/** Whether {@code mediaType} is one of the RDF syntaxes, which write a collection directly. */
	private static boolean isRdf(MediaType mediaType) {
		if (mediaType == null) {
			return false;
		}
		String type = mediaType.getType() + "/" + mediaType.getSubtype();
		return RDF_MEDIA_TYPES.contains(type);
	}

	/**
	 * Renders a collection response. A non-{@code EObject} element is passed through
	 * rather than dropped — this filter has no business deciding what a resource may
	 * return.
	 */
	private List<Object> renderAll(Collection<?> collection) {
		List<Object> rendered = new ArrayList<>(collection.size());
		for (Object element : collection) {
			rendered.add(element instanceof EObject eObject ? PublicView.render(eObject, publicIris) : element);
		}
		return rendered;
	}
}

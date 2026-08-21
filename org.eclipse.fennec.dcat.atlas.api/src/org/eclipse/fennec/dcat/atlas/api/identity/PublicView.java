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
package org.eclipse.fennec.dcat.atlas.api.identity;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

import rdf.IdentifiedResource;

/**
 * Turns a stored entity into the form clients see: the same content, with every
 * identity of ours rendered under the public base.
 *
 * <h2>Why rendering is not storage</h2>
 *
 * Stored entities carry logical identities and link to each other as EMF
 * cross-resource references, so the store resolves them for real. A response has
 * no store behind it — the client cannot resolve our hrefs against our
 * filesystem — so what it needs is not a reference but an <em>address</em>.
 * <p>
 * This produces a detached copy in which each link is a proxy carrying the target's
 * public IRI. That covers every serializer at once without any of them knowing:
 * EMF's XMI writer takes an href from {@code eProxyURI()} before it considers
 * resource URIs, and {@code EObjectToJena} reads {@code about} — so both are set to
 * the same public IRI and both come out right.
 *
 * <h2>What is not rebased</h2>
 *
 * Only identities under a base we own. A publisher IRI, an EU vocabulary term or a
 * licence URI passes through untouched, because {@code PublicIris} decides
 * ownership structurally rather than from a list someone has to maintain.
 */
public final class PublicView {

	private PublicView() {
	}

	/**
	 * A detached, public-facing copy of {@code entity}. The stored object is left
	 * untouched — it belongs to the store, and mutating it to render it would write
	 * the public host back into the data we deliberately keep host-free.
	 */
	public static <T extends EObject> T render(T entity, PublicIris publicIris) {
		if (entity == null) {
			return null;
		}
		EcoreUtil.Copier copier = new EcoreUtil.Copier(false);
		@SuppressWarnings("unchecked")
		T copy = (T) copier.copy(entity);
		copier.copyReferences();

		for (EObject object : withContents(copy)) {
			rebaseIdentity(object, publicIris);
			rebaseLinks(object, publicIris);
		}
		return copy;
	}

	/** As {@link #render}, for a collection response. */
	public static <T extends EObject> List<T> renderAll(List<T> entities, PublicIris publicIris) {
		return entities.stream().map(entity -> render(entity, publicIris)).toList();
	}

	/**
	 * The inverse: folds a client-supplied entity's identities back to the stored
	 * form, <em>in place</em>.
	 * <p>
	 * A client refers to our resources by the public IRIs we served it, so a Catalog
	 * arriving with {@code href="https://public-host/…/datasets/air#/"} means one of
	 * ours. Storing that verbatim would put the public host back into data we keep
	 * host-free — the original problem arriving from outside instead of from
	 * {@code UriInfo}. Foreign IRIs are left alone, so a publisher or vocabulary
	 * reference survives untouched.
	 * <p>
	 * In place rather than copied: this runs on an object just deserialized from the
	 * request body, which nothing else holds a reference to.
	 */
	public static <T extends EObject> T fold(T entity, PublicIris publicIris) {
		if (entity == null) {
			return null;
		}
		for (EObject object : withContents(entity)) {
			if (object instanceof IdentifiedResource identified && identified.getAbout() != null) {
				identified.setAbout(publicIris.toLogical(identified.getAbout()));
			}
			foldLinks(object, publicIris);
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	private static void foldLinks(EObject object, PublicIris publicIris) {
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (reference.isContainment() || reference.isDerived() || !object.eIsSet(reference)) {
				continue;
			}
			if (reference.isMany()) {
				EList<EObject> values = (EList<EObject>) object.eGet(reference, false);
				for (Iterator<EObject> it = basicIterator(values); it.hasNext();) {
					foldLink(it.next(), publicIris);
				}
			} else {
				foldLink((EObject) object.eGet(reference, false), publicIris);
			}
		}
	}

	/**
	 * Re-points one link at the stored identity. The proxy URI is what the XMI reader
	 * populated and what the store will write back out, so it is the thing that has to
	 * change; {@code about} is kept in step for the RDF converter.
	 * <p>
	 * The proxy URI is set whether or not the link arrived as a proxy, which is what makes
	 * an <em>inline</em> member work. A client may write the member out in full instead of
	 * as an {@code href}; under a non-containment reference XMI has no way to express that,
	 * so EMF falls back to a same-document IDREF (the {@code about} doubles as the XMI
	 * {@code iD}) — an IDREF naming an object that is not in that file, which resolves to
	 * nothing on the next read and loses the membership silently. An inline object bearing
	 * an identity of ours <em>is</em> a reference to it, so it becomes one here, and its
	 * inline content is dropped rather than written into somebody else's resource.
	 * Anything not under a base we own is left exactly as it came.
	 */
	private static void foldLink(EObject link, PublicIris publicIris) {
		String iri = targetIri(link);
		if (iri == null || !publicIris.isOwned(iri)) {
			return;
		}
		String logical = publicIris.toLogical(iri);
		if (link instanceof IdentifiedResource identified) {
			identified.setAbout(logical);
		}
		((InternalEObject) link).eSetProxyURI(URI.createURI(logical).appendFragment("/"));
	}

	private static void rebaseIdentity(EObject object, PublicIris publicIris) {
		if (object instanceof IdentifiedResource identified && identified.getAbout() != null) {
			identified.setAbout(publicIris.toPublic(identified.getAbout()));
		}
	}

	/**
	 * Replaces each link with a stub addressed at the target's public IRI.
	 * <p>
	 * The stub is a proxy so XMI writes {@code href}, and carries {@code about} so
	 * the RDF converter has a subject — the two serializers read different things,
	 * and a stub that satisfied only one would fail silently in the other.
	 */
	@SuppressWarnings("unchecked")
	private static void rebaseLinks(EObject object, PublicIris publicIris) {
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (reference.isContainment() || reference.isDerived() || !object.eIsSet(reference)) {
				continue;
			}
			if (reference.isMany()) {
				EList<EObject> values = (EList<EObject>) object.eGet(reference, false);
				List<EObject> stubs = stubsFor(values, publicIris);
				values.clear();
				values.addAll(stubs);
			} else {
				EObject value = (EObject) object.eGet(reference, false);
				EObject stub = stubFor(value, publicIris);
				if (stub != null) {
					object.eSet(reference, stub);
				}
			}
		}
	}

	private static List<EObject> stubsFor(EList<EObject> values, PublicIris publicIris) {
		List<EObject> stubs = new java.util.ArrayList<>(values.size());
		for (Iterator<EObject> it = basicIterator(values); it.hasNext();) {
			EObject stub = stubFor(it.next(), publicIris);
			if (stub != null) {
				stubs.add(stub);
			}
		}
		return stubs;
	}

	/**
	 * A stub standing for {@code target}, or {@code null} if it names nothing — which
	 * happens when a referenced resource has been deleted. Dropping it is right for a
	 * response: advertising a link a client cannot follow is worse than omitting it,
	 * and FR-1 stops this arising in the first place.
	 */
	private static EObject stubFor(EObject target, PublicIris publicIris) {
		String iri = targetIri(target);
		if (iri == null || iri.isBlank()) {
			return null;
		}
		String publicIri = publicIris.toPublic(iri);
		EObject stub = EcoreUtil.create(target.eClass());
		if (stub instanceof IdentifiedResource identified) {
			identified.setAbout(publicIri);
		}
		// The fragment names the sole root object of the target's document, matching how
		// the store writes it; without it EMF has an href with nothing to point at.
		((InternalEObject) stub).eSetProxyURI(URI.createURI(publicIri).appendFragment("/"));
		return stub;
	}

	/**
	 * The logical IRI a link points at, whether or not it has been resolved.
	 * <p>
	 * {@code about} first, when the object carries one: it is the identity the object
	 * claims, and for an <em>inline</em> member — not a proxy, and in no resource —
	 * {@link EcoreUtil#getURI} has nothing to report but a bare fragment. For a stored
	 * object the two agree. Falling back to {@link EcoreUtil#getURI} covers the
	 * unresolved proxy, whose {@code about} is {@code null} and whose destination is
	 * recorded in the proxy URI, so nothing is loaded just to find out where a link goes.
	 * The {@code #/} fragment is storage plumbing and is trimmed.
	 */
	private static String targetIri(EObject target) {
		if (target == null) {
			return null;
		}
		if (target instanceof IdentifiedResource identified && identified.getAbout() != null
				&& !identified.getAbout().isBlank()) {
			return identified.getAbout();
		}
		URI uri = EcoreUtil.getURI(target);
		return uri == null ? null : uri.trimFragment().toString();
	}

	private static List<EObject> withContents(EObject root) {
		List<EObject> all = new java.util.ArrayList<>();
		all.add(root);
		for (Iterator<EObject> it = EcoreUtil.getAllProperContents(root, false); it.hasNext();) {
			all.add(it.next());
		}
		return all;
	}

	private static Iterator<EObject> basicIterator(EList<EObject> values) {
		return values instanceof InternalEList ? ((InternalEList<EObject>) values).basicIterator() : values.iterator();
	}
}

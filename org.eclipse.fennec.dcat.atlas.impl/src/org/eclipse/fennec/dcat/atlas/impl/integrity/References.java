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
package org.eclipse.fennec.dcat.atlas.impl.integrity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.fennec.dcat.atlas.api.integrity.DanglingReferenceException;
import org.eclipse.fennec.dcat.atlas.api.integrity.ResourceInUseException;
import org.eclipse.fennec.dcat.atlas.impl.store.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

/**
 * Reverse-reference handling for deletes (FR-1).
 *
 * <h2>Why this exists</h2>
 *
 * Deleting a resource that something still links to does not fail loudly. The
 * referrer keeps its link, EMF loads it as an unresolved proxy, and the target
 * reports {@code about == null} — at which point serializing <em>the referrer</em>
 * to RDF throws, turning one delete into a 500 on an unrelated resource. So a
 * delete either refuses (409) or unlinks first (cascade).
 *
 * <h2>Why it is reflective</h2>
 *
 * The scan walks every non-containment {@link EReference} rather than naming
 * {@code dcat:dataset}, {@code dcat:service} and friends one at a time. There are
 * seven such references today across five classes, some of them nested inside
 * contained objects ({@code Distribution.accessService} lives under a Dataset,
 * {@code CatalogRecord.primaryTopic} under a Catalog) — and a hand-written list
 * would silently stop covering the model the first time one is added.
 */
public final class References {

	private References() {
	}

	/** One link, identified by the stored root that carries it. */
	private record Referrer(String collection, String id) {

		String iri() {
			return StoreLayout.logicalIri(collection, id);
		}
	}

	/**
	 * Clears the way to delete {@code collection}/{@code id}.
	 * <p>
	 * The returned identities are the referrers this call rewrote, and they are the same
	 * list the refusal carries on the other branch — the scan happens once and serves both
	 * outcomes. A caller reports them so that whoever asked for the cascade can invalidate
	 * the ETags it just invalidated on their behalf; every one of those resources moved.
	 *
	 * @param cascade when {@code true}, unlink from every referrer; when
	 *                {@code false}, refuse the delete if there is one
	 * @return the logical IRIs of the resources that were unlinked, in the order found;
	 *         empty when nothing referenced the target, which is also the only case where
	 *         {@code cascade} makes no difference
	 * @throws ResourceInUseException if something still references the target and
	 *                                {@code cascade} is {@code false}
	 */
	public static List<String> detach(Store store, String collection, String id, boolean cascade) {
		String targetIri = StoreLayout.logicalIri(collection, id);
		List<Referrer> referrers = referrersTo(store, targetIri);
		if (referrers.isEmpty()) {
			return List.of();
		}
		List<String> iris = referrers.stream().map(Referrer::iri).toList();
		if (!cascade) {
			throw new ResourceInUseException(
					"Cannot delete %s: still referenced by %d resource(s)".formatted(targetIri, referrers.size()),
					iris);
		}
		for (Referrer referrer : referrers) {
			store.<EObject>get(referrer.collection(), referrer.id())
					.ifPresent(owner -> unlinkAll(owner, targetIri, store));
		}
		return iris;
	}

	/**
	 * Refuses a write that would store a link to an identity of ours that is not there —
	 * {@link #detach}'s mirror image, and the same invariant from the other side: no stored
	 * link resolves to nothing.
	 * <p>
	 * Only identities under one of our collection bases are checked. A reference to
	 * somebody else's IRI — a publisher, a licence, a vocabulary concept — is not ours to
	 * resolve, and an IRI under our base that names no collection is left alone rather than
	 * guessed at.
	 *
	 * @throws DanglingReferenceException if any link names a missing identity of ours
	 */
	public static void requireResolvable(Store store, EObject entity) {
		Set<String> missing = new LinkedHashSet<>();
		for (EObject object : withContents(entity)) {
			for (EReference reference : nonContainmentRefs(object)) {
				for (String iri : targets(object, reference)) {
					if (iri != null && isMissingIdentityOfOurs(store, iri)) {
						missing.add(iri);
					}
				}
			}
		}
		if (!missing.isEmpty()) {
			throw new DanglingReferenceException(
					"Cannot store a reference to %d identity/identities that do not exist: %s"
							.formatted(missing.size(), String.join(", ", missing)),
					List.copyOf(missing));
		}
	}

	/**
	 * The stored resources {@code entity} points at, for use as SHACL context.
	 *
	 * <h2>Why the shapes need these</h2>
	 *
	 * A submitted entity is validated as a graph on its own, so {@code dcat:inSeries} or
	 * {@code dcat:accessService} arrives as a bare IRI. DCAT-AP.de says such a reference
	 * MUSS point at a node of a given class, which no graph lacking the target's type can
	 * satisfy — so without this a resource that had been linked could not be re-submitted
	 * as it was served. Only the {@code rdf:type} of what is returned here is used; see
	 * {@code EObjectToJena.typeGraph}.
	 *
	 * <h2>What is left out</h2>
	 *
	 * Only identities under one of our collection bases, exactly as
	 * {@link #requireResolvable}: somebody else's publisher, licence or vocabulary concept
	 * is not ours to type, and asserting a type for it would be inventing data. A
	 * reference that does not resolve is skipped rather than raised — {@code requireResolvable}
	 * runs first and is what reports that, with a better message.
	 *
	 * @return the referenced resources, each once, in encounter order
	 */
	public static List<EObject> referenced(Store store, EObject entity) {
		Map<String, EObject> resolved = new LinkedHashMap<>();
		for (EObject object : withContents(entity)) {
			for (EReference reference : nonContainmentRefs(object)) {
				for (String iri : targets(object, reference)) {
					if (iri != null && !resolved.containsKey(iri)) {
						ourResource(store, iri).ifPresent(target -> resolved.put(iri, target));
					}
				}
			}
		}
		return List.copyOf(resolved.values());
	}

	/** The stored resource {@code iri} names, if it is one of ours and it is there. */
	private static Optional<EObject> ourResource(Store store, String iri) {
		for (String collection : StoreLayout.COLLECTIONS) {
			String id = StoreLayout.idOf(collection, iri);
			if (id != null) {
				return store.get(collection, id);
			}
		}
		return Optional.empty();
	}

	/** True when {@code iri} names a resource in one of our collections and it is absent. */
	private static boolean isMissingIdentityOfOurs(Store store, String iri) {
		for (String collection : StoreLayout.COLLECTIONS) {
			String id = StoreLayout.idOf(collection, iri);
			if (id != null) {
				return store.get(collection, id).isEmpty();
			}
		}
		return false;
	}

	/** The stored roots that link to {@code targetIri}, each reported once. */
	private static List<Referrer> referrersTo(Store store, String targetIri) {
		List<Referrer> referrers = new ArrayList<>();
		for (String collection : StoreLayout.COLLECTIONS) {
			// Through the session, so a scan sees what the operation running it has already
			// staged - a cascade unlinks referrers one at a time and must not re-find them.
			for (String id : store.ids(collection)) {
				if (targetIri.equals(StoreLayout.logicalIri(collection, id))) {
					// A resource does not hold itself up.
					continue;
				}
				store.<EObject>get(collection, id) //
						.filter(owner -> linksTo(owner, targetIri)) //
						.ifPresent(owner -> referrers.add(new Referrer(collection, id)));
			}
		}
		return referrers;
	}

	private static boolean linksTo(EObject owner, String targetIri) {
		for (EObject object : withContents(owner)) {
			for (EReference reference : nonContainmentRefs(object)) {
				if (targets(object, reference).contains(targetIri)) {
					return true;
				}
			}
		}
		return false;
	}

	/** Removes every link to {@code targetIri} from {@code owner}, then saves it. */
	private static void unlinkAll(EObject owner, String targetIri, Store store) {
		boolean changed = false;
		for (EObject object : withContents(owner)) {
			for (EReference reference : nonContainmentRefs(object)) {
				changed |= unlink(object, reference, targetIri);
			}
		}
		if (changed) {
			store.save(owner);
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean unlink(EObject object, EReference reference, String targetIri) {
		if (reference.isMany()) {
			// By index, because the non-resolving iterator is read-only. Removing by index
			// resolves nothing either, so this stays free of file reads.
			EList<EObject> values = (EList<EObject>) object.eGet(reference, false);
			int index = 0;
			for (Iterator<EObject> it = basicIterator(values); it.hasNext(); index++) {
				if (targetIri.equals(Members.targetIri(it.next()))) {
					values.remove(index);
					return true;
				}
			}
			return false;
		}
		EObject value = (EObject) object.eGet(reference, false);
		if (value != null && targetIri.equals(Members.targetIri(value))) {
			object.eUnset(reference);
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private static Set<String> targets(EObject object, EReference reference) {
		Set<String> iris = new LinkedHashSet<>();
		if (reference.isMany()) {
			for (Iterator<EObject> it = basicIterator((EList<EObject>) object.eGet(reference, false)); it.hasNext();) {
				iris.add(Members.targetIri(it.next()));
			}
		} else {
			EObject value = (EObject) object.eGet(reference, false);
			if (value != null) {
				iris.add(Members.targetIri(value));
			}
		}
		return iris;
	}

	/** {@code owner} and everything contained in it, since links nest. */
	private static List<EObject> withContents(EObject owner) {
		List<EObject> all = new ArrayList<>();
		all.add(owner);
		for (Iterator<EObject> it = EcoreUtil.getAllProperContents(owner, false); it.hasNext();) {
			all.add(it.next());
		}
		return all;
	}

	/** Set, non-containment, non-derived references — the ones that can dangle. */
	private static List<EReference> nonContainmentRefs(EObject object) {
		List<EReference> references = new ArrayList<>();
		for (EReference reference : object.eClass().getEAllReferences()) {
			if (!reference.isContainment() && !reference.isDerived() && object.eIsSet(reference)) {
				references.add(reference);
			}
		}
		return references;
	}

	private static Iterator<EObject> basicIterator(EList<EObject> values) {
		return values instanceof InternalEList ? ((InternalEList<EObject>) values).basicIterator() : values.iterator();
	}
}

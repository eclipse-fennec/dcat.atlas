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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * Membership over EMF cross-resource references, read <em>without resolving</em>
 * them.
 *
 * <h2>Why resolution is avoided</h2>
 *
 * Since the model gained {@code resolveProxies="true"}, touching a membership list
 * the ordinary way resolves each entry — a file read per member. Answering "is this
 * dataset already in this catalog?" or "drop the link to this id" needs only the
 * IRI, which {@link EcoreUtil#getURI} yields from the proxy itself. So these walk
 * the list with {@link InternalEList#basicIterator()}, which hands back entries as
 * they are.
 * <p>
 * That also makes the operations robust where resolution is not: a member whose
 * file has been deleted resolves to nothing and reports {@code about == null}, but
 * its proxy URI still says exactly which id it referred to — so it can still be
 * found and unlinked.
 */
public final class Members {

	private Members() {
	}

	/**
	 * The logical IRI a membership entry points at, whether or not it has been
	 * resolved. The fragment ({@code #/}, naming the file's root object) is storage
	 * plumbing and is trimmed off.
	 */
	public static String targetIri(EObject member) {
		if (member == null) {
			return null;
		}
		org.eclipse.emf.common.util.URI uri = EcoreUtil.getURI(member);
		return uri == null ? null : uri.trimFragment().toString();
	}

	/** The id a membership entry names within {@code collection}, or {@code null}. */
	public static String targetId(String collection, EObject member) {
		return StoreLayout.idOf(collection, targetIri(member));
	}

	/** Whether {@code members} already contains {@code id}; membership is idempotent. */
	public static boolean contains(EList<? extends EObject> members, String collection, String id) {
		return ids(members, collection).contains(id);
	}

	/** Every id in {@code members}, in order, skipping entries that name none. */
	public static List<String> ids(EList<? extends EObject> members, String collection) {
		List<String> ids = new ArrayList<>(members.size());
		for (Iterator<? extends EObject> it = basicIterator(members); it.hasNext();) {
			String id = targetId(collection, it.next());
			if (id != null) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * Removes the entry naming {@code id}, if present.
	 * <p>
	 * Found by index rather than through the iterator: the non-resolving iterator is
	 * read-only ({@code remove()} throws {@code UnsupportedOperationException}).
	 * Removing by index does not resolve either, so the walk stays free of file reads.
	 *
	 * @return whether anything was removed
	 */
	public static boolean remove(EList<? extends EObject> members, String collection, String id) {
		int index = indexOf(members, collection, id);
		if (index < 0) {
			return false;
		}
		members.remove(index);
		return true;
	}

	/** The position of the entry naming {@code id}, or {@code -1}; resolves nothing. */
	public static int indexOf(EList<? extends EObject> members, String collection, String id) {
		if (id == null) {
			return -1;
		}
		int index = 0;
		for (Iterator<? extends EObject> it = basicIterator(members); it.hasNext(); index++) {
			if (id.equals(targetId(collection, it.next()))) {
				return index;
			}
		}
		return -1;
	}

	@SuppressWarnings("unchecked")
	private static Iterator<? extends EObject> basicIterator(EList<? extends EObject> members) {
		if (members instanceof InternalEList) {
			return ((InternalEList<? extends EObject>) members).basicIterator();
		}
		return members.iterator();
	}
}

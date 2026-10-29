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
package org.eclipse.fennec.dcat.atlas.impl.store;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.api.validation.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.records.CatalogRecords;
import org.eclipse.fennec.dcat.atlas.impl.validation.ModelValidation;
import org.eclipse.fennec.dcat.atlas.impl.validation.ShaclValidation;

import dcat.Catalog;
import rdf.IdentifiedResource;

/**
 * The portal's write rules, over any {@link StorageSession}.
 * <p>
 * Everything a write has to satisfy before it is allowed to reach storage lives here and
 * only here: who may write, what a stored resource is called, the catalogue records the
 * server maintains, and the two validation floors. A backend implements
 * {@link StorageSession} and inherits all of it.
 * <p>
 * Reads pass straight through. The helpers it calls — {@code References},
 * {@code CatalogRecords} — are given {@code this} rather than the delegate on purpose: they
 * read through the session, and what they must see is the session's own view, staged
 * changes included.
 */
public class GuardedStoreSession implements DcatStoreSession {

	private final StorageSession storage;
	private final Caller caller;
	private final DcatValidationService validationService;
	private final boolean validateOnWrite;

	public GuardedStoreSession(StorageSession storage, Caller caller, DcatValidationService validationService,
			boolean validateOnWrite) {
		this.storage = storage;
		this.caller = caller;
		this.validationService = validationService;
		this.validateOnWrite = validateOnWrite;
	}

	@Override
	public <T extends EObject> Optional<T> get(String collection, String id) {
		return storage.get(collection, id);
	}

	@Override
	public <T extends EObject> List<T> list(String collection) {
		return storage.list(collection);
	}

	@Override
	public List<String> ids(String collection) {
		return storage.ids(collection);
	}

	@Override
	public <T extends EObject> T put(String collection, String id, T object) {
		StoreLayout.requireSafeId(id);
		String iri = StoreLayout.logicalIri(collection, id);
		String managedBy = storedManagedBy(collection, id);
		caller.requireMayWrite(managedBy, iri);
		if (object instanceof IdentifiedResource identified) {
			identified.setAbout(iri);
			identified.setManagedBy(managedBy != null ? managedBy : caller.stamp());
		}
		// Records are the server's, whatever the body says, and they are rebuilt before
		// validation so what is checked is what will be stored.
		if (object instanceof Catalog catalog) {
			CatalogRecords.preserveRecords(this, collection, id, catalog);
		}
		// Completed before validation, so what is checked is what will be stored: a format
		// derived from the media type must itself clear the floor.
		FormatDefaults.apply(object);
		// After the identity is stamped — "a stored entity has an about" is one of the
		// constraints, and a SHACL report has to name the node that will actually be
		// stored — and before anything is staged, because the point is that an invalid
		// entity never reaches a commit.
		validate(object);
		return storage.store(collection, id, object);
	}

	@Override
	public void save(EObject object) {
		requireMayWrite(object);
		FormatDefaults.apply(object);
		validate(object);
		storage.stage(object);
	}

	@Override
	public void saveRemoval(EObject object) {
		requireMayWrite(object);
		storage.stage(object);
	}

	@Override
	public void saveBookkeeping(EObject object) {
		storage.stage(object);
	}

	@Override
	public boolean delete(String collection, String id) {
		// Checked only when there is something to delete, so deleting what is not there
		// stays the plain false callers rely on rather than becoming a way to probe for
		// managed resources.
		if (storage.exists(collection, id)) {
			caller.requireMayWrite(storedManagedBy(collection, id), StoreLayout.logicalIri(collection, id));
		}
		return storage.delete(collection, id);
	}

	@Override
	public Optional<String> commit(String message) {
		return storage.commit(message);
	}

	/** The {@code managedBy} of what is stored under {@code id}, or {@code null}. */
	private String storedManagedBy(String collection, String id) {
		return storage.<EObject>get(collection, id) //
				.filter(IdentifiedResource.class::isInstance) //
				.map(stored -> ((IdentifiedResource) stored).getManagedBy()) //
				.orElse(null);
	}

	/**
	 * Refuses a re-staged object this caller does not manage.
	 * <p>
	 * Reading the management off the object is sound here and not in {@link #put}: what is
	 * re-staged came out of {@link #get} and was mutated, so its {@code managedBy} is the
	 * stored one.
	 */
	private void requireMayWrite(EObject object) {
		if (object instanceof IdentifiedResource identified) {
			caller.requireMayWrite(identified.getManagedBy(), identified.getAbout());
		}
	}

	/**
	 * The write-side floor, shared by {@link #put} and {@link #save} so the two cannot
	 * drift — which is how issue 34 happened in the first place.
	 */
	private void validate(EObject object) {
		// Refuse a link to an identity of ours that is not there, before anything is
		// written — the write-side half of the rule References.detach enforces on delete.
		//
		// It runs before validation on purpose. EMF's own validate_EveryProxyResolves
		// would also reject a dangling link, but as a generic "a proxy did not resolve"
		// — losing which member was missing, and answering 422 where the API has always
		// answered 409. The more specific diagnosis wins.
		References.requireResolvable(this, object);
		// Ungated, like requireResolvable above and unlike ModelValidation.check below.
		// Distinct identities are not a question of conformance an operator may switch off:
		// the store addresses a Distribution by its about, so two of them under one about
		// makes the second unreachable and a DELETE of that id answer 204 while leaving it
		// in place. validateOnWrite=false must still not be able to store that.
		ModelValidation.checkDistinctDistributions(object);
		// Model constraints first: they are cheap and structural, where SHACL serializes
		// the entity to RDF and unions the vocabulary graph. An entity missing its
		// publisher should not pay for a shapes run to be told so.
		if (validateOnWrite) {
			ModelValidation.check(object);
		}
		// The referenced resources come along as SHACL context — only their rdf:type —
		// so a reference-typing constraint can be answered. requireResolvable above has
		// already established that they all exist.
		ShaclValidation.check(validationService, object, References.referenced(this, object));
	}
}

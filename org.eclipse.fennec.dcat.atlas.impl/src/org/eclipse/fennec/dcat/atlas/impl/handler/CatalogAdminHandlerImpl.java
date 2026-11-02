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
package org.eclipse.fennec.dcat.atlas.impl.handler;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminHandler;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService;
import org.eclipse.fennec.dcat.atlas.api.store.DcatStoreSession;
import org.eclipse.fennec.dcat.atlas.impl.integrity.Members;
import org.eclipse.fennec.dcat.atlas.impl.integrity.References;
import org.eclipse.fennec.dcat.atlas.impl.records.CatalogRecords;
import org.eclipse.fennec.dcat.atlas.impl.runtime.DcatChangeNotifier;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import rdf.IdentifiedResource;

/**
 * File-backed {@link CatalogAdminHandler} (write side).
 *
 * <h2>Membership (FR-9)</h2>
 *
 * A member is an EMF cross-resource reference to the entity in its own store, so
 * the catalog file records {@code href="http://dcat.atlas/datasets/air#/"} and
 * there stays exactly one description of the dataset. A dataset may be listed by
 * several catalogs ({@code dcat:dataset} is {@code [*]} with no inverse
 * constraint), which containment could not express at all.
 * <p>
 * Two ways in per entity type: {@code addXToCatalog} takes the entity and stores
 * it before linking; {@code linkXToCatalog} takes an id and requires it to exist.
 * Both are idempotent on the membership itself, so a repeat leaves the catalog —
 * and its ETag — untouched.
 */
public class CatalogAdminHandlerImpl extends CatalogReadOnlyHandlerImpl implements CatalogAdminHandler {


	/** How this service says it changed the portal; see DcatChangeNotifier. */
	private final DcatChangeNotifier changeNotifier;

	private final DcatGraphService graphService;

	/** Which membership list on a Catalog a given collection is linked through. */
	private static final Function<Catalog, EList<? extends EObject>> DATASETS = Catalog::getDataset;
	private static final Function<Catalog, EList<? extends EObject>> SERVICES = Catalog::getService;
	private static final Function<Catalog, EList<? extends EObject>> SUB_CATALOGS = Catalog::getCatalog;


	public CatalogAdminHandlerImpl(DcatChangeNotifier changeNotifier,
			DcatStoreService store,
			DcatGraphService graphService) {
		super(store);
		this.changeNotifier = changeNotifier;
		this.graphService = graphService;
	}

	/** For callers with no projection to reconcile - the tests, and a portal without SPARQL. */
	public CatalogAdminHandlerImpl(DcatChangeNotifier changeNotifier, DcatStoreService store) {
		this(changeNotifier, store, null);
	}


	@Override
	public Catalog upsertCatalog(Catalog catalog, Caller caller) {
		String id = idOrMint(catalog);
		DcatStoreSession session = store(caller);
		session.put(collection, id, catalog);
		// A catalog is also a member - of its parent, when it is a sub-catalog (#56).
		CatalogRecords.memberWritten(session, StoreLayout.logicalIri(collection, id));
		session.commit("Store catalog " + id);
		reproject(id);
		changeNotifier.entityChanged(collection, id);
		return catalog;
	}

	@Override
	public List<String> deleteCatalog(String id, boolean cascade, Caller caller) {
		DcatStoreSession session = store(caller);
		if (cascade) {
			// Before detach, which would otherwise strip the records' primaryTopic and leave
			// them meaningless and invalid - see CatalogRecords.memberDeleted.
			CatalogRecords.memberDeleted(session, StoreLayout.logicalIri(collection, id));
		}
		List<String> unlinked = References.detach(session, collection, id, cascade);
		session.delete(collection, id);
		// One commit for the delete and every unlink it caused: a cascade that committed
		// per referrer would publish states in which the catalog is gone but something
		// still points at it, and the SPARQL projection reads the same store.
		session.commit(cascade ? "Delete catalog " + id + " and unlink its referrers" : "Delete catalog " + id);
		reproject(id);
		changeNotifier.entityDeleted(collection, id);
		return unlinked;
	}

	// --- FR-9 catalog membership -------------------------------------------

	@Override
	public Catalog addDatasetToCatalog(String catalogId, Dataset dataset, Caller caller) {
		return add(catalogId, StoreLayout.DATASETS, dataset, DATASETS, caller);
	}

	@Override
	public Catalog linkDatasetToCatalog(String catalogId, String datasetId, Caller caller) {
		return link(catalogId, StoreLayout.DATASETS, datasetId, DATASETS, caller);
	}

	@Override
	public void deleteDatasetFromCatalog(String catalogId, String datasetId, Caller caller) {
		unlink(catalogId, StoreLayout.DATASETS, datasetId, DATASETS, caller);
	}

	@Override
	public Catalog addDataServiceToCatalog(String catalogId, DataService dataService, Caller caller) {
		return add(catalogId, StoreLayout.DATA_SERVICES, dataService, SERVICES, caller);
	}

	@Override
	public Catalog linkDataServiceToCatalog(String catalogId, String dataServiceId, Caller caller) {
		return link(catalogId, StoreLayout.DATA_SERVICES, dataServiceId, SERVICES, caller);
	}

	@Override
	public void deleteDataServiceFromCatalog(String catalogId, String dataServiceId, Caller caller) {
		unlink(catalogId, StoreLayout.DATA_SERVICES, dataServiceId, SERVICES, caller);
	}

	@Override
	public Catalog addSubCatalogToCatalog(String catalogId, Catalog subCatalog, Caller caller) {
		return add(catalogId, StoreLayout.CATALOGS, subCatalog, SUB_CATALOGS, caller);
	}

	@Override
	public Catalog linkSubCatalogToCatalog(String catalogId, String subCatalogId, Caller caller) {
		return link(catalogId, StoreLayout.CATALOGS, subCatalogId, SUB_CATALOGS, caller);
	}

	@Override
	public void deleteSubCatalogFromCatalog(String catalogId, String subCatalogId, Caller caller) {
		unlink(catalogId, StoreLayout.CATALOGS, subCatalogId, SUB_CATALOGS, caller);
	}

	// --- the three membership shapes ---------------------------------------

	/**
	 * Stores {@code member}, then links it.
	 * <p>
	 * The member is written first because a link to something that is not there yet
	 * is exactly the dangling reference this design refuses to create. Storing is an
	 * upsert, matching {@code upsertDataset} and friends: a caller that hands over a
	 * whole entity means that entity to be what is stored.
	 */
	private <T extends EObject> Catalog add(String catalogId, String memberCollection, T member,
			Function<Catalog, EList<? extends EObject>> membership, Caller caller) {
		if (member == null) {
			throw new IllegalArgumentException("Cannot add nothing to catalog " + catalogId);
		}
		DcatStoreSession session = store(caller);
		requireCatalog(session, catalogId);
		String memberId = memberIdOrMint(memberCollection, member);
		session.put(memberCollection, memberId, member);
		return connect(session, catalogId, memberCollection, memberId, membership,
				"Add %s %s to catalog %s".formatted(memberCollection, memberId, catalogId));
	}

	/** Links an entity that must already exist. */
	private Catalog link(String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership, Caller caller) {
		DcatStoreSession session = store(caller);
		requireCatalog(session, catalogId);
		if (session.get(memberCollection, memberId).isEmpty()) {
			throw new NoSuchElementException("Unknown " + memberCollection + ": " + memberId);
		}
		return connect(session, catalogId, memberCollection, memberId, membership,
				"Link %s %s to catalog %s".formatted(memberCollection, memberId, catalogId));
	}

	@SuppressWarnings("unchecked")
	private Catalog connect(DcatStoreSession session, String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership, String message) {
		Catalog catalog = requireCatalog(session, catalogId);
		EList<EObject> members = (EList<EObject>) membership.apply(catalog);
		boolean linked = false;
		if (!Members.contains(members, memberCollection, memberId)) {
			EObject member = session.<EObject>get(memberCollection, memberId).orElseThrow(
					() -> new NoSuchElementException("Unknown " + memberCollection + ": " + memberId));
			members.add(member);
			// The entry is new, so it gets its catalogue record in the same write (#56).
			CatalogRecords.memberLinked(catalog, member);
			session.save(catalog);
			linked = true;
		}
		// Commits whatever the operation staged, as one commit: for addX the member and the
		// catalog together, for a repeat of an existing membership the member alone, and for
		// a repeated link nothing at all - which commits nothing and leaves the ETag alone.
		session.commit(message);
		reproject(catalogId);
		// After the commit, and only for a link that happened: delivery is asynchronous, so a
		// consumer can read the portal before the operation that announced the change finished.
		if (linked) {
			changeNotifier.entityLinked(memberCollection, memberId);
		}
		return catalog;
	}

	private void unlink(String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership, Caller caller) {
		DcatStoreSession session = store(caller);
		Catalog catalog = requireCatalog(session, catalogId);
		if (Members.remove(membership.apply(catalog), memberCollection, memberId)) {
			// The entry is gone, and a record describes an entry (#56).
			CatalogRecords.memberUnlinked(catalog, StoreLayout.logicalIri(memberCollection, memberId));
			// A removal. See DcatStoreSession.saveRemoval.
			session.saveRemoval(catalog);
			session.commit("Unlink %s %s from catalog %s".formatted(memberCollection, memberId, catalogId));
			reproject(catalogId);
			changeNotifier.entityUnlinked(memberCollection, memberId);
		}
	}

	// --- helpers ------------------------------------------------------------

	private static Catalog requireCatalog(DcatStoreSession session, String catalogId) {
		return session.<Catalog>get(StoreLayout.CATALOGS, catalogId)
				.orElseThrow(() -> new NoSuchElementException("Unknown catalog: " + catalogId));
	}

	/** As {@code idOrMint}, but for a member of a collection other than this store's. */
	private static String memberIdOrMint(String memberCollection, EObject member) {
		String about = member instanceof IdentifiedResource identified ? identified.getAbout() : null;
		return DcatIds.idForWrite(memberCollection, about);
	}

	/** Re-projects one catalog into the RDF graph, if a projection is present. */
	private void reproject(String catalogId) {
		DcatGraphService graph = graphService;
		if (graph != null) {
			graph.invalidate(DcatEntity.CATALOG, catalogId);
		}
	}
}

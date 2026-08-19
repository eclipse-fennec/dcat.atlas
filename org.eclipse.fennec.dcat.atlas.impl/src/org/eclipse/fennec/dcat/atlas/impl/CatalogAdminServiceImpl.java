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
package org.eclipse.fennec.dcat.atlas.impl;

import java.util.NoSuchElementException;
import java.util.function.Function;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.DcatEntity;
import org.eclipse.fennec.dcat.atlas.api.DcatGraphService;
import org.eclipse.fennec.dcat.atlas.api.DcatValidationService;
import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper.Store;
import org.eclipse.fennec.dcat.atlas.impl.helper.Members;
import org.eclipse.fennec.dcat.atlas.impl.helper.References;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.jgit.api.GitService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import rdf.IdentifiedResource;

/**
 * File-backed {@link CatalogAdminService} (write side).
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
@Component(name = "CatalogAdminService", service = CatalogAdminService.class)
@Designate(ocd = StoreConfig.class)
public class CatalogAdminServiceImpl extends CatalogReadOnlyServiceImpl implements CatalogAdminService {

	/** Which membership list on a Catalog a given collection is linked through. */
	private static final Function<Catalog, EList<? extends EObject>> DATASETS = Catalog::getDataset;
	private static final Function<Catalog, EList<? extends EObject>> SERVICES = Catalog::getService;
	private static final Function<Catalog, EList<? extends EObject>> SUB_CATALOGS = Catalog::getCatalog;

	@Activate
	public CatalogAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory,
			@Reference(name = "gitService") GitService gitService, StoreConfig config) {
		super(resourceSetFactory, gitService, config);
	}

	/** Package-visible for tests. */
	CatalogAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath) {
		super(resourceSetFactory, gitService, basePath);
	}

	/** Package-visible for tests that need the model constraints enforced. */
	CatalogAdminServiceImpl(ResourceSetFactory resourceSetFactory, GitService gitService, String basePath,
			boolean validateOnWrite) {
		super(resourceSetFactory, gitService, basePath, validateOnWrite);
	}

	/**
	 * The RDF projection behind the SPARQL endpoint, maintained here rather than in
	 * the REST layer: this service is the persistence boundary, and REST is only one
	 * of its callers (persistence plan constraint G2).
	 * <p>
	 * Optional and dynamic so the projection can be reconfigured, or absent
	 * altogether, without recycling the store services — absent simply means no
	 * SPARQL, and a projection that missed an update is repaired by its own
	 * reconciliation pass.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatGraphService graphService;

	/**
	 * On-write SHACL enforcement (FR-4), maintained here rather than in the REST layer:
	 * this service is the persistence boundary, and REST is only one of its callers.
	 * <p>
	 * Optional and dynamic, so absence simply means no enforcement — see
	 * {@link org.eclipse.fennec.dcat.atlas.impl.helper.ShaclValidation}. An operator who
	 * needs the strict reading raises this reference's minimum cardinality in
	 * configuration ({@code validationService.cardinality.minimum=1}), which makes this
	 * component unsatisfiable without a validation service instead of letting writes
	 * through unchecked.
	 */
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	volatile DcatValidationService validationService;

	@Override
	protected DcatValidationService writeValidation() {
		return validationService;
	}

	@Override
	public Catalog upsertCatalog(Catalog catalog) {
		String id = idOrMint(catalog);
		Store store = store();
		store.put(collection, id, catalog);
		store.commit("Store catalog " + id);
		reproject(id);
		return catalog;
	}

	@Override
	public void deleteCatalog(String id, boolean cascade) {
		Store store = store();
		References.detach(store, collection, id, cascade);
		store.delete(collection, id);
		// One commit for the delete and every unlink it caused: a cascade that committed
		// per referrer would publish states in which the catalog is gone but something
		// still points at it, and the SPARQL projection reads the same store.
		store.commit(cascade ? "Delete catalog " + id + " and unlink its referrers" : "Delete catalog " + id);
		reproject(id);
	}

	// --- FR-9 catalog membership -------------------------------------------

	@Override
	public Catalog addDatasetToCatalog(String catalogId, Dataset dataset) {
		return add(catalogId, StoreLayout.DATASETS, dataset, DATASETS);
	}

	@Override
	public Catalog linkDatasetToCatalog(String catalogId, String datasetId) {
		return link(catalogId, StoreLayout.DATASETS, datasetId, DATASETS);
	}

	@Override
	public void deleteDatasetFromCatalog(String catalogId, String datasetId) {
		unlink(catalogId, StoreLayout.DATASETS, datasetId, DATASETS);
	}

	@Override
	public Catalog addDataServiceToCatalog(String catalogId, DataService dataService) {
		return add(catalogId, StoreLayout.DATA_SERVICES, dataService, SERVICES);
	}

	@Override
	public Catalog linkDataServiceToCatalog(String catalogId, String dataServiceId) {
		return link(catalogId, StoreLayout.DATA_SERVICES, dataServiceId, SERVICES);
	}

	@Override
	public void deleteDataServiceFromCatalog(String catalogId, String dataServiceId) {
		unlink(catalogId, StoreLayout.DATA_SERVICES, dataServiceId, SERVICES);
	}

	@Override
	public Catalog addSubCatalogToCatalog(String catalogId, Catalog subCatalog) {
		return add(catalogId, StoreLayout.CATALOGS, subCatalog, SUB_CATALOGS);
	}

	@Override
	public Catalog linkSubCatalogToCatalog(String catalogId, String subCatalogId) {
		return link(catalogId, StoreLayout.CATALOGS, subCatalogId, SUB_CATALOGS);
	}

	@Override
	public void deleteSubCatalogFromCatalog(String catalogId, String subCatalogId) {
		unlink(catalogId, StoreLayout.CATALOGS, subCatalogId, SUB_CATALOGS);
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
			Function<Catalog, EList<? extends EObject>> membership) {
		if (member == null) {
			throw new IllegalArgumentException("Cannot add nothing to catalog " + catalogId);
		}
		Store store = store();
		requireCatalog(store, catalogId);
		String memberId = memberIdOrMint(memberCollection, member);
		store.put(memberCollection, memberId, member);
		return connect(store, catalogId, memberCollection, memberId, membership,
				"Add %s %s to catalog %s".formatted(memberCollection, memberId, catalogId));
	}

	/** Links an entity that must already exist. */
	private Catalog link(String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership) {
		Store store = store();
		requireCatalog(store, catalogId);
		if (store.get(memberCollection, memberId).isEmpty()) {
			throw new NoSuchElementException("Unknown " + memberCollection + ": " + memberId);
		}
		return connect(store, catalogId, memberCollection, memberId, membership,
				"Link %s %s to catalog %s".formatted(memberCollection, memberId, catalogId));
	}

	@SuppressWarnings("unchecked")
	private Catalog connect(Store store, String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership, String message) {
		Catalog catalog = requireCatalog(store, catalogId);
		EList<EObject> members = (EList<EObject>) membership.apply(catalog);
		if (!Members.contains(members, memberCollection, memberId)) {
			members.add(store.<EObject>get(memberCollection, memberId).orElseThrow(
					() -> new NoSuchElementException("Unknown " + memberCollection + ": " + memberId)));
			store.save(catalog);
		}
		// Commits whatever the operation staged, as one commit: for addX the member and the
		// catalog together, for a repeat of an existing membership the member alone, and for
		// a repeated link nothing at all - which commits nothing and leaves the ETag alone.
		store.commit(message);
		reproject(catalogId);
		return catalog;
	}

	private void unlink(String catalogId, String memberCollection, String memberId,
			Function<Catalog, EList<? extends EObject>> membership) {
		Store store = store();
		Catalog catalog = requireCatalog(store, catalogId);
		if (Members.remove(membership.apply(catalog), memberCollection, memberId)) {
			store.save(catalog);
			store.commit("Unlink %s %s from catalog %s".formatted(memberCollection, memberId, catalogId));
			reproject(catalogId);
		}
	}

	// --- helpers ------------------------------------------------------------

	private static Catalog requireCatalog(Store store, String catalogId) {
		return store.<Catalog>get(StoreLayout.CATALOGS, catalogId)
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

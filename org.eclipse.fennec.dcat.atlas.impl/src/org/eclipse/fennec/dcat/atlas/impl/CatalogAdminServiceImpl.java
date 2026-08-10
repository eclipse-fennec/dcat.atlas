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

import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetContainer;
import dcat.DcatFactory;
import dcat.DcatPackage;

/**
 * File-backed {@link CatalogAdminService} (write side). Extends
 * {@link CatalogReadOnlyServiceImpl} to inherit {@code getCatalog}/{@code
 * listCatalogs} and adds create/replace/delete; all persistence goes through
 * {@link DcatHelper} using the storage location set up by the superclass.
 * <p>
 * The {@code id} is the last path segment of the catalog's {@code rdf:about} URI
 * (the REST adapter sets that URI from the request URL before delegating here).
 * <p>
 * Deliberately simple for now: no ETag/optimistic locking, no SHACL validation,
 * no cascade handling and no transactions.
 */
@Component(name = "CatalogAdminService", service = CatalogAdminService.class)
@Designate(ocd = StoreConfig.class)
public class CatalogAdminServiceImpl extends CatalogReadOnlyServiceImpl implements CatalogAdminService {

	@Activate
	public CatalogAdminServiceImpl(@Reference ResourceSetFactory resourceSetFactory, StoreConfig config) {
		super(resourceSetFactory, config);
	}

	/** Package-visible for tests. */
	CatalogAdminServiceImpl(ResourceSetFactory resourceSetFactory, Path directory) {
		super(resourceSetFactory, directory);
	}

	@Override
	public Catalog upsertCatalog(Catalog catalog) {
		String id = DcatHelper.idOf(catalog.getAbout());
		if (id == null) {
			// Mint an id when the client supplied no about (D2/FR-3).
			id = UUID.randomUUID().toString();
		}
		DcatHelper.write(resourceSetFactory, directory, id, DcatPackage.Literals.DCATAP_ROOT__CATALOG, catalog);
		return catalog;
	}

	@Override
	public void deleteCatalog(String id, boolean cascade) {
		// TODO FR-1: 409 when the catalog is still referenced; cascade currently ignored.
		DcatHelper.delete(directory, id);
	}

	// --- FR-9 catalog membership -------------------------------------------
	//
	// Dataset, DataService and sub-catalog are all *containment* references on
	// Catalog (Catalog.dataset -> DatasetContainer -> Dataset, Catalog.service,
	// Catalog.catalog), so membership is maintained entirely inside the catalog's
	// own file: load the catalog, add/remove the member, store the catalog again.
	// The caller sends only the member, never the whole catalog (FR-9). Members
	// are matched on delete by the last path segment of their rdf:about.

	@Override
	public Catalog addDatasetToCatalog(String catalogId, Dataset dataset) {
		Catalog catalog = requireCatalog(catalogId);
		String datasetId = DcatHelper.idOf(dataset.getAbout());
		boolean present = catalog.getDataset().stream()
				.anyMatch(c -> c.getDataset() != null && datasetId != null
						&& datasetId.equals(DcatHelper.idOf(c.getDataset().getAbout())));
		if (present) {
			// Idempotent: membership already recorded, leave the catalog (and its ETag) untouched.
			return catalog;
		}
		DatasetContainer container = DcatFactory.eINSTANCE.createDatasetContainer();
		container.setDataset(dataset);
		catalog.getDataset().add(container);
		return store(catalogId, catalog);
	}

	@Override
	public void deleteDatasetFromCatalog(String catalogId, String datasetId) {
		Catalog catalog = requireCatalog(catalogId);
		if (catalog.getDataset().removeIf(
				c -> c.getDataset() != null && datasetId.equals(DcatHelper.idOf(c.getDataset().getAbout())))) {
			store(catalogId, catalog);
		}
	}

	@Override
	public Catalog addDataServiceToCatalog(String catalogId, DataService dataService) {
		Catalog catalog = requireCatalog(catalogId);
		String serviceId = DcatHelper.idOf(dataService.getAbout());
		boolean present = catalog.getService().stream()
				.anyMatch(s -> serviceId != null && serviceId.equals(DcatHelper.idOf(s.getAbout())));
		if (present) {
			return catalog;
		}
		catalog.getService().add(dataService);
		return store(catalogId, catalog);
	}

	@Override
	public void deleteDataServiceFromCatalog(String catalogId, String dataServiceId) {
		Catalog catalog = requireCatalog(catalogId);
		if (catalog.getService().removeIf(s -> dataServiceId.equals(DcatHelper.idOf(s.getAbout())))) {
			store(catalogId, catalog);
		}
	}

	@Override
	public Catalog addSubCatalogToCatalog(String catalogId, Catalog subCatalog) {
		Catalog catalog = requireCatalog(catalogId);
		String subCatalogId = DcatHelper.idOf(subCatalog.getAbout());
		boolean present = catalog.getCatalog().stream()
				.anyMatch(c -> subCatalogId != null && subCatalogId.equals(DcatHelper.idOf(c.getAbout())));
		if (present) {
			return catalog;
		}
		catalog.getCatalog().add(subCatalog);
		return store(catalogId, catalog);
	}

	@Override
	public void deleteSubCatalogFromCatalog(String catalogId, String subCatalogId) {
		Catalog catalog = requireCatalog(catalogId);
		if (catalog.getCatalog().removeIf(c -> subCatalogId.equals(DcatHelper.idOf(c.getAbout())))) {
			store(catalogId, catalog);
		}
	}

	private Catalog requireCatalog(String catalogId) {
		return getCatalog(catalogId)
				.orElseThrow(() -> new NoSuchElementException("Unknown catalog: " + catalogId));
	}

	private Catalog store(String catalogId, Catalog catalog) {
		DcatHelper.write(resourceSetFactory, directory, catalogId, DcatPackage.Literals.DCATAP_ROOT__CATALOG, catalog);
		return catalog;
	}
}

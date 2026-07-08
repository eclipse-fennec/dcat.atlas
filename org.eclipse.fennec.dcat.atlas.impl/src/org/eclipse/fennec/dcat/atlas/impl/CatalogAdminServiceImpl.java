package org.eclipse.fennec.dcat.atlas.impl;

import java.nio.file.Path;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import dcat.Catalog;
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
}

package org.eclipse.fennec.dcat.atlas.rest.tests;

import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.Catalog;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

public class CatalogResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	CatalogAdminService service;

	@Override
	protected String collection() {
		return "catalogs";
	}

	@Override
	protected String typeName() {
		return "Catalog";
	}

	@Override
	protected String readResourceName() {
		return "CatalogReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "CatalogAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(reads() + "/" + id);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		catalog.getTitle().add(literal);
		service.upsertCatalog(catalog);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getCatalog(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getCatalog(id).get().getTitle().get(0).getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteCatalog(id, false);
	}
}

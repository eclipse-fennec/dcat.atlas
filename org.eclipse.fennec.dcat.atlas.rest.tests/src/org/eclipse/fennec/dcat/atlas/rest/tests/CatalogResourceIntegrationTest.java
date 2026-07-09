package org.eclipse.fennec.dcat.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.eclipse.fennec.dcat.atlas.api.CatalogAdminService;
import org.junit.jupiter.api.Test;
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

	// --- FR-9 catalog membership -------------------------------------------

	@Test
	void addAndRemoveDatasetMembershipOverHttp() throws Exception {
		track("cat1");
		seed("cat1", "GovData");
		String memberAbout = reads() + "/cat1/datasets/ds1";

		HttpResponse<String> add = postRdfXml(writes() + "/cat1/datasets", rdfXmlBody("Dataset", memberAbout, "Air"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, service.getCatalog("cat1").get().getDataset().size());
		String etagAfterAdd = add.headers().firstValue("ETag").orElseThrow();

		// Idempotent (F-16): re-adding the same dataset changes nothing, so the
		// catalog's ETag is identical and there is still a single member.
		HttpResponse<String> reAdd = postRdfXml(writes() + "/cat1/datasets", rdfXmlBody("Dataset", memberAbout, "Air"));
		assertEquals(200, reAdd.statusCode(), reAdd.body());
		assertEquals(etagAfterAdd, reAdd.headers().firstValue("ETag").orElseThrow());
		assertEquals(1, service.getCatalog("cat1").get().getDataset().size());

		HttpResponse<String> remove = delete(writes() + "/cat1/datasets/ds1");
		assertEquals(204, remove.statusCode());
		assertTrue(service.getCatalog("cat1").get().getDataset().isEmpty());
	}

	@Test
	void addAndRemoveSubCatalogMembershipOverHttp() throws Exception {
		track("cat1");
		seed("cat1", "GovData");
		String memberAbout = reads() + "/eu";

		HttpResponse<String> add = postRdfXml(writes() + "/cat1/catalogs", rdfXmlBody("Catalog", memberAbout, "EU"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, service.getCatalog("cat1").get().getCatalog().size());

		HttpResponse<String> remove = delete(writes() + "/cat1/catalogs/eu");
		assertEquals(204, remove.statusCode());
		assertTrue(service.getCatalog("cat1").get().getCatalog().isEmpty());
	}

	@Test
	void addMembershipToUnknownCatalogIsNotFound() throws Exception {
		HttpResponse<String> add = postRdfXml(writes() + "/missing/datasets",
				rdfXmlBody("Dataset", reads() + "/missing/datasets/ds1", "Air"));
		assertEquals(404, add.statusCode());
	}
}

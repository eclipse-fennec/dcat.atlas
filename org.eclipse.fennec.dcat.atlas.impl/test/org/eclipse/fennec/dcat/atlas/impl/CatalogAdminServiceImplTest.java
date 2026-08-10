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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.eclipse.fennec.dcat.atlas.impl.helper.DcatHelper;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import adms.AdmsPackage;
import adms.impl.AdmsPackageImpl;
import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.DcatPackage;
import dcat.impl.DcatPackageImpl;
import dcatde.DcatDEPackage;
import dcatde.impl.DcatDEPackageImpl;
import foaf.FoafPackage;
import foaf.impl.FoafPackageImpl;
import locn.LocnPackage;
import locn.impl.LocnPackageImpl;
import odrl.OdrlPackage;
import odrl.impl.OdrlPackageImpl;
import owl.OwlPackage;
import owl.impl.OwlPackageImpl;
import prov.ProvPackage;
import prov.impl.ProvPackageImpl;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import rdf.RdfPackage;
import rdf.impl.RdfPackageImpl;
import rdf.util.RdfResourceFactoryImpl;
import schema.SchemaPackage;
import schema.impl.SchemaPackageImpl;
import skos.SkosPackage;
import skos.impl.SkosPackageImpl;
import spdx.SpdxPackage;
import spdx.impl.SpdxPackageImpl;
import terms.TermsPackage;
import terms.impl.TermsPackageImpl;
import vcard.VcardPackage;
import vcard.impl.VcardPackageImpl;

/**
 * Round-trips the file-backed catalog store: upsert writes an RDF/XML file that
 * {@code getCatalog}/{@code listCatalogs} read back into an equivalent EMF model.
 */
public class CatalogAdminServiceImplTest {

	private static final String BASE = "https://portal.example/admin/api/v1/catalogs/";

	@TempDir
	Path storage;

	private CatalogAdminServiceImpl service() {
		ResourceSetFactory factory = CatalogAdminServiceImplTest::newResourceSet;
		return new CatalogAdminServiceImpl(factory, storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));

		Optional<Catalog> loaded = service.getCatalog("gov");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "gov", loaded.get().getAbout());
		assertEquals("GovData", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));
		service.upsertCatalog(catalog(BASE + "eu", "EU Portal"));

		List<Catalog> all = service.listCatalogs();
		assertEquals(2, all.size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getCatalog("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));
		service.deleteCatalog("gov", false);
		assertTrue(service.getCatalog("gov").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(null, "Untitled about"));
		assertEquals(1, service.listCatalogs().size());
	}

	@Test
	void idIsDerivedFromAboutLastSegment() {
		assertEquals("gov", DcatHelper.idOf(BASE + "gov"));
		assertNull(DcatHelper.idOf(null));
	}

	// --- FR-9 catalog membership -------------------------------------------

	@Test
	void addAndRemoveDatasetMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));

		service.addDatasetToCatalog("gov", dataset(BASE + "datasets/air", "Air quality"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getDataset().size());
		assertEquals(BASE + "datasets/air", stored.getDataset().get(0).getDataset().getAbout());

		service.deleteDatasetFromCatalog("gov", "air");
		assertTrue(service.getCatalog("gov").get().getDataset().isEmpty());
	}

	@Test
	void addAndRemoveServiceMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));

		service.addDataServiceToCatalog("gov", dataService(BASE + "services/sparql"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getService().size());
		assertEquals(BASE + "services/sparql", stored.getService().get(0).getAbout());

		service.deleteDataServiceFromCatalog("gov", "sparql");
		assertTrue(service.getCatalog("gov").get().getService().isEmpty());
	}

	@Test
	void addAndRemoveSubCatalogMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(BASE + "gov", "GovData"));

		service.addSubCatalogToCatalog("gov", catalog(BASE + "eu", "EU Portal"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getCatalog().size());
		assertEquals(BASE + "eu", stored.getCatalog().get(0).getAbout());

		service.deleteSubCatalogFromCatalog("gov", "eu");
		assertTrue(service.getCatalog("gov").get().getCatalog().isEmpty());
	}

	@Test
	void membershipOnUnknownCatalogThrows() {
		CatalogAdminServiceImpl service = service();
		assertThrows(NoSuchElementException.class,
				() -> service.addDatasetToCatalog("missing", dataset(BASE + "datasets/air", "Air quality")));
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		return dataset;
	}

	private static DataService dataService(String about) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(about);
		return service;
	}

	private static Catalog catalog(String about, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		if (about != null) {
			catalog.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		catalog.getTitle().add(literal);
		return catalog;
	}

	private static ResourceSet newResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		Registry registry = resourceSet.getPackageRegistry();
		registry.put(SchemaPackage.eNS_URI, SchemaPackageImpl.init());
		registry.put(TermsPackage.eNS_URI, TermsPackageImpl.init());
		registry.put(FoafPackage.eNS_URI, FoafPackageImpl.init());
		registry.put(AdmsPackage.eNS_URI, AdmsPackageImpl.init());
		registry.put(DcatPackage.eNS_URI, DcatPackageImpl.init());
		registry.put(DcatDEPackage.eNS_URI, DcatDEPackageImpl.init());
		registry.put(LocnPackage.eNS_URI, LocnPackageImpl.init());
		registry.put(OdrlPackage.eNS_URI, OdrlPackageImpl.init());
		registry.put(OwlPackage.eNS_URI, OwlPackageImpl.init());
		registry.put(ProvPackage.eNS_URI, ProvPackageImpl.init());
		registry.put(RdfPackage.eNS_URI, RdfPackageImpl.init());
		registry.put(SkosPackage.eNS_URI, SkosPackageImpl.init());
		registry.put(VcardPackage.eNS_URI, VcardPackageImpl.init());
		registry.put(SpdxPackage.eNS_URI, SpdxPackageImpl.init());
		registry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		registry.put(XMLNamespacePackage.eNS_URI, XMLNamespacePackage.eINSTANCE);
		registry.put(XMLTypePackage.eNS_URI, XMLTypePackage.eINSTANCE);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("rdf",
				new RdfResourceFactoryImpl());
		return resourceSet;
	}
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.identity.ForeignIdentityException;
import org.eclipse.fennec.dcat.atlas.api.integrity.ResourceInUseException;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.dcat.atlas.api.integrity.DanglingReferenceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;

/**
 * Round-trips the file-backed catalog store, and pins the two things the storage
 * rework changed: identities are logical and minted by the store, and membership
 * is an EMF cross-resource reference rather than a copied IRI.
 */
public class CatalogAdminServiceImplTest {

	private static final String CATALOGS = StoreLayout.LOGICAL_BASE + "catalogs/";
	private static final String DATASETS = StoreLayout.LOGICAL_BASE + "datasets/";
	private static final String SERVICES = StoreLayout.LOGICAL_BASE + "data-services/";

	@TempDir
	Path storage;

	private CatalogAdminServiceImpl service() {
		return new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
	}

	private DatasetAdminServiceImpl datasets() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
	}

	// --- storage round trip -------------------------------------------------

	@Test
	void upsertThenGetReturnsEquivalentCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		Optional<Catalog> loaded = service.getCatalog("gov");
		assertTrue(loaded.isPresent());
		assertEquals(CATALOGS + "gov", loaded.get().getAbout());
		assertEquals("GovData", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.upsertCatalog(catalog(CATALOGS + "eu", "EU Portal"));

		assertEquals(2, service.listCatalogs().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getCatalog("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheCatalog() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.deleteCatalog("gov", false);
		assertTrue(service.getCatalog("gov").isEmpty());
	}

	// --- identity -----------------------------------------------------------

	@Test
	void theStoreMintsAnIdWhenNoneIsSupplied() {
		CatalogAdminServiceImpl service = service();
		Catalog stored = service.upsertCatalog(catalog(null, "Untitled about"));

		assertEquals(1, service.listCatalogs().size());
		assertTrue(stored.getAbout().startsWith(CATALOGS), stored.getAbout());
	}

	/**
	 * A foreign {@code about} is refused here, at the persistence boundary, not merely by
	 * the REST adapter above it.
	 * <p>
	 * It used to be answered with a minted id — which did keep the store safe (the trap
	 * being to file this under {@code air} and serve somebody else's URL as an identity of
	 * ours) but told the caller nothing, so it believed its chosen identity had been kept.
	 * That made the two doors disagree: {@code POST /admin/catalogs} refused the same body
	 * with {@code 400} while this accepted it. The service is the boundary every consumer
	 * shares — an importer, a migration, another bundle — so the contract has to be the
	 * same whichever way in you come.
	 */
	@Test
	void aForeignAboutIsRefusedRatherThanQuietlyMinted() {
		CatalogAdminServiceImpl service = service();

		ForeignIdentityException thrown = assertThrows(ForeignIdentityException.class,
				() -> service.upsertCatalog(catalog("https://someone-else.example/datasets/air", "Foreign")));

		assertTrue(thrown.getMessage().contains("https://someone-else.example/datasets/air"), thrown.getMessage());
		assertTrue(service.getCatalog("air").isEmpty(), "must not be filed under a foreign URL's last segment");
		assertTrue(service.listCatalogs().isEmpty(), "a refused write must store nothing at all");
	}

	/**
	 * The public IRI is the sharpest case, because it is one of *ours* — but the
	 * public→logical fold lives in {@code PublicIriFilter}, i.e. in the REST layer, so a
	 * direct caller never gets it. Refusing says so instead of minting a second identity
	 * for a resource that already has one.
	 */
	@Test
	void aPublicIriIsRefusedToADirectCaller() {
		CatalogAdminServiceImpl service = service();

		assertThrows(ForeignIdentityException.class,
				() -> service.upsertCatalog(catalog("http://localhost:8085/dcat/rest/catalogs/gov", "GovData")));

		assertTrue(service.listCatalogs().isEmpty());
	}

	@Test
	void theStoredIdentityIsLogicalNotTheRequestHost() throws Exception {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		String stored = TestGitStore.stored(storage, StoreLayout.CATALOGS, "gov");
		assertTrue(stored.contains("about=\"" + CATALOGS + "gov\""), stored);
	}

	// --- FR-9 catalog membership -------------------------------------------

	@Test
	void addAndRemoveDatasetMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		service.addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getDataset().size());
		assertEquals(DATASETS + "air", stored.getDataset().get(0).getAbout());

		service.deleteDatasetFromCatalog("gov", "air");
		assertTrue(service.getCatalog("gov").get().getDataset().isEmpty());
	}

	@Test
	void membershipIsAnHrefNotACopyOfTheDataset() throws Exception {
		// The whole reason membership is a cross-resource reference: one description of
		// the dataset, in its own file, however many catalogs list it.
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		String stored = TestGitStore.stored(storage, StoreLayout.CATALOGS, "gov");
		assertTrue(stored.contains("href=\"" + DATASETS + "air#/\""), stored);
		assertFalse(stored.contains("Air quality"), "the dataset must not be copied into the catalog: " + stored);
	}

	@Test
	void addStoresTheDatasetInItsOwnCollection() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		assertTrue(datasets().getDataset("air").isPresent());
	}

	@Test
	void linkRequiresTheDatasetToExistAlready() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		assertThrows(NoSuchElementException.class, () -> service.linkDatasetToCatalog("gov", "air"));

		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		Catalog stored = service.linkDatasetToCatalog("gov", "air");
		assertEquals(1, stored.getDataset().size());
	}

	@Test
	void linkIsIdempotentAndLeavesTheEtagAlone() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));

		service.linkDatasetToCatalog("gov", "air");
		String first = service.etag("gov").orElseThrow();
		service.linkDatasetToCatalog("gov", "air");

		assertEquals(1, service.getCatalog("gov").get().getDataset().size());
		assertEquals(first, service.etag("gov").orElseThrow());
	}

	@Test
	void oneDatasetCanBeListedByTwoCatalogs() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.upsertCatalog(catalog(CATALOGS + "eu", "EU Portal"));
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));

		service.linkDatasetToCatalog("gov", "air");
		service.linkDatasetToCatalog("eu", "air");

		assertEquals(DATASETS + "air", service.getCatalog("gov").get().getDataset().get(0).getAbout());
		assertEquals(DATASETS + "air", service.getCatalog("eu").get().getDataset().get(0).getAbout());
	}

	@Test
	void addAndRemoveServiceMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		service.addDataServiceToCatalog("gov", dataService(SERVICES + "sparql"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getService().size());
		assertEquals(SERVICES + "sparql", stored.getService().get(0).getAbout());

		service.deleteDataServiceFromCatalog("gov", "sparql");
		assertTrue(service.getCatalog("gov").get().getService().isEmpty());
	}

	@Test
	void addAndRemoveSubCatalogMembership() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));

		service.addSubCatalogToCatalog("gov", catalog(CATALOGS + "eu", "EU Portal"));

		Catalog stored = service.getCatalog("gov").get();
		assertEquals(1, stored.getCatalog().size());
		assertEquals(CATALOGS + "eu", stored.getCatalog().get(0).getAbout());

		service.deleteSubCatalogFromCatalog("gov", "eu");
		assertTrue(service.getCatalog("gov").get().getCatalog().isEmpty());
	}

	@Test
	void membershipOnUnknownCatalogThrows() {
		CatalogAdminServiceImpl service = service();
		assertThrows(NoSuchElementException.class,
				() -> service.addDatasetToCatalog("missing", dataset(DATASETS + "air", "Air quality")));
	}

	// --- FR-1 delete blocking ----------------------------------------------

	@Test
	void deletingAReferencedDatasetIsRefused() {
		// Deleting anyway would leave the catalog pointing at nothing, which EMF
		// tolerates silently — and then serializing the *catalog* to RDF fails.
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		ResourceInUseException thrown = assertThrows(ResourceInUseException.class,
				() -> datasets().deleteDataset("air", false));

		assertEquals(List.of(CATALOGS + "gov"), thrown.getReferencedBy());
		assertTrue(datasets().getDataset("air").isPresent(), "the refused delete must not have removed it");
	}

	@Test
	void cascadeUnlinksThenDeletes() {
		CatalogAdminServiceImpl service = service();
		service.upsertCatalog(catalog(CATALOGS + "gov", "GovData"));
		service.addDatasetToCatalog("gov", dataset(DATASETS + "air", "Air quality"));

		datasets().deleteDataset("air", true);

		assertTrue(datasets().getDataset("air").isEmpty());
		assertTrue(service.getCatalog("gov").get().getDataset().isEmpty(),
				"cascade must unlink, not leave a dangling reference");
	}

	@Test
	void anUnreferencedDatasetDeletesWithoutCascade() {
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		datasets().deleteDataset("air", false);
		assertTrue(datasets().getDataset("air").isEmpty());
	}

	// --- fixtures -----------------------------------------------------------

	// --- referential integrity on write (the mirror of FR-1's delete side) ---

	@Test
	void upsertRejectsAReferenceToAnIdentityOfOursThatDoesNotExist() {
		CatalogAdminServiceImpl service = service();
		Catalog catalog = catalog(CATALOGS + "gov", "GovData");
		// A link to an identity under our base that was never stored. Left alone it becomes
		// an unresolvable proxy: `about == null` on load, and EObjectToJena then throws while
		// serializing the *catalog* — a 500 on a resource that looks fine. FR-1 refuses this
		// on the delete side; this is the same rule on the write side.
		catalog.getDataset().add(proxyTo(DATASETS + "air"));

		assertThrows(DanglingReferenceException.class, () -> service.upsertCatalog(catalog));
		assertTrue(service.getCatalog("gov").isEmpty(), "nothing should have been stored");
	}

	@Test
	void upsertAcceptsAReferenceToAnIdentityOfOursThatExists() {
		CatalogAdminServiceImpl service = service();
		datasets().upsertDataset(dataset(DATASETS + "air", "Air quality"));
		Catalog catalog = catalog(CATALOGS + "gov", "GovData");
		catalog.getDataset().add(proxyTo(DATASETS + "air"));

		service.upsertCatalog(catalog);

		assertEquals(1, service.getCatalog("gov").orElseThrow().getDataset().size());
		assertEquals(DATASETS + "air", service.getCatalog("gov").orElseThrow().getDataset().get(0).getAbout());
	}

	@Test
	void upsertLeavesAForeignReferenceAlone() {
		CatalogAdminServiceImpl service = service();
		Catalog catalog = catalog(CATALOGS + "gov", "GovData");
		// Not an identity we own — we cannot know whether it resolves, and it is not ours
		// to refuse. Themes, publishers and licences are all like this.
		catalog.getDataset().add(proxyTo("https://someone-else.example/datasets/air"));

		service.upsertCatalog(catalog);

		assertTrue(service.getCatalog("gov").isPresent());
	}

	/** A link exactly as the XMI reader leaves one: a proxy at the target's identity. */
	private static Dataset proxyTo(String iri) {
		Dataset link = DcatFactory.eINSTANCE.createDataset();
		link.setAbout(iri);
		((org.eclipse.emf.ecore.InternalEObject) link)
				.eSetProxyURI(org.eclipse.emf.common.util.URI.createURI(iri).appendFragment("/"));
		return link;
	}

	private static Dataset dataset(String about, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(about);
		return TestEntities.mandatoryDataset(dataset, title);
	}

	private static DataService dataService(String about) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(about);
		return TestEntities.mandatoryDataService(service, "Service");
	}

	private static Catalog catalog(String about, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		if (about != null) {
			catalog.setAbout(about);
		}
		return TestEntities.mandatoryDataset(catalog, title);
	}

}

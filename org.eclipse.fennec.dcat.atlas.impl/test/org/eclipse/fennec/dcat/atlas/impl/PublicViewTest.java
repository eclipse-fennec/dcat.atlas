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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicView;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout;
import org.eclipse.fennec.dcat.atlas.impl.helper.StoreResourceSets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;
import dcat.DcatFactory;
import dcat.Dataset;
import dcat.Distribution;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * What a client receives is the stored document with our identities rendered
 * public — same XMI, same shape, different base. Nothing about where the bytes
 * live may appear in either.
 */
class PublicViewTest {

	private static final String LOGICAL = StoreLayout.LOGICAL_BASE;
	private static final String PUBLIC = "https://opendata.example.de/dcat/rest/";

	private final org.eclipse.fennec.dcat.atlas.api.identity.PublicIris iris = new PublicIrisImpl(PUBLIC);

	@TempDir
	Path storage;

	@Test
	void theRenderedDocumentIsTheStoredOneWithOnlyTheBaseChanged() throws Exception {
		CatalogAdminServiceImpl catalogs = new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		catalogs.upsertCatalog(catalog("gov", "GovData"));
		catalogs.addDatasetToCatalog("gov", dataset("air", "Air quality"));

		String stored = TestGitStore.stored(storage, StoreLayout.CATALOGS, "gov");
		String rendered = toXmi(PublicView.render(catalogs.getCatalog("gov").orElseThrow(), iris));

		assertEquals(stored.replace(LOGICAL, PUBLIC), rendered,
				"the wire form must be the stored form with identities rebased, nothing else");
	}

	@Test
	void noFileUriAppearsInEitherForm() throws Exception {
		CatalogAdminServiceImpl catalogs = new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		catalogs.upsertCatalog(catalog("gov", "GovData"));
		catalogs.addDatasetToCatalog("gov", dataset("air", "Air quality"));

		String stored = TestGitStore.stored(storage, StoreLayout.CATALOGS, "gov");
		String rendered = toXmi(PublicView.render(catalogs.getCatalog("gov").orElseThrow(), iris));

		// EMF deresolves hrefs against the saving resource by default, which would turn
		// an identity into "../datasets/air" — a storage path in a served document.
		assertFalse(stored.contains("file:") || stored.contains("../"), stored);
		assertFalse(rendered.contains("file:") || rendered.contains("../"), rendered);
		assertFalse(rendered.contains(storage.toString()), rendered);
	}

	@Test
	void identitiesAndLinksAreBothPublic() {
		CatalogAdminServiceImpl catalogs = new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		catalogs.upsertCatalog(catalog("gov", "GovData"));
		catalogs.addDatasetToCatalog("gov", dataset("air", "Air quality"));

		Catalog rendered = PublicView.render(catalogs.getCatalog("gov").orElseThrow(), iris);

		assertEquals(PUBLIC + "catalogs/gov", rendered.getAbout());
		// The link carries `about` for the RDF converter and a proxy URI for the XMI
		// writer; the two serializers read different things and both must be right.
		assertEquals(PUBLIC + "datasets/air", rendered.getDataset().get(0).getAbout());
		assertEquals(PUBLIC + "datasets/air#/",
				((org.eclipse.emf.ecore.InternalEObject) rendered.getDataset().get(0)).eProxyURI().toString());
	}

	@Test
	void containedObjectsAreRebasedToo() {
		DatasetAdminServiceImpl datasets = new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		DistributionAdminServiceImpl distributions = new DistributionAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH, datasets);
		datasets.upsertDataset(dataset("air", "Air quality"));
		distributions.upsertDistributionToDataset("air", distribution("csv"));

		Dataset rendered = PublicView.render(datasets.getDataset("air").orElseThrow(), iris);

		assertEquals(PUBLIC + "datasets/air/distributions/csv", rendered.getDistribution().get(0).getAbout());
	}

	@Test
	void aForeignIriSurvivesRendering() {
		DatasetAdminServiceImpl datasets = new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		Dataset air = dataset("air", "Air quality");
		air.getTheme().add("http://publications.europa.eu/resource/authority/data-theme/ENVI");
		datasets.upsertDataset(air);

		Dataset rendered = PublicView.render(datasets.getDataset("air").orElseThrow(), iris);

		assertEquals("http://publications.europa.eu/resource/authority/data-theme/ENVI",
				rendered.getTheme().get(0));
	}

	@Test
	void renderingDoesNotDisturbTheStoredEntity() {
		CatalogAdminServiceImpl catalogs = new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage), TestGitStore.BASE_PATH);
		catalogs.upsertCatalog(catalog("gov", "GovData"));

		Catalog stored = catalogs.getCatalog("gov").orElseThrow();
		PublicView.render(stored, iris);

		// Rendering must not write the public host back into data kept host-free.
		assertEquals(LOGICAL + "catalogs/gov", stored.getAbout());
		assertTrue(catalogs.getCatalog("gov").orElseThrow().getAbout().startsWith(LOGICAL));
	}

	// --- helpers ------------------------------------------------------------

	/** Serializes exactly as the store does, so the comparison is like for like. */
	private static String toXmi(EObject entity) throws Exception {
		ResourceSet resourceSet = TestResourceSets.newResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI("urn:render"));
		resource.getContents().add(entity);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		resource.save(out, StoreResourceSets.saveOptions());
		return out.toString(StandardCharsets.UTF_8);
	}

	// --- fold: what a client sends becomes what we store --------------------

	@Test
	void foldTurnsAnInlineMemberIntoAReference() {
		// A client that writes the member out in full, rather than as an href, still means
		// the identity its `about` names. XMI cannot express an inline object under a
		// non-containment reference: EMF falls back to a same-document IDREF, which resolves
		// to nothing on the next read — the membership is silently lost. So an inline object
		// bearing an identity of ours has to become a proxy before it reaches the store.
		Catalog catalog = catalog("gov", "GovData");
		catalog.getDataset().add(dataset("air", "Air quality"));

		PublicView.fold(catalog, iris);

		Dataset link = catalog.getDataset().get(0);
		assertTrue(link.eIsProxy(), "an inline member of ours must be folded into a proxy");
		assertEquals(LOGICAL + "datasets/air#/",
				((org.eclipse.emf.ecore.InternalEObject) link).eProxyURI().toString());
		assertEquals(LOGICAL + "datasets/air", link.getAbout());
	}

	@Test
	void foldTurnsAnInlinePublicMemberIntoALogicalReference() {
		Catalog catalog = catalog("gov", "GovData");
		Dataset inline = DcatFactory.eINSTANCE.createDataset();
		inline.setAbout(PUBLIC + "datasets/air");
		inline.getTitle().add(literal("Air quality"));
		catalog.getDataset().add(inline);

		PublicView.fold(catalog, iris);

		Dataset link = catalog.getDataset().get(0);
		assertTrue(link.eIsProxy());
		assertEquals(LOGICAL + "datasets/air#/",
				((org.eclipse.emf.ecore.InternalEObject) link).eProxyURI().toString());
	}

	@Test
	void foldLeavesAForeignInlineMemberAlone() {
		// Not an identity we own, so we have nothing to point at — leave it as it came and
		// let the store decide. Folding it would claim a resource that is not ours.
		Catalog catalog = catalog("gov", "GovData");
		Dataset foreign = DcatFactory.eINSTANCE.createDataset();
		foreign.setAbout("https://someone-else.example/datasets/air");
		catalog.getDataset().add(foreign);

		PublicView.fold(catalog, iris);

		assertFalse(catalog.getDataset().get(0).eIsProxy());
		assertEquals("https://someone-else.example/datasets/air", catalog.getDataset().get(0).getAbout());
	}

	/**
	 * An {@code href} sent without the {@code #/} fragment means the same resource, and is
	 * normalised to the stored form. The fragment is XMI pointer syntax — a document URL
	 * plus the object inside it — not part of the identity, so a client that writes the
	 * plain IRI (the form our own RDF renders, and the only form a non-EMF client would
	 * think to send) must not have its membership dropped. {@code targetIri} trims the
	 * fragment before deciding ownership, which is what makes both forms arrive here alike.
	 */
	@Test
	void foldAcceptsAnHrefWithoutTheFragment() {
		Catalog catalog = catalog("gov", "GovData");
		Dataset link = DcatFactory.eINSTANCE.createDataset();
		((org.eclipse.emf.ecore.InternalEObject) link).eSetProxyURI(URI.createURI(PUBLIC + "datasets/air"));
		catalog.getDataset().add(link);

		PublicView.fold(catalog, iris);

		assertEquals(LOGICAL + "datasets/air#/",
				((org.eclipse.emf.ecore.InternalEObject) catalog.getDataset().get(0)).eProxyURI().toString(),
				"a fragment-less href must fold to the same reference as one carrying #/");
	}

	private static Catalog catalog(String id, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(LOGICAL + "catalogs/" + id);
		return TestEntities.mandatoryDataset(catalog, title);
	}

	private static Dataset dataset(String id, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(LOGICAL + "datasets/" + id);
		return TestEntities.mandatoryDataset(dataset, title);
	}

	private static Distribution distribution(String id) {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(StoreLayout.distributionIri("air", id));
		distribution.setTitle(TestEntities.literal("CSV download"));
		return TestEntities.mandatoryDistribution(distribution);
	}

	private static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(value);
		return literal;
	}
}

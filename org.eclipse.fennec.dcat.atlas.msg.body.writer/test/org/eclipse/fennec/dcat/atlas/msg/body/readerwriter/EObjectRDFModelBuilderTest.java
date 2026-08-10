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
package org.eclipse.fennec.dcat.atlas.msg.body.readerwriter;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xml.namespace.XMLNamespacePackage;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import adms.AdmsPackage;
import adms.impl.AdmsPackageImpl;
import dcat.Catalog;
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
 * Exercises the EMF-to-Jena conversion at the heart of every RDF writer: it
 * proves that the DCAT-AP model's own RDF/XML serialization is understood by
 * Apache Jena and yields the expected triples (real vocabulary URIs, subjects
 * from {@code rdf:about}, language-tagged literals).
 */
public class EObjectRDFModelBuilderTest {

	private static final String DCAT_NS = "http://www.w3.org/ns/dcat#";
	private static final String DCT_TITLE = "http://purl.org/dc/terms/title";

	private ResourceSet resourceSet;

	@BeforeEach
	void setup() {
		resourceSet = newResourceSet();
	}

	static ResourceSet newResourceSet() {
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

	@Test
	void singleCatalogBecomesTypedRdfSubject() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("https://govdata.de#catalog");
		catalog.getTitle().add(literal("de", "GovData"));
		catalog.getDescription().add(literal("de", "Das Datenportal für Deutschland"));

		Model model = EObjectRDFModelBuilder.toModel(catalog, resourceSet);

		Resource subject = model.getResource("https://govdata.de#catalog");
		Property title = model.getProperty(DCT_TITLE);
		assertAll(//
				() -> assertFalse(model.isEmpty(), "model should contain triples"),
				() -> assertTrue(
						model.contains(subject, RDF.type, model.getResource(DCAT_NS + "Catalog")),
						"catalog should be typed dcat:Catalog"),
				() -> assertTrue(model.contains(subject, title), "catalog should have a dct:title"),
				() -> assertEquals("GovData", model.getProperty(subject, title).getString()));
	}

	@Test
	void datasetCollectionKeepsEveryMember() {
		Dataset first = DcatFactory.eINSTANCE.createDataset();
		first.setAbout("https://example.org/dataset/1");
		first.getTitle().add(literal("en", "First dataset"));
		Dataset second = DcatFactory.eINSTANCE.createDataset();
		second.setAbout("https://example.org/dataset/2");
		second.getTitle().add(literal("en", "Second dataset"));

		Model model = EObjectRDFModelBuilder.toModel(List.of(first, second), resourceSet);

		Resource datasetType = model.getResource(DCAT_NS + "Dataset");
		assertAll(//
				() -> assertTrue(model.contains(model.getResource("https://example.org/dataset/1"),
						RDF.type, datasetType)),
				() -> assertTrue(model.contains(model.getResource("https://example.org/dataset/2"),
						RDF.type, datasetType)),
				() -> assertEquals(2, model.listResourcesWithProperty(RDF.type, datasetType).toList().size()));
	}

	@Test
	void languageTagIsPreserved() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("https://govdata.de#catalog");
		catalog.getTitle().add(literal("de", "GovData"));

		Model model = EObjectRDFModelBuilder.toModel(catalog, resourceSet);

		RDFNode titleNode = model.getProperty(model.getResource("https://govdata.de#catalog"),
				model.getProperty(DCT_TITLE)).getObject();
		assertTrue(titleNode.isLiteral());
		assertEquals("de", titleNode.asLiteral().getLanguage());
	}

	@Test
	void emptyContentIsRejected() {
		assertThrows(IllegalArgumentException.class,
				() -> EObjectRDFModelBuilder.toModel(List.<EObject>of(), resourceSet));
	}

	@Test
	void rdfXmlWriteThenParseRoundTrips() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("https://govdata.de/catalogs/gov");
		catalog.getTitle().add(literal("de", "GovData"));

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EObjectRDFModelBuilder.writeRdfXml(catalog, newResourceSet(), out);

		List<EObject> parsed = EObjectRDFModelBuilder.parse(
				new ByteArrayInputStream(out.toByteArray()), newResourceSet());
		assertEquals(1, parsed.size());
		Catalog roundTripped = (Catalog) parsed.get(0);
		assertEquals("https://govdata.de/catalogs/gov", roundTripped.getAbout());
		assertEquals("GovData", roundTripped.getTitle().get(0).getValue());
	}

	@Test
	void parseExtractsEveryEntity() {
		Dataset first = DcatFactory.eINSTANCE.createDataset();
		first.setAbout("https://example.org/dataset/1");
		Dataset second = DcatFactory.eINSTANCE.createDataset();
		second.setAbout("https://example.org/dataset/2");

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EObjectRDFModelBuilder.writeRdfXml(List.of(first, second), newResourceSet(), out);

		List<EObject> parsed = EObjectRDFModelBuilder.parse(
				new ByteArrayInputStream(out.toByteArray()), newResourceSet());
		assertEquals(2, parsed.size());
	}

	private static PlainLiteral literal(String lang, String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang(lang);
		literal.setValue(value);
		return literal;
	}
}

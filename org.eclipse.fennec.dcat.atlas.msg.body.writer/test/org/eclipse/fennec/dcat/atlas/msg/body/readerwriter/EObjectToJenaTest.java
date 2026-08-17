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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.DcatPackage;
import dcat.Dataset;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafPackage;
import foaf.FoafFactory;
import org.eclipse.emf.ecore.EObject;

import rdf.PlainLiteral;
import rdf.RdfPackage;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsPackage;
import terms.TermsFactory;

/**
 * The contract of the EMF&rarr;Jena converter.
 * <p>
 * Everything is asserted on the <em>graph</em>, never on serialized text, and
 * links are asserted on the parsed triple's <b>object</b> rather than on the
 * triple merely existing — the failure mode this guards is silent, producing a
 * blank node or a string literal where an IRI belongs.
 */
class EObjectToJenaTest {

	private static final String BASE = "http://example.org/rest/";
	private static final String CATALOG = BASE + "catalogs/c1";
	private static final String DATASET = BASE + "datasets/d1";
	private static final String DISTRIBUTION = BASE + "datasets/d1/distributions/x1";
	private static final String PUBLISHER = BASE + "organizations/uba";
	private static final String LICENSE = "http://dcat-ap.de/def/licenses/dl-by-de/2.0";
	private static final String CONTRIBUTOR = "http://dcat-ap.de/def/contributors/uba";
	private static final String THEME = "http://publications.europa.eu/resource/authority/data-theme/ENVI";
	private static final String MEDIA_TYPE = "http://www.iana.org/assignments/media-types/text/csv";
	private static final String PACKAGE_FORMAT = "http://publications.europa.eu/resource/authority/file-type/ZIP";
	private static final String ENDPOINT_URL = "https://example.org/api/luftqualitaet";
	private static final String ENDPOINT_DESCRIPTION = "https://example.org/api/luftqualitaet/openapi.json";

	private static final String DCAT = "http://www.w3.org/ns/dcat#";
	private static final String DCT = "http://purl.org/dc/terms/";
	private static final String FOAF = "http://xmlns.com/foaf/0.1/";
	private static final String DCATDE = "http://dcat-ap.de/def/dcatde/";

	// --- identity and typing ------------------------------------------------

	@Test
	void anEntityIsIdentifiedByItsAbout() {
		Model model = EObjectToJena.toModel(catalog());

		assertTrue(model.contains(model.createResource(CATALOG), model.createProperty(DCT + "title")),
				() -> "the catalog's triples must hang off its rdf:about IRI, not a blank node\n" + dump(model));
	}

	@Test
	void aboutIsTheSubjectAndNeverAPredicate() {
		Model model = EObjectToJena.toModel(catalog());

		assertFalse(model.listStatements(null, model.createProperty(RDF.getURI() + "about"), (RDFNode) null).hasNext(),
				() -> "rdf:about is RDF/XML syntax for the node's identity, not a property\n" + dump(model));
	}

	@Test
	void rdfTypeComesFromTheModelAnnotations() {
		Model model = EObjectToJena.toModel(catalog());

		assertTrue(model.contains(model.createResource(CATALOG), RDF.type, model.createResource(DCAT + "Catalog")),
				() -> dump(model));
	}

	// --- the AnyURI rule ----------------------------------------------------

	@Test
	void anyUriAttributesBecomeIriNodesNotLiterals() {
		Dataset dataset = dataset();
		dataset.getTheme().add(THEME);
		dataset.getContributorID().add(CONTRIBUTOR);

		Model model = EObjectToJena.toModel(dataset);

		// The silent failure this guards: emitting the IRI as a string literal is
		// valid RDF and produces the wrong graph with no error.
		assertObjectIri(model, DATASET, DCAT + "theme", THEME, "dcat:theme");
		assertObjectIri(model, DATASET, DCATDE + "contributorID", CONTRIBUTOR, "dcatde:contributorID");
	}

	/**
	 * The datatype decides, and nothing else does — in particular not the Java type, which
	 * is {@code String} on both sides of this test.
	 * <p>
	 * {@code XMLType#//AnyURI} maps to {@code java.lang.String} exactly as
	 * {@code XMLType#//String} does, so {@code getVersion()} and {@code getHasVersion()} are
	 * indistinguishable to a caller. What separates them is the {@code EDataType} the feature
	 * points at: {@code objectOf} compares it against {@code XMLTypePackage.Literals.ANY_URI}
	 * by identity. That is why retyping a feature to the stock {@code AnyURI} is enough to
	 * move it onto the IRI branch, and why a purpose-built datatype with a URI-ish
	 * {@code instanceClassName} would be the wrong fix — it would not be the instance this
	 * comparison tests for, and would land back on the literal branch.
	 */
	@Test
	void theEmfDatatypeAloneDecidesIriOrLiteral() {
		Dataset dataset = dataset();
		dataset.setVersion("1.2.3"); // XMLType#//String  -> literal
		dataset.getHasVersion().add(BASE + "datasets/d0"); // XMLType#//AnyURI -> IRI

		Model model = EObjectToJena.toModel(dataset);

		assertObjectIri(model, DATASET, DCAT + "hasVersion", BASE + "datasets/d0", "dcat:hasVersion");
		List<RDFNode> version = model
				.listObjectsOfProperty(model.createResource(DATASET), model.createProperty(DCAT + "version")).toList();
		assertEquals(1, version.size(), () -> dump(model));
		assertTrue(version.get(0).isLiteral(), () -> "dcat:version is a String feature and must stay a literal\n"
				+ dump(model));
		assertEquals("1.2.3", version.get(0).asLiteral().getString(), () -> dump(model));
	}

	/**
	 * The four features that were typed {@code String} and therefore emitted as literals
	 * where the DCAT-AP.de shapes require IRIs (`sh:NodeKindConstraintComponent`). The
	 * effect on {@code dcat:DataService} was total: {@code dcat:endpointURL} is mandatory,
	 * so a service that omitted it failed the cardinality rule and one that supplied it
	 * failed the nodekind rule — no DataService could be written at all under FR-4
	 * enforcement. Retyped to {@code XMLType#//AnyURI} in {@code dcatap.ecore} and
	 * regenerated; pinned here per feature, because the datatype is invisible in the
	 * generated Java (all four are still {@code String}) and a later regeneration could
	 * silently put it back.
	 */
	@Test
	void mediaTypeAndPackageFormatAreIris() {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DISTRIBUTION);
		distribution.getMediaType().add(MEDIA_TYPE);
		distribution.getPackageFormat().add(PACKAGE_FORMAT);

		Model model = EObjectToJena.toModel(distribution);

		assertObjectIri(model, DISTRIBUTION, DCAT + "mediaType", MEDIA_TYPE, "dcat:mediaType");
		assertObjectIri(model, DISTRIBUTION, DCAT + "packageFormat", PACKAGE_FORMAT, "dcat:packageFormat");
	}

	@Test
	void endpointUrlAndDescriptionAreIris() {
		dcat.DataService service = dataService();
		service.getEndpointURL().add(ENDPOINT_URL);
		service.getEndpointDescription().add(ENDPOINT_DESCRIPTION);

		Model model = EObjectToJena.toModel(service);

		assertObjectIri(model, BASE + "data-services/s1", DCAT + "endpointURL", ENDPOINT_URL, "dcat:endpointURL");
		assertObjectIri(model, BASE + "data-services/s1", DCAT + "endpointDescription", ENDPOINT_DESCRIPTION,
				"dcat:endpointDescription");
	}

	@Test
	void plainLiteralsKeepTheirLanguageTag() {
		Model model = EObjectToJena.toModel(dataset());

		assertTrue(model.contains(model.createResource(DATASET), model.createProperty(DCT + "title"),
				model.createLiteral("Air quality", "en")), () -> dump(model));
	}

	// --- references ---------------------------------------------------------

	@Test
	void aNonContainmentReferenceIsALinkNotACopy() {
		Catalog catalog = catalog();
		catalog.getDataset().add(dataset());

		Model model = EObjectToJena.toModel(catalog);

		assertObjectIri(model, CATALOG, DCAT + "dataset", DATASET, "dcat:dataset");
		// The dataset is stored and served in its own right; inlining it here is the
		// duplication problem the pointer convention existed to prevent.
		assertFalse(model.contains(model.createResource(DATASET), model.createProperty(DCT + "title")),
				() -> "a linked dataset must not be inlined into the catalog's graph\n" + dump(model));
	}

	@Test
	void aContainmentReferenceIsInlinedUnderItsOwnIri() {
		Model model = EObjectToJena.toModel(datasetWithDistribution());

		assertObjectIri(model, DATASET, DCAT + "distribution", DISTRIBUTION, "dcat:distribution");
		assertTrue(model.contains(model.createResource(DISTRIBUTION), model.createProperty(DCT + "title")),
				() -> "a contained distribution must be emitted, under its own IRI\n" + dump(model));
	}

	@Test
	void linkingSomethingWithoutAnAboutFails() {
		Catalog catalog = catalog();
		Dataset anonymous = DcatFactory.eINSTANCE.createDataset();
		anonymous.getTitle().add(literal("No identity", "en"));
		catalog.getDataset().add(anonymous);

		// Loud, not silent: a link to a resource with no IRI has nothing to point at.
		IllegalStateException thrown = assertThrows(IllegalStateException.class,
				() -> EObjectToJena.toModel(catalog));
		assertTrue(thrown.getMessage().contains("rdf:about"), thrown.getMessage());
	}

	// --- the identity-bearing features that regressed under N28 --------------

	@Test
	void publisherKeepsItsIri() {
		Dataset dataset = dataset();
		Agent publisher = FoafFactory.eINSTANCE.createAgent();
		publisher.setAbout(PUBLISHER);
		publisher.getName().add(literal("Umweltbundesamt", "de"));
		dataset.setPublisher(publisher);

		Model model = EObjectToJena.toModel(dataset);

		assertObjectIri(model, DATASET, DCT + "publisher", PUBLISHER, "dct:publisher");
		assertTrue(model.contains(model.createResource(PUBLISHER), model.createProperty(FOAF + "name")),
				() -> "the publisher's own triples must hang off its IRI\n" + dump(model));
	}

	@Test
	void licenseKeepsItsIri() {
		Dataset dataset = dataset();
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout(LICENSE);
		dataset.setLicense(license);

		Model model = EObjectToJena.toModel(dataset);

		assertObjectIri(model, DATASET, DCT + "license", LICENSE, "dct:license");
	}

	// --- IRI validation -----------------------------------------------------

	@Test
	void aMalformedAboutIsRejectedRatherThanEmitted() {
		Catalog broken = DcatFactory.eINSTANCE.createCatalog();
		broken.setAbout("http://x/catalogs/bro ken");

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> EObjectToJena.toModel(broken));

		// Jena's createResource validates nothing, so an unchecked space would project
		// into the graph and serve through Turtle, N-Triples and JSON-LD — the last one
		// raw and unescaped — blowing up only in the RDF/XML writer at response time.
		assertTrue(thrown.getMessage().contains("Catalog.about"), thrown.getMessage());
	}

	/**
	 * An {@code AnyURI} value that is not an absolute IRI becomes a <em>literal</em>, not
	 * an error.
	 * <p>
	 * It used to throw. That was wrong in both directions: RDF lets the same property take
	 * an IRI on one class and a literal on another (the shapes require an IRI for
	 * {@code dcterms:type} on a Dataset and leave it free on a Catalog — the Jena city
	 * portal publishes {@code dct:type "ckan"} and validates clean), while an
	 * {@code EAttribute} cannot express that split. So a value the profile permits could
	 * be stored and then made every RDF read of the resource a 500, while XMI reads kept
	 * working and hid it. Whether an IRI was required is SHACL's question, and FR-4 already
	 * asks it on write.
	 */
	@Test
	void anAnyUriAttributeWithoutASchemeBecomesALiteral() {
		Dataset dataset = dataset();
		dataset.getTheme().add("not-an-iri");

		Model model = EObjectToJena.toModel(dataset);

		List<RDFNode> themes = model
				.listObjectsOfProperty(model.createResource(DATASET), model.createProperty(DCAT + "theme")).toList();
		assertEquals(1, themes.size(), () -> dump(model));
		assertTrue(themes.get(0).isLiteral(), () -> "a value with no scheme cannot be an IRI node\n" + dump(model));
		assertEquals("not-an-iri", themes.get(0).asLiteral().getString(), () -> dump(model));
	}

	/**
	 * And it survives the round trip, which is the point of preferring a literal to an
	 * exception: {@code JenaToEObject} reads an IRI node or a literal into an
	 * {@code AnyURI} attribute alike, so nothing is lost either way.
	 */
	@Test
	void aNonIriAnyUriValueRoundTrips() {
		Dataset dataset = dataset();
		dataset.getTheme().add("not-an-iri");

		assertRoundTrips(dataset);
	}

	/**
	 * The boundary: only attribute *objects* may degrade. A subject must be a real IRI —
	 * RDF has no literal subjects — so a malformed {@code about} still fails loudly rather
	 * than being emitted as something else.
	 */
	@Test
	void aRelativeAboutIsStillRejected() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("catalogs/no-scheme");

		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> EObjectToJena.toModel(catalog));

		assertTrue(thrown.getMessage().contains("Catalog.about"), thrown.getMessage());
	}

	@Test
	void anIriWithAFragmentIsAccepted() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("https://govdata.de#catalog");

		Model model = EObjectToJena.toModel(catalog);

		// Guards the guard: RFC 3986's "absolute-URI" production excludes a fragment, so
		// validating with isAbsolute() would reject this perfectly good identifier. What
		// RDF requires is a scheme, which is what the check actually tests.
		assertTrue(model.contains(model.createResource("https://govdata.de#catalog"), RDF.type,
				model.createResource(DCAT + "Catalog")));
	}

	@Test
	void aCollectionOfEntitiesLandsInOneGraph() {
		Model model = EObjectToJena.toModel(List.of(catalog(), dataset()));

		assertTrue(model.contains(model.createResource(CATALOG), RDF.type, model.createResource(DCAT + "Catalog")));
		assertTrue(model.contains(model.createResource(DATASET), RDF.type, model.createResource(DCAT + "Dataset")));
	}

	// --- round trip: EMF -> Jena -> EMF -> Jena -----------------------------

	@Test
	void aFullyPopulatedDatasetRoundTrips() {
		Dataset dataset = datasetWithDistribution();
		dataset.getTheme().add(THEME);
		dataset.getContributorID().add(CONTRIBUTOR);
		Agent publisher = FoafFactory.eINSTANCE.createAgent();
		publisher.setAbout(PUBLISHER);
		publisher.getName().add(literal("Umweltbundesamt", "de"));
		dataset.setPublisher(publisher);
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout(LICENSE);
		dataset.setLicense(license);

		assertRoundTrips(dataset);
	}

	@Test
	void aCatalogWithLinksRoundTrips() {
		Catalog catalog = catalog();
		catalog.getDataset().add(dataset());
		catalog.getService().add(dataService());

		// The links survive as links: the round trip must not inline the targets, nor
		// invent an rdf:type for a resource the graph never described.
		assertRoundTrips(catalog);
	}

	@Test
	void languageTaggedAndPlainLiteralsRoundTrip() {
		Dataset dataset = dataset();
		dataset.getTitle().add(literal("Luftqualität", "de"));
		dataset.getTitle().add(literal("Untagged", null));

		assertRoundTrips(dataset);
	}

	/**
	 * Writes, reads back, writes again, and compares the two graphs. Isomorphism
	 * alone would pass if both passes lost the same triple, so the entity count and
	 * every IRI-valued link are checked on the re-read model too.
	 */
	private static void assertRoundTrips(EObject original) {
		Model first = EObjectToJena.toModel(original);

		List<EObject> reread = JenaToEObject
				.over(DcatPackage.eINSTANCE, FoafPackage.eINSTANCE, TermsPackage.eINSTANCE, RdfPackage.eINSTANCE)
				.parse(first);
		assertEquals(1, reread.size(),
				() -> "expected exactly one root entity back, got " + reread.size() + "\n" + dump(first));

		Model second = EObjectToJena.toModel(reread.get(0));
		assertTrue(first.isIsomorphicWith(second),
				() -> "round trip changed the graph\nfirst:\n" + dump(first) + "\nsecond:\n" + dump(second));
	}

	// --- helpers ------------------------------------------------------------

	/**
	 * The object of {@code subject property ?o} must be exactly one IRI node equal
	 * to {@code expected}. A blank node or a literal here means the link was lost
	 * while still producing a parseable graph.
	 */
	private static void assertObjectIri(Model model, String subject, String property, String expected, String what) {
		List<RDFNode> objects = model
				.listObjectsOfProperty(model.createResource(subject), model.createProperty(property)).toList();
		assertEquals(1, objects.size(), () -> "expected exactly one " + what + " on " + subject + "\n" + dump(model));
		RDFNode object = objects.get(0);
		assertTrue(object.isURIResource(),
				() -> what + " must be an IRI, not a blank node or literal\n" + dump(model));
		assertEquals(expected, object.asResource().getURI(), () -> dump(model));
	}

	private static String dump(Model model) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RDFDataMgr.write(out, model, Lang.TURTLE);
		return out.toString(StandardCharsets.UTF_8);
	}

	private static Catalog catalog() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(CATALOG);
		catalog.getTitle().add(literal("Catalogue", "en"));
		return catalog;
	}

	private static Dataset dataset() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTitle().add(literal("Air quality", "en"));
		return dataset;
	}

	private static dcat.DataService dataService() {
		dcat.DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(BASE + "data-services/s1");
		return service;
	}

	private static Dataset datasetWithDistribution() {
		Dataset dataset = dataset();
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DISTRIBUTION);
		distribution.setTitle(literal("CSV download", "en"));
		dataset.getDistribution().add(distribution);
		return dataset;
	}

	private static PlainLiteral literal(String value, String lang) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang(lang);
		return literal;
	}
}

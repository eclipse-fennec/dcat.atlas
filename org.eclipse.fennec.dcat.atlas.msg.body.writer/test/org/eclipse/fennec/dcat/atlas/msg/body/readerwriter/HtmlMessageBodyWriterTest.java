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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import javax.xml.datatype.DatatypeFactory;

import rdf.DateOrDateTimeLiteral;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsFactory;

/**
 * Verifies the {@code text/html} representation (N25): that the page really is the whole
 * graph, that the pieces a crawler reads are in it, and — the part with teeth — that
 * stored data cannot become markup or script.
 */
public class HtmlMessageBodyWriterTest {

	private static final String BASE = "http://localhost:8085/dcat/rest/";
	private static final String DATASET = BASE + "datasets/luftqualitaet-2026";
	private static final String DISTRIBUTION = DATASET + "/distributions/csv";
	private static final String SERIES = BASE + "dataset-series/luftqualitaet";
	private static final String PUBLISHER = "https://www.umweltbundesamt.de/";
	private static final String LICENSE = "http://dcat-ap.de/def/licenses/dl-by-de/2.0";

	// --- the page is the graph --------------------------------------------

	@Test
	void everyPredicateOfTheSubjectGetsARow() {
		String html = render(fullDataset());
		assertTrue(html.contains("dcterms:title"), html);
		assertTrue(html.contains("dcterms:description"), html);
		assertTrue(html.contains("dcat:keyword"), html);
		assertTrue(html.contains("dcterms:publisher"), html);
		assertTrue(html.contains("dcat:distribution"), html);
		assertTrue(html.contains("dcat:inSeries"), html);
	}

	/**
	 * The heading is a view of the title, not a replacement for it — the invariant that
	 * makes this renderer trustworthy is that no predicate is held back from the table.
	 */
	@Test
	void theTitleIsBothTheHeadingAndARow() {
		String html = render(fullDataset());
		assertTrue(html.contains("<h1>Luftqualität 2026</h1>"), html);
		assertTrue(html.contains("<th>dcterms:title</th>"), html);
	}

	@Test
	void theTypeAndTheSubjectIriAreShown() {
		String html = render(fullDataset());
		assertTrue(html.contains("dcat:Dataset"), html);
		assertTrue(html.contains(DATASET), html);
	}

	/**
	 * Rows are sorted by CURIE because a Jena model does not preserve emission order.
	 * Pinning it here is what makes the page byte-stable between requests.
	 * <p>
	 * Deliberately a dataset with no contained nodes: a nested block carries rows of its
	 * own, so on the full fixture the first {@code dcterms:title} in the document is the
	 * distribution's, not the dataset's, and a document-wide {@code indexOf} would be
	 * comparing rows from two different tables.
	 */
	@Test
	void rowsAreOrderedAlphabeticallyByCurie() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTitle().add(literal("Luftqualität 2026", "de"));
		dataset.getDescription().add(literal("Messwerte.", "de"));
		dataset.getKeyword().add(literal("NO2", "de"));
		dataset.getTheme().add("http://publications.europa.eu/resource/authority/data-theme/ENVI");

		String html = render(dataset);
		int keyword = html.indexOf("<th>dcat:keyword</th>");
		int theme = html.indexOf("<th>dcat:theme</th>");
		int description = html.indexOf("<th>dcterms:description</th>");
		int title = html.indexOf("<th>dcterms:title</th>");
		assertTrue(keyword > 0 && theme > 0 && description > 0 && title > 0, html);
		assertTrue(keyword < theme, "dcat:keyword before dcat:theme");
		assertTrue(theme < description, "the dcat: namespace before the dcterms: one");
		assertTrue(description < title, "dcterms:description before dcterms:title");
	}

	@Test
	void aLanguageTagIsShownNextToItsValue() {
		assertTrue(render(fullDataset()).contains("@de"), "the language tag belongs to the value");
	}

	/**
	 * Containment is inlined and a link is not: {@code dcat:distribution} is a containment
	 * reference, so the distribution's own properties are part of this graph and belong on
	 * this page, while {@code dcat:inSeries} points at a resource served in its own right.
	 */
	@Test
	void containedNodesAreNestedAndReferencesAreOnlyLinked() {
		String html = render(fullDataset());
		assertTrue(html.contains("<div class=\"nested\">"), html);
		assertTrue(html.contains("dcat:downloadURL"), "the contained distribution's properties: " + html);
		assertTrue(html.contains(SERIES), html);
		// The series is a bare link, so nothing of its own can appear — it has no
		// properties in this graph at all.
		assertFalse(html.contains("dcat:seriesMember"), html);
	}

	@Test
	void aResourceWithoutATitleStillGetsAHeading() {
		Dataset untitled = DcatFactory.eINSTANCE.createDataset();
		untitled.setAbout(DATASET);
		String html = render(untitled);
		assertTrue(html.contains("<h1>dcat:Dataset</h1>"), html);
	}

	// --- escaping ---------------------------------------------------------

	/**
	 * A title is client-supplied and stored verbatim, so the one thing this page must
	 * never do is hand it back as markup.
	 */
	@Test
	void storedMarkupIsEscapedAndNeverExecutes() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTitle().add(literal("<script>alert('x')</script>", "en"));

		String html = render(dataset);
		assertFalse(html.contains("<script>alert"), "the stored script tag survived: " + html);
		assertTrue(html.contains("&lt;script&gt;alert(&#39;x&#39;)&lt;/script&gt;"), html);
	}

	/**
	 * An {@code AnyURI} attribute is stored as given, and {@code javascript:…} is a
	 * syntactically valid IRI, so it reaches the renderer as a node rather than a literal.
	 * It must not become a working link.
	 */
	@Test
	void anUnsafeSchemeIsRenderedAsTextRatherThanALink() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTheme().add("javascript:alert('x')");

		String html = render(dataset);
		assertFalse(html.contains("href=\"javascript:"), "an unsafe scheme became an href: " + html);
		assertTrue(html.contains("javascript:alert(&#39;x&#39;)"), "the value is still shown: " + html);
	}

	@Test
	void anOrdinaryHttpIriIsLinked() {
		assertTrue(render(fullDataset()).contains("href=\"" + SERIES + "\""), "a http(s) IRI should be an href");
	}

	// --- schema.org -------------------------------------------------------

	@Test
	void theSchemaOrgBlockDescribesTheDataset() {
		String html = render(fullDataset());
		assertTrue(html.contains("<script type=\"application/ld+json\">"), html);
		assertTrue(html.contains("\"@context\": \"https://schema.org\""), html);
		assertTrue(html.contains("\"@type\": \"Dataset\""), html);
		assertTrue(html.contains("\"@id\": \"" + DATASET + "\""), html);
		assertTrue(html.contains("\"name\""), html);
		assertTrue(html.contains("\"keywords\""), html);
		assertTrue(html.contains("\"dateModified\""), html);
	}

	@Test
	void theSchemaOrgBlockCarriesThePublisherAndTheDownload() {
		String html = render(fullDataset());
		assertTrue(html.contains("\"@type\": \"Organization\""), html);
		assertTrue(html.contains("Umweltbundesamt"), html);
		assertTrue(html.contains("\"@type\": \"DataDownload\""), html);
		assertTrue(html.contains("\"contentUrl\""), html);
		assertTrue(html.contains("\"encodingFormat\""), html);
	}

	@Test
	void eachDcatTypeGetsItsSchemaOrgCounterpart() {
		assertTrue(render(fullDataset()).contains("\"@type\": \"Dataset\""));

		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout(SERIES);
		assertTrue(render(series).contains("\"@type\": \"Dataset\""), "a series is a Dataset to schema.org");

		dcat.Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(BASE + "catalogs/example");
		assertTrue(render(catalog).contains("\"@type\": \"DataCatalog\""));

		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(BASE + "data-services/luftqualitaet-api");
		assertTrue(render(service).contains("\"@type\": \"WebAPI\""));
	}

	/**
	 * A type schema.org cannot express gets no block at all. An empty one would tell a
	 * crawler the page describes nothing.
	 */
	@Test
	void anUnmappedTypeGetsNoSchemaOrgBlock() {
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout(LICENSE);
		Model model = EObjectToJena.toModel(license);
		assertNull(SchemaOrgJsonLd.of(model, model.getResource(LICENSE)));
		// Not the bare media type: the page's footer names it as one of the representations
		// on offer. What must be absent is the element itself.
		assertFalse(render(license).contains("<script type=\"application/ld+json\">"));
	}

	/**
	 * The JSON-LD sits inside a {@code <script>} element, where a literal
	 * {@code </script>} in any stored value would close it early and turn the rest of the
	 * data into markup.
	 */
	@Test
	void theJsonLdCannotCloseItsOwnScriptElement() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTitle().add(literal("</script><img src=x onerror=alert(1)>", "en"));

		String html = render(dataset);
		int scriptStart = html.indexOf("<script type=\"application/ld+json\">");
		int scriptEnd = html.indexOf("</script>", scriptStart);
		assertTrue(scriptStart >= 0 && scriptEnd > scriptStart, html);
		String block = html.substring(scriptStart, scriptEnd);
		assertFalse(block.contains("</"), "the JSON-LD block contains a tag closer: " + block);
		assertTrue(block.contains("\\u003c"), "'<' should be escaped to its \\u form: " + block);
	}

	@Test
	void jsonStringEscapesTheCharactersThatMatterInAScript() {
		assertEquals("\"a\\u003cb\\u003ec\\u0026d\"", SchemaOrgJsonLd.jsonString("a<b>c&d"));
		assertEquals("\"say \\\"hi\\\"\"", SchemaOrgJsonLd.jsonString("say \"hi\""));
		assertEquals("\"a\\\\b\"", SchemaOrgJsonLd.jsonString("a\\b"));
		assertEquals("\"line\\nbreak\"", SchemaOrgJsonLd.jsonString("line\nbreak"));
		assertEquals("\"\\u0001\"", SchemaOrgJsonLd.jsonString("\u0001"));
	}

	// --- the writer itself ------------------------------------------------

	@Test
	void isWriteableAcceptsAnEObjectAndNothingElse() {
		HtmlMessageBodyWriter writer = new HtmlMessageBodyWriter();
		assertTrue(writer.isWriteable(Dataset.class, Dataset.class, null, null));
		assertFalse(writer.isWriteable(String.class, String.class, null, null));
		assertEquals(-1, writer.getSize(null, null, null, null, null));
	}

	/**
	 * DCAT-AP.de content is full of umlauts, so the response has to say which encoding
	 * those bytes are in — {@code @Produces} cannot carry the charset without making it
	 * part of the negotiated media type.
	 */
	@Test
	void theResponseStatesUtf8AndTheBytesAreUtf8() throws Exception {
		HtmlMessageBodyWriter writer = new HtmlMessageBodyWriter();
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		writer.writeTo(fullDataset(), Dataset.class, Dataset.class, null, MediaType.TEXT_HTML_TYPE, headers, out);

		assertEquals("text/html;charset=UTF-8", headers.getFirst("Content-Type"));
		String html = out.toString(StandardCharsets.UTF_8);
		assertTrue(html.startsWith("<!DOCTYPE html>"), html);
		assertTrue(html.contains("Luftqualität 2026"), "the umlaut survived the round trip");
		assertTrue(html.contains("<meta charset=\"utf-8\">"), html);
	}

	/**
	 * The subject is the entity, not one of the nodes contained in it — the dataset's own
	 * IRI heads the page and its distribution appears below, inside the table.
	 */
	@Test
	void thePageIsAboutTheEntityAndNotOneOfItsChildren() {
		String html = render(fullDataset());
		assertTrue(html.contains("<p class=\"self\">"), html);
		assertTrue(html.indexOf(DATASET) < html.indexOf(DISTRIBUTION), "the dataset is the subject, not its CSV");
	}

	// --- fixtures ---------------------------------------------------------

	private static String render(EObject entity) {
		HtmlMessageBodyWriter writer = new HtmlMessageBodyWriter();
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			writer.writeTo(entity, entity.getClass(), entity.getClass(), null, MediaType.TEXT_HTML_TYPE, headers, out);
		} catch (Exception e) {
			throw new AssertionError("rendering failed", e);
		}
		return out.toString(StandardCharsets.UTF_8);
	}

	/** The worked example from {@code docs/opendata-portal-user-guide.md}. */
	private static Dataset fullDataset() {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASET);
		dataset.getTitle().add(literal("Luftqualität 2026", "de"));
		dataset.getDescription().add(literal("Stündliche Messwerte an 412 Messstationen.", "de"));
		dataset.getKeyword().add(literal("Luftqualität", "de"));
		dataset.getKeyword().add(literal("NO2", "de"));

		Agent publisher = FoafFactory.eINSTANCE.createAgent();
		publisher.setAbout(PUBLISHER);
		publisher.getName().add(literal("Umweltbundesamt", "de"));
		dataset.setPublisher(publisher);

		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout(LICENSE);
		dataset.setLicense(license);

		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DISTRIBUTION);
		distribution.setTitle(literal("CSV-Download", "de"));
		distribution.setFormat("http://publications.europa.eu/resource/authority/file-type/CSV");
		distribution.getDownloadURL().add("https://example.org/data/luftqualitaet-2026.csv");
		distribution.getAccessURL().add("https://example.org/data/luftqualitaet-2026.csv");
		dataset.getDistribution().add(distribution);

		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout(SERIES);
		dataset.getInSeries().add(series);

		dataset.setModified(dateLiteral("2026-08-01"));
		return dataset;
	}

	private static PlainLiteral literal(String value, String lang) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setValue(value);
		literal.setLang(lang);
		return literal;
	}

	private static DateOrDateTimeLiteral dateLiteral(String value) {
		DateOrDateTimeLiteral literal = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		literal.setValue(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(value));
		return literal;
	}
}

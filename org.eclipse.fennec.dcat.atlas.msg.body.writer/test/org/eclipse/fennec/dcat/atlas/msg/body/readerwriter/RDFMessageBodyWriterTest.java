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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.Test;

import dcat.Catalog;

/**
 * Verifies the writers' acceptance rules and that each RDF syntax they emit is
 * valid: the bytes are parsed back with Jena and must reproduce the input graph.
 */
public class RDFMessageBodyWriterTest {

	private static final String CATALOG_URI = "https://govdata.de#catalog";
	private static final String DCT_TITLE = "http://purl.org/dc/terms/title";

	// Used only to obtain a Collection<EObject> parameterized type via reflection.
	@SuppressWarnings("unused")
	private List<Catalog> catalogList;

	@Test
	void isWriteableAcceptsEObjectAndEObjectCollection() throws Exception {
		Type collectionType = getClass().getDeclaredField("catalogList").getGenericType();
		TurtleMessageBodyWriter writer = new TurtleMessageBodyWriter();
		assertTrue(writer.isWriteable(Catalog.class, Catalog.class, null, null), "single EObject");
		assertTrue(writer.isWriteable(List.class, collectionType, null, null), "Collection<EObject>");
	}

	@Test
	void isWriteableRejectsUnrelatedTypes() {
		TurtleMessageBodyWriter writer = new TurtleMessageBodyWriter();
		assertFalse(writer.isWriteable(String.class, String.class, null, null));
		assertFalse(AbstractRDFMessageBodyWriter.isSupported(EObject[].class, EObject[].class));
	}

	@Test
	void turtleOutputParsesBackToTheSameGraph() {
		assertRoundTrip(new TurtleMessageBodyWriter(), Lang.TURTLE);
	}

	@Test
	void jsonLdOutputParsesBackToTheSameGraph() {
		assertRoundTrip(new JsonLdMessageBodyWriter(), Lang.JSONLD11);
	}

	@Test
	void n3OutputParsesBackToTheSameGraph() {
		assertRoundTrip(new N3MessageBodyWriter(), Lang.N3);
	}

	private void assertRoundTrip(AbstractRDFMessageBodyWriter writer, Lang lang) {
		Model source = sampleModel();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeModel(source, out);

		assertTrue(out.size() > 0, "writer produced no output");

		Model reparsed = ModelFactory.createDefaultModel();
		RDFDataMgr.read(reparsed, new ByteArrayInputStream(out.toByteArray()), lang);
		assertTrue(source.isIsomorphicWith(reparsed),
				() -> lang + " round-trip changed the graph:\n" + out);
	}

	private static Model sampleModel() {
		Model model = ModelFactory.createDefaultModel();
		Resource catalog = model.createResource(CATALOG_URI);
		catalog.addProperty(RDF.type, model.createResource("http://www.w3.org/ns/dcat#Catalog"));
		catalog.addProperty(model.createProperty(DCT_TITLE), "GovData", "de");
		return model;
	}
}

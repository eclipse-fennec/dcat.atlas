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
package org.eclipse.fennec.dcat.atlas.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.fennec.odata.query.ODataResourceParser;
import org.eclipse.fennec.odata.query.ResourcePath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Whether a DCAT identity survives the OData resource path (AP-DCAT-2, work package 1).
 * <p>
 * The model's one key is {@code rdf:about}, an absolute IRI, so a by-key read reads
 * {@code Dataset('http://host/dcat/rest/datasets/abc')}. This measures the <em>parser</em>
 * half of that question — the half that lives in {@code emf.odata}. The other half is the
 * servlet container: Jetty normalises and may reject {@code //} and {@code %2F} inside a
 * path segment before the parser ever sees it, and only the live runtime can answer that.
 * <p>
 * It has since answered: Jetty refuses that path, and the form a client uses is
 * {@code $entity?$id=Dataset('…')}, where the same literal travels as a query value. That
 * does not make these tests historical — the {@code $id} is handed to this same parser once
 * it is resolved against the service root, so what is measured here is still on the path of
 * every by-key read. {@code ODataEndpointIntegrationTest} covers it end to end.
 */
public class IriKeyInUrlTest {

	private final ODataResourceParser parser = new ODataResourceParser();

	@Test
	@DisplayName("an IRI key parses: slashes inside the quoted literal are data, not segments")
	void iriKeyParses() {
		ResourcePath path = parser.parse("Dataset('http://localhost:8085/dcat/rest/datasets/abc')");

		assertEquals("Dataset", path.entitySet());
		// key() is the key literal as written - quotes included; unquoting is the servlet's
		assertEquals("'http://localhost:8085/dcat/rest/datasets/abc'", path.key());
		assertEquals(0, path.segments().size());
	}

	@Test
	@DisplayName("...and so does a walk off an IRI key")
	void iriKeyWithNavigation() {
		ResourcePath path = parser
				.parse("Dataset('http://localhost:8085/dcat/rest/datasets/abc')/distribution");

		assertEquals("Dataset", path.entitySet());
		// key() is the key literal as written - quotes included; unquoting is the servlet's
		assertEquals("'http://localhost:8085/dcat/rest/datasets/abc'", path.key());
		assertEquals(1, path.segments().size());
	}

	@Test
	@DisplayName("a short opaque id parses the same way, for comparison")
	void plainKeyParses() {
		ResourcePath path = parser.parse("Dataset('abc')");

		assertEquals("Dataset", path.entitySet());
		assertEquals("'abc'", path.key());
	}
}

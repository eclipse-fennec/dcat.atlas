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
package org.eclipse.fennec.dcat.atlas.rest.tests;

import dcat.DataService;
import dcat.DcatResource;
import dcat.Dataset;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * The properties DCAT-AP.de makes mandatory, for entities seeded straight through the OSGi
 * services rather than over HTTP.
 * <p>
 * The counterpart of {@code AbstractEntityResourceIntegrationTest.mandatoryFor}, which does
 * the same for XMI request bodies. Both exist because these suites used to seed entities
 * carrying a title and nothing else, which the profile does not accept — it only worked
 * while nothing validated. Duplicated rather than shared with the {@code impl} suite's
 * {@code TestEntities} because the two bundles have no test-code dependency between them.
 */
final class RestEntities {

	private RestEntities() {
	}

	static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(value);
		return literal;
	}

	static Agent publisher() {
		Agent agent = FoafFactory.eINSTANCE.createAgent();
		agent.setAbout("https://example.de/organisation/uba");
		agent.getName().add(literal("Umweltbundesamt"));
		return agent;
	}

	/** Title and publisher: what every {@code DcatResource} needs. */
	static <T extends DcatResource> T mandatory(T resource, String title) {
		resource.getTitle().add(literal(title));
		resource.setPublisher(publisher());
		return resource;
	}

	/** Adds {@code dct:description}, Pflicht for Catalog, Dataset and DatasetSeries but not DataService. */
	static <T extends Dataset> T mandatoryDataset(T dataset, String title) {
		mandatory(dataset, title);
		dataset.getDescription().add(literal(title + " description"));
		return dataset;
	}

	/** Adds {@code dcat:endpointURL}, Pflicht for a DataService. */
	static DataService mandatoryDataService(DataService service, String title) {
		mandatory(service, title);
		service.getEndpointURL().add("https://example.de/sparql");
		return service;
	}
}

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

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatResource;
import dcat.Distribution;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;
import terms.LicenseDocument;
import terms.TermsFactory;

/**
 * The properties DCAT-AP.de makes mandatory, so a test fixture is a <em>valid</em> entity.
 *
 * <h2>Why this exists</h2>
 *
 * Every fixture in this suite used to build an entity with a title and nothing else. That
 * was not a valid Dataset — §4.3 makes {@code dct:description} and {@code dct:publisher}
 * Pflicht too — it simply never mattered, because nothing checked. Once the write boundary
 * started enforcing the model's constraints, keeping those fixtures would have meant
 * leaving {@code validateOnWrite} off in the tests, so the whole suite would exercise a
 * path production does not take.
 * <p>
 * Collected here rather than repeated in each test because the mandatory set is a property
 * of the profile, not of any one test: when the profile changes, one place changes.
 */
final class TestEntities {

	private TestEntities() {
	}

	static PlainLiteral literal(String value) {
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(value);
		return literal;
	}

	/** An Agent carrying the {@code foaf:name} its own multiplicity requires. */
	static Agent publisher() {
		Agent agent = FoafFactory.eINSTANCE.createAgent();
		agent.setAbout("https://example.de/organisation/uba");
		agent.getName().add(literal("Umweltbundesamt"));
		return agent;
	}

	static LicenseDocument license() {
		LicenseDocument license = TermsFactory.eINSTANCE.createLicenseDocument();
		license.setAbout("http://dcat-ap.de/def/licenses/dl-by-de/2.0");
		return license;
	}

	/**
	 * Title and publisher: what every {@code DcatResource} needs, DataService included.
	 * Description is deliberately <em>not</em> here — see {@link #mandatoryDataset}.
	 */
	static <T extends DcatResource> T mandatory(T resource, String title) {
		resource.getTitle().add(literal(title));
		resource.setPublisher(publisher());
		return resource;
	}

	/**
	 * Adds {@code dct:description}, which §4.2/§4.3/§4.5 make Pflicht for Catalog, Dataset
	 * and DatasetSeries and §4.4 does not for DataService — the asymmetry that forced the
	 * obligation out of the ecore and into the {@code Dataset::HasDescription} invariant.
	 * <p>
	 * A separate name rather than an overload: overload resolution on the static type would
	 * silently pick the wrong one for a Dataset held in a {@code DcatResource} variable.
	 */
	static <T extends Dataset> T mandatoryDataset(T dataset, String title) {
		mandatory(dataset, title);
		dataset.getDescription().add(literal(title + " description"));
		return dataset;
	}

	/** Adds {@code dcat:endpointURL}, Pflicht for a DataService (§4.4). */
	static DataService mandatoryDataService(DataService service, String title) {
		mandatory(service, title);
		service.getEndpointURL().add("https://example.de/sparql");
		return service;
	}

	/** {@code dcat:accessURL} and {@code dct:license}, the two Pflicht properties of §4.6. */
	static Distribution mandatoryDistribution(Distribution distribution) {
		distribution.getAccessURL().add("https://example.de/data.csv");
		distribution.setLicense(license());
		return distribution;
	}
}

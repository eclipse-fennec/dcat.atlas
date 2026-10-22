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

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import javax.xml.datatype.DatatypeFactory;

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import dcat.Dataset;
import dcat.DcatFactory;
import rdf.DateOrDateTimeLiteral;
import rdf.RdfFactory;

public class DatasetResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DatasetAdminService service;

	@Override
	protected String collection() {
		return "datasets";
	}

	@Override
	protected String typeName() {
		return "Dataset";
	}

	@Override
	protected String readResourceName() {
		return "DatasetReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "DatasetAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		// Seeded the way the store mints identities: logical, not the request URL.
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		RestEntities.mandatoryDataset(dataset, title);
		service.upsertDataset(dataset, Caller.ANONYMOUS);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getDataset(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getDataset(id).get().getTitle().get(0).getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteDataset(id, false, Caller.ANONYMOUS);
	}

	/**
	 * A read carries {@code Last-Modified} taken from the entity's own
	 * {@code dcterms:modified}, formatted as an HTTP date in GMT.
	 */
	@Test
	void aStoredModifiedDateBecomesTheLastModifiedHeader() throws Exception {
		track("lm1");
		seedModified("lm1", "2026-10-19T08:30:00Z");

		HttpResponse<String> read = get(reads() + "/lm1", TURTLE);
		assertEquals(200, read.statusCode(), read.body());
		assertEquals("Mon, 19 Oct 2026 08:30:00 GMT", read.headers().firstValue("Last-Modified").orElseThrow(
				() -> new AssertionError("a dataset with dcterms:modified must carry Last-Modified")));
	}

	/**
	 * A {@code dcterms:modified} carrying an offset is <em>converted</em> to GMT, not read as
	 * though its local fields were already GMT. The lexical form here is the same instant as
	 * the one above, so it must produce the same header - and a German portal writes
	 * {@code +01:00}/{@code +02:00}, so this is the common case rather than an edge one.
	 */
	@Test
	void aModifiedDateWithAnOffsetIsConvertedToGmt() throws Exception {
		track("lm4");
		seedModified("lm4", "2026-10-19T10:30:00+02:00");

		HttpResponse<String> read = get(reads() + "/lm4", TURTLE);
		assertEquals("Mon, 19 Oct 2026 08:30:00 GMT", read.headers().firstValue("Last-Modified").orElseThrow());
	}

	/** A date-only {@code dcterms:modified} is read as midnight UTC, not midnight local time. */
	@Test
	void aDateOnlyModifiedIsReadAsMidnightUtc() throws Exception {
		track("lm2");
		seedModified("lm2", "2026-10-19");

		HttpResponse<String> read = get(reads() + "/lm2", TURTLE);
		assertEquals("Mon, 19 Oct 2026 00:00:00 GMT", read.headers().firstValue("Last-Modified").orElseThrow());
	}

	/**
	 * No {@code dcterms:modified}, no header. The portal has no other honest source for one:
	 * the store's own timestamps are per commit, not per resource.
	 */
	@Test
	void anEntityWithoutAModifiedDateCarriesNoLastModifiedHeader() throws Exception {
		track("lm3");
		seed("lm3", "No date");

		HttpResponse<String> read = get(reads() + "/lm3", TURTLE);
		assertEquals(200, read.statusCode(), read.body());
		assertTrue(read.headers().firstValue("Last-Modified").isEmpty(),
				"a resource that does not say when it changed must not claim a date");
	}

	private void seedModified(String id, String lexical) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		RestEntities.mandatoryDataset(dataset, "Last modified " + id);
		DateOrDateTimeLiteral modified = RdfFactory.eINSTANCE.createDateOrDateTimeLiteral();
		modified.setValue(DatatypeFactory.newDefaultInstance().newXMLGregorianCalendar(lexical));
		dataset.setModified(modified);
		service.upsertDataset(dataset, Caller.ANONYMOUS);
	}
}

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import dcat.DataService;
import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

public class DataServiceResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DataServiceAdminService service;

	/** servesDataset points at a separately stored Dataset. */
	@InjectService
	DatasetAdminService datasetService;

	@Override
	protected String collection() {
		return "data-services";
	}

	@Override
	protected String typeName() {
		return "DataService";
	}

	@Override
	protected String readResourceName() {
		return "DataServiceReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "DataServiceAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		DataService dataService = DcatFactory.eINSTANCE.createDataService();
		// Seeded the way the store mints identities: logical, not the request URL.
		dataService.setAbout(DcatIds.logicalIri(DcatIds.DATA_SERVICES, id));
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataService.getTitle().add(literal);
		service.upsertDataService(dataService);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getDataService(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getDataService(id).get().getTitle().get(0).getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteDataService(id, false);
	}

	// --- dcat:servesDataset membership --------------------------------------

	private static final String SERVED_DATASET_ID = "serves-member-ds";

	@AfterEach
	void removeServedDataset() {
		// Unlink first: FR-1 refuses to delete a dataset a service still serves, which is
		// exactly the guarantee these endpoints rely on. Runs before the base class's
		// cleanup (JUnit works up the hierarchy for @AfterEach), so svc1 is still there.
		service.getDataService("svc1")
				.ifPresent(unused -> service.deleteDatasetFromDataService("svc1", SERVED_DATASET_ID));
		datasetService.deleteDataset(SERVED_DATASET_ID, false);
	}

	@Test
	void addAndRemoveServedDatasetOverHttp() throws Exception {
		track("svc1");
		seed("svc1", "SPARQL endpoint");

		HttpResponse<String> add = postXmi(writes() + "/svc1/datasets",
				xmiBody("Dataset", DcatIds.logicalIri(DcatIds.DATASETS, SERVED_DATASET_ID), "NO2"));

		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, service.getDataService("svc1").get().getServesDataset().size());
		// The dataset is stored in its own right, not embedded in the service.
		assertTrue(datasetService.getDataset(SERVED_DATASET_ID).isPresent());

		HttpResponse<String> remove = delete(writes() + "/svc1/datasets/" + SERVED_DATASET_ID);
		assertEquals(204, remove.statusCode());
		assertTrue(service.getDataService("svc1").get().getServesDataset().isEmpty());
		// Unlinking removes the membership, not the dataset.
		assertTrue(datasetService.getDataset(SERVED_DATASET_ID).isPresent());
	}

	/**
	 * A Dataset that already exists is refused rather than replaced: this endpoint writes
	 * the body, and a Dataset served by one service is typically catalogued and served
	 * elsewhere too. The {@code PUT} below is the way to serve one that exists.
	 */
	@Test
	void addingADatasetThatAlreadyExistsIsRefused() throws Exception {
		track("svc1");
		seed("svc1", "SPARQL endpoint");
		seedServedDataset("NO2 measurements");

		HttpResponse<String> refused = postXmi(writes() + "/svc1/datasets",
				xmiBody("Dataset", DcatIds.logicalIri(DcatIds.DATASETS, SERVED_DATASET_ID), "Rewritten"));

		assertEquals(409, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains("PUT /admin/data-services/svc1/datasets/" + SERVED_DATASET_ID),
				"the 409 should point at the link request: " + refused.body());
		assertEquals(BASE + "/datasets/" + SERVED_DATASET_ID, refused.headers().firstValue("Location").orElse(null),
				"...and Location at the dataset that is in the way");
		assertEquals("NO2 measurements",
				datasetService.getDataset(SERVED_DATASET_ID).get().getTitle().get(0).getValue(),
				"the refused POST must not have rewritten the dataset");
		assertTrue(service.getDataService("svc1").get().getServesDataset().isEmpty(),
				"and must not have linked it either");
	}

	@Test
	void linkExistingDatasetOverHttp() throws Exception {
		track("svc1");
		seed("svc1", "SPARQL endpoint");
		seedServedDataset("NO2 measurements");

		HttpResponse<String> link = putEmpty(writes() + "/svc1/datasets/" + SERVED_DATASET_ID);

		assertEquals(200, link.statusCode(), link.body());
		assertEquals(1, service.getDataService("svc1").get().getServesDataset().size());
		// The point of linking: the dataset keeps its own content.
		assertEquals("NO2 measurements",
				datasetService.getDataset(SERVED_DATASET_ID).get().getTitle().get(0).getValue());
	}

	@Test
	void linkUnknownDatasetIsNotFound() throws Exception {
		track("svc1");
		seed("svc1", "SPARQL endpoint");

		HttpResponse<String> link = putEmpty(writes() + "/svc1/datasets/does-not-exist");

		assertEquals(404, link.statusCode(), link.body());
	}

	@Test
	void membershipOnUnknownDataServiceIsNotFound() throws Exception {
		assertEquals(404, putEmpty(writes() + "/missing/datasets/" + SERVED_DATASET_ID).statusCode());
		assertEquals(404, postXmi(writes() + "/missing/datasets",
				xmiBody("Dataset", DcatIds.logicalIri(DcatIds.DATASETS, SERVED_DATASET_ID), "NO2")).statusCode());
	}

	/** Stores the served Dataset on its own, so linking has something that already exists. */
	private void seedServedDataset(String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, SERVED_DATASET_ID));
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		datasetService.upsertDataset(dataset);
	}
}

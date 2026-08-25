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

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetSeriesAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;

public class DatasetSeriesResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DatasetSeriesAdminService service;

	/** Series membership materialises as {@code dcat:inSeries} on the Dataset, so we inspect the dataset store. */
	@InjectService
	DatasetAdminService datasetService;

	@Override
	protected String collection() {
		return "dataset-series";
	}

	@Override
	protected String typeName() {
		return "DatasetSeries";
	}

	@Override
	protected String readResourceName() {
		return "DatasetSeriesReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "DatasetSeriesAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		// Seeded the way the store mints identities: logical, not the request URL.
		series.setAbout(DcatIds.logicalIri(DcatIds.DATASET_SERIES, id));
		RestEntities.mandatoryDataset(series, title);
		service.upsertDatasetSeries(series);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getDatasetSeries(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getDatasetSeries(id).get().getTitle().get(0).getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteDatasetSeries(id, false);
	}

	// --- FR-11 series membership -------------------------------------------

	private static final String MEMBER_DATASET_ID = "series-member-ds";

	@AfterEach
	void removeMemberDataset() {
		datasetService.deleteDataset(MEMBER_DATASET_ID, false);
	}

	@Test
	void addAndRemoveDatasetMembershipOverHttp() throws Exception {
		track("series1");
		seed("series1", "Air quality series");
		String datasetAbout = BASE + "/datasets/" + MEMBER_DATASET_ID;

		HttpResponse<String> add = postXmi(writes() + "/series1/datasets",
				xmiBody("Dataset", datasetAbout, "NO2"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().size());
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().get(0).getAbout()
				.endsWith("/series1"));

		HttpResponse<String> remove = delete(writes() + "/series1/datasets/" + MEMBER_DATASET_ID);
		assertEquals(204, remove.statusCode());
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().isEmpty());
	}

	/**
	 * A Dataset that already exists is refused rather than replaced. Membership in a series
	 * is stored as {@code dcat:inSeries} on the Dataset itself, so this endpoint writes the
	 * whole Dataset — and the same Dataset is very likely listed in catalogs and served by
	 * services that did not ask for its content to change.
	 */
	@Test
	void addingADatasetThatAlreadyExistsIsRefused() throws Exception {
		track("series1");
		seed("series1", "Air quality series");
		seedMemberDataset("NO2 measurements");

		HttpResponse<String> refused = postXmi(writes() + "/series1/datasets",
				xmiBody("Dataset", BASE + "/datasets/" + MEMBER_DATASET_ID, "Rewritten"));

		assertEquals(409, refused.statusCode(), refused.body());
		assertTrue(refused.body().contains("PUT /admin/dataset-series/series1/datasets/" + MEMBER_DATASET_ID),
				"the 409 should point at the link request: " + refused.body());
		assertEquals(BASE + "/datasets/" + MEMBER_DATASET_ID, refused.headers().firstValue("Location").orElse(null),
				"...and Location at the dataset that is in the way");
		assertEquals("NO2 measurements",
				datasetService.getDataset(MEMBER_DATASET_ID).get().getTitle().get(0).getValue(),
				"the refused POST must not have rewritten the dataset");
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().isEmpty(),
				"and must not have linked it either");
	}

	@Test
	void addMembershipToUnknownSeriesIsNotFound() throws Exception {
		HttpResponse<String> add = postXmi(writes() + "/missing/datasets",
				xmiBody("Dataset", BASE + "/datasets/" + MEMBER_DATASET_ID, "NO2"));
		assertEquals(404, add.statusCode());
	}

	// --- FR-11 membership by reference (link a Dataset that already exists) ---

	@Test
	void linkExistingDatasetOverHttp() throws Exception {
		track("series1");
		seed("series1", "Air quality series");
		seedMemberDataset("NO2 measurements");

		HttpResponse<String> link = putEmpty(writes() + "/series1/datasets/" + MEMBER_DATASET_ID);

		assertEquals(200, link.statusCode(), link.body());
		assertEquals(1, datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().size());
		// The point of linking: the dataset keeps its own content. A POST of a stub to the
		// collection would have replaced it, title and all.
		assertEquals("NO2 measurements",
				datasetService.getDataset(MEMBER_DATASET_ID).get().getTitle().get(0).getValue());

		HttpResponse<String> remove = delete(writes() + "/series1/datasets/" + MEMBER_DATASET_ID);
		assertEquals(204, remove.statusCode());
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().isEmpty());
	}

	@Test
	void linkUnknownDatasetIsNotFound() throws Exception {
		track("series1");
		seed("series1", "Air quality series");

		// NoSuchElementException from the service must surface as a 404, not a 500.
		HttpResponse<String> link = putEmpty(writes() + "/series1/datasets/does-not-exist");

		assertEquals(404, link.statusCode(), link.body());
	}

	@Test
	void linkToUnknownSeriesIsNotFound() throws Exception {
		HttpResponse<String> link = putEmpty(writes() + "/missing/datasets/" + MEMBER_DATASET_ID);
		assertEquals(404, link.statusCode());
	}

	/** Stores the member Dataset on its own, so linking has something that already exists. */
	private void seedMemberDataset(String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, MEMBER_DATASET_ID));
		RestEntities.mandatoryDataset(dataset, title);
		datasetService.upsertDataset(dataset);
	}
}

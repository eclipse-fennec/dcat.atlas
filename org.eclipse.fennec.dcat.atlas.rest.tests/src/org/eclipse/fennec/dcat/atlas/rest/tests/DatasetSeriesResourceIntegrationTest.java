package org.eclipse.fennec.dcat.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.common.annotation.InjectService;

import dcat.DatasetSeries;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

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
		series.setAbout(reads() + "/" + id);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		series.getTitle().add(literal);
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

		HttpResponse<String> add = postRdfXml(writes() + "/series1/datasets",
				rdfXmlBody("Dataset", datasetAbout, "NO2"));
		assertEquals(200, add.statusCode(), add.body());
		assertEquals(1, datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().size());
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().get(0).getAbout()
				.endsWith("/series1"));

		HttpResponse<String> remove = delete(writes() + "/series1/datasets/" + MEMBER_DATASET_ID);
		assertEquals(204, remove.statusCode());
		assertTrue(datasetService.getDataset(MEMBER_DATASET_ID).get().getInSeries().isEmpty());
	}

	@Test
	void addMembershipToUnknownSeriesIsNotFound() throws Exception {
		HttpResponse<String> add = postRdfXml(writes() + "/missing/datasets",
				rdfXmlBody("Dataset", BASE + "/datasets/" + MEMBER_DATASET_ID, "NO2"));
		assertEquals(404, add.statusCode());
	}
}

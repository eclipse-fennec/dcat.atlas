package org.eclipse.fennec.dcat.atlas.rest.tests;

import org.eclipse.fennec.dcat.atlas.api.DatasetSeriesAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.DatasetSeries;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

public class DatasetSeriesResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DatasetSeriesAdminService service;

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
}

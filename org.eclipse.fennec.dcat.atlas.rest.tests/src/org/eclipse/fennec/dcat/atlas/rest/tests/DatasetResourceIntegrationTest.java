package org.eclipse.fennec.dcat.atlas.rest.tests;

import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.Dataset;
import dcat.DcatFactory;
import rdf.PlainLiteral;
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
		dataset.setAbout(reads() + "/" + id);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		dataset.getTitle().add(literal);
		service.upsertDataset(dataset);
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
		service.deleteDataset(id, false);
	}
}

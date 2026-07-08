package org.eclipse.fennec.dcat.atlas.rest.tests;

import org.eclipse.fennec.dcat.atlas.api.DistributionAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.DcatFactory;
import dcat.Distribution;
import rdf.PlainLiteral;
import rdf.RdfFactory;

public class DistributionResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DistributionAdminService service;

	@Override
	protected String collection() {
		return "distributions";
	}

	@Override
	protected String typeName() {
		return "Distribution";
	}

	@Override
	protected String readResourceName() {
		return "DistributionReadOnlyResource";
	}

	@Override
	protected String adminResourceName() {
		return "DistributionAdminResource";
	}

	@Override
	protected void seed(String id, String title) {
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(reads() + "/" + id);
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		// Distribution has a single-valued title.
		distribution.setTitle(literal);
		service.upsertDistribution(distribution);
	}

	@Override
	protected boolean storedPresent(String id) {
		return service.getDistribution(id).isPresent();
	}

	@Override
	protected String storedTitle(String id) {
		return service.getDistribution(id).get().getTitle().getValue();
	}

	@Override
	protected void removeFromStore(String id) {
		service.deleteDistribution(id, false);
	}
}

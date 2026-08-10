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

import org.eclipse.fennec.dcat.atlas.api.DataServiceAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.DataService;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

public class DataServiceResourceIntegrationTest extends AbstractEntityResourceIntegrationTest {

	@InjectService
	DataServiceAdminService service;

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
		dataService.setAbout(reads() + "/" + id);
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
}

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

import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.DatasetAdminService;
import org.osgi.test.common.annotation.InjectService;

import dcat.Dataset;
import dcat.DcatFactory;

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

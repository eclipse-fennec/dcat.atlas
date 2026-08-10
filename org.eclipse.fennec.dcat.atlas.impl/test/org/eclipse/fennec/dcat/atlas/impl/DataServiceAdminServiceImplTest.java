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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.DataService;
import dcat.DcatFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

/**
 * Round-trips the file-backed data-service store (admin impl, which also covers
 * the inherited read operations).
 */
public class DataServiceAdminServiceImplTest {

	private static final String BASE = "https://portal.example/admin/api/v1/data-services/";

	@TempDir
	Path storage;

	private DataServiceAdminServiceImpl service() {
		return new DataServiceAdminServiceImpl(TestResourceSets.factory(), storage);
	}

	@Test
	void upsertThenGetReturnsEquivalentDataService() {
		service().upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));

		Optional<DataService> loaded = service().getDataService("sparql");
		assertTrue(loaded.isPresent());
		assertEquals(BASE + "sparql", loaded.get().getAbout());
		assertEquals("SPARQL endpoint", loaded.get().getTitle().get(0).getValue());
	}

	@Test
	void listReturnsEveryStoredDataService() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		service.upsertDataService(dataService(BASE + "wfs", "WFS endpoint"));
		assertEquals(2, service.listDataServices().size());
	}

	@Test
	void getUnknownIsEmpty() {
		assertTrue(service().getDataService("does-not-exist").isEmpty());
	}

	@Test
	void deleteRemovesTheDataService() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(BASE + "sparql", "SPARQL endpoint"));
		service.deleteDataService("sparql", false);
		assertTrue(service.getDataService("sparql").isEmpty());
	}

	@Test
	void mintsIdWhenAboutMissing() {
		DataServiceAdminServiceImpl service = service();
		service.upsertDataService(dataService(null, "Untitled about"));
		assertEquals(1, service.listDataServices().size());
	}

	private static DataService dataService(String about, String title) {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		if (about != null) {
			service.setAbout(about);
		}
		PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("en");
		literal.setValue(title);
		service.getTitle().add(literal);
		return service;
	}
}

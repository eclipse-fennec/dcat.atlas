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
package org.eclipse.fennec.dcat.atlas.odata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.DataService;
import dcat.DcatFactory;
import dcat.DcatPackage;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.Distribution;

/**
 * The snapshot the OData endpoints are served from.
 */
public class DcatEntityRepositoryTest {

	// --- which sets exist -----------------------------------------------------------

	@Test
	@DisplayName("supplies exactly the four collections, by exact type")
	void suppliesTheFourCollections() {
		DcatEntityRepository repository = repository(List.of(), List.of(), List.of(), List.of());

		assertTrue(repository.supplies(DcatPackage.Literals.CATALOG));
		assertTrue(repository.supplies(DcatPackage.Literals.DATASET));
		assertTrue(repository.supplies(DcatPackage.Literals.DATASET_SERIES));
		assertTrue(repository.supplies(DcatPackage.Literals.DATA_SERVICE));

		// A distribution is contained in its dataset and has no read service of its own,
		// so there is nothing for a root set to list.
		assertFalse(repository.supplies(DcatPackage.Literals.DISTRIBUTION));
		assertFalse(repository.supplies(DcatPackage.Literals.CATALOG_RECORD));
		assertFalse(repository.supplies(DcatPackage.Literals.DCAT_RESOURCE));
	}

	@Test
	@DisplayName("the Dataset set holds no catalogs, though Catalog extends Dataset in Ecore")
	void datasetSetIsTypeExact() {
		DcatEntityRepository repository = repository(List.of(catalog("c1")), List.of(dataset("d1")),
				List.of(series("s1")), List.of());
		repository.refresh();

		assertEquals(List.of(publicIri(DcatIds.DATASETS, "d1")),
				abouts(repository.entities(DcatPackage.Literals.DATASET)));
		assertEquals(List.of(publicIri(DcatIds.CATALOGS, "c1")),
				abouts(repository.entities(DcatPackage.Literals.CATALOG)));
		assertEquals(List.of(publicIri(DcatIds.DATASET_SERIES, "s1")),
				abouts(repository.entities(DcatPackage.Literals.DATASET_SERIES)));
	}

	@Test
	@DisplayName("nothing is served before the first refresh")
	void emptyUntilBuilt() {
		DcatEntityRepository repository = repository(List.of(catalog("c1")), List.of(), List.of(), List.of());

		assertEquals(List.of(), repository.entities(DcatPackage.Literals.CATALOG));
		assertFalse(repository.isReady());
	}

	// --- what the snapshot does to identities and links -------------------------------

	@Test
	@DisplayName("identities are rendered public, as a REST read returns them")
	void identitiesArePublic() {
		DcatEntityRepository repository = repository(List.of(), List.of(dataset("d1")), List.of(), List.of());
		repository.refresh();

		assertEquals(List.of(publicIri(DcatIds.DATASETS, "d1")),
				abouts(repository.entities(DcatPackage.Literals.DATASET)));
		assertTrue(repository.isReady());
	}

	@Test
	@DisplayName("a link to a stored resource becomes that very snapshot object")
	void linksPointIntoTheSnapshot() {
		Dataset stored = dataset("d1");
		Catalog storedCatalog = catalog("c1");
		storedCatalog.getDataset().add(stored);

		DcatEntityRepository repository = repository(List.of(storedCatalog), List.of(stored), List.of(), List.of());
		repository.refresh();

		Catalog served = (Catalog) repository.entities(DcatPackage.Literals.CATALOG).get(0);
		EObject servedDataset = repository.entities(DcatPackage.Literals.DATASET).get(0);

		assertEquals(1, served.getDataset().size());
		// Identity, not equality: $expand=dataset has to hand back the entity the Dataset
		// set serves, or a client sees two different objects for one resource.
		assertSame(servedDataset, served.getDataset().get(0));
	}

	@Test
	@DisplayName("a link to something the portal does not hold keeps its key and loses its proxy")
	void unmatchedLinksBecomeKeyOnlyEntities() {
		Dataset absent = DcatFactory.eINSTANCE.createDataset();
		absent.setAbout("https://elsewhere.example/datasets/not-ours");
		Catalog storedCatalog = catalog("c1");
		storedCatalog.getDataset().add(absent);

		DcatEntityRepository repository = repository(List.of(storedCatalog), List.of(), List.of(), List.of());
		repository.refresh();

		Catalog served = (Catalog) repository.entities(DcatPackage.Literals.CATALOG).get(0);
		assertEquals(1, served.getDataset().size());
		Dataset link = served.getDataset().get(0);

		assertEquals("https://elsewhere.example/datasets/not-ours", link.getAbout());
		// A proxy must not reach a query: the snapshot is detached, so nothing would
		// resolve it, and the repository contract forbids handing one out.
		assertFalse(((InternalEObject) link).eIsProxy(), "the link stub is still a proxy");
	}

	@Test
	@DisplayName("a link on a contained entity is stitched too")
	void containedEntitiesAreStitchedAsWell() {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(DcatIds.logicalIri(DcatIds.DATA_SERVICES, "svc"));
		Dataset stored = dataset("d1");
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DcatIds.distributionIri("d1", "dist"));
		distribution.getAccessService().add(service);
		stored.getDistribution().add(distribution);

		DcatEntityRepository repository = repository(List.of(), List.of(stored), List.of(), List.of(service));
		repository.refresh();

		Dataset served = (Dataset) repository.entities(DcatPackage.Literals.DATASET).get(0);
		EObject servedService = repository.entities(DcatPackage.Literals.DATA_SERVICE).get(0);

		assertEquals(1, served.getDistribution().size());
		assertSame(servedService, served.getDistribution().get(0).getAccessService().get(0),
				"accessService sits on a contained Distribution, not on the dataset itself");
	}

	@Test
	@DisplayName("distributions arrive contained in their dataset")
	void distributionsComeWithTheirDataset() {
		Dataset stored = dataset("d1");
		Distribution distribution = DcatFactory.eINSTANCE.createDistribution();
		distribution.setAbout(DcatIds.distributionIri("d1", "dist"));
		distribution.getAccessURL().add("https://files.example/d1.csv");
		stored.getDistribution().add(distribution);

		DcatEntityRepository repository = repository(List.of(), List.of(stored), List.of(), List.of());
		repository.refresh();

		Dataset served = (Dataset) repository.entities(DcatPackage.Literals.DATASET).get(0);
		assertEquals(1, served.getDistribution().size());
		assertEquals(List.of("https://files.example/d1.csv"), served.getDistribution().get(0).getAccessURL());
	}

	@Test
	@DisplayName("the stored entities are left untouched")
	void theStoreIsNotMutated() {
		Dataset stored = dataset("d1");
		Catalog storedCatalog = catalog("c1");
		storedCatalog.getDataset().add(stored);

		DcatEntityRepository repository = repository(List.of(storedCatalog), List.of(stored), List.of(), List.of());
		repository.refresh();

		// The snapshot is a copy; rendering the public host into the stored object would
		// write it back into data that is deliberately kept host-free.
		assertEquals(DcatIds.logicalIri(DcatIds.DATASETS, "d1"), stored.getAbout());
		assertSame(stored, storedCatalog.getDataset().get(0));
	}

	@Test
	@DisplayName("a refresh replaces the snapshot rather than adding to it")
	void refreshIsIdempotent() {
		DcatEntityRepository repository = repository(List.of(catalog("c1")), List.of(), List.of(), List.of());

		repository.refresh();
		repository.refresh();

		assertEquals(1, repository.entities(DcatPackage.Literals.CATALOG).size());
	}

	// --- helpers ----------------------------------------------------------------------

	private static DcatEntityRepository repository(List<Catalog> catalogs, List<Dataset> datasets,
			List<DatasetSeries> series, List<DataService> dataServices) {
		return new DcatEntityRepository(FakeReads.publicIris(), FakeReads.catalogs(catalogs),
				FakeReads.datasets(datasets), FakeReads.series(series), FakeReads.dataServices(dataServices), true);
	}

	private static Catalog catalog(String id) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, id));
		return catalog;
	}

	private static Dataset dataset(String id) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		return dataset;
	}

	private static DatasetSeries series(String id) {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout(DcatIds.logicalIri(DcatIds.DATASET_SERIES, id));
		return series;
	}

	private static String publicIri(String collection, String id) {
		return FakeReads.PUBLIC_BASE + collection + "/" + id;
	}

	private static List<String> abouts(List<EObject> entities) {
		return entities.stream().map(e -> ((rdf.IdentifiedResource) e).getAbout()).toList();
	}
}

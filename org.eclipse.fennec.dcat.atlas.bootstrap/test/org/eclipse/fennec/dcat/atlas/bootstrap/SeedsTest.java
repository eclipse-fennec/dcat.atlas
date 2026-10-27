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
package org.eclipse.fennec.dcat.atlas.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import foaf.FoafFactory;

/**
 * What a folder of XMI files means, before anything is written.
 *
 * <p>
 * The planning is separated from the writing precisely so it can be checked here: a
 * seeding run reads and understands every file first, so a folder with one bad file in it
 * stores nothing rather than half of itself.
 * </p>
 */
class SeedsTest {

	private final Map<Path, EObject> files = new LinkedHashMap<>();

	/** A catalog is a dataset in this model, so asking in the wrong order files it wrong. */
	@Test
	void aCatalogIsFiledUnderCatalogsEvenThoughACatalogIsADataset() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, "example"));
		file("catalog.xmi", catalog);

		Seed seed = plan().get(0);

		assertEquals(DcatIds.CATALOGS, seed.collection());
		assertEquals("example", seed.id());
	}

	/** So is a dataset series. */
	@Test
	void aDatasetSeriesIsFiledUnderDatasetSeries() {
		DatasetSeries series = DcatFactory.eINSTANCE.createDatasetSeries();
		series.setAbout(DcatIds.logicalIri(DcatIds.DATASET_SERIES, "yearly"));
		file("series.xmi", series);

		assertEquals(DcatIds.DATASET_SERIES, plan().get(0).collection());
	}

	@Test
	void aDataServiceIsFiledUnderDataServices() {
		DataService service = DcatFactory.eINSTANCE.createDataService();
		service.setAbout(DcatIds.logicalIri(DcatIds.DATA_SERVICES, "api"));
		file("service.xmi", service);

		assertEquals(DcatIds.DATA_SERVICES, plan().get(0).collection());
	}

	@Test
	void aPlainDatasetIsFiledUnderDatasets() {
		file("dataset.xmi", dataset("air"));

		assertEquals(DcatIds.DATASETS, plan().get(0).collection());
	}

	/**
	 * Members are seeded before the catalogs that point at them.
	 *
	 * <p>
	 * A catalog naming a member that is not stored yet is refused for referential
	 * integrity, so a folder would otherwise only work if the operator happened to name
	 * the files in an order the loader never promised.
	 * </p>
	 */
	@Test
	void membersAreSeededBeforeTheCatalogsThatPointAtThem() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, "example"));
		file("a-catalog.xmi", catalog);
		file("z-dataset.xmi", dataset("air"));

		List<String> collections = plan().stream().map(Seed::collection).toList();

		assertEquals(List.of(DcatIds.DATASETS, DcatIds.CATALOGS), collections);
	}

	/** Within one collection the folder's own order is kept, so a run is reproducible. */
	@Test
	void twoSeedsForTheSameCollectionKeepTheOrderTheyCameIn() {
		file("one.xmi", dataset("air"));
		file("two.xmi", dataset("traffic"));

		assertEquals(List.of("air", "traffic"), plan().stream().map(Seed::id).toList());
	}

	/**
	 * A seed must say what it is called.
	 *
	 * <p>
	 * An absent {@code about} is minted into a fresh UUID on an ordinary write, which is
	 * right for a client and useless here: the whole point of a seed is an identity the
	 * other systems can address, and a minted one would differ per deployment and per
	 * restart of an empty store.
	 * </p>
	 */
	@Test
	void aSeedWithoutAnAboutIsRefused() {
		file("nameless.xmi", DcatFactory.eINSTANCE.createCatalog());

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, this::plan);

		assertTrue(refused.getMessage().contains("nameless.xmi"), refused.getMessage());
		assertTrue(refused.getMessage().contains("about"), refused.getMessage());
	}

	@Test
	void aSeedWhoseIdentityIsSomebodyElsesIsRefused() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("https://somebody-else.example/catalogs/theirs");
		file("foreign.xmi", catalog);

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, this::plan);

		assertTrue(refused.getMessage().contains("foreign.xmi"), refused.getMessage());
	}

	@Test
	void twoSeedsWithTheSameIdentityAreRefused() {
		file("one.xmi", dataset("air"));
		file("two.xmi", dataset("air"));

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, this::plan);

		assertTrue(refused.getMessage().contains("one.xmi"), refused.getMessage());
		assertTrue(refused.getMessage().contains("two.xmi"), refused.getMessage());
	}

	/** The portal stores four kinds of thing; anything else has nowhere to go. */
	@Test
	void aRootThatIsNotAStoredDcatResourceIsRefused() {
		file("agent.xmi", FoafFactory.eINSTANCE.createAgent());

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, this::plan);

		assertTrue(refused.getMessage().contains("agent.xmi"), refused.getMessage());
	}

	@Test
	void aFileHoldingNothingIsRefused() {
		files.put(Path.of("empty.xmi"), null);

		IllegalArgumentException refused = assertThrows(IllegalArgumentException.class, this::plan);

		assertTrue(refused.getMessage().contains("empty.xmi"), refused.getMessage());
	}

	// --- applying the plan -------------------------------------------------

	/**
	 * A resource the portal already holds is left exactly as it is.
	 *
	 * <p>
	 * This is the rule the whole component turns on. A write replaces what is stored, so
	 * a loader that re-seeded on every restart would drop every membership the generating
	 * systems had added since — the catalog would come back empty, which is the one thing
	 * it exists to prevent.
	 * </p>
	 */
	@Test
	void aResourceThePortalAlreadyHoldsIsLeftAlone() {
		file("dataset.xmi", dataset("air"));
		RecordingTarget datasets = new RecordingTarget("air");

		List<Seed> written = Seeds.apply(plan(), Map.of(DcatIds.DATASETS, datasets));

		assertTrue(written.isEmpty(), "nothing should have been written");
		assertTrue(datasets.written.isEmpty(), "and the stored resource should be untouched");
	}

	@Test
	void aResourceThePortalDoesNotHoldIsWritten() {
		file("dataset.xmi", dataset("air"));
		RecordingTarget datasets = new RecordingTarget();

		List<Seed> written = Seeds.apply(plan(), Map.of(DcatIds.DATASETS, datasets));

		assertEquals(List.of("air"), written.stream().map(Seed::id).toList());
		assertEquals(1, datasets.written.size());
	}

	@Test
	void theWritesHappenInThePlansOrder() {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, "example"));
		file("a-catalog.xmi", catalog);
		file("z-dataset.xmi", dataset("air"));
		RecordingTarget catalogs = new RecordingTarget();
		RecordingTarget datasets = new RecordingTarget();

		List<Seed> written = Seeds.apply(plan(),
				Map.of(DcatIds.CATALOGS, catalogs, DcatIds.DATASETS, datasets));

		assertEquals(List.of("air", "example"), written.stream().map(Seed::id).toList());
	}

	// --- helpers -----------------------------------------------------------

	private List<Seed> plan() {
		return Seeds.plan(List.copyOf(files.keySet()), files::get);
	}

	private void file(String name, EObject resource) {
		files.put(Path.of(name), resource);
	}

	private static Dataset dataset(String id) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, id));
		return dataset;
	}

	/** A collection that holds the ids it was built with and remembers what it was given. */
	private static final class RecordingTarget implements SeedTarget {

		private final Map<String, Boolean> held = new HashMap<>();
		final List<EObject> written = new ArrayList<>();

		RecordingTarget(String... ids) {
			for (String id : ids) {
				held.put(id, Boolean.TRUE);
			}
		}

		@Override
		public boolean holds(String id) {
			return held.containsKey(id);
		}

		@Override
		public void write(EObject resource) {
			written.add(resource);
		}
	}
}

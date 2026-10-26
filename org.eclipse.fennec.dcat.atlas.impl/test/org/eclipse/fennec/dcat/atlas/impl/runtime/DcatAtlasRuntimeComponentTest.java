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
package org.eclipse.fennec.dcat.atlas.impl.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.Page;
import org.eclipse.fennec.dcat.atlas.api.read.PageRequest;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.ResourceDTO;
import org.junit.jupiter.api.Test;

import dcat.Catalog;
import dcat.DataService;
import dcat.Dataset;
import dcat.DatasetSeries;
import dcat.DcatFactory;
import rdf.RdfFactory;

/**
 * What the component does beyond mapping: when it reads, how it names what it found, and
 * when it tells a consumer that something moved.
 */
class DcatAtlasRuntimeComponentTest {

	/**
	 * Taking a snapshot reads nothing.
	 *
	 * <p>
	 * This is the rule the whole shape rests on: the telemetry bundle gives every gauge its
	 * own {@code () -> snapshot()}, so one export interval takes a handful of snapshots. A
	 * snapshot that listed the store would list it once per gauge per collection.
	 * </p>
	 */
	@Test
	void repeatedSnapshotsWithNoChangeReadNothing() {
		CountingDatasets datasets = new CountingDatasets(List.of("air", "traffic"));
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(datasets);

		component.snapshot();
		int afterFirst = datasets.listings.get();
		component.snapshot();
		component.snapshot();
		component.snapshot();

		assertEquals(1, afterFirst, "the first snapshot after a change reads once");
		assertEquals(afterFirst, datasets.listings.get(),
				"and the rest assemble from what it read: one export interval must not list per gauge");
	}

	/** A change makes the next snapshot read again, and only the next one. */
	@Test
	void aChangeMakesExactlyTheNextSnapshotReadAgain() {
		CountingDatasets datasets = new CountingDatasets(List.of("air"));
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(datasets);
		component.snapshot();

		component.changed();
		component.snapshot();
		component.snapshot();

		assertEquals(2, datasets.listings.get());
	}

	/**
	 * Being told about a change touches no storage at all.
	 * <p>
	 * It is called from inside {@code Store.commit}, on the thread still completing the
	 * write: reading the store from there re-enters it and the operation fails with a 500
	 * after its commit is already written.
	 */
	@Test
	void beingToldAboutAChangeReadsNothing() {
		CountingDatasets datasets = new CountingDatasets(List.of("air"));
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(datasets);
		component.snapshot();
		int beforeNotification = datasets.listings.get();

		component.changed();

		assertEquals(beforeNotification, datasets.listings.get(),
				"the notification runs inside the write; it may not touch the store");
	}

	/**
	 * Counting datasets lists their ids and loads none of them.
	 *
	 * <p>
	 * The reason the snapshot carries no dataset titles: a title has to be read out of the
	 * entity, and a portal with a few thousand datasets would read all of them after every
	 * write. Catalogues are read because there are tens of them and nothing else answers
	 * what the portal holds.
	 * </p>
	 */
	@Test
	void enumeratingDatasetsLoadsNoEntity() {
		CountingDatasets datasets = new CountingDatasets(List.of("air", "traffic"));
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(datasets);

		DcatAtlasRuntimeDTO dto = component.snapshot();

		assertEquals(2, dto.datasets.count);
		assertEquals(0, datasets.loads.get(), "the ids come from the listing; nothing is materialised");
	}

	/** Each caller gets its own DTO: the fields are public and a shared one is shared state. */
	@Test
	void everySnapshotIsAFreshObject() {
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(new CountingDatasets(List.of("air")));

		assertNotSame(component.snapshot(), component.snapshot());
	}

	/**
	 * A service arriving or leaving moves the change count.
	 *
	 * <p>
	 * Which is why every reference is bound by method. A field-injected one is set without
	 * telling anybody, so a collection service that came up late left the snapshot reporting
	 * an empty portal and no consumer was told it had stopped being true.
	 * </p>
	 */
	@Test
	void aCollectionServiceComingAndGoingMovesTheChangeCount() {
		DcatAtlasRuntimeComponent component = component();
		CountingDatasets datasets = new CountingDatasets(List.of("air"));

		long before = component.snapshot().changeCount;
		component.setDatasets(datasets);
		long afterBind = component.snapshot().changeCount;
		component.unsetDatasets(datasets);
		long afterUnbind = component.snapshot().changeCount;

		assertTrue(afterBind > before, "a collection coming up changes what the portal holds");
		assertTrue(afterUnbind > afterBind, "and so does one going away");
	}

	/** Nothing bound is an empty portal, not a broken one. */
	@Test
	void aPortalWithNoCollectionServicesStillAnswers() {
		DcatAtlasRuntimeDTO dto = component().snapshot();

		assertEquals(0, dto.catalogs.count);
		assertEquals(0, dto.datasets.count);
		assertEquals(0, dto.dataServices.count);
		assertEquals(0, dto.datasetSeries.count);
	}

	/** An unbind only clears the service that actually left. */
	@Test
	void unbindingAServiceThatIsNotTheBoundOneChangesNothing() {
		DcatAtlasRuntimeComponent component = component();
		CountingDatasets bound = new CountingDatasets(List.of("air"));
		component.setDatasets(bound);

		component.unsetDatasets(new CountingDatasets(List.of("traffic")));

		assertEquals(1, component.snapshot().datasets.count, "the bound service is still the bound service");
	}

	/** Every resource is named by the identity a client can dereference, the way refusals are. */
	@Test
	void resourcesAreNamedByTheirPublicIri() {
		DcatAtlasRuntimeComponent component = component();
		component.setPublicIris(new FixedPublicIris("https://daten.example.de/dcat/rest/"));
		component.setDatasets(new CountingDatasets(List.of("air")));

		ResourceDTO dataset = component.snapshot().datasets.resources.get(0);

		assertEquals("air", dataset.id);
		assertEquals("https://daten.example.de/dcat/rest/datasets/air", dataset.iri);
	}

	/** With no public base known, the logical identity is what there is to report. */
	@Test
	void withoutAPublicBaseTheLogicalIriIsReported() {
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(new CountingDatasets(List.of("air")));

		assertEquals("http://dcat.atlas/datasets/air", component.snapshot().datasets.resources.get(0).iri);
	}

	/** A catalogue's members are counted from the catalogue; nothing it points at is resolved. */
	@Test
	void aCatalogsMembersAreCountedFromTheCatalogItself() {
		DcatAtlasRuntimeComponent component = component();
		component.setPublicIris(new FixedPublicIris("https://daten.example.de/dcat/rest/"));
		component.setCatalogs(new FakeCatalogs(List.of(catalog("gov", "Offene Daten", 2, 1))));

		DcatAtlasRuntimeDTO dto = component.snapshot();

		assertEquals(1, dto.catalogs.count);
		assertEquals("gov", dto.catalogs.resources.get(0).id);
		assertEquals("https://daten.example.de/dcat/rest/catalogs/gov", dto.catalogs.resources.get(0).iri);
		assertEquals("Offene Daten", dto.catalogs.resources.get(0).title);
		assertEquals(2, dto.catalogs.resources.get(0).datasets);
		assertEquals(1, dto.catalogs.resources.get(0).dataServices);
	}

	/** A collection that cannot be listed is reported as empty, not as a thrown snapshot. */
	@Test
	void aCollectionThatThrowsLeavesTheRestOfTheSnapshotStanding() {
		DcatAtlasRuntimeComponent component = component();
		component.setDatasets(new BrokenDatasets());
		component.setDataServices(new FakeDataServices(List.of("api")));

		DcatAtlasRuntimeDTO dto = component.snapshot();

		assertEquals(0, dto.datasets.count, "what could not be listed holds nothing as far as we know");
		assertEquals(1, dto.dataServices.count, "and the rest of the inventory is still reported");
	}

	/** The four collections are read independently and reported in their own fields. */
	@Test
	void allFourCollectionsAreReported() {
		DcatAtlasRuntimeComponent component = component();
		component.setCatalogs(new FakeCatalogs(List.of(catalog("gov", "Offene Daten", 0, 0))));
		component.setDatasets(new CountingDatasets(List.of("air", "traffic")));
		component.setDataServices(new FakeDataServices(List.of("api")));
		component.setDatasetSeries(new FakeDatasetSeries(List.of("air-2024", "air-2025", "air-2026")));

		DcatAtlasRuntimeDTO dto = component.snapshot();

		assertEquals(1, dto.catalogs.count);
		assertEquals(2, dto.datasets.count);
		assertEquals(1, dto.dataServices.count);
		assertEquals(3, dto.datasetSeries.count);
		assertEquals(List.of("air", "traffic"),
				dto.datasets.resources.stream().map(r -> r.id).collect(Collectors.toList()));
	}

	// --- fixtures -----------------------------------------------------------

	private static DcatAtlasRuntimeComponent component() {
		return new DcatAtlasRuntimeComponent();
	}

	private static Catalog catalog(String id, String title, int datasets, int services) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout("http://dcat.atlas/catalogs/" + id);
		catalog.getTitle().add(literal(title));
		for (int i = 0; i < datasets; i++) {
			Dataset dataset = DcatFactory.eINSTANCE.createDataset();
			dataset.setAbout("http://dcat.atlas/datasets/" + id + "-" + i);
			catalog.getDataset().add(dataset);
		}
		for (int i = 0; i < services; i++) {
			DataService service = DcatFactory.eINSTANCE.createDataService();
			service.setAbout("http://dcat.atlas/data-services/" + id + "-" + i);
			catalog.getService().add(service);
		}
		return catalog;
	}

	private static rdf.PlainLiteral literal(String value) {
		rdf.PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
		literal.setLang("de");
		literal.setValue(value);
		return literal;
	}

	/** Translates under one base and passes everything else through, as the real one does. */
	private static final class FixedPublicIris implements PublicIris {

		private final String publicBase;

		FixedPublicIris(String publicBase) {
			this.publicBase = publicBase;
		}

		@Override
		public String toPublic(String iri) {
			return iri != null && iri.startsWith("http://dcat.atlas/")
					? publicBase + iri.substring("http://dcat.atlas/".length())
					: iri;
		}

		@Override
		public String toLogical(String iri) {
			return iri;
		}

		@Override
		public boolean isOwned(String iri) {
			return iri != null && iri.startsWith("http://dcat.atlas/");
		}

		@Override
		public String publicBase() {
			return publicBase;
		}
	}

	/** Counts what the component asked of it: listings taken, entities materialised. */
	private static final class CountingDatasets implements DatasetReadOnlyService {

		final AtomicInteger listings = new AtomicInteger();
		final AtomicInteger loads = new AtomicInteger();
		private final List<String> ids;

		CountingDatasets(List<String> ids) {
			this.ids = ids;
		}

		@Override
		public List<String> ids() {
			listings.incrementAndGet();
			return ids;
		}

		@Override
		public Optional<Dataset> getDataset(String id) {
			loads.incrementAndGet();
			return Optional.empty();
		}

		@Override
		public List<Dataset> listDatasets() {
			loads.incrementAndGet();
			return List.of();
		}

		@Override
		public Page<Dataset> listDatasets(PageRequest page) {
			loads.incrementAndGet();
			return Page.empty();
		}

		@Override
		public Optional<String> etag(String id) {
			return Optional.empty();
		}
	}

	/** A collection whose listing fails, the way an unreachable store makes it fail. */
	private static final class BrokenDatasets implements DatasetReadOnlyService {

		@Override
		public List<String> ids() {
			throw new IllegalStateException("the repository is not reachable");
		}

		@Override
		public Optional<Dataset> getDataset(String id) {
			return Optional.empty();
		}

		@Override
		public List<Dataset> listDatasets() {
			return List.of();
		}

		@Override
		public Page<Dataset> listDatasets(PageRequest page) {
			return Page.empty();
		}

		@Override
		public Optional<String> etag(String id) {
			return Optional.empty();
		}
	}

	private static final class FakeCatalogs implements CatalogReadOnlyService {

		private final List<Catalog> catalogs;

		FakeCatalogs(List<Catalog> catalogs) {
			this.catalogs = catalogs;
		}

		@Override
		public List<Catalog> listCatalogs() {
			return catalogs;
		}

		@Override
		public List<String> ids() {
			return catalogs.stream().map(c -> c.getAbout().substring(c.getAbout().lastIndexOf('/') + 1))
					.collect(Collectors.toList());
		}

		@Override
		public Optional<Catalog> getCatalog(String id) {
			return Optional.empty();
		}

		@Override
		public Page<Catalog> listCatalogs(PageRequest page) {
			return Page.empty();
		}

		@Override
		public Optional<String> etag(String id) {
			return Optional.empty();
		}
	}

	private static final class FakeDataServices implements DataServiceReadOnlyService {

		private final List<String> ids;

		FakeDataServices(List<String> ids) {
			this.ids = ids;
		}

		@Override
		public List<String> ids() {
			return ids;
		}

		@Override
		public Optional<DataService> getDataService(String id) {
			return Optional.empty();
		}

		@Override
		public List<DataService> listDataServices() {
			return List.of();
		}

		@Override
		public Page<DataService> listDataServices(PageRequest page) {
			return Page.empty();
		}

		@Override
		public Optional<String> etag(String id) {
			return Optional.empty();
		}
	}

	private static final class FakeDatasetSeries implements DatasetSeriesReadOnlyService {

		private final List<String> ids;

		FakeDatasetSeries(List<String> ids) {
			this.ids = ids;
		}

		@Override
		public List<String> ids() {
			return ids;
		}

		@Override
		public Optional<DatasetSeries> getDatasetSeries(String id) {
			return Optional.empty();
		}

		@Override
		public List<DatasetSeries> listDatasetSeries() {
			return List.of();
		}

		@Override
		public Page<DatasetSeries> listDatasetSeries(PageRequest page) {
			return Page.empty();
		}

		@Override
		public Optional<String> etag(String id) {
			return Optional.empty();
		}
	}
}

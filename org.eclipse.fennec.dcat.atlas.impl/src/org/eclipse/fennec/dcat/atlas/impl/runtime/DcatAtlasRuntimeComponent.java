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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.CatalogReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreChanges;
import org.eclipse.fennec.services.runtime.ChangeCountPublisher;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import dcat.Catalog;

/**
 * Keeps an inventory of what this portal holds and publishes it with a change count.
 *
 * <p>
 * <strong>It registers the service itself rather than letting DS do it</strong>, and that is
 * the one thing here that is not a preference. {@code service.changecount} can only be moved
 * through {@code ServiceRegistration.setProperties}, and a DS-declared service hands out no
 * registration to call it on. So the component activates, registers through
 * {@link ChangeCountPublisher} — which already does this for the broker and client runtimes
 * and coalesces the updates onto a daemon thread — and keeps the handle. {@code service = {}}
 * on the annotation is load-bearing: without it DS would register a second copy of this
 * service beside the publisher's, one with a change count that moves and one with properties
 * that never do, and a consumer binding the wrong one would be told nothing for the lifetime
 * of the framework.
 * </p>
 *
 * <p>
 * <strong>Everything it reports is optional, and bound by method.</strong> Optional because a
 * runtime service that needed a collection in order to start would be absent in exactly the
 * situation it exists to report. By method because binding is itself a change: a field is
 * injected without telling anybody, so a collection service arriving late left the snapshot
 * reporting an empty portal with nothing to say it had stopped being true. Each
 * {@code set}/{@code unset} pair raises the change count, so a consumer watching
 * {@code service.changecount} learns about it.
 * </p>
 *
 * <p>
 * <strong>Reading happens on change; assembly happens per call.</strong>
 * {@link #currentReadings()} does the work that touches the store, and runs when a write
 * lands or a service comes and goes. {@link #snapshot()} assembles a fresh DTO from what was
 * read, so it costs an allocation and nothing else — which is what makes the telemetry
 * consumer affordable, since it takes one snapshot per gauge per export interval. A fresh DTO
 * per call also means no reader holds the instance another reader could change: a DTO has
 * public fields.
 * </p>
 */
@Component(name = "DcatAtlasRuntime", immediate = true, service = {})
public class DcatAtlasRuntimeComponent implements DcatAtlasRuntime {

	private static final Logger LOG = Logger.getLogger(DcatAtlasRuntimeComponent.class.getName());

	private final AtomicLong changeCount = new AtomicLong();

	private volatile PublicIris publicIris;

	private volatile CatalogReadOnlyService catalogs;

	private volatile DatasetReadOnlyService datasets;

	private volatile DataServiceReadOnlyService dataServices;

	private volatile DatasetSeriesReadOnlyService datasetSeries;

	private ChangeCountPublisher<DcatAtlasRuntime> publisher;

	private AutoCloseable commitHook;

	/** What was last read. Replaced wholesale, never mutated in place. */
	private volatile Readings readings = Readings.empty();

	/**
	 * Something changed and the readings have not caught up.
	 *
	 * <p>
	 * <strong>Why the reading is deferred rather than done when the change arrives.</strong>
	 * The notification fires inside {@code Store.commit}, on the thread that is still
	 * completing the write. Reading the store from there re-enters it — a listing per
	 * collection and a load per catalogue, through the same resource set the operation is
	 * still using — and the operation fails with a {@code 500} after its commit has already
	 * been written. Measured, not theorised: it broke six write tests the first time this
	 * was built the other way round.
	 * </p>
	 *
	 * <p>
	 * So a change costs a flag and an increment, and the reading happens on the next
	 * snapshot that needs it. The cost rule survives: a burst of snapshots with nothing
	 * happening in between reads nothing, so an export interval that takes one snapshot per
	 * gauge still reads at most once, and only if the portal changed.
	 * </p>
	 */
	private volatile boolean stale = true;

	@Activate
	void activate(BundleContext context) {
		// Every write in this portal ends in one commit, and that is where the inventory is
		// told. Polling instead would re-read the tree once per gauge.
		commitHook = StoreChanges.onCommit(this::changed);

		publisher = new ChangeCountPublisher<>(context, DcatAtlasRuntime.class, this, changeCount::get,
				"DCAT.Atlas runtime service");
		publisher.open();
	}

	@Deactivate
	void deactivate() {
		closeQuietly(commitHook);
		if (publisher != null) {
			publisher.close();
		}
	}

	@Reference(name = "publicIris", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	void setPublicIris(PublicIris publicIris) {
		this.publicIris = publicIris;
		changed();
	}

	void unsetPublicIris(PublicIris publicIris) {
		if (this.publicIris == publicIris) {
			this.publicIris = null;
		}
		changed();
	}

	@Reference(name = "catalogs", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	void setCatalogs(CatalogReadOnlyService catalogs) {
		this.catalogs = catalogs;
		changed();
	}

	void unsetCatalogs(CatalogReadOnlyService catalogs) {
		if (this.catalogs == catalogs) {
			this.catalogs = null;
		}
		changed();
	}

	@Reference(name = "datasets", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	void setDatasets(DatasetReadOnlyService datasets) {
		this.datasets = datasets;
		changed();
	}

	void unsetDatasets(DatasetReadOnlyService datasets) {
		if (this.datasets == datasets) {
			this.datasets = null;
		}
		changed();
	}

	@Reference(name = "dataServices", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	void setDataServices(DataServiceReadOnlyService dataServices) {
		this.dataServices = dataServices;
		changed();
	}

	void unsetDataServices(DataServiceReadOnlyService dataServices) {
		if (this.dataServices == dataServices) {
			this.dataServices = null;
		}
		changed();
	}

	@Reference(name = "datasetSeries", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.OPTIONAL)
	void setDatasetSeries(DatasetSeriesReadOnlyService datasetSeries) {
		this.datasetSeries = datasetSeries;
		changed();
	}

	void unsetDatasetSeries(DatasetSeriesReadOnlyService datasetSeries) {
		if (this.datasetSeries == datasetSeries) {
			this.datasetSeries = null;
		}
		changed();
	}

	@Override
	public DcatAtlasRuntimeDTO snapshot() {
		Readings current = currentReadings();
		return SnapshotBuilder.build(changeCount.get(), System.currentTimeMillis(), current.catalogs(),
				current.datasets(), current.dataServices(), current.datasetSeries());
	}

	/**
	 * Something changed: raise the count now, read later.
	 *
	 * <p>
	 * Called from the commit hook and whenever a service comes or goes. It does no I/O at
	 * all — see {@link #stale} for why that is not an optimisation.
	 * </p>
	 */
	void changed() {
		stale = true;
		changeCount.incrementAndGet();
	}

	/**
	 * The readings, re-read first if anything has happened since they were taken.
	 *
	 * <p>
	 * The expensive half, and the only half that touches the store: a listing per collection
	 * and one read per catalogue, with no dataset ever loaded.
	 * </p>
	 */
	private Readings currentReadings() {
		if (!stale) {
			return readings;
		}
		// Cleared before the read, not after: a change that lands while this one is running
		// has to leave the flag set, or it would be read over and lost.
		stale = false;
		readings = new Readings(catalogs(), ids(datasets, DcatIds.DATASETS, DatasetReadOnlyService::ids),
				ids(dataServices, DcatIds.DATA_SERVICES, DataServiceReadOnlyService::ids),
				ids(datasetSeries, DcatIds.DATASET_SERIES, DatasetSeriesReadOnlyService::ids));
		return readings;
	}

	/**
	 * What one collection holds, named both ways and without loading anything.
	 *
	 * <p>
	 * The ids come from the collection's listing; the IRI beside each one is a string built
	 * from it, so enumerating a collection of any size still costs one listing and no entity
	 * read. The pair is what makes the inventory usable: the id is what a store operation
	 * takes, the IRI is what a caller can dereference.
	 * </p>
	 *
	 * <p>
	 * A collection whose listing fails is reported as holding nothing rather than taking the
	 * snapshot down with it. The portal's health checks are what say a store is unwell; an
	 * inventory says what it was able to see.
	 * </p>
	 */
	private <S> List<ResourceReading> ids(S service, String collection, Function<S, List<String>> lister) {
		if (service == null) {
			return List.of();
		}
		List<String> ids;
		try {
			ids = lister.apply(service);
		} catch (RuntimeException unreadable) {
			LOG.warning("[DCAT.Atlas] the " + collection + " collection could not be listed for the runtime "
					+ "inventory, reporting it as empty: " + unreadable);
			return List.of();
		}
		List<ResourceReading> readings = new ArrayList<>(ids.size());
		for (String id : ids) {
			readings.add(new ResourceReading(id, publicIri(collection, id)));
		}
		return readings;
	}

	/**
	 * The catalogues, which are the one kind of resource this reads rather than only counts.
	 *
	 * <p>
	 * There are tens of them and the question "what is in this portal" cannot be answered
	 * without them. Their members are counted from the catalogue that is already in hand —
	 * nothing a catalogue points at is resolved, which would load every dataset in the
	 * portal.
	 * </p>
	 */
	private List<CatalogReading> catalogs() {
		CatalogReadOnlyService service = catalogs;
		if (service == null) {
			return List.of();
		}
		List<Catalog> stored;
		try {
			stored = service.listCatalogs();
		} catch (RuntimeException unreadable) {
			LOG.warning("[DCAT.Atlas] the catalogs collection could not be read for the runtime inventory, "
					+ "reporting it as empty: " + unreadable);
			return List.of();
		}
		List<CatalogReading> readings = new ArrayList<>(stored.size());
		for (Catalog catalog : stored) {
			String about = catalog.getAbout();
			readings.add(new CatalogReading(DcatIds.idOf(DcatIds.CATALOGS, about), toPublic(about),
					firstTitle(catalog), catalog.getDataset().size(), catalog.getService().size(),
					catalog.getCatalog().size(), catalog.getRecord().size()));
		}
		return readings;
	}

	/** Named by the identity a client can dereference, the way refusals are (#46). */
	private String publicIri(String collection, String id) {
		return toPublic(DcatIds.logicalIri(collection, id));
	}

	private String toPublic(String logical) {
		PublicIris iris = publicIris;
		return iris == null || logical == null ? logical : iris.toPublic(logical);
	}

	private static String firstTitle(Catalog catalog) {
		return catalog.getTitle().isEmpty() ? null : catalog.getTitle().get(0).getValue();
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception ignored) {
			// Deactivation is not a place to fail.
		}
	}

	/** Everything the snapshot reports, as it was last read. */
	record Readings(List<CatalogReading> catalogs, List<ResourceReading> datasets,
			List<ResourceReading> dataServices, List<ResourceReading> datasetSeries) {

		static Readings empty() {
			return new Readings(List.of(), List.of(), List.of(), List.of());
		}
	}
}

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
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.eclipse.fennec.dcat.atlas.api.read.DataServiceReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DatasetSeriesReadOnlyHandler;
import org.eclipse.fennec.dcat.atlas.api.read.DcatReadOnlyService;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatChangeNotifierTopics;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import dcat.Catalog;

/**
 * Keeps an inventory of what this portal holds and publishes it with a change count.
 *
 * <p>
 * <strong>It registers the service itself rather than letting DS do it</strong>, and that is
 * the one thing here that is not a preference. {@code service.changecount} can only be moved
 * through {@link ServiceRegistration#setProperties}, and a DS-declared service hands out no
 * registration to call it on. So the component activates, registers
 * {@link DcatAtlasRuntime} itself and keeps the handle. What DS does declare is the
 * {@link EventHandler} — the service this component <em>is</em>, as far as DS is concerned —
 * and the {@code @Capability} above is what tells a resolver about the other one, which no
 * component description mentions.
 * </p>
 *
 * <p>
 * <strong>It learns that something happened by being told, not by watching.</strong> An admin
 * service that has finished a write announces it through {@code DcatChangeNotifier}, and this
 * component handles every topic under {@link DcatChangeNotifierTopics#BASE_TOPIC} the same
 * way: whatever it was, the inventory is out of date. The hop through EventAdmin is also what
 * keeps the reading off the writing thread — see {@link #stale}.
 * </p>
 *
 * <p>
 * <strong>Reading happens on change; assembly happens per call.</strong>
 * {@link #currentReadings()} does the work that touches the store, and runs on the first
 * snapshot after a write landed. {@link #snapshot()} assembles a fresh DTO from what was
 * read, so it costs an allocation and nothing else — which is what makes the telemetry
 * consumer affordable, since it takes one snapshot per gauge per export interval. A fresh DTO
 * per call also means no reader holds the instance another reader could change: a DTO has
 * public fields.
 * </p>
 */
@Capability(namespace = "osgi.service",
		attribute = { "objectClass:List<String>=\"org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime\"",
				"uses:=org.eclipse.fennec.dcat.atlas.api.runtime" })
@Component(name = "DcatAtlasRuntime", immediate = true, service = EventHandler.class,
		property = EventConstants.EVENT_TOPIC + "=" + DcatChangeNotifierTopics.BASE_TOPIC + "*")
public class DcatAtlasRuntimeComponent implements DcatAtlasRuntime, EventHandler {

	private static final Logger LOG = Logger.getLogger(DcatAtlasRuntimeComponent.class.getName());

	private final AtomicLong changeCount = new AtomicLong();

	@Reference
	private PublicIris publicIris;

	/**
	 * The read side as one service: the inventory names what the portal holds, so the four
	 * collections it counts have to come from the same store.
	 */
	@Reference
	private DcatReadOnlyService dcatReadOnlyService;

	private ServiceRegistration<DcatAtlasRuntime> runtimeServiceRegistration;

	/** What was last read. Replaced wholesale, never mutated in place. */
	private volatile Readings readings = Readings.empty();

	/**
	 * Something changed and the readings have not caught up.
	 *
	 * <p>
	 * <strong>Why the reading is deferred rather than done when the change arrives.</strong>
	 * Reading the store from inside a write re-enters it — a listing per collection and a
	 * load per catalogue, through the same resource set the operation is still using — and
	 * the operation fails with a {@code 500} after its commit has already been written.
	 * Measured, not theorised: it broke six write tests the first time this was built with
	 * the reading hung directly off the commit. Posting the event covers half of that, by
	 * taking the announcement off the writing thread; the flag covers the rest, by not
	 * reading until somebody asks.
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
		runtimeServiceRegistration = context.registerService(DcatAtlasRuntime.class, this,
				withChangeCount(changeCount.get()));
	}

	@Deactivate
	void deactivate() {
		runtimeServiceRegistration.unregister();
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
	synchronized void changed() {
		stale = true;
		runtimeServiceRegistration.setProperties(withChangeCount(changeCount.incrementAndGet()));
	}

	/**
	 * The registration's properties, which are the change count and nothing else.
	 * <p>
	 * Built fresh rather than read back from the reference and amended: everything else on a
	 * registration is set by the framework, and {@code setProperties} ignores those keys
	 * anyway, so carrying them along would only suggest they were being maintained here.
	 */
	private static Hashtable<String, Object> withChangeCount(long count) {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(Constants.SERVICE_CHANGECOUNT, count);
		return properties;
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
		readings = new Readings(catalogs(),
				ids(dcatReadOnlyService.getDatasetReadOnlyHandler(), DcatIds.DATASETS, DatasetReadOnlyHandler::ids),
				ids(dcatReadOnlyService.getDataServiceReadOnlyHandler(), DcatIds.DATA_SERVICES, DataServiceReadOnlyHandler::ids),
				ids(dcatReadOnlyService.getDataSeriesReadOnlyHandler(), DcatIds.DATASET_SERIES, DatasetSeriesReadOnlyHandler::ids));
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
		List<Catalog> stored;
		try {
			stored = dcatReadOnlyService.getCatalogReadOnlyHandler().listCatalogs();
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
		return logical == null ? null : publicIris.toPublic(logical);
	}

	private static String firstTitle(Catalog catalog) {
		return catalog.getTitle().isEmpty() ? null : catalog.getTitle().get(0).getValue();
	}


	/** Everything the snapshot reports, as it was last read. */
	record Readings(List<CatalogReading> catalogs, List<ResourceReading> datasets,
			List<ResourceReading> dataServices, List<ResourceReading> datasetSeries) {

		static Readings empty() {
			return new Readings(List.of(), List.of(), List.of(), List.of());
		}
	}

	/**
	 * Any announcement at all means the inventory is out of date.
	 * <p>
	 * The topic and the resource it names are deliberately not looked at: this component
	 * re-reads the whole portal, so there is nothing a narrower reaction could save.
	 */
	@Override
	public void handleEvent(Event event) {
		changed();
	}
}

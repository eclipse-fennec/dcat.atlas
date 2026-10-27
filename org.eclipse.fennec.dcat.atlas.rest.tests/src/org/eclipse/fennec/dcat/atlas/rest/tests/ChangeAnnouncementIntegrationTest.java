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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatChangeNotifierTopics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;

/**
 * What a write announces, read off the real EventAdmin.
 *
 * <p>
 * This is the only place the announcements can honestly be checked. The notifier posts into
 * whatever {@code EventAdmin} DS injected, and Felix's cannot run outside a framework — it is
 * built from a {@code BundleContext} and finds its handlers by tracking services — so the
 * plain JUnit tests in the impl bundle wire one that drops everything and assert nothing
 * about it. Here the real one is running, this test is an ordinary subscriber, and what it
 * receives is what any consumer would.
 * </p>
 *
 * <p>
 * It therefore has to wait, which is itself part of the contract: posting hands the event to
 * EventAdmin's delivery thread so the write returns without a consumer reading the store from
 * inside it.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ChangeAnnouncementIntegrationTest {

	/** Long enough for a posted event to be delivered on an unloaded machine, short enough to fail. */
	private static final Duration DELIVERY = Duration.ofSeconds(5);

	@InjectService
	DatasetAdminService datasets;

	@InjectService
	CatalogAdminService catalogs;

	private final List<Event> announced = Collections.synchronizedList(new ArrayList<>());

	private ServiceRegistration<EventHandler> subscription;
	private String datasetWritten;
	private String catalogWritten;

	@BeforeEach
	void subscribe(@InjectBundleContext BundleContext context) {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, DcatChangeNotifierTopics.BASE_TOPIC + "*");
		subscription = context.registerService(EventHandler.class, announced::add, properties);
	}

	@AfterEach
	void removeWhatThisTestWrote() {
		subscription.unregister();
		if (catalogWritten != null) {
			try {
				catalogs.deleteCatalog(catalogWritten, true);
			} catch (RuntimeException alreadyGone) {
				// The store is shared with the live runtime; cleaning up is best effort.
			}
			catalogWritten = null;
		}
		if (datasetWritten != null) {
			try {
				datasets.deleteDataset(datasetWritten, true);
			} catch (RuntimeException alreadyGone) {
				// The store is shared with the live runtime; cleaning up is best effort.
			}
			datasetWritten = null;
		}
	}

	/**
	 * A stored resource is announced as changed, and named both ways.
	 * <p>
	 * An id on its own does not identify anything — the collections have separate id spaces
	 * — so a consumer that wants to act on one resource rather than re-read everything needs
	 * the collection with it.
	 */
	@Test
	void aWriteAnnouncesWhatItStoredAndWhereItLives() {
		writeDataset();

		Event change = awaitOne(DcatChangeNotifierTopics.CHANGE_TOPIC);

		assertEquals(DcatIds.DATASETS, change.getProperty(DcatChangeNotifierTopics.ENTITY_COLLECTION));
		assertEquals(datasetWritten, change.getProperty(DcatChangeNotifierTopics.ENTITY_ID));
	}

	/**
	 * A membership announces the <em>member</em> that moved, not the container.
	 * <p>
	 * The catalog changed too, but the dataset is what a consumer has to go and look at: it
	 * is now published, and that is the fact the link created.
	 */
	@Test
	void aLinkAnnouncesTheMemberItLinked() {
		writeDataset();
		writeCatalog();
		awaitDelivery(() -> announced.size() >= 2, "both writes to be announced");
		announced.clear();

		catalogs.linkDatasetToCatalog(catalogWritten, datasetWritten);

		Event link = awaitOne(DcatChangeNotifierTopics.LINK_TOPIC);
		assertEquals(DcatIds.DATASETS, link.getProperty(DcatChangeNotifierTopics.ENTITY_COLLECTION));
		assertEquals(datasetWritten, link.getProperty(DcatChangeNotifierTopics.ENTITY_ID));
	}

	/**
	 * A repeat announces nothing, because it changed nothing.
	 * <p>
	 * The same write that leaves the ETag alone has to leave the announcements alone: a
	 * consumer re-reads the whole portal on one, and re-linking every member is what a
	 * publisher does on every sync.
	 */
	@Test
	void aRepeatedLinkAnnouncesNothing() {
		writeDataset();
		writeCatalog();
		catalogs.linkDatasetToCatalog(catalogWritten, datasetWritten);
		awaitDelivery(() -> announced.size() >= 3, "the link to be announced");
		announced.clear();

		catalogs.linkDatasetToCatalog(catalogWritten, datasetWritten);

		// Nothing to wait for: the assertion is that nothing is coming. Give delivery the
		// window it would have had, so a regression fails here rather than racing past.
		sleep(DELIVERY.toMillis() / 5);
		assertTrue(announced.isEmpty(), () -> "a link that changed nothing announced " + topics());
	}

	/** An unlink says so, rather than announcing itself as a link. */
	@Test
	void anUnlinkAnnouncesItsOwnTopic() {
		writeDataset();
		writeCatalog();
		catalogs.linkDatasetToCatalog(catalogWritten, datasetWritten);
		awaitDelivery(() -> announced.size() >= 3, "the link to be announced");
		announced.clear();

		catalogs.deleteDatasetFromCatalog(catalogWritten, datasetWritten);

		Event unlink = awaitOne(DcatChangeNotifierTopics.UNLINK_TOPIC);
		assertEquals(datasetWritten, unlink.getProperty(DcatChangeNotifierTopics.ENTITY_ID));
	}

	// --- helpers -----------------------------------------------------------

	private void writeDataset() {
		datasetWritten = "announce-" + UUID.randomUUID();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, datasetWritten));
		RestEntities.mandatoryDataset(dataset, "Change announcement probe");
		datasets.upsertDataset(dataset);
	}

	private void writeCatalog() {
		catalogWritten = "announce-cat-" + UUID.randomUUID();
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, catalogWritten));
		RestEntities.mandatoryDataset(catalog, "Change announcement probe");
		catalogs.upsertCatalog(catalog);
	}

	/** The one event on {@code topic}, once it has been delivered. */
	private Event awaitOne(String topic) {
		awaitDelivery(() -> announced.stream().anyMatch(event -> topic.equals(event.getTopic())),
				"an event on " + topic);
		List<Event> matching;
		synchronized (announced) {
			matching = announced.stream().filter(event -> topic.equals(event.getTopic())).toList();
		}
		assertEquals(1, matching.size(), () -> "exactly one " + topic + " was expected, got " + topics());
		return matching.get(0);
	}

	private void awaitDelivery(BooleanSupplier arrived, String what) {
		long deadline = System.nanoTime() + DELIVERY.toNanos();
		while (System.nanoTime() < deadline) {
			if (arrived.getAsBoolean()) {
				return;
			}
			sleep(20);
		}
		assertTrue(arrived.getAsBoolean(), () -> what + " within " + DELIVERY + "; announced " + topics());
	}

	private List<String> topics() {
		synchronized (announced) {
			return announced.stream().map(Event::getTopic).toList();
		}
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new AssertionError("interrupted", interrupted);
		}
	}
}

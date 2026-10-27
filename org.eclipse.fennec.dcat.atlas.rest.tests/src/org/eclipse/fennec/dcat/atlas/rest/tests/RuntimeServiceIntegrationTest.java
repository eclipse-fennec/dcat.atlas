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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.eclipse.fennec.dcat.atlas.api.admin.CatalogAdminService;
import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;

/**
 * The runtime service in a real framework.
 *
 * <p>
 * Everything here is something a plain unit test cannot see: that the component registers
 * {@code DcatAtlasRuntime} at all, when the only service DS declares for it is an
 * {@code EventHandler}; that the change count travels as a service property and moves; and
 * that what the inventory reports matches what the portal actually holds.
 * </p>
 *
 * <p>
 * <strong>It also has to wait, and that is the design showing through.</strong> A write
 * announces itself by posting to EventAdmin, which delivers on its own thread — that hop is
 * what keeps the inventory from re-entering the store it is reading. So a write returns
 * before the component has been told about it, and a test that asked for the snapshot on the
 * next line would be reading a portal that is correct but not yet caught up.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class RuntimeServiceIntegrationTest {

	@InjectService
	DcatAtlasRuntime runtime;

	@InjectService
	DatasetAdminService datasets;

	@InjectService
	CatalogAdminService catalogs;

	/** Long enough for a posted event to be delivered on an unloaded machine, short enough to fail. */
	private static final Duration DELIVERY = Duration.ofSeconds(5);

	private String written;
	private String catalogWritten;

	@AfterEach
	void removeWhatThisTestWrote() {
		if (catalogWritten != null) {
			try {
				catalogs.deleteCatalog(catalogWritten, true);
			} catch (RuntimeException alreadyGone) {
				// The store is shared with the live runtime; cleaning up is best effort.
			}
			catalogWritten = null;
		}
		if (written != null) {
			try {
				datasets.deleteDataset(written, true);
			} catch (RuntimeException alreadyGone) {
				// The store is shared with the live runtime; cleaning up is best effort.
			}
			written = null;
		}
	}

	/**
	 * Waits for something that only becomes true once a posted event has been delivered.
	 *
	 * <p>
	 * Polling rather than listening: what is being waited for is the <em>effect</em> of the
	 * delivery on the runtime service, and that is the thing under test. A latch on the event
	 * itself would pass while the component was still none the wiser.
	 * </p>
	 */
	private static void awaitDelivery(BooleanSupplier caughtUp, String what) {
		long deadline = System.nanoTime() + DELIVERY.toNanos();
		while (System.nanoTime() < deadline) {
			if (caughtUp.getAsBoolean()) {
				return;
			}
			try {
				Thread.sleep(20);
			} catch (InterruptedException interrupted) {
				Thread.currentThread().interrupt();
				throw new AssertionError("interrupted while waiting for " + what, interrupted);
			}
		}
		assertTrue(caughtUp.getAsBoolean(), () -> what + " within " + DELIVERY);
	}

	/**
	 * Waits until the change count has stopped moving, and answers where it stopped.
	 *
	 * <p>
	 * Needed before a test can assert that something did <em>not</em> move it: the writes
	 * that set the test up announce themselves too, and an announcement still in flight would
	 * otherwise be read as the one the test is claiming never happened.
	 * </p>
	 */
	private long awaitQuiet() {
		long seen = runtime.snapshot().changeCount;
		long deadline = System.nanoTime() + DELIVERY.toNanos();
		while (System.nanoTime() < deadline) {
			sleep(DELIVERY.toMillis() / 25);
			long now = runtime.snapshot().changeCount;
			if (now == seen) {
				return now;
			}
			seen = now;
		}
		throw new AssertionError("the portal never stopped announcing within " + DELIVERY);
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new AssertionError("interrupted", interrupted);
		}
	}

	/**
	 * The service is there, and it carries its change count.
	 * <p>
	 * It is registered by the component rather than by DS, which is the only way a service
	 * property can be made to move — so "is it registered" is a real question here and not a
	 * formality.
	 */
	@Test
	void theRuntimeServiceIsRegisteredWithAChangeCount(@InjectBundleContext BundleContext context) {
		assertNotNull(runtime);

		ServiceReference<DcatAtlasRuntime> reference = context.getServiceReference(DcatAtlasRuntime.class);
		assertNotNull(reference, "the component registers the service itself; DS declares none");
		assertNotNull(reference.getProperty(Constants.SERVICE_CHANGECOUNT),
				"the change count is how a consumer is told anything happened");
	}

	/** Every stored collection is reported, whether or not anything is in it. */
	@Test
	void theSnapshotReportsEveryCollection() {
		DcatAtlasRuntimeDTO snapshot = runtime.snapshot();

		assertNotNull(snapshot.catalogs, "a portal that cannot say what catalogues it holds is not configured");
		assertNotNull(snapshot.datasets);
		assertNotNull(snapshot.dataServices);
		assertNotNull(snapshot.datasetSeries);
		assertEquals(snapshot.datasets.resources.size(), snapshot.datasets.count,
				"the count is what the collection lists");
	}

	/** Each caller gets its own: a DTO has public fields, so a shared one is shared state. */
	@Test
	void everyCallerGetsItsOwnSnapshot() {
		assertNotSame(runtime.snapshot(), runtime.snapshot());
	}

	/**
	 * A write moves the change count and shows up in the inventory.
	 * <p>
	 * The whole protocol in one test: something happened, the number moved, and asking again
	 * says what.
	 */
	@Test
	void aWriteMovesTheChangeCountAndTheInventory() {
		DcatAtlasRuntimeDTO before = runtime.snapshot();
		int datasetsBefore = before.datasets.count;

		written = "runtime-" + UUID.randomUUID();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, written));
		RestEntities.mandatoryDataset(dataset, "Runtime service probe");
		datasets.upsertDataset(dataset);
		awaitDelivery(() -> runtime.snapshot().changeCount > before.changeCount,
				"a commit has to reach the inventory");

		DcatAtlasRuntimeDTO after = runtime.snapshot();

		assertEquals(datasetsBefore + 1, after.datasets.count,
				"the new dataset should be counted from the store listing");

		List<String> ids = after.datasets.resources.stream().map(r -> r.id).toList();
		assertTrue(ids.contains(written), "and named: " + ids);

		List<String> iris = after.datasets.resources.stream().map(r -> r.iri).toList();
		assertTrue(iris.stream().anyMatch(iri -> iri != null && iri.endsWith("/datasets/" + written)),
				"and by the identity a client can dereference, not only by its store id: " + iris);
	}

	/**
	 * Re-linking what is already linked announces nothing, so the change count stays put.
	 *
	 * <p>
	 * The same write that leaves the ETag alone has to leave the change count alone: a
	 * consumer re-reads the whole portal on an announcement, and re-linking every member is
	 * what a publisher does on every sync.
	 * </p>
	 */
	@Test
	void aRepeatedLinkMovesNothing() {
		written = "runtime-" + UUID.randomUUID();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, written));
		RestEntities.mandatoryDataset(dataset, "Runtime service probe");
		datasets.upsertDataset(dataset);

		catalogWritten = "runtime-cat-" + UUID.randomUUID();
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(DcatIds.logicalIri(DcatIds.CATALOGS, catalogWritten));
		RestEntities.mandatoryDataset(catalog, "Runtime service probe");
		catalogs.upsertCatalog(catalog);
		long beforeTheLink = awaitQuiet();

		catalogs.linkDatasetToCatalog(catalogWritten, written);
		awaitDelivery(() -> runtime.snapshot().changeCount > beforeTheLink, "the link to be announced");
		long settled = awaitQuiet();

		catalogs.linkDatasetToCatalog(catalogWritten, written);

		// Nothing to wait for: the assertion is that nothing is coming. Give delivery the
		// window it would have had, so a regression fails here rather than racing past.
		sleep(DELIVERY.toMillis() / 10);
		assertEquals(settled, runtime.snapshot().changeCount,
				"a link that changed nothing has nothing to announce");
	}
}

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.eclipse.fennec.dcat.atlas.api.admin.DatasetAdminService;
import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntime;
import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.api.runtime.IntegrityDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import dcat.Dataset;
import dcat.DcatFactory;

/**
 * The runtime service in a real framework.
 *
 * <p>
 * Everything here is something the unit tests cannot see: that the component registers at
 * all despite declaring {@code service = {}}, that the change count travels as a service
 * property and moves, and that what the snapshot reports matches what the portal actually
 * holds.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class RuntimeServiceIntegrationTest {

	@InjectService
	DcatAtlasRuntime runtime;

	@InjectService
	DatasetAdminService datasets;

	private String written;

	@AfterEach
	void removeWhatThisTestWrote() {
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

	@Test
	void theSnapshotKnowsWhatThePortalIsAndWhatItHolds() {
		DcatAtlasRuntimeDTO snapshot = runtime.snapshot();

		assertNotNull(snapshot.identity, "a portal that cannot say what it calls things is not configured");
		assertNotNull(snapshot.identity.logicalBase);
		assertFalse(snapshot.collections.isEmpty(), "the stores register themselves for introspection");
		assertTrue(snapshot.collections.stream().anyMatch(c -> "datasets".equals(c.name)),
				"the datasets collection should be among them: " + snapshot.collections);
		assertNotNull(snapshot.store);
		assertNotNull(snapshot.validation);
		assertNotNull(snapshot.graph);
	}

	/**
	 * The portal does not have to be told where it serves.
	 * <p>
	 * With nothing configured, the endpoints come from the Jakarta RS whiteboard's own
	 * {@code osgi.jakartars.endpoint} — the address it is actually listening on, rather
	 * than one a deployment wrote down twice and has to keep in step.
	 */
	@Test
	void theEndpointsComeFromTheRestLayerWhenNothingIsConfigured() {
		DcatAtlasRuntimeDTO snapshot = runtime.snapshot();

		assertFalse(snapshot.endpoints.isEmpty(),
				"the whiteboard is running in this framework, so it should have announced an endpoint");
	}

	/** Each collection is reported once, though a read-only and an admin store both answer for it. */
	@Test
	void eachCollectionAppearsOnce() {
		List<String> names = runtime.snapshot().collections.stream().map(c -> c.name).toList();

		assertEquals(names.stream().distinct().toList(), names, "a collection listed twice: " + names);
	}

	/** Each caller gets its own: a DTO has public fields, so a shared one is shared state. */
	@Test
	void everyCallerGetsItsOwnSnapshot() {
		assertNotSame(runtime.snapshot(), runtime.snapshot());
	}

	/**
	 * A write moves the change count and shows up in the counts.
	 * <p>
	 * The whole protocol in one test: something happened, the number moved, and asking again
	 * says what.
	 */
	@Test
	void aWriteMovesTheChangeCountAndTheCount() {
		DcatAtlasRuntimeDTO before = runtime.snapshot();
		int datasetsBefore = countOf(before, "datasets");

		written = "runtime-" + UUID.randomUUID();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, written));
		RestEntities.mandatoryDataset(dataset, "Runtime service probe");
		datasets.upsertDataset(dataset);

		DcatAtlasRuntimeDTO after = runtime.snapshot();

		assertTrue(after.changeCount > before.changeCount,
				"a commit has to reach the snapshot: " + before.changeCount + " -> " + after.changeCount);
		assertEquals(datasetsBefore + 1, countOf(after, "datasets"),
				"the new dataset should be counted from the store listing");
		assertTrue(idsOf(after, "datasets").contains(written), "and named");
	}

	/**
	 * The scan reads the content and says so.
	 * <p>
	 * Its value is that it is <em>not</em> the snapshot: it reports how many resources it
	 * read, which is the number the snapshot refuses to pay for.
	 */
	@Test
	void theIntegrityScanReadsTheContentAndReportsWhatItRead() {
		written = "integrity-" + UUID.randomUUID();
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DcatIds.logicalIri(DcatIds.DATASETS, written));
		RestEntities.mandatoryDataset(dataset, "Integrity scan probe");
		datasets.upsertDataset(dataset);

		IntegrityDTO integrity = runtime.checkIntegrity();

		assertNotNull(integrity.failures);
		assertTrue(integrity.resourcesScanned > 0,
				"the scan reads every stored resource; it should have found at least the one just written");
		assertTrue(integrity.takenAt > 0);
	}

	/** A snapshot never pays for the scan: the two are separate calls for that reason. */
	@Test
	void aSnapshotDoesNotRunTheScan() {
		DcatAtlasRuntimeDTO snapshot = runtime.snapshot();

		assertTrue(snapshot.failures.stream().noneMatch(f -> "UNPARSEABLE_RESOURCE".equals(f.reason)
				|| "DANGLING_REFERENCE".equals(f.reason) || "NON_CONFORMANT_RESOURCE".equals(f.reason)),
				"content reasons can only come from checkIntegrity(): " + snapshot.failures);
	}

	private static int countOf(DcatAtlasRuntimeDTO snapshot, String collection) {
		return snapshot.collections.stream().filter(c -> collection.equals(c.name)).mapToInt(c -> c.count).findFirst()
				.orElse(-1);
	}

	private static java.util.List<String> idsOf(DcatAtlasRuntimeDTO snapshot, String collection) {
		return snapshot.collections.stream().filter(c -> collection.equals(c.name)).map(c -> c.ids).findFirst()
				.orElse(java.util.List.of());
	}
}

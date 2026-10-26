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

import java.util.List;
import java.util.UUID;

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

import dcat.Dataset;
import dcat.DcatFactory;

/**
 * The runtime service in a real framework.
 *
 * <p>
 * Everything here is something the unit tests cannot see: that the component registers at
 * all despite declaring {@code service = {}}, that the change count travels as a service
 * property and moves, and that what the inventory reports matches what the portal actually
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

		DcatAtlasRuntimeDTO after = runtime.snapshot();

		assertTrue(after.changeCount > before.changeCount,
				"a commit has to reach the inventory: " + before.changeCount + " -> " + after.changeCount);
		assertEquals(datasetsBefore + 1, after.datasets.count,
				"the new dataset should be counted from the store listing");

		List<String> ids = after.datasets.resources.stream().map(r -> r.id).toList();
		assertTrue(ids.contains(written), "and named: " + ids);

		List<String> iris = after.datasets.resources.stream().map(r -> r.iri).toList();
		assertTrue(iris.stream().anyMatch(iri -> iri != null && iri.endsWith("/datasets/" + written)),
				"and by the identity a client can dereference, not only by its store id: " + iris);
	}
}

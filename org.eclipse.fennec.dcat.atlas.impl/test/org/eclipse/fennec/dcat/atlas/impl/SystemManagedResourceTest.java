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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.access.Caller;
import org.eclipse.fennec.dcat.atlas.api.access.SystemManagedException;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import dcat.Catalog;
import dcat.Dataset;
import dcat.DcatFactory;

/**
 * What a system generates stays the system's.
 * <p>
 * A write carries the identity the gateway authenticated it under; it is stamped onto the
 * resource as {@code managedBy}, and from then on only that same system may replace or
 * delete it.
 * <p>
 * {@code managedBy} travels in the entity, so a client can put whatever it likes in the one
 * it sends. Every test asserting on the <em>stored</em> value guards the rule that the
 * store takes it from the caller and from what is already stored, never from the body —
 * otherwise clearing the flag and deleting would be a two-step escape from it.
 */
public class SystemManagedResourceTest {

	private static final String DATASETS = StoreLayout.LOGICAL_BASE + "datasets/";
	private static final String CATALOGS = StoreLayout.LOGICAL_BASE + "catalogs/";

	/** The two generating systems the issue names, as the gateway would hand them over. */
	private static final Caller MODEL_ATLAS = Caller.of("model.atlas");
	private static final Caller DATA_ATLAS = Caller.of("data.atlas");

	@TempDir
	Path storage;

	// --- stamping -----------------------------------------------------------

	@Test
	void aSystemsCreateStampsItAsTheManagingSystem() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		assertEquals("model.atlas", datasets().getDataset("air").orElseThrow().getManagedBy());
	}

	@Test
	void aHumansCreateLeavesTheResourceUnmanaged() {
		datasets().upsertDataset(dataset("air", "Air quality"), Caller.ANONYMOUS);

		assertNull(datasets().getDataset("air").orElseThrow().getManagedBy());
	}

	@Test
	void theBodyCannotClaimAManagingSystem() {
		Dataset claiming = dataset("air", "Air quality");
		claiming.setManagedBy("model.atlas");

		datasets().upsertDataset(claiming, Caller.ANONYMOUS);

		// Stamped from the caller, not from the body - otherwise anyone could fence off a
		// resource, or (worse, below) hand themselves somebody else's.
		assertNull(datasets().getDataset("air").orElseThrow().getManagedBy());
	}

	@Test
	void theBodyCannotClearTheManagingSystem() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		// The owner replaces it with a body that says nothing about management.
		datasets().upsertDataset(dataset("air", "Air quality (v2)"), MODEL_ATLAS);

		assertEquals("model.atlas", datasets().getDataset("air").orElseThrow().getManagedBy());
	}

	@Test
	void theBodyCannotHandTheResourceToAnotherSystem() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		Dataset reassigning = dataset("air", "Air quality (v2)");
		reassigning.setManagedBy("data.atlas");
		datasets().upsertDataset(reassigning, MODEL_ATLAS);

		assertEquals("model.atlas", datasets().getDataset("air").orElseThrow().getManagedBy());
	}

	// --- the refusals -------------------------------------------------------

	@Test
	void anotherSystemCannotReplaceAManagedResource() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		SystemManagedException refused = assertThrows(SystemManagedException.class,
				() -> datasets().upsertDataset(dataset("air", "Hijacked"), DATA_ATLAS));

		assertEquals("model.atlas", refused.getManagedBy());
		assertEquals(DATASETS + "air", refused.getResource());
		// Refused before anything was staged, so the stored title is untouched.
		assertEquals("Air quality", datasets().getDataset("air").orElseThrow().getTitle().get(0).getValue());
	}

	@Test
	void aHumanCannotReplaceAManagedResource() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		assertThrows(SystemManagedException.class,
				() -> datasets().upsertDataset(dataset("air", "Edited by hand"), Caller.ANONYMOUS));
	}

	@Test
	void aHumanCannotDeleteAManagedResource() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		assertThrows(SystemManagedException.class, () -> datasets().deleteDataset("air", false, Caller.ANONYMOUS));

		assertTrue(datasets().getDataset("air").isPresent());
	}

	@Test
	void theManagingSystemReplacesAndDeletesItsOwn() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);

		datasets().upsertDataset(dataset("air", "Air quality (v2)"), MODEL_ATLAS);
		assertEquals("Air quality (v2)", datasets().getDataset("air").orElseThrow().getTitle().get(0).getValue());

		datasets().deleteDataset("air", false, MODEL_ATLAS);
		assertTrue(datasets().getDataset("air").isEmpty());
	}

	@Test
	void anUnmanagedResourceIsAnyonesToEdit() {
		datasets().upsertDataset(dataset("air", "Air quality"), Caller.ANONYMOUS);

		datasets().upsertDataset(dataset("air", "Air quality (v2)"), Caller.ANONYMOUS);
		assertEquals("Air quality (v2)", datasets().getDataset("air").orElseThrow().getTitle().get(0).getValue());

		datasets().deleteDataset("air", false, Caller.ANONYMOUS);
		assertTrue(datasets().getDataset("air").isEmpty());
	}

	@Test
	void aSystemsWriteClaimsAnUnmanagedResource() {
		datasets().upsertDataset(dataset("air", "Air quality"), Caller.ANONYMOUS);

		datasets().upsertDataset(dataset("air", "Air quality (v2)"), DATA_ATLAS);

		// Not only a create: whatever a system last wrote in full is its content. It is also
		// what marks the resources stored before management existed, with no migration step.
		assertEquals("data.atlas", datasets().getDataset("air").orElseThrow().getManagedBy());
		assertThrows(SystemManagedException.class, () -> datasets().deleteDataset("air", false, Caller.ANONYMOUS));
	}

	// --- the collateral writes ----------------------------------------------

	@Test
	void aHumanCannotAddAMemberToAManagedCatalog() {
		catalogs().upsertCatalog(catalog("gov", "GovData"), MODEL_ATLAS);

		assertThrows(SystemManagedException.class,
				() -> catalogs().addDatasetToCatalog("gov", dataset("air", "Air quality"), Caller.ANONYMOUS));

		assertTrue(catalogs().getCatalog("gov").orElseThrow().getDataset().isEmpty());
	}

	@Test
	void aCascadeCannotUnlinkAManagedCatalog() {
		// The member is nobody's; the catalog listing it is model.atlas's. Deleting the
		// member would rewrite the catalog, which is an edit of the system's content by
		// the back door - the one the UI reaches without ever naming the catalog.
		datasets().upsertDataset(dataset("air", "Air quality"), Caller.ANONYMOUS);
		catalogs().upsertCatalog(catalog("gov", "GovData"), MODEL_ATLAS);
		catalogs().linkDatasetToCatalog("gov", "air", MODEL_ATLAS);

		assertThrows(SystemManagedException.class, () -> datasets().deleteDataset("air", true, Caller.ANONYMOUS));

		assertTrue(datasets().getDataset("air").isPresent());
		assertFalse(catalogs().getCatalog("gov").orElseThrow().getDataset().isEmpty());
	}

	@Test
	void theServersOwnBookkeepingIsNotRefused() {
		// Same shape as above, but the write is allowed: an unmanaged member being edited.
		// Its catalogue record lives in the managed catalog and has to follow it, and that is
		// the server writing on its own behalf, not the caller editing the catalog.
		datasets().upsertDataset(dataset("air", "Air quality"), Caller.ANONYMOUS);
		catalogs().upsertCatalog(catalog("gov", "GovData"), MODEL_ATLAS);
		catalogs().linkDatasetToCatalog("gov", "air", MODEL_ATLAS);
		String before = catalogs().etag("gov").orElseThrow();

		datasets().upsertDataset(dataset("air", "Air quality (v2)"), Caller.ANONYMOUS);

		assertEquals("Air quality (v2)", datasets().getDataset("air").orElseThrow().getTitle().get(0).getValue());
		assertNotEquals(before, catalogs().etag("gov").orElseThrow(), "the record's modified should have moved");
		// And the bookkeeping did not quietly take the catalog away from its system.
		assertEquals("model.atlas", catalogs().getCatalog("gov").orElseThrow().getManagedBy());
	}

	@Test
	void theManagingSystemsOwnCascadeIsAllowed() {
		datasets().upsertDataset(dataset("air", "Air quality"), MODEL_ATLAS);
		catalogs().upsertCatalog(catalog("gov", "GovData"), MODEL_ATLAS);
		catalogs().linkDatasetToCatalog("gov", "air", MODEL_ATLAS);

		List<String> unlinked = datasets().deleteDataset("air", true, MODEL_ATLAS);

		assertEquals(List.of(CATALOGS + "gov"), unlinked);
		assertTrue(datasets().getDataset("air").isEmpty());
	}

	// --- fixtures -----------------------------------------------------------

	private DatasetAdminServiceImpl datasets() {
		return new DatasetAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH);
	}

	private CatalogAdminServiceImpl catalogs() {
		return new CatalogAdminServiceImpl(TestResourceSets.factory(), TestGitStore.at(storage),
				TestGitStore.BASE_PATH);
	}

	private static Dataset dataset(String id, String title) {
		Dataset dataset = DcatFactory.eINSTANCE.createDataset();
		dataset.setAbout(DATASETS + id);
		return TestEntities.mandatoryDataset(dataset, title);
	}

	private static Catalog catalog(String id, String title) {
		Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
		catalog.setAbout(CATALOGS + id);
		return TestEntities.mandatoryDataset(catalog, title);
	}
}

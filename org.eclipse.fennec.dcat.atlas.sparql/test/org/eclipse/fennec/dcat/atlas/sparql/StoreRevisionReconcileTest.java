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
package org.eclipse.fennec.dcat.atlas.sparql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.eclipse.fennec.dcat.atlas.api.identity.PublicIris;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * That the reconcile poll does no work while the store has not moved (P1-7 / Phase 2 W8).
 * <p>
 * The point of the poll is to bound how long the projection can be silently wrong, not to
 * re-read the whole store every interval forever. These tests pin the two halves of that:
 * it skips when the store version is unchanged, and it still runs whenever it cannot prove
 * that — no revision service, an unreadable one, a store with no content, or a version that
 * has actually moved.
 */
class StoreRevisionReconcileTest {

	private static final String BASE = "http://dcat.atlas/";
	private static final PublicIris IDENTITY_IRIS = new PublicIris() {

		@Override
		public String toPublic(String iri) {
			return iri;
		}

		@Override
		public String toLogical(String iri) {
			return iri;
		}

		@Override
		public boolean isOwned(String iri) {
			return false;
		}

		@Override
		public String publicBase() {
			return BASE;
		}
	};

	private FakeStores stores;

	@BeforeEach
	void setUp() {
		stores = new FakeStores();
	}

	private DcatGraphServiceImpl graph() {
		return new DcatGraphServiceImpl(IDENTITY_IRIS, stores, true, 0L, 0L);
	}

	@Test
	void anUnchangedStoreSkipsTheWork() {
		stores.revision = "commit-1";
		DcatGraphServiceImpl graph = graph();
		graph.refresh();
		// refresh() alone does not record the revision - the tick around it does.
		graph.reconcileQuietly();
		int readsAfterFirstTick = stores.catalogReads;

		graph.reconcileQuietly();
		graph.reconcileQuietly();

		assertEquals(readsAfterFirstTick, stores.catalogReads,
				"a tick with an unchanged store version must not re-read the store");
		assertTrue(graph.isReady());
	}

	@Test
	void aMovedStoreIsReconciled() {
		stores.revision = "commit-1";
		DcatGraphServiceImpl graph = graph();
		graph.reconcileQuietly();
		int readsAfterFirstTick = stores.catalogReads;

		stores.revision = "commit-2";
		graph.reconcileQuietly();

		assertTrue(stores.catalogReads > readsAfterFirstTick,
				"a tick after the store moved must re-read it");
	}

	@Test
	void withoutARevisionEveryTickReconciles() {
		DcatGraphServiceImpl graph = graph();
		graph.reconcileQuietly();
		int readsAfterFirstTick = stores.catalogReads;

		graph.reconcileQuietly();

		// The optimisation is optional; its absence must cost work, never correctness.
		assertTrue(stores.catalogReads > readsAfterFirstTick);
	}

	@Test
	void anUnreadableRevisionFallsBackToReconciling() {
		stores.revisionFailure = new IllegalStateException("repository unreachable");
		DcatGraphServiceImpl graph = graph();
		graph.reconcileQuietly();
		int readsAfterFirstTick = stores.catalogReads;

		graph.reconcileQuietly();

		assertTrue(stores.catalogReads > readsAfterFirstTick,
				"a fingerprint that cannot be read must not be taken as 'nothing changed'");
	}

	@Test
	void anEmptyRevisionIsNeverTreatedAsUnchanged() {
		// A store with no content reports no version. Two such answers are not evidence
		// that nothing changed - they are two absences of evidence.
		stores.revision = null;
		DcatGraphServiceImpl graph = graph();
		graph.reconcileQuietly();
		int readsAfterFirstTick = stores.catalogReads;

		graph.reconcileQuietly();

		assertTrue(stores.catalogReads > readsAfterFirstTick);
	}
}

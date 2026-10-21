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
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.fennec.dcat.atlas.api.runtime.DcatAtlasRuntimeDTO;
import org.eclipse.fennec.dcat.atlas.impl.store.IntegrityFinding;
import org.eclipse.fennec.dcat.atlas.impl.store.StoreIntrospection;
import org.junit.jupiter.api.Test;

/**
 * The cost rule and the change count, which are the design (D3, D4) rather than details.
 */
class DcatAtlasRuntimeComponentTest {

	/**
	 * Taking a snapshot reads nothing.
	 *
	 * <p>
	 * This is the rule the whole shape rests on: {@code RuntimeTelemetry} gives every gauge
	 * its own {@code () -> snapshot()}, so one export interval takes eight or nine snapshots.
	 * A snapshot that listed the store would list it eight or nine times a collection.
	 * </p>
	 */
	@Test
	void repeatedSnapshotsWithNoChangeReadNothingFromTheStore() {
		CountingStore store = new CountingStore("datasets", true, List.of("air", "traffic"));
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(store);

		component.snapshot();
		int afterFirst = store.listings.get();
		component.snapshot();
		component.snapshot();
		component.snapshot();

		assertEquals(1, afterFirst, "the first snapshot after a change reads the store once");
		assertEquals(afterFirst, store.listings.get(),
				"and the rest assemble from what it read: one export interval must not list the store per gauge");
	}

	/** A change makes the next snapshot read again, and only the next one. */
	@Test
	void aChangeMakesExactlyTheNextSnapshotReadAgain() {
		CountingStore store = new CountingStore("datasets", true, List.of("air"));
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(store);
		component.snapshot();

		component.changed();
		component.snapshot();
		component.snapshot();

		assertEquals(2, store.listings.get());
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
		CountingStore store = new CountingStore("datasets", true, List.of("air"));
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(store);
		component.snapshot();
		int beforeNotification = store.listings.get();

		component.changed();

		assertEquals(beforeNotification, store.listings.get(),
				"the notification runs inside the write; it may not touch the store");
	}

	/** Each caller gets its own DTO: the fields are public and a shared one is shared state. */
	@Test
	void everySnapshotIsAFreshObject() {
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(new CountingStore("datasets", true, List.of("air")));

		assertNotSame(component.snapshot(), component.snapshot());
	}

	@Test
	void aStoreArrivingAndLeavingMovesTheChangeCount() {
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		CountingStore store = new CountingStore("datasets", true, List.of("air"));

		long before = component.snapshot().changeCount;
		component.addStore(store);
		long afterAdd = component.snapshot().changeCount;
		component.removeStore(store);
		long afterRemove = component.snapshot().changeCount;

		assertTrue(afterAdd > before, "a store coming up changes what the portal holds");
		assertTrue(afterRemove > afterAdd, "and so does one going away");
	}

	@Test
	void aWriteMovesTheChangeCountWithoutAnythingElseChanging() {
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(new CountingStore("datasets", true, List.of("air")));

		long before = component.snapshot().changeCount;
		component.changed();

		assertTrue(component.snapshot().changeCount > before);
	}

	@Test
	void theCountedIdsComeFromTheStoreListing() {
		DcatAtlasRuntimeComponent component = new DcatAtlasRuntimeComponent();
		component.addStore(new CountingStore("datasets", true, List.of("air", "traffic", "noise")));

		DcatAtlasRuntimeDTO snapshot = component.snapshot();

		assertEquals(1, snapshot.collections.size());
		assertEquals(3, snapshot.collections.get(0).count);
		assertEquals(List.of("air", "traffic", "noise"), snapshot.collections.get(0).ids);
	}

	/**
	 * The whiteboard may announce its endpoint as a string, an array or a collection, and
	 * two runtimes may announce the same one.
	 */
	@Test
	void endpointsAreReadWhateverShapeTheWhiteboardUsed() {
		assertEquals(List.of("http://localhost:8085/dcat/rest"),
				DcatAtlasRuntimeComponent.endpointsOf(List.of("http://localhost:8085/dcat/rest")));

		assertEquals(List.of("http://a/rest", "http://b/rest"), DcatAtlasRuntimeComponent
				.endpointsOf(List.of(new String[] { "http://a/rest", "http://b/rest" })));

		assertEquals(List.of("http://a/rest"),
				DcatAtlasRuntimeComponent.endpointsOf(List.of(List.of("http://a/rest"))));

		assertEquals(List.of("http://a/rest"),
				DcatAtlasRuntimeComponent.endpointsOf(List.of("http://a/rest", "http://a/rest")),
				"two runtimes announcing the same endpoint is one endpoint");

		assertEquals(List.of(), DcatAtlasRuntimeComponent.endpointsOf(List.of("", "   ")),
				"a blank announcement is not an endpoint");
	}

	/** Counts how often it was listed, which is the whole point of it. */
	private static final class CountingStore implements StoreIntrospection {

		private final String collection;
		private final boolean validateOnWrite;
		private final List<String> ids;
		final AtomicInteger listings = new AtomicInteger();

		CountingStore(String collection, boolean validateOnWrite, List<String> ids) {
			this.collection = collection;
			this.validateOnWrite = validateOnWrite;
			this.ids = ids;
		}

		@Override
		public String collection() {
			return collection;
		}

		@Override
		public boolean validateOnWrite() {
			return validateOnWrite;
		}

		@Override
		public List<String> ids() {
			listings.incrementAndGet();
			return ids;
		}

		/** The expensive one, and no snapshot may reach it: calling it here is a failure. */
		@Override
		public List<IntegrityFinding> scan() {
			throw new AssertionError("scan() is the full read; nothing on the snapshot path may call it");
		}
	}
}

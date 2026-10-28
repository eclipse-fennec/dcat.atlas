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
package org.eclipse.fennec.dcat.atlas.impl.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

/** The one notification every write in the portal goes through. */
class StoreChangesTest {

	@Test
	void aListenerIsToldAboutEveryCommitUntilItIsRemoved() throws Exception {
		AtomicInteger told = new AtomicInteger();

		try (AutoCloseable handle = StoreChanges.onCommit(told::incrementAndGet)) {
			StoreChanges.committed();
			StoreChanges.committed();
			assertEquals(2, told.get());
		}

		StoreChanges.committed();
		assertEquals(2, told.get(), "a closed handle must stop the notifications");
	}

	/**
	 * A listener that throws is swallowed.
	 * <p>
	 * The notification happens after the commit is written, so letting it out would turn a
	 * write that is already durable into a failed operation — the caller would retry
	 * something that already happened.
	 */
	@Test
	void aListenerThatThrowsCannotFailTheWriteThatAlreadyHappened() throws Exception {
		AtomicInteger told = new AtomicInteger();

		try (AutoCloseable boom = StoreChanges.onCommit(() -> {
			throw new IllegalStateException("bookkeeping went wrong");
		}); AutoCloseable good = StoreChanges.onCommit(told::incrementAndGet)) {
			assertDoesNotThrow(StoreChanges::committed);
			assertEquals(1, told.get(), "the listener after the failing one is still told");
		}
	}
}

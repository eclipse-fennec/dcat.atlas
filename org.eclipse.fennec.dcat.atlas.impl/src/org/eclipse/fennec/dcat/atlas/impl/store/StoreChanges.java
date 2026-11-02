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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Tells whoever is listening that a write landed.
 *
 * <p>
 * <strong>Why here, and why static.</strong> The runtime service has to be informed of change
 * rather than go looking for it: a snapshot that polled the store would re-read the tree once
 * per gauge per export interval. Every write in this portal — through any of the ten stores,
 * whether it rewrites one resource or five — ends in exactly one place,
 * {@code DcatStoreSession#commit(String)}, so that is the one call site that covers all of
 * them. A listener service threaded through ten components would put the same notification in
 * ten places and let a new store forget it.
 * </p>
 *
 * <p>
 * The listeners are held statically because the notifier and its one listener are in the same
 * bundle and the same class loader, and because {@code DcatStoreSession} is created per operation and
 * owns nothing that outlives it. Nothing outside this bundle can register.
 * </p>
 */
public final class StoreChanges {

	private static final List<Runnable> LISTENERS = new CopyOnWriteArrayList<>();

	private StoreChanges() {
	}

	/**
	 * Registers a listener, called after a commit has been written.
	 *
	 * @param listener what to call; must not throw
	 * @return a handle that removes it again
	 */
	public static AutoCloseable onCommit(Runnable listener) {
		LISTENERS.add(listener);
		return () -> LISTENERS.remove(listener);
	}

	/**
	 * A commit landed.
	 * <p>
	 * A listener that throws is swallowed: bookkeeping must never turn a write that has
	 * already been committed into a failed operation.
	 */
	static void committed() {
		for (Runnable listener : LISTENERS) {
			try {
				listener.run();
			} catch (RuntimeException swallowed) {
				// Deliberately ignored - see above. The write is done either way.
			}
		}
	}
}

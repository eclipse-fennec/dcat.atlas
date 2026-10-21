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

/**
 * What one store can say about itself, for the runtime service.
 *
 * <p>
 * Internal to this bundle and deliberately not API: it exists because
 * {@code validateOnWrite} is configured once per store — ten times over — and two of them
 * disagreeing is a state nothing else makes visible. A snapshot that read the configuration
 * once would report the setting rather than the disagreement.
 * </p>
 *
 * <p>
 * {@link #ids()} is a listing and never loads an entity: the runtime snapshot is taken once
 * per gauge per export interval, and a snapshot proportional to the size of the portal would
 * cost more than everything it reports.
 * </p>
 */
public interface StoreIntrospection {

	/** The collection this store holds, as its path segment — e.g. {@code datasets}. */
	String collection();

	/** What this store was configured to do on a write. */
	boolean validateOnWrite();

	/** The ids it holds, from the store's listing. Empty when the store cannot be read. */
	List<String> ids();

	/**
	 * Reads every resource in this collection and reports what is wrong with it.
	 *
	 * <p>
	 * <strong>The expensive one.</strong> Unlike {@link #ids()} this loads every entity, so
	 * it is never called from a snapshot — it is what
	 * {@code DcatAtlasRuntime.checkIntegrity()} is for, and why that is a separate method
	 * with a separate warning on it.
	 * </p>
	 *
	 * @return what is wrong, empty when nothing is
	 */
	List<IntegrityFinding> scan();
}

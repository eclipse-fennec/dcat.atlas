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
package org.eclipse.fennec.dcat.atlas.api.read;

import java.util.Optional;

/**
 * The read side of the portal, as one service.
 * <p>
 * The handlers behind it are plain objects over a single store, selected once in this
 * service's configuration. That is what keeps them from disagreeing about which store they
 * read: a portal whose catalogues came from one repository and whose datasets came from
 * another would answer every membership question wrongly and say nothing about it.
 *
 * <h2>Deployable on its own</h2>
 *
 * A portal that only publishes needs nothing from the write side, so this service does not
 * depend on it. The reverse is not true — see
 * {@code org.eclipse.fennec.dcat.atlas.api.admin.DcatAdminService}, which extends this
 * interface because anything allowed to write is allowed to read.
 */
public interface DcatReadOnlyService {

	CatalogReadOnlyHandler getCatalogReadOnlyHandler();

	DataServiceReadOnlyHandler getDataServiceReadOnlyHandler();

	DatasetReadOnlyHandler getDatasetReadOnlyHandler();

	DistributionReadOnlyHandler getDistributionReadOnlyHandler();

	DatasetSeriesReadOnlyHandler getDataSeriesReadOnlyHandler();

	/**
	 * The version of the store these handlers read, as
	 * {@link org.eclipse.fennec.dcat.atlas.api.store.DcatStoreService#revision()} defines it.
	 * <p>
	 * Surfaced here, and not left to be fetched from the store, because every consumer of it
	 * is a read consumer that already holds one of these — the collection ETags and the
	 * projection's reconciliation pass. Binding the store as well would make a JAX-RS resource
	 * depend on the persistence layer and would add a third place where a store has to be
	 * selected and the three selections made to agree.
	 */
	Optional<String> revision();

}

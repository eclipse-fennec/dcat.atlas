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
package org.eclipse.fennec.dcat.atlas.api.graph;

/**
 * The DCAT-AP entity types that have their own store and their own read/admin
 * service pair. Used to address a stored resource by type and id without
 * depending on the EMF classes — see
 * {@link DcatGraphService#invalidate(DcatEntity, String)}.
 */
public enum DcatEntity {

	CATALOG,
	DATASET,
	DATASET_SERIES,
	DATA_SERVICE,
	DISTRIBUTION;
}

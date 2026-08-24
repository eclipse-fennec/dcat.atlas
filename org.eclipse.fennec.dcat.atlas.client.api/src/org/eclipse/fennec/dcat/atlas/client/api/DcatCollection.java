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
package org.eclipse.fennec.dcat.atlas.client.api;

/**
 * The portal's four root collections, and the path segment each one lives under.
 * <p>
 * {@code Distribution} is deliberately absent: it has no collection of its own. A
 * distribution exists only in the context of its dataset
 * ({@code /admin/datasets/{datasetId}/distributions}), so it is addressed through the
 * dataset-scoped operations rather than through this enum.
 */
public enum DcatCollection {

	CATALOGS("catalogs"),
	DATASETS("datasets"),
	DATASET_SERIES("dataset-series"),
	DATA_SERVICES("data-services");

	private final String segment;

	DcatCollection(String segment) {
		this.segment = segment;
	}

	/**
	 * @return the URL path segment, e.g. {@code dataset-series} — note the hyphen,
	 *         which is why this is not derived from {@link #name()}
	 */
	public String segment() {
		return segment;
	}
}

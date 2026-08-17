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
package org.eclipse.fennec.dcat.atlas.impl.helper;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.DcatIds;

/**
 * Where a stored resource lives on disk.
 * <p>
 * The identity vocabulary itself — the logical base, the collection names, and the
 * IRI &harr; id mapping — lives in {@link DcatIds}, because the REST layer and the
 * RDF projection have to agree with the store about it. This adds only what is
 * genuinely about the filesystem, and re-exports the collection names so callers
 * that are already talking to the store need not reach past it.
 *
 * <pre>
 *   logical IRI   http://dcat.atlas/datasets/air
 *   URI map       http://dcat.atlas/datasets/  ->  file:/&lt;root&gt;/datasets/
 *   file          &lt;root&gt;/datasets/air
 * </pre>
 */
public final class StoreLayout {

	private StoreLayout() {
	}

	public static final String LOGICAL_BASE = DcatIds.LOGICAL_BASE;
	public static final String CATALOGS = DcatIds.CATALOGS;
	public static final String DATASETS = DcatIds.DATASETS;
	public static final String DATA_SERVICES = DcatIds.DATA_SERVICES;
	public static final String DATASET_SERIES = DcatIds.DATASET_SERIES;
	public static final List<String> COLLECTIONS = DcatIds.COLLECTIONS;

	public static String logicalIri(String collection, String id) {
		return DcatIds.logicalIri(collection, id);
	}

	public static String logicalBase(String collection) {
		return DcatIds.logicalBase(collection);
	}

	public static String distributionIri(String datasetId, String distributionId) {
		return DcatIds.distributionIri(datasetId, distributionId);
	}

	public static String idOf(String collection, String iri) {
		return DcatIds.idOf(collection, iri);
	}

	public static String distributionIdOf(String datasetId, String iri) {
		return DcatIds.distributionIdOf(datasetId, iri);
	}

	public static String requireSafeId(String id) {
		return DcatIds.requireSafeId(id);
	}

	/** The directory holding {@code collection} under {@code root}. */
	public static Path directory(Path root, String collection) {
		return root.resolve(collection);
	}

	/**
	 * The file backing {@code id} in {@code collection}.
	 * <p>
	 * No extension: EMF derives cross-resource hrefs from the target's resource URI,
	 * so anything appended here would surface inside a stored identity. See
	 * {@code StoreResourceSets#resourceUri}.
	 */
	public static Path file(Path root, String collection, String id) {
		return directory(root, collection).resolve(requireSafeId(id));
	}
}

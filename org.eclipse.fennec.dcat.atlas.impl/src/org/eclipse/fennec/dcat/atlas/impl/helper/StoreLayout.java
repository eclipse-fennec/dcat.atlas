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

import java.util.List;

import org.eclipse.fennec.dcat.atlas.api.identity.DcatIds;

/**
 * Where a stored resource lives in the git repository.
 * <p>
 * The identity vocabulary itself — the logical base, the collection names, and the
 * IRI &harr; id mapping — lives in {@link DcatIds}, because the REST layer and the
 * RDF projection have to agree with the store about it. This adds only what is
 * genuinely about storage, and re-exports the collection names so callers that are
 * already talking to the store need not reach past it.
 *
 * <pre>
 *   logical IRI   http://dcat.atlas/datasets/air
 *   repo path     &lt;basePath&gt;/datasets/air.xmi
 * </pre>
 *
 * <h2>Why the base path is configurable and the collection folders are not</h2>
 *
 * {@code basePath} lets the portal share a repository with unrelated content. The four
 * collection folders below it stay fixed because they are not really storage at all: they
 * are the collection segment of every stored identity ({@link DcatIds#logicalIri}) and of
 * every REST path, so renaming one is a data migration, not a setting. Making them
 * configurable would also mean every store needed the whole map rather than its own name —
 * {@code References} scans all four to find dangling links — and ten {@code @Designate}d
 * copies of that map is a way for two of them to disagree.
 *
 * <h2>Why stored blobs carry an extension and stored identities do not</h2>
 *
 * They used to have none: the store URI <em>was</em> the file path, and EMF derives the
 * href of a cross-resource reference from the target's resource URI, so an {@code .xmi}
 * would have surfaced inside a stored identity. Since the git store resolves URIs through
 * a {@code URIHandler} rather than a URI map, identity and location are separate, and the
 * blob can be named for what it is without the identity mentioning a serialization format.
 * See {@code StoreResourceSets#resourceUri}.
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

	/** The extension stored blobs carry. Not part of any identity — see the class comment. */
	public static final String EXTENSION = ".xmi";

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

	/**
	 * Normalises a configured base path to the form the other methods here expect:
	 * no leading or trailing {@code /}, and {@code ""} for the repository root.
	 *
	 * @throws IllegalArgumentException if the path is absolute, escapes upwards, or
	 *                                  contains a segment that is not a plain name
	 */
	public static String requireSafeBasePath(String basePath) {
		if (basePath == null) {
			return "";
		}
		String trimmed = basePath.strip();
		while (trimmed.startsWith("/")) {
			trimmed = trimmed.substring(1);
		}
		while (trimmed.endsWith("/")) {
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		if (trimmed.isEmpty()) {
			return "";
		}
		// A base path becomes a prefix of every stored blob's path, so a ".." in it would
		// silently file the whole store somewhere else in the repository - and a backslash
		// or a scheme would make the path mean different things to git and to a checkout.
		for (String segment : trimmed.split("/", -1)) {
			if (segment.isBlank() || segment.equals(".") || segment.equals("..") || segment.contains("\\")
					|| segment.contains(":")) {
				throw new IllegalArgumentException("Illegal store base path: " + basePath);
			}
		}
		return trimmed;
	}

	/** The path prefix every blob of {@code collection} sits directly under; ends in {@code /}. */
	public static String collectionPrefix(String basePath, String collection) {
		return basePath.isEmpty() ? collection + "/" : basePath + "/" + collection + "/";
	}

	/** The repository-relative path of the blob backing {@code id} in {@code collection}. */
	public static String repoPath(String basePath, String collection, String id) {
		return collectionPrefix(basePath, collection) + requireSafeId(id) + EXTENSION;
	}

	/**
	 * The id a repository path names in {@code collection}, or {@code null} when the path
	 * is not one of ours.
	 * <p>
	 * Deliberately strict: a listing is recursive, so anything nested deeper than the
	 * collection folder, or without the expected extension, is somebody else's file that
	 * happens to live under our prefix and must not be read as a resource.
	 */
	public static String idOfPath(String basePath, String collection, String path) {
		String prefix = collectionPrefix(basePath, collection);
		if (path == null || !path.startsWith(prefix) || !path.endsWith(EXTENSION)) {
			return null;
		}
		String id = path.substring(prefix.length(), path.length() - EXTENSION.length());
		return DcatIds.isSafeId(id) ? id : null;
	}
}

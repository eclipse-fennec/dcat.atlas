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

import org.eclipse.fennec.dcat.atlas.api.read.DistributionReadOnlyService;

/**
 * Maintains the in-memory RDF graph that backs the SPARQL endpoint (WP-DCAT-5,
 * Phase 1 of the persistence plan).
 * <p>
 * The graph is a <em>disposable projection</em> of the file store, never a second
 * store of record: the files stay authoritative and the graph can be thrown away
 * and rebuilt at any time. Nothing here accepts RDF or model content — callers
 * only report <em>which</em> resource changed and the implementation re-reads it
 * through the ordinary read services. That keeps the graph a function of what is
 * actually on disk (guiding constraint G1) and makes every update idempotent (G3),
 * so retries, duplicate notifications and reconciliation passes are all harmless.
 * <p>
 * Implementations must be safe for concurrent use: SPARQL reads race admin writes
 * by nature.
 */
public interface DcatGraphService {

	/**
	 * Brings the graph back in line with the store for a single resource, whatever
	 * state either is in: the resource is re-read and its named graph replaced, or
	 * — if it is no longer in the store — the named graph is dropped.
	 * <p>
	 * Called from the persistence boundary after a successful write or delete
	 * (constraint G2), so that a mutation made through the OSGi admin services
	 * without going through REST keeps the graph correct too.
	 * <p>
	 * Must never throw on account of the graph: once the file is written the write
	 * has succeeded, and a failed projection is recoverable through
	 * {@link #rebuild()} and the periodic reconciliation. Implementations log and
	 * carry on.
	 *
	 * @param entity the entity type whose store holds the resource
	 * @param id     the storage id of the resource
	 */
	default void invalidate(DcatEntity entity, String id) {
		invalidate(entity, null, id);
	}

	/**
	 * As {@link #invalidate(DcatEntity, String)}, for a resource that is only
	 * reachable through an owning resource.
	 * <p>
	 * A {@link DcatEntity#DISTRIBUTION} exists only in the context of a dataset
	 * (FR-10) and {@code DistributionReadOnlyService} will only resolve it when the
	 * dataset still references it, so re-reading one requires the dataset id. For
	 * every other entity type {@code parentId} is {@code null}.
	 *
	 * @param entity   the entity type whose store holds the resource
	 * @param parentId the owning dataset id for a distribution, otherwise {@code null}
	 * @param id       the storage id of the resource
	 */
	void invalidate(DcatEntity entity, String parentId, String id);

	/**
	 * Discards the graph and rebuilds it from the store. This is the recovery path
	 * for every way the projection can drift, and it is what makes losing the graph
	 * a non-event. Runs asynchronously; {@link #isReady()} reports completion.
	 */
	void rebuild();

	/**
	 * Whether the graph has finished its initial build and may be queried.
	 * <p>
	 * This must gate the SPARQL endpoint. An incomplete graph does not fail
	 * queries — it answers them <em>successfully, with too few results</em>, which
	 * is the more dangerous outcome: the same failure class as a portal serving
	 * unvalidated data because no SHACL shapes were mounted.
	 */
	boolean isReady();
}

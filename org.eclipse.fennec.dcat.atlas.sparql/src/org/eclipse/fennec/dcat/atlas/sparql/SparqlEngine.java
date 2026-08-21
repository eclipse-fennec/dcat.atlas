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
package org.eclipse.fennec.dcat.atlas.sparql;

import java.util.function.Function;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;

/**
 * Query side of the in-memory projection. Bundle-internal: it is a second service
 * interface of the graph component rather than exported API, so that Jena query
 * types stay inside this bundle.
 * <p>
 * Kept separate from
 * {@link org.eclipse.fennec.dcat.atlas.api.graph.DcatGraphService} because the two have
 * different audiences — the persistence boundary maintains the graph, the REST
 * endpoint queries it — and nothing should need both.
 */
public interface SparqlEngine {

	/** Whether SPARQL is switched on at all ({@code GraphConfig.enabled}). */
	boolean isEnabled();

	/** Whether the initial build has completed and queries may run. */
	boolean isReady();

	/**
	 * Runs {@code query} against the projection inside a read transaction, with the
	 * configured timeout applied, and hands the live {@link QueryExecution} to
	 * {@code consumer}.
	 * <p>
	 * The consumer must fully materialize whatever it needs — the transaction and
	 * the execution are both closed when this returns, so a lazily-consumed
	 * {@code ResultSet} would be read after close. Bounded by the configured row
	 * cap, so materializing is safe.
	 */
	<T> T execute(Query query, Function<QueryExecution, T> consumer);

	/**
	 * The row cap to apply to a SELECT, or {@code 0} for none. Exposed so the
	 * endpoint can report truncation rather than silently returning a short answer.
	 */
	long maxResultRows();
}

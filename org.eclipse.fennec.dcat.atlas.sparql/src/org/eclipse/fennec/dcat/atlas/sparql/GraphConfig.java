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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configuration for the in-memory RDF projection and the SPARQL endpoint
 * (persistence plan P1-9).
 */
@ObjectClassDefinition(name = "DCAT.Atlas SPARQL graph", description = "In-memory RDF projection of the file store, and the SPARQL endpoint over it")
public @interface GraphConfig {

	/**
	 * Turning the endpoint off also stops the graph being built, so a deployment
	 * that does not want SPARQL pays neither the memory nor the startup cost.
	 */
	@AttributeDefinition(name = "Enabled", description = "Whether to build the graph and serve SPARQL at all")
	boolean enabled() default true;

	/**
	 * Wall-clock budget for a single query. Guiding constraint G4 (queries are
	 * expected to be 'rooted') is an expectation, not a guard — ARQ will happily
	 * execute anything, so the timeout is the actual protection.
	 */
	@AttributeDefinition(name = "Query timeout (ms)", description = "Maximum execution time for one SPARQL query; 0 disables the limit")
	long queryTimeoutMillis() default 30_000L;

	/**
	 * Upper bound on rows returned by a SELECT. Applied by wrapping the query in a
	 * LIMIT, so an over-large result is truncated rather than refused.
	 */
	@AttributeDefinition(name = "Maximum result rows", description = "Row cap applied to SELECT results; 0 disables the cap")
	long maxResultRows() default 10_000L;

	/**
	 * How often to compare the graph against the store and repair any drift. The
	 * check re-reads the stores, so it is not free; it exists to bound how long the
	 * projection can be silently wrong, not to be run every second.
	 */
	@AttributeDefinition(name = "Reconcile interval (s)", description = "Interval for the store/graph reconciliation pass; 0 disables it")
	long reconcileIntervalSeconds() default 300L;
}

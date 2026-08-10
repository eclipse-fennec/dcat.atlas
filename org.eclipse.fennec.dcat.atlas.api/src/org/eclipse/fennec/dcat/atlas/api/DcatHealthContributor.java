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
package org.eclipse.fennec.dcat.atlas.api;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * One contribution to the portal's readiness signal (F-25).
 * <p>
 * Registered as an OSGi service by whatever component owns the subsystem being
 * reported on — a store, the SHACL validation, later the in-memory SPARQL graph.
 * The readiness endpoint aggregates every registered contributor, so a new
 * subsystem becomes visible by registering one and needs no change to the
 * endpoint itself.
 * <p>
 * Contributors report on their <em>own</em> state only and must be cheap: the
 * readiness endpoint is polled by an orchestrator, so no contributor may perform
 * expensive or blocking work.
 *
 * @since Aug 10, 2026
 */
@ConsumerType
public interface DcatHealthContributor {

	/**
	 * Stable, short identifier of the reported subsystem, e.g. {@code store:catalogs}
	 * or {@code shacl}. Appears verbatim in the readiness response, so keep it stable
	 * across versions — operators alert on it.
	 */
	String name();

	/**
	 * Whether this subsystem is fit to serve traffic. A single {@code false} makes the
	 * whole portal report not-ready (HTTP 503).
	 * <p>
	 * "Deliberately not configured" is not a failure: an optional subsystem that is
	 * absent by choice is ready, and says so through {@link #detail()}. Only a
	 * subsystem that was <em>asked</em> to work and cannot is unready.
	 */
	boolean ready();

	/**
	 * Human-readable explanation for an operator reading the response — what was
	 * checked and what was found. Never {@code null}.
	 */
	String detail();

}

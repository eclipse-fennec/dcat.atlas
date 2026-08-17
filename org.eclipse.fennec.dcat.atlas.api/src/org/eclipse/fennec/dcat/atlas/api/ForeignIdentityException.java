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

/**
 * A write supplied an {@code rdf:about} that names no resource of ours, or one we
 * could not file.
 *
 * <h2>Why this is refused rather than replaced</h2>
 *
 * A store cannot honour an identity it does not own: filing an entity under the last
 * path segment of somebody else's URL would quietly claim a resource that is not ours.
 * The alternative to refusing is what this codebase used to do — mint a fresh id and
 * overwrite the {@code about} — which tells the caller nothing, so it believes the
 * identity it chose was kept and only discovers otherwise by reading back a resource
 * under a name it has never seen. Only an <em>absent</em> {@code about} mints, because
 * that is the request that asks us to choose.
 *
 * <h2>Thrown by the service, not only by REST</h2>
 *
 * The rule belongs to the persistence boundary, so it holds for every caller — an
 * importer, a migration script, another bundle — and not merely for requests that
 * happen to arrive over HTTP. It used to live only in the REST adapter, which meant
 * {@code POST /admin/datasets} answered {@code 400} while the same body passed straight
 * to {@code upsertDataset} was accepted and silently stored under a minted id. That
 * asymmetry is how {@code SparqlEndpointIntegrationTest} leaked a dataset per run for a
 * day without anyone noticing.
 * <p>
 * An {@link IllegalArgumentException} because it is exactly that: the argument names an
 * identity this store cannot use. The REST layer maps it to {@code 400} through
 * {@code ForeignIdentityExceptionMapper}; a specific type rather than
 * {@code IllegalArgumentException} itself, so that genuine argument bugs keep surfacing
 * as {@code 500} instead of being relabelled as the client's fault.
 */
public class ForeignIdentityException extends IllegalArgumentException {

	private static final long serialVersionUID = 1L;

	public ForeignIdentityException(String message) {
		super(message);
	}
}

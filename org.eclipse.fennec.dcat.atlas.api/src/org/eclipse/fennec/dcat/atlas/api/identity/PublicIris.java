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
package org.eclipse.fennec.dcat.atlas.api.identity;

/**
 * Translates between the identity a resource is <em>stored</em> under and the one
 * it is <em>served</em> under.
 *
 * <h2>Why the two differ</h2>
 *
 * {@code rdf:about} is a resource's identity in RDF — what a harvester ingests as
 * canonical. Deriving it from the incoming request URL froze the writing host into
 * the stored file, so data was not portable between environments and, behind a
 * reverse proxy without {@code X-Forwarded-*} handling, recorded an internal
 * address no client could dereference. Stored identities are therefore
 * deployment-independent, and the public one is computed at render time.
 *
 * <h2>What gets translated</h2>
 *
 * Only identities under a base we own. Publisher IRIs, EU vocabulary terms and
 * licence URIs pass through <em>verbatim</em> — decided structurally, from the IRI
 * itself, rather than from a list someone has to remember to update.
 * <p>
 * Ownership is matched on path-segment boundaries, never raw string prefixes:
 * {@code https://example.org/dcat/} must not claim
 * {@code https://example.org/dcatalog/air}.
 */
public interface PublicIris {

	/** Renders a stored identity for a client; foreign IRIs are returned unchanged. */
	String toPublic(String iri);

	/** Folds a client-supplied identity back to the stored form; foreign IRIs unchanged. */
	String toLogical(String iri);

	/** Whether {@code iri} is one of ours, in either form. */
	boolean isOwned(String iri);

	/** The base clients dereference under; always ends in {@code /}. */
	String publicBase();
}

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

import java.util.List;
import java.util.UUID;

/**
 * The mapping between a resource's {@code rdf:about} and its id.
 * <p>
 * This lives in the API because more than one component has to agree on it: the
 * store derives file names from it, the REST layer stamps it on writes, and the RDF
 * projection uses {@code about} as a named graph while being told which id changed.
 * Two independent copies of this rule would drift.
 *
 * <h2>The identity is logical</h2>
 *
 * Identities are minted under {@link #LOGICAL_BASE}, never from the incoming
 * request URL. Deriving them from the request froze the writing host into the
 * stored file, so data was not portable between environments and, behind a reverse
 * proxy without {@code X-Forwarded-*} handling, recorded an internal address no
 * client could dereference. What clients see is computed at render time by
 * {@link PublicIris}.
 */
public final class DcatIds {

	private DcatIds() {
	}

	/**
	 * The base every stored identity is minted under. Deliberately not configurable:
	 * it is internal, never served, and making it configurable would only create a way
	 * for two deployments to disagree about what the same resource is called.
	 */
	public static final String LOGICAL_BASE = "http://dcat.atlas/";

	/** How much of a caller-supplied {@code about} a refusal will quote back. */
	private static final int MAX_ECHOED = 200;

	public static final String CATALOGS = "catalogs";
	public static final String DATASETS = "datasets";
	public static final String DATA_SERVICES = "data-services";
	public static final String DATASET_SERIES = "dataset-series";

	/**
	 * Collections that are stored in their own right. Distributions are absent by
	 * design: {@code dcat:distribution} is containment, so a Distribution lives inside
	 * its Dataset (FR-10) and its identity nests accordingly.
	 */
	public static final List<String> COLLECTIONS = List.of(CATALOGS, DATASETS, DATA_SERVICES, DATASET_SERIES);

	/** The path segment distributions dereference under, within their dataset. */
	public static final String DISTRIBUTIONS = "distributions";

	/** The logical base of {@code collection}; ends in {@code /}. */
	public static String logicalBase(String collection) {
		return LOGICAL_BASE + collection + "/";
	}

	/** The logical identity of {@code id} in {@code collection}. */
	public static String logicalIri(String collection, String id) {
		return logicalBase(collection) + requireSafeId(id);
	}

	/** The logical identity of a Distribution, nested under its Dataset. */
	public static String distributionIri(String datasetId, String distributionId) {
		return logicalIri(DATASETS, datasetId) + "/" + DISTRIBUTIONS + "/" + requireSafeId(distributionId);
	}

	/**
	 * The id {@code iri} names within {@code collection}, or {@code null} if it names
	 * none.
	 * <p>
	 * Deliberately narrow: it returns {@code null} for anything not under this
	 * collection's logical base. Taking the last path segment of an arbitrary IRI
	 * would let an entity whose {@code about} is somebody else's URL be filed under an
	 * id carved out of that URL — quietly claiming a resource we do not own.
	 */
	public static String idOf(String collection, String iri) {
		return tailUnder(iri, logicalBase(collection));
	}

	/** The distribution id {@code iri} names within {@code datasetId}, or {@code null}. */
	public static String distributionIdOf(String datasetId, String iri) {
		if (datasetId == null) {
			return null;
		}
		return tailUnder(iri, logicalIri(DATASETS, datasetId) + "/" + DISTRIBUTIONS + "/");
	}

	/**
	 * Ensures an id cannot escape its store directory, and — because the id also
	 * becomes a path segment of the resource's IRI — cannot smuggle in a fragment or
	 * query that would change what the identity means.
	 */
	public static String requireSafeId(String id) {
		if (!isSafeId(id)) {
			throw new IllegalArgumentException("Illegal id: " + id);
		}
		return id;
	}

	/**
	 * Whether {@code id} is one {@link #requireSafeId} would accept.
	 * <p>
	 * For callers that derive an id from client input and must decide what to do about
	 * an unusable one, rather than be thrown at: {@link #idOf} only refuses a tail
	 * containing a slash, so {@code …/catalogs/gov#frag} and {@code …/catalogs/..} still
	 * come back as ids that cannot be used.
	 */
	public static boolean isSafeId(String id) {
		return id != null && !id.isBlank() && !id.contains("/") && !id.contains("\\") && !id.contains("..")
				&& !id.contains("#") && !id.contains("?");
	}

	/**
	 * The id a write stores {@code about} under in {@code collection}: the one it names
	 * when that is an identity of ours, a fresh one when it names nothing at all, and a
	 * {@link ForeignIdentityException} otherwise.
	 * <p>
	 * This is the whole identity rule for a write, in one place because both the store
	 * and the REST adapter have to apply it and two copies would drift — they already
	 * had, which is the point of {@link ForeignIdentityException}. The adapter catches
	 * the exception to render {@code 400}; the store lets it out.
	 */
	public static String idForWrite(String collection, String about) {
		return idForWrite(collection, collection, about, idOf(collection, about));
	}

	/**
	 * The same for a Distribution, whose identity nests inside its Dataset (FR-10)
	 * rather than sitting in a collection of its own.
	 */
	public static String distributionIdForWrite(String datasetId, String about) {
		return idForWrite(DATASETS + "/" + datasetId + "/" + DISTRIBUTIONS, DATASETS + "/" + datasetId + "/"
				+ DISTRIBUTIONS, about, distributionIdOf(datasetId, about));
	}

	/**
	 * @param path    how this collection is named back to the caller
	 * @param derived the id {@code about} names here, or {@code null} if it names none
	 */
	private static String idForWrite(String path, String urlPath, String about, String derived) {
		if (about == null || about.isBlank()) {
			return UUID.randomUUID().toString();
		}
		if (derived == null) {
			throw new ForeignIdentityException("about " + echoed(about) + " is not an identity of " + path
					+ ". Omit about to have one minted, or name a resource under /" + urlPath + "/.");
		}
		if (!isSafeId(derived)) {
			// idOf only refuses a tail containing a slash, so a fragment or a ".." reaches
			// here; requireSafeId would otherwise throw untyped, out of the write itself.
			throw new ForeignIdentityException("about " + echoed(about)
					+ " does not name an id we can file: an id may not be blank or contain '/', '\\', '..', '#' or '?'.");
		}
		return derived;
	}

	/** A caller's own {@code about} quoted back at it, bounded so a huge one cannot fill a log. */
	private static String echoed(String about) {
		return about.length() <= MAX_ECHOED ? about : about.substring(0, MAX_ECHOED) + "…";
	}

	private static String tailUnder(String iri, String base) {
		if (iri == null || !iri.startsWith(base)) {
			return null;
		}
		String tail = iri.substring(base.length());
		return tail.isBlank() || tail.contains("/") ? null : tail;
	}
}

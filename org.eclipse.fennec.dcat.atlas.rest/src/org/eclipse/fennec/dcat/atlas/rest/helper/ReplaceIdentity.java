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
package org.eclipse.fennec.dcat.atlas.rest.helper;

import org.eclipse.fennec.dcat.atlas.api.DcatIds;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import rdf.IdentifiedResource;

/**
 * The identity rule for a {@code PUT}: the body's {@code rdf:about} must be absent, or name
 * the very resource the path addresses.
 *
 * <h2>Why a {@code PUT} needs a rule at all, when it ignores the body's {@code about}</h2>
 *
 * A {@code PUT} takes its identity from the path and always did — it stamps the logical IRI
 * of {@code {id}} over whatever the body carried (D1/D2, replace-only F-17), so nothing was
 * ever stored under a client-supplied identity here. That is precisely the problem: the
 * request was answered {@code 200} as though the body had been stored verbatim, while an
 * {@code about} naming somebody else's URL — or, worse, naming a <em>different</em> resource
 * of ours — was silently dropped. {@link CreateIdentity} refuses both on the create door;
 * leaving them accepted here would only mean clients learn the rule from whichever door they
 * happen to use first.
 * <p>
 * Nothing becomes unreachable by refusing them. The path already says which resource is
 * meant, so an {@code about} is never needed to address one: omit it, or send back the one
 * that was served. An entity whose natural IRI belongs to somebody else is created by
 * {@code PUT}ting it to an id of our choosing (this is an upsert — {@code 201} when the id
 * was free), and its original IRI belongs in {@code dct:identifier} or
 * {@code adms:identifier}, which are data and are never rewritten.
 * <p>
 * As on a create, only the entity being stored is subject to this. Contained resources —
 * {@code dct:publisher}, {@code dct:license} and the rest — keep the identities they arrive
 * with.
 */
public final class ReplaceIdentity {

	/** How much of a client-supplied {@code about} an error message will quote back. */
	private static final int MAX_ECHOED = 200;

	private ReplaceIdentity() {
	}

	/**
	 * Checks the body's {@code about} against {@code collection/id} and, when they agree (or
	 * the body names no identity at all), stamps the logical identity the entity will be
	 * stored under.
	 *
	 * @return the response refusing the write, or {@code null} when it may proceed
	 */
	public static ResponseBuilder stamp(String collection, String id, IdentifiedResource entity) {
		if (!DcatIds.isSafeId(id)) {
			return unusablePathId(collection + "/" + id);
		}
		ResponseBuilder refusal = check(collection, id, DcatIds.idOf(collection, aboutOf(entity)), aboutOf(entity));
		if (refusal != null) {
			return refusal;
		}
		stampOn(entity, DcatIds.logicalIri(collection, id));
		return null;
	}

	/**
	 * The same for a Distribution, whose identity nests inside its Dataset (FR-10). "Not
	 * ours" therefore includes another dataset's distribution: the body would otherwise have
	 * been filed under this one with its identity rewritten.
	 */
	public static ResponseBuilder stampDistribution(String datasetId, String distributionId,
			IdentifiedResource entity) {
		String path = DcatIds.DATASETS + "/" + datasetId + "/" + DcatIds.DISTRIBUTIONS;
		if (!DcatIds.isSafeId(datasetId) || !DcatIds.isSafeId(distributionId)) {
			return unusablePathId(path + "/" + distributionId);
		}
		String about = aboutOf(entity);
		ResponseBuilder refusal = check(path, distributionId, DcatIds.distributionIdOf(datasetId, about), about);
		if (refusal != null) {
			return refusal;
		}
		stampOn(entity, DcatIds.distributionIri(datasetId, distributionId));
		return null;
	}

	/**
	 * The rule itself. Both refusals name resources collection-relatively, never by their
	 * logical IRI — that base is internal and no filter would rebase it out of a plain-text
	 * body (the same reasoning as in {@link CreateIdentity}).
	 *
	 * @param derived the id the body's {@code about} names here, or {@code null} for none
	 */
	private static ResponseBuilder check(String path, String id, String derived, String about) {
		if (about == null || about.isBlank()) {
			return null;
		}
		if (derived == null) {
			return refuse("about " + echoed(about) + " is not an identity of " + path + ". Omit about, or name "
					+ path + "/" + id + " — the resource this request addresses.");
		}
		if (!derived.equals(id)) {
			return refuse("about " + echoed(about) + " names " + path + "/" + derived + ", but this request addresses "
					+ path + "/" + id + ". Omit about, or make the two agree.");
		}
		return null;
	}

	/**
	 * A path segment that could never be an id (a {@code ..} or an encoded {@code #} that
	 * survived routing). {@code requireSafeId} would otherwise throw out of the stamp below
	 * and surface as a 500, and the request is the client's to fix.
	 */
	private static ResponseBuilder unusablePathId(String path) {
		return refuse(path + " is not a usable resource path: an id may not be blank or contain '/', '\\', '..', "
				+ "'#' or '?'.");
	}

	private static ResponseBuilder refuse(String message) {
		return Response.status(Status.BAD_REQUEST).entity(message).type(MediaType.TEXT_PLAIN);
	}

	private static String echoed(String about) {
		return about.length() <= MAX_ECHOED ? about : about.substring(0, MAX_ECHOED) + "…";
	}

	private static String aboutOf(IdentifiedResource entity) {
		return entity == null ? null : entity.getAbout();
	}

	/** A {@code null} entity is left to the service to reject, as the write path already does. */
	private static void stampOn(IdentifiedResource entity, String about) {
		if (entity != null) {
			entity.setAbout(about);
		}
	}
}

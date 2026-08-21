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

import java.net.URI;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.eclipse.fennec.dcat.atlas.api.DcatIds;
import org.eclipse.fennec.dcat.atlas.api.ForeignIdentityException;
import org.eclipse.fennec.dcat.atlas.api.PublicIris;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import rdf.IdentifiedResource;

/**
 * The identity a {@code POST} stores an entity under — or the refusal that says why it
 * will not be stored at all.
 *
 * <h2>Why a create has to read the body's {@code about}</h2>
 *
 * A create used to mint a fresh UUID unconditionally and overwrite whatever identity the
 * client sent. That made the same request sent twice — a retry, a double-click, a
 * re-submitted export — produce two resources describing one thing, both answered
 * {@code 201}, with no way for the client to notice. Minting first cannot be repaired by
 * adding a conflict check afterwards: nothing is ever stored under the id the client named,
 * so the second request finds nothing in its way either.
 * <p>
 * So a client-supplied identity under our own base is honoured, and a second request
 * naming it is refused with {@code 409}. This is the rule {@code PUT} applies to the id in
 * its path, and — since the add-member endpoints were folded onto this helper — the rule
 * every door that writes an entity now applies.
 *
 * <h2>An {@code about} that is not ours is refused, not replaced</h2>
 *
 * An {@code about} naming somebody else's URL, or a would-be id we could not file under
 * (a fragment, a path, a {@code ..}), earns {@code 400}. It used to mint instead, which
 * meant a client was told {@code 201} while the identity it had chosen was silently
 * dropped, and — carrying nothing we can recognise on a second attempt — such a body was
 * the one case the conflict rule could not protect. Refusing it says so, rather than
 * leaving the client to discover the duplicate later. Only an <em>absent</em>
 * {@code about} mints, which is the request that asks us to choose an identity.
 * <p>
 * This governs the entity being stored, and nothing else in the body. The contained
 * resources that come with it — {@code dct:publisher}, {@code dct:license},
 * {@code dcat:contactPoint} and the rest — are expected to carry external identities and
 * keep them untouched; refusing those would make it impossible to say who published a
 * dataset. {@link DcatIds#idForWrite} is only ever asked about the root.
 * <p>
 * The rule itself lives in {@link DcatIds}, not here: the store applies it too, and when
 * this adapter owned it privately the two drifted — a foreign {@code about} was a
 * {@code 400} over HTTP and a silent mint through {@code upsertDataset}. What is left here
 * is the part that is genuinely REST's: turning a refusal into a status code.
 * <p>
 * Note that a client refers to our resources by the <em>public</em> IRIs it was served, not
 * the logical ones. Those are folded back to the logical form before a resource method ever
 * runs (see {@code PublicIriFilter#aroundReadFrom}), so both forms are recognised here
 * without this helper knowing the public base at all.
 *
 * <h2>A conflict says where the resource it collided with is</h2>
 *
 * The {@code 409} carries the same {@code Location} header the {@code 201} would have: the
 * read URL of the resource already holding that identity. A client that POSTs a catalog and
 * gets a conflict then has the one thing it came for — the URL, and with it the id it needs
 * to go on and {@code POST .../catalogs/{id}/datasets} — without parsing the message or
 * guessing how the id was derived from its {@code about}. That matters most where the client
 * did <em>not</em> choose the id in a form it can predict, and it saves the extra
 * {@code GET} otherwise needed after every retry.
 * <p>
 * Only the conflict gets one; a {@code 400} names no resource of ours to point at. The
 * header is built from the request's own base URI, exactly as {@code Response.created} does
 * on the way out — never from the logical base, which stays inside (that is also why the
 * message body still names the resource collection-relatively: a {@code text/plain} body is
 * not an entity, so no filter would rebase a logical IRI that leaked into one).
 *
 * @param id      the id the entity will be stored under, or {@code null} when refused
 * @param refusal the response refusing the write, or {@code null} when it may proceed
 */
public record CreateIdentity(String id, ResponseBuilder refusal) {

	/** Whether the write was refused; {@link #refusal()} is then the response to send. */
	public boolean refused() {
		return refusal != null;
	}

	/**
	 * Resolves the identity for a create in {@code collection} and stamps it on
	 * {@code entity}, so what follows — SHACL enforcement especially — sees the exact form
	 * that will be stored.
	 *
	 * @param exists  whether an entity is already stored under a given id in this collection
	 * @param publicIris the configured public base, for the {@code Location} a conflict points at
	 */
	public static CreateIdentity resolve(String collection, IdentifiedResource entity, Predicate<String> exists,
			PublicIris publicIris) {
		return resolve(collection, entity, exists, publicIris, () -> DcatIds.idForWrite(collection, aboutOf(entity)),
				id -> DcatIds.logicalIri(collection, id), id -> replaceAdvice(collection, id));
	}

	/**
	 * The same for a member arriving in the body of an add-member {@code POST}, which
	 * stores it in its own collection before linking it.
	 * <p>
	 * A member that already exists is refused rather than overwritten. These endpoints read
	 * as <em>attach this to that</em>, but they write the whole member, so upserting made
	 * {@code POST /admin/catalogs/{id}/datasets} quietly replace a dataset that
	 * {@code POST /admin/datasets} would have refused — and a dataset may be a member of
	 * several catalogs, series and services at once, so that replacement was never confined
	 * to the collection the request named. The refusal points at both requests that are
	 * honest about which is meant: the link, or an update through the member's own endpoint.
	 *
	 * @param membershipPath the path this membership lives under, e.g.
	 *                       {@code catalogs/gov/datasets}, used to name the link request
	 */
	public static CreateIdentity resolveMember(String collection, IdentifiedResource member, Predicate<String> exists,
			String membershipPath, PublicIris publicIris) {
		// The Location is the member's own read URL, not one under the membership path: the
		// member it collided with is a resource of that collection, and the link request the
		// message points at is spelled out there in full.
		return resolve(collection, member, exists, publicIris, () -> DcatIds.idForWrite(collection, aboutOf(member)),
				id -> DcatIds.logicalIri(collection, id), id -> memberAdvice(collection, membershipPath, id));
	}

	/**
	 * The same for a Distribution, whose identity nests inside its Dataset (FR-10) rather
	 * than sitting in a collection of its own.
	 */
	public static CreateIdentity resolveDistribution(String datasetId, IdentifiedResource distribution,
			Predicate<String> exists, PublicIris publicIris) {
		String path = DcatIds.DATASETS + "/" + datasetId + "/" + DcatIds.DISTRIBUTIONS;
		return resolve(path, distribution, exists, publicIris,
				() -> DcatIds.distributionIdForWrite(datasetId, aboutOf(distribution)),
				id -> DcatIds.distributionIri(datasetId, id), id -> replaceAdvice(path, id));
	}

	/**
	 * The one rule all of the above share: honour an identity of ours, refuse one that is
	 * taken or that we could not file, mint only when none was sent.
	 *
	 * @param path        how this collection is named in a message and a URL
	 * @param publicIris  the configured public base a conflict's {@code Location} is built on,
	 *                    so it matches the {@code about} the same resource is served with
	 * @param idForWrite  the shared identity rule for this collection; throws when refused
	 * @param logicalIri  the identity to stamp for an id, which differs for a Distribution
	 * @param takenAdvice what to tell a client whose id is already taken
	 */
	private static CreateIdentity resolve(String path, IdentifiedResource entity, Predicate<String> exists,
			PublicIris publicIris, Supplier<String> idForWrite, UnaryOperator<String> logicalIri,
			UnaryOperator<String> takenAdvice) {
		String id;
		try {
			// The identity rule itself lives in DcatIds, because the store applies it too and
			// two copies drifted once already: this adapter refused a foreign about while the
			// service under it minted silently. Here it only has to become a response.
			id = idForWrite.get();
		} catch (ForeignIdentityException e) {
			return refuse(Status.BAD_REQUEST, e.getMessage());
		}
		if (exists.test(id)) {
			return refuse(Status.CONFLICT, path + "/" + id + " already exists. " + takenAdvice.apply(id),
					PublicUri.of(publicIris, path, id));
		}
		stamp(entity, logicalIri.apply(id));
		return new CreateIdentity(id, null);
	}

	/**
	 * Refuses the write, naming the resource collection-relatively on purpose: the logical
	 * IRI is never served to clients (that is the whole point of {@code PublicIris}), and a
	 * plain-text body is not an entity, so no response filter would rebase one that leaked
	 * in here.
	 */
	private static CreateIdentity refuse(Status status, String message) {
		return refuse(status, message, null);
	}

	/**
	 * The same, pointing at the resource the write collided with. {@code location} is the
	 * public read URL, built from the configured public base — the header the {@code 201}
	 * would have carried had the identity been free, and the same URL the resource's
	 * {@code about} is served with.
	 */
	private static CreateIdentity refuse(Status status, String message, URI location) {
		ResponseBuilder refusal = Response.status(status).entity(message).type(MediaType.TEXT_PLAIN);
		if (location != null) {
			refusal.location(location);
		}
		return new CreateIdentity(null, refusal);
	}

	private static String replaceAdvice(String path, String id) {
		return "POST creates a new resource; use PUT /admin/" + path + "/" + id + " to replace this one.";
	}

	private static String memberAdvice(String collection, String membershipPath, String id) {
		return "POST stores the member it is sent; use PUT /admin/" + membershipPath + "/" + id
				+ " to link the one that exists, or PUT /admin/" + collection + "/" + id
				+ " to change it — which changes it everywhere it is referenced.";
	}

	private static String aboutOf(IdentifiedResource entity) {
		return entity == null ? null : entity.getAbout();
	}

	/** A {@code null} entity is left to the service to reject, as the write path already does. */
	private static void stamp(IdentifiedResource entity, String about) {
		if (entity != null) {
			entity.setAbout(about);
		}
	}
}

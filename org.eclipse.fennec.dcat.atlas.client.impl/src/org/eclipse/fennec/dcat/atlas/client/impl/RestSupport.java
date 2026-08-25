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
package org.eclipse.fennec.dcat.atlas.client.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.fennec.dcat.atlas.client.api.BadRequestException;
import org.eclipse.fennec.dcat.atlas.client.api.ConflictException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClientException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatModelConstraintException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatShaclException;
import org.eclipse.fennec.dcat.atlas.client.api.NotFoundException;
import org.eclipse.fennec.dcat.atlas.client.api.PreconditionFailedException;
import org.eclipse.fennec.dcat.atlas.client.api.RetryableException;
import org.eclipse.fennec.dcat.atlas.client.api.TransportException;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status.Family;

/**
 * Turns a portal response into either "fine" or the right exception.
 *
 * <h2>Statuses, and which server-side causes they merge</h2>
 *
 * The mapping below is what the portal actually puts on the wire, measured from its
 * {@code ExceptionMapper}s rather than assumed from the service layer:
 *
 * <table>
 * <caption>status to exception</caption>
 * <tr><th>422 + {@code X-SHACL-Conforms}<td>{@link DcatShaclException}
 * <tr><th>422 without it<td>{@link DcatModelConstraintException}
 * <tr><th>400<td>{@link BadRequestException}
 * <tr><th>404<td>{@link NotFoundException}
 * <tr><th>409<td>{@link ConflictException}
 * <tr><th>412<td>{@link PreconditionFailedException}
 * <tr><th>415<td>{@link TransportException}
 * <tr><th>503<td>{@link RetryableException}
 * <tr><th>other<td>{@link TransportException}
 * </table>
 *
 * <h2>The header is the discriminator, not the body</h2>
 *
 * Both {@code 422} branches can answer {@code text/plain}: the SHACL mapper takes that
 * shape when it has a violation but no report object. So media type is <em>not</em> a safe
 * way to tell a profile refusal from an OCL refusal — {@code X-SHACL-Conforms} is, because
 * only the SHACL branch sets it, on both of its shapes.
 *
 * <h2>Where the plan and the wire disagree</h2>
 *
 * The implementation plan (§6.9) suggested reusing the portal's own exception types for
 * the failures that carry only strings — {@code ResourceInUseException},
 * {@code ReferentialIntegrityException}, {@code ForeignIdentityException} — so a caller
 * would get the same type over HTTP as through the OSGi service. That turns out not to be
 * implementable from a response:
 * <ul>
 * <li><b>409 has two causes</b> — a repeat identity and a dangling reference — rendered by
 * two different mappers as {@code text/plain} with no discriminating header;</li>
 * <li><b>400 likewise</b> — a foreign {@code about} and an unconvertible query parameter.</li>
 * </ul>
 * Splitting either would mean matching on a human-readable message, which breaks the first
 * time somebody rewords it. One type per status, carrying the message verbatim, is the
 * honest mapping. Giving the portal a discriminating header would make the finer split
 * possible later without changing this class much.
 */
final class RestSupport {

	/** Set only by the SHACL branch, on both its RDF and its text/plain shape. */
	static final String SHACL_CONFORMS = "X-SHACL-Conforms";

	private static final int UNPROCESSABLE_ENTITY = 422;

	private RestSupport() {
	}

	/** {@code true} for any 2xx. */
	static boolean isSuccess(Response response) {
		return response.getStatusInfo().getFamily() == Family.SUCCESSFUL;
	}

	static boolean isNotFound(Response response) {
		return response.getStatus() == Response.Status.NOT_FOUND.getStatusCode();
	}

	static boolean isPreconditionFailed(Response response) {
		return response.getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode();
	}

	/**
	 * The exception for a response that is not a success.
	 * <p>
	 * Reads the body once, before deciding, because a JAX-RS entity stream can only be
	 * consumed once and two of the branches need it.
	 *
	 * @param response the failing response
	 * @param what     what was attempted, e.g. {@code PUT /admin/datasets/x}
	 */
	static DcatAtlasClientException statusError(Response response, String what) {
		int status = response.getStatus();
		String body = safeBody(response);
		String mediaType = response.getMediaType() == null ? null : response.getMediaType().toString();
		String detail = what + " — " + status + (body.isEmpty() ? "" : ": " + body);

		if (status == UNPROCESSABLE_ENTITY) {
			return validationError(response, what, body, mediaType);
		}
		return switch (status) {
		case 400 -> new BadRequestException(detail);
		case 404 -> new NotFoundException(detail);
		case 409 -> new ConflictException(detail);
		case 412 -> new PreconditionFailedException(detail);
		case 415 -> new TransportException(detail
				+ " (the portal accepts application/xmi on writes; application/xml is a different codec)");
		case 503 -> new RetryableException(detail
				+ " (the commit is durable in the portal; its mirror push failed and will be retried)");
		default -> new TransportException(detail);
		};
	}

	/**
	 * Which kind of {@code 422} this is. See the class comment on why the header decides.
	 */
	private static DcatAtlasClientException validationError(Response response, String what, String body,
			String mediaType) {
		if (response.getHeaderString(SHACL_CONFORMS) != null) {
			return new DcatShaclException(what + " — refused by SHACL validation (422)", body, mediaType);
		}
		return new DcatModelConstraintException(what + " — refused by a model constraint (422)", lines(body));
	}

	/** The violation lines of a {@code text/plain} constraint report, blanks dropped. */
	private static List<String> lines(String body) {
		if (body == null || body.isBlank()) {
			return List.of();
		}
		return Arrays.stream(body.split("\\R")).map(String::strip).filter(line -> !line.isEmpty()).toList();
	}

	/**
	 * The body, or empty when there is none or it cannot be read. Best effort on purpose:
	 * failing to read an error body must not replace the error with a different one.
	 */
	static String safeBody(Response response) {
		try {
			if (response.hasEntity()) {
				String body = response.readEntity(String.class);
				return body == null ? "" : body.strip();
			}
		} catch (RuntimeException e) {
			// best effort only — the status is the information that matters here
		}
		return "";
	}
}

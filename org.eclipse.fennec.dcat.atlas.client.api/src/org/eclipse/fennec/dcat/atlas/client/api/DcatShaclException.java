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
package org.eclipse.fennec.dcat.atlas.client.api;

/**
 * {@code 422} — SHACL validation refused the write: the metadata is well-formed but
 * does not conform to the DCAT-AP.de profile.
 *
 * <h2>A routine outcome, not an exception path</h2>
 *
 * SHACL enforcement is on by default in the portal, and the profile requires title,
 * description, publisher, licence and theme. A consumer that publishes what a model
 * knows about itself and nothing more will meet this on its first registration, so
 * the report has to be usable rather than merely logged.
 *
 * <h2>Why the report is raw bytes</h2>
 *
 * The portal answers with the native {@code sh:ValidationReport} in whichever RDF
 * syntax the request's {@code Accept} allowed. Parsing it would mean Jena in the
 * client, and therefore Jena in every consumer of the client — for a payload most
 * callers only log or display. So it is carried exactly as it arrived, with its
 * media type, and a caller that wants structure brings its own parser.
 * <p>
 * The report can also be {@code text/plain}: the portal takes that branch when it
 * has a violation but no report object. {@link #getReportMediaType()} says which
 * arrived, so a caller never has to guess whether the bytes are RDF.
 *
 * <h2>Not a 400</h2>
 *
 * This is {@code 422 Unprocessable Entity}, and the distinction from
 * {@link BadRequestException} matters: 400 means the request was wrong, 422 means
 * the request was fine and the metadata was not. Retrying a 400 unchanged is
 * pointless; a 422 is fixed by supplying the missing properties.
 */
public class DcatShaclException extends DcatAtlasClientException {

	private static final long serialVersionUID = 1L;

	private final String report;
	private final String reportMediaType;

	/**
	 * @param message         a short description naming the operation that was refused
	 * @param report          the validation report exactly as received; may be empty,
	 *                        never {@code null}
	 * @param reportMediaType the media type the report arrived in, e.g.
	 *                        {@code text/turtle} or {@code text/plain}
	 */
	public DcatShaclException(String message, String report, String reportMediaType) {
		super(message);
		this.report = report == null ? "" : report;
		this.reportMediaType = reportMediaType;
	}

	/**
	 * The {@code sh:ValidationReport} as it arrived, unparsed. Interpret it according
	 * to {@link #getReportMediaType()}.
	 *
	 * @return the report body; empty if the portal sent none
	 */
	public String getReport() {
		return report;
	}

	/**
	 * @return the report's media type, or {@code null} if the response carried none
	 */
	public String getReportMediaType() {
		return reportMediaType;
	}
}

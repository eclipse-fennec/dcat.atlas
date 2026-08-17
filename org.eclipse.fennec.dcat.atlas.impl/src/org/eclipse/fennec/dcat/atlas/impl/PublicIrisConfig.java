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
package org.eclipse.fennec.dcat.atlas.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/** Configuration for the logical &harr; public IRI mapping. */
@ObjectClassDefinition(name = "DCAT-AP Atlas public IRIs", description = "How stored identities are rendered to clients")
public @interface PublicIrisConfig {

	/**
	 * The base clients dereference resources under, e.g.
	 * {@code https://opendata.example.de/dcat/rest/}. Stored identities are rendered
	 * under this at read time and folded back to the logical base on write, so the
	 * same data serves correctly from any host.
	 */
	@AttributeDefinition(name = "Public base URL", description = "Base URL resources are served under")
	String publicBaseUrl() default "http://localhost:8085/dcat/rest/";

	/**
	 * Extra bases to treat as ours when folding inbound IRIs back to logical ones.
	 * <p>
	 * Empty by default and expected to stay that way: an IRI is recognised as ours
	 * structurally, by sitting under the public or logical base, and everything else
	 * — publisher IRIs, EU vocabulary terms, licences — is foreign and must pass
	 * through untouched. This exists for the two cases that rule cannot cover:
	 * absorbing data written under an old hostname, and a hostname change. A
	 * <em>required</em> list would be a silent-failure generator, so if a fresh
	 * deployment needs entries here, something else is wrong.
	 */
	@AttributeDefinition(name = "Additional owned bases", description = "Legacy or alternate bases to fold inbound; a migration aid, normally empty", required = false)
	String[] additionalOwnedBases() default {};
}

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

/**
 * Shared configuration for the git-backed DCAT-AP stores. Every entity service
 * {@code @Designate}s this same OCD.
 * <p>
 * The repository itself is not configured here: it belongs to the
 * {@code org.eclipse.fennec.jgit} {@code GitService} each store binds, so which repository,
 * which branch and which credentials are one {@code GitConfig} the stores share rather than
 * ten copies that could disagree. Point the stores at a particular one with the usual DS
 * {@code gitService.target} property.
 * <p>
 * Persistence is one XMI blob per resource, at
 * {@code <basePath>/<collection>/<id>.xmi}. Distributions have no folder of their own:
 * {@code dcat:distribution} is containment, so a Distribution is stored inside its Dataset's
 * blob (FR-10).
 */
@ObjectClassDefinition(name = "DCAT-AP Atlas git store", description = "Git-backed persistence for the DCAT-AP entity stores")
public @interface StoreConfig {

	/**
	 * The folder inside the repository the four collection folders sit under. Empty means
	 * the repository root.
	 * <p>
	 * Configurable so the portal can share a repository with unrelated content; the
	 * collection folders below it are not, because they are the collection segment of
	 * every stored identity rather than a storage detail — see
	 * {@link org.eclipse.fennec.dcat.atlas.impl.helper.StoreLayout}.
	 */
	@AttributeDefinition(name = "Store base path", description = "Folder inside the repository holding the per-collection store folders", required = false)
	String basePath() default "dcat";

	/**
	 * Whether a write is checked against the model's own constraints — the ecore
	 * multiplicities and the OCL invariants annotated on it — and refused if it
	 * violates them.
	 * <p>
	 * Defaults to {@code true}: unlike SHACL, these constraints ship inside the model and
	 * need no operator setup, so this is the only check a deployment always has. It briefly
	 * defaulted to {@code false} because the test fixtures built entities that were not
	 * valid DCAT-AP.de — title only, no description or publisher — which would have left the
	 * whole suite exercising a path production does not take. The fixtures were corrected
	 * instead; see {@code TestEntities}.
	 * <p>
	 * Switch it off ({@code MODEL_VALIDATE=false}) to import a corpus written before a
	 * constraint existed, rather than removing the constraint.
	 */
	@AttributeDefinition(name = "Validate on write", description = "Refuse a write whose entity violates the model's OCL constraints or declared multiplicities")
	boolean validateOnWrite() default true;
}
